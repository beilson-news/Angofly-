package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.PaymentReceipt
import com.example.data.model.SubscriptionPlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PaymentRepository(context: Context, private val authRepository: AuthRepository) {

    private val db = AppDatabase.getInstance(context)
    private val paymentDao = db.paymentDao()

    val allReceipts: Flow<List<PaymentReceipt>> = paymentDao.getAllReceipts()

    fun getReceiptsByUser(username: String): Flow<List<PaymentReceipt>> =
        paymentDao.getReceiptsByUser(username)

    suspend fun submitPayment(
        username: String,
        plan: SubscriptionPlan,
        method: String,
        referenceOrPhone: String,
        transactionCode: String,
        proofNote: String
    ): Long = withContext(Dispatchers.IO) {
        val receipt = PaymentReceipt(
            username = username,
            planId = plan.id,
            planName = plan.title,
            amountKz = plan.priceKz,
            method = method,
            referenceOrPhone = referenceOrPhone,
            transactionCode = transactionCode,
            proofNote = proofNote,
            status = "PENDING",
            createdAt = System.currentTimeMillis()
        )
        paymentDao.insertReceipt(receipt)
    }

    suspend fun approvePayment(receipt: PaymentReceipt) = withContext(Dispatchers.IO) {
        paymentDao.updateStatus(receipt.id, "APPROVED")
        val days = when (receipt.planId) {
            "PLAN_TESTE_24H" -> 1
            "PLAN_7D" -> 7
            "PLAN_30D" -> 30
            "PLAN_90D" -> 90
            "PLAN_180D" -> 180
            "PLAN_365D" -> 365
            else -> 30
        }
        authRepository.extendUserPlan(receipt.username, days, receipt.planName)
    }

    suspend fun rejectPayment(receiptId: Long) = withContext(Dispatchers.IO) {
        paymentDao.updateStatus(receiptId, "REJECTED")
    }
}
