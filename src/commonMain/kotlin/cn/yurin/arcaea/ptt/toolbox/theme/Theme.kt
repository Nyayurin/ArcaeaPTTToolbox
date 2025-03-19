package cn.yurin.arcaea.ptt.toolbox.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.*

@Composable
fun Theme(content: @Composable () -> Unit) {
	val defaultTypography = MaterialTheme.typography
	var typography by remember { mutableStateOf<Typography?>(null) }
	typography(defaultTypography) { typography = it }
	MaterialTheme(
		typography = typography ?: defaultTypography,
		content = content
	)
}

@Composable
expect fun typography(defaultTypography: Typography, setTypography: (Typography) -> Unit)