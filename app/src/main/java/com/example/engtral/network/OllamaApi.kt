import com.example.engtral.network.OllamaRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface OllamaApi {
    @POST("/api/generate")
    fun chatWithOllama(@Body request: OllamaRequest): Call<ResponseBody> // Change return type
}