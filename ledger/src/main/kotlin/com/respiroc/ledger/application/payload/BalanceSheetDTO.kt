package com.respiroc.ledger.application.payload

import java.math.BigDecimal

data class BalanceSheetDTO(
    val entries: List<BalanceSheetEntry>,
    val totalBalance: BigDecimal
)

data class BalanceSheetEntry(
    val accountNumber: String,
    val accountName: String,
    val amount: BigDecimal,
) {
    val amountClass: String
        get() = when {
            amount > BigDecimal.ZERO -> "r-positive-amount"
            amount < BigDecimal.ZERO -> "r-negative-amount"
            else -> "r-zero-amount"
        }
}
