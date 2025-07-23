package com.respiroc.timesheet.application

import com.respiroc.timesheet.domain.model.TimesheetEntry
import com.respiroc.timesheet.domain.repository.TimesheetJdbcRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*

@Service
@Transactional
class TimesheetService(
    private val timesheetRepository: TimesheetJdbcRepository
) {
    
    fun getWeeklyEntries(tenantId: Long, targetDate: LocalDate): List<TimesheetEntry> {
        val weekStart = targetDate.with(TemporalAdjusters.previousOrSame(
            WeekFields.of(Locale.getDefault()).firstDayOfWeek))
        val weekEnd = weekStart.plusDays(6)
        
        return timesheetRepository.findEntriesByWeek(tenantId, weekStart, weekEnd)
    }
    
    fun saveEntry(entry: TimesheetEntry): TimesheetEntry {
        return timesheetRepository.save(entry)
    }
    
    fun updateHours(entryId: Long, tenantId: Long, hours: BigDecimal): TimesheetEntry? {
        val entry = timesheetRepository.findById(entryId)
        if (entry == null || entry.tenantId != tenantId) return null
        entry.hours = hours
        return timesheetRepository.save(entry)
    }
    
    fun deleteEntry(entryId: Long, tenantId: Long): Boolean {
        val entry = timesheetRepository.findById(entryId)
        if (entry == null || entry.tenantId != tenantId) return false
        timesheetRepository.delete(entry)
        return true
    }
    
    fun deleteEntry(tenantId: Long, employeeName: String, project: String, activity: String, entryDate: LocalDate): Boolean {
        val entry = timesheetRepository.findByUniqueKey(tenantId, employeeName, project, activity, entryDate)
        if (entry != null) {
            timesheetRepository.delete(entry)
            return true
        }
        return false
    }
    
    fun getEmployeeNames(tenantId: Long): List<String> {
        return timesheetRepository.findDistinctEmployeeNames(tenantId)
    }
    
    fun getProjects(tenantId: Long): List<String> {
        return timesheetRepository.findDistinctProjects(tenantId)
    }
    
    fun getActivities(tenantId: Long): List<String> {
        return timesheetRepository.findDistinctActivities(tenantId)
    }
    
    fun getComment(tenantId: Long, employeeName: String, project: String, activity: String, entryDate: LocalDate): String? {
        return timesheetRepository.findComment(tenantId, employeeName, project, activity, entryDate)
    }
    
    fun generateReport(tenantId: Long, startDate: LocalDate, endDate: LocalDate, employeeName: String?): List<TimesheetEntry> {
        val entries = timesheetRepository.findEntriesByWeek(tenantId, startDate, endDate)
        return if (employeeName != null) {
            entries.filter { it.employeeName == employeeName }
        } else {
            entries
        }
    }
    
    fun cleanupDuplicates(tenantId: Long) {
        timesheetRepository.removeDuplicates(tenantId)
    }
}