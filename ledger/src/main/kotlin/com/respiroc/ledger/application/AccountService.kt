package com.respiroc.ledger.application

import com.respiroc.ledger.domain.model.Account
import com.respiroc.ledger.domain.model.AccountType
import jakarta.annotation.PostConstruct
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.yaml.snakeyaml.Yaml

@Service
class AccountService {

    private lateinit var accounts: Map<String, Account>

    @PostConstruct
    fun loadChartOfAccounts() {
        val yaml = Yaml()
        val inputStream = ClassPathResource("data/chart-of-accounts.yaml").inputStream

        @Suppress("UNCHECKED_CAST")
        val data = yaml.load<Map<String, Any>>(inputStream)
        val accountsList = data["accounts"] as List<Map<String, Any>>

        accounts = accountsList.associate { accountData ->
            val noAccountNumber = accountData["noAccountNumber"] as String
            val account = Account(
                noAccountNumber = noAccountNumber,
                accountName = accountData["accountName"] as String,
                accountDescription = accountData["accountDescription"] as String,
                accountType = AccountType.valueOf(accountData["accountType"] as String)
            )
            noAccountNumber to account
        }
    }

    fun findAccountByNumber(noAccountNumber: String): Account? {
        return accounts[noAccountNumber]
    }

    fun findAllAccounts(): Collection<Account> {
        return accounts.values
    }

    fun findAccountsByType(accountType: AccountType): List<Account> {
        return accounts.values.filter { it.accountType == accountType }
    }

    fun searchAccountsByName(searchTerm: String): List<Account> {
        return accounts.values.filter {
            it.accountName.contains(searchTerm, ignoreCase = true) ||
                    it.accountDescription?.contains(searchTerm, ignoreCase = true) == true
        }
    }

    fun accountExists(noAccountNumber: String): Boolean {
        return accounts.containsKey(noAccountNumber)
    }

    fun findAssetAccounts(): List<Account> = findAccountsByType(AccountType.ASSET)

    fun findLiabilityAccounts(): List<Account> = findAccountsByType(AccountType.LIABILITY)

    fun findEquityAccounts(): List<Account> = findAccountsByType(AccountType.EQUITY)

    fun findRevenueAccounts(): List<Account> = findAccountsByType(AccountType.REVENUE)

    fun findExpenseAccounts(): List<Account> = findAccountsByType(AccountType.EXPENSE)

    fun findCostOfGoodsSoldAccounts(): List<Account> = findAccountsByType(AccountType.COST_OF_GOODS_SOLD)
}