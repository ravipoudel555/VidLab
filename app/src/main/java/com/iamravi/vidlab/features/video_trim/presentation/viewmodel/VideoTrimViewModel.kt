package com.iamravi.vidlab.features.video_trim.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamravi.vidlab.features.video_trim.utils.VideoTrimUtils
import com.iamravi.vidlab.features.video_trim.utils.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import trimVideo
import java.io.File
import javax.inject.Inject

data class VideoTrimUiState(
    val videoDuration: Long? = null,
    val trimStartTime: Long = 0,
    val trimEndTime: Long = 0,
    val isTrimming: Boolean = false,
    val trimProgress: Double = 0.0,
    val errorMessage: String? = null,
    val canTrim: Boolean = false
)

@HiltViewModel
class VideoTrimViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(VideoTrimUiState())
    val uiState: StateFlow<VideoTrimUiState> = _uiState.asStateFlow()

    fun initializeVideo(videoFile: File) {
        viewModelScope.launch {
            try {
                val duration = VideoTrimUtils.getVideoDuration(videoFile)
                _uiState.value = _uiState.value.copy(
                    videoDuration = duration,
                    trimEndTime = duration,
                    canTrim = duration > 0
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load video: ${e.message}"
                )
            }
        }
    }

    fun updateStartTime(startTime: Long) {
        val currentState = _uiState.value
        val newStartTime = startTime.coerceAtLeast(0)
        val newEndTime = if (newStartTime >= currentState.trimEndTime) {
            (newStartTime + 1000).coerceAtMost(currentState.videoDuration ?: 0)
        } else {
            currentState.trimEndTime
        }
        
        val validation = VideoTrimUtils.validateTrimParameters(
            newStartTime, newEndTime, currentState.videoDuration ?: 0
        )
        
        _uiState.value = currentState.copy(
            trimStartTime = newStartTime,
            trimEndTime = newEndTime,
            canTrim = validation is ValidationResult.Success,
            errorMessage = if (validation is ValidationResult.Error) validation.message else null
        )
    }

    fun updateEndTime(endTime: Long) {
        val currentState = _uiState.value
        val newEndTime = endTime.coerceAtMost(currentState.videoDuration ?: 0)
        
        val validation = VideoTrimUtils.validateTrimParameters(
            currentState.trimStartTime, newEndTime, currentState.videoDuration ?: 0
        )
        
        _uiState.value = currentState.copy(
            trimEndTime = newEndTime,
            canTrim = validation is ValidationResult.Success,
            errorMessage = if (validation is ValidationResult.Error) validation.message else null
        )
    }

    fun trimVideo(
        context: Context,
        videoFile: File,
        onComplete: (File) -> Unit
    ) {
        val currentState = _uiState.value
        if (!currentState.canTrim || currentState.videoDuration == null) return

        viewModelScope.launch {
            try {
                _uiState.value = currentState.copy(
                    isTrimming = true,
                    trimProgress = 0.0,
                    errorMessage = null
                )

                // Create output file
                val outputFile = VideoTrimUtils.createTrimmedVideoFile(context, videoFile.name)

                // Start trimming with callbacks
                trimVideo(
                    context = context,
                    srcPath = videoFile.absolutePath,
                    destPath = outputFile.absolutePath,
                    trimStartMs = currentState.trimStartTime,
                    trimEndMs = currentState.trimEndTime,
                    onProgress = { progress ->
                        _uiState.value = _uiState.value.copy(trimProgress = progress)
                    },
                    onComplete = { destPath ->
                        _uiState.value = _uiState.value.copy(
                            isTrimming = false,
                            trimProgress = 1.0
                        )
                        onComplete(File(destPath))
                    },
                    onError = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isTrimming = false,
                            errorMessage = "Trim failed: ${exception.message}"
                        )
                    }
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isTrimming = false,
                    errorMessage = "Trim failed: ${e.message}"
                )
            }
        }
    }

}
