package app.switchback.map

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

/** Where the camera should start when a style loads. */
data class MapCamera(val lat: Double, val lon: Double, val zoom: Double)

/**
 * A MapLibre map hosted in Compose. [styleJson] is a complete style; changing it reloads the map.
 */
@Composable
fun TrailMap(
    styleJson: String,
    camera: MapCamera,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mapView = remember {
        MapLibre.getInstance(context.applicationContext)
        MapView(context).apply { onCreate(Bundle()) }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    LaunchedEffect(mapView, styleJson) {
        mapView.getMapAsync { map ->
            map.setStyle(Style.Builder().fromJson(styleJson)) {
                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(camera.lat, camera.lon))
                    .zoom(camera.zoom)
                    .build()
            }
        }
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}
