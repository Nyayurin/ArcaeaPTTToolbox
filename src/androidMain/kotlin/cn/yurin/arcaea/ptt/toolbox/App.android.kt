package cn.yurin.arcaea.ptt.toolbox

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.ByteArrayOutputStream

@Composable
actual fun BoxWithScrollbar(
	verticalState: ScrollState,
	horizontalState: ScrollState,
	modifier: Modifier,
	content: @Composable () -> Unit
) {
	Box(modifier = modifier) {
		content()
	}
}

actual suspend fun ImageBitmap.toByteArray(): ByteArray {
	ByteArrayOutputStream().use { stream ->
		asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream)
		stream.flush()
		return stream.toByteArray()
	}
}

object ContextContainer {
	lateinit var context: Context
}