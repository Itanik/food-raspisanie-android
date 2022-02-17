package com.example.fooduploader.data

import kotlinx.serialization.Serializable

@Serializable
data class Credentials(
    val host: String,
    val user: String,
    val password: String
)