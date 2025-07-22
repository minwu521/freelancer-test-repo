package com.respiroc.timesheet.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.TenantId
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "timesheet_entries")
data class TimesheetEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @TenantId
    @Column(name = "tenant_id", nullable = false)
    val tenantId: Long,

    @Column(name = "employee_name", nullable = false)
    val employeeName: String,

    @Column(nullable = false)
    val project: String,

    @Column(nullable = false)
    val activity: String,

    @Column(name = "entry_date", nullable = false)
    val entryDate: LocalDate,

    @Column(nullable = false, precision = 4, scale = 2)
    val hours: BigDecimal = BigDecimal.ZERO,

    @Column
    val comments: String? = null,

    @Column(nullable = false)
    val completed: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: ZonedDateTime = ZonedDateTime.now()
)