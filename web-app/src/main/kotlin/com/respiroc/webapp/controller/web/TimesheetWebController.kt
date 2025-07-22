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
        val entries = timesheetService.getWeeklyEntries(tenantId, currentDate)
        val employees = timesheetService.getEmployeeNames(tenantId)
        val projects = timesheetService.getProjects(tenantId)
        val activities = timesheetService.getActivities(tenantId)
        
        model.addAttribute("entries", entries)
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
        println("DEBUG: Timesheet controller - entries count: ${entries.size}")
        println("DEBUG: Timesheet controller - employees count: ${employees.size}")
        println("DEBUG: Timesheet controller - currentDate: $currentDate")
        
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
        val entry = TimesheetEntry(
            tenantId = tenantId,
            employeeName = employeeName,
            project = project,
            activity = activity,
            entryDate = entryDate,
            hours = hours,
            comments = comments
        )
        
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