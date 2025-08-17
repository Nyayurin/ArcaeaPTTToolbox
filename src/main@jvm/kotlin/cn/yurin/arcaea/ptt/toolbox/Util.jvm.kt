package cn.yurin.arcaea.ptt.toolbox

import androidx.compose.runtime.Composable
import okio.Path
import okio.Path.Companion.toOkioPath
import java.io.File

actual fun privatePath(parent: String, child: String): Path {
	val parentFile = File("cache", parent)
	if (!parentFile.exists()) {
		parentFile.mkdirs()
	}
	return File(parentFile, child).toOkioPath()
}

@Composable
actual fun BackHandler(block: () -> Unit) {}