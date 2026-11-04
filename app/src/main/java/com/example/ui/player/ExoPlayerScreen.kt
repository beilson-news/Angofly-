package com.example.ui.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.model.Channel
import com.example.data.parser.StreamFallbackManager
import com.example.ui.theme.AngoBlack
import com.example.ui.theme.AngoCard
import com.example.ui.theme.AngoGold
import com.example.ui.theme.AngoRed
import com.example.ui.theme.AngoSuccess
import com.example.ui.theme.AngoWhite
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun ExoPlayerScreen(
    channel: Channel,
    allChannels: List<Channel>,
    onClose: () -> Unit,
    onSelectChannel: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit
) {
    val context = LocalContext.current

    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var showControls by remember { mutableStateOf(true) }
    var resizeModeIndex by remember { mutableIntStateOf(0) } // 0: FIT, 1: ZOOM, 2: FILL

    // Failover tracking
    var activeStreamUrl by remember(channel.id) {
        mutableStateOf(StreamFallbackManager.sanitizeStreamUrl(channel.streamUrl, channel.name))
    }
    var isFailoverActive by remember(channel.id) { mutableStateOf(false) }

    val resizeModes = listOf(
        AspectRatioFrameLayout.RESIZE_MODE_FIT to "Ajustar",
        AspectRatioFrameLayout.RESIZE_MODE_ZOOM to "Preencher (Zoom)",
        AspectRatioFrameLayout.RESIZE_MODE_FILL to "Esticar"
    )

    BackHandler {
        onClose()
    }

    // Auto-hide controls after 4 seconds
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(4500)
            showControls = false
        }
    }

    // Initialize ExoPlayer
    val exoPlayer = remember(channel.id) {
        playbackError = null
        isBuffering = true
        isFailoverActive = false
        val initialUrl = StreamFallbackManager.sanitizeStreamUrl(channel.streamUrl, channel.name)
        activeStreamUrl = initialUrl

        val userAgent = if (channel.userAgent.isNotBlank()) {
            channel.userAgent
        } else {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
        }

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(20000)

        val headers = mutableMapOf(
            "Accept" to "*/*",
            "Accept-Language" to "pt-PT,pt;q=0.9,en-US;q=0.8,en;q=0.7"
        )
        if (channel.referrer.isNotBlank()) {
            headers["Referer"] = channel.referrer
            if (channel.referrer.contains("rtp.pt")) {
                headers["Origin"] = "https://www.rtp.pt"
            } else if (channel.referrer.contains("sic.pt")) {
                headers["Origin"] = "https://sic.pt"
            } else if (channel.referrer.contains("iol.pt")) {
                headers["Origin"] = "https://tviplayer.iol.pt"
            }
        }
        httpDataSourceFactory.setDefaultRequestProperties(headers)

        val mediaSourceFactory = DefaultMediaSourceFactory(httpDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                val mediaItem = MediaItem.fromUri(initialUrl)
                setMediaItem(mediaItem)
                playWhenReady = true
                prepare()

                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_BUFFERING -> isBuffering = true
                            Player.STATE_READY -> {
                                isBuffering = false
                                playbackError = null
                            }
                            Player.STATE_ENDED -> isBuffering = false
                            Player.STATE_IDLE -> Unit
                        }
                    }

                    override fun onIsPlayingChanged(playing: Boolean) {
                        isPlaying = playing
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        isBuffering = false
                        // If not yet tried failover, automatically switch to robust backup
                        if (!isFailoverActive) {
                            val backup = StreamFallbackManager.getFailoverUrl(activeStreamUrl, channel.name)
                            if (backup != activeStreamUrl) {
                                isFailoverActive = true
                                activeStreamUrl = backup
                                playbackError = null
                                isBuffering = true
                                setMediaItem(MediaItem.fromUri(backup))
                                prepare()
                                play()
                                return
                            }
                        }

                        playbackError = "Sinal original com restrição geográfica ou temporariamente indisponível."
                    }
                })
            }
    }

    DisposableEffect(channel.id) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AngoBlack)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
    ) {
        // Video Surface
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    resizeMode = resizeModes[resizeModeIndex].first
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
                playerView.resizeMode = resizeModes[resizeModeIndex].first
            }
        )

        // Buffering Spinner
        if (isBuffering && playbackError == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = AngoRed,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isFailoverActive) "Conectando ao sinal de backup..." else "Carregando transmissão...",
                            color = AngoWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = channel.name,
                            color = if (isFailoverActive) AngoGold else AngoRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Playback Error Overlay
        if (playbackError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.88f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Erro",
                        tint = AngoGold,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Sinal em Manutenção",
                        color = AngoWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "A transmissão do canal '${channel.name}' possui bloqueio do servidor ou restrição geográfica no momento.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Action buttons
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            onClick = {
                                val altUrl = StreamFallbackManager.VERIFIED_BACKUPS[1] // TVI
                                isFailoverActive = true
                                activeStreamUrl = altUrl
                                playbackError = null
                                isBuffering = true
                                exoPlayer.setMediaItem(MediaItem.fromUri(altUrl))
                                exoPlayer.prepare()
                                exoPlayer.play()
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = AngoRed
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AngoWhite)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Assistir no Sinal de Backup", color = AngoWhite, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Next Channel button
                        val currentIndex = allChannels.indexOfFirst { it.id == channel.id }
                        val nextChannel = if (currentIndex != -1 && currentIndex + 1 < allChannels.size) {
                            allChannels[currentIndex + 1]
                        } else allChannels.firstOrNull()

                        if (nextChannel != null) {
                            Surface(
                                onClick = { onSelectChannel(nextChannel) },
                                shape = RoundedCornerShape(10.dp),
                                color = AngoCard
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.SkipNext, contentDescription = null, tint = AngoWhite)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Próximo Canal (${nextChannel.name})", color = AngoWhite, fontSize = 12.sp)
                                }
                            }
                        }

                        Surface(
                            onClick = onClose,
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Transparent
                        ) {
                            Text(
                                "Voltar para Lista",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Overlay Controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.85f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.92f)
                            )
                        )
                    )
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar",
                                tint = AngoWhite
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Channel logo or icon
                        if (channel.logoUrl.isNotBlank()) {
                            AsyncImage(
                                model = channel.logoUrl,
                                contentDescription = channel.name,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.1f)),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = channel.name,
                                    color = AngoWhite,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Live Badge
                                Surface(
                                    color = if (isFailoverActive) AngoGold else AngoRed,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isFailoverActive) "SINAL CDN" else "AO VIVO",
                                        color = if (isFailoverActive) AngoBlack else AngoWhite,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = channel.currentProgram,
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Aspect Ratio Toggle
                        IconButton(
                            onClick = {
                                resizeModeIndex = (resizeModeIndex + 1) % resizeModes.size
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = resizeModes[resizeModeIndex].second,
                                tint = AngoWhite
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Favorite Toggle
                        IconButton(
                            onClick = { onToggleFavorite(channel) },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (channel.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (channel.isFavorite) AngoRed else AngoWhite
                            )
                        }
                    }
                }

                // Center Play/Pause button
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                exoPlayer.pause()
                            } else {
                                exoPlayer.play()
                            }
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .background(AngoRed.copy(alpha = 0.9f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                            tint = AngoWhite,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Bottom Quick Channel Switcher
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mudar de Canal",
                            color = AngoWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Modo: ${resizeModes[resizeModeIndex].second}",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allChannels, key = { it.id }) { ch ->
                            val isSelected = ch.id == channel.id
                            Surface(
                                onClick = { onSelectChannel(ch) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AngoRed else AngoCard.copy(alpha = 0.85f),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (ch.logoUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = ch.logoUrl,
                                            contentDescription = ch.name,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            contentScale = ContentScale.Fit
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Tv,
                                            contentDescription = null,
                                            tint = if (isSelected) AngoWhite else Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(
                                        text = ch.name,
                                        color = AngoWhite,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
