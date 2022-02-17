package com.example.fooduploader.data

import kotlinx.serialization.Serializable

@Serializable
data class Food(
    val name: String,
    val path: String
)