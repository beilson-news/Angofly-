package com.example.data.parser

object StreamFallbackManager {

    // High availability 200 OK verified streams
    val VERIFIED_BACKUPS = listOf(
        "https://sgn-cdn-video.vods2africa.com/Tv-Zimbo/index.fmp4.m3u8",
        "https://github.com/LITUATUI/M3UPT/raw/main/M3U/TVI.m3u8",
        "https://github.com/LITUATUI/M3UPT/raw/main/M3U/CNN_Portugal.m3u8",
        "https://6e52fb8b.wurl.com/master/f36d25e7e52f1ba8d7e56eb859c636563214f541/UmxheHhUVi1ldV9FdXJvbmV3c1BvcnR1Z3Vlc19ITFM/playlist.m3u8",
        "https://production-fast-sic.content.okast.tv/fa2e8c4385712f9a7dce4ff2dcebac2e/channels/d9070446-8448-455e-8075-773b1ba12562/d47eae0f-ad77-414a-9a1d-2a6628ba18c3/media_.m3u8",
        "https://d25usgadhphvwi.cloudfront.net/hls/main.m3u8",
        "https://playout175.livextend.cloud/livenlin4/_definst_/2liveartvpub2/playlist.m3u8",
        "https://w1.manasat.com/ktv-angola/smil:ktv-angola.smil/playlist.m3u8",
        "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
    )

    fun sanitizeStreamUrl(originalUrl: String, channelName: String): String {
        val trimmed = originalUrl.trim()
        val nameLower = channelName.lowercase()
        val urlLower = trimmed.lowercase()

        return when {
            // SIC blocked on CloudFront -> use SIC Fast TV 1080p stream
            nameLower.contains("sic") && (urlLower.contains("impresa.pt") || urlLower.contains("sic1080p") || urlLower.contains("sicnot")) ->
                "https://production-fast-sic.content.okast.tv/fa2e8c4385712f9a7dce4ff2dcebac2e/channels/d9070446-8448-455e-8075-773b1ba12562/d47eae0f-ad77-414a-9a1d-2a6628ba18c3/media_.m3u8"

            // Dead commondatastorage bucket (403 AccessDenied) -> use Mux HLS or Archive.org
            urlLower.contains("commondatastorage.googleapis.com") ->
                "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"

            // Outdated Samsung Euronews endpoint -> use active Wurl endpoint
            urlLower.contains("euronews") && urlLower.contains("samsung") ->
                "https://6e52fb8b.wurl.com/master/f36d25e7e52f1ba8d7e56eb859c636563214f541/UmxheHhUVi1ldV9FdXJvbmV3c1BvcnR1Z3Vlc19ITFM/playlist.m3u8"

            // TVI -> ensure using raw GitHub master playlist
            nameLower == "tvi" || nameLower.startsWith("tvi ") ->
                "https://github.com/LITUATUI/M3UPT/raw/main/M3U/TVI.m3u8"

            // CNN Portugal -> ensure using raw GitHub master playlist
            nameLower.contains("cnn portugal") ->
                "https://github.com/LITUATUI/M3UPT/raw/main/M3U/CNN_Portugal.m3u8"

            // TV Zimbo
            nameLower.contains("zimbo") ->
                "https://sgn-cdn-video.vods2africa.com/Tv-Zimbo/index.fmp4.m3u8"

            else -> trimmed
        }
    }

    fun getFailoverUrl(failedUrl: String, channelName: String): String {
        val nameLower = channelName.lowercase()
        return when {
            nameLower.contains("rtp") ->
                "https://github.com/LITUATUI/M3UPT/raw/main/M3U/TVI.m3u8"
            nameLower.contains("sic") ->
                "https://production-fast-sic.content.okast.tv/fa2e8c4385712f9a7dce4ff2dcebac2e/channels/d9070446-8448-455e-8075-773b1ba12562/d47eae0f-ad77-414a-9a1d-2a6628ba18c3/media_.m3u8"
            nameLower.contains("notícia") || nameLower.contains("news") ->
                "https://github.com/LITUATUI/M3UPT/raw/main/M3U/CNN_Portugal.m3u8"
            nameLower.contains("filme") || nameLower.contains("movie") || nameLower.contains("cinema") ->
                "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
            nameLower.contains("angola") || nameLower.contains("zimbo") ->
                "https://sgn-cdn-video.vods2africa.com/Tv-Zimbo/index.fmp4.m3u8"
            else ->
                "https://github.com/LITUATUI/M3UPT/raw/main/M3U/TVI.m3u8"
        }
    }
}
