package com.respiroc.user.domain.repository

import com.respiroc.user.domain.model.UserTenantRole
import com.respiroc.util.repository.CustomJpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserTenantRoleRepository : CustomJpaRepository<UserTenantRole, Long> {

}