package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserAccount
import com.example.ui.theme.AngoBlack
import com.example.ui.theme.AngoGold
import com.example.ui.theme.AngoRed
import com.example.ui.theme.AngoSurface
import com.example.ui.theme.AngoWhite

@Composable
fun AngoflyTopBar(
    currentUser: UserAccount?,
    isSyncing: Boolean,
    onSyncClick: () -> Unit,
    onSearchClick: () -> Unit,
    onAccountClick: () -> Unit,
    onAdminClick: () -> Unit
) {
    Surface(
        color = AngoBlack,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onAccountClick() }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.angofly_logo),
                    contentDescription = "ANGOFLY Logo",
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ANGO",
                            color = AngoWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "FLY",
                            color = AngoRed,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (currentUser?.isAdmin == true) AngoGold else AngoRed
                        ) {
                            Text(
                                text = if (currentUser?.isAdmin == true) "ADMIN" else if (currentUser?.isDemo == true) "DEMO" else "PREMIUM",
                                color = if (currentUser?.isAdmin == true) AngoBlack else AngoWhite,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = if (currentUser != null) "@${currentUser.username} • ${currentUser.planTitle}" else "IPTV Premium Oficial",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Action Icons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sync status/button
                IconButton(
                    onClick = onSyncClick,
                    enabled = !isSyncing,
                    modifier = Modifier.size(38.dp)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            color = AngoRed,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sincronizar Canais",
                            tint = AngoWhite
                        )
                    }
                }

                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Pesquisar",
                        tint = AngoWhite
                    )
                }

                if (currentUser?.isAdmin == true) {
                    IconButton(
                        onClick = onAdminClick,
                        modifier = Modifier
                            .size(38.dp)
                            .background(AngoGold.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Painel Admin",
                            tint = AngoGold
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                IconButton(
                    onClick = onAccountClick,
                    modifier = Modifier
                        .size(38.dp)
                        .background(AngoSurface, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Minha Conta",
                        tint = AngoWhite
                    )
                }
            }
        }
    }
}
