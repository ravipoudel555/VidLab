package com.iamravi.vidlab.features.video_player.presentation

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File

@Composable
fun VideoPreviewPlayer(videoFile: File) {
    val context = LocalContext.current
    var player: ExoPlayer? by remember { mutableStateOf(null) }
    var playerView: PlayerView? by remember { mutableStateOf(null) }

    // Create and configure the player
    LaunchedEffect(videoFile) {
        player = ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.fromFile(videoFile))
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = false
        }
        
        // Set the player to the PlayerView if it exists
        playerView?.player = player
    }

    // Clean up the player when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            player?.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = true
                playerView = this
                // Set the player if it's already created
                player?.let { this.player = it }
            }
        },
        update = { view ->
            // Update the player reference when the view is updated
            view.player = player
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}