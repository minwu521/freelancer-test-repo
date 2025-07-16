package com.respiroc.ledger.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.respiroc.ledger.domain.model.ChartOfAccount
import org.springframework.stereotype.Service

@Service
class LedgerService  {
     fun getChartOfAccounts(): List<ChartOfAccount> {
        val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
        val inputStream = this.javaClass.getResourceAsStream("/data/chart-of-accounts.yaml")
        val chartOfAccounts = mapper.readValue<Map<String, List<ChartOfAccount>>>(inputStream)
        return chartOfAccounts["accounts"] ?: emptyList()
    }
}
