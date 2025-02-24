package com.example.engtral.network

data class MistralResponseChunk(
    val model: String,
    val createdAt: String,
    val response: String,
    val done: Boolean
)