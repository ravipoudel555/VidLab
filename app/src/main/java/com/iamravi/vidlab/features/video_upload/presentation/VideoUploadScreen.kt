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
import com.iamravi.vidlab.features.video_upload.presentation.viewmodel.VideoUploadViewModel

@Composable
fun VideoUploadScreen(viewModel: VideoUploadViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val videoFile by viewModel.videoFile.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

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
        }

        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text("Error: $msg", color = MaterialTheme.colorScheme.error)
        }
    }
}
