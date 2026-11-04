package com.example

import com.example.data.parser.M3UParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testM3uParserExtractsChannels() {
        val sampleM3u = """
            #EXTM3U
            #EXTINF:-1 group-title="TV" tvg-id="TVI.pt" tvg-logo="https://example.com/tvi.png",TVI Portugal
            #EXTVLCOPT:http-user-agent="Mozilla/5.0"
            #EXTVLCOPT:http-referrer=https://tviplayer.iol.pt
            https://github.com/LITUATUI/M3UPT/raw/main/M3U/TVI.m3u8
            #EXTINF:-1 group-title="Filmes" tvg-id="Movie.pt" tvg-logo="https://example.com/movie.png",Cinema 1 HD
            https://example.com/stream.m3u8
        """.trimIndent()

        val channels = M3UParser.parse(sampleM3u, playlistId = 1L)
        assertTrue(channels.isNotEmpty())

        val movie = channels.firstOrNull { it.name == "Cinema 1 HD" }
        assertNotNull(movie)
        assertEquals("Filmes", movie?.groupTitle)
        assertEquals("MOVIE", movie?.categoryType)
    }

    @Test
    fun testEmergencyFallbacks() {
        val fallbacks = M3UParser.getEmergencyFallbackChannels(1L)
        assertTrue(fallbacks.isNotEmpty())
        assertTrue(fallbacks.any { it.name.contains("Zimbo") || it.name.contains("TVI") })
    }
}
