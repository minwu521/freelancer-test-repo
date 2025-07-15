package com.respiroc.ledger.application.payload

import java.math.BigDecimal
import java.time.LocalDate

data class GeneralLedgerPayload(
    val accounts: List<GeneralLedgerAccountEntry>,
    val totalAmount: BigDecimal
)

data class GeneralLedgerAccountEntry(
    val accountNumber: String,
    val accountName: String,
    val openingBalance: BigDecimal,
    val postings: List<GeneralLedgerPostingEntry>,
    val closingBalance: BigDecimal
)

data class GeneralLedgerPostingEntry(
    val id: Long,
    val voucherId: Long?,
    val voucherNumber: String?,
    val date: LocalDate,
    val description: String?,
    val vatCode: String?,
    val currency: String,
    val amount: BigDecimal,
    val originalCurrency: String?,
    val originalAmount: BigDecimal?,
    val closed: Boolean = false // For future use
) 