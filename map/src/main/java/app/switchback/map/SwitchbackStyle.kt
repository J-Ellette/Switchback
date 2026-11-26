package app.switchback.map

import android.content.Context
import java.io.File

/** Builds the app's map style for a local PMTiles file. */
object SwitchbackStyle {
    private const val ASSET = "style/switchback.json"
    private const val TILES_TOKEN = "{TILES_PATH}"

    fun forRegion(context: Context, tiles: File): String {
        val template = context.assets.open(ASSET).bufferedReader().use { it.readText() }
        return fill(template, tiles.absolutePath)
    }

    internal fun fill(template: String, absolutePath: String): String {
        require(TILES_TOKEN in template) { "Style template is missing $TILES_TOKEN" }
        return template.replace(TILES_TOKEN, absolutePath)
    }
}
