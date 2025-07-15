package com.respiroc.user.application.payload

import com.respiroc.util.context.SpringUser

data class LoginPayload(val token: String, val user: SpringUser)
