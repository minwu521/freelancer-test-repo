package com.respiroc.webapp

import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AttachmentServiceTest {

    private val attachmentService = AttachmentService()

    @Test
    fun `convertToPdf should convert test png to PDF`() {
        // Given
        val testPngFile = File("src/test/kotlin/com/respiroc/webapp/test.png")
        val pngData = testPngFile.readBytes()
        val filename = "test.png"
        val mimeType = "image/png"

        // When
        val result = attachmentService.convertToPdf(pngData, filename, mimeType)

        // Then
        assertNotNull(result.first)
        assertTrue(result.first.isNotEmpty())
        assertEquals("test.pdf", result.second)
        assertEquals("application/pdf", result.third)
        
        // Verify it's a valid PDF by checking PDF header
        val pdfHeader = result.first.sliceArray(0..3)
        assertEquals("%PDF", String(pdfHeader))
        
        // Write the PDF to disk
        val outputFile = File("src/test/kotlin/com/respiroc/webapp/test.pdf")
        outputFile.writeBytes(result.first)
        assertTrue(outputFile.exists())
        assertTrue(outputFile.length() > 0)
    }
}