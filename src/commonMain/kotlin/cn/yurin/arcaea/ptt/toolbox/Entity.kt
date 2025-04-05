package cn.yurin.arcaea.ptt.toolbox

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Score(
	val success: Boolean,
	val value: Value? = null
) {
	@Serializable
	data class Value(
		@SerialName("best_rated_scores")
		val b30: List<Track>,
		@SerialName("recent_rated_scores")
		val r10: List<Track>
	)
}

@Serializable
data class Track(
	val difficulty: Int,
	val rating: Double,
	val score: Int,
	@SerialName("perfect_count")
	val pure: Int,
	@SerialName("near_count")
	val far: Int,
	@SerialName("miss_count")
	val lost: Int,
	val title: Title,
	@SerialName("time_played")
	val time: Long,
	val bg: String
) {
	@Serializable
	data class Title(val en: String)
}

enum class Difficult {
	PST, PRS, FTR, BYD, ETR
}

@Serializable
data class Login(
	val isLoggedIn: Boolean? = null,
	val error: Error? = null
) {
	@Serializable
	data class Error(
		val name: String,
		val message: String
	)
}

@Serializable
data class LoginRequest(
	val email: String,
	val password: String
)

@Serializable
data class User(
	val success: Boolean,
	@SerialName("error_code")
	val errorCode: Int? = null,
	val value: Value? = null
) {
	@Serializable
	data class Value(
		@SerialName("character_stats")
		val characterStats: List<CharacterStat>,
		val settings: Settings,
		val banners: List<Banner>,
		@SerialName("user_code")
		val userCode: String,
		@SerialName("display_name")
		val displayName: String,
		val character: Int,
		@SerialName("custom_banner")
		val customBanner: String? = null
	) {
		@Serializable
		data class CharacterStat(
			val icon: String,
			@SerialName("character_id")
			val characterId: Int
		)

		@Serializable
		data class Settings(
			@SerialName("is_hide_rating")
			val isHideRating: Boolean
		)

		@Serializable
		data class Banner(
			val resource: String,
			val id: String
		)
	}
}

sealed class PTTDialog {
	data class User(
		val b30: Double,
		val r10: Double,
		val b10: Double,
		val rel: Double,
		val max: Double,
		val min: Double
	) : PTTDialog()

	data class Track(
		val title: String,
		val image: String,
		val chartConstant: Double,
		val difficult: Difficult,
		val ptt: Double,
		val timestamp: Long
	) : PTTDialog()
}