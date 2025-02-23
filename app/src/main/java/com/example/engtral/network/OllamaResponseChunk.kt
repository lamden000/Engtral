package com.example.engtral.network

data class OllamaResponseChunk(
    val model: String,
    val createdAt: String,
    val response: String,
    val done: Boolean
)