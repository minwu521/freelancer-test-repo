package com.respiroc.timesheet.domain.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "timesheet_entries")
class TimesheetEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "tenant_id", nullable = false)
    var tenantId: Long = 0

    @Column(name = "employee_name", nullable = false)
    var employeeName: String = ""

    @Column(nullable = false)
    var project: String = ""

    @Column(nullable = false)
    var activity: String = ""

    @Column(name = "entry_date", nullable = false)
    var entryDate: LocalDate = LocalDate.now()

    @Column(nullable = false, precision = 4, scale = 2)
    var hours: BigDecimal = BigDecimal.ZERO

    @Column
    var comments: String? = null

    @Column(nullable = false)
    var completed: Boolean = false

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
}