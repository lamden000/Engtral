import com.example.engtral.network.LlavaRequest
import com.example.engtral.network.MistralRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface OllamaApi {
    @POST("/api/generate")
    fun chatWithMistral(@Body request: MistralRequest): Call<ResponseBody> // Change return type
    @POST("/api/generate")
    fun chatWithLlava(@Body request: LlavaRequest): Call<ResponseBody>
}