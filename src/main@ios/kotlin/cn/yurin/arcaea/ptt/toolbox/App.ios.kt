package cn.yurin.arcaea.ptt.toolbox

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

@Composable
actual fun BoxWithScrollbar(
	verticalState: ScrollState,
	horizontalState: ScrollState,
	content: @Composable (() -> Unit),
) {
	content()
}

actual suspend fun ImageBitmap.toByteArray(): ByteArray {
	TODO()
}