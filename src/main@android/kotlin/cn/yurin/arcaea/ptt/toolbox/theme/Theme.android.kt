package cn.yurin.arcaea.ptt.toolbox.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
actual fun typography(defaultTypography: Typography, setTypography: (Typography) -> Unit) {
	val fontFamily = FontFamily(
		Font(
			path = "font/MapleMono-NF-CN-Regular.ttf",
			assetManager = LocalContext.current.assets,
			weight = FontWeight.Normal,
			style = FontStyle.Normal
		)
	)
	setTypography(
		Typography(
			defaultTypography.displayLarge.copy(fontFamily = fontFamily),
			defaultTypography.displayMedium.copy(fontFamily = fontFamily),
			defaultTypography.displaySmall.copy(fontFamily = fontFamily),
			defaultTypography.headlineLarge.copy(fontFamily = fontFamily),
			defaultTypography.headlineMedium.copy(fontFamily = fontFamily),
			defaultTypography.headlineSmall.copy(fontFamily = fontFamily),
			defaultTypography.titleLarge.copy(fontFamily = fontFamily),
			defaultTypography.titleMedium.copy(fontFamily = fontFamily),
			defaultTypography.titleSmall.copy(fontFamily = fontFamily),
			defaultTypography.bodyLarge.copy(fontFamily = fontFamily),
			defaultTypography.bodyMedium.copy(fontFamily = fontFamily),
			defaultTypography.bodySmall.copy(fontFamily = fontFamily),
			defaultTypography.labelLarge.copy(fontFamily = fontFamily),
			defaultTypography.labelMedium.copy(fontFamily = fontFamily),
			defaultTypography.labelSmall.copy(fontFamily = fontFamily),
		)
	)
}