package cn.yurin.arcaea.ptt.toolbox

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Composable
actual fun BoxWithScrollbar(
	verticalState: ScrollState,
	horizontalState: ScrollState,
	content: @Composable () -> Unit
) {
	Box {
		content()
		VerticalScrollbar(
			adapter = rememberScrollbarAdapter(verticalState),
			modifier = Modifier.align(Alignment.CenterEnd)
		)
		HorizontalScrollbar(
			adapter = rememberScrollbarAdapter(horizontalState),
			modifier = Modifier.align(Alignment.BottomCenter)
		)
	}
}

actual suspend fun ImageBitmap.toByteArray(): ByteArray {
	ByteArrayOutputStream().use { stream ->
		ImageIO.write(toAwtImage(), "png", stream)
		stream.flush()
		return stream.toByteArray()
	}
}