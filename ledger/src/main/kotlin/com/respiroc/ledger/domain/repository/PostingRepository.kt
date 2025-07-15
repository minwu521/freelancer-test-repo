package com.respiroc.ledger.domain.repository

import com.respiroc.ledger.domain.model.Posting
import com.respiroc.util.repository.CustomJpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDate

@Repository
interface PostingRepository : CustomJpaRepository<Posting, Long> {

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Posting p WHERE p.accountNumber = :accountNumber AND p.tenantId = :tenantId")
    fun getAccountBalance(@Param("accountNumber") accountNumber: String, @Param("tenantId") tenantId: Long): BigDecimal

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Posting p WHERE p.accountNumber = :accountNumber AND p.tenantId = :tenantId AND p.postingDate < :beforeDate")
    fun getAccountBalanceBeforeDate(
        @Param("accountNumber") accountNumber: String,
        @Param("tenantId") tenantId: Long,
        @Param("beforeDate") beforeDate: LocalDate
    ): BigDecimal

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Posting p WHERE p.accountNumber = :accountNumber AND p.tenantId = :tenantId AND p.postingDate BETWEEN :startDate AND :endDate")
    fun getAccountMovementInPeriod(
        @Param("accountNumber") accountNumber: String,
        @Param("tenantId") tenantId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): BigDecimal

    @Query("SELECT DISTINCT p.accountNumber FROM Posting p WHERE p.tenantId = :tenantId ORDER BY p.accountNumber")
    fun findDistinctAccountNumbersByTenant(@Param("tenantId") tenantId: Long): List<String>

    @Query(
        """
        SELECT p.accountNumber,
               COALESCE(SUM(CASE WHEN p.postingDate < :startDate THEN p.amount ELSE 0 END), 0) as openingBalance,
               COALESCE(SUM(CASE WHEN p.postingDate BETWEEN :startDate AND :endDate THEN p.amount ELSE 0 END), 0) as periodMovement,
               COUNT(CASE WHEN p.postingDate BETWEEN :startDate AND :endDate THEN 1 END) as transactionCount
        FROM Posting p 
        WHERE (:accountNumber IS NULL OR p.accountNumber = :accountNumber)
        AND p.tenantId = :tenantId 
        GROUP BY p.accountNumber
        HAVING COALESCE(SUM(CASE WHEN p.postingDate < :startDate THEN p.amount ELSE 0 END), 0) != 0
            OR COALESCE(SUM(CASE WHEN p.postingDate BETWEEN :startDate AND :endDate THEN p.amount ELSE 0 END), 0) != 0
        ORDER BY p.accountNumber
    """
    )
    fun getGeneralLedgerSummary(
        @Param("accountNumber") accountNumber: String?,
        @Param("tenantId") tenantId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<Array<Any>>

    @Query(
        """
        SELECT p FROM Posting p 
        LEFT JOIN FETCH p.voucher v
        WHERE p.accountNumber = :accountNumber 
        AND p.tenantId = :tenantId 
        AND p.postingDate BETWEEN :startDate AND :endDate 
        ORDER BY p.postingDate, p.id
    """
    )
    fun findPostingsByAccountAndDateRange(
        @Param("accountNumber") accountNumber: String,
        @Param("tenantId") tenantId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<Posting>

    @Query("""
    SELECT 
        CASE 
            WHEN p.accountNumber LIKE '1%' THEN 'ASSET'
            WHEN p.accountNumber LIKE '3%' THEN 'REVENUE'
            WHEN p.accountNumber LIKE '4%' OR p.accountNumber LIKE '5%' 
              OR p.accountNumber LIKE '6%' OR p.accountNumber LIKE '7%' THEN 'EXPENSE'
            ELSE 'OTHER'
        END AS accountType,
        p.accountNumber,
        SUM(p.amount) as totalAmount 
    FROM Posting p 
    WHERE p.tenantId = :tenantId
      AND (
            p.accountNumber LIKE '1%' OR 
            p.accountNumber LIKE '3%' OR 
            p.accountNumber LIKE '4%' OR 
            p.accountNumber LIKE '5%' OR 
            p.accountNumber LIKE '6%' OR 
            p.accountNumber LIKE '7%'
          )
      AND p.postingDate BETWEEN :startDate AND :endDate 
    GROUP BY accountType, p.accountNumber
    ORDER BY accountType, p.accountNumber
""")
    fun findProfitLossPostings(
        @Param("tenantId") tenantId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<Array<Any>>

    @Query("""
    SELECT 
        CASE 
            WHEN p.accountNumber LIKE '1%' THEN 'ASSET'
            WHEN p.accountNumber LIKE '20%' THEN 'EQUITY'
            WHEN p.accountNumber LIKE '2%' AND p.accountNumber NOT LIKE '20%' THEN 'LIABILITY'
            ELSE 'OTHER'
        END AS accountType,
        p.accountNumber,
        SUM(p.amount) as totalAmount 
    FROM Posting p 
    WHERE p.tenantId = :tenantId
      AND p.accountNumber >= '1' AND p.accountNumber < '3'
      AND p.postingDate BETWEEN :startDate AND :endDate 
    GROUP BY accountType, p.accountNumber
    ORDER BY accountType, p.accountNumber
""")
    fun findBalanceSheetPostings(
        @Param("tenantId") tenantId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<Array<Any>>

    @Modifying
    @Query("DELETE FROM Posting p WHERE p.voucherId = :voucherId AND p.rowNumber = :rowNumber AND p.tenantId = :tenantId")
    fun deleteByVoucherIdAndRowNumber(
        @Param("voucherId") voucherId: Long,
        @Param("rowNumber") rowNumber: Int,
        @Param("tenantId") tenantId: Long
    )

    @Modifying
    @Query("UPDATE Posting p SET p.rowNumber = p.rowNumber - 1 WHERE p.voucherId = :voucherId AND p.rowNumber > :deletedRowNumber AND p.tenantId = :tenantId")
    fun decrementRowNumbersAfterDeleted(
        @Param("voucherId") voucherId: Long,
        @Param("deletedRowNumber") deletedRowNumber: Int,
        @Param("tenantId") tenantId: Long
    )
}
