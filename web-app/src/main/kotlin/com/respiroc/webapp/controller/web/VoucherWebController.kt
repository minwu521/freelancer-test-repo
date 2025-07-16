package com.respiroc.webapp.controller.web

import com.respiroc.ledger.application.AccountService
import com.respiroc.ledger.application.VatService
import com.respiroc.ledger.application.VoucherService
import com.respiroc.util.currency.CurrencyService
import com.respiroc.webapp.controller.BaseController
import com.respiroc.webapp.controller.request.CreateVoucherRequest
import com.respiroc.webapp.controller.response.Callout
import com.respiroc.webapp.service.VoucherWebService
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDate

@Controller
@RequestMapping(value = ["/voucher"])
class VoucherWebController(
    private val accountService: AccountService,
    private val currencyService: CurrencyService,
    private val vatService: VatService,
    private val voucherApi: VoucherService,
    private val voucherWebService: VoucherWebService
) : BaseController() {

    @GetMapping(value = [])
    fun voucher(): String {
        return "redirect:/voucher/overview?tenantId=${tenantId()}"
    }

    @GetMapping(value = ["/overview"])
    fun overview(model: Model): String {
        val vouchers = voucherApi.findAllVoucherSummaries()
        addCommonAttributes(model, "Voucher Overview")
        model.addAttribute("vouchers", vouchers)
        return "voucher/overview"
    }

    @GetMapping(value = ["/new-advanced-voucher"])
    fun newAdvancedVoucher(): String {
        val emptyVoucher = voucherApi.findOrCreateEmptyVoucher()
        return "redirect:/voucher/${emptyVoucher.id}?tenantId=${tenantId()}"
    }

    @GetMapping(value = ["/{id}"])
    fun editVoucher(@PathVariable id: Long, model: Model): String {
        val voucher = voucherApi.findVoucherById(id)
            ?: throw IllegalArgumentException("Voucher not found")

        val uiPostingLines = if (voucher.postings.isNotEmpty()) {
            voucherWebService.convertPostingsToUILines(voucher.postings.toList())
        } else {
            emptyList()
        }

        setupModelAttributes(model)
        model.addAttribute("companyCurrencyCode", countryCode())
        model.addAttribute("voucher", voucher)
        model.addAttribute("uiPostingLines", uiPostingLines)
        model.addAttribute("voucherId", id)
        model.addAttribute("voucherDate", voucher.date.toString())
        return "voucher/advanced-voucher"
    }

    // -------------------------------
    // Private Helper Methods
    // -------------------------------

    private fun setupModelAttributes(model: Model) {
        val accounts = accountService.findAllAccounts()
        val vatCodes = vatService.findAllVatCodes()
        val supportedCurrencies = currencyService.getSupportedCurrencies()

        addCommonAttributes(model, "New Voucher")
        model.addAttribute("accounts", accounts)
        model.addAttribute("vatCodes", vatCodes)
        model.addAttribute("defaultVatCode", vatCodes.first().code)
        model.addAttribute("supportedCurrencies", supportedCurrencies)
    }
}


@Controller
@RequestMapping("/htmx/voucher")
class VoucherHTMXController(
    private val accountService: AccountService,
    private val currencyService: CurrencyService,
    private val vatService: VatService,
    private val voucherWebService: VoucherWebService
) : BaseController() {

    @PostMapping("/update/{voucherId}")
    @HxRequest
    fun updateVoucherHTMX(
        @PathVariable voucherId: Long,
        @Valid @ModelAttribute createVoucherRequest: CreateVoucherRequest,
        bindingResult: BindingResult,
        model: Model
    ): String {
        if (bindingResult.hasErrors()) {
            val errorMessages = bindingResult.allErrors.joinToString(", ") { it.defaultMessage ?: "Validation error" }
            model.addAttribute(calloutAttributeName, Callout.Error(errorMessages))
            return "fragments/callout-message"
        }

        try {
            val callout = voucherWebService.updateVoucherWithPostings(
                voucherId,
                createVoucherRequest,
                countryCode()
            )

            model.addAttribute(calloutAttributeName, callout)

            return "fragments/callout-message"
        } catch (e: Exception) {
            model.addAttribute(calloutAttributeName, Callout.Error("Failed to update voucher: ${e.message}"))
            return "fragments/callout-message"
        }
    }

    @GetMapping("/add-posting-line")
    @HxRequest
    fun addPostingLineHTMX(
        @RequestParam(defaultValue = "0") rowCounter: Int,
        model: Model
    ): String {
        val accounts = accountService.findAllAccounts()
        val vatCodes = vatService.findAllVatCodes()
        val supportedCurrencies = currencyService.getSupportedCurrencies()
        val initialDate = LocalDate.now()

        model.addAttribute("rowCounter", rowCounter)
        model.addAttribute("accounts", accounts)
        model.addAttribute("vatCodes", vatCodes)
        model.addAttribute("defaultVatCode", vatCodes.first().code)
        model.addAttribute("companyCurrencyCode", countryCode())
        model.addAttribute("supportedCurrencies", supportedCurrencies)
        model.addAttribute("initialDate", initialDate)

        return "fragments/posting-line-row"
    }

    @PostMapping("/update-balance")
    @HxRequest
    fun updateBalanceHTMX(
        @ModelAttribute createVoucherRequest: CreateVoucherRequest,
        model: Model
    ): String {
        try {
            val (totalDebit, totalCredit, balance, isBalanced, hasValidEntries) = calculateBalance(
                createVoucherRequest,
                countryCode()
            )

            model.addAttribute("totalDebit", "%.2f".format(totalDebit))
            model.addAttribute("totalCredit", "%.2f".format(totalCredit))
            model.addAttribute("balance", "%.2f".format(balance))
            model.addAttribute("isBalanced", isBalanced)
            model.addAttribute("hasValidEntries", hasValidEntries)

            return "fragments/balance-row-simple"
        } catch (_: Exception) {
            // Return original balance on error - use company currency or fallback
            val fallbackCurrency = try {
                countryCode()
            } catch (_: Exception) {
                "NOK"
            }
            model.addAttribute("totalDebit", "0.00 $fallbackCurrency")
            model.addAttribute("totalCredit", "0.00 $fallbackCurrency")
            model.addAttribute("balance", "0.00 $fallbackCurrency")
            return "fragments/balance-row-simple"
        }
    }

    // -------------------------------
    // Private Helper
    // -------------------------------

    private fun calculateBalance(createVoucherRequest: CreateVoucherRequest, companyCurrency: String): BalanceResult {
        var totalDebit = BigDecimal.ZERO
        var totalCredit = BigDecimal.ZERO
        var hasValidEntries = false

        // Use the posting lines from the request
        createVoucherRequest.postingLines.filterNotNull().forEach { posting ->
            // Check if this posting has valid data
            if (posting.amount != null && posting.amount > BigDecimal.ZERO &&
                (posting.debitAccount.isNotBlank() || posting.creditAccount.isNotBlank())
            ) {

                // Convert to company currency if needed and round to 2 decimal places
                val convertedAmount = if (posting.currency != companyCurrency) {
                    currencyService.convertCurrency(posting.amount, posting.currency, companyCurrency)
                        .setScale(2, java.math.RoundingMode.HALF_UP)
                } else {
                    posting.amount.setScale(2, java.math.RoundingMode.HALF_UP)
                }

                // Add to totals based on account selection
                when {
                    posting.debitAccount.isNotBlank() && posting.creditAccount.isNotBlank() -> {
                        // Both debit and credit - self-balanced entry
                        totalDebit = totalDebit.add(convertedAmount)
                        totalCredit = totalCredit.add(convertedAmount)
                        hasValidEntries = true
                    }

                    posting.debitAccount.isNotBlank() -> {
                        // Only debit
                        totalDebit = totalDebit.add(convertedAmount)
                        hasValidEntries = true
                    }

                    posting.creditAccount.isNotBlank() -> {
                        // Only credit
                        totalCredit = totalCredit.add(convertedAmount)
                        hasValidEntries = true
                    }
                }
            }
        }

        val balance = totalDebit.subtract(totalCredit)
        val isBalanced = balance.abs() < BigDecimal("0.01")

        return BalanceResult(totalDebit, totalCredit, balance, isBalanced, hasValidEntries)
    }

    data class BalanceResult(
        val totalDebit: BigDecimal,
        val totalCredit: BigDecimal,
        val balance: BigDecimal,
        val isBalanced: Boolean,
        val hasValidEntries: Boolean
    )
}
