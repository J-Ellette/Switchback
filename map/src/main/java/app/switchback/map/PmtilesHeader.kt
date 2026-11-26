package app.switchback.map

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * The parts of a PMTiles v3 header the app needs: zoom range, bounds and center.
 * Spec: https://github.com/protomaps/PMTiles/blob/main/spec/v3/spec.md
 */
data class PmtilesHeader(
    val minZoom: Int,
    val maxZoom: Int,
    val minLon: Double,
    val minLat: Double,
    val maxLon: Double,
    val maxLat: Double,
    val centerZoom: Int,
    val centerLon: Double,
    val centerLat: Double,
) {
    companion object {
        const val HEADER_SIZE = 127
        private val MAGIC = "PMTiles".toByteArray(Charsets.US_ASCII)

        fun read(file: File): PmtilesHeader {
            val bytes = ByteArray(HEADER_SIZE)
            RandomAccessFile(file, "r").use { it.readFully(bytes) }
            return parse(bytes)
        }

        fun parse(bytes: ByteArray): PmtilesHeader {
            require(bytes.size >= HEADER_SIZE) { "PMTiles header is $HEADER_SIZE bytes, got ${bytes.size}" }
            require(bytes.copyOfRange(0, 7).contentEquals(MAGIC)) { "Not a PMTiles file" }
            require(bytes[7].toInt() == 3) { "Only PMTiles version 3 is supported, got ${bytes[7]}" }

            val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            fun e7(offset: Int) = buf.getInt(offset) / 10_000_000.0
            return PmtilesHeader(
                minZoom = bytes[100].toInt() and 0xFF,
                maxZoom = bytes[101].toInt() and 0xFF,
                minLon = e7(102),
                minLat = e7(106),
                maxLon = e7(110),
                maxLat = e7(114),
                centerZoom = bytes[118].toInt() and 0xFF,
                centerLon = e7(119),
                centerLat = e7(123),
            )
        }
    }
}
