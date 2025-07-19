package com.respiroc.util.currency

import org.javamoney.moneta.Money
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap
import javax.money.convert.MonetaryConversions

@Service
class CurrencyService {
    companion object {
        private val logger = LoggerFactory.getLogger(CurrencyService::class.java)
        private val conversionCache = ConcurrentHashMap<String, Double>()

        // Country to currency mapping
        private val countryCurrencyMap = mapOf(
            "NO" to "NOK", // Norway
            "DK" to "DKK", // Denmark
            "SE" to "SEK", // Sweden
            "FI" to "EUR", // Finland
            "IS" to "ISK", // Iceland
            "GB" to "GBP", // United Kingdom
            "US" to "USD", // United States
            "CA" to "CAD", // Canada
            "AU" to "AUD", // Australia
            "NZ" to "NZD", // New Zealand
            "CH" to "CHF", // Switzerland
            "JP" to "JPY", // Japan
            "DE" to "EUR", // Germany
            "FR" to "EUR", // France
            "IT" to "EUR", // Italy
            "ES" to "EUR", // Spain
            "NL" to "EUR", // Netherlands
            "BE" to "EUR", // Belgium
            "AT" to "EUR", // Austria
            "PT" to "EUR", // Portugal
            "IE" to "EUR", // Ireland
            "LU" to "EUR", // Luxembourg
            "MT" to "EUR", // Malta
            "CY" to "EUR", // Cyprus
            "SK" to "EUR", // Slovakia
            "SI" to "EUR", // Slovenia
            "EE" to "EUR", // Estonia
            "LV" to "EUR", // Latvia
            "LT" to "EUR"  // Lithuania
        )

        private fun getRate(fromCurrency: String, toCurrency: String): Double {
            if (fromCurrency == toCurrency) return 1.0

            val key = "$fromCurrency->$toCurrency"
            return conversionCache.computeIfAbsent(key) {
                try {
                    val conversionQuery = MonetaryConversions.getConversion(toCurrency)
                    val money = Money.of(1.0, fromCurrency)
                    money.with(conversionQuery).number.toDouble()
                } catch (e: Exception) {
                    logger.error("Error getting conversion rate from $fromCurrency to $toCurrency", e)
                    1.0
                }
            }
        }
    }

    fun getCompanyCurrency(countryCode: String): String {
        return countryCurrencyMap[countryCode.uppercase()] ?: "NOK"
    }

    fun convertCurrency(amount: BigDecimal, fromCurrency: String, toCurrency: String = "NOK"): BigDecimal {
        if (fromCurrency == toCurrency) return amount

        val conversionFactor = getRate(fromCurrency, toCurrency)
        return amount.multiply(BigDecimal.valueOf(conversionFactor))
            .setScale(2, java.math.RoundingMode.HALF_UP)
    }

    fun getSupportedCurrencies(): List<String> {
        return listOf("NOK", "DKK", "SEK", "EUR", "USD", "GBP", "CHF", "CAD", "AUD", "JPY")
    }

}