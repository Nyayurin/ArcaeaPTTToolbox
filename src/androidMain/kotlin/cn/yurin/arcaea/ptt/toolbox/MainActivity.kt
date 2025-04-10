package cn.yurin.arcaea.ptt.toolbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.vinceglb.filekit.core.FileKit

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		FileKit.init(this)
		enableEdgeToEdge()
		ContextContainer.context = this
		setContent {
			App()
		}
	}
}