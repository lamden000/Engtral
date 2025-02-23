import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://localhost:11434/"  // Use 10.0.2.2 for Emulator
    val gson = GsonBuilder().setLenient().create()
    val instance: OllamaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create()) // Add this for plain text
            .addConverterFactory(GsonConverterFactory.create(gson)) // Keep Gson for JSON parsing
            .build()
            .create(OllamaApi::class.java)
    }
}
