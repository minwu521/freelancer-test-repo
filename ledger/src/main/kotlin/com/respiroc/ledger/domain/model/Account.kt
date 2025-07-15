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

fun AccountType.increasesWithDebit(): Boolean = when (this) {
    AccountType.ASSET, AccountType.EXPENSE, AccountType.COST_OF_GOODS_SOLD -> true
    AccountType.LIABILITY, AccountType.EQUITY, AccountType.REVENUE -> false
} 