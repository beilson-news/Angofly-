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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.data.model.UserAccount
import com.example.ui.theme.AngoBlack
import com.example.ui.theme.AngoCard
import com.example.ui.theme.AngoGold
import com.example.ui.theme.AngoRed
import com.example.ui.theme.AngoSuccess
import com.example.ui.theme.AngoSurfaceVariant
import com.example.ui.theme.AngoWarning
import com.example.ui.theme.AngoWhite
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.IptvViewModel

@Composable
fun AdminPanelScreen(
    authViewModel: AuthViewModel,
    iptvViewModel: IptvViewModel,
    onClose: () -> Unit
) {
    val users by authViewModel.allUsers.collectAsState()
    val receipts by authViewModel.allReceipts.collectAsState()
    val channels by iptvViewModel.allChannels.collectAsState()
    val supportPhone by authViewModel.supportWhatsAppNumber.collectAsState()
    val infoMessage by authViewModel.infoMessage.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pagamentos, 1: Clientes, 2: Sistema
    var showCreateUserDialog by remember { mutableStateOf(false) }

    var newUsername by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var newFullName by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }

    var editingWhatsApp by remember { mutableStateOf(supportPhone) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AngoBlack)
            .padding(top = 10.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = AngoGold, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Painel Administrativo", color = AngoWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Gestão Global ANGOFLY IPTV", color = Color.Gray, fontSize = 11.sp)
                }
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Fechar", tint = AngoWhite)
            }
        }

        // Stats Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AngoCard,
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("CLIENTES", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("${users.size}", color = AngoWhite, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AngoCard,
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("CANAIS", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("${channels.size}", color = AngoGold, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AngoCard,
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("PENDENTES", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("${receipts.count { it.status == "PENDING" }}", color = AngoRed, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // Navigation Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = AngoSurfaceVariant,
            contentColor = AngoWhite,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = AngoGold
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Comprovativos (${receipts.count { it.status == "PENDING" }})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Clientes (${users.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Configurações", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        // Content
        when (selectedTab) {
            0 -> {
                // Pagamentos / Comprovativos
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val pending = receipts.filter { it.status == "PENDING" }
                    val others = receipts.filter { it.status != "PENDING" }

                    if (pending.isEmpty() && others.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Nenhum comprovativo registrado ainda.", color = Color.Gray)
                            }
                        }
                    }

                    if (pending.isNotEmpty()) {
                        item {
                            Text("Aguardando Validação:", color = AngoWarning, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        items(pending, key = { it.id }) { receipt ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = AngoCard),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("@${receipt.username}", color = AngoWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("%,d Kz".format(receipt.amountKz), color = AngoGold, fontWeight = FontWeight.Black)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Plano: ${receipt.planName}", color = Color.LightGray, fontSize = 12.sp)
                                    Text("Ref Transação: ${receipt.transactionCode}", color = AngoWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Método: ${receipt.method.replace('_', ' ')}", color = Color.Gray, fontSize = 11.sp)
                                    if (receipt.proofNote.isNotBlank()) {
                                        Text("Nota: ${receipt.proofNote}", color = Color.LightGray, fontSize = 11.sp)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { authViewModel.approveReceipt(receipt) },
                                            colors = ButtonDefaults.buttonColors(containerColor = AngoSuccess),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = AngoBlack, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Aprovar & Ativar", color = AngoBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { authViewModel.rejectReceipt(receipt.id) },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Recusar", color = AngoRed, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (others.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Histórico de Comprovativos:", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        items(others, key = { it.id }) { receipt ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = AngoCard.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("@${receipt.username} • ${receipt.planName}", color = AngoWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Ref: ${receipt.transactionCode}", color = Color.Gray, fontSize = 11.sp)
                                    }
                                    Surface(
                                        color = if (receipt.status == "APPROVED") AngoSuccess else AngoRed,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = receipt.status,
                                            color = AngoBlack,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Gestão de Clientes
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Button(
                            onClick = { showCreateUserDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AngoRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = AngoWhite)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Criar Novo Assinante", color = AngoWhite, fontWeight = FontWeight.Bold)
                        }
                    }

                    items(users, key = { it.username }) { u ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = AngoCard),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("@${u.username}", color = AngoWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            if (u.isAdmin) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(color = AngoGold, shape = RoundedCornerShape(4.dp)) {
                                                    Text("ADMIN", color = AngoBlack, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                }
                                            }
                                        }
                                        Text(u.fullName, color = Color.LightGray, fontSize = 12.sp)
                                    }

                                    Surface(
                                        color = if (u.isActive) AngoSuccess.copy(alpha = 0.2f) else AngoRed.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (u.isActive) "ATIVO" else "BLOQUEADO",
                                            color = if (u.isActive) AngoSuccess else AngoRed,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Plano: ${u.planTitle} (${if (u.isAdmin) "Ilimitado" else "${u.daysRemaining} dias restantes"})",
                                    color = AngoGold,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Action Buttons: Estender +30d, +90d, Bloquear
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = { authViewModel.extendUserManual(u.username, 30, "Plano Mensal (30 Dias)") },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("+30 Dias", fontSize = 10.sp, color = AngoWhite)
                                    }

                                    OutlinedButton(
                                        onClick = { authViewModel.extendUserManual(u.username, 90, "Plano Trimestral (90 Dias)") },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("+90 Dias", fontSize = 10.sp, color = AngoWhite)
                                    }

                                    if (!u.isAdmin) {
                                        OutlinedButton(
                                            onClick = { authViewModel.toggleUserBlock(u.username, u.isActive) },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(if (u.isActive) "Bloquear" else "Desbloquear", fontSize = 10.sp, color = if (u.isActive) AngoRed else AngoSuccess)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Configurações do Sistema
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = AngoCard),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("WhatsApp Suporte de Angola", color = AngoWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Número para onde os clientes enviam comprovativos", color = Color.Gray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = editingWhatsApp,
                                    onValueChange = { editingWhatsApp = it },
                                    label = { Text("Número WhatsApp (Ex: +244923000111)") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AngoGold, focusedTextColor = AngoWhite, unfocusedTextColor = AngoWhite),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { authViewModel.updateWhatsAppNumber(editingWhatsApp) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AngoGold),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Guardar Número", color = AngoBlack, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = AngoCard),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Playlist Padrão Interna", color = AngoWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("https://raw.githubusercontent.com/LITUATUI/M3UPT/main/M3U/M3UPT.m3u", color = AngoGold, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { iptvViewModel.syncDefaultPlaylist(force = true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AngoRed),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null, tint = AngoWhite)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Forçar Sincronização Geral", color = AngoWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Create User Dialog
        if (showCreateUserDialog) {
            AlertDialog(
                onDismissRequest = { showCreateUserDialog = false },
                title = { Text("Criar Assinante Manualmente", color = AngoWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newFullName,
                            onValueChange = { newFullName = it },
                            label = { Text("Nome Completo") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AngoRed, focusedTextColor = AngoWhite, unfocusedTextColor = AngoWhite),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newUsername,
                            onValueChange = { newUsername = it },
                            label = { Text("Utilizador") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AngoRed, focusedTextColor = AngoWhite, unfocusedTextColor = AngoWhite),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Senha") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AngoRed, focusedTextColor = AngoWhite, unfocusedTextColor = AngoWhite),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = { newEmail = it },
                            label = { Text("Email ou Telemóvel") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AngoRed, focusedTextColor = AngoWhite, unfocusedTextColor = AngoWhite),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            authViewModel.register(newUsername, newPassword, newFullName, newEmail)
                            showCreateUserDialog = false
                            newUsername = ""
                            newPassword = ""
                            newFullName = ""
                            newEmail = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AngoRed)
                    ) {
                        Text("Cadastrar Cliente", color = AngoWhite)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateUserDialog = false }) {
                        Text("Cancelar", color = Color.Gray)
                    }
                },
                containerColor = AngoCard
            )
        }
    }
}
