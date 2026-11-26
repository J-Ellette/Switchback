package app.switchback.ui.map

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.switchback.map.TrailMap

@Composable
fun MapScreen(vm: MapViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.import(uri)
    }
    val pick = { picker.launch(arrayOf("application/octet-stream", "application/vnd.pmtiles", "*/*")) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            vm.messageShown()
        }
    }

    Box(Modifier.fillMaxSize()) {
        val region = state.selected
        val style = state.styleJson
        when {
            region != null && style != null -> {
                TrailMap(styleJson = style, camera = region.camera, modifier = Modifier.fillMaxSize())
                ExtendedFloatingActionButton(
                    onClick = pick,
                    icon = { Icon(Icons.Outlined.FileOpen, contentDescription = null) },
                    text = { Text("Add region") },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                )
            }
            state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            else -> NoRegion(onImport = pick, modifier = Modifier.align(Alignment.Center))
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun NoRegion(onImport: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("No offline map yet", style = MaterialTheme.typography.titleLarge)
        Text(
            "Build a region with the tile tool (see tools/tiles), copy the .pmtiles file to this phone, then import it here.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onImport) { Text("Import a .pmtiles file") }
    }
}
