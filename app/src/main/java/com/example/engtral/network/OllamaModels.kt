package com.example.engtral.network
import com.google.gson.annotations.SerializedName

data class OllamaRequest(
    @SerializedName("model") val model: String,
    @SerializedName("prompt") val prompt: String,
    @SerializedName("context") val context: List<String>? = null, // Change to List<Int>?
    @SerializedName("images") val images: List<String>? = null
)
data class OllamaResponse(
    val response: String,
    val context: String? // Nullable in case it's not always present
)
