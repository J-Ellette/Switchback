package app.switchback.map

import android.content.Context
import android.net.Uri
import java.io.File

/** An offline map region stored on the device. */
data class Region(val name: String, val tiles: File, val header: PmtilesHeader) {
    val camera: MapCamera
        get() = MapCamera(
            lat = header.centerLat,
            lon = header.centerLon,
            zoom = header.centerZoom.coerceAtLeast(header.minZoom).toDouble(),
        )
}

/**
 * Keeps region tile files in app-private storage (filesDir/regions/<name>.pmtiles).
 * Phase 0 imports files the user picks; phase 1 adds downloads.
 */
class RegionStore(private val context: Context) {
    private val dir: File
        get() = File(context.filesDir, "regions").apply { mkdirs() }

    fun list(): List<Region> =
        dir.listFiles { f -> f.isFile && f.extension == "pmtiles" }
            .orEmpty()
            .sortedBy { it.name }
            .mapNotNull { file ->
                runCatching { Region(file.nameWithoutExtension, file, PmtilesHeader.read(file)) }.getOrNull()
            }

    /** Copies a picked .pmtiles file into app storage. Throws if it is not a valid PMTiles v3 file. */
    fun import(uri: Uri, displayName: String): Region {
        val name = displayName.substringBeforeLast('.').ifBlank { "region" }
            .replace(Regex("[^A-Za-z0-9_-]"), "_")
        val target = File(dir, "$name.pmtiles")
        val partial = File(dir, "$name.pmtiles.part")
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Could not open the selected file" }
            partial.outputStream().use { input.copyTo(it) }
        }
        val header = try {
            PmtilesHeader.read(partial)
        } catch (e: Exception) {
            partial.delete()
            throw e
        }
        if (!partial.renameTo(target)) {
            target.delete()
            check(partial.renameTo(target)) { "Could not save ${target.name}" }
        }
        return Region(name, target, header)
    }

    fun delete(region: Region) {
        region.tiles.delete()
    }
}
