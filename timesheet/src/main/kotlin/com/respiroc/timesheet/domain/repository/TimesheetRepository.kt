package com.respiroc.timesheet.domain.repository

import com.respiroc.timesheet.domain.model.TimesheetEntry
import com.respiroc.util.repository.CustomJpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface TimesheetRepository : CustomJpaRepository<TimesheetEntry, Long> {
    
    @Query("""
        SELECT te FROM TimesheetEntry te 
        WHERE te.tenantId = :tenantId 
        AND te.entryDate BETWEEN :startDate AND :endDate
        ORDER BY te.employeeName, te.project, te.activity, te.entryDate
    """)
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