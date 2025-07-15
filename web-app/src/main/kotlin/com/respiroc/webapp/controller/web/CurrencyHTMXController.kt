package com.respiroc.webapp.controller.web

import com.respiroc.util.currency.CurrencyService
import com.respiroc.webapp.controller.BaseController
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/htmx/currency")
class CurrencyHTMXController(private val currencyService: CurrencyService) : BaseController() {

    @GetMapping("/convert")
    @HxRequest
    fun convertCurrency(
        @RequestParam allParams: Map<String, String>,
        model: Model
    ): String {
        val toCurrency = allParams["toCurrency"] ?: return "fragments/empty"

        // Find amount parameter (could be 'amount' or 'postingLines[X].amount')
        val amountEntry = allParams.entries.firstOrNull {
            it.key == "amount" || it.key.matches(Regex("postingLines\\[\\d+\\]\\.amount"))
        }

        // Find fromCurrency parameter (could be 'fromCurrency' or 'postingLines[X].currency')
        val fromCurrencyEntry = allParams.entries.firstOrNull {
            it.key == "fromCurrency" || it.key.matches(Regex("postingLines\\[\\d+\\]\\.currency"))
        }

        if (amountEntry == null || fromCurrencyEntry == null) {
            return "fragments/empty"
        }

        val amount = try {
            amountEntry.value.toBigDecimal()
        } catch (_: Exception) {
            return "fragments/empty"
        }

        val fromCurrency = fromCurrencyEntry.value

        if (toCurrency.equals(fromCurrency, ignoreCase = true)) {
            return "fragments/empty"
        }

        val convertedAmount = currencyService.convertCurrency(amount, fromCurrency, toCurrency)
        model.addAttribute("convertedAmount", "%.2f".format(convertedAmount))
        model.addAttribute("companyCurrency", toCurrency)
        model.addAttribute("fromCurrency", fromCurrency)
        return "fragments/converted-currency"
    }
}