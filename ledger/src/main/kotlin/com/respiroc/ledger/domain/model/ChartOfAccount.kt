package com.respiroc.ledger.domain.model

import com.respiroc.ledger.domain.model.AccountType

data class ChartOfAccount(
    val noAccountNumber: String,
    val accountName: String,
    val accountDescription: String,
    val accountType: AccountType
)
