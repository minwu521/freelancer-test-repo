package com.respiroc.user.domain.repository

import com.respiroc.user.domain.model.UserSession
import com.respiroc.util.repository.CustomJpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserSessionRepository : CustomJpaRepository<UserSession, Long> {

    fun findByToken(token: String): UserSession?
}