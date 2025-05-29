package cn.yurin.arcaea.ptt.toolbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.ktor.client.call.*
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.headers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Login(onBack: () -> Unit) {
	val scope = rememberCoroutineScope()
	var email by remember { mutableStateOf("") }
	var password by remember { mutableStateOf("") }
	var onDisconnect by remember { mutableStateOf<(() -> Unit)?>(null) }
	var connecting by remember { mutableStateOf(false) }
	val snackBarState = remember { SnackbarHostState() }
	BackHandler(onBack)
	Scaffold(
		snackbarHost = { SnackbarHost(snackBarState) }
	) {
		Box {
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
							try {
								if (email.isEmpty() || password.isEmpty()) {
									snackBarState.showSnackbar("账号和密码不能为空")
									return@launch
								}
								connecting = true
								val deferred = async {
									val response = withContext(Dispatchers.IO) {
										client.post("https://webapi.lowiro.com/auth/login") {
											headers {
												append(HttpHeaders.UserAgent, "ktor client")
												append(HttpHeaders.Accept, "*/*")
												append(HttpHeaders.Host, "webapi.lowiro.com")
											}
											contentType(ContentType.Application.Json)
											setBody(LoginRequest(email, password))
										}
									}
									val login = response.body<LoginResponse>()
									when (login.isLoggedIn) {
										true -> {
											sid = response.setCookie().find { it.name == "sid" }!!.value
												.replace("%3A", ":")
												.replace("%2B", "+")
												.replace("%2F", "/")
											save(sid)
											onBack()
										}

										else -> snackBarState.showSnackbar("登陆失败: ${response.bodyAsText()}")
									}
								}
								onDisconnect = {
									deferred.cancel()
									connecting = false
									onDisconnect = null
								}
								deferred.await()
							} catch (e: Exception) {
								snackBarState.showSnackbar("异常: $e")
							} finally {
								connecting = false
								onDisconnect = null
							}
						}
					}
				) {
					Text(
						text = "登录"
					)
				}
			}
			if (connecting) {
				BackHandler(onDisconnect!!)
				Dialog(
					onDismissRequest = onDisconnect!!
				) {
					Card(
						shape = RoundedCornerShape(16.dp)
					) {
						CircularProgressIndicator(
							modifier = Modifier.padding(16.dp)
						)
					}
				}
			}
		}
	}
}