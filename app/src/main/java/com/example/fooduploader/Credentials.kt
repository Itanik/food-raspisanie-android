package com.example.fooduploader

import kotlinx.serialization.Serializable

@Serializable
data class Credentials(
    val host: String,
    val user: String,
    val password: String
)