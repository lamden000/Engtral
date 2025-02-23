package com.example.engtral.network
import com.google.gson.annotations.SerializedName

data class OllamaRequest(
    @SerializedName("model") val model: String,
    @SerializedName("prompt") val prompt: String,
    @SerializedName("context") val context: List<Int>? = null, // Change to List<Int>?
    @SerializedName("images") val images: List<String>? = null
)
data class OllamaResponse(
    val response: String,
    val context: List<Int>? // Nullable in case it's not always present
)
