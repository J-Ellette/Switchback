package app.switchback.ui.map

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.switchback.map.Region
import app.switchback.map.RegionStore
import app.switchback.map.SwitchbackStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MapUiState(
    val loading: Boolean = true,
    val regions: List<Region> = emptyList(),
    val selected: Region? = null,
    val styleJson: String? = null,
    val message: String? = null,
)

class MapViewModel(app: Application) : AndroidViewModel(app) {
    private val store = RegionStore(app)
    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    private fun refresh(select: String? = null) {
        viewModelScope.launch {
            val regions = withContext(Dispatchers.IO) { store.list() }
            val selected = regions.firstOrNull { it.name == select } ?: regions.firstOrNull()
            val style = selected?.let { region ->
                withContext(Dispatchers.IO) { SwitchbackStyle.forRegion(getApplication(), region.tiles) }
            }
            _state.update { it.copy(loading = false, regions = regions, selected = selected, styleJson = style) }
        }
    }

    fun select(region: Region) = refresh(select = region.name)

    fun import(uri: Uri) {
        _state.update { it.copy(loading = true, message = null) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { store.import(uri, displayName(uri)) }
            }
            result
                .onSuccess { refresh(select = it.name) }
                .onFailure { e ->
                    _state.update { it.copy(loading = false, message = "Couldn't import that file: ${e.message}") }
                }
        }
    }

    fun messageShown() = _state.update { it.copy(message = null) }

    private fun displayName(uri: Uri): String {
        val resolver = getApplication<Application>().contentResolver
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) return c.getString(0)
        }
        return uri.lastPathSegment ?: "region.pmtiles"
    }
}
