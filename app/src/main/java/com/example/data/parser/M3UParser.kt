package com.example.data.parser

import com.example.data.model.Channel
import java.io.BufferedReader
import java.io.StringReader
import java.util.regex.Pattern

object M3UParser {

    private val GROUP_PATTERN = Pattern.compile("group-title=\"([^\"]*)\"")
    private val LOGO_PATTERN = Pattern.compile("tvg-logo=\"([^\"]*)\"")
    private val ID_PATTERN = Pattern.compile("tvg-id=\"([^\"]*)\"")
    private val USER_AGENT_PATTERN = Pattern.compile("http-user-agent=\"?([^\"]*)\"?")
    private val REFERRER_PATTERN = Pattern.compile("http-referrer=\"?([^\"]*)\"?")

    fun parse(m3uContent: String, playlistId: Long = 1L): List<Channel> {
        val channels = mutableListOf<Channel>()
        val reader = BufferedReader(StringReader(m3uContent))

        var currentGroup = "TV"
        var currentLogo = ""
        var currentTvgId = ""
        var currentName = ""
        var currentUserAgent = ""
        var currentReferrer = ""

        var line: String? = reader.readLine()
        while (line != null) {
            val trimmed = line.trim()

            if (trimmed.startsWith("#EXTINF:")) {
                // Extract group
                val groupMatcher = GROUP_PATTERN.matcher(trimmed)
                currentGroup = if (groupMatcher.find()) groupMatcher.group(1)?.trim() ?: "TV" else "TV"

                // Extract logo
                val logoMatcher = LOGO_PATTERN.matcher(trimmed)
                currentLogo = if (logoMatcher.find()) logoMatcher.group(1)?.trim() ?: "" else ""

                // Extract tvg-id
                val idMatcher = ID_PATTERN.matcher(trimmed)
                currentTvgId = if (idMatcher.find()) idMatcher.group(1)?.trim() ?: "" else ""

                // Extract channel name (after last comma)
                val commaIndex = trimmed.lastIndexOf(',')
                currentName = if (commaIndex != -1 && commaIndex < trimmed.length - 1) {
                    trimmed.substring(commaIndex + 1).trim()
                } else {
                    "Canal " + (channels.size + 1)
                }
            } else if (trimmed.startsWith("#EXTVLCOPT:")) {
                val opt = trimmed.substring("#EXTVLCOPT:".length).trim()
                val uaMatcher = USER_AGENT_PATTERN.matcher(opt)
                if (uaMatcher.find()) {
                    currentUserAgent = uaMatcher.group(1)?.trim() ?: ""
                }
                val refMatcher = REFERRER_PATTERN.matcher(opt)
                if (refMatcher.find()) {
                    currentReferrer = refMatcher.group(1)?.trim() ?: ""
                }
            } else if (trimmed.isNotEmpty() && !trimmed.startsWith("#") && (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("rtmp://"))) {
                val streamUrl = trimmed
                val categoryType = determineCategoryType(currentGroup, currentName, streamUrl)

                channels.add(
                    Channel(
                        name = currentName.ifEmpty { "Canal " + (channels.size + 1) },
                        streamUrl = streamUrl,
                        logoUrl = currentLogo,
                        groupTitle = currentGroup.ifEmpty { "TV ao Vivo" },
                        tvgId = currentTvgId,
                        userAgent = currentUserAgent,
                        referrer = currentReferrer,
                        playlistId = playlistId,
                        categoryType = categoryType,
                        currentProgram = getEstimatedProgram(currentName),
                        nextProgram = "Programação Regular"
                    )
                )

                // Reset per-stream metadata
                currentGroup = "TV"
                currentLogo = ""
                currentTvgId = ""
                currentName = ""
                currentUserAgent = ""
                currentReferrer = ""
            }

            line = reader.readLine()
        }

        return if (channels.isEmpty()) getEmergencyFallbackChannels(playlistId) else channels
    }

    private fun determineCategoryType(group: String, name: String, url: String): String {
        val lowerGroup = group.lowercase()
        val lowerName = name.lowercase()
        val lowerUrl = url.lowercase()

        return when {
            lowerGroup.contains("rádio") || lowerGroup.contains("radio") || lowerGroup.contains("audio") || lowerGroup.contains("fm") -> "RADIO"
            lowerGroup.contains("filme") || lowerGroup.contains("movie") || lowerGroup.contains("cinema") || lowerGroup.contains("100 ou mais") || lowerGroup.contains("eixo do mal") || lowerUrl.endsWith(".mp4") || lowerUrl.endsWith(".mkv") -> "MOVIE"
            lowerGroup.contains("série") || lowerGroup.contains("series") || lowerGroup.contains("novela") || lowerGroup.contains("episod") -> "SERIES"
            else -> "LIVE"
        }
    }

    private fun getEstimatedProgram(channelName: String): String {
        return when {
            channelName.contains("RTP 1", ignoreCase = true) -> "Telejornal & Informação em Direto"
            channelName.contains("RTP 2", ignoreCase = true) -> "Sociedade Civil & Cultura"
            channelName.contains("SIC Notícias", ignoreCase = true) -> "Edição da Noite - Notícias 24h"
            channelName.contains("SIC", ignoreCase = true) -> "Jornal da Noite"
            channelName.contains("TVI", ignoreCase = true) -> "Jornal Nacional & Entretenimento"
            channelName.contains("Desporto", ignoreCase = true) || channelName.contains("Sport", ignoreCase = true) -> "Resumo Desportivo & Futebol"
            channelName.contains("CNN", ignoreCase = true) -> "CNN Notícias 24/7"
            channelName.contains("Rádio", ignoreCase = true) || channelName.contains("RFM", ignoreCase = true) -> "Grandes Êxitos & Música"
            else -> "Transmissão Oficial em Direto HD"
        }
    }

    fun getEmergencyFallbackChannels(playlistId: Long = 1L): List<Channel> {
        return listOf(
            Channel(
                name = "RTP 1 HD",
                streamUrl = "https://streaming-live.rtp.pt/liverepeater/smil:rtp1HD.smil/playlist.m3u8",
                logoUrl = "https://raw.githubusercontent.com/LITUATUI/M3UPT/main/logos/RTP1.pt.png",
                groupTitle = "TV",
                tvgId = "RTP1.pt",
                playlistId = playlistId,
                categoryType = "LIVE",
                currentProgram = "Telejornal Especial",
                nextProgram = "Grande Reportagem"
            ),
            Channel(
                name = "RTP 2 HD",
                streamUrl = "https://streaming-live.rtp.pt/liverepeater/rtp2HD.smil/playlist.m3u8",
                logoUrl = "https://raw.githubusercontent.com/LITUATUI/M3UPT/main/logos/RTP2.pt.png",
                groupTitle = "TV",
                tvgId = "RTP2.pt",
                playlistId = playlistId,
                categoryType = "LIVE",
                currentProgram = "Cultura & Sociedade",
                nextProgram = "Documentário Internacional"
            ),
            Channel(
                name = "SIC HD",
                streamUrl = "https://sic.live.impresa.pt/sic1080p.m3u8",
                logoUrl = "https://raw.githubusercontent.com/LITUATUI/M3UPT/main/logos/SIC.pt.png",
                groupTitle = "TV",
                tvgId = "SIC.pt",
                playlistId = playlistId,
                categoryType = "LIVE",
                currentProgram = "Jornal da Noite",
                nextProgram = "Série Especial"
            ),
            Channel(
                name = "RTP Notícias",
                streamUrl = "https://streaming-live.rtp.pt/livetvhlsDVR/rtpnHDdvr.smil/playlist.m3u8",
                logoUrl = "https://raw.githubusercontent.com/LITUATUI/M3UPT/main/logos/RTPNoticias.pt.png",
                groupTitle = "Notícias",
                tvgId = "RTPNoticias.pt",
                playlistId = playlistId,
                categoryType = "LIVE",
                currentProgram = "Notícias 24 Horas",
                nextProgram = "Fronteiras da Atualidade"
            ),
            Channel(
                name = "RTP África",
                streamUrl = "https://streaming-live.rtp.pt/liverepeater/rtpAfricaHD.smil/playlist.m3u8",
                logoUrl = "https://raw.githubusercontent.com/LITUATUI/M3UPT/main/logos/RTPAfrica.pt.png",
                groupTitle = "TV",
                tvgId = "RTPAfrica.pt",
                playlistId = playlistId,
                categoryType = "LIVE",
                currentProgram = "Artediário & Voz de África",
                nextProgram = "Repórter África"
            ),
            Channel(
                name = "Euronews Português",
                streamUrl = "https://euronews-euronews-portuguese-1-pt.samsung.wurl.com/manifest/playlist.m3u8",
                logoUrl = "https://raw.githubusercontent.com/LITUATUI/M3UPT/main/logos/Euronews.pt.png",
                groupTitle = "Notícias",
                tvgId = "Euronews.pt",
                playlistId = playlistId,
                categoryType = "LIVE",
                currentProgram = "Notícias Globais em Português",
                nextProgram = "Economia Real"
            ),
            Channel(
                name = "Big Buck Bunny (Filme HD Demo)",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Big_buck_bunny_poster_big.jpg/440px-Big_buck_bunny_poster_big.jpg",
                groupTitle = "Filmes & Animação",
                tvgId = "BBB.vod",
                playlistId = playlistId,
                categoryType = "MOVIE",
                currentProgram = "Curta-Metragem de Animação 4K",
                nextProgram = "Bastidores"
            ),
            Channel(
                name = "Tears of Steel (Filme Ficção Científica)",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Tears_of_Steel_poster.jpg/440px-Tears_of_Steel_poster.jpg",
                groupTitle = "Filmes & Ficção",
                tvgId = "TOS.vod",
                playlistId = playlistId,
                categoryType = "MOVIE",
                currentProgram = "Ficção Científica Cinema",
                nextProgram = "Efeitos Visuais"
            ),
            Channel(
                name = "Sintel (Filme Épico de Fantasia)",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8f/Sintel_poster.jpg/440px-Sintel_poster.jpg",
                groupTitle = "Filmes & Aventura",
                tvgId = "SINTEL.vod",
                playlistId = playlistId,
                categoryType = "MOVIE",
                currentProgram = "Fantasia e Aventura",
                nextProgram = "Créditos"
            ),
            Channel(
                name = "Antena 1 (Rádio Portugal)",
                streamUrl = "https://streaming-live.rtp.pt/liveradio/antena180a/playlist.m3u8",
                logoUrl = "https://raw.githubusercontent.com/LITUATUI/M3UPT/main/logos/Antena1.pt.png",
                groupTitle = "Rádios",
                tvgId = "Antena1.pt",
                playlistId = playlistId,
                categoryType = "RADIO",
                currentProgram = "Emissão Nacional em Direto",
                nextProgram = "Noticiário da Meia-Hora"
            )
        )
    }
}
