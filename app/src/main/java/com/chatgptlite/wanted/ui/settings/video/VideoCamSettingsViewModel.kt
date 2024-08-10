package com.chatgptlite.wanted.ui.settings.video

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayInputStream

class VideoCamSettingsViewModel : ViewModel() {
    private val client = OkHttpClient()
    val currentFrame = mutableStateOf<Bitmap?>(null)

    fun receiveFeed(ipAddress: String, port: String, route: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("http://$ipAddress:$port$route")
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw Exception("Unexpected code $response")

                    val input = response.body?.byteStream() ?: return@use
                    val reader = MjpegReader(input)

                    while (true) {
                        val frameBytes = reader.readFrame() ?: break
                        val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(frameBytes))
                        currentFrame.value = bitmap
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error
            }
        }
    }
}

class MjpegReader(private val input: java.io.InputStream) {
    private val buffer = ByteArray(1024)

    fun readFrame(): ByteArray? {
        val baos = java.io.ByteArrayOutputStream()
        var lastBytes = ByteArray(2)
        var isJpegStart = false

        while (true) {
            val bytesRead = input.read(buffer)
            if (bytesRead == -1) return null

            for (i in 0 until bytesRead) {
                baos.write(buffer[i].toInt())
                System.arraycopy(lastBytes, 1, lastBytes, 0, 1)
                lastBytes[1] = buffer[i]

                if (!isJpegStart && lastBytes[0] == 0xFF.toByte() && lastBytes[1] == 0xD8.toByte()) {
                    isJpegStart = true
                    baos.reset()
                    baos.write(0xFF)
                    baos.write(0xD8)
                } else if (isJpegStart && lastBytes[0] == 0xFF.toByte() && lastBytes[1] == 0xD9.toByte()) {
                    return baos.toByteArray()
                }
            }
        }
    }
}