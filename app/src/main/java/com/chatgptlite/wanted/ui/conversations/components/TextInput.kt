package com.chatgptlite.wanted.ui.conversations.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chatgptlite.wanted.helpers.AudioPlayer
import com.chatgptlite.wanted.helpers.AudioRecorder
import com.chatgptlite.wanted.permission.PermissionCheck
import com.chatgptlite.wanted.ui.conversations.ConversationViewModel
import kotlinx.coroutines.launch
import java.io.File


@Composable
fun TextInput(
    conversationViewModel: ConversationViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    TextInputIn(
        sendMessage = { text ->
            coroutineScope.launch {
                conversationViewModel.sendMessage(text)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextInputIn(
    sendMessage: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf(TextFieldValue("")) }
    var showVoiceInputPrompt by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val audioRecorder = remember { AudioRecorder(context) }
    val audioPlayer = remember { AudioPlayer() }
    val permissionCheck = remember { PermissionCheck(context) }
    var isRecording by remember { mutableStateOf(false) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    fun openPressRecordingButton() {
        permissionCheck.checkAudioRecordingPermission(
            onPermissionGranted = {
                showVoiceInputPrompt = true
                isRecording = true
                recordedFile = audioRecorder.startRecording()
            },
            onPermissionDenied = {
                showPermissionDialog = true
            }
        )
    }

    Box(
        // Use navigationBarsPadding() imePadding() and , to move the input panel above both the
        // navigation bar, and on-screen keyboard (IME)
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Column {
            Divider(Modifier.height(0.2.dp))
            Box(
                Modifier
                    .padding(horizontal = 4.dp)
                    .padding(top = 6.dp, bottom = 10.dp)
            ) {
                Row {
                    TextField(
                        value = text,
                        onValueChange = {
                            text = it
                        },
                        label = null,
                        placeholder = { Text("Ask me anything", fontSize = 12.sp) },
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                            .weight(1f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                        ),
                    )
                    IconButton(onClick = {
                        scope.launch {
                            val textClone = text.text
                            text = TextFieldValue("")
                            sendMessage(textClone)

                        }
                    }) {
                        Icon(
                            Icons.Filled.Send,
                            "sendMessage",
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    IconButton(
                        onClick = { openPressRecordingButton() },
                    ) {
                        Icon(
                            Icons.Filled.Mic,
                            "voiceInput",
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    // TODO: play audio
//                    IconButton(
//                        onClick = {
//                            if (isPlaying) {
//                                audioPlayer.stopPlaying()
//                                isPlaying = false
//                            } else {
//                                recordedFile?.let {
//                                    audioPlayer.startPlaying(it)
//                                    isPlaying = true
//                                    scope.launch {
//                                        isPlaying = false
//                                    }
//                                }
//                            }
//                        },
//                        enabled = recordedFile != null && !isRecording
//                    ) {
//                        Icon(
//                            imageVector = Icons.Filled.PlayArrow,
//                            contentDescription = if (isPlaying) "stop" else "play",
//                            tint = if (isPlaying) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
//                        )
//                    }
                }
            }
        }
    }

    if (showVoiceInputPrompt) {
        RecordingDialog(
            onDismissRequest = {
                showVoiceInputPrompt = false
                isRecording = false
                audioRecorder.stopRecording()
            },
            isRecording = isRecording,
        )
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Audio Recording Permission Required") },
            text = { Text("To use the recording feature, we need permission to access your microphone. Please grant this permission in the settings.") },
            confirmButton = {
                Button(onClick = {
                    showPermissionDialog = false
                    permissionCheck.openAppSettings()
                }) {
                    Text("Go to Settings")
                }
            },
            dismissButton = {
                Button(onClick = { showPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview()
@Composable
fun PreviewTextInput(
) {
    TextInputIn(
        sendMessage = {}
    )
}