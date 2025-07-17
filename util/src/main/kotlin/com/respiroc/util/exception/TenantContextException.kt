package com.respiroc.util.exception

class MissingTenantContextException(override val message: String = "Current tenant is not set in context") :
    BaseException(message)