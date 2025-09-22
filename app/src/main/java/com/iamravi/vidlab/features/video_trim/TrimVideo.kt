import android.content.Context
import android.util.Log
import android.widget.Toast
import com.daasuu.mp4compose.FillMode
import com.daasuu.mp4compose.filter.GlFilterGroup
import com.daasuu.mp4compose.filter.GlMonochromeFilter
import com.daasuu.mp4compose.filter.GlVignetteFilter
import com.daasuu.mp4compose.Rotation
import com.daasuu.mp4compose.composer.Mp4Composer

fun trimVideo(
    context: Context,
    srcPath: String,
    destPath: String,
    trimStartMs: Long,
    trimEndMs: Long,
    onProgress: ((Double) -> Unit)? = null,
    onComplete: ((String) -> Unit)? = null,
    onError: ((Exception) -> Unit)? = null
) {
    Mp4Composer(srcPath, destPath)
        .rotation(Rotation.NORMAL) // use ROTATION_90 if needed
        .size(540, 960) // resize output
        .fillMode(FillMode.PRESERVE_ASPECT_FIT) // keep aspect ratio
        .filter(
            GlFilterGroup(
                GlMonochromeFilter(), // grayscale
                GlVignetteFilter()    // vignette effect
            )
        )
        .trim(trimStartMs, trimEndMs) // trimming in ms
        .listener(object : Mp4Composer.Listener {
            override fun onProgress(progress: Double) {
                Log.d("TrimVideo", "Progress = $progress")
                onProgress?.invoke(progress)
            }

            override fun onCurrentWrittenVideoTime(timeUs: Long) {
                Log.d("TrimVideo", "Current written time: ${timeUs / 1000}ms")
            }

            override fun onCompleted() {
                Log.d("TrimVideo", "Completed! Saved to $destPath")
                Toast.makeText(
                    context,
                    "Trim complete. File saved at: $destPath",
                    Toast.LENGTH_SHORT
                ).show()
                onComplete?.invoke(destPath)
            }

            override fun onCanceled() {
                Log.d("TrimVideo", "Canceled")
            }

            override fun onFailed(exception: java.lang.Exception) {
                Log.e("TrimVideo", "Failed", exception)
                Toast.makeText(
                    context,
                    "Trim failed: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
                onError?.invoke(exception)
            }
        })
        .start()
}
