package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserAccount
import com.example.ui.components.AngoflyTab
import com.example.ui.theme.AngoBlack
import com.example.ui.theme.AngoCard
import com.example.ui.theme.AngoGold
import com.example.ui.theme.AngoRed
import com.example.ui.theme.AngoSuccess
import com.example.ui.theme.AngoSurfaceVariant
import com.example.ui.theme.AngoWhite

@Composable
fun MenuDrawerScreen(
    currentUser: UserAccount?,
    supportWhatsAppNumber: String,
    onNavigateTab: (AngoflyTab) -> Unit,
    onNavigateToPlaylists: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AngoBlack),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 95.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // User Profile Summary Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AngoCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAccount() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.angofly_logo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentUser?.fullName ?: "Cliente ANGOFLY",
                                color = AngoWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentUser?.isAdmin == true) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(color = AngoGold, shape = RoundedCornerShape(4.dp)) {
                                    Text("ADMIN", color = AngoBlack, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                        }

                        Text(
                            text = "@${currentUser?.username ?: "angofly"}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = currentUser?.planTitle ?: "Plano Ativo",
                            color = AngoRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }
        }

        // Section: Streaming & Conteúdos
        item {
            Text(
                text = "STREAMING & ENTRETENIMENTO",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AngoCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    MenuItemRow(
                        icon = Icons.Default.Home,
                        title = "Home / Início",
                        subtitle = "Painel principal e destaques",
                        onClick = { onNavigateTab(AngoflyTab.HOME) }
                    )
                    HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.5.dp)
                    MenuItemRow(
                        icon = Icons.Default.LiveTv,
                        title = "TV ao Vivo",
                        subtitle = "Todos os canais em direto",
                        onClick = { onNavigateTab(AngoflyTab.LIVE_TV) }
                    )
                    HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.5.dp)
                    MenuItemRow(
                        icon = Icons.Default.Movie,
                        title = "Filmes",
                        subtitle = "Catálogo de filmes em HD",
                        onClick = { onNavigateTab(AngoflyTab.MOVIES) }
                    )
                    HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.5.dp)
                    MenuItemRow(
                        icon = Icons.Default.VideoLibrary,
                        title = "Séries",
                        subtitle = "Temporadas e episódios",
                        onClick = { onNavigateTab(AngoflyTab.SERIES) }
                    )
                    HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.5.dp)
                    MenuItemRow(
                        icon = Icons.Default.Favorite,
                        title = "Favoritos",
                        subtitle = "Os seus canais guardados",
                        onClick = onNavigateToFavorites
                    )
                    HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.5.dp)
                    MenuItemRow(
                        icon = Icons.Default.History,
                        title = "Histórico",
                        subtitle = "Últimas transmissões assistidas",
                        onClick = onNavigateToHistory
                    )
                    HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.5.dp)
                    MenuItemRow(
                        icon = Icons.Default.PlaylistPlay,
                        title = "Playlists M3U",
                        subtitle = "Fonte padrão e listas adicionais",
                        onClick = onNavigateToPlaylists
                    )
                }
            }
        }

        // Section: Conta e Assinaturas
        item {
            Text(
                text = "CONTA & PAGAMENTOS ANGOLA",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AngoCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    MenuItemRow(
                        icon = Icons.Default.Star,
                        title = "Planos & Assinaturas",
                        subtitle = "Teste, 7, 30, 90, 180 e 365 dias",
                        iconColor = AngoGold,
                        onClick = onNavigateToSubscriptions
                    )
                    HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.5.dp)
                    MenuItemRow(
                        icon = Icons.Default.Payment,
                        title = "Pagamentos Multicaixa / Unitel",
                        subtitle = "Comprovativos e referências de pagamento",
                        onClick = onNavigateToPayments
                    )
                    HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.5.dp)
                    MenuItemRow(
                        icon = Icons.Default.SupportAgent,
                        title = "WhatsApp Suporte 24/7",
                        subtitle = "Fale diretamente com nossa equipa",
                        iconColor = Color(0xFF25D366),
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://wa.me/${supportWhatsAppNumber.replace("+", "")}?text=${Uri.encode("Olá ANGOFLY Suporte! Preciso de ajuda com a minha conta @${currentUser?.username ?: ""}.")}")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {}
                        }
                    )
                    HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.5.dp)
                    MenuItemRow(
                        icon = Icons.Default.Person,
                        title = "Minha Conta",
                        subtitle = "Validade, dados e segurança",
                        onClick = onNavigateToAccount
                    )
                    if (currentUser?.isAdmin == true) {
                        HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.5.dp)
                        MenuItemRow(
                            icon = Icons.Default.AdminPanelSettings,
                            title = "Painel Administrativo",
                            subtitle = "Aprovação de comprovativos e gestão",
                            iconColor = AngoGold,
                            onClick = onNavigateToAdmin
                        )
                    }
                }
            }
        }

        // Section: Configurações & Sair
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AngoCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    MenuItemRow(
                        icon = Icons.Default.Settings,
                        title = "Configurações",
                        subtitle = "Player Media3, buffer e cache",
                        onClick = onNavigateToSettings
                    )
                    HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.5.dp)
                    MenuItemRow(
                        icon = Icons.Default.ExitToApp,
                        title = "Terminar Sessão (Sair)",
                        subtitle = "Sair da conta neste dispositivo",
                        iconColor = AngoRed,
                        onClick = onLogout
                    )
                }
            }
        }
    }
}

@Composable
fun MenuItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color = AngoRed,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = AngoWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 11.sp
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.DarkGray,
            modifier = Modifier.size(18.dp)
        )
    }
}
