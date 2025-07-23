package com.respiroc.timesheet.domain.repository

import com.respiroc.timesheet.domain.model.TimesheetEntry
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.LocalDate

@Repository
class TimesheetJdbcRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    
    private val timesheetRowMapper = RowMapper<TimesheetEntry> { rs: ResultSet, _: Int ->
        TimesheetEntry().apply {
            id = rs.getLong("id")
            tenantId = rs.getLong("tenant_id")
            employeeName = rs.getString("employee_name")
            project = rs.getString("project")
            activity = rs.getString("activity")
            entryDate = rs.getDate("entry_date").toLocalDate()
            hours = rs.getBigDecimal("hours")
            comments = rs.getString("comments")
            completed = rs.getBoolean("completed")
            createdAt = rs.getTimestamp("created_at").toLocalDateTime()
            updatedAt = rs.getTimestamp("updated_at").toLocalDateTime()
        }
    }
    
    fun findEntriesByWeek(tenantId: Long, startDate: LocalDate, endDate: LocalDate): List<TimesheetEntry> {
        val sql = """
            SELECT * FROM timesheet_entries 
            WHERE tenant_id = ? 
            AND entry_date BETWEEN ? AND ?
            ORDER BY employee_name, project, activity, entry_date
        """
        return jdbcTemplate.query(sql, timesheetRowMapper, tenantId, startDate, endDate)
    }
    
    fun findDistinctEmployeeNames(tenantId: Long): List<String> {
        val sql = "SELECT DISTINCT employee_name FROM timesheet_entries WHERE tenant_id = ? ORDER BY employee_name"
        return jdbcTemplate.queryForList(sql, String::class.java, tenantId)
    }
    
    fun findDistinctProjects(tenantId: Long): List<String> {
        val sql = "SELECT DISTINCT project FROM timesheet_entries WHERE tenant_id = ? ORDER BY project"
        return jdbcTemplate.queryForList(sql, String::class.java, tenantId)
    }
    
    fun findDistinctActivities(tenantId: Long): List<String> {
        val sql = "SELECT DISTINCT activity FROM timesheet_entries WHERE tenant_id = ? ORDER BY activity"
        return jdbcTemplate.queryForList(sql, String::class.java, tenantId)
    }
    
    fun save(entry: TimesheetEntry): TimesheetEntry {
        if (entry.id == null) {
            // Check if an entry already exists for this combination
            val existingEntry = findByUniqueKey(entry.tenantId, entry.employeeName, entry.project, entry.activity, entry.entryDate)
            if (existingEntry != null) {
                // Update existing entry
                entry.id = existingEntry.id
                val sql = """
                    UPDATE timesheet_entries 
                    SET hours = ?, comments = ?, completed = ?, updated_at = CURRENT_TIMESTAMP
                    WHERE id = ? AND tenant_id = ?
                """
                jdbcTemplate.update(
                    sql, entry.hours, entry.comments, entry.completed,
                    entry.id, entry.tenantId
                )
            } else {
                // Insert new entry
                val sql = """
                    INSERT INTO timesheet_entries 
                    (tenant_id, employee_name, project, activity, entry_date, hours, comments, completed)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    RETURNING id
                """
                val id = jdbcTemplate.queryForObject(
                    sql, Long::class.java,
                    entry.tenantId, entry.employeeName, entry.project, entry.activity,
                    entry.entryDate, entry.hours, entry.comments, entry.completed
                )
                entry.id = id
            }
        } else {
            val sql = """
                UPDATE timesheet_entries 
                SET employee_name = ?, project = ?, activity = ?, entry_date = ?, 
                    hours = ?, comments = ?, completed = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND tenant_id = ?
            """
            jdbcTemplate.update(
                sql, entry.employeeName, entry.project, entry.activity,
                entry.entryDate, entry.hours, entry.comments, entry.completed,
                entry.id, entry.tenantId
            )
        }
        return entry
    }
    
    fun findById(id: Long): TimesheetEntry? {
        val sql = "SELECT * FROM timesheet_entries WHERE id = ?"
        return jdbcTemplate.query(sql, timesheetRowMapper, id).firstOrNull()
    }
    
    fun findByUniqueKey(tenantId: Long, employeeName: String, project: String, activity: String, entryDate: LocalDate): TimesheetEntry? {
        val sql = """
            SELECT * FROM timesheet_entries 
            WHERE tenant_id = ? AND employee_name = ? AND project = ? AND activity = ? AND entry_date = ?
        """
        return try {
            jdbcTemplate.query(sql, timesheetRowMapper, tenantId, employeeName, project, activity, entryDate).firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
    
    fun delete(entry: TimesheetEntry) {
        val sql = "DELETE FROM timesheet_entries WHERE id = ?"
        jdbcTemplate.update(sql, entry.id)
    }
    
    fun findComment(tenantId: Long, employeeName: String, project: String, activity: String, entryDate: LocalDate): String? {
        val sql = """
            SELECT comments FROM timesheet_entries 
            WHERE tenant_id = ? AND employee_name = ? AND project = ? AND activity = ? AND entry_date = ?
        """
        return try {
            jdbcTemplate.queryForObject(sql, String::class.java, tenantId, employeeName, project, activity, entryDate)
        } catch (e: Exception) {
            null
        }
    }
    
    fun removeDuplicates(tenantId: Long) {
        val sql = """
            DELETE FROM timesheet_entries 
            WHERE id NOT IN (
                SELECT MIN(id) FROM timesheet_entries 
                WHERE tenant_id = ? 
                GROUP BY tenant_id, employee_name, project, activity, entry_date
            ) AND tenant_id = ?
        """
        jdbcTemplate.update(sql, tenantId, tenantId)
    }
}