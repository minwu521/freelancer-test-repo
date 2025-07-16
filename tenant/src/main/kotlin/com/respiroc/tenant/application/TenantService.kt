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
import java.text.Normalizer
import java.util.regex.Pattern

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
        tenant.slug = generateSlug(company.name)
        return tenantRepository.saveAndFlush(tenant)
    }

    fun createNewTenant(command: CreateCompanyPayload): Tenant {
        val company = companyService.getOrCreateCompany(command)
        val tenant = Tenant()
        tenant.company = company
        tenant.slug = generateSlug(company.name)
        return tenantRepository.saveAndFlush(tenant)
    }

    fun findTenantBySlug(slug: String): Tenant? {
        return tenantRepository.findBySlug(slug)
    }

    fun findTenantRoleByCode(role: TenantRoleCode): TenantRole {
        return tenantRoleRepository.findByCode(role.code)
    }

    private fun generateSlug(name: String): String {
        val normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\p{InCombiningDiacriticalMarks}+")
        val slug = pattern.matcher(normalized).replaceAll("").lowercase().replace(" ", "-")
        var finalSlug = slug
        var counter = 1
        while (tenantRepository.findBySlug(finalSlug) != null) {
            finalSlug = "$slug-${counter++}"
        }
        return finalSlug
    }
}
