package com.respiroc.webapp.controller.rest

import com.respiroc.company.api.CompanyInternalApi
import com.respiroc.tenant.infrastructure.context.TenantContextHolder
import com.respiroc.webapp.model.VoucherDocument
import com.respiroc.webapp.service.VoucherReceptionService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class VoucherReceptionController(
    private val voucherReceptionService: VoucherReceptionService,
    private val companyInternalApi: CompanyInternalApi
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
        val company = companyInternalApi.findCompanyBySlug(companySlug)
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "Company not found"))

        TenantContextHolder.setTenantId(company.tenant.id!!)

        if (request.fileSize > 25 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(mapOf("error" to "File too large"))
        }

        // Decode base64 file data
        val fileData = try {
            Base64.getDecoder().decode(request.fileData)
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Invalid base64 data"))
        }


        val document = VoucherDocument().apply {
            this.company = company
            this.tenant = company.tenant
            this.filename = request.filename
            this.fileData = fileData
            this.mimeType = request.mimeType
            this.fileSize = request.fileSize
            this.senderEmail = request.senderEmail
        }

        val saved = voucherReceptionService.saveDocument(document)

        return ResponseEntity.ok(
            mapOf<String, Any>(
                "id" to (saved.id ?: 0),
                "filename" to saved.filename,
                "status" to "received"
            )
        )
    }
}