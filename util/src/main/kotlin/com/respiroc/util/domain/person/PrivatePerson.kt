package com.respiroc.util.domain.person

import com.respiroc.util.domain.address.Address
import jakarta.persistence.*
import jakarta.validation.constraints.Size

@Entity
@Table(name = "private_persons")
class PrivatePerson() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long = -1

    @Size(max = 255)
    @Column(name = "name", nullable = false)
    lateinit var name: String

    @Column(name = "address_id", nullable = true, updatable = false, insertable = false)
    var addressId: Long? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_id", nullable = true)
    var address: Address? = null

    constructor(
        name: String,
        address: Address,
    ) : this() {
        this.name = normalizeField(name)!!
        this.address = address
    }

    private fun normalizeField(input: String?): String? {
        if (input == null) return null
        val cleaned = removeSpaces(input, " ") ?: return null
        if (cleaned.isEmpty()) return cleaned
        return cleaned
            .lowercase()
            .split(" ")
            .filter { it.isNotEmpty() }
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercaseChar() } }
    }

    private fun removeSpaces(input: String?, replacement: String = ""): String? {
        return input
            ?.trim()
            ?.replace(Regex("\\s+"), replacement)
    }

    companion object {
        fun upsertPerson(entityManager: EntityManager, person: PrivatePerson): PrivatePerson {
            val query = entityManager.createNativeQuery(query, PrivatePerson::class.java)
            query.setParameter("name", person.name)
            query.setParameter("address_id", person.address?.id)
            return query.singleResult as PrivatePerson
        }

        private val query =
            """
            INSERT INTO private_persons (name, address_id)
            VALUES (:name,:address_id)
            ON CONFLICT (name)
            DO UPDATE SET name = EXCLUDED.name
            RETURNING *;
            """.trimIndent()
    }
}