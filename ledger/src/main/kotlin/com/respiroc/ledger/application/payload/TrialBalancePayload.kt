package com.respiroc.ledger.application.payload

import java.math.BigDecimal

data class TrialBalancePayload (
    val entries: List<TrialBalanceEntry>,
    val totalOpeningBalance: BigDecimal,
    val totalDifference: BigDecimal,
    val totalClosingBalance: BigDecimal
)

data class TrialBalanceEntry(
    val accountNumber: String,
    val accountName: String,
    val openingBalance: BigDecimal,
    val difference: BigDecimal,
    val closingBalance: BigDecimal
)