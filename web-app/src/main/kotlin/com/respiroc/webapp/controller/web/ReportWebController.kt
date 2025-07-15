package com.respiroc.webapp.controller.web

import com.respiroc.ledger.api.payload.BalanceSheetDTO
import com.respiroc.ledger.application.payload.ProfitLossPayload
import com.respiroc.ledger.application.PostingService
import com.respiroc.ledger.domain.model.AccountType
import com.respiroc.webapp.controller.BaseController
import com.respiroc.webapp.controller.response.Callout
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.math.BigDecimal
import java.time.LocalDate

@Controller
@RequestMapping(value = ["/report"])
class ReportWebController(
    private val postingService: PostingService
) : BaseController() {

    @GetMapping(value = ["/trial-balance"])
    fun trialBalance(
        @RequestParam(name = "startDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        startDate: LocalDate?,
        @RequestParam(name = "endDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        endDate: LocalDate?,
        model: Model
    ): String {
        // Default to current month if no dates provided
        val defaultStartDate = startDate ?: LocalDate.now().withDayOfMonth(1)
        val defaultEndDate = endDate ?: LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())
        val trialBalanceData = postingService.getTrialBalance(defaultStartDate, defaultEndDate)

        addCommonAttributes(model, "Trial Balance")
        model.addAttribute("trialBalanceData", trialBalanceData)
        model.addAttribute("startDate", defaultStartDate)
        model.addAttribute("endDate", defaultEndDate)

        return "report/trial-balance"
    }

    @GetMapping(value = ["/profit-loss"])
    fun profitLoss(
        @RequestParam(name = "startDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        startDate: LocalDate?,
        @RequestParam(name = "endDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        endDate: LocalDate?,
        model: Model
    ): String {
        val defaultStartDate = startDate ?: LocalDate.now().withDayOfMonth(1)
        val defaultEndDate = endDate ?: LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())

        val postingsForProfitLoss = postingService.getPostingsForProfitLoss(defaultStartDate, defaultEndDate)

        addCommonAttributes(model, "Profit & Loss")
        model.addAttribute("startDate", defaultStartDate)
        model.addAttribute("endDate", defaultEndDate)
        model.addAttribute("assetPostings", postingsForProfitLoss[AccountType.ASSET] ?: ProfitLossPayload(emptyList(), BigDecimal.ZERO))
        model.addAttribute("revenuePostings", postingsForProfitLoss[AccountType.REVENUE] ?: ProfitLossPayload(emptyList(), BigDecimal.ZERO))
        model.addAttribute("operatingCostPostings", postingsForProfitLoss[AccountType.EXPENSE] ?: ProfitLossPayload(emptyList(), BigDecimal.ZERO))

        return "report/profit-loss"
    }

    @GetMapping(value = ["/balance-sheet"])
    fun balanceSheet(
        @RequestParam(name = "startDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        startDate: LocalDate?,
        @RequestParam(name = "endDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        endDate: LocalDate?,
        model: Model
    ): String {
        val springUser = springUser()

        val defaultStartDate = startDate ?: LocalDate.now().withDayOfMonth(1)
        val defaultEndDate = endDate ?: LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())

        val postingsForBalanceSheet = postingService.getPostingsForBalanceSheet(defaultStartDate, defaultEndDate)

        model.addAttribute("user", springUser)
        model.addAttribute("startDate", defaultStartDate)
        model.addAttribute("endDate", defaultEndDate)
        model.addAttribute("assetPostings", postingsForBalanceSheet[AccountType.ASSET] ?: BalanceSheetDTO(emptyList(),BigDecimal.ZERO))
        model.addAttribute("equityPostings", postingsForBalanceSheet[AccountType.EQUITY] ?: BalanceSheetDTO(emptyList(), BigDecimal.ZERO))
        model.addAttribute("liabilityPostings", postingsForBalanceSheet[AccountType.LIABILITY] ?: BalanceSheetDTO(emptyList(), BigDecimal.ZERO))

        return "report/balance-sheet"
    }
}

@Controller
@RequestMapping("/htmx/report")
class ReportHTMXController(
    private val postingService: PostingService
) : BaseController() {

    @GetMapping("/trial-balance")
    @HxRequest
    fun trialBalanceHTMX(
        @RequestParam(name = "startDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        startDate: LocalDate?,
        @RequestParam(name = "endDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        endDate: LocalDate?,
        model: Model
    ): String {
        return try {
            val defaultStartDate = startDate ?: LocalDate.now().withDayOfMonth(1)
            val defaultEndDate = endDate ?: LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())

            val trialBalanceData = postingService.getTrialBalance(defaultStartDate, defaultEndDate)

            model.addAttribute(userAttributeName, springUser())
            model.addAttribute("trialBalanceData", trialBalanceData)
            model.addAttribute("startDate", defaultStartDate)
            model.addAttribute("endDate", defaultEndDate)

            "report/trial-balance :: tableContent"
        } catch (e: Exception) {
            model.addAttribute(calloutAttributeName, Callout.Error("Error loading trial balance: ${e.message}"))
            "report/trial-balance :: error-message"
        }
    }

    @GetMapping(value = ["/profit-loss"])
    @HxRequest
    fun profitLossHTMX(
        @RequestParam(name = "startDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        startDate: LocalDate?,
        @RequestParam(name = "endDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        endDate: LocalDate?,
        model: Model
    ): String {
        return try {
            val defaultStartDate = startDate ?: LocalDate.now().withDayOfMonth(1)
            val defaultEndDate = endDate ?: LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())

            val postingsForProfitLoss = postingService.getPostingsForProfitLoss(defaultStartDate, defaultEndDate)

            model.addAttribute(userAttributeName, springUser())
            model.addAttribute("startDate", defaultStartDate)
            model.addAttribute("endDate", defaultEndDate)
            model.addAttribute("assetPostings", postingsForProfitLoss[AccountType.ASSET] ?: ProfitLossPayload(emptyList(), BigDecimal.ZERO))
            model.addAttribute("revenuePostings", postingsForProfitLoss[AccountType.REVENUE] ?: ProfitLossPayload(emptyList(), BigDecimal.ZERO))
            model.addAttribute("operatingCostPostings", postingsForProfitLoss[AccountType.EXPENSE] ?: ProfitLossPayload(emptyList(), BigDecimal.ZERO))

            "report/profit-loss :: tableContent"
        } catch (e: Exception) {
            model.addAttribute(calloutAttributeName, Callout.Error("Error loading profit loss: ${e.message}"))
            return "report/profit-loss :: error-message"
        }
    }

    @GetMapping(value = ["/balance-sheet"], headers = ["HX-Request"])
    fun balanceSheetHtmx(
        @RequestParam(name = "startDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        startDate: LocalDate?,
        @RequestParam(name = "endDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        endDate: LocalDate?,
        model: Model
    ): String {
        return try {
            val springUser = springUser()

            val defaultStartDate = startDate ?: LocalDate.now().withDayOfMonth(1)
            val defaultEndDate = endDate ?: LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())

            val postingsForBalanceSheet = postingService.getPostingsForBalanceSheet(defaultStartDate, defaultEndDate)

            model.addAttribute("user", springUser)
            model.addAttribute("startDate", defaultStartDate)
            model.addAttribute("endDate", defaultEndDate)
            model.addAttribute("assetPostings", postingsForBalanceSheet[AccountType.ASSET] ?: BalanceSheetDTO(emptyList(),BigDecimal.ZERO))
            model.addAttribute("equityPostings", postingsForBalanceSheet[AccountType.EQUITY] ?: BalanceSheetDTO(emptyList(), BigDecimal.ZERO))
            model.addAttribute("liabilityPostings", postingsForBalanceSheet[AccountType.LIABILITY] ?: BalanceSheetDTO(emptyList(), BigDecimal.ZERO))

            "report/balance-sheet :: tableContent"
        } catch (e: Exception) {
            model.addAttribute(calloutAttributeName, Callout.Error("Error loading balance sheet: ${e.message}"))
            return "report/balance-sheet :: error-message"
        }
    }
}