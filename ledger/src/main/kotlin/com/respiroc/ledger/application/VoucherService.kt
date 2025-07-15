package com.respiroc.ledger.application

import com.respiroc.ledger.application.payload.CreatePostingPayload
import com.respiroc.ledger.application.payload.CreateVoucherPayload
import com.respiroc.ledger.application.payload.VoucherPayload
import com.respiroc.ledger.application.payload.VoucherSummaryPayload
import com.respiroc.ledger.domain.exception.AccountNotFoundException
import com.respiroc.ledger.domain.exception.InvalidPostingsException
import com.respiroc.ledger.domain.exception.InvalidVatCodeException
import com.respiroc.ledger.domain.exception.PostingsNotBalancedException
import com.respiroc.ledger.domain.model.Posting
import com.respiroc.ledger.domain.model.Voucher
import com.respiroc.ledger.domain.repository.PostingRepository
import com.respiroc.ledger.domain.repository.VoucherRepository
import com.respiroc.util.context.ContextAwareApi
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
@Transactional
class VoucherService(
    private val voucherRepository: VoucherRepository,
    private val postingRepository: PostingRepository,
    private val accountService: AccountService,
    private val vatService: VatService
) : ContextAwareApi {

    fun createVoucher(payload: CreateVoucherPayload): VoucherPayload {
        val tenantId = currentTenantId()

        val voucherNumber = generateNextVoucherNumber(tenantId, payload.date)

        if (payload.postings.isNotEmpty()) {
            validatePostingCommands(payload.postings)
            validateBalance(payload.postings)
        }

        val voucher = createVoucherEntity(payload, tenantId, voucherNumber)
        val savedVoucher = voucherRepository.save(voucher)

        if (payload.postings.isNotEmpty()) {
            saveNonZeroPostings(payload.postings, tenantId, savedVoucher.id)
        }

        return VoucherPayload(
            id = savedVoucher.id,
            number = savedVoucher.getDisplayNumber(),
            date = savedVoucher.date
        )
    }

    @Transactional(readOnly = true)
    fun findAllVoucherSummaries(): List<VoucherSummaryPayload> {
        val vouchers = voucherRepository.findVoucherSummariesByTenantId(currentTenantId())

        return vouchers.map { voucher ->
            VoucherSummaryPayload(
                id = voucher.id,
                number = voucher.getDisplayNumber(),
                date = voucher.date,
                description = voucher.description,
                postingCount = voucher.postings.size
            )
        }
    }

    fun updateVoucherWithPostings(voucherId: Long, postings: List<CreatePostingPayload>): VoucherPayload {
        val tenantId = currentTenantId()

        val voucher = voucherRepository.findByIdAndTenantIdWithPostings(voucherId, tenantId)
            ?: throw IllegalArgumentException("Voucher not found")

        if (postings.isNotEmpty()) {
            validatePostingCommands(postings)
            validateBalance(postings)
        }

        if (voucher.postings.isNotEmpty()) {
            postingRepository.deleteAll(voucher.postings)
        }

        if (postings.isNotEmpty()) {
            saveNonZeroPostings(postings, tenantId, voucherId)
        }

        return VoucherPayload(
            id = voucher.id,
            number = voucher.getDisplayNumber(),
            date = voucher.date
        )
    }

    @Transactional(readOnly = true)
    fun findVoucherById(id: Long): Voucher? {
        return voucherRepository.findByIdAndTenantIdWithPostings(id, currentTenantId())
    }

    fun deletePostingLineAndAdjustRowNumbers(voucherId: Long, rowNumber: Int) {
        val tenantId = currentTenantId()

        postingRepository.deleteByVoucherIdAndRowNumber(voucherId, rowNumber, tenantId)
        postingRepository.decrementRowNumbersAfterDeleted(voucherId, rowNumber, tenantId)
    }

    // -------------------------------
    // Private Helper Methods
    // -------------------------------

    private fun generateNextVoucherNumber(tenantId: Long, date: LocalDate): Short {
        val maxNumber = voucherRepository.findMaxVoucherNumberForYear(tenantId, date.year)
        return (maxNumber + 1).toShort()
    }

    private fun validatePostingCommands(postings: List<CreatePostingPayload>) {
        // Ensure there are at least 2 non-zero postings for valid double-entry bookkeeping
        val nonZeroPostingsCount = postings.count { it.amount.compareTo(BigDecimal.ZERO) != 0 }
        if (nonZeroPostingsCount < 2) {
            throw InvalidPostingsException()
        }

        postings.forEach { postingData ->
            validateAccount(postingData.accountNumber)
            validateVatCode(postingData.vatCode)
        }
    }

    private fun validateAccount(accountNumber: String) {
        if (accountService.findAccountByNumber(accountNumber) == null) {
            throw AccountNotFoundException(accountNumber)
        }
    }

    private fun validateVatCode(vatCode: String?) {
        if (vatCode != null && !vatService.vatCodeExists(vatCode)) {
            throw InvalidVatCodeException(vatCode)
        }
    }

    private fun validateBalance(postings: List<CreatePostingPayload>) {
        val totalAmount = postings.sumOf { it.amount }
        // Round to 2 decimal places for balance validation (currency conversion can introduce extra decimals)
        val roundedAmount = totalAmount.setScale(2, java.math.RoundingMode.HALF_UP)
        if (roundedAmount.compareTo(BigDecimal.ZERO) != 0) {
            throw PostingsNotBalancedException(roundedAmount)
        }
    }

    private fun createVoucherEntity(payload: CreateVoucherPayload, tenantId: Long, voucherNumber: Short): Voucher {
        val voucher = Voucher()
        voucher.number = voucherNumber
        voucher.date = payload.date
        voucher.description = payload.description
        voucher.tenantId = tenantId
        return voucher
    }

    private fun createPostingEntity(
        postingData: CreatePostingPayload,
        tenantId: Long,
        voucherId: Long
    ): Posting {
        val posting = Posting()
        posting.accountNumber = postingData.accountNumber
        posting.amount = postingData.amount
        posting.currency = postingData.currency
        posting.postingDate = postingData.postingDate
        posting.description = postingData.description
        posting.tenantId = tenantId
        posting.originalAmount = postingData.originalAmount
        posting.originalCurrency = postingData.originalCurrency
        posting.vatCode = postingData.vatCode
        posting.voucherId = voucherId

        return posting
    }

    private fun saveNonZeroPostings(
        postings: List<CreatePostingPayload>,
        tenantId: Long,
        voucherId: Long
    ) {
        val nonZeroPostings = postings
            .filter { it.amount.compareTo(BigDecimal.ZERO) != 0 }
            .map { postingData ->
                createPostingEntity(postingData, tenantId, voucherId)
            }

        // Save only non-zero postings to optimize storage
        if (nonZeroPostings.isNotEmpty()) {
            postingRepository.saveAll(nonZeroPostings)
        }
    }
}
