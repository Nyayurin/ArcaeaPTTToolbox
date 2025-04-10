package cn.yurin.arcaea.ptt.toolbox.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import arcaeaptttoolbox.generated.resources.MapleMono_NF_CN_Regular
import arcaeaptttoolbox.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getFontResourceBytes
import org.jetbrains.compose.resources.getSystemResourceEnvironment

@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun typography(defaultTypography: Typography, setTypography: (Typography) -> Unit) {
	LaunchedEffect(Unit) {
		val fontFamily = FontFamily(
			Font(
				identity = "MapleMono",
				data = getFontResourceBytes(
					environment = getSystemResourceEnvironment(),
					resource = Res.font.MapleMono_NF_CN_Regular,
				),
				weight = FontWeight.Normal,
				style = FontStyle.Normal
			),
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
}