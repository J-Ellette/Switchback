package app.switchback.map

import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PmtilesHeaderTest {

    private fun header(): ByteArray {
        val buf = ByteBuffer.allocate(PmtilesHeader.HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        buf.put("PMTiles".toByteArray(Charsets.US_ASCII))
        buf.put(3)
        buf.put(100, 0)
        buf.put(101, 14)
        buf.putInt(102, -1030024000) // -103.0024
        buf.putInt(106, 336158000) // 33.6158
        buf.putInt(110, -944310000) // -94.431
        buf.putInt(114, 370022000) // 37.0022
        buf.put(118, 7)
        buf.putInt(119, -986000000) // -98.6
        buf.putInt(123, 353000000) // 35.3
        return buf.array()
    }

    @Test
    fun parsesBoundsZoomAndCenter() {
        val h = PmtilesHeader.parse(header())
        assertEquals(0, h.minZoom)
        assertEquals(14, h.maxZoom)
        assertEquals(-103.0024, h.minLon, 1e-7)
        assertEquals(37.0022, h.maxLat, 1e-7)
        assertEquals(7, h.centerZoom)
        assertEquals(-98.6, h.centerLon, 1e-7)
        assertEquals(35.3, h.centerLat, 1e-7)
    }

    @Test
    fun rejectsOtherFiles() {
        val bytes = header().also { it[0] = 'X'.code.toByte() }
        assertThrows(IllegalArgumentException::class.java) { PmtilesHeader.parse(bytes) }
    }

    @Test
    fun fillsStyleTemplate() {
        val style = SwitchbackStyle.fill("""{"url":"pmtiles://file://{TILES_PATH}"}""", "/data/x.pmtiles")
        assertEquals("""{"url":"pmtiles://file:///data/x.pmtiles"}""", style)
    }
}
