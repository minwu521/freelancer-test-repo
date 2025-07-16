package com.respiroc.ledger.application.payload

import java.time.LocalDate

data class UpdateVoucherPayload(
    val id: Long,
    val date: LocalDate,
    val description: String?,
    val postings: List<CreatePostingPayload>
)
