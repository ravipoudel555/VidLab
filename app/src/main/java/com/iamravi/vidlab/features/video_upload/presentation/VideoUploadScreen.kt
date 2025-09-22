import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.iamravi.vidlab.features.video_player.presentation.VideoPreviewPlayer
import com.iamravi.vidlab.features.video_trim.presentation.VideoTrimScreen
import com.iamravi.vidlab.features.video_upload.presentation.viewmodel.VideoUploadViewModel
import java.io.File

@Composable
fun VideoUploadScreen(viewModel: VideoUploadViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val videoFile by viewModel.videoFile.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showTrimScreen by remember { mutableStateOf(false) }
    var trimmedFile by remember { mutableStateOf<File?>(null) }

    // Launcher for picking a video
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                // Grant temporary permission
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.copyVideoToCache(context, it)
            }
        }
    )

    // Show trim screen if video is selected and user wants to trim
    if (showTrimScreen && videoFile != null) {
        VideoTrimScreen(
            videoFile = videoFile!!,
            onTrimComplete = { trimmed ->
                trimmedFile = trimmed
                showTrimScreen = false
            },
            onBack = {
                showTrimScreen = false
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                launcher.launch(arrayOf("video/*"))
            }) {
                Text("Pick Video")
            }

            Spacer(modifier = Modifier.height(16.dp))

            videoFile?.let { file ->
                Text("Copied to cache: ${file.absolutePath}")

                Spacer(modifier = Modifier.height(16.dp))

                // Preview player
                VideoPreviewPlayer(videoFile = file)

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { showTrimScreen = true }
                    ) {
                        Text("Trim Video")
                    }
                    
                    Button(
                        onClick = { /* TODO: Add other video processing options */ }
                    ) {
                        Text("Process Video")
                    }
                }
            }

            // Show trimmed video if available
            trimmedFile?.let { file ->
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Trimmed Video",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Saved to: ${file.absolutePath}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        VideoPreviewPlayer(videoFile = file)
                    }
                }
            }

            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text("Error: $msg", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
