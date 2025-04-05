package cn.yurin.arcaea.ptt.toolbox

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.unit.IntSize
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.math.floor
import kotlin.math.round

fun Modifier.drawToLayer(layer: GraphicsLayer, size: IntSize) = this.drawWithContent {
	val rawSize = this.size
	layer.record(size) {
		val scaleX = size.width.toFloat() / rawSize.width
		val scaleY = size.height.toFloat() / rawSize.height
		withTransform({ scale(scaleX, scaleY, Offset(0F, 0F)) }) {
			this@drawWithContent.drawContent()
		}
	}
	drawLayer(layer)
	drawContent()
}

fun floorPtt(ptt: Double) = buildString {
	val list = (floor(ptt * 10000) / 10000).toString().split('.')
	append(list[0])
	append('.')
	val fixed = list[1]
	append(fixed)
	if (fixed.length < 4) {
		repeat(4 - fixed.length) {
			append('0')
		}
	}
}

fun roundPtt(ptt: Double) = buildString {
	val list = (round(ptt * 10000) / 10000).toString().split('.')
	append(list[0])
	append('.')
	val fixed = list[1]
	append(fixed)
	if (fixed.length < 4) {
		repeat(4 - fixed.length) {
			append('0')
		}
	}
}

fun makeScoreString(score: Int) = buildString {
	score.toString().reversed().toList().forEachIndexed { index, char ->
		if (index == 4) {
			append(',')
		}
		append(char)
	}
	if (length < 9) {
		repeat(9 - length) {
			append('0')
		}
	}
}.reversed()

fun calculatorTime(time: Long): String {
	val instant = Instant.fromEpochMilliseconds(time)
	val now = Clock.System.now()
	val duration = now - instant
	return when {
		duration.inWholeDays > 0 -> duration.inWholeDays.toString() + " Days ago"
		duration.inWholeHours > 0 -> duration.inWholeHours.toString() + " Hours ago"
		duration.inWholeMinutes > 0 -> duration.inWholeMinutes.toString() + " Minutes ago"
		else -> duration.inWholeSeconds.toString() + " Seconds ago"
	}
}

fun lowerPure(score: Int, pure: Int, far: Int, lost: Int): Int {
	val sum = (pure + far + lost).toDouble()
	return pure - (score - (10000000 * (1 - lost / sum - far / sum / 2)).toInt())
}

fun pttIcon(ptt: Double, hide: Boolean) = when {
	hide -> "https://arcwiki.mcd.blue/images/3/34/Rating_off.png"
	ptt < 3.5 -> "https://arcwiki.mcd.blue/images/2/2f/Rating_0.png"
	ptt < 7.0 -> "https://arcwiki.mcd.blue/images/1/10/Rating_1.png"
	ptt < 10.0 -> "https://arcwiki.mcd.blue/images/4/44/Rating_2.png"
	ptt < 11.0 -> "https://arcwiki.mcd.blue/images/5/56/Rating_3.png"
	ptt < 12.0 -> "https://arcwiki.mcd.blue/images/9/9a/Rating_4.png"
	ptt < 12.5 -> "https://arcwiki.mcd.blue/images/1/1a/Rating_5.png"
	ptt < 13.0 -> "https://arcwiki.mcd.blue/images/e/ee/Rating_6.png"
	else -> "https://arcwiki.mcd.blue/images/2/22/Rating_7.png"
}

fun Track.isPM() = far + lost == 0 && score >= 10000000
fun Track.isFR() = lost == 0
fun Track.isEX() = score >= 9800000

fun colorOnBanner(id: String?): Color = when (id) {
	null -> Color.Unspecified
	"course_banner_1" -> Color(61, 87, 101)
	"course_banner_2" -> Color(56, 80, 92)
	"course_banner_3" -> Color(67, 79, 90)
	"course_banner_4" -> Color(52, 73, 94)
	"course_banner_5" -> Color(85, 62, 82)
	"course_banner_6" -> Color(85, 62, 82)
	"course_banner_7" -> Color.White
	"course_banner_8" -> Color.White
	"course_banner_9" -> Color.White
	"course_banner_10" -> Color(225, 230, 229)
	"course_banner_11" -> Color(222, 221, 228)
	"course_banner_12" -> Color.White
	"online_banner_2024_06" -> Color.White
	"online_banner_2024_07" -> Color(86, 53, 71)
	"online_banner_2024_08" -> Color.White
	"online_banner_2024_09" -> Color(50, 20, 17)
	"online_banner_2024_10" -> Color.White
	"online_banner_2024_11" -> Color.White
	"online_banner_2024_12" -> Color(225, 230, 229)
	"online_banner_2025_01" -> Color.White
	"online_banner_2025_02" -> Color(36, 30, 64)
	"online_banner_2025_03" -> Color(252, 247, 255)
	"online_banner_2025_04" -> Color(120, 71, 82)
	else -> null
} ?: Color.White