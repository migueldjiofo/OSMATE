package com.osmate.app.ui.map

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleOpacity
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.fillColor
import org.maplibre.android.style.layers.PropertyFactory.fillOpacity
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineOpacity
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.FeatureCollection

private const val RESULT_SOURCE_ID = "osmate-search-results-source"
private const val RESULT_FILL_LAYER_ID = "osmate-search-results-fill-layer"
private const val RESULT_LINE_LAYER_ID = "osmate-search-results-line-layer"
private const val RESULT_CIRCLE_LAYER_ID = "osmate-search-results-circle-layer"
private const val EMPTY_FEATURE_COLLECTION = """{"type":"FeatureCollection","features":[]}"""

private val DEFAULT_LOCATION = LatLng(52.521918, 13.413215)

private val OSM_RASTER_STYLE = """
{
  "version": 8,
  "sources": {
    "osm-raster": {
      "type": "raster",
      "tiles": [
        "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
      ],
      "tileSize": 256,
      "attribution": "OpenStreetMap contributors"
    }
  },
  "layers": [
    {
      "id": "osm-raster-layer",
      "type": "raster",
      "source": "osm-raster"
    }
  ]
}
""".trimIndent()

@Composable
fun OsmateMapView(
    geoJson: String,
    selectedLatitude: Double?,
    selectedLongitude: Double?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapLibre.getInstance(context.applicationContext)

        MapView(context).apply {
            onCreate(Bundle())

            setOnTouchListener { view, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE,
                    -> view.parent?.requestDisallowInterceptTouchEvent(true)

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL,
                    -> view.parent?.requestDisallowInterceptTouchEvent(false)
                }

                false
            }

            getMapAsync { map ->
                map.setStyle(Style.Builder().fromJson(OSM_RASTER_STYLE)) { style ->
                    addOrUpdateSearchResults(
                        style = style,
                        geoJson = EMPTY_FEATURE_COLLECTION,
                    )

                    moveToDefaultLocation(map)
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        var isStarted = false
        var isResumed = false
        var isDestroyed = false

        fun startMapView() {
            if (!isStarted && !isDestroyed) {
                mapView.onStart()
                isStarted = true
            }
        }

        fun resumeMapView() {
            if (!isResumed && !isDestroyed) {
                mapView.onResume()
                isResumed = true
            }
        }

        fun pauseMapView() {
            if (isResumed && !isDestroyed) {
                mapView.onPause()
                isResumed = false
            }
        }

        fun stopMapView() {
            if (isStarted && !isDestroyed) {
                mapView.onStop()
                isStarted = false
            }
        }

        fun destroyMapViewOnce() {
            if (!isDestroyed) {
                pauseMapView()
                stopMapView()
                mapView.onDestroy()
                isDestroyed = true
            }
        }

        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            startMapView()
        }

        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            resumeMapView()
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> startMapView()
                Lifecycle.Event.ON_RESUME -> resumeMapView()
                Lifecycle.Event.ON_PAUSE -> pauseMapView()
                Lifecycle.Event.ON_STOP -> stopMapView()
                Lifecycle.Event.ON_DESTROY -> destroyMapViewOnce()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            destroyMapViewOnce()
        }
    }

    AndroidView(
        factory = {
            mapView
        },
        update = { view ->
            view.getMapAsync { map ->
                val style = map.style ?: return@getMapAsync

                addOrUpdateSearchResults(
                    style = style,
                    geoJson = geoJson.ifBlank {
                        EMPTY_FEATURE_COLLECTION
                    },
                )

                if (selectedLatitude != null && selectedLongitude != null) {
                    centerMapOnSelectedResult(
                        map = map,
                        latitude = selectedLatitude,
                        longitude = selectedLongitude,
                    )
                } else if (geoJson.isBlank()) {
                    moveToDefaultLocation(map)
                } else {
                    centerMapOnGeoJson(
                        map = map,
                        geoJson = geoJson,
                    )
                }
            }
        },
        modifier = modifier,
    )
}

private fun addOrUpdateSearchResults(
    style: Style,
    geoJson: String,
) {
    val featureCollection = parseFeatureCollection(geoJson)

    val source = style.getSource(RESULT_SOURCE_ID) as? GeoJsonSource

    if (source == null) {
        style.addSource(
            GeoJsonSource(
                RESULT_SOURCE_ID,
                featureCollection,
            ),
        )
    } else {
        source.setGeoJson(featureCollection)
    }

    if (style.getLayer(RESULT_FILL_LAYER_ID) == null) {
        style.addLayer(
            FillLayer(
                RESULT_FILL_LAYER_ID,
                RESULT_SOURCE_ID,
            ).withProperties(
                fillColor(Color.parseColor("#5267A3")),
                fillOpacity(0.25f),
            ),
        )
    }

    if (style.getLayer(RESULT_LINE_LAYER_ID) == null) {
        style.addLayer(
            LineLayer(
                RESULT_LINE_LAYER_ID,
                RESULT_SOURCE_ID,
            ).withProperties(
                lineColor(Color.parseColor("#5267A3")),
                lineWidth(3.0f),
                lineOpacity(0.9f),
            ),
        )
    }

    if (style.getLayer(RESULT_CIRCLE_LAYER_ID) == null) {
        style.addLayer(
            CircleLayer(
                RESULT_CIRCLE_LAYER_ID,
                RESULT_SOURCE_ID,
            ).withProperties(
                circleRadius(8.0f),
                circleColor(Color.parseColor("#5267A3")),
                circleStrokeColor(Color.WHITE),
                circleStrokeWidth(2.0f),
                circleOpacity(0.95f),
            ),
        )
    }
}

private fun parseFeatureCollection(geoJson: String): FeatureCollection {
    return runCatching {
        FeatureCollection.fromJson(
            geoJson.ifBlank {
                EMPTY_FEATURE_COLLECTION
            },
        )
    }.getOrElse {
        FeatureCollection.fromJson(EMPTY_FEATURE_COLLECTION)
    }
}

private fun moveToDefaultLocation(map: MapLibreMap) {
    map.moveCamera(
        CameraUpdateFactory.newLatLngZoom(
            DEFAULT_LOCATION,
            14.0,
        ),
    )
}

private fun centerMapOnSelectedResult(
    map: MapLibreMap,
    latitude: Double,
    longitude: Double,
) {
    map.animateCamera(
        CameraUpdateFactory.newLatLngZoom(
            LatLng(
                latitude,
                longitude,
            ),
            17.0,
        ),
    )
}

private fun centerMapOnGeoJson(
    map: MapLibreMap,
    geoJson: String,
) {
    val coordinates = extractCoordinates(geoJson)

    if (coordinates.isEmpty()) {
        moveToDefaultLocation(map)
        return
    }

    val averageLatitude = coordinates.map { coordinate ->
        coordinate.latitude
    }.average()

    val averageLongitude = coordinates.map { coordinate ->
        coordinate.longitude
    }.average()

    map.moveCamera(
        CameraUpdateFactory.newLatLngZoom(
            LatLng(
                averageLatitude,
                averageLongitude,
            ),
            15.0,
        ),
    )
}

private fun extractCoordinates(geoJson: String): List<LatLng> {
    val result = mutableListOf<LatLng>()

    try {
        val root = JSONObject(geoJson)
        val features = root.optJSONArray("features") ?: return result

        for (index in 0 until features.length()) {
            val feature = features.optJSONObject(index) ?: continue
            val geometry = feature.optJSONObject("geometry") ?: continue

            extractCoordinatesFromGeometry(
                geometry = geometry,
                result = result,
            )
        }
    } catch (_: Exception) {
        return emptyList()
    }

    return result
}

private fun extractCoordinatesFromGeometry(
    geometry: JSONObject,
    result: MutableList<LatLng>,
) {
    val type = geometry.optString("type")
    val coordinates = geometry.optJSONArray("coordinates") ?: return

    when (type) {
        "Point" -> addPosition(coordinates, result)

        "MultiPoint",
        "LineString",
        -> addPositions(coordinates, result)

        "MultiLineString",
        "Polygon",
        -> addNestedPositions(coordinates, result)

        "MultiPolygon" -> addDeepNestedPositions(coordinates, result)
    }
}

private fun addPosition(
    coordinates: JSONArray,
    result: MutableList<LatLng>,
) {
    val longitude = coordinates.optDouble(0, Double.NaN)
    val latitude = coordinates.optDouble(1, Double.NaN)

    if (!latitude.isNaN() && !longitude.isNaN()) {
        result += LatLng(
            latitude,
            longitude,
        )
    }
}

private fun addPositions(
    coordinates: JSONArray,
    result: MutableList<LatLng>,
) {
    for (index in 0 until coordinates.length()) {
        val position = coordinates.optJSONArray(index) ?: continue
        addPosition(position, result)
    }
}

private fun addNestedPositions(
    coordinates: JSONArray,
    result: MutableList<LatLng>,
) {
    for (index in 0 until coordinates.length()) {
        val positions = coordinates.optJSONArray(index) ?: continue
        addPositions(positions, result)
    }
}

private fun addDeepNestedPositions(
    coordinates: JSONArray,
    result: MutableList<LatLng>,
) {
    for (index in 0 until coordinates.length()) {
        val nestedPositions = coordinates.optJSONArray(index) ?: continue
        addNestedPositions(nestedPositions, result)
    }
}