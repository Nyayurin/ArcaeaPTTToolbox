package cn.yurin.arcaea.ptt.toolbox

import okio.Path
import okio.Path.Companion.toOkioPath
import java.io.File

actual fun privatePath(parent: String, child: String): Path {
	val parentFile = File(ContextContainer.context.cacheDir, parent)
	if (!parentFile.exists()) {
		parentFile.mkdirs()
	}
	return File(parentFile, child).toOkioPath()
}