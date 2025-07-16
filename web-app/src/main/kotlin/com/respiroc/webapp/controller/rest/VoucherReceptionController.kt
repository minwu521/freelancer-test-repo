package com.respiroc.webapp.controller.rest

import com.respiroc.tenant.application.TenantService
import com.respiroc.webapp.model.VoucherDocument
import com.respiroc.webapp.service.VoucherReceptionService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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
        val fileSize: Long,
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

        if (request.fileSize > 25 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(mapOf("error" to "File too large"))
        }

        val fileData = try {
            Base64.getDecoder().decode(request.fileData)
        } catch (_: Exception) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Invalid base64 data"))
        }

        // convert and compress file as pdf if image, else just compress pdf




        return ResponseEntity.ok(
            mapOf<String, Any>(
                "id" to (saved.id ?: 0),
                "filename" to saved.filename,
                "status" to "received"
            )
        )
    }
}