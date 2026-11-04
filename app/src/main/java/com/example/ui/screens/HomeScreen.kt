package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Channel
import com.example.data.model.UserAccount
import com.example.ui.components.ChannelCard
import com.example.ui.components.ContinueWatchingCard
import com.example.ui.components.MovieCard
import com.example.ui.theme.AngoBlack
import com.example.ui.theme.AngoCard
import com.example.ui.theme.AngoGold
import com.example.ui.theme.AngoRed
import com.example.ui.theme.AngoSurfaceVariant
import com.example.ui.theme.AngoWhite
import com.example.ui.viewmodel.IptvViewModel

@Composable
fun HomeScreen(
    iptvViewModel: IptvViewModel,
    currentUser: UserAccount?,
    onPlayChannel: (Channel) -> Unit,
    onNavigateToLiveTv: () -> Unit,
    onNavigateToMovies: () -> Unit,
    onNavigateToSeries: () -> Unit,
    onNavigateToSubscriptions: () -> Unit
) {
    val allChannels by iptvViewModel.allChannels.collectAsState()
    val liveChannels by iptvViewModel.liveTvChannels.collectAsState()
    val movieChannels by iptvViewModel.movieChannels.collectAsState()
    val seriesChannels by iptvViewModel.seriesChannels.collectAsState()
    val recentHistory by iptvViewModel.recentHistory.collectAsState()
    val searchQuery by iptvViewModel.searchQuery.collectAsState()

    var showSearchField by remember { mutableStateOf(false) }

    val categories = listOf("Todos", "TV ao Vivo", "Notícias", "Desporto", "Filmes", "Séries", "Rádios")
    val selectedCategory by iptvViewModel.selectedCategory.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AngoBlack),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Expiry / Subscription alert banner if in demo or expiring soon
        if (currentUser != null && (currentUser.isDemo || currentUser.daysRemaining <= 3)) {
            item {
                Surface(
                    color = AngoRed.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { onNavigateToSubscriptions() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AngoGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (currentUser.isDemo) "Conta de Teste Grátis Ativa" else "Assinatura a Expirar",
                                    color = AngoWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${currentUser.hoursRemaining} horas restantes. Atualize para o plano VIP sem cortes.",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Surface(
                            color = AngoRed,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "VER PLANOS",
                                color = AngoWhite,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Live Search Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { iptvViewModel.searchQuery.value = it },
                    placeholder = { Text("Pesquisar canais, filmes ou séries...", color = Color.Gray) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = AngoRed)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { iptvViewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpar", tint = Color.Gray)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AngoRed,
                        unfocusedBorderColor = Color(0xFF2C2C2E),
                        focusedContainerColor = AngoCard,
                        unfocusedContainerColor = AngoCard,
                        focusedTextColor = AngoWhite,
                        unfocusedTextColor = AngoWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Quick Category Chips
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    Surface(
                        onClick = { iptvViewModel.selectedCategory.value = cat },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) AngoRed else AngoSurfaceVariant,
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = cat,
                            color = AngoWhite,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Hero Banner (when no search query)
        if (searchQuery.isBlank()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AngoCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.angofly_hero_banner),
                            contentDescription = "Destaques ANGOFLY",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Gradient fade
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.5f),
                                            Color.Black.copy(alpha = 0.95f)
                                        )
                                    )
                                )
                        )

                        // Banner Content
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Surface(
                                color = AngoRed,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "TRANSMISSÃO EXCLUSIVA 4K",
                                    color = AngoWhite,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Grandes Jogos & Cinema Mundial",
                                color = AngoWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Mais de 500 canais abertos e fechados sem cortes",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = {
                                        val firstChannel = allChannels.firstOrNull { it.name.contains("TVI", ignoreCase = true) }
                                            ?: allChannels.firstOrNull { it.name.contains("Zimbo", ignoreCase = true) }
                                            ?: allChannels.firstOrNull { it.name.contains("CNN", ignoreCase = true) }
                                            ?: allChannels.firstOrNull()
                                        if (firstChannel != null) onPlayChannel(firstChannel)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AngoRed),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = AngoWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Assistir Agora",
                                        color = AngoWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = onNavigateToMovies,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Text(
                                        "Explorar Filmes",
                                        color = AngoWhite,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Continuar Assistindo Section
        if (recentHistory.isNotEmpty() && searchQuery.isBlank()) {
            item {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Continuar Assistindo",
                            color = AngoWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recentHistory, key = { it.id }) { item ->
                            ContinueWatchingCard(
                                history = item,
                                onClick = {
                                    val ch = allChannels.firstOrNull { it.id == item.channelId }
                                        ?: Channel(
                                            id = item.channelId,
                                            name = item.title,
                                            streamUrl = item.streamUrl,
                                            logoUrl = item.logoUrl,
                                            groupTitle = item.groupTitle
                                        )
                                    onPlayChannel(ch)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Live TV Highlights Section
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = null,
                            tint = AngoRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TV ao Vivo em Destaque",
                            color = AngoWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigateToLiveTv() }
                    ) {
                        Text(
                            text = "Ver Todos",
                            color = AngoRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = AngoRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                val previewLive = (if (liveChannels.isNotEmpty()) liveChannels else allChannels).take(6)
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    previewLive.forEach { channel ->
                        ChannelCard(
                            channel = channel,
                            onClick = { onPlayChannel(channel) },
                            onToggleFavorite = { iptvViewModel.toggleFavorite(channel) }
                        )
                    }
                }
            }
        }

        // Movies Highlights Section
        if (movieChannels.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 20.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = AngoRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Filmes & Cinema",
                                color = AngoWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onNavigateToMovies() }
                        ) {
                            Text(
                                text = "Catálogo",
                                color = AngoRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = AngoRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(movieChannels.take(10), key = { it.id }) { movie ->
                            MovieCard(
                                channel = movie,
                                onClick = { onPlayChannel(movie) }
                            )
                        }
                    }
                }
            }
        }

        // Series & Shows Highlights Section
        if (seriesChannels.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 20.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = null,
                                tint = AngoRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Séries & Novidades",
                                color = AngoWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onNavigateToSeries() }
                        ) {
                            Text(
                                text = "Explorar",
                                color = AngoRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = AngoRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(seriesChannels.take(10), key = { it.id }) { series ->
                            MovieCard(
                                channel = series,
                                onClick = { onPlayChannel(series) }
                            )
                        }
                    }
                }
            }
        }
    }
}
