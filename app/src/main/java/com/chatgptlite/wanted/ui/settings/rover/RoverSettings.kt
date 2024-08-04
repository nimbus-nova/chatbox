package com.chatgptlite.wanted.ui.settings.rover

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chatgptlite.wanted.ui.common.AppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackPressed: () -> Unit) {
    var ipAddress by remember { mutableStateOf("10.0.0.9") }
    var port by remember {
        mutableStateOf("8000")
    }
    var textToSend by remember { mutableStateOf("ros2 topic list") }

    val viewModel: RoverSettingsViewModel = viewModel()

    val pingResult by viewModel.pingResult.collectAsState()
    val messageResult by viewModel.messageResult.collectAsState()

    LaunchedEffect(Unit) {
        val config = viewModel.loadConfig()
        config?.let {
            ipAddress = it.ipAddress
            port = it.port
            textToSend = it.textToSend
        }
    }

    Scaffold(
        topBar = {
            AppBar(
                title = "Rover Settings",
                onBackPressed = onBackPressed
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // IP Address and Port row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { ipAddress = it },
                    label = { Text("IP Address") },
                    modifier = Modifier.weight(2f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text("Port") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = textToSend,
                onValueChange = { textToSend = it },
                label = { Text("Text to send") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(

                    onClick = { viewModel.sendPing("$ipAddress:$port") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Send Ping")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { viewModel.sendMessage("$ipAddress:$port", textToSend = textToSend) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Send Message")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { viewModel.saveConfig(ipAddress, port, textToSend) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }


            pingResult?.let { Text("Ping result: $it") }
            messageResult?.let { Text("Message result: $it") }
        }
    }
}