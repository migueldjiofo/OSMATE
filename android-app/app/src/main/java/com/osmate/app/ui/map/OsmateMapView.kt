package com.osmate.app.ui.map

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleOpacity
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineOpacity
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.FeatureCollection

private const val RESULT_SOURCE_ID = "osmate-search-results-source"
private const val RESULT_CIRCLE_LAYER_ID = "osmate-search-results-circle-layer"

private const val SELECTED_SOURCE_ID = "osmate-selected-result-source"
private const val SELECTED_CIRCLE_LAYER_ID = "osmate-selected-result-circle-layer"

private const val ROUTE_SOURCE_ID = "osmate-route-source"
private const val ROUTE_LINE_LAYER_ID = "osmate-route-line-layer"

private const val USER_SOURCE_ID = "osmate-user-location-source"
private const val USER_CIRCLE_LAYER_ID = "osmate-user-location-circle-layer"

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
    routeGeoJson: String,
    selectedLatitude: Double?,
    selectedLongitude: Double?,
    userLatitude: Double?,
    userLongitude: Double?,
    userLocationFocusRequest: Int,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val lastCenteredGeoJson = remember {
        mutableStateOf<String?>(null)
    }

    val lastCenteredSelectedKey = remember {
        mutableStateOf<String?>(null)
    }

    val lastCenteredUserLocationFocusRequest = remember {
        mutableStateOf(-1)
    }

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

                    addOrUpdateRoute(
                        style = style,
                        geoJson = EMPTY_FEATURE_COLLECTION,
                    )

                    addOrUpdateSelectedResult(
                        style = style,
                        latitude = null,
                        longitude = null,
                    )

                    addOrUpdateUserLocation(
                        style = style,
                        latitude = null,
                        longitude = null,
                    )

                    moveToDefaultLocation(map)
                    lastCenteredGeoJson.value = ""
                    lastCenteredSelectedKey.value = null
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
                val normalizedGeoJson = geoJson.ifBlank {
                    EMPTY_FEATURE_COLLECTION
                }

                addOrUpdateSearchResults(
                    style = style,
                    geoJson = normalizedGeoJson,
                )

                addOrUpdateRoute(
                    style = style,
                    geoJson = routeGeoJson.ifBlank {
                        EMPTY_FEATURE_COLLECTION
                    },
                )

                addOrUpdateSelectedResult(
                    style = style,
                    latitude = selectedLatitude,
                    longitude = selectedLongitude,
                )

                addOrUpdateUserLocation(
                    style = style,
                    latitude = userLatitude,
                    longitude = userLongitude,
                )

                updateCameraIfNeeded(
                    map = map,
                    geoJson = geoJson,
                    selectedLatitude = selectedLatitude,
                    selectedLongitude = selectedLongitude,
                    lastCenteredGeoJson = lastCenteredGeoJson.value,
                    userLatitude = userLatitude,
                    userLongitude = userLongitude,
                    userLocationFocusRequest = userLocationFocusRequest,
                    lastCenteredSelectedKey = lastCenteredSelectedKey.value,
                    lastCenteredUserLocationFocusRequest = lastCenteredUserLocationFocusRequest.value,
                    onGeoJsonCentered = { centeredGeoJson ->
                        lastCenteredGeoJson.value = centeredGeoJson
                    },
                    onSelectedCentered = { selectedKey ->
                        lastCenteredSelectedKey.value = selectedKey
                    },
                    onUserLocationCentered = { focusRequest ->
                        lastCenteredUserLocationFocusRequest.value = focusRequest
                    },
                )
            }
        },
        modifier = modifier,
    )
}

private fun updateCameraIfNeeded(
    map: MapLibreMap,
    geoJson: String,
    selectedLatitude: Double?,
    selectedLongitude: Double?,
    userLatitude: Double?,
    userLongitude: Double?,
    userLocationFocusRequest: Int,
    lastCenteredGeoJson: String?,
    lastCenteredSelectedKey: String?,
    lastCenteredUserLocationFocusRequest: Int,
    onGeoJsonCentered: (String) -> Unit,
    onSelectedCentered: (String?) -> Unit,
    onUserLocationCentered: (Int) -> Unit,
) {
    val selectedKey = createSelectedKey(
        latitude = selectedLatitude,
        longitude = selectedLongitude,
    )

    if (selectedKey != null && selectedKey != lastCenteredSelectedKey) {
        centerMapOnSelectedResult(
            map = map,
            latitude = selectedLatitude ?: return,
            longitude = selectedLongitude ?: return,
        )

        onSelectedCentered(selectedKey)
        return
    }

    if (
        userLatitude != null &&
        userLongitude != null &&
        userLocationFocusRequest != lastCenteredUserLocationFocusRequest
    ) {
        centerMapOnUserLocation(
            map = map,
            latitude = userLatitude,
            longitude = userLongitude,
        )

        onUserLocationCentered(userLocationFocusRequest)
        return
    }

    if (selectedKey == null && geoJson.isBlank()) {
        if (lastCenteredGeoJson != "" || lastCenteredSelectedKey != null) {
            moveToDefaultLocation(map)
            onGeoJsonCentered("")
            onSelectedCentered(null)
        }

        return
    }

    if (selectedKey == null && geoJson.isNotBlank() && geoJson != lastCenteredGeoJson) {
        centerMapOnGeoJson(
            map = map,
            geoJson = geoJson,
        )

        onGeoJsonCentered(geoJson)
        onSelectedCentered(null)
    }
}

private fun createSelectedKey(
    latitude: Double?,
    longitude: Double?,
): String? {
    if (latitude == null || longitude == null) {
        return null
    }

    return "$latitude,$longitude"
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


private fun addOrUpdateRoute(
    style: Style,
    geoJson: String,
) {
    val featureCollection = parseFeatureCollection(geoJson)
    val source = style.getSource(ROUTE_SOURCE_ID) as? GeoJsonSource

    if (source == null) {
        style.addSource(
            GeoJsonSource(
                ROUTE_SOURCE_ID,
                featureCollection,
            ),
        )
    } else {
        source.setGeoJson(featureCollection)
    }

    if (style.getLayer(ROUTE_LINE_LAYER_ID) == null) {
        style.addLayer(
            LineLayer(
                ROUTE_LINE_LAYER_ID,
                ROUTE_SOURCE_ID,
            ).withProperties(
                lineColor(Color.parseColor("#1B8A5A")),
                lineWidth(5.0f),
                lineOpacity(0.85f),
            ),
        )
    }
}
private fun addOrUpdateSelectedResult(
    style: Style,
    latitude: Double?,
    longitude: Double?,
) {
    val selectedGeoJson = createSelectedPointGeoJson(
        latitude = latitude,
        longitude = longitude,
    )

    val featureCollection = parseFeatureCollection(selectedGeoJson)
    val source = style.getSource(SELECTED_SOURCE_ID) as? GeoJsonSource

    if (source == null) {
        style.addSource(
            GeoJsonSource(
                SELECTED_SOURCE_ID,
                featureCollection,
            ),
        )
    } else {
        source.setGeoJson(featureCollection)
    }

    if (style.getLayer(SELECTED_CIRCLE_LAYER_ID) == null) {
        style.addLayer(
            CircleLayer(
                SELECTED_CIRCLE_LAYER_ID,
                SELECTED_SOURCE_ID,
            ).withProperties(
                circleRadius(14.0f),
                circleColor(Color.parseColor("#FF7A00")),
                circleStrokeColor(Color.WHITE),
                circleStrokeWidth(4.0f),
                circleOpacity(0.95f),
            ),
        )
    }
}

private fun createSelectedPointGeoJson(
    latitude: Double?,
    longitude: Double?,
): String {
    if (latitude == null || longitude == null) {
        return EMPTY_FEATURE_COLLECTION
    }

    return """
    {
      "type": "FeatureCollection",
      "features": [
        {
          "type": "Feature",
          "properties": {
            "selected": true
          },
          "geometry": {
            "type": "Point",
            "coordinates": [$longitude, $latitude]
          }
        }
      ]
    }
    """.trimIndent()
}

private fun addOrUpdateUserLocation(
    style: Style,
    latitude: Double?,
    longitude: Double?,
) {
    val userGeoJson = createUserLocationGeoJson(
        latitude = latitude,
        longitude = longitude,
    )

    val featureCollection = parseFeatureCollection(userGeoJson)
    val source = style.getSource(USER_SOURCE_ID) as? GeoJsonSource

    if (source == null) {
        style.addSource(
            GeoJsonSource(
                USER_SOURCE_ID,
                featureCollection,
            ),
        )
    } else {
        source.setGeoJson(featureCollection)
    }

    if (style.getLayer(USER_CIRCLE_LAYER_ID) == null) {
        style.addLayer(
            CircleLayer(
                USER_CIRCLE_LAYER_ID,
                USER_SOURCE_ID,
            ).withProperties(
                circleRadius(11.0f),
                circleColor(Color.parseColor("#00A86B")),
                circleStrokeColor(Color.WHITE),
                circleStrokeWidth(4.0f),
                circleOpacity(0.95f),
            ),
        )
    }
}

private fun createUserLocationGeoJson(
    latitude: Double?,
    longitude: Double?,
): String {
    if (latitude == null || longitude == null) {
        return EMPTY_FEATURE_COLLECTION
    }

    return """
    {
      "type": "FeatureCollection",
      "features": [
        {
          "type": "Feature",
          "properties": {
            "user_location": true
          },
          "geometry": {
            "type": "Point",
            "coordinates": [$longitude, $latitude]
          }
        }
      ]
    }
    """.trimIndent()
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

private fun centerMapOnUserLocation(
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

    map.animateCamera(
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