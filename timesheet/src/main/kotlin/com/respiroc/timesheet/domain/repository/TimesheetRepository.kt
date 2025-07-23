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
    
    @Query(value = """
        SELECT DISTINCT employee_name FROM timesheet_entries 
        WHERE tenant_id = :tenantId 
        ORDER BY employee_name
    """, nativeQuery = true)
    fun findDistinctEmployeeNames(@Param("tenantId") tenantId: Long): List<String>
    
    @Query(value = """
        SELECT DISTINCT project FROM timesheet_entries 
        WHERE tenant_id = :tenantId 
        ORDER BY project
    """, nativeQuery = true)
    fun findDistinctProjects(@Param("tenantId") tenantId: Long): List<String>
    
    @Query(value = """
        SELECT DISTINCT activity FROM timesheet_entries 
        WHERE tenant_id = :tenantId 
        ORDER BY activity
    """, nativeQuery = true)
    fun findDistinctActivities(@Param("tenantId") tenantId: Long): List<String>
}