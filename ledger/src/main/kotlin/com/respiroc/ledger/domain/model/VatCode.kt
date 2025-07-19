package com.respiroc.ledger.domain.model

import java.math.BigDecimal

data class VatCode(
    val code: String,
    val description: String,
    val rate: BigDecimal,
    val vatType: VatType,
    val vatCategory: VatCategory
)

enum class VatType {
    INPUT_VAT,      // VAT on purchases (deductible)
    OUTPUT_VAT,     // VAT on sales (payable)
    EXEMPT,         // VAT exempt transactions
    OUTSIDE_SCOPE,  // Outside the scope of VAT
    REVERSE_CHARGE  // Reverse charge VAT
}

enum class VatCategory {
    STANDARD,  // Standard rate (usually 25% in Norway)
    MEDIUM,    // Medium rate (usually 15% in Norway)
    LOW,       // Low rate (usually 12% in Norway)
    EXEMPT     // Tax exempt or outside scope
}

/**
 * Determines if this VAT type requires VAT calculation
 */
fun VatType.requiresVatCalculation(): Boolean = when (this) {
    VatType.INPUT_VAT, VatType.OUTPUT_VAT, VatType.REVERSE_CHARGE -> true
    VatType.EXEMPT, VatType.OUTSIDE_SCOPE -> false
} 