package com.respiroc.webapp.controller.web

import com.respiroc.timesheet.application.TimesheetService
import com.respiroc.timesheet.domain.model.TimesheetEntry
import com.respiroc.util.context.SpringUser
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*

data class WeeklyTimesheetRow(
    val employeeName: String,
    val project: String,
    val activity: String,
    val hours: Array<BigDecimal?> = arrayOfNulls(7), // Mon-Sun
    val comments: Array<String?> = arrayOfNulls(7) // Mon-Sun comments
)

data class MonthlyReportData(
    val employeeName: String,
    val monthYear: String,
    val dailyHours: Array<BigDecimal?> = arrayOfNulls(31), // 1-31 days
    val projectActivities: List<MonthlyProjectActivity>,
    val summary: MonthlyReportSummary,
    val flextimeBalance: FlextimeBalance,
    val holidayLeave: HolidayLeave
)

data class MonthlyProjectActivity(
    val project: String,
    val activity: String,
    val totalHours: BigDecimal,
    val dailyHours: Array<BigDecimal?> = arrayOfNulls(31)
)

data class MonthlyReportSummary(
    val standardTime: BigDecimal,
    val surplusHoursInPeriod: BigDecimal,
    val accumulatedSurplus: BigDecimal,
    val chargeableHours: BigDecimal,
    val nonChargeableHoursWithPay: BigDecimal,
    val hoursWithPay: BigDecimal
)

data class FlextimeBalance(
    val openingBalance: BigDecimal,
    val surplusHoursInPeriod: BigDecimal,
    val paymentDayDeduction: BigDecimal,
    val closingBalance: BigDecimal
)

data class HolidayLeave(
    val holidayTakenYearToDate: BigDecimal,
    val openingBalance: BigDecimal,
    val paymentDayDeduction: BigDecimal,
    val closingBalance: BigDecimal
)

@Controller
@RequestMapping("/timesheet")
class TimesheetWebController(
    private val timesheetService: TimesheetService
) {
    
    @GetMapping
    fun overview(
        @AuthenticationPrincipal user: SpringUser,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) targetDate: LocalDate?,
        model: Model
    ): String {
        val currentDate = targetDate ?: LocalDate.now()
        val tenantId = user.ctx.currentTenant?.id ?: throw IllegalStateException("No tenant selected")
        
        // Clean up any duplicate entries first
        timesheetService.cleanupDuplicates(tenantId)
        
        val entries = timesheetService.getWeeklyEntries(tenantId, currentDate)
        val employees = timesheetService.getEmployeeNames(tenantId)
        val projects = timesheetService.getProjects(tenantId)
        val activities = timesheetService.getActivities(tenantId)
        
        // Calculate week start (Monday)
        val weekStart = currentDate.with(TemporalAdjusters.previousOrSame(
            WeekFields.of(Locale.getDefault()).firstDayOfWeek))
        val weekEnd = weekStart.plusDays(6)
        
        // Get report data for current week
        val weeklyReportEntries = timesheetService.generateReport(tenantId, weekStart, weekEnd, null)
        
        // Group entries by employee/project/activity
        val groupedEntries = entries.groupBy { 
            Triple(it.employeeName, it.project, it.activity) 
        }
        
        // Convert to weekly timesheet rows
        val weeklyRows = groupedEntries.map { (key, entryList) ->
            val row = WeeklyTimesheetRow(key.first, key.second, key.third)
            
            // Populate hours and comments for each day of the week
            entryList.forEach { entry ->
                val dayOfWeek = entry.entryDate.dayOfWeek.value // 1=Mon, 2=Tue, ..., 7=Sun
                val arrayIndex = dayOfWeek - 1 // Convert to 0-6 array index (0=Mon, 6=Sun)
                if (arrayIndex in 0..6) {
                    row.hours[arrayIndex] = entry.hours
                    row.comments[arrayIndex] = entry.comments
                }
            }
            row
        }
        
        model.addAttribute("entries", weeklyRows)
        model.addAttribute("currentDate", currentDate)
        model.addAttribute("employees", employees)
        model.addAttribute("projects", projects)
        model.addAttribute("activities", activities)
        model.addAttribute("weeklyReportEntries", weeklyReportEntries)
        model.addAttribute("weekStart", weekStart)
        model.addAttribute("weekEnd", weekEnd)
        model.addAttribute("title", "Timesheet")
        model.addAttribute("user", user)
        model.addAttribute("currentTenant", user.ctx.currentTenant)
        model.addAttribute("tenants", user.ctx.tenants)
        
        return "timesheet/overview"
    }
    
    @PostMapping("/entry")
    @ResponseBody
    fun saveEntry(
        @AuthenticationPrincipal user: SpringUser,
        @RequestParam employeeName: String,
        @RequestParam project: String,
        @RequestParam activity: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) entryDate: LocalDate,
        @RequestParam hours: BigDecimal,
        @RequestParam(required = false) comments: String?
    ): String {
        val tenantId = user.ctx.currentTenant?.id ?: throw IllegalStateException("No tenant selected")
        
        // If hours is 0 and no meaningful comments, delete any existing entry
        if (hours.compareTo(BigDecimal.ZERO) == 0 && (comments.isNullOrBlank() || comments.trim().isEmpty())) {
            timesheetService.deleteEntry(tenantId, employeeName, project, activity, entryDate)
            return "Entry deleted (empty hours)"
        }
        
        val entry = TimesheetEntry().apply {
            this.tenantId = tenantId
            this.employeeName = employeeName
            this.project = project
            this.activity = activity
            this.entryDate = entryDate
            this.hours = hours
            this.comments = comments
        }
        
        timesheetService.saveEntry(entry)
        return "Entry saved successfully"
    }
    
    @PutMapping("/entry/{id}/hours")
    @ResponseBody
    fun updateHours(
        @AuthenticationPrincipal user: SpringUser,
        @PathVariable id: Long,
        @RequestParam hours: BigDecimal
    ): String {
        val tenantId = user.ctx.currentTenant?.id ?: throw IllegalStateException("No tenant selected")
        val updated = timesheetService.updateHours(id, tenantId, hours)
        return if (updated != null) "Hours updated" else "Entry not found"
    }
    
    @DeleteMapping("/entry/{id}")
    @ResponseBody
    fun deleteEntry(
        @AuthenticationPrincipal user: SpringUser,
        @PathVariable id: Long
    ): String {
        val tenantId = user.ctx.currentTenant?.id ?: throw IllegalStateException("No tenant selected")
        val deleted = timesheetService.deleteEntry(id, tenantId)
        return if (deleted) "Entry deleted" else "Entry not found"
    }
    
    @GetMapping("/entry/comment")
    @ResponseBody
    fun getComment(
        @AuthenticationPrincipal user: SpringUser,
        @RequestParam employeeName: String,
        @RequestParam project: String,
        @RequestParam activity: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) entryDate: LocalDate
    ): String {
        val tenantId = user.ctx.currentTenant?.id ?: throw IllegalStateException("No tenant selected")
        val comment = timesheetService.getComment(tenantId, employeeName, project, activity, entryDate)
        return comment ?: ""
    }
    
    @GetMapping("/report")
    fun generateReport(
        @AuthenticationPrincipal user: SpringUser,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        @RequestParam(required = false) employeeName: String?,
        model: Model
    ): String {
        val start = startDate ?: LocalDate.now().withDayOfMonth(1)
        val end = endDate ?: LocalDate.now()
        val tenantId = user.ctx.currentTenant?.id ?: throw IllegalStateException("No tenant selected")
        
        val entries = timesheetService.generateReport(tenantId, start, end, employeeName)
        val employees = timesheetService.getEmployeeNames(tenantId)
        
        model.addAttribute("entries", entries)
        model.addAttribute("startDate", start)
        model.addAttribute("endDate", end)
        model.addAttribute("selectedEmployee", employeeName)
        model.addAttribute("employees", employees)
        model.addAttribute("title", "Timesheet Report")
        model.addAttribute("user", user)
        model.addAttribute("currentTenant", user.ctx.currentTenant)
        model.addAttribute("tenants", user.ctx.tenants)
        
        return "timesheet/report"
    }
    
    @GetMapping("/monthly-report")
    fun monthlyReport(
        @AuthenticationPrincipal user: SpringUser,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") monthYear: String?,
        @RequestParam(required = false) employeeName: String?,
        model: Model
    ): String {
        val currentMonthYear = monthYear ?: LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"))
        val tenantId = user.ctx.currentTenant?.id ?: throw IllegalStateException("No tenant selected")
        val employees = timesheetService.getEmployeeNames(tenantId)
        
        val selectedEmployee = employeeName ?: employees.firstOrNull() ?: ""
        
        if (selectedEmployee.isNotEmpty()) {
            val reportData = generateMonthlyReport(tenantId, selectedEmployee, currentMonthYear)
            model.addAttribute("reportData", reportData)
        }
        
        model.addAttribute("monthYear", currentMonthYear)
        model.addAttribute("selectedEmployee", selectedEmployee)
        model.addAttribute("employees", employees)
        model.addAttribute("title", "Monthly Time Report")
        model.addAttribute("user", user)
        model.addAttribute("currentTenant", user.ctx.currentTenant)
        model.addAttribute("tenants", user.ctx.tenants)
        
        return "timesheet/monthly-report"
    }
    
    private fun generateMonthlyReport(tenantId: Long, employeeName: String, monthYear: String): MonthlyReportData {
        val (year, month) = monthYear.split("-").map { it.toInt() }
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.withDayOfMonth(startDate.lengthOfMonth())
        
        val entries = timesheetService.generateReport(tenantId, startDate, endDate, employeeName)
        
        // Group entries by project/activity
        val projectActivities = entries.groupBy { "${it.project}/${it.activity}" }
            .map { (projectActivity, entryList) ->
                val (project, activity) = projectActivity.split("/", limit = 2)
                val dailyHours = arrayOfNulls<BigDecimal>(31)
                var totalHours = BigDecimal.ZERO
                
                entryList.forEach { entry ->
                    val dayOfMonth = entry.entryDate.dayOfMonth - 1 // 0-based index
                    dailyHours[dayOfMonth] = entry.hours
                    totalHours = totalHours.add(entry.hours)
                }
                
                MonthlyProjectActivity(project, activity, totalHours, dailyHours)
            }
        
        // Calculate daily totals
        val dailyHours = arrayOfNulls<BigDecimal>(31)
        for (day in 0..30) {
            var dayTotal = BigDecimal.ZERO
            projectActivities.forEach { pa ->
                pa.dailyHours[day]?.let { dayTotal = dayTotal.add(it) }
            }
            if (dayTotal > BigDecimal.ZERO) {
                dailyHours[day] = dayTotal
            }
        }
        
        // Calculate summary (simplified calculations for now)
        val totalHours = entries.sumOf { it.hours }
        val workingDaysInMonth = calculateWorkingDaysInMonth(year, month)
        val standardTime = BigDecimal.valueOf(workingDaysInMonth * 8L) // 8 hours per day
        val surplusHours = totalHours.subtract(standardTime)
        
        val summary = MonthlyReportSummary(
            standardTime = standardTime,
            surplusHoursInPeriod = surplusHours,
            accumulatedSurplus = surplusHours, // Simplified - should track over time
            chargeableHours = totalHours,
            nonChargeableHoursWithPay = BigDecimal.ZERO,
            hoursWithPay = totalHours
        )
        
        val flextimeBalance = FlextimeBalance(
            openingBalance = BigDecimal.ZERO, // Should be calculated from previous period
            surplusHoursInPeriod = surplusHours,
            paymentDayDeduction = BigDecimal.ZERO,
            closingBalance = surplusHours // Simplified
        )
        
        val holidayLeave = HolidayLeave(
            holidayTakenYearToDate = BigDecimal.ZERO,
            openingBalance = BigDecimal.valueOf(25), // Standard 25 days
            paymentDayDeduction = BigDecimal.ZERO,
            closingBalance = BigDecimal.valueOf(25)
        )
        
        return MonthlyReportData(
            employeeName = employeeName,
            monthYear = "${java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy").format(startDate)}",
            dailyHours = dailyHours,
            projectActivities = projectActivities,
            summary = summary,
            flextimeBalance = flextimeBalance,
            holidayLeave = holidayLeave
        )
    }
    
    private fun calculateWorkingDaysInMonth(year: Int, month: Int): Int {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.withDayOfMonth(startDate.lengthOfMonth())
        var workingDays = 0
        var currentDate = startDate
        
        while (!currentDate.isAfter(endDate)) {
            if (currentDate.dayOfWeek.value <= 5) { // Monday to Friday
                workingDays++
            }
            currentDate = currentDate.plusDays(1)
        }
        
        return workingDays
    }
}