package cn.yurin.arcaea.ptt.toolbox

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import org.jetbrains.skiko.wasm.onWasmReady

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
	onWasmReady {
		CanvasBasedWindow("Arcaea PTT Toolbox") {
			App()
		}
	}
}