package com.respiroc.webapp

import com.respiroc.util.repository.CustomJpaRepository
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.springframework.stereotype.Repository
import java.time.Instant
import java.io.ByteArrayOutputStream
import org.springframework.stereotype.Service
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject

@Entity
@Table(name = "attachments")
class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "file_data", nullable = false, columnDefinition = "BYTEA")
    lateinit var fileData: ByteArray

    @Column(name = "filename", nullable = false)
    lateinit var filename: String

    @Column(name = "mimetype", nullable = false)
    lateinit var mimetype: String

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant? = null
}

@Repository
interface AttachmentRepository : CustomJpaRepository<Attachment, Long>

/**
 * Converts incoming images to single‑page PDFs so that the system stores *only* PDF files.
 * Uses Apache PDFBox 3 (latest) for the conversion.
 */
@Service
class AttachmentService {

    private val imageMimeTypes = setOf(
        "image/png", "image/jpeg", "image/jpg",
        "image/bmp", "image/gif", "image/tiff", "image/webp"
    )

    /**
     * @return Triple(pdfBytes, safeFilename, "application/pdf")
     */
    fun convertToPdf(
        fileData: ByteArray,
        filename: String,
        mimeType: String
    ): Triple<ByteArray, String, String> {

        // Already a PDF → no work needed
        if (mimeType == "application/pdf") {
            return Triple(fileData, ensurePdfExt(filename), mimeType)
        }

        // Only images are supported for conversion; everything else is rejected
        require(mimeType.lowercase() in imageMimeTypes) {
            "Unsupported file type $mimeType – only image/* or application/pdf allowed"
        }

        PDDocument().use { doc ->
            val img = PDImageXObject.createFromByteArray(doc, fileData, filename)
            val page = PDPage(PDRectangle.LETTER)
            doc.addPage(page)

            // Scale image to fit page while keeping aspect ratio
            val pageW = page.mediaBox.width
            val pageH = page.mediaBox.height
            val imgW = img.width.toFloat()
            val imgH = img.height.toFloat()
            val scale = minOf(pageW / imgW, pageH / imgH)
            val drawW = imgW * scale
            val drawH = imgH * scale
            val offsetX = (pageW - drawW) / 2
            val offsetY = (pageH - drawH) / 2

            PDPageContentStream(doc, page).use { cs ->
                cs.drawImage(img, offsetX, offsetY, drawW, drawH)
            }

            val baos = ByteArrayOutputStream()
            doc.save(baos)
            return Triple(baos.toByteArray(), ensurePdfExt(filename), "application/pdf")
        }
    }

    private fun ensurePdfExt(name: String) =
        if (name.lowercase().endsWith(".pdf")) name else name.substringBeforeLast('.', name) + ".pdf"
}
