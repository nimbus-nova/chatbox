package com.chatgptlite.wanted.ui.settings.video

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Image
import androidx.compose.material3.OutlinedTextField

import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoStreamingSetting(onBackPressed: () -> Unit) {
    var ipAddress by remember { mutableStateOf("10.0.0.9") }
    var port by remember {
        mutableStateOf("8000")
    }
    var route by remember {
        mutableStateOf("/v1/video")
    }

    val viewModel: VideoCamSettingsViewModel = viewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // IP Address and Port row (keep existing code)
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
                value = route,
                onValueChange = { route = it },
                label = { Text("route") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Add the video feed display
            VideoFeedDisplay(viewModel.currentFrame.value)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { viewModel.receiveFeed(ipAddress, port, route) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Receive Video")
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun VideoFeedDisplay(bitmap: Bitmap?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Video Feed",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = "No video feed",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}