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
import androidx.compose.ui.graphics.toPixelMap
import io.ktor.client.fetch.*
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

@Composable
actual fun BoxWithScrollbar(
	verticalState: ScrollState,
	horizontalState: ScrollState,
	content: @Composable (() -> Unit)
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
	val buffer = toPixelMap().buffer
	val rgba = buildList {
		for (i in buffer.indices) {
			val pixel = buffer[i]
			val a = ((pixel shr 24) and 0xFF).toByte()
			val r = ((pixel shr 16) and 0xFF).toByte()
			val g = ((pixel shr 8) and 0xFF).toByte()
			val b = ((pixel shr 0) and 0xFF).toByte()
			add(r)
			add(g)
			add(b)
			add(a)
		}
	}.toTypedArray().toByteArray()

	val canvas = document.createElement("canvas") as HTMLCanvasElement
	canvas.width = width
	canvas.height = height

	val context = canvas.getContext("2d") as CanvasRenderingContext2D
	val imageData = context.createImageData(width.toDouble(), height.toDouble())

	val uint8Array = imageData.data.unsafeCast<Uint8Array>()
	for (i in rgba.indices) {
		uint8Array[i] = rgba[i]
	}

	// 将像素数据写入 Canvas
	context.putImageData(imageData, 0.0, 0.0)

	// 生成 PNG 的 Base64 数据 URL
	val dataUrl = canvas.toDataURL("image/png")

	// 提取 Base64 部分并解码为 ByteArray
	val base64Data = dataUrl.split(",")[1]

	val binaryString = window.atob(base64Data) // 解码为二进制字符串

	// 将字符串转换为 ByteArray
	val byteArray = ByteArray(binaryString.length)
	for (i in 0 until binaryString.length) {
		byteArray[i] = binaryString[i].code.toByte()
	}
	return byteArray
}