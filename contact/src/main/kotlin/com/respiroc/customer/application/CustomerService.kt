package com.respiroc.customer.application

import com.respiroc.common.payload.NewContactPayload
import com.respiroc.common.service.BaseService
import com.respiroc.customer.domain.model.Customer
import com.respiroc.customer.domain.repository.CustomerRepository
import com.respiroc.tenant.domain.model.Tenant
import com.respiroc.util.exception.ContactExistException
import com.respiroc.util.exception.ContactNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val baseService: BaseService
) {

    fun createNewCustomer(
        payload: NewContactPayload,
        tenantId: Long
    ): Customer {
        val tenant = Tenant().apply { id = tenantId }
        return if (payload.privateContact) createPrivateCustomer(payload, tenant)
        else createCompanyCustomer(payload, tenant)
    }


    private fun createPrivateCustomer(payload: NewContactPayload, tenant: Tenant): Customer {
        val person = baseService.getOrCreatePerson(payload)
        if (customerRepository.existsCustomersByPerson_NameAndTenantId(person.name, tenant.id))
            throw ContactExistException("Customer already exists")
        return customerRepository.save(
            Customer().apply {
                this.person = person
                this.tenant = tenant
            }
        )
    }

    private fun createCompanyCustomer(payload: NewContactPayload, tenant: Tenant): Customer {
        val company = baseService.getOrCreateCompany(payload)
        if (customerRepository.existsCustomersByCompany_NameAndCompany_OrganizationNumberAndTenantId(
                company.name, company.organizationNumber, tenant.id
            )
        ) {
            throw ContactExistException("Customer already exists")
        }
        return customerRepository.save(
            Customer().apply {
                this.company = company
                this.tenant = tenant
            }
        )
    }

    fun deleteByIdAndTenantId(id: Long, tenantId: Long) {
        val exists = customerRepository.existsByIdAndTenantId(id, tenantId)
        if (!exists)
            throw ContactNotFoundException("Customer with id=$id and tenantId=$tenantId not found.")
        customerRepository.deleteById(id)
    }

    fun findAllCustomerByTenantId(tenantId: Long): List<Customer> {
        return customerRepository.findCustomersByTenantId(tenantId)
    }

    fun findByNameContainingAndTenantId(name: String, tenantId: Long): List<Customer> {
        return customerRepository.findCustomersByNameContainingIgnoreCaseAndTenantId(name, tenantId)
    }
}