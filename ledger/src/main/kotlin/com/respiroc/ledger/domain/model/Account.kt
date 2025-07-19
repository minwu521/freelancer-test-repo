package com.respiroc.ledger.domain.model

data class Account(
    val noAccountNumber: String,
    val accountName: String,
    val accountDescription: String,
    val accountType: AccountType
)

enum class AccountType {
    ASSET,
    LIABILITY,
    EQUITY,
    REVENUE,
    EXPENSE,
    COST_OF_GOODS_SOLD
}

