package com.respiroc.webapp

/* ---------- iText 7 & imaging ---------- */
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.WriterProperties
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.respiroc.util.repository.CustomJpaRepository
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Instant
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.stream.ImageOutputStream


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


@Service
class AttachmentService {

    private val imageMimeTypes = setOf(
        "image/png",
        "image/jpeg",
        "image/jpg",
        "image/bmp",
        "image/gif"
    )

    fun convertToPdf(
        fileData: ByteArray,
        filename: String,
        mimeType: String
    ): Triple<ByteArray, String, String> {

        /* ---- short‑circuit for existing PDF ---- */
        if (mimeType.equals("application/pdf", true)) {
            return Triple(fileData, ensurePdfExt(filename), "application/pdf")
        }

        val lowerMime = mimeType.lowercase()
        require(lowerMime in imageMimeTypes) {
            "Unsupported MIME type “$mimeType”. Allowed: PNG, JPEG, BMP, GIF, or PDF."
        }

        val buffered = ImageIO.read(ByteArrayInputStream(fileData))
            ?: throw IllegalArgumentException("Unable to decode image data for $mimeType")

        val jpegBytes = bufferedToJpegBytes(buffered)

        val pdfBaos = ByteArrayOutputStream()
        val writer = PdfWriter(pdfBaos, WriterProperties().setCompressionLevel(9))
        val pdfDoc = PdfDocument(writer)
        val document = Document(pdfDoc, PageSize.LETTER)

        val imgData = ImageDataFactory.create(jpegBytes)
        val image = Image(imgData).apply {
            scaleToFit(PageSize.LETTER.width, PageSize.LETTER.height)
        }
        document.add(image)
        document.close()   // closes pdfDoc & writer

        return Triple(pdfBaos.toByteArray(), ensurePdfExt(filename), "application/pdf")
    }

    private fun bufferedToJpegBytes(src: BufferedImage): ByteArray {
        // --- force sRGB without alpha ------------------------------------------
        val rgbImg = if (src.type == BufferedImage.TYPE_3BYTE_BGR ||
            src.type == BufferedImage.TYPE_INT_RGB) {
            src
        } else {
            val converted = BufferedImage(src.width, src.height,
                BufferedImage.TYPE_3BYTE_BGR)
            val g = converted.createGraphics()
            g.drawImage(src, 0, 0, java.awt.Color.WHITE, null) // fills alpha w/ white
            g.dispose()
            converted
        }

        val quality = 0.70f
        ByteArrayOutputStream().use { baos ->
            val writer = ImageIO.getImageWritersByFormatName("jpg").next()
            ImageIO.createImageOutputStream(baos).use { ios ->
                writer.output = ios
                writer.defaultWriteParam.apply {
                    if (canWriteCompressed()) {
                        compressionMode = ImageWriteParam.MODE_EXPLICIT
                        compressionQuality = quality
                    }
                }.also { param ->
                    writer.write(null, IIOImage(rgbImg, null, null), param)
                }
                writer.dispose()
            }
            return baos.toByteArray()
        }
    }


    private fun ensurePdfExt(name: String): String =
        if (name.lowercase().endsWith(".pdf")) name else "${name.substringBeforeLast('.', name)}.pdf"
}
