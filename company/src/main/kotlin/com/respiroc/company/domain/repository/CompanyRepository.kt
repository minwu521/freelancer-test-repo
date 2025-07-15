package com.respiroc.company.domain.repository

import com.respiroc.company.domain.model.Company
import com.respiroc.util.repository.CustomJpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CompanyRepository : CustomJpaRepository<Company, Long> {
    fun findCompanyByOrganizationNumberAndName(organizationNumber: String, name: String): Company?
}