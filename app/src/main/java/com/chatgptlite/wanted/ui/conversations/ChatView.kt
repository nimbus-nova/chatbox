package com.chatgptlite.wanted.ui.conversations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chatgptlite.wanted.ui.conversations.components.RecordingDialog
import com.chatgptlite.wanted.ui.settings.mlc.MessageData
import com.chatgptlite.wanted.ui.settings.mlc.MessageRole
import com.chatgptlite.wanted.ui.settings.mlc.MlcModelSettingsViewModel
import com.chatgptlite.wanted.ui.settings.video.VideoCamSettingsViewModel
import com.chatgptlite.wanted.ui.settings.video.VideoFeedDisplay
import kotlinx.coroutines.launch
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.chatgptlite.wanted.constants.debugMode
import com.chatgptlite.wanted.data.whisper.asr.IRecorderListener
import com.chatgptlite.wanted.data.whisper.asr.IWhisperListener
import com.chatgptlite.wanted.data.whisper.asr.Recorder
import com.chatgptlite.wanted.data.whisper.asr.Whisper
import com.chatgptlite.wanted.data.whisper.utils.WaveUtil
import com.chatgptlite.wanted.helpers.AudioPlayer
import com.chatgptlite.wanted.permission.PermissionCheck
import com.chatgptlite.wanted.ui.settings.rover.RoverSettingsViewModel
import java.io.File

@Composable
fun ChatView(
    roverSettingsViewModel: RoverSettingsViewModel,
    viewModel: VideoCamSettingsViewModel,
    mlcModelSettingsViewModelhatState: MlcModelSettingsViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
    ) {
        val lazyColumnListState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        var isShowingVideoStream by remember { mutableStateOf(false) }
        if (isShowingVideoStream) {
            VideoFeedDisplay(viewModel.currentFrame.value)
        }
        LazyColumn(
            modifier = Modifier.weight(9f),
            verticalArrangement = Arrangement.spacedBy(5.dp, alignment = Alignment.Bottom),
            state = lazyColumnListState
        ) {
            coroutineScope.launch {
                lazyColumnListState.animateScrollToItem(mlcModelSettingsViewModelhatState.chatState.messages.size)
            }
            items(
                items = mlcModelSettingsViewModelhatState.chatState.messages,
                key = { message -> message.id },
            ) { message ->
                MessageView(roverSettingsViewModel, messageData = message)
            }
            item {
                // place holder item for scrolling to the bottom
            }
        }
        TextInput(
            mlcModelSettingsViewModelhatState.chatState,
            isShowingVideoStream,
            setVideoStreamShow = {enable: Boolean ->
                Log.d("ChatView", "setVideoStreamShow: $enable")
                isShowingVideoStream = enable
            },
            sendMessage = {
                mlcModelSettingsViewModelhatState.chatState.requestGenerate(it)
            }
        )
    }
}

@Composable
fun MessageView(roverSettingsViewModel: RoverSettingsViewModel, messageData: MessageData) {
    val context = LocalContext.current
    var isSending by remember { mutableStateOf(false) }
    SelectionContainer {
        if (messageData.role == MessageRole.Assistant) {
            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = messageData.text,
                    textAlign = TextAlign.Left,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .wrapContentWidth()
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .padding(5.dp)
                        .widthIn(max = 300.dp)
                        .clickable(true) {
                            if (isSending) return@clickable
                            roverSettingsViewModel.sendMessage(
                                textToSend = messageData.text,
                                onSuccess = {
                                    Toast.makeText(context, "Send Command", Toast.LENGTH_SHORT).show()
                                    isSending = false
                                },
                                onFail = {error ->
                                    isSending = false
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                )

            }
        } else {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = messageData.text,
                    textAlign = TextAlign.Right,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .wrapContentWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .padding(5.dp)
                        .widthIn(max = 300.dp)
                )

            }
        }
    }
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
private fun TextInput(
    chatState: MlcModelSettingsViewModel.ChatState?,
    isShowVideoStream: Boolean,
    setVideoStreamShow: (enable: Boolean) -> Unit,
    sendMessage: (String) -> Unit,
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
                    IconButton(
                        onClick = {
                            if (text.text != "" && chatState?.chatable() == true) {
                                setVideoStreamShow(false)
                                scope.launch {
                                    val textClone = text.text
                                    text = TextFieldValue("")
                                    sendMessage(textClone)
                                }
                            }
                            else if (chatState?.isInInitialization() == true) {
                                Toast.makeText(context, "Please load model first", Toast.LENGTH_SHORT).show()
                            }
                            else {
                                Toast.makeText(context, "Please wait for the chat model ready", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
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
                    IconButton(
                        onClick = {
                            setVideoStreamShow(!isShowVideoStream)
                        }
                    ) {
                        Icon(
                            imageVector = if (isShowVideoStream) Icons.Filled.HideImage else Icons.Filled.Image,
                            contentDescription = "hide video stream",
                            tint = if (isShowVideoStream) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
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
    TextInput(
        chatState = null,
        isShowVideoStream = false,
        setVideoStreamShow = {},
        sendMessage = {}
    )
}