package com.example.engtral

import android.content.Context
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.EditText
import android.widget.Button
import android.widget.TextView
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.engtral.network.LLaVaResponseChunk
import com.example.engtral.network.LlavaRequest
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import java.io.ByteArrayOutputStream
import java.io.InputStream


class LlavaActivity : AppCompatActivity() {


    private lateinit var inputText: EditText
    private lateinit var sendButton: Button
    private lateinit var responseText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var selectImageButton: Button
    private var base64Image: String? = null

    private val imagePickerResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                encodeImageToBase64(imageUri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_llava)

        inputText = findViewById(R.id.inputText)
        sendButton = findViewById(R.id.sendButton)
        responseText = findViewById(R.id.responseText)
        progressBar = findViewById(R.id.progressBar)
        selectImageButton = findViewById(R.id.selectImageButton)

        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerResult.launch(intent)
        }

        sendButton.setOnClickListener {
            val prompt = inputText.text.toString()
            if (prompt.isNotEmpty()) {
                responseText.text = ""
                sendLlavaRequest(prompt, base64Image)
            } else {
                Toast.makeText(this, "Enter a prompt!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun encodeImageToBase64(imageUri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)
            inputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error encoding image", Toast.LENGTH_SHORT).show()
        }
    }


    private fun sendLlavaRequest(prompt: String, base64Image: String?) {
        progressBar.visibility = View.VISIBLE

        val request = LlavaRequest(model = "llava", prompt = prompt, images = base64Image?.let { listOf(it) })

        RetrofitClient.instance.chatWithLlava(request).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        try {
                            val inputStream = responseBody.byteStream()
                            val reader = BufferedReader(InputStreamReader(inputStream))
                            val fullResponse = StringBuilder()

                            reader.useLines { lines ->
                                lines.forEach { line ->
                                    val trimmedLine = line.trim()
                                    if (trimmedLine.isNotEmpty()) {
                                        try {
                                            val chunk = Gson().fromJson(trimmedLine, LLaVaResponseChunk::class.java)
                                            Log.d("LLaVAResponse", "Parsed chunk: $chunk")

                                            val cleanedResponse = chunk.response
                                                .trim('"')
                                                .trim()
                                                .replace("\\s+".toRegex(), " ")
                                                .replace("\\u00A0", " ")
                                                .replace("\\u200B", "")
                                            fullResponse.append(cleanedResponse).append(" ")

                                            runOnUiThread {
                                                val currentResponse = fullResponse.toString().trim()
                                                responseText.text = currentResponse
                                                Log.d("LLaVAResponse", "Current response: $currentResponse")
                                            }

                                            if (chunk.isDone() && chunk.normalizedModel() == "llava") {
                                                progressBar.visibility = View.GONE
                                            }
                                        } catch (e: Exception) {
                                            Log.e("LLaVAResponse", "Parsing error: $e, Line: $trimmedLine")
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }

                        } catch (e: IOException) {
                            e.printStackTrace()
                            runOnUiThread {
                                progressBar.visibility = View.GONE
                                responseText.text = "Error reading response: ${e.message}"
                            }
                        } finally {
                            runOnUiThread {
                                progressBar.visibility = View.GONE
                            }
                        }
                    }
                } else {
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        responseText.text = "Error: ${response.errorBody()?.string()}"
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    responseText.text = "Error: ${t.message}"
                }
            }
        })
    }

}
