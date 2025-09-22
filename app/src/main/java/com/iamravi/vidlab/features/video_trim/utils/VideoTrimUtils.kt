package com.iamravi.vidlab.features.video_trim.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import java.io.File

object VideoTrimUtils {
    
    /**
     * Get video duration in milliseconds
     */
    fun getVideoDuration(videoFile: File): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(videoFile.absolutePath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        } finally {
            retriever.release()
        }
    }
    
    /**
     * Get video dimensions
     */
    fun getVideoDimensions(videoFile: File): Pair<Int, Int> {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(videoFile.absolutePath)
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            Pair(width, height)
        } catch (e: Exception) {
            Pair(0, 0)
        } finally {
            retriever.release()
        }
    }
    
    /**
     * Create a unique output file for trimmed video
     */
    fun createTrimmedVideoFile(context: Context, originalFileName: String): File {
        val timestamp = System.currentTimeMillis()
        val fileName = "trimmed_${timestamp}_$originalFileName"
        return File(context.cacheDir, fileName)
    }
    
    /**
     * Format duration from milliseconds to MM:SS format
     */
    fun formatDuration(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    /**
     * Validate trim parameters
     */
    fun validateTrimParameters(
        startTime: Long,
        endTime: Long,
        videoDuration: Long
    ): ValidationResult {
        return when {
            startTime < 0 -> ValidationResult.Error("Start time cannot be negative")
            endTime <= startTime -> ValidationResult.Error("End time must be greater than start time")
            endTime > videoDuration -> ValidationResult.Error("End time cannot exceed video duration")
            (endTime - startTime) < 1000 -> ValidationResult.Error("Trim duration must be at least 1 second")
            else -> ValidationResult.Success
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
