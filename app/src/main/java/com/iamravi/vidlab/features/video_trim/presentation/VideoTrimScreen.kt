package com.iamravi.vidlab.features.video_trim.presentation

import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.iamravi.vidlab.features.video_player.presentation.VideoPreviewPlayer
import com.iamravi.vidlab.features.video_trim.presentation.viewmodel.VideoTrimViewModel
import com.iamravi.vidlab.features.video_trim.utils.VideoTrimUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoTrimScreen(
    videoFile: File,
    onTrimComplete: (File) -> Unit,
    onBack: () -> Unit,
    viewModel: VideoTrimViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    // Initialize video metadata when screen loads
    LaunchedEffect(videoFile) {
        viewModel.initializeVideo(videoFile)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← Back")
            }
            Text(
                text = "Trim Video",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(80.dp)) // Balance the layout
        }

        // Video Preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            VideoPreviewPlayer(videoFile = videoFile)
        }

        // Video Info
        uiState.videoDuration?.let { duration ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Video Duration: ${VideoTrimUtils.formatDuration(duration)}",
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "File: ${videoFile.name}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Trim Controls
        if (uiState.videoDuration != null) {
            TrimControls(
                duration = uiState.videoDuration!!,
                startTime = uiState.trimStartTime,
                endTime = uiState.trimEndTime,
                onStartTimeChange = viewModel::updateStartTime,
                onEndTimeChange = viewModel::updateEndTime
            )
        }

        // Trim Button
        Button(
            onClick = {
                viewModel.trimVideo(context, videoFile) { trimmedFile ->
                    onTrimComplete(trimmedFile)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.canTrim && !uiState.isTrimming
        ) {
            if (uiState.isTrimming) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Text("Trimming...")
                }
            } else {
                Text("Trim Video")
            }
        }

        // Progress indicator
        if (uiState.isTrimming && uiState.trimProgress > 0) {
            Column {
                Text(
                    text = "Progress: ${(uiState.trimProgress * 100).toInt()}%",
                    fontSize = 14.sp
                )
                LinearProgressIndicator(
                    progress = uiState.trimProgress.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Error message
        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun TrimControls(
    duration: Long,
    startTime: Long,
    endTime: Long,
    onStartTimeChange: (Long) -> Unit,
    onEndTimeChange: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Trim Settings",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Start time slider
            Column {
                Text(
                    text = "Start Time: ${VideoTrimUtils.formatDuration(startTime)}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Slider(
                    value = startTime.toFloat(),
                    onValueChange = { onStartTimeChange(it.toLong()) },
                    valueRange = 0f..endTime.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // End time slider
            Column {
                Text(
                    text = "End Time: ${VideoTrimUtils.formatDuration(endTime)}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Slider(
                    value = endTime.toFloat(),
                    onValueChange = { onEndTimeChange(it.toLong()) },
                    valueRange = startTime.toFloat()..duration.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Duration info
            val trimDuration = endTime - startTime
            Text(
                text = "Trimmed Duration: ${VideoTrimUtils.formatDuration(trimDuration)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

