package com.respiroc.ledger.application.payload

import java.time.LocalDate

data class VoucherSummaryPayload(
    val id: Long,
    val number: String,
    val date: LocalDate,
    val description: String?,
    val postingCount: Int
) 