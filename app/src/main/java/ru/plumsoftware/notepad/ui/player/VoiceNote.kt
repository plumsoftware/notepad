package ru.plumsoftware.notepad.ui.player

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max

/**
 * Секция записи/воспроизведения голосовой заметки для экрана редактирования.
 * Пишет аудио (MediaRecorder) и параллельно расшифровывает (SpeechRecognizer, best-effort).
 */
@Composable
fun VoiceRecorderSection(
    voicePath: String?,
    transcription: String?,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onRecorded: (path: String, transcription: String) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var recorder by remember { mutableStateOf<android.media.MediaRecorder?>(null) }
    var recognizer by remember { mutableStateOf<android.speech.SpeechRecognizer?>(null) }
    var currentFile by remember { mutableStateOf<java.io.File?>(null) }
    val transcriptBuilder = remember { StringBuilder() }
    var elapsed by remember { mutableIntStateOf(0) }

    fun stopRecording(save: Boolean) {
        try {
            recorder?.stop()
        } catch (_: Exception) {
        }
        try {
            recorder?.release()
        } catch (_: Exception) {
        }
        recorder = null
        try {
            recognizer?.stopListening()
            recognizer?.destroy()
        } catch (_: Exception) {
        }
        recognizer = null
        isRecording = false
        val file = currentFile
        if (save && file != null && file.exists() && file.length() > 0) {
            onRecorded(file.absolutePath, transcriptBuilder.toString().trim())
        } else {
            file?.delete()
        }
    }

    fun startRecording() {
        val file = ru.plumsoftware.notepad.data.filesaver.createVoiceFile(context)
        currentFile = file
        transcriptBuilder.setLength(0)
        elapsed = 0
        try {
            val rec = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                android.media.MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                android.media.MediaRecorder()
            }
            rec.setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
            rec.setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4)
            rec.setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC)
            rec.setOutputFile(file.absolutePath)
            rec.prepare()
            rec.start()
            recorder = rec
            isRecording = true
        } catch (e: Exception) {
            recorder = null
            isRecording = false
            return
        }
        // Параллельная расшифровка (best-effort)
        try {
            if (android.speech.SpeechRecognizer.isRecognitionAvailable(context)) {
                val sr = android.speech.SpeechRecognizer.createSpeechRecognizer(context)
                sr.setRecognitionListener(object : android.speech.RecognitionListener {
                    override fun onReadyForSpeech(params: android.os.Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {}
                    override fun onResults(results: android.os.Bundle?) {
                        val matches = results?.getStringArrayList(
                            android.speech.SpeechRecognizer.RESULTS_RECOGNITION
                        )
                        if (!matches.isNullOrEmpty()) {
                            if (transcriptBuilder.isNotEmpty()) transcriptBuilder.append(" ")
                            transcriptBuilder.append(matches[0])
                        }
                    }

                    override fun onPartialResults(partialResults: android.os.Bundle?) {}
                    override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
                })
                val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(
                        android.speech.RecognizerIntent.EXTRA_LANGUAGE,
                        java.util.Locale.getDefault()
                    )
                }
                sr.startListening(intent)
                recognizer = sr
            }
        } catch (_: Exception) {
            recognizer = null
        }
    }

    LaunchedEffect(isRecording) {
        while (isRecording) {
            kotlinx.coroutines.delay(1000)
            elapsed++
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isRecording) {
                try {
                    recorder?.release()
                } catch (_: Exception) {
                }
                try {
                    recognizer?.destroy()
                } catch (_: Exception) {
                }
            }
        }
    }

    Column(modifier = modifier) {
        if (voicePath != null) {
            VoicePlayer(
                path = voicePath,
                accentColor = accentColor,
                full = true,
                transcription = transcription,
                onRerecord = {
                    onDelete()
                    startRecording()
                },
                onDelete = onDelete
            )
        } else if (isRecording) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFFF3B30).copy(alpha = 0.12f))
                    .clickable { stopRecording(save = true) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFF3B30))
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = formatMillis(elapsed * 1000),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFFFF3B30)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(ru.plumsoftware.notepad.R.string.done),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(accentColor.copy(alpha = 0.14f))
                    .clickable { startRecording() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Outlined.Mic,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(ru.plumsoftware.notepad.R.string.voice_record),
                    style = MaterialTheme.typography.titleMedium,
                    color = accentColor
                )
            }
        }
    }
}

private fun formatMillis(ms: Int): String {
    val totalSec = ms / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    return "%d:%02d".format(m, s)
}

// Псевдо-«волны»: детерминированные высоты по пути файла (для одинакового вида).
private fun waveHeights(seed: String, count: Int = 34): List<Float> {
    var h = seed.hashCode().toLong()
    return List(count) {
        h = h * 6364136223846793005L + 1442695040888963407L
        val v = ((h ushr 33).toInt() and 0xFF) / 255f
        0.25f + v * 0.75f
    }
}

/**
 * Плеер голосовой заметки. [accentColor] — акцент (берётся из цвета заметки).
 * При [full] показывает управление «Перезаписать / скорость / удалить» и расшифровку.
 */
@Composable
fun VoicePlayer(
    path: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    full: Boolean = false,
    transcription: String? = null,
    onRerecord: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val player = remember(path) {
        MediaPlayer().apply {
            try {
                setDataSource(path)
                prepare()
            } catch (_: Exception) {
            }
        }
    }
    var isPlaying by remember(path) { mutableStateOf(false) }
    var position by remember(path) { mutableIntStateOf(0) }
    var duration by remember(path) { mutableIntStateOf(0) }
    var speed by remember(path) { mutableFloatStateOf(1f) }
    var showTranscription by remember(path) { mutableStateOf(false) }

    LaunchedEffect(path) {
        duration = try {
            player.duration
        } catch (_: Exception) {
            0
        }
    }

    DisposableEffect(path) {
        player.setOnCompletionListener {
            isPlaying = false
            position = 0
            try {
                player.seekTo(0)
            } catch (_: Exception) {
            }
        }
        onDispose {
            try {
                player.release()
            } catch (_: Exception) {
            }
        }
    }

    // Обновление прогресса во время воспроизведения
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            position = try {
                player.currentPosition
            } catch (_: Exception) {
                position
            }
            kotlinx.coroutines.delay(60)
        }
    }

    fun togglePlay() {
        try {
            if (isPlaying) {
                player.pause()
                isPlaying = false
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    player.playbackParams = player.playbackParams.setSpeed(speed)
                }
                player.start()
                isPlaying = true
            }
        } catch (_: Exception) {
        }
    }

    val heights = remember(path) { waveHeights(path) }
    val progress = if (duration > 0) position.toFloat() / duration else 0f
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (full) 44.dp else 36.dp)
                    .clip(CircleShape)
                    .background(accentColor)
                    .clickable { togglePlay() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(if (full) 24.dp else 20.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            // Волновая дорожка с перемоткой
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp)
                    .pointerInput(duration) {
                        if (duration > 0) {
                            detectHorizontalDragGestures { change, _ ->
                                val ratio = (change.position.x / size.width).coerceIn(0f, 1f)
                                val target = (ratio * duration).toInt()
                                try {
                                    player.seekTo(target)
                                } catch (_: Exception) {
                                }
                                position = target
                            }
                        }
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    heights.forEachIndexed { index, h ->
                        val played = index.toFloat() / heights.size <= progress
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height((30 * h).dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (played) accentColor else onSurface.copy(alpha = 0.18f)
                                )
                        )
                    }
                }
            }

            Spacer(Modifier.width(10.dp))

            // Кнопка расшифровки — слева от времени
            if (!transcription.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            if (showTranscription) accentColor.copy(alpha = 0.18f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { showTranscription = !showTranscription },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Subtitles,
                        contentDescription = androidx.compose.ui.res.stringResource(
                            ru.plumsoftware.notepad.R.string.voice_transcription
                        ),
                        tint = if (showTranscription) accentColor else onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
            }

            Text(
                text = formatMillis(if (position > 0) position else duration),
                style = MaterialTheme.typography.labelMedium,
                color = onSurface.copy(alpha = 0.6f)
            )
        }

        // Расшифровка (по кнопке) — общая для карточки и редактора
        if (showTranscription && !transcription.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Top) {
                Text("✨", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "«" + transcription + "»",
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurface.copy(alpha = 0.7f)
                )
            }
        }

        if (full) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onRerecord != null) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accentColor.copy(alpha = 0.12f))
                            .clickable { onRerecord() }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Mic,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = androidx.compose.ui.res.stringResource(ru.plumsoftware.notepad.R.string.voice_rerecord),
                            style = MaterialTheme.typography.labelLarge,
                            color = accentColor
                        )
                    }
                }
                // Переключатель скорости
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            speed = when (speed) {
                                1f -> 1.5f
                                1.5f -> 2f
                                else -> 1f
                            }
                            if (isPlaying && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                try {
                                    player.playbackParams = player.playbackParams.setSpeed(speed)
                                } catch (_: Exception) {
                                }
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${if (speed % 1f == 0f) speed.toInt().toString() else speed.toString()}×",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = onSurface
                    )
                }
                if (onDelete != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onDelete() }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
