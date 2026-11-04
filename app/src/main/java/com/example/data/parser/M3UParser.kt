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
        
        // Add core verified channels first so user always has instant, guaranteed streaming
        channels.addAll(getEmergencyFallbackChannels(playlistId))

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
                val rawUrl = trimmed
                val sanitizedUrl = StreamFallbackManager.sanitizeStreamUrl(rawUrl, currentName)
                val categoryType = determineCategoryType(currentGroup, currentName, sanitizedUrl)

                // Skip if duplicate name already in initial verified list
                val alreadyExists = channels.any { it.name.equals(currentName, ignoreCase = true) }
                if (!alreadyExists) {
                    channels.add(
                        Channel(
                            name = currentName.ifEmpty { "Canal " + (channels.size + 1) },
                            streamUrl = sanitizedUrl,
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
                }

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
            channelName.contains("Zimbo", ignoreCase = true) -> "Jornal da Noite & Informação Nacional"
            channelName.contains("TVI", ignoreCase = true) -> "Jornal Nacional & Entretenimento HD"
            channelName.contains("CNN", ignoreCase = true) -> "CNN Notícias 24/7 em Direto"
            channelName.contains("SIC", ignoreCase = true) -> "Novelas & Grandes Histórias HD"
            channelName.contains("Euronews", ignoreCase = true) -> "Notícias Globais em Português"
            channelName.contains("RTP", ignoreCase = true) -> "Telejornal & Informação em Direto"
            channelName.contains("Desporto", ignoreCase = true) || channelName.contains("Sport", ignoreCase = true) -> "Resumo Desportivo & Futebol"
            else -> "Transmissão Oficial em Direto HD"
        }
    }

    fun getEmergencyFallbackChannels(playlistId: Long = 1L): List<Channel> {
        return listOf(
            Channel(
                name = "TV Zimbo (Angola HD)",
                streamUrl = "https://sgn-cdn-video.vods2africa.com/Tv-Zimbo/index.fmp4.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/pt/thumb/a/a2/Logotipo_TV_Zimbo.png/260px-Logotipo_TV_Zimbo.png",
                groupTitle = "TV",
                tvgId = "TVZimbo.ao",
                playlistId = playlistId,
                categoryType = "LIVE",
                currentProgram = "Telejornal & Noticiário de Angola",
                nextProgram = "A Tarde é Nossa"
            ),
            Channel(
                name = "TVI HD",
                streamUrl = "https://github.com/LITUATUI/M3UPT/raw/main/M3U/TVI.m3u8",
                logoUrl = "https://raw.githubusercontent.com/LITUATUI/M3UPT/main/logos/TVI.pt.png",
                groupTitle = "TV",
                tvgId = "TVI.pt",
                playlistId = playlistId,
                categoryType = "LIVE",
                currentProgram = "Jornal Nacional em Direto",
                nextProgram = "Novela da Noite"
            ),
            Channel(
                name = "CNN Portugal HD",
                streamUrl = "https://github.com/LITUATUI/M3UPT/raw/main/M3U/CNN_Portugal.m3u8",
                logoUrl = "https://raw.githubusercontent.com/LITUATUI/M3UPT/main/logos/CNNPortugal.pt.png",
                groupTitle = "Notícias",
                tvgId = "CNNPortugal.pt",
                playlistId = playlistId,
                categoryType = "LIVE",
                currentProgram = "CNN Notícias 24 Horas",
                nextProgram = "Mundo em Atualização"
            ),
            Channel(
                name = "SIC Novelas & Séries HD",
                streamUrl = "https://production-fast-sic.content.okast.tv/fa2e8c4385712f9a7dce4ff2dcebac2e/channels/d9070446-8448-455e-8075-773b1ba12562/d47eae0f-ad77-414a-9a1d-2a6628ba18c3/media_.m3u8",
                logoUrl = "https://raw.githubusercontent.com/LITUATUI/M3UPT/main/logos/SIC.pt.png",
                groupTitle = "TV",
                tvgId = "SIC.pt",
                playlistId = playlistId,
                categoryType = "LIVE",
                currentProgram = "Mar Salgado & Grandes Novelas",
                nextProgram = "Laços de Sangue"
            ),
            Channel(
                name = "Euronews Português HD",
                streamUrl = "https://6e52fb8b.wurl.com/master/f36d25e7e52f1ba8d7e56eb859c636563214f541/UmxheHhUVi1ldV9FdXJvbmV3c1BvcnR1Z3Vlc19ITFM/playlist.m3u8",
                logoUrl = "https://raw.githubusercontent.com/LITUATUI/M3UPT/main/logos/Euronews.pt.png",
                groupTitle = "Notícias",
                tvgId = "Euronews.pt",
                playlistId = playlistId,
                categoryType = "LIVE",
                currentProgram = "Notícias Globais em Português",
                nextProgram = "Economia Real e Mundo"
            ),
            Channel(
                name = "KK TV Angola",
                streamUrl = "https://w1.manasat.com/ktv-angola/smil:ktv-angola.smil/playlist.m3u8",
                logoUrl = "https://raw.githubusercontent.com/LITUATUI/M3UPT/main/logos/KTV.pt.png",
                groupTitle = "TV",
                tvgId = "KKTV.ao",
                playlistId = playlistId,
                categoryType = "LIVE",
                currentProgram = "Cultura e Música Angolana",
                nextProgram = "Espaço Jovem Luanda"
            ),
            Channel(
                name = "ARTV Canal Parlamento",
                streamUrl = "https://playout175.livextend.cloud/livenlin4/_definst_/2liveartvpub2/playlist.m3u8",
                logoUrl = "https://raw.githubusercontent.com/LITUATUI/M3UPT/main/logos/ARTV.pt.png",
                groupTitle = "Notícias",
                tvgId = "ARTV.pt",
                playlistId = playlistId,
                categoryType = "LIVE",
                currentProgram = "Emissão Oficial em Direto",
                nextProgram = "Debates e Comissões"
            ),
            Channel(
                name = "Kuriakos TV HD",
                streamUrl = "https://d25usgadhphvwi.cloudfront.net/hls/main.m3u8",
                logoUrl = "https://raw.githubusercontent.com/LITUATUI/M3UPT/main/logos/KuriakosTV.pt.png",
                groupTitle = "TV",
                tvgId = "KuriakosTV.pt",
                playlistId = playlistId,
                categoryType = "LIVE",
                currentProgram = "Transmissão Internacional",
                nextProgram = "Música e Vida"
            ),
            Channel(
                name = "TV Brasil Internacional",
                streamUrl = "https://tvbrasilinternacional-stream.ebc.com.br/index.m3u8",
                logoUrl = "https://raw.githubusercontent.com/LITUATUI/M3UPT/main/logos/TVBrasil.pt.png",
                groupTitle = "TV",
                tvgId = "TVBrasil.br",
                playlistId = playlistId,
                categoryType = "LIVE",
                currentProgram = "Repórter Brasil em Direto",
                nextProgram = "Caminhos da Reportagem"
            ),
            Channel(
                name = "Big Buck Bunny (Filme HD Demo)",
                streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
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
                streamUrl = "https://archive.org/download/ElephantsDream/ed_1024_512kb.mp4",
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
                streamUrl = "https://archive.org/download/Sintel/sintel-2048-surround.mp4",
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
