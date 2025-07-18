package com.respiroc.customer.domain.repository

import com.respiroc.customer.domain.model.Customer
import com.respiroc.util.repository.CustomJpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepository : CustomJpaRepository<Customer, Long> {
    @Query(
        """
        SELECT c FROM Customer c
        LEFT JOIN FETCH c.company company
        LEFT JOIN FETCH c.person person
        WHERE c.tenantId = :tenantId
    """
    )
    fun findCustomersByTenantId(@Param("tenantId") tenantId: Long): List<Customer>

    @Query(
        """
        SELECT c FROM Customer c
        LEFT JOIN FETCH c.company company
        LEFT JOIN FETCH c.person person
        WHERE c.tenantId = :tenantId
        AND (
                (person.name ILIKE '%' || :name || '%') 
                OR 
                (company.name ILIKE '%' || :name || '%')
            )
    """
    )
    fun findCustomersByNameContainingIgnoreCaseAndTenantId(
        @Param("name") name: String,
        @Param("tenantId") tenantId: Long
    ): List<Customer>

    fun existsByIdAndTenantId(id: Long, tenantId: Long): Boolean

    fun existsCustomersByCompany_NameAndCompany_OrganizationNumberAndTenantId(
        companyName: String,
        companyOrganizationNumber: String,
        tenantId: Long
    ): Boolean

    fun existsCustomersByPerson_NameAndTenantId(personName: String, tenantId: Long): Boolean
}