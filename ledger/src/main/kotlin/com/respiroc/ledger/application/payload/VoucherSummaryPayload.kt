package com.respiroc.ledger.application.payload

import java.time.LocalDate

data class VoucherSummaryPayload(
    val id: Long,
    val number: String,
    val postings: List<PostingSummaryPayload> = emptyList()
)

data class PostingSummaryPayload(
    val id: Long,
    val date: LocalDate,
    val description: String?,
    val accountNumber: String,
    val accountName: String?,
    val vatCode: String?,
    val amount: java.math.BigDecimal
) 