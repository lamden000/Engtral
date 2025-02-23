package com.example.engtral

import RetrofitClient
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.engtral.network.OllamaRequest
import com.example.engtral.network.OllamaResponse
import com.example.engtral.network.OllamaResponseChunk
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private var chatContext: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputField = findViewById<EditText>(R.id.inputField)
        val sendButton = findViewById<Button>(R.id.sendButton)
        val responseText = findViewById<TextView>(R.id.responseText)

        sendButton.setOnClickListener {
            val userInput = inputField.text.toString()
            chatWithOllama(userInput, responseText)
        }
    }

    private fun chatWithOllama(prompt: String, responseText: TextView) {
        val request: OllamaRequest
        val contextList: List<String>? = if (chatContext.isNotEmpty()) {
            Gson().fromJson(chatContext, object : TypeToken<List<Int>>() {}.type)
        } else {
            listOf()
        }

        request = OllamaRequest(model = "mistral", prompt = prompt, context = contextList)

        RetrofitClient.instance.chatWithOllama(request).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val inputStream = response.body()?.byteStream()
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val gson = Gson()
                        val jsonReader = JsonReader(reader)
                        jsonReader.isLenient = true

                        var fullResponse = ""
                        var context : String? = null

                        while (reader.ready()) {
                            val line = reader.readLine()
                            if (line != null && line.isNotEmpty()) {
                                try {
                                    val chunk = gson.fromJson(line, OllamaResponseChunk::class.java)
                                    fullResponse += chunk.response
                                    context = gson.fromJson(line, OllamaResponse::class.java).context

                                    runOnUiThread {
                                        responseText.text = fullResponse
                                    }
                                } catch (e: Exception) {
                                    Log.e("OllamaResponse", "Error parsing chunk: ${e.message}", e)
                                }
                            }
                        }
                        if (context != null){
                            chatContext = gson.toJson(context)
                        }

                    } catch (e: Exception) {
                        Log.e("OllamaResponse", "Error reading response: ${e.message}", e)
                        runOnUiThread {
                            responseText.text = "Error reading response"
                        }
                    }
                } else {
                    runOnUiThread {
                        responseText.text = "Error: ${response.code()}"
                    }
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("OllamaResponse", "Network Failure: ${t.message}")
                responseText.text = "Failed: ${t.message}"
            }
        })
    }
}
