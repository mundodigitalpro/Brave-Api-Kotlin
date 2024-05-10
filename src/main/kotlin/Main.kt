package dev.josejordan
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import com.google.gson.Gson
import okio.GzipSource
import okio.Buffer

fun main() {
    // Construir el cliente HTTP
    val client = OkHttpClient.Builder().build()

    // Configurar la solicitud
    val request = Request.Builder()
        .url("https://api.search.brave.com/res/v1/news/search?q=cordoba&count=3&country=es&search_lang=es&spellcheck=1")
        .addHeader("Accept", "application/json")
        .addHeader("Accept-Encoding", "gzip")
        .addHeader("X-Subscription-Token", "BrakeApiToken")
        .build()

    // Ejecutar la solicitud
    client.newCall(request).execute().use { response ->
        val body: ResponseBody? = response.body
        if (body != null) {
            val responseBody = if (response.header("Content-Encoding") == "gzip") {
                // Descomprimir manualmente si es necesario
                val gzipSource = GzipSource(body.source())
                val decompressedBuffer = Buffer().apply { writeAll(gzipSource) }
                decompressedBuffer.readUtf8()
            } else {
                body.string()
            }

            // Imprimir la respuesta cruda para verificar
            println("Raw Response: $responseBody")

            // Procesar con Gson
            try {
                val gson = Gson()
                val apiResponse = gson.fromJson(responseBody, ApiResponse::class.java)
                println("Query: ${apiResponse.query.original}")
                apiResponse.results.forEach { result ->
                    println("Title: ${result.title}")
                    println("URL: ${result.url}")
                    println("Description: ${result.description}")
                    println("Age: ${result.age}")
                    println("Thumbnail: ${result.thumbnail.src}")
                    println("-----------")
                }
            } catch (e: Exception) {
                println("Error processing JSON: ${e.message}")
            }
        } else {
            println("No response body!")
        }
    }
}


data class ApiResponse(
    val type: String,
    val query: QueryData,
    val results: List<NewsResult>
)

data class QueryData(
    val original: String,
    val spellcheck_off: Boolean,
    val show_strict_warning: Boolean
)

data class NewsResult(
    val type: String,
    val title: String,
    val url: String,
    val description: String,
    val age: String,
    val page_age: String,
    val meta_url: MetaUrl,
    val thumbnail: Thumbnail
)

data class MetaUrl(
    val scheme: String,
    val netloc: String,
    val hostname: String,
    val favicon: String,
    val path: String
)

data class Thumbnail(
    val src: String
)
