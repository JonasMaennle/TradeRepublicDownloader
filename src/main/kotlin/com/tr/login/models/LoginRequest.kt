package com.tr.login.models

data class LoginRequest(
    val phoneNumber: String,
    val pin: String
)