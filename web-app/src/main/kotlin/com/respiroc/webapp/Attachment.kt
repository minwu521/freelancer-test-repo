package com.respiroc.webapp

import com.respiroc.util.repository.CustomJpaRepository
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.springframework.stereotype.Repository
import java.time.Instant

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