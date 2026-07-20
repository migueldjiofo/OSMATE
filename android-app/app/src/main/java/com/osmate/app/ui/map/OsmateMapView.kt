package com.osmate.app.ui.map

import android.graphics.Color
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleOpacity
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.sources.GeoJsonSource

private const val MAP_STYLE_URL = "https://demotiles.maplibre.org/style.json"
private const val RESULT_SOURCE_ID = "osmate-search-results-source"
private const val RESULT_LAYER_ID = "osmate-search-results-layer"
private const val EMPTY_FEATURE_COLLECTION = """{"type":"FeatureCollection","features":[]}"""

@Composable
fun OsmateMapView(
    geoJson: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapLibre.getInstance(context)

        MapView(context).apply {
            onCreate(Bundle())

            getMapAsync { map ->
                map.setStyle(MAP_STYLE_URL) { style ->
                    addOrUpdateSearchResults(
                        style = style,
                        geoJson = EMPTY_FEATURE_COLLECTION,
                    )
                }

                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(52.521918, 13.413215))
                    .zoom(13.0)
                    .build()
            }
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        var isDestroyed = false

        fun destroyMapViewOnce() {
            if (!isDestroyed) {
                mapView.onDestroy()
                isDestroyed = true
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
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
            if (geoJson.isNotBlank()) {
                view.getMapAsync { map ->
                    val style = map.style

                    if (style != null) {
                        addOrUpdateSearchResults(
                            style = style,
                            geoJson = geoJson,
                        )
                    }
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
    val cleanGeoJson = geoJson.ifBlank {
        EMPTY_FEATURE_COLLECTION
    }

    val source = style.getSource(RESULT_SOURCE_ID) as? GeoJsonSource

    if (source == null) {
        style.addSource(
            GeoJsonSource(
                RESULT_SOURCE_ID,
                cleanGeoJson,
            ),
        )
    } else {
        source.setGeoJson(cleanGeoJson)
    }

    if (style.getLayer(RESULT_LAYER_ID) == null) {
        val layer = CircleLayer(
            RESULT_LAYER_ID,
            RESULT_SOURCE_ID,
        )

        layer.setProperties(
            circleRadius(7.0f),
            circleColor(Color.parseColor("#5267A3")),
            circleStrokeColor(Color.WHITE),
            circleStrokeWidth(2.0f),
            circleOpacity(0.9f),
        )

        style.addLayer(layer)
    }
}
