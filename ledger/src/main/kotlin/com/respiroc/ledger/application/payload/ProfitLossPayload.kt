package com.respiroc.ledger.application.payload

import java.math.BigDecimal

data class ProfitLossPayload(
    val entries: List<ProfitLossEntry>,
    val totalBalance: BigDecimal
)

data class ProfitLossEntry(
    val accountNumber: String,
    val accountName: String,
    val accountDescription: String,
    val amount: BigDecimal,
)
