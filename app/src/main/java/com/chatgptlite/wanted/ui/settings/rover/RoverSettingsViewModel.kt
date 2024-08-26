package com.chatgptlite.wanted.ui.settings.rover

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chatgptlite.wanted.helpers.sendMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class RoverSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _pingResult = MutableStateFlow<String?>(null)
    val pingResult: StateFlow<String?> = _pingResult.asStateFlow()

    private val _messageResult = MutableStateFlow<String?>(null)
    val messageResult: StateFlow<String?> = _messageResult.asStateFlow()

    fun sendPing(ipAddress: String) {
        viewModelScope.launch {
            try {
                val (ip, port) = ipAddress.split(":")
                val response = com.chatgptlite.wanted.helpers.sendPing(ip, port)
                if (response.isSuccessful) {
                    _pingResult.value = "Ping successful"
                } else {
                    _pingResult.value = "Error sending ping: ${response.message()}"
                }
            } catch (e: Exception) {
                _pingResult.value = "Error sending ping: [${e.message}]"
            }
        }
    }

    fun sendMessage(textToSend: String, ipAddress: String? = null, onSuccess: (() -> Unit)? = null, onFail: ((error: String) -> Unit)? = null) {
        val newIpAddress = if (ipAddress == null) {
            val sharedPreferences = getApplication<Application>().getSharedPreferences("RoverSettings", Context.MODE_PRIVATE)
            sharedPreferences.getString("ipAddress", "") ?: return
        } else ipAddress
        viewModelScope.launch {
            try {
                val (ip, port) = newIpAddress.split(":")
                // url encode the textToSend
                val encodedText = URLEncoder.encode(textToSend, StandardCharsets.UTF_8.toString())

                val response = sendMessage(ip, port, encodedText)

                if (response.isSuccessful) {
                    val statusResponse = response.body()
                    _messageResult.value = "Message sent successfully. Server status: ${statusResponse?.status}. Request: ${statusResponse?.request}"
                    onSuccess?.invoke()
                } else {
                    val msg = "Error sending message: ${response.message()}"
                    _messageResult.value = msg
                    onFail?.invoke(msg)
                }
            } catch (e: Exception) {
                val msg = "Error sending message: ${e.message}"
                _messageResult.value = msg
                e.message?.let { onFail?.invoke(msg) }
            }
        }
    }

    fun saveConfig(ipAddress: String, port: String, textToSend: String) {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("RoverSettings", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("ipAddress", ipAddress)
            putString("port", port)
            putString("textToSend", textToSend)
            apply()
        }
        Log.i("RoverSettingsViewModel", "Config saved: $ipAddress:$port -> $textToSend")
    }

    fun loadConfig(): RoverConfig? {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("RoverSettings", Context.MODE_PRIVATE)
        val ipAddress = sharedPreferences.getString("ipAddress", null)
        val port = sharedPreferences.getString("port", null)
        val textToSend = sharedPreferences.getString("textToSend", null)

        return if (ipAddress != null && port != null && textToSend != null) {
            RoverConfig(ipAddress, port, textToSend)
        } else {
            null
        }
    }

    fun clearResults() {
        _pingResult.value = null
        _messageResult.value = null
    }
}

data class RoverConfig(
    val ipAddress: String,
    val port: String,
    val textToSend: String
)