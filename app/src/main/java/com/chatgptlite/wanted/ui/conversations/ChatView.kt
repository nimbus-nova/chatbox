package com.chatgptlite.wanted.ui.conversations

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chatgptlite.wanted.constants.debugMode
import com.chatgptlite.wanted.data.whisper.asr.IRecorderListener
import com.chatgptlite.wanted.data.whisper.asr.IWhisperListener
import com.chatgptlite.wanted.data.whisper.asr.Recorder
import com.chatgptlite.wanted.data.whisper.asr.Whisper
import com.chatgptlite.wanted.data.whisper.utils.WaveUtil
import com.chatgptlite.wanted.helpers.AudioPlayer
import com.chatgptlite.wanted.permission.PermissionCheck
import com.chatgptlite.wanted.ui.conversations.components.RecordingDialog
import com.chatgptlite.wanted.ui.conversations.components.TAG
import com.chatgptlite.wanted.ui.conversations.components.getFilePath
import com.chatgptlite.wanted.ui.settings.mlc.MessageData
import com.chatgptlite.wanted.ui.settings.mlc.MessageRole
import com.chatgptlite.wanted.ui.settings.mlc.MlcModelSettingsViewModel
import kotlinx.coroutines.launch
import java.io.File

@ExperimentalMaterial3Api
@Composable
fun ChatView(
    navController: NavController, chatState: MlcModelSettingsViewModel.ChatState
) {
    val localFocusManager = LocalFocusManager.current
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    text = "MLCChat: " + chatState.modelName.value.split("-")[0],
                    color = MaterialTheme.colorScheme.onPrimary
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
            navigationIcon = {
                IconButton(
                    onClick = { navController.popBackStack() },
                    enabled = chatState.interruptable()
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "back home page",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { chatState.requestResetChat() },
                    enabled = chatState.interruptable()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Replay,
                        contentDescription = "reset the chat",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            })
    }, modifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            localFocusManager.clearFocus()
        })
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 10.dp)
        ) {
            val lazyColumnListState = rememberLazyListState()
            val coroutineScope = rememberCoroutineScope()
            Text(
                text = chatState.report.value,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 5.dp)
            )
            Divider(thickness = 1.dp, modifier = Modifier.padding(vertical = 5.dp))
            LazyColumn(
                modifier = Modifier.weight(9f),
                verticalArrangement = Arrangement.spacedBy(5.dp, alignment = Alignment.Bottom),
                state = lazyColumnListState
            ) {
                coroutineScope.launch {
                    lazyColumnListState.animateScrollToItem(chatState.messages.size)
                }
                items(
                    items = chatState.messages,
                    key = { message -> message.id },
                ) { message ->
                    MessageView(messageData = message)
                }
                item {
                    // place holder item for scrolling to the bottom
                }
            }
            Divider(thickness = 1.dp, modifier = Modifier.padding(top = 5.dp))
            SendMessageView(chatState = chatState)
        }
    }
}

@Composable
fun MessageView(messageData: MessageData) {
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

@ExperimentalMaterial3Api
@Composable
fun SendMessageView(chatState: MlcModelSettingsViewModel.ChatState) {
    val localFocusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    var showVoiceInputPrompt by remember { mutableStateOf(false) }
    var text by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val audioPlayer = remember { AudioPlayer() }
    val permissionCheck = remember { PermissionCheck(context) }
    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    val useMultilingual by remember { mutableStateOf(false) }
    var modelPath by remember { mutableStateOf("") }
    var vocabPath by remember { mutableStateOf("") }
    modelPath = if (useMultilingual) getFilePath(context, "whisper-tiny.tflite")
    else getFilePath(context, "whisper-tiny-en.tflite")
    vocabPath = if (useMultilingual) getFilePath(context, "filters_vocab_multilingual.bin")
    else getFilePath(context, "filters_vocab_en.bin")
    val focusRequester = remember { FocusRequester() }
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
                    text = result.orEmpty()
                }
            })
        }
    }
    val record = remember {
        Recorder(context).apply {
            setListener(object : IRecorderListener {
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
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(IntrinsicSize.Max)
            .fillMaxWidth()
            .padding(bottom = 5.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(text = "Input") },
            modifier = Modifier
                .weight(9f)
                .focusRequester(focusRequester),
        )
        IconButton(
            onClick = {
                localFocusManager.clearFocus()
                chatState.requestGenerate(text)
                text = ""
            },
            modifier = Modifier
                .aspectRatio(1f)
                .weight(1f),
            enabled = (text != "" && chatState.chatable())
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = "send message",
            )
        }
        IconButton(
            onClick = {
                permissionCheck.checkAudioRecordingPermission(
                    onPermissionGranted = {
                        showVoiceInputPrompt = true
                        isRecording = true
                        text = ""
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
