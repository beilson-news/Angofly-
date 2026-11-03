package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.AngoCard
import com.example.ui.theme.AngoGold
import com.example.ui.theme.AngoRed
import com.example.ui.theme.AngoSuccess
import com.example.ui.theme.AngoWhite

@Composable
fun AccountScreen(
    currentUser: UserAccount?,
    onBack: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    onLogout: () -> Unit
) {
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
                    text = "Minha Conta ANGOFLY",
                    color = AngoWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Profile Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AngoCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.angofly_logo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentUser?.fullName ?: "Utilizador",
                        color = AngoWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "@${currentUser?.username ?: "angofly"}",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = if (currentUser?.isAdmin == true) AngoGold else AngoRed,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = if (currentUser?.isAdmin == true) "ADMINISTRADOR VIP" else if (currentUser?.isDemo == true) "CONTA DE TESTE (24H)" else "ASSINANTE PREMIUM",
                            color = if (currentUser?.isAdmin == true) AngoBlack else AngoWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Plan Details Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AngoCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estado da Assinatura",
                            color = AngoWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = if (currentUser?.isExpired == true) AngoRed else AngoSuccess,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (currentUser?.isExpired == true) "EXPIRADO" else "ATIVO",
                                color = AngoBlack,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Plano Atual: ${currentUser?.planTitle ?: "Plano Padrão"}",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )

                    Text(
                        text = if (currentUser?.isAdmin == true) "Acesso perpétuo de administrador." else "Tempo Restante: ${currentUser?.daysRemaining ?: 0} dias (${currentUser?.hoursRemaining ?: 0} horas)",
                        color = AngoGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onNavigateToSubscriptions,
                        colors = ButtonDefaults.buttonColors(containerColor = AngoRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = AngoWhite)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RENOVAR OU FAZER UPGRADE DO PLANO", color = AngoWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Device and Technical Details Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AngoCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Dispositivo & Segurança",
                        color = AngoWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ID Dispositivo: ${currentUser?.deviceId ?: "ANG-ANDROID-889"}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Conexões Simultâneas Permitidas: Até 2 telas",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Servidor: Luanda / Lisboa CDN Ultrarrápido",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Logout Button
        item {
            OutlinedButton(
                onClick = onLogout,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = AngoRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Terminar Sessão", color = AngoRed, fontWeight = FontWeight.Bold)
            }
        }
    }
}
