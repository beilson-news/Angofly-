package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubscriptionPlan
import com.example.data.model.SubscriptionPlansCatalog
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

@Composable
fun PaymentsScreen(
    authViewModel: AuthViewModel,
    currentUser: UserAccount?,
    initialPlan: SubscriptionPlan? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val receipts by authViewModel.allReceipts.collectAsState()
    val supportPhone by authViewModel.supportWhatsAppNumber.collectAsState()
    val infoMessage by authViewModel.infoMessage.collectAsState()

    var selectedPlan by remember {
        mutableStateOf(initialPlan ?: SubscriptionPlansCatalog.plans[2]) // Default 30 days
    }
    var selectedMethodTab by remember { mutableIntStateOf(0) } // 0: Multicaixa, 1: Unitel Money

    var transactionCode by remember { mutableStateOf("") }
    var proofNote by remember { mutableStateOf("") }
    var copyAlert by remember { mutableStateOf<String?>(null) }

    // User's own receipts
    val userReceipts = receipts.filter { it.username == currentUser?.username }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AngoBlack),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 95.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Pagamentos & Ativação",
                color = AngoWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Efetue o pagamento através dos canais autorizados em Angola e envie o código do comprovativo.",
                color = Color.LightGray,
                fontSize = 12.sp
            )
        }

        // Selected plan preview card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AngoCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Plano Selecionado:",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = selectedPlan.title,
                            color = AngoWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${selectedPlan.durationDays} Dias de Acesso Ilimitado",
                            color = AngoGold,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = selectedPlan.formattedPrice,
                        color = AngoRed,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Payment Method Tabs
        item {
            TabRow(
                selectedTabIndex = selectedMethodTab,
                containerColor = AngoSurfaceVariant,
                contentColor = AngoWhite,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedMethodTab]),
                        color = AngoRed
                    )
                }
            ) {
                Tab(
                    selected = selectedMethodTab == 0,
                    onClick = { selectedMethodTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Multicaixa Express", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedMethodTab == 1,
                    onClick = { selectedMethodTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Unitel Money", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }

        // Instructions Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AngoCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (selectedMethodTab == 0) {
                        Text(
                            text = "Instruções Multicaixa Express:",
                            color = AngoWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. Abra a sua aplicação Multicaixa Express ou dirija-se a um Caixa ATM.\n" +
                                    "2. Selecione 'Transferência / Envio de Dinheiro' para o número oficial:\n" +
                                    "3. Número de Destino: 923 000 111 (ANGOFLY SERVIÇOS)\n" +
                                    "4. Digite o valor exato: ${selectedPlan.formattedPrice}\n" +
                                    "5. Guarde o ID / Referência da operação e insira abaixo.",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    clipboardManager.setText(AnnotatedString("923000111"))
                                    copyAlert = "Número copiado para a área de transferência!"
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Nº Multicaixa: 923 000 111", color = AngoGold, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = AngoWhite, modifier = Modifier.size(18.dp))
                            }
                        }
                    } else {
                        Text(
                            text = "Instruções Unitel Money:",
                            color = AngoWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. Disque *449# no seu telemóvel Unitel ou abra o app Unitel Money.\n" +
                                    "2. Escolha 'Transferir Dinheiro'.\n" +
                                    "3. Insira o número da conta da carteira: 923 000 111\n" +
                                    "4. Digite o valor: ${selectedPlan.formattedPrice}\n" +
                                    "5. Confirme o seu PIN e copie a referência do SMS recebido.",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    clipboardManager.setText(AnnotatedString("923000111"))
                                    copyAlert = "Número Unitel Money copiado!"
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Conta Unitel Money: 923 000 111", color = AngoGold, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = AngoWhite, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    copyAlert?.let {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(it, color = AngoSuccess, fontSize = 11.sp)
                    }
                }
            }
        }

        // Proof Submission Form
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AngoCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Submeter Comprovativo de Pagamento",
                        color = AngoWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "A ativação é validada pelo administrador em poucos minutos.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = transactionCode,
                        onValueChange = { transactionCode = it },
                        label = { Text("ID da Transação / Referência (Ex: MCX-892102)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AngoRed,
                            focusedTextColor = AngoWhite,
                            unfocusedTextColor = AngoWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = proofNote,
                        onValueChange = { proofNote = it },
                        label = { Text("Observação / Nome no Comprovativo (Opcional)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AngoRed,
                            focusedTextColor = AngoWhite,
                            unfocusedTextColor = AngoWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val code = transactionCode.ifBlank { "MCX-${System.currentTimeMillis() % 1000000}" }
                            authViewModel.submitPayment(
                                plan = selectedPlan,
                                method = if (selectedMethodTab == 0) "MULTICAIXA_EXPRESS" else "UNITEL_MONEY",
                                referenceOrPhone = "923000111",
                                transactionCode = code,
                                proofNote = proofNote
                            )
                            transactionCode = ""
                            proofNote = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AngoRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = AngoWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ENVIAR PARA APROVAÇÃO", color = AngoWhite, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // WhatsApp Support Direct Button
                    Button(
                        onClick = {
                            val msg = "Olá equipa ANGOFLY, acabei de efetuar o pagamento do ${selectedPlan.title} (${selectedPlan.formattedPrice}) para o utilizador @${currentUser?.username ?: "cliente"}. Segue o comprovativo para ativação."
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://wa.me/${supportPhone.replace("+", "")}?text=${Uri.encode(msg)}")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, tint = AngoWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ENVIAR NO WHATSAPP SUPORTE", color = AngoWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // User History of Receipts
        if (userReceipts.isNotEmpty()) {
            item {
                Text(
                    text = "Os Seus Pedidos de Ativação",
                    color = AngoWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(userReceipts, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = AngoCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = item.planName, color = AngoWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Ref: ${item.transactionCode}", color = Color.Gray, fontSize = 11.sp)
                            Text(text = "Método: ${item.method.replace('_', ' ')}", color = Color.LightGray, fontSize = 11.sp)
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (item.status) {
                                "APPROVED" -> AngoSuccess
                                "REJECTED" -> AngoRed
                                else -> AngoWarning
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (item.status == "APPROVED") Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = AngoBlack,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (item.status) {
                                        "APPROVED" -> "ATIVADO"
                                        "REJECTED" -> "RECUSADO"
                                        else -> "PENDENTE"
                                    },
                                    color = AngoBlack,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
