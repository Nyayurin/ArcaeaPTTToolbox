package cn.yurin.arcaea.ptt.toolbox

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
	Window(
		title = "Arcaea PTT Toolbox",
		state = rememberWindowState(WindowPlacement.Maximized),
		onCloseRequest = ::exitApplication,
	) {
		App()
	}
}
