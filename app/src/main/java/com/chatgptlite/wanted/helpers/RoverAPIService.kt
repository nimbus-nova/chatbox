package com.chatgptlite.wanted.helpers

import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface RoverAPIService {
    @POST("/cmd")
    suspend fun sendCommand(@Body commandRequest: JsonObject): Response<StatusResponse>

    @GET("/ping")
    suspend fun ping(): Response<PingResponse>
}

data class StatusResponse(val status: String, val request: String?)
data class PingResponse(val status: String, val message: String)

suspend fun sendPing(addr: String, port: String) : Response<PingResponse> {
    val retrofit = Retrofit.Builder()
        .baseUrl("http://$addr:$port")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(RoverAPIService::class.java)
    val response = apiService.ping()
    return response
}

suspend fun sendMessage(addr: String, port: String, textToSend: String): Response<StatusResponse> {
    val retrofit = Retrofit.Builder()
        .baseUrl("http://$addr:$port")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService = retrofit.create(RoverAPIService::class.java)
    val commandRequest = JsonObject().apply {
        addProperty("cmd", textToSend)
    }
    val response = apiService.sendCommand(commandRequest)
    return response
}
