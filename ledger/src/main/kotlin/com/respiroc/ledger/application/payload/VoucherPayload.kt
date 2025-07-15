package com.respiroc.ledger.application.payload

import java.time.LocalDate

data class VoucherPayload(
    val id: Long,
    val number: String,
    val date: LocalDate
) 