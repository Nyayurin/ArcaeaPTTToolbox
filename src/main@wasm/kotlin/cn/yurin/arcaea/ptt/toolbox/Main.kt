package cn.yurin.arcaea.ptt.toolbox

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
	CanvasBasedWindow("Arcaea PTT Toolbox") {
		App()
	}
}