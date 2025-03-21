package cn.yurin.arcaea.ptt.toolbox

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.github.panpf.sketch.AsyncImage
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.format.char
import kotlin.math.round

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PTT(value: Score.Value, onBack: () -> Unit) {
	val user = user!!

	val layer = rememberGraphicsLayer()
	val verticalScrollState = rememberScrollState()
	val horizontalScrollState = rememberScrollState()

	val b30 = remember { value.b30.map { it.rating }.sortedDescending() }
	val b10 = remember { b30.take(10) }
	val r10 = remember { value.r10.map { it.rating }.sortedDescending() }
	val b30Ptt = remember { floorPtt(b30.sum() / 30) }
	val b10Ptt = remember { floorPtt(b10.sum() / 10) }
	val r10Ptt = remember { floorPtt(r10.sum() / 10) }
	val relPtt = remember { floorPtt((b30.sum() + r10.sum()) / 40) }
	val maxPtt = remember { floorPtt((b30.sum() + b10.sum()) / 40) }
	val minPtt = remember { floorPtt(b30.sum() / 40) }

	BoxWithScrollbar(
		verticalState = verticalScrollState,
		horizontalState = horizontalScrollState
	) {
		Column {
			TopBar(
				layer = layer,
				onBack = onBack
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
						Box {
							Box(
								contentAlignment = Alignment.CenterStart,
								modifier = Modifier.height(180.dp)
							) {
								val character = user.characterStats.find { it.characterId == user.character }!!
								val banner = user.banners.find { it.id == user.customBanner }!!
								AsyncImage(
									uri = "https://webassets.lowiro.com/${banner.resource}.png",
									contentDescription = null,
									modifier = Modifier
										.size(564.dp, 74.dp)
										.padding(start = 90.dp)
										.graphicsLayer {
											scaleX = -1F
										}
								)
								Box {
									AsyncImage(
										uri = "https://webassets.lowiro.com/chr/${character.icon}.png",
										contentDescription = null,
										modifier = Modifier.size(180.dp)
									)
									Box(
										contentAlignment = Alignment.Center,
										modifier = Modifier
											.offset(80.dp, 80.dp)
									) {
										AsyncImage(
											uri = pttIcon(relPtt.toDouble(), user.settings.isHideRating),
											contentDescription = null,
											modifier = Modifier.size(120.dp)
										)
										Text(
											text = relPtt,
											color = Color.White,
											style = MaterialTheme.typography.titleMedium
										)
									}
								}
							}
							Text(
								text = user.displayName,
								color = Color.White,
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
						Column(
							horizontalAlignment = Alignment.CenterHorizontally,
							verticalArrangement = Arrangement.spacedBy(8.dp),
						) {
							Row(
								horizontalArrangement = Arrangement.spacedBy(16.dp)
							) {
								Text(
									text = "B30: $b30Ptt"
								)
								Text(
									text = "R10: $r10Ptt"
								)
								Text(
									text = "B10: $b10Ptt"
								)
							}
							Text(
								text = "Range: $maxPtt ~ $minPtt"
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
							TrackList(value.b30)
						}
						Column(
							horizontalAlignment = Alignment.CenterHorizontally,
							verticalArrangement = Arrangement.spacedBy(8.dp)
						) {
							Text(
								text = "Recent 10",
								style = MaterialTheme.typography.headlineLarge
							)
							TrackList(value.r10)
						}
					}
				}
			}
		}
	}
}

@Composable
fun TopBar(layer: GraphicsLayer, onBack: () -> Unit) {
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
fun TrackList(list: List<Track>) {
	Column(
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		repeat(list.size / 5) { y ->
			Row(
				horizontalArrangement = Arrangement.spacedBy(16.dp)
			) {
				for (x in 0 until 5) {
					TrackCard(y * 5 + x, list[y * 5 + x])
				}
			}
		}
	}
}

@Composable
fun TrackCard(index: Int, track: Track) {
	Card(
		colors = CardDefaults.cardColors(
			containerColor = when {
				track.far + track.lost == 0 -> MaterialTheme.colorScheme.primaryContainer
				track.lost == 0 -> MaterialTheme.colorScheme.tertiaryContainer
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
					val chartConstant = remember {
						round(
							when {
								track.score >= 10000000 -> track.rating - 2
								track.score >= 9800000 -> track.rating - 1 - (track.score - 9800000) / 200000.0
								else -> track.rating - (track.score - 9500000) / 300000.0
							} * 10
						) / 10
					}
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