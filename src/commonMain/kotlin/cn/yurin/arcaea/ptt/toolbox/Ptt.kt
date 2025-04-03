package cn.yurin.arcaea.ptt.toolbox

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.LoadState
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.format.char
import kotlin.math.round

@Composable
fun PTT(value: Score.Value, onBack: () -> Unit) {
	val user = remember { user!! }

	val layer = rememberGraphicsLayer()
	val verticalScrollState = rememberScrollState()
	val horizontalScrollState = rememberScrollState()

	val b30 = remember { value.b30.map { it.rating }.sortedDescending() }
	val b10 = remember { b30.take(10) }
	val r10 = remember { value.r10.map { it.rating }.sortedDescending() }
	val b30Ptt = remember { b30.sum() / 30 }
	val b10Ptt = remember { b10.sum() / 10 }
	val r10Ptt = remember { r10.sum() / 10 }
	val relPtt = remember { (b30.sum() + r10.sum()) / 40 }
	val maxPtt = remember { (b30.sum() + b10.sum()) / 40 }
	val minPtt = remember { b30.sum() / 40 }
	val b30PttFloor = remember { floorPtt(b30Ptt) }
	val b10PttFloor = remember { floorPtt(b10Ptt) }
	val r10PttFloor = remember { floorPtt(r10Ptt) }
	val relPttFloor = remember { floorPtt(relPtt) }
	val maxPttFloor = remember { floorPtt(maxPtt) }
	val minPttFloor = remember { floorPtt(minPtt) }
	val userDialog = remember {
		PTTDialog.User(
			b30 = b30Ptt,
			b10 = b10Ptt,
			r10 = r10Ptt,
			rel = relPtt,
			max = maxPtt,
			min = minPtt
		)
	}
	var currentDialog by remember { mutableStateOf<PTTDialog?>(null) }
	var headerLoaded by remember { mutableStateOf(false) }
	var b30Loaded by remember { mutableStateOf(false) }
	var r10Loaded by remember { mutableStateOf(false) }
	var loading by remember { mutableStateOf(false) }

	BoxWithScrollbar(
		verticalState = verticalScrollState,
		horizontalState = horizontalScrollState
	) {
		Column {
			TopBar(
				layer = layer,
				onBack = onBack,
				onChangeLoading = { loading = it }
			)
			Box(
				modifier = Modifier
					.verticalScroll(verticalScrollState)
					.horizontalScroll(horizontalScrollState)
			) {
				Surface(
					color = MaterialTheme.colorScheme.background,
					modifier = Modifier.drawToLayer(layer)
				) {
					Column(
						horizontalAlignment = Alignment.CenterHorizontally,
						verticalArrangement = Arrangement.spacedBy(32.dp),
						modifier = Modifier.padding(16.dp)
					) {
						Header(
							user = user,
							relPttFloor = relPttFloor,
							onDialog = { currentDialog = userDialog },
							onLoaded = { headerLoaded = true }
						)
						Column(
							horizontalAlignment = Alignment.CenterHorizontally,
							verticalArrangement = Arrangement.spacedBy(8.dp),
						) {
							Row(
								horizontalArrangement = Arrangement.spacedBy(16.dp)
							) {
								Text(
									text = "B30: $b30PttFloor"
								)
								Text(
									text = "R10: $r10PttFloor"
								)
								Text(
									text = "B10: $b10PttFloor"
								)
							}
							Text(
								text = "Range: $maxPttFloor ~ $minPttFloor"
							)
						}
						Column(
							horizontalAlignment = Alignment.CenterHorizontally,
							verticalArrangement = Arrangement.spacedBy(8.dp)
						) {
							Text(
								text = "Best 30",
								style = MaterialTheme.typography.headlineLarge
							)
							TrackList(
								list = value.b30,
								onDialog = { currentDialog = it },
								onLoaded = { b30Loaded = true }
							)
						}
						Column(
							horizontalAlignment = Alignment.CenterHorizontally,
							verticalArrangement = Arrangement.spacedBy(8.dp)
						) {
							Text(
								text = "Recent 10",
								style = MaterialTheme.typography.headlineLarge
							)
							TrackList(
								list = value.r10,
								onDialog = { currentDialog = it },
								onLoaded = { r10Loaded = true }
							)
						}
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
									text = "B10: ${it.b10}"
								)
								Text(
									text = "R10: ${it.r10}"
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
									text = "Time: ${
										Instant.fromEpochMilliseconds(it.timestamp)
											.toLocalDateTime(TimeZone.currentSystemDefault())
									}"
								)
							}
						}
					}
				}
			}
		}
		if (!(headerLoaded && b30Loaded && r10Loaded) || loading) {
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

@Composable
fun TopBar(layer: GraphicsLayer, onBack: () -> Unit, onChangeLoading: (Boolean) -> Unit) {
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
				scope.launch(Dispatchers.IO) {
					onChangeLoading(true)
					val bitmap = layer.toImageBitmap()
					val byteArray = bitmap.toByteArray()
					val time = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).format(
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
						baseName = "ptt_$time",
						extension = "png"
					)
					onChangeLoading(false)
				}
			}
		) {
			Text(
				text = "保存图片"
			)
		}
	}
}

@Composable
fun Header(
	user: User.Value,
	relPttFloor: String,
	onDialog: () -> Unit,
	onLoaded: () -> Unit
) {
	val character = remember { user.characterStats.find { it.characterId == user.character }!! }
	val banner = remember { user.banners.find { it.id == user.customBanner } }
	var bannerLoaded by remember { mutableStateOf(false) }
	var characterLoaded by remember { mutableStateOf(false) }
	var pttLoaded by remember { mutableStateOf(false) }
	remember(bannerLoaded, characterLoaded, pttLoaded) {
		if (characterLoaded && pttLoaded && (banner == null || bannerLoaded)) {
			onLoaded()
		}
	}
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
					uri = "https://webassets.lowiro.com/${banner.resource}.png",
					contentDescription = null,
					state = rememberAsyncImageState().apply {
						onLoadState = {
							if (it is LoadState.Success) {
								bannerLoaded = true
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
					uri = "https://webassets.lowiro.com/chr/${character.icon}.png",
					contentDescription = null,
					state = rememberAsyncImageState().apply {
						onLoadState = {
							if (it is LoadState.Success) {
								characterLoaded = true
							}
						}
					},
					modifier = Modifier
						.size(180.dp)
						.clickable(onClick = onDialog)
				)
				Box(
					contentAlignment = Alignment.Center,
					modifier = Modifier
						.offset(80.dp, 80.dp)
				) {
					AsyncImage(
						uri = pttIcon(relPttFloor.toDouble(), user.settings.isHideRating),
						contentDescription = null,
						state = rememberAsyncImageState().apply {
							onLoadState = {
								if (it is LoadState.Success) {
									pttLoaded = true
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
			modifier = Modifier.align(Alignment.Center)
		)
		Text(
			text = "ID: ${user.userCode}",
			style = MaterialTheme.typography.titleLarge,
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.offset(y = (-10).dp)
		)
	}
}

@Composable
fun TrackList(list: List<Track>, onDialog: (PTTDialog.Track) -> Unit, onLoaded: () -> Unit) {
	var loadedList = remember { MutableList(list.size) { false } }
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
						onLoaded = {
							loadedList[index] = true
							if (loadedList.all { it }) {
								onLoaded()
							}
						}
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
		modifier = Modifier.width(380.dp)
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
						style = MaterialTheme.typography.titleLarge,
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
						style = MaterialTheme.typography.titleLarge,
						overflow = TextOverflow.Ellipsis
					)
				}
				Text(
					text = "#${index + 1}",
					maxLines = 1,
					style = MaterialTheme.typography.titleLarge
				)
			}
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Box(
					modifier = Modifier
						.size(200.dp)
						.clip(RoundedCornerShape(8.dp))
				) {
					AsyncImage(
						uri = "https://webassets.lowiro.com/${track.bg}.jpg",
						contentDescription = null,
						state = rememberAsyncImageState().apply {
							onLoadState = {
								if (it is LoadState.Success) {
									onLoaded()
								}
							}
						},
						modifier = Modifier.size(200.dp)
					)
					Text(
						text = makeScoreString(track.score),
						maxLines = 1,
						color = Color.White,
						style = MaterialTheme.typography.headlineLarge,
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
							style = MaterialTheme.typography.headlineLarge
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
								style = MaterialTheme.typography.bodyLarge.copy(
									brush = Brush.verticalGradient(listOf(Color(0xFF9B1691), Color(0xFF4176B9)))
								),
							)
							Text(
								text = "F: ${track.far}",
								maxLines = 1,
								style = MaterialTheme.typography.bodyLarge.copy(
									brush = Brush.verticalGradient(listOf(Color(0xFFB86800), Color(0xFFD29400)))
								),
							)
							Text(
								text = "L: ${track.lost}",
								maxLines = 1,
								style = MaterialTheme.typography.bodyLarge.copy(
									brush = Brush.verticalGradient(listOf(Color(0xFFDE0B4C), Color(0xFF633348)))
								),
							)
						}
						Text(
							text = calculatorTime(track.time),
							maxLines = 1,
							style = MaterialTheme.typography.bodyLarge
						)
					}
				}
			}
		}
	}
}