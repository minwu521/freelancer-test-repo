package com.respiroc.supplier.domain.repository

import com.respiroc.supplier.domain.model.Supplier
import com.respiroc.util.repository.CustomJpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SupplierRepository : CustomJpaRepository<Supplier, Long> {
    @Query(
        """
        SELECT s FROM Supplier s
        LEFT JOIN FETCH s.company company
        LEFT JOIN FETCH s.person person
        WHERE s.tenantId = :tenantId
        """
    )
    fun findSuppliersByTenantId(@Param("tenantId") tenantId: Long): List<Supplier>

    @Query(
        """
        SELECT s FROM Supplier s
        LEFT JOIN FETCH s.company company
        LEFT JOIN FETCH s.person person
        WHERE s.tenantId = :tenantId
        AND (
                (person.name ILIKE '%' || :name || '%') 
                OR 
                (company.name ILIKE '%' || :name || '%')
            )
    """
    )
    fun findSuppliersByNameContainingIgnoreCaseAndTenantId(
        @Param("name") name: String,
        @Param("tenantId") tenantId: Long
    ): List<Supplier>

    fun existsByIdAndTenantId(id: Long, tenantId: Long): Boolean

    fun existsSuppliersByCompany_NameAndCompany_OrganizationNumberAndTenantId(companyName: String, companyOrganizationNumber: String, tenantId: Long): Boolean

    fun existsSuppliersByPerson_NameAndTenantId(personName: String, tenantId: Long): Boolean
}