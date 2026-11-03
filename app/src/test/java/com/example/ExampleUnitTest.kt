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
            #EXTINF:-1 group-title="TV" tvg-id="RTP1.pt" tvg-logo="https://example.com/rtp1.png",RTP 1
            #EXTVLCOPT:http-user-agent="Mozilla/5.0"
            #EXTVLCOPT:http-referrer=https://www.rtp.pt
            https://streaming-live.rtp.pt/liverepeater/smil:rtp1HD.smil/playlist.m3u8
            #EXTINF:-1 group-title="Filmes" tvg-id="Movie.pt" tvg-logo="https://example.com/movie.png",Cinema 1 HD
            https://example.com/stream.m3u8
        """.trimIndent()

        val channels = M3UParser.parse(sampleM3u, playlistId = 1L)
        assertEquals(2, channels.size)

        val rtp1 = channels[0]
        assertEquals("RTP 1", rtp1.name)
        assertEquals("TV", rtp1.groupTitle)
        assertEquals("LIVE", rtp1.categoryType)
        assertEquals("https://streaming-live.rtp.pt/liverepeater/smil:rtp1HD.smil/playlist.m3u8", rtp1.streamUrl)
        assertEquals("Mozilla/5.0", rtp1.userAgent)
        assertEquals("https://www.rtp.pt", rtp1.referrer)

        val movie = channels[1]
        assertEquals("Cinema 1 HD", movie.name)
        assertEquals("Filmes", movie.groupTitle)
        assertEquals("MOVIE", movie.categoryType)
    }

    @Test
    fun testEmergencyFallbacks() {
        val fallbacks = M3UParser.getEmergencyFallbackChannels(1L)
        assertTrue(fallbacks.isNotEmpty())
        assertTrue(fallbacks.any { it.name.contains("RTP 1") })
    }
}
