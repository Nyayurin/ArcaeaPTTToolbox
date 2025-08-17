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
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Home(onChangePage: (Page) -> Unit) {
	val scope = rememberCoroutineScope()
	val snackBarState = remember { SnackbarHostState() }
	var onDisLoading by remember { mutableStateOf<(() -> Unit)?>(null) }
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
				}
				AnimatedVisibility(sid != null) {
					Button(
						onClick = {
							scope.launch {
								try {
									loading = true
									val deferred = async {
										val userDeferred = async(Dispatchers.IO) {
											loadUser(sid!!)
												.body<UserResponse>().value!!
										}
										val scoreResponseDeferred = async(Dispatchers.IO) {
											client.get("https://webapi.lowiro.com/webapi/score/rating/me") {
												cookie("sid", sid!!)
											}
										}
										val user = userDeferred.await()
										val scoreResponse = scoreResponseDeferred.await()
										val score = scoreResponse.body<ScoreResponse>()
										if (score.success) {
											onChangePage(Page.PTT(User.from(user, score.value!!)))
										} else {
											snackBarState.showSnackbar("生成失败: ${scoreResponse.bodyAsText()}")
										}
									}
									onDisLoading = {
										deferred.cancel()
										loading = false
										onDisLoading = null
									}
									deferred.await()
								} catch (e: Exception) {
									snackBarState.showSnackbar("异常: $e")
								} finally {
									loading = false
									onDisLoading = null
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
				BackHandler(onDisLoading!!)
				Dialog(
					onDismissRequest = onDisLoading!!
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