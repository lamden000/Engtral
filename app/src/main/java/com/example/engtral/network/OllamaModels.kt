package com.example.engtral.network
import com.google.gson.annotations.SerializedName

data class MistralRequest(
    @SerializedName("model") val model: String,
    @SerializedName("prompt") val prompt: String,
    @SerializedName("context") val context: List<Int>? = null, // Change to List<Int>?
)
data class MistralResponse(
    val response: String,
    val context: List<Int>? // Nullable in case it's not always present
)
data class LlavaRequest(
    @SerializedName("model") val model: String = "llava",
    @SerializedName("prompt") val prompt: String,
    @SerializedName("images") val images: List<String>? = null // Image paths or base64-encoded strings
)

data class LlavaResponse(
    val response: String,
    val context: List<Int>? = null
)