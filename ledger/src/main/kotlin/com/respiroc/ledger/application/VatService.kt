package com.respiroc.ledger.application

import com.respiroc.ledger.domain.model.VatCategory
import com.respiroc.ledger.domain.model.VatCode
import com.respiroc.ledger.domain.model.VatType
import com.respiroc.ledger.domain.model.requiresVatCalculation
import jakarta.annotation.PostConstruct
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.yaml.snakeyaml.Yaml
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class VatService {

    private lateinit var vatCodes: Map<String, VatCode>

    @PostConstruct
    fun loadVatCodes() {
        val yaml = Yaml()
        val inputStream = ClassPathResource("data/vat-codes.yaml").inputStream

        @Suppress("UNCHECKED_CAST")
        val data = yaml.load<Map<String, Any>>(inputStream)
        val vatCodesList = data["vatCodes"] as List<Map<String, Any>>

        vatCodes = vatCodesList.associate { vatCodeData ->
            val code = vatCodeData["code"] as String
            val vatCode = VatCode(
                code = code,
                description = vatCodeData["description"] as String,
                rate = BigDecimal.valueOf((vatCodeData["rate"] as Number).toDouble()),
                vatType = VatType.valueOf(vatCodeData["vatType"] as String),
                vatCategory = VatCategory.valueOf(vatCodeData["vatCategory"] as String)
            )
            code to vatCode
        }
    }

    fun findVatCodeByCode(code: String): VatCode? {
        return vatCodes[code]
    }

    fun findAllVatCodes(): Collection<VatCode> {
        return vatCodes.values
    }

    fun findVatCodesByType(vatType: VatType): List<VatCode> {
        return vatCodes.values.filter { it.vatType == vatType }
    }

    fun findVatCodesByCategory(vatCategory: VatCategory): List<VatCode> {
        return vatCodes.values.filter { it.vatCategory == vatCategory }
    }

    fun searchVatCodesByDescription(searchTerm: String): List<VatCode> {
        return vatCodes.values.filter {
            it.description.contains(searchTerm, ignoreCase = true)
        }
    }

    fun vatCodeExists(code: String): Boolean {
        return vatCodes.containsKey(code)
    }

    fun findInputVatCodes(): List<VatCode> = findVatCodesByType(VatType.INPUT_VAT)

    fun findOutputVatCodes(): List<VatCode> = findVatCodesByType(VatType.OUTPUT_VAT)

    fun findExemptVatCodes(): List<VatCode> {
        return vatCodes.values.filter {
            it.vatType == VatType.EXEMPT || it.vatType == VatType.OUTSIDE_SCOPE
        }
    }

    fun calculateVatAmount(baseAmount: BigDecimal, vatCode: VatCode): BigDecimal {
        if (!vatCode.vatType.requiresVatCalculation()) {
            return BigDecimal.ZERO
        }

        return baseAmount.multiply(vatCode.rate)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
    }

    fun calculateBaseAmountFromVatInclusive(vatInclusiveAmount: BigDecimal, vatCode: VatCode): BigDecimal {
        if (!vatCode.vatType.requiresVatCalculation()) {
            return vatInclusiveAmount
        }

        val divisor = BigDecimal.ONE.add(vatCode.rate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
        return vatInclusiveAmount.divide(divisor, 2, RoundingMode.HALF_UP)
    }
} 