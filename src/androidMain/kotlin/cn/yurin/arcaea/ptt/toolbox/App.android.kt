package cn.yurin.arcaea.ptt.toolbox

import android.graphics.Bitmap
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.ByteArrayOutputStream

@Composable
actual fun BoxWithScrollbar(
	verticalState: ScrollState,
	horizontalState: ScrollState,
	content: @Composable () -> Unit
) {
	content()
}

actual suspend fun ImageBitmap.toByteArray(): ByteArray {
	ByteArrayOutputStream().use { stream ->
		asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream)
		stream.flush()
		return stream.toByteArray()
	}
}