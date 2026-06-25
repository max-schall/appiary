package io.github.max_schall.appiary.ui.screen.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.ui.AppViewModelProvider
import io.github.max_schall.appiary.ui.components.EmptyState
import io.github.max_schall.appiary.util.OsmConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBack: () -> Unit,
    onOpenApiary: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val located by viewModel.located.collectAsStateWithLifecycle()
    val context = LocalContext.current
    OsmConfig.ensure(context)

    Column(modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.map_title)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }
            },
        )

        if (located.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Map,
                title = stringResource(R.string.map_empty_title),
                subtitle = stringResource(R.string.map_empty_subtitle),
                modifier = Modifier.fillMaxSize(),
            )
            return@Column
        }

        val mapView = remember {
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(6.0)
            }
        }
        DisposableEffect(Unit) {
            mapView.onResume()
            onDispose { mapView.onPause(); mapView.onDetach() }
        }

        Box(Modifier.fillMaxSize()) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize(),
                update = { mv ->
                    mv.overlays.clear()
                    located.forEach { apiary ->
                        val marker = Marker(mv).apply {
                            position = GeoPoint(apiary.latitude, apiary.longitude)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = apiary.name
                            setOnMarkerClickListener { _, _ -> onOpenApiary(apiary.apiaryId); true }
                        }
                        mv.overlays.add(marker)
                    }
                    // Frame all markers (or center the single one).
                    if (located.size == 1) {
                        mv.controller.setCenter(GeoPoint(located[0].latitude, located[0].longitude))
                        mv.controller.setZoom(12.0)
                    } else {
                        val box = BoundingBox.fromGeoPoints(
                            located.map { GeoPoint(it.latitude, it.longitude) },
                        )
                        mv.post { mv.zoomToBoundingBox(box.increaseByScale(1.4f), false) }
                    }
                    mv.invalidate()
                },
            )
        }
    }
}
