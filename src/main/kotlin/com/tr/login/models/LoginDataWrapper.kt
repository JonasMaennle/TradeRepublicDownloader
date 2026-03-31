package com.tr.login.models

data class LoginDataWrapper (
    val loginResponse: LoginResponse,
    val awsHeaderToken: String,
)