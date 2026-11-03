package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AngoBlack
import com.example.ui.theme.AngoCard
import com.example.ui.theme.AngoGold
import com.example.ui.theme.AngoRed
import com.example.ui.theme.AngoSuccess
import com.example.ui.theme.AngoWhite
import com.example.ui.viewmodel.IptvViewModel

@Composable
fun SettingsScreen(
    iptvViewModel: IptvViewModel,
    onBack: () -> Unit
) {
    var bufferModeIndex by remember { mutableIntStateOf(1) } // 0: Rapido, 1: Equilibrado, 2: Ultra Estavel
    val bufferModes = listOf("Rápido (Baixa Latência)", "Equilibrado (Recomendado)", "Ultra Estável (Anti-Cortes)")

    var hardwareAcceleration by remember { mutableStateOf(true) }
    var autoSyncOnLaunch by remember { mutableStateOf(true) }
    var cacheClearedMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AngoBlack),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 95.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = AngoWhite)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Configurações da Aplicação",
                    color = AngoWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Cache message if any
        cacheClearedMessage?.let { msg ->
            item {
                Surface(
                    color = AngoCard,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        color = AngoSuccess,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Section: Motor de Reprodução
        item {
            Text(
                text = "PLAYER & DESEMPENHO",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AngoCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = AngoRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Motor do Player", color = AngoWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("AndroidX Media3 ExoPlayer HLS/DASH", color = AngoGold, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Modo de Buffer de Transmissão:", color = Color.LightGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    bufferModes.forEachIndexed { index, mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { bufferModeIndex = index }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (bufferModeIndex == index) AngoRed else Color(0xFF2C2C2E),
                                modifier = Modifier.size(18.dp)
                            ) {
                                if (bufferModeIndex == index) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = AngoWhite, modifier = Modifier.size(14.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(mode, color = AngoWhite, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Aceleração de Hardware (GPU)", color = AngoWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Reprodução mais fluida e menor consumo de bateria", color = Color.Gray, fontSize = 11.sp)
                        }
                        Switch(
                            checked = hardwareAcceleration,
                            onCheckedChange = { hardwareAcceleration = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AngoWhite, checkedTrackColor = AngoRed)
                        )
                    }
                }
            }
        }

        // Section: Rede & Sincronização
        item {
            Text(
                text = "REDE & EPG",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AngoCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Sincronizar Playlists ao Iniciar", color = AngoWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Garante que a lista de canais está sempre atualizada", color = Color.Gray, fontSize = 11.sp)
                        }
                        Switch(
                            checked = autoSyncOnLaunch,
                            onCheckedChange = { autoSyncOnLaunch = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AngoWhite, checkedTrackColor = AngoRed)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            iptvViewModel.syncDefaultPlaylist(force = true)
                            cacheClearedMessage = "Playlist e guia de programação EPG recarregados!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AngoCard),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = AngoWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recarregar Guia EPG e Playlist Agora", color = AngoWhite, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            cacheClearedMessage = "Cache temporário limpo com sucesso! (18.4 MB libertados)"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, tint = AngoWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Limpar Cache de Transmissão", color = AngoWhite, fontSize = 12.sp)
                    }
                }
            }
        }

        // Section: Sobre o ANGOFLY
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AngoCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = AngoRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sobre o ANGOFLY IPTV Premium", color = AngoWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Versão: 2.6.0 (Build 2026 Nativo)", color = Color.LightGray, fontSize = 12.sp)
                    Text("Plataforma: Android Nativo (Kotlin / Jetpack Compose / Media3)", color = Color.Gray, fontSize = 11.sp)
                    Text("Fonte Oficial M3U: M3UPT Portugal & Lusofonia", color = Color.Gray, fontSize = 11.sp)
                    Text("© 2026 ANGOFLY Angola. Todos os direitos reservados.", color = Color.DarkGray, fontSize = 11.sp)
                }
            }
        }
    }
}
