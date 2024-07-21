package com.chatgptlite.wanted.ui.conversations.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chatgptlite.wanted.constants.debugMode
import com.chatgptlite.wanted.data.whisper.asr.IRecorderListener
import com.chatgptlite.wanted.data.whisper.asr.IWhisperListener
import com.chatgptlite.wanted.data.whisper.asr.Recorder
import com.chatgptlite.wanted.data.whisper.asr.Whisper
import com.chatgptlite.wanted.data.whisper.utils.WaveUtil
import com.chatgptlite.wanted.helpers.AudioPlayer
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
val TAG = "TextInput"

fun getFilePath(context: Context, assetName: String): String {
    val outfile = File(context.filesDir, assetName);
    if (!outfile.exists()) {
        Log.d(TAG, "File not found - " + outfile.absolutePath)
    }

    Log.d(TAG, "Returned asset path: " + outfile.absolutePath)
    return outfile.absolutePath
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
    val audioPlayer = remember { AudioPlayer() }
    val permissionCheck = remember { PermissionCheck(context) }
    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val useMultilingual by remember { mutableStateOf(false) }
    var modelPath by remember { mutableStateOf("") }
    var vocabPath by remember { mutableStateOf("") }
    modelPath = if (useMultilingual) getFilePath(context, "whisper-tiny.tflite")
        else getFilePath(context, "whisper-tiny-en.tflite")
    vocabPath = if (useMultilingual) getFilePath(context, "filters_vocab_multilingual.bin")
        else getFilePath(context, "filters_vocab_en.bin")
    val waveFilePath = remember {
        getFilePath(context, WaveUtil.RECORDING_FILE)
    }
    val whisper = remember {
        Whisper(context).apply {
            loadModel(modelPath, vocabPath, useMultilingual)
            setListener(object : IWhisperListener {
                override fun onUpdateReceived(message: String?) {
                    Log.d(TAG, "onUpdateReceived: $message")
                }

                override fun onResultReceived(result: String?) {
                    Log.d(TAG, "onResultReceived: $result")
                    text = TextFieldValue(result ?: "")
                }
            })
        }
    }
    val record = remember {
        Recorder(context).apply {
            setListener(object : IRecorderListener{
                override fun onUpdateReceived(message: String) {
                    Log.d(TAG, "onUpdateReceived: $message")
                    if (message.contains("done")) {
                        Log.d(TAG, "start translation")
                        whisper.setFilePath(getFilePath(context, WaveUtil.RECORDING_FILE))
                        whisper.setAction(Whisper.ACTION_TRANSCRIBE)
                        whisper.start()
                    }
                    Log.d(TAG, "${message.contains("done")} $message ${Whisper.MSG_PROCESSING_DONE}")
                }

                override fun onDataReceived(samples: FloatArray?) {
//                    Log.d(TAG, "onDataReceived: $samples")
                }

            })
        }
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
                            .focusRequester(focusRequester)
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
                        onClick = {
                            permissionCheck.checkAudioRecordingPermission(
                                onPermissionGranted = {
                                    showVoiceInputPrompt = true
                                    isRecording = true
                                    text = TextFieldValue("")
                                    record.setFilePath(waveFilePath)
                                    record.start()
                                },
                                onPermissionDenied = {
                                    showPermissionDialog = true
                                }
                            )
                        },
                    ) {
                        Icon(
                            Icons.Filled.Mic,
                            "voiceInput",
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    if (debugMode) {
                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    audioPlayer.stopPlaying()
                                    isPlaying = false
                                } else {
                                    audioPlayer.startPlaying(File(waveFilePath))
                                    isPlaying = true
                                    scope.launch {
                                        isPlaying = false
                                    }
                                }
                            },
                            enabled = !isRecording
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "stop" else "play",
                                tint = if (isPlaying) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showVoiceInputPrompt) {
        RecordingDialog(
            onDismissRequest = {
                showVoiceInputPrompt = false
                isRecording = false
                record.stop()
                focusRequester.requestFocus()
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