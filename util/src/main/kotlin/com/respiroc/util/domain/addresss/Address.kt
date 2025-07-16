package com.respiroc.util.domain.addresss

import jakarta.persistence.*

@Entity
@Table(name = "addresses")
class Address() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long = -1

    @Column(name = "country_iso_code", nullable = false, length = 2)
    var countryIsoCode: String = "NO"

    @Column(name = "administrative_division_code")
    var administrativeDivisionCode: String? = null

    @Column(name = "city", nullable = false)
    var city: String = ""

    @Column(name = "postal_code")
    var postalCode: String? = null

    @Column(name = "address_1", nullable = false)
    var addressPart1: String = ""

    @Column(name = "address_2")
    var addressPart2: String? = null

    constructor(
        countryIsoCode: String,
        administrativeDivisionCode: String?,
        city: String,
        postalCode: String?,
        addressPart1: String,
        addressPart2: String?
    ) : this() {
        this.countryIsoCode = countryIsoCode.uppercase()
        this.city = normalizeAddressField(city)!!
        this.postalCode = removeSpaces(postalCode)
        this.administrativeDivisionCode = removeSpaces(administrativeDivisionCode)?.uppercase()
        this.addressPart1 = normalizeAddressField(addressPart1)!!
        this.addressPart2 = normalizeAddressField(addressPart2)
    }

    private fun normalizeAddressField(input: String?): String? {
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
        fun upsertAddress(entityManager: EntityManager, address: Address): Address {
            val query = entityManager.createNativeQuery(query, Address::class.java)
            setAddressParameters(query, address)
            return query.singleResult as Address
        }

        private fun setAddressParameters(query: Query, address: Address) {
            query.setParameter("countryIsoCode", address.countryIsoCode)
            query.setParameter("adminDivisionCode", address.administrativeDivisionCode)
            query.setParameter("city", address.city)
            query.setParameter("postalCode", address.postalCode)
            query.setParameter("streetAddress1", address.addressPart1)
            query.setParameter("streetAddress2", address.addressPart2)
        }

        private val query =
            """
            INSERT INTO addresses (
                country_iso_code,
                administrative_division_code,
                city,
                postal_code,
                address_1,
                address_2
            )
            VALUES (
                :countryIsoCode,
                :adminDivisionCode,
                :city,
                :postalCode,
                :streetAddress1,
                :streetAddress2
            )
            ON CONFLICT (country_iso_code, administrative_division_code, city, postal_code, address_1, address_2)
            DO UPDATE SET 
                administrative_division_code = EXCLUDED.administrative_division_code
            RETURNING *;
            """.trimIndent()
    }
}