package com.respiroc.timesheet.domain.repository

import com.respiroc.timesheet.domain.model.TimesheetEntry
import com.respiroc.util.repository.CustomJpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface TimesheetRepository : CustomJpaRepository<TimesheetEntry, Long> {
    
    @Query(value = """
        SELECT * FROM timesheet_entries 
        WHERE tenant_id = :tenantId 
        AND entry_date BETWEEN :startDate AND :endDate
        ORDER BY employee_name, project, activity, entry_date
    """, nativeQuery = true)
    fun findEntriesByWeek(
        @Param("tenantId") tenantId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<TimesheetEntry>
    
    @Query("""
        SELECT DISTINCT te.employeeName FROM TimesheetEntry te 
        WHERE te.tenantId = :tenantId 
        ORDER BY te.employeeName
    """)
    fun findDistinctEmployeeNames(@Param("tenantId") tenantId: Long): List<String>
    
    @Query("""
        SELECT DISTINCT te.project FROM TimesheetEntry te 
        WHERE te.tenantId = :tenantId 
        ORDER BY te.project
    """)
    fun findDistinctProjects(@Param("tenantId") tenantId: Long): List<String>
    
    @Query("""
        SELECT DISTINCT te.activity FROM TimesheetEntry te 
        WHERE te.tenantId = :tenantId 
        ORDER BY te.activity
    """)
    fun findDistinctActivities(@Param("tenantId") tenantId: Long): List<String>
}