package cn.yurin.arcaea.ptt.toolbox

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
	val email: String,
	val password: String
)