package com.respiroc.webapp.controller.rest

import com.respiroc.company.api.CompanyInternalApi
import com.respiroc.tenant.infrastructure.context.TenantContextHolder
import com.respiroc.webapp.model.VoucherDocument
import com.respiroc.webapp.service.VoucherReceptionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.Base64

@RestController
@RequestMapping("/api/voucher-reception")
class VoucherReceptionController(
    private val voucherReceptionService: VoucherReceptionService,
    private val companyInternalApi: CompanyInternalApi
) {

    data class EmailDocumentRequest(
        val filename: String,
        val mimeType: String,
        val fileSize: Long,
        val fileData: String, // base64 encoded
        val senderEmail: String
    )

    // Endpoint for Cloudflare Worker email reception
    @PostMapping("/documents")
    fun receiveDocumentFromEmail(
        @RequestHeader("X-Company-Slug") companySlug: String,
        @RequestBody request: EmailDocumentRequest
    ): ResponseEntity<Map<String, Any>> {
        
        // Find company by slug
        val company = companyInternalApi.findCompanyBySlug(companySlug)
            ?: return ResponseEntity.badRequest().body(mapOf("error" to "Company not found"))
        
        // Log company information
        println("=== EMAIL RECEIVED FOR COMPANY ===")
        println("Company Slug: $companySlug")
        println("Company ID: ${company.id}")
        println("Company Name: ${company.name}")
        println("Tenant ID: ${company.tenant.id}")
        println("Tenant Name: ${company.tenant.name}")
        println("Sender Email: ${request.senderEmail}")
        println("File: ${request.fileData} ")
        println("===================================")
        
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
        println("File: $fileData ")
        println("===================================")

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
        
        return ResponseEntity.ok(mapOf<String, Any>(
            "id" to (saved.id ?: 0),
            "filename" to saved.filename,
            "status" to "received"
        ))
    }
}