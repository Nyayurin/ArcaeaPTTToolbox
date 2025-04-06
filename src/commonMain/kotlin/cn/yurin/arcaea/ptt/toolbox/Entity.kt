package cn.yurin.arcaea.ptt.toolbox

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import okio.Path
import kotlin.math.round

enum class Difficulty(val color: Color) {
	PST(Color(0xFF328AA0)),
	PRS(Color(0xFF8C9B51)),
	FTR(Color(0xFF772F63)),
	BYD(Color(0xFFA0303F)),
	ETR(Color(0xFF675781))
}

data class User(
	val name: String,
	val id: String,
	val banner: Banner?,
	val character: Character,
	val ptt: PTT,
	val best: List<Track>,
	val recent: List<Track>
) {
	data class Banner(
		val resource: String,
		val id: String,
		val path: Path
	) {
		companion object {
			fun from(banner: UserResponse.Value.Banner, path: Path): Banner {
				return Banner(
					resource = banner.resource,
					id = banner.id,
					path = path
				)
			}
		}
	}

	data class Character(
		val icon: String,
		val path: Path
	) {
		companion object {
			fun from(character: UserResponse.Value.CharacterStat, path: Path): Character {
				return Character(
					icon = character.icon,
					path = path
				)
			}
		}
	}

	data class PTT(
		val real: Double,
		val max: Double,
		val min: Double,
		val b30: Double,
		val r10: Double,
		val b10: Double,
		val hide: Boolean,
		val path: Path
	)

	data class Track(
		val title: String,
		val img: Path,
		val difficulty: Difficulty,
		val chartConstant: Double,
		val score: Int,
		val ptt: Double,
		val pure: Int,
		val lowerPure: Int,
		val far: Int,
		val lost: Int,
		val notes: Int,
		val rating: Rating?,
		val instant: Instant
	) {
		fun isPM() = far + lost == 0 && score >= 10000000
		fun isFR() = lost == 0

		data class Rating(
			val max: Double,
			val lost: Double,
			val score: Double,
			val acc: Double
		)

		companion object {
			fun from(track: TrackResponse, path: Path): Track {
				val difficulty = Difficulty.entries[track.difficulty]
				val chartConstant = round(
					when {
						track.far + track.lost == 0 && track.score >= 10000000 -> track.rating - 2
						track.score >= 9800000 -> track.rating - 1 - (track.score - 9800000) / 200000.0
						else -> track.rating - (track.score - 9500000) / 300000.0
					} * 10
				) / 10
				val lowerPure = lowerPure(track.score, track.pure, track.far, track.lost)
				val maxScore = round(chartConstant * 4 * 95) / 10
				val notes = notes(track.score, track.pure, track.far, lowerPure)
				val ratingAcc = ((track.pure - lowerPure) / notes.toDouble()).let {
					when {
						it <= 0.9 -> 0.0
						it >= 0.995 -> 1.0
						else -> (it - 0.9) / 0.095
					}
				}
				val ratingScore = when {
					track.score <= 9900000 -> 0.0
					track.score >= 10000000 -> 1.0
					else -> (track.score - 9900000) / 100000.0
				} * 3
				val lostScore = maxScore - (ratingAcc + ratingScore) * chartConstant * 9.5
				return Track(
					title = track.title.en,
					img = path,
					difficulty = difficulty,
					chartConstant = chartConstant,
					score = track.score,
					ptt = track.rating,
					pure = track.pure,
					lowerPure = lowerPure,
					far = track.far,
					lost = track.lost,
					notes = notes,
					rating = Rating(
						max = maxScore,
						lost = lostScore,
						score = ratingScore,
						acc = ratingAcc
					),
					instant = Instant.fromEpochMilliseconds(track.time)
				)
			}
		}
	}

	companion object {
		fun from(user: UserResponse.Value, score: ScoreResponse.Value): User = runBlocking {
			val b30 = score.b30.map { it.rating }.sortedDescending()
			val r10 = score.r10.map { it.rating }.sortedDescending()
			val b10 = b30.take(10)
			val ptt = (b30.sum() + r10.sum()) / 40
			val banner = user.banners.find { it.id == user.customBanner }
			val character = user.characterStats.find { it.characterId == user.character }!!
			val bannerDeferred = async(Dispatchers.IO) {
				banner?.let {
					loadImage(
						url = "https://webassets.lowiro.com/${banner.resource}.png",
						path = privatePath("banner", banner.id + ".png")
					)
				}
			}
			val characterDeferred = async(Dispatchers.IO) {
				loadImage(
					url = "https://webassets.lowiro.com/chr/${character.icon}.png",
					path = privatePath("character", character.icon + ".png")
				)
			}
			val pttDeferred = async(Dispatchers.IO) {
				loadImage(
					url = pttIcon(ptt, user.settings.isHideRating),
					path = privatePath(
						"ptt", when {
							user.settings.isHideRating -> "off"
							ptt < 3.5 -> "0"
							ptt < 7.0 -> "1"
							ptt < 10.0 -> "2"
							ptt < 11.0 -> "3"
							ptt < 12.0 -> "4"
							ptt < 12.5 -> "5"
							ptt < 13.0 -> "6"
							else -> "7"
						} + ".png"
					)
				)
			}
			val trackDeferred = buildMap<String, Deferred<Path>> {
				(score.b30 + score.r10).distinctBy { it.bg }.forEach {
					put(
						key = it.bg,
						value = async(Dispatchers.IO) {
							loadImage(
								url = "https://webassets.lowiro.com/${it.bg}.jpg",
								path = privatePath("track", it.bg + ".jpg")
							)
						}
					)
				}
			}
			User(
				name = user.displayName,
				id = user.userCode,
				banner = banner?.let { Banner.from(it, bannerDeferred.await()!!) },
				character = Character.from(character, characterDeferred.await()),
				ptt = PTT(
					real = (b30.sum() + r10.sum()) / 40,
					max = (b30.sum() + b10.sum()) / 40,
					min = b30.sum() / 40,
					b30 = b30.sum() / 30,
					r10 = r10.sum() / 10,
					b10 = b10.sum() / 10,
					hide = user.settings.isHideRating,
					pttDeferred.await()
				),
				best = score.b30.map { Track.from(it, trackDeferred[it.bg]!!.await()) },
				recent = score.r10.map { Track.from(it, trackDeferred[it.bg]!!.await()) }
			)
		}
	}
}

sealed class PTTDialog {
	data object User : PTTDialog()
	data class Track(val track: cn.yurin.arcaea.ptt.toolbox.User.Track) : PTTDialog()
}