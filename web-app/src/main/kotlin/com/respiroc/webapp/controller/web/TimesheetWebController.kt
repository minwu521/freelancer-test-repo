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
        model.addAttribute("title", "Timesheet")
        model.addAttribute("user", user)
        model.addAttribute("currentTenant", user.ctx.currentTenant)
        model.addAttribute("tenants", user.ctx.tenants)
        
        // Debug logging
        println("DEBUG: Timesheet controller - tenantId: $tenantId")
        println("DEBUG: Timesheet controller - raw entries count: ${entries.size}")
        println("DEBUG: Timesheet controller - grouped rows count: ${weeklyRows.size}")
        println("DEBUG: Timesheet controller - employees count: ${employees.size}")
        println("DEBUG: Timesheet controller - currentDate: $currentDate")
        
        entries.forEach { entry ->
            println("DEBUG: Entry - ${entry.employeeName}/${entry.project}/${entry.activity} on ${entry.entryDate} = ${entry.hours} hours, comments: '${entry.comments}'")
        }
        
        weeklyRows.forEach { row ->
            println("DEBUG: Row - ${row.employeeName}/${row.project}/${row.activity}")
            println("  Hours: ${row.hours.contentToString()}")
            println("  Comments: ${row.comments.contentToString()}")
        }
        
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
}