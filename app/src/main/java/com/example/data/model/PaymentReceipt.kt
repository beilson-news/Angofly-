package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_receipts")
data class PaymentReceipt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val planId: String,
    val planName: String,
    val amountKz: Long,
    val method: String, // MULTICAIXA_EXPRESS, UNITEL_MONEY
    val referenceOrPhone: String,
    val transactionCode: String,
    val proofNote: String = "",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val createdAt: Long = System.currentTimeMillis(),
    val approvedAt: Long = 0L
)

data class SubscriptionPlan(
    val id: String,
    val title: String,
    val durationDays: Int,
    val priceKz: Long,
    val badge: String,
    val isPopular: Boolean = false,
    val features: List<String>
) {
    val formattedPrice: String
        get() = if (priceKz == 0L) "Grátis" else "%,d Kz".format(priceKz).replace(',', '.')
}

object SubscriptionPlansCatalog {
    val plans = listOf(
        SubscriptionPlan(
            id = "PLAN_TESTE_24H",
            title = "Teste 24 Horas",
            durationDays = 1,
            priceKz = 0,
            badge = "DEMO GRÁTIS",
            features = listOf(
                "Acesso a todos os canais de TV",
                "Filmes e Séries em HD",
                "1 Dispositivo ativo",
                "Válido por 24 horas"
            )
        ),
        SubscriptionPlan(
            id = "PLAN_7D",
            title = "Plano 7 Dias",
            durationDays = 7,
            priceKz = 2500,
            badge = "SEMANAL",
            features = listOf(
                "Canais ao vivo em Full HD",
                "Biblioteca de Filmes & Séries",
                "Servidor ultrarrápido anti-travamento",
                "Suporte WhatsApp rápido"
            )
        ),
        SubscriptionPlan(
            id = "PLAN_30D",
            title = "Plano Mensal (30 Dias)",
            durationDays = 30,
            priceKz = 7000,
            badge = "MAIS POPULAR",
            isPopular = true,
            features = listOf(
                "Mais de 500 canais em Full HD & 4K",
                "Todos os canais de Desporto e Notícias",
                "Filmes e Séries atualizados diariamente",
                "Até 2 dispositivos simultâneos",
                "Guia de programação EPG completo",
                "Suporte prioritário via WhatsApp"
            )
        ),
        SubscriptionPlan(
            id = "PLAN_90D",
            title = "Plano Trimestral (90 Dias)",
            durationDays = 90,
            priceKz = 18000,
            badge = "POUPANÇA 15%",
            features = listOf(
                "Acesso total por 3 meses",
                "Qualidade Ultra HD 4K",
                "2 Telas simultâneas",
                "Atualizações automáticas de playlists",
                "Suporte VIP dedicado"
            )
        ),
        SubscriptionPlan(
            id = "PLAN_180D",
            title = "Plano Semestral (180 Dias)",
            durationDays = 180,
            priceKz = 32000,
            badge = "POUPANÇA 25%",
            features = listOf(
                "Acesso total por 6 meses",
                "Prioridade máxima nos servidores",
                "3 Telas simultâneas",
                "Catálogo de filmes e séries completo",
                "Suporte VIP 24/7"
            )
        ),
        SubscriptionPlan(
            id = "PLAN_365D",
            title = "Plano Anual VIP (365 Dias)",
            durationDays = 365,
            priceKz = 55000,
            badge = "MELHOR VALOR",
            features = listOf(
                "1 Ano de entretenimento completo",
                "Economia de mais de 29.000 Kz",
                "Acesso a eventos desportivos exclusivos",
                "4 Telas simultâneas",
                "Linha direta WhatsApp VIP"
            )
        )
    )
}
