package cn.yurin.arcaea.ptt.toolbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import io.ktor.http.setCookie
import kotlinx.coroutines.launch

@Composable
fun Login(onBack: () -> Unit) {
	val scope = rememberCoroutineScope()
	var email by remember { mutableStateOf("") }
	var password by remember { mutableStateOf("") }
	val snackBarState = remember { SnackbarHostState() }
	Scaffold(
		snackbarHost = { SnackbarHost(snackBarState) }
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				modifier = Modifier
					.fillMaxWidth()
					.background(MaterialTheme.colorScheme.surfaceContainer)
					.padding(8.dp)
					.statusBarsPadding()
			) {
				Button(
					onClick = {
						scope.launch {
							onBack()
						}
					}
				) {
					Text(
						text = "返回"
					)
				}
			}
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Text(
					text = "账户"
				)
				TextField(
					value = email,
					onValueChange = { email = it }
				)
			}
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Text(
					text = "密码"
				)
				TextField(
					value = password,
					onValueChange = { password = it }
				)
			}
			Button(
				onClick = {
					scope.launch {
						if (email.isEmpty() || password.isEmpty()) {
							snackBarState.showSnackbar("账号和密码不能为空")
							return@launch
						}
						state = State.Loading
						val response = client.post("https://webapi.lowiro.com/auth/login") {
							headers {
								append(HttpHeaders.UserAgent, "ktor client")
								append(HttpHeaders.ContentType, "application/json")
								append(HttpHeaders.Accept, "*/*")
								append(HttpHeaders.Host, "webapi.lowiro.com")
							}
							setBody("{\"email\":\"$email\",\"password\":\"$password\"}")
						}
						val login = response.body<Login>()
						if (login.isLoggedIn != true) {
							snackBarState.showSnackbar("登陆失败: ${response.bodyAsText()}")
							return@launch
						}
						sid = response.setCookie().find { it.name == "sid" }!!.value
							.replace("%3A", ":")
							.replace("%2B", "+")
							.replace("%2F", "/")
						save(sid)
						val userResponse = loadUser(sid!!)
						val tempUser = userResponse.body<User>()
						if (tempUser.success) {
							user = tempUser.value!!
							state = State.Online
							onBack()
						} else {
							state = State.Offline
							snackBarState.showSnackbar("用户数据加载失败: ${userResponse.bodyAsText()}")
						}
					}
				}
			) {
				Text(
					text = "登录"
				)
			}
		}
	}
}