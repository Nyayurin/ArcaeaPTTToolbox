package cn.yurin.arcaea.ptt.toolbox

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.client.call.body
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.launch

@Composable
fun Home(onChangePage: (Page) -> Unit) {
	val scope = rememberCoroutineScope()
	val snackBarState = remember { SnackbarHostState() }
	Scaffold(
		snackbarHost = { SnackbarHost(snackBarState) }
	) {
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
				val sid = sid!!
				Button(
					onClick = {
						scope.launch {
							try {
								val response = client.get("https://webapi.lowiro.com/webapi/score/rating/me") {
									cookie("sid", sid)
								}
								val score = response.body<Score>()
								if (score.success) {
									onChangePage(Page.PTT(score.value!!))
								} else {
									snackBarState.showSnackbar("生成失败: ${response.bodyAsText()}")
								}
							} catch (e: Exception) {
								snackBarState.showSnackbar("异常: ${e.localizedMessage}}")
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
	}
}