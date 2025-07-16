package com.respiroc.webapp.controller.rest

import com.respiroc.tenant.application.TenantService
import com.respiroc.tenant.domain.model.Tenant
import com.respiroc.util.repository.CustomJpaRepository
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
class VoucherReceptionController(
    private val voucherReceptionService: VoucherReceptionService,
    private val tenantService: TenantService
) {

    private val logger = LoggerFactory.getLogger(VoucherReceptionController::class.java)

    data class EmailDocumentRequest(
        val filename: String,
        val mimeType: String,
        val fileData: String, // base64 encoded
        val senderEmail: String
    )

    // Cloudflare worker in index.js calls this
    @PostMapping("/api/voucher-reception")
    fun receiveDocumentFromEmail(
        @RequestHeader("X-Company-Slug") companySlug: String,
        @RequestBody request: EmailDocumentRequest
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Receiving Voucher Receipt for $companySlug")
        val tenantId = tenantService.findTenantIdBySlug(companySlug)
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "Company not found"))

        // We are already validating file size in index.js is not too large
        val fileData = try {
            Base64.getDecoder().decode(request.fileData)
        } catch (_: Exception) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Invalid base64 data"))
        }

        // convert and compress file as pdf if image, else just compress pdf

        // then save in DB


        return ResponseEntity.ok(
            mapOf<String, Any>(
                "id" to (saved.id ?: 0),
                "filename" to saved.filename,
                "status" to "received"
            )
        )
    }
}

@Service
@Transactional
class VoucherReceptionService(
    private val voucherDocumentRepository: VoucherDocumentRepository
) {

    fun saveDocument(document: VoucherReceptionDocument): VoucherReceptionDocument {
        return voucherDocumentRepository.save(document)
    }
}

@Repository
interface VoucherDocumentRepository : CustomJpaRepository<VoucherReceptionDocument, Long>

@Entity
@Table(name = "voucher_reception_documents")
class VoucherReceptionDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "attachment_id")
    var attachmentId: Long? = null

    @Column(name = "file_data", columnDefinition = "BYTEA")
    var fileData: ByteArray? = null


    @Column(name = "filename", nullable = false)
    lateinit var filename: String

    @Column(name = "mime_type")
    var mimeType: String? = null

    @CreationTimestamp
    @Column(name = "received_at", nullable = false)
    var receivedAt: Instant? = null

    @Column(name = "sender_email")
    var senderEmail: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    lateinit var tenant: Tenant
}