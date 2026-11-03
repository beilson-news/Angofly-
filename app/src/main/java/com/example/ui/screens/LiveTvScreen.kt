package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Channel
import com.example.ui.components.ChannelCard
import com.example.ui.components.ChannelGridItem
import com.example.ui.theme.AngoBlack
import com.example.ui.theme.AngoCard
import com.example.ui.theme.AngoRed
import com.example.ui.theme.AngoSurfaceVariant
import com.example.ui.theme.AngoWhite
import com.example.ui.viewmodel.IptvViewModel

@Composable
fun LiveTvScreen(
    iptvViewModel: IptvViewModel,
    onPlayChannel: (Channel) -> Unit
) {
    val channels by iptvViewModel.filteredChannels.collectAsState()
    val isSyncing by iptvViewModel.isSyncing.collectAsState()
    val searchQuery by iptvViewModel.searchQuery.collectAsState()
    val selectedCategory by iptvViewModel.selectedCategory.collectAsState()

    var isGridView by remember { mutableStateOf(false) }

    val liveCategories = listOf("Todos", "TV ao Vivo", "TV", "Notícias", "Desporto", "Rádios", "Favoritos")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AngoBlack)
    ) {
        // Top Search & View Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { iptvViewModel.searchQuery.value = it },
                placeholder = { Text("Procurar canais de TV...", color = Color.Gray, fontSize = 13.sp) },
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
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { isGridView = !isGridView },
                modifier = Modifier
                    .size(48.dp)
                    .background(AngoCard, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                    contentDescription = "Alternar Visualização",
                    tint = AngoWhite
                )
            }
        }

        // Category Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(liveCategories) { cat ->
                val isSelected = cat == selectedCategory
                Surface(
                    onClick = { iptvViewModel.selectedCategory.value = cat },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) AngoRed else AngoSurfaceVariant,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = cat,
                        color = AngoWhite,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Stats summary header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${channels.size} canais disponíveis",
                color = Color.LightGray,
                fontSize = 12.sp
            )
            if (isSyncing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = AngoRed, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Atualizando...", color = AngoRed, fontSize = 11.sp)
                }
            }
        }

        // Channels List / Grid
        if (channels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = null,
                        tint = Color.DarkGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Nenhum canal encontrado",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Tente outra categoria ou sincronize a playlist.",
                        color = Color.DarkGray,
                        fontSize = 12.sp
                    )
                }
            }
        } else if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp, top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(channels, key = { it.id }) { ch ->
                    ChannelGridItem(
                        channel = ch,
                        onClick = { onPlayChannel(ch) }
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(channels, key = { it.id }) { ch ->
                    ChannelCard(
                        channel = ch,
                        onClick = { onPlayChannel(ch) },
                        onToggleFavorite = { iptvViewModel.toggleFavorite(ch) }
                    )
                }
            }
        }
    }
}
