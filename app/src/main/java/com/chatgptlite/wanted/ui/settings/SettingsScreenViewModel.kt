package com.chatgptlite.wanted.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatgptlite.wanted.helpers.sendMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.InetAddress

class SettingsScreenViewModel : ViewModel() {

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

    fun sendMessage(ipAddress: String, textToSend: String) {
        viewModelScope.launch {
            try {
                val (ip, port) = ipAddress.split(":")
                val response = sendMessage(ip, port, textToSend)

                if (response.isSuccessful) {
                    val statusResponse = response.body()
                    _messageResult.value = "Message sent successfully. Server status: ${statusResponse?.status}"
                } else {
                    _messageResult.value = "Error sending message: ${response.message()}"
                }
            } catch (e: Exception) {
                _messageResult.value = "Error sending message: ${e.message}"
            }
        }
    }

    fun clearResults() {
        _pingResult.value = null
        _messageResult.value = null
    }
}