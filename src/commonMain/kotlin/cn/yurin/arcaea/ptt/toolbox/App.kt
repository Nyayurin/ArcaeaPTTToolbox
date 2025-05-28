package cn.yurin.arcaea.ptt.toolbox

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntOffset
import cn.yurin.arcaea.ptt.toolbox.theme.Theme
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

val json = Json { ignoreUnknownKeys = true }
val client = HttpClient {
	install(ContentNegotiation) {
		json(json)
	}
}

val settings = Settings()

var sid by mutableStateOf<String?>(settings.getStringOrNull("sid"))

fun save(sid: String?) {
	settings["sid"] = sid
}

suspend fun loadUser(sid: String): HttpResponse {
	return client.get("https://webapi.lowiro.com/webapi/user/me") {
		cookie("sid", sid)
	}
}

@Composable
fun App() {
	Theme {
		Surface(
			color = MaterialTheme.colorScheme.background,
			modifier = Modifier
				.fillMaxSize()
		) {
			var page: Page by remember { mutableStateOf(Page.Home) }
			AnimatedContent(
				targetState = page,
				transitionSpec = {
					slideIn { IntOffset((targetState.index - initialState.index) * it.width, 0) }
						.togetherWith(slideOut { IntOffset(-(targetState.index - initialState.index) * it.width, 0) })
				}
			) {
				when (it) {
					Page.Home -> {
						Home(
							onChangePage = { page = it }
						)
					}

					Page.Login -> {
						Login(
							onBack = { page = Page.Home }
						)
					}

					is Page.PTT -> {
						PTT(
							user = it.user,
							onBack = { page = Page.Home }
						)
					}
				}
			}
		}
	}
}

@Composable
expect fun BoxWithScrollbar(
	verticalState: ScrollState,
	horizontalState: ScrollState,
	modifier: Modifier,
	content: @Composable () -> Unit
)

expect suspend fun ImageBitmap.toByteArray(): ByteArray

sealed class Page(val index: Int) {
	data object Home : Page(0)
	data object Login : Page(1)
	data class PTT(val user: User) : Page(1)
}