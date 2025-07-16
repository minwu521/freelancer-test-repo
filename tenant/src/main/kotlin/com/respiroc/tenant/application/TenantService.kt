package com.respiroc.tenant.application

import com.respiroc.company.application.CompanyService
import com.respiroc.company.application.payload.CreateCompanyPayload
import com.respiroc.company.domain.model.Company
import com.respiroc.tenant.domain.model.Tenant
import com.respiroc.tenant.domain.model.TenantRole
import com.respiroc.tenant.domain.repository.TenantRepository
import com.respiroc.tenant.domain.repository.TenantRoleRepository
import com.respiroc.util.constant.TenantRoleCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TenantService(
    private val companyService: CompanyService,
    private val tenantRepository: TenantRepository,
    private val tenantRoleRepository: TenantRoleRepository
) {

    fun createNewTenant(companyId: Long): Tenant {
        val tenant = Tenant()
        val company = Company()
        company.id = companyId
        tenant.company = company
        return tenantRepository.saveAndFlush(tenant)
    }

    fun createNewTenant(command: CreateCompanyPayload): Tenant {
        // TODO add slug here as well when company is created, make sure it is not duplicate, on conflict add increasing number
        val company = companyService.getOrCreateCompany(command)
        return createNewTenant(company.id)
    }

    fun findTenantBySlug(slug: String): Tenant? {
        // find directly from tenant table, need to add slug to tenant
    }

    fun findTenantRoleByCode(role: TenantRoleCode): TenantRole {
        return tenantRoleRepository.findByCode(role.code)
    }
}