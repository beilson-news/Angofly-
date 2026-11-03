package com.example.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Channel
import com.example.ui.theme.AngoBlack
import com.example.ui.theme.AngoCard
import com.example.ui.theme.AngoGold
import com.example.ui.theme.AngoRed
import com.example.ui.theme.AngoWhite
import com.example.ui.viewmodel.IptvViewModel

@Composable
fun MoviesScreen(
    iptvViewModel: IptvViewModel,
    onPlayMovie: (Channel) -> Unit
) {
    val movies by iptvViewModel.movieChannels.collectAsState()
    val allChannels by iptvViewModel.allChannels.collectAsState()

    // Fallback movies from all channels if movie-specific list is small
    val displayMovies = if (movies.isNotEmpty()) movies else allChannels.take(15)

    var searchQuery by remember { mutableStateOf("") }
    var selectedMovie by remember { mutableStateOf<Channel?>(null) }

    val filteredMovies = displayMovies.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.groupTitle.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AngoBlack)
    ) {
        // Search header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Procurar no catálogo de filmes...", color = Color.Gray, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = AngoRed)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Catálogo de Filmes VoD (${filteredMovies.size})",
                color = AngoWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Surface(
                color = AngoGold.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "FULL HD / 4K",
                    color = AngoGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Movie Poster Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp, top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredMovies, key = { it.id }) { movie ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = AngoCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedMovie = movie }
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(Color(0xFF1E1E1E)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (movie.logoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = movie.logoUrl,
                                    contentDescription = movie.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = AngoRed,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            // 4K badge
                            Surface(
                                color = Color.Black.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = "4K",
                                    color = AngoWhite,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(6.dp)) {
                            Text(
                                text = movie.name,
                                color = AngoWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = AngoGold, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "4.8",
                                    color = Color.LightGray,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Movie Detail Modal Dialog
        selectedMovie?.let { movie ->
            AlertDialog(
                onDismissRequest = { selectedMovie = null },
                title = {
                    Text(movie.name, color = AngoWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                text = {
                    Column {
                        if (movie.logoUrl.isNotBlank()) {
                            AsyncImage(
                                model = movie.logoUrl,
                                contentDescription = movie.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = AngoRed,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "FILME PREMIUM",
                                    color = AngoWhite,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text("Duração: 1h 48m", color = Color.Gray, fontSize = 11.sp)
                            Text("Áudio: Português / Original", color = Color.Gray, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Categoria: ${movie.groupTitle}",
                            color = AngoGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Assista a este título cinematográfico em alta definição no player avançado ANGOFLY sem travamentos.",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedMovie = null
                            onPlayMovie(movie)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AngoRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AngoWhite)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Assistir Filme", color = AngoWhite, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedMovie = null }) {
                        Text("Fechar", color = Color.Gray)
                    }
                },
                containerColor = AngoCard
            )
        }
    }
}
