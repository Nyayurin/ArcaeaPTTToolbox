package cn.yurin.arcaea.ptt.toolbox

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.LoadState
import com.github.panpf.sketch.util.Size
import io.github.vinceglb.filekit.core.FileKit
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.datetime.*
import kotlinx.datetime.format.char
import kotlin.math.round

val dateTimeFormat = LocalDateTime.Format {
	year()
	char('-')
	monthNumber()
	char('-')
	dayOfMonth()
	char(' ')
	hour()
	char(':')
	minute()
	char(':')
	second()
}

@Composable
fun PTT(value: Score.Value, onBack: () -> Unit) {
	var score by remember { mutableStateOf(value) }
	var instant by remember { mutableStateOf(Clock.System.now()) }
	val user = remember { user!! }

	val layer = rememberGraphicsLayer()
	val verticalScrollState = rememberScrollState()
	val horizontalScrollState = rememberScrollState()
	val snackBarState = remember { SnackbarHostState() }

	val b30 = remember { score.b30.map { it.rating }.sortedDescending() }
	val b10 = remember { b30.take(10) }
	val r10 = remember { score.r10.map { it.rating }.sortedDescending() }
	val b30Ptt = remember { b30.sum() / 30 }
	val b10Ptt = remember { b10.sum() / 10 }
	val r10Ptt = remember { r10.sum() / 10 }
	val relPtt = remember { (b30.sum() + r10.sum()) / 40 }
	val maxPtt = remember { (b30.sum() + b10.sum()) / 40 }
	val minPtt = remember { b30.sum() / 40 }
	val userDialog = remember {
		PTTDialog.User(
			b30 = b30Ptt,
			r10 = r10Ptt,
			b10 = b10Ptt,
			rel = relPtt,
			max = maxPtt,
			min = minPtt
		)
	}
	var currentDialog by remember { mutableStateOf<PTTDialog?>(null) }
	val imageCount = remember { 2 + (user.customBanner?.let { 1 } ?: 0) + (score.b30.size + score.r10.size) }
	var imageLoaded by remember { mutableStateOf(0) }
	var loading by remember { mutableStateOf(false) }

	LaunchedEffect(Unit) {
		while (true) {
			delay(100)
			instant = Clock.System.now()
		}
	}

	Scaffold(
		snackbarHost = { SnackbarHost(snackBarState) }
	) {
		BoxWithScrollbar(
			verticalState = verticalScrollState,
			horizontalState = horizontalScrollState
		) {
			Column {
				TopBar(
					layer = layer,
					snackBarState = snackBarState,
					instant = instant,
					onBack = onBack,
					onChangeLoading = { loading = it },
					onChangeScore = { score = it }
				)
				Box(
					modifier = Modifier
						.verticalScroll(verticalScrollState)
						.horizontalScroll(horizontalScrollState)
				) {
					val density = LocalDensity.current
					var dpSize by remember { mutableStateOf(IntSize.Zero) }
					Surface(
						color = MaterialTheme.colorScheme.background,
						modifier = Modifier.drawToLayer(layer, dpSize)
					) {
						Column(
							horizontalAlignment = Alignment.CenterHorizontally,
							verticalArrangement = Arrangement.spacedBy(32.dp),
							modifier = Modifier
								.onSizeChanged {
									dpSize = with(density) {
										IntSize(
											width = it.width.toDp().value.toInt(),
											height = it.height.toDp().value.toInt()
										)
									}
								}
								.padding(16.dp)
								.width(1464.dp)
						) {
							Header(
								user = user,
								relPttFloor = remember { floorPtt(relPtt) },
								b30PttFloor = remember { floorPtt(b30Ptt) },
								r10PttFloor = remember { floorPtt(r10Ptt) },
								b10PttFloor = remember { floorPtt(b10Ptt) },
								maxPttFloor = remember { floorPtt(maxPtt) },
								minPttFloor = remember { floorPtt(minPtt) },
								onDialog = { currentDialog = userDialog },
								onLoaded = { imageLoaded++ }
							)
							Column(
								horizontalAlignment = Alignment.CenterHorizontally,
								verticalArrangement = Arrangement.spacedBy(8.dp)
							) {
								HorizontalDivider()
								Text(
									text = "Best 30",
									style = MaterialTheme.typography.headlineLarge
								)
								TrackList(
									list = score.b30,
									onDialog = { currentDialog = it },
									onLoaded = { imageLoaded++ }
								)
							}
							Column(
								horizontalAlignment = Alignment.CenterHorizontally,
								verticalArrangement = Arrangement.spacedBy(8.dp)
							) {
								HorizontalDivider()
								Text(
									text = "Recent 10",
									style = MaterialTheme.typography.headlineLarge
								)
								TrackList(
									list = score.r10,
									onDialog = { currentDialog = it },
									onLoaded = { imageLoaded++ }
								)
							}
							Text(
								text = buildString {
									append("Generated by Arcaea PTT Toolbox")
									append("(https://github.com/Nyayurin/ArcaeaPTTToolbox)")
									append(" @ ")
									append(
										instant.toLocalDateTime(TimeZone.currentSystemDefault()).format(dateTimeFormat)
									)
								},
								style = MaterialTheme.typography.headlineSmall
							)
						}
					}
				}
			}
			currentDialog?.let {
				Dialog(
					onDismissRequest = { currentDialog = null }
				) {
					Card(
						shape = RoundedCornerShape(16.dp)
					) {
						SelectionContainer {
							Column(
								verticalArrangement = Arrangement.spacedBy(8.dp),
								modifier = Modifier.padding(16.dp)
							) {
								when (it) {
									is PTTDialog.User -> {
										Text(
											text = "PTT: ${it.rel}"
										)
										Text(
											text = "B30: ${it.b30}"
										)
										Text(
											text = "R10: ${it.r10}"
										)
										Text(
											text = "B10: ${it.b10}"
										)
										Text(
											text = "Max: ${it.max}"
										)
										Text(
											text = "Min: ${it.min}"
										)
									}

									is PTTDialog.Track -> {
										Text(
											text = "Title: ${it.title}"
										)
										Text(
											text = "Difficult: ${it.difficult}"
										)
										Text(
											text = "ChartConstant: ${it.chartConstant}"
										)
										AsyncImage(
											uri = it.image,
											contentDescription = null
										)
										Text(
											text = "PTT: ${it.ptt}"
										)
										Text(
											text = "Play at: ${
												Instant.fromEpochMilliseconds(it.timestamp)
													.toLocalDateTime(TimeZone.currentSystemDefault())
													.format(dateTimeFormat)
											}"
										)
									}
								}
							}
						}
					}
				}
			}
			if (imageLoaded != imageCount || loading) {
				Dialog(
					onDismissRequest = {}
				) {
					Card(
						shape = RoundedCornerShape(16.dp)
					) {
						Column(
							horizontalAlignment = Alignment.CenterHorizontally,
							verticalArrangement = Arrangement.spacedBy(16.dp)
						) {
							if (imageLoaded != imageCount) {
								Text(
									text = "加载图片中($imageLoaded/$imageCount)"
								)
							}
							CircularProgressIndicator(
								modifier = Modifier.padding(16.dp)
							)
						}
					}
				}
			}
		}
	}
}

@Composable
fun TopBar(
	layer: GraphicsLayer,
	snackBarState: SnackbarHostState,
	instant: Instant,
	onBack: () -> Unit,
	onChangeLoading: (Boolean) -> Unit,
	onChangeScore: (Score.Value) -> Unit
) {
	val scope = rememberCoroutineScope()
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
		Button(
			onClick = {
				scope.launch {
					onChangeLoading(true)
					val bitmap = layer.toImageBitmap()
					withContext(Dispatchers.IO) {
						val byteArray = bitmap.toByteArray()
						val time = instant.toLocalDateTime(TimeZone.currentSystemDefault()).format(
							LocalDateTime.Format {
								year()
								char('_')
								monthNumber()
								char('_')
								dayOfMonth()
								char('-')
								hour()
								char('_')
								minute()
								char('_')
								second()
							}
						)
						FileKit.saveFile(
							bytes = byteArray,
							baseName = "PTT_$time",
							extension = "png"
						)
						onChangeLoading(false)
					}
				}
			}
		) {
			Text(
				text = "保存图片"
			)
		}
		Button(
			onClick = {
				scope.launch {
					try {
						onChangeLoading(true)
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
							onChangeScore(score.value!!)
						} else {
							snackBarState.showSnackbar("生成失败: ${response.bodyAsText()}")
						}
					} catch (e: Exception) {
						snackBarState.showSnackbar("异常: ${e.localizedMessage}}")
					} finally {
						onChangeLoading(false)
					}
				}
			}
		) {
			Text(
				text = "刷新数据"
			)
		}
	}
}

@Composable
fun Header(
	user: User.Value,
	relPttFloor: String,
	b30PttFloor: String,
	r10PttFloor: String,
	b10PttFloor: String,
	maxPttFloor: String,
	minPttFloor: String,
	onDialog: () -> Unit,
	onLoaded: () -> Unit
) {
	val character = remember { user.characterStats.find { it.characterId == user.character }!! }
	val banner = remember { user.banners.find { it.id == user.customBanner } }
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween,
		modifier = Modifier.fillMaxWidth()
	) {
		Box {
			Box(
				contentAlignment = Alignment.CenterStart,
				modifier = Modifier.height(180.dp)
			) {
				when (banner) {
					null -> Spacer(
						modifier = Modifier
							.size(564.dp, 74.dp)
							.padding(start = 90.dp)
					)

					else -> AsyncImage(
						request = ImageRequest
							.Builder(
								context = LocalPlatformContext.current,
								uri = "https://webassets.lowiro.com/${banner.resource}.png"
							)
							.size(Size.Origin)
							.build(),
						contentDescription = null,
						state = rememberAsyncImageState().apply {
							onLoadState = {
								if (it is LoadState.Success) {
									onLoaded()
								}
							}
						},
						modifier = Modifier
							.size(564.dp, 74.dp)
							.padding(start = 90.dp)
							.graphicsLayer {
								scaleX = -1F
							}
					)
				}
				Box {
					AsyncImage(
						request = ImageRequest
							.Builder(
								context = LocalPlatformContext.current,
								uri = "https://webassets.lowiro.com/chr/${character.icon}.png"
							)
							.size(Size.Origin)
							.build(),
						contentDescription = null,
						state = rememberAsyncImageState().apply {
							onLoadState = {
								if (it is LoadState.Success) {
									onLoaded()
								}
							}
						},
						modifier = Modifier
							.size(170.dp)
							.clickable(onClick = onDialog)
					)
					Box(
						contentAlignment = Alignment.Center,
						modifier = Modifier
							.offset(80.dp, 80.dp)
					) {
						AsyncImage(
							request = ImageRequest
								.Builder(
									context = LocalPlatformContext.current,
									uri = pttIcon(relPttFloor.toDouble(), user.settings.isHideRating)
								)
								.size(Size.Origin)
								.build(),
							contentDescription = null,
							state = rememberAsyncImageState().apply {
								onLoadState = {
									if (it is LoadState.Success) {
										onLoaded()
									}
								}
							},
							modifier = Modifier.size(120.dp)
						)
						Text(
							text = relPttFloor,
							color = Color.White,
							style = MaterialTheme.typography.titleMedium
						)
					}
				}
			}
			Text(
				text = user.displayName,
				color = colorOnBanner(banner?.id),
				style = MaterialTheme.typography.headlineLarge,
				modifier = Modifier
					.align(Alignment.CenterStart)
					.padding(start = 200.dp)
			)
			Text(
				text = "ID: ${user.userCode}",
				style = MaterialTheme.typography.titleLarge,
				modifier = Modifier
					.align(Alignment.BottomCenter)
					.offset(y = (-10).dp)
			)
		}
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(8.dp),
			modifier = Modifier.width(200.dp)
		) {
			PTTRow("B30PTT:", b30PttFloor)
			PTTRow("R10PTT:", r10PttFloor)
			PTTRow("B10PTT:", b10PttFloor)
			PTTRow("MaxPTT:", maxPttFloor)
			PTTRow("MinPTT:", minPttFloor)
		}
	}
}

@Composable
fun PTTRow(left: String, right: String) {
	Row(
		horizontalArrangement = Arrangement.SpaceBetween,
		modifier = Modifier.fillMaxWidth()
	) {
		Text(
			text = left,
			style = MaterialTheme.typography.titleLarge
		)
		Text(
			text = right,
			style = MaterialTheme.typography.titleLarge
		)
	}
}

@Composable
fun TrackList(list: List<Track>, onDialog: (PTTDialog.Track) -> Unit, onLoaded: () -> Unit) {
	Column(
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		repeat(list.size / 5) { y ->
			Row(
				horizontalArrangement = Arrangement.spacedBy(16.dp)
			) {
				for (x in 0 until 5) {
					val index = y * 5 + x
					TrackCard(
						index = index,
						track = list[y * 5 + x],
						onDialog = onDialog,
						onLoaded = onLoaded
					)
				}
			}
		}
	}
}

@Composable
fun TrackCard(index: Int, track: Track, onDialog: (PTTDialog.Track) -> Unit, onLoaded: () -> Unit) {
	val chartConstant = remember {
		round(
			when {
				track.isPM() -> track.rating - 2
				track.isEX() -> track.rating - 1 - (track.score - 9800000) / 200000.0
				else -> track.rating - (track.score - 9500000) / 300000.0
			} * 10
		) / 10
	}
	Card(
		onClick = {
			onDialog(
				PTTDialog.Track(
					title = track.title.en,
					image = "https://webassets.lowiro.com/${track.bg}.jpg",
					chartConstant = chartConstant,
					difficult = Difficult.entries[track.difficulty],
					ptt = track.rating,
					timestamp = track.time
				)
			)
		},
		colors = CardDefaults.cardColors(
			containerColor = when {
				track.isPM() -> MaterialTheme.colorScheme.primaryContainer
				track.isFR() -> MaterialTheme.colorScheme.tertiaryContainer
				else -> MaterialTheme.colorScheme.surfaceContainerHighest
			}
		),
		shape = RoundedCornerShape(16.dp),
		modifier = Modifier.width(280.dp)
	) {
		Column(
			verticalArrangement = Arrangement.spacedBy(4.dp),
			modifier = Modifier.padding(10.dp)
		) {
			Row(
				horizontalArrangement = Arrangement.SpaceBetween,
				modifier = Modifier.fillMaxWidth()
			) {
				Row(
					modifier = Modifier
						.weight(1F)
						.padding(end = 8.dp)
				) {
					Text(
						text = "[${Difficult.entries[track.difficulty]}$chartConstant]",
						maxLines = 1,
						style = MaterialTheme.typography.titleSmall,
						color = when (Difficult.entries[track.difficulty]) {
							Difficult.PST -> Color(0xFF328AA0)
							Difficult.PRS -> Color(0xFF8C9B51)
							Difficult.FTR -> Color(0xFF772F63)
							Difficult.BYD -> Color(0xFFA0303F)
							Difficult.ETR -> Color(0xFF675781)
						}
					)
					Text(
						text = track.title.en,
						maxLines = 1,
						style = MaterialTheme.typography.titleSmall,
						overflow = TextOverflow.Ellipsis
					)
				}
				Text(
					text = "#${index + 1}",
					maxLines = 1,
					style = MaterialTheme.typography.titleSmall
				)
			}
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Box(
					modifier = Modifier
						.size(150.dp)
						.clip(RoundedCornerShape(8.dp))
				) {
					AsyncImage(
						request = ImageRequest
							.Builder(LocalPlatformContext.current, "https://webassets.lowiro.com/${track.bg}.jpg")
							.size(Size.Origin)
							.build(),
						contentDescription = null,
						state = rememberAsyncImageState().apply {
							onLoadState = {
								if (it is LoadState.Success) {
									onLoaded()
								}
							}
						},
						modifier = Modifier.fillMaxSize()
					)
					Text(
						text = makeScoreString(track.score),
						maxLines = 1,
						color = Color.White,
						style = MaterialTheme.typography.headlineSmall,
						textAlign = TextAlign.Center,
						modifier = Modifier
							.align(Alignment.BottomCenter)
							.fillMaxWidth()
							.background(Brush.verticalGradient(listOf(Color(0x00000000), Color(0xFF000000))))
					)
				}
				Box(
					contentAlignment = Alignment.Center,
					modifier = Modifier.fillMaxSize()
				) {
					Column(
						verticalArrangement = Arrangement.spacedBy(16.dp)
					) {
						Text(
							text = roundPtt(track.rating),
							maxLines = 1,
							style = MaterialTheme.typography.headlineSmall
						)
						Column {
							Text(
								text = "P: ${track.pure}(-${
									lowerPure(
										track.score,
										track.pure,
										track.far,
										track.lost
									)
								})",
								maxLines = 1,
								style = MaterialTheme.typography.bodySmall.copy(
									brush = Brush.verticalGradient(listOf(Color(0xFF9B1691), Color(0xFF4176B9)))
								),
							)
							Text(
								text = "F: ${track.far}",
								maxLines = 1,
								style = MaterialTheme.typography.bodySmall.copy(
									brush = Brush.verticalGradient(listOf(Color(0xFFB86800), Color(0xFFD29400)))
								),
							)
							Text(
								text = "L: ${track.lost}",
								maxLines = 1,
								style = MaterialTheme.typography.bodySmall.copy(
									brush = Brush.verticalGradient(listOf(Color(0xFFDE0B4C), Color(0xFF633348)))
								),
							)
						}
						Text(
							text = calculatorTime(track.time),
							maxLines = 1,
							style = MaterialTheme.typography.bodySmall
						)
					}
				}
			}
		}
	}
}