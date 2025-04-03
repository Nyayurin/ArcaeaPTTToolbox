package cn.yurin.arcaea.ptt.toolbox

import androidx.compose.animation.AnimatedVisibility
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
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Composable
fun Home(onChangePage: (Page) -> Unit) {
	val scope = rememberCoroutineScope()
	val snackBarState = remember { SnackbarHostState() }
	var loading by remember { mutableStateOf(false) }
	Scaffold(
		snackbarHost = { SnackbarHost(snackBarState) }
	) {
		Box {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.SpaceBetween,
					modifier = Modifier
						.fillMaxWidth()
						.background(MaterialTheme.colorScheme.surfaceContainer)
						.statusBarsPadding()
						.padding(16.dp)
				) {
					Button(
						onClick = {
							scope.launch {
								onChangePage(Page.Login)
							}
						}
					) {
						Text(
							text = sid?.let { "切换账号" } ?: "登录"
						)
					}
					AnimatedVisibility(sid != null) {
						Text(
							text = user?.displayName ?: state.name
						)
					}
				}
				AnimatedVisibility(user != null) {
					Button(
						onClick = {
							scope.launch {
								try {
									loading = true
									val sid = sid!!
									val userDeferred = async(Dispatchers.IO) {
										loadUser(sid).body<User>().value
									}
									val responseDeferred = async(Dispatchers.IO) {
										client.get("https://webapi.lowiro.com/webapi/score/rating/me") {
											cookie("sid", sid)
										}
									}
									user = userDeferred.await()
									val response = responseDeferred.await()
									val score = response.body<Score>()
									if (score.success) {
										onChangePage(Page.PTT(score.value!!))
									} else {
										snackBarState.showSnackbar("生成失败: ${response.bodyAsText()}")
									}
								} catch (e: Exception) {
									snackBarState.showSnackbar("异常: ${e.localizedMessage}}")
								} finally {
									loading = false
								}
							}
						},
						modifier = Modifier
							.padding(8.dp)
					) {
						Text(
							text = "生成"
						)
					}
				}
			}
			if (loading) {
				Dialog(
					onDismissRequest = {}
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