package com.iamravi.vidlab.features.video_upload.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamravi.vidlab.core.utils.copyUriToCacheFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class VideoUploadViewModel @Inject constructor() : ViewModel() {

    private val _videoFile = MutableStateFlow<File?>(null)
    val videoFile: StateFlow<File?> = _videoFile

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    fun copyVideoToCache(context: Context, uri: Uri) {
        viewModelScope.launch {

            try {
                val file = copyUriToCacheFile(context, uri)
                _videoFile.value = file
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }

        }
    }
}