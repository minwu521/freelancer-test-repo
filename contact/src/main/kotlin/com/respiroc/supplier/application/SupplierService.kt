package com.respiroc.supplier.application

import com.respiroc.common.payload.NewCustomerSupplierPayload
import com.respiroc.common.service.BaseService
import com.respiroc.supplier.domain.model.Supplier
import com.respiroc.supplier.domain.repository.SupplierRepository
import com.respiroc.tenant.domain.model.Tenant
import com.respiroc.util.exception.ContactExistException
import com.respiroc.util.exception.ContactNotFoundException
import org.springframework.stereotype.Service

@Service
class SupplierService(
    private val supplierRepository: SupplierRepository,
    private val baseService: BaseService
) {

    fun createNewSupplier(
        payload: NewCustomerSupplierPayload,
        tenantId: Long
    ): Supplier {
        val tenant = Tenant().apply { id = tenantId }
        return if (payload.privateCustomer) createPrivateSupplier(payload, tenant)
        else createCompanySupplier(payload, tenant)
    }

    private fun createPrivateSupplier(payload: NewCustomerSupplierPayload, tenant: Tenant): Supplier {
        val person = baseService.getOrCreatePerson(payload)
        if (supplierRepository.existsSuppliersByPerson_NameAndTenantId(person.name, tenant.id))
            throw ContactExistException("Supplier already exists")
        return supplierRepository.save(
            Supplier().apply {
                this.person = person
                this.tenant = tenant
            }
        )
    }

    private fun createCompanySupplier(payload: NewCustomerSupplierPayload, tenant: Tenant): Supplier {
        val company = baseService.getOrCreateCompany(payload)
        if (supplierRepository.existsSuppliersByCompany_NameAndCompany_OrganizationNumberAndTenantId(
                company.name, company.organizationNumber, tenant.id
            )
        ) {
            throw ContactExistException("Supplier already exists")
        }
        return supplierRepository.save(
            Supplier().apply {
                this.company = company
                this.tenant = tenant
            }
        )
    }

    fun deleteByIdAndTenantId(id: Long, tenantId: Long) {
        val exists = supplierRepository.existsByIdAndTenantId(id, tenantId)
        if (!exists)
            throw ContactNotFoundException("Supplier with id=$id and tenantId=$tenantId not found.")
        supplierRepository.deleteById(id)
    }

    fun findAllSupplierByTenantId(tenantId: Long): List<Supplier> {
        return supplierRepository.findSuppliersByTenantId(tenantId)
    }

    fun findByNameContainingAndTenantId(name: String, tenantId: Long): List<Supplier> {
        return supplierRepository.findSuppliersByNameContainingIgnoreCaseAndTenantId(name, tenantId)
    }
}