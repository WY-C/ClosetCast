package com.example.closetcast.api

import retrofit2.http.POST
import retrofit2.http.Body
import com.google.gson.annotations.SerializedName

// ===== 로그인 =====
data class LoginRequest(
    @SerializedName("loginId")
    val loginId: String,

    @SerializedName("password")
    val password: String
)

data class LoginResult(
    @SerializedName("memberId")
    val memberId: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("loginId")
    val loginId: String,

    @SerializedName("token")
    val token: String
)

data class LoginResponse(
    @SerializedName("isSuccess")
    val isSuccess: Boolean,

    @SerializedName("code")
    val code: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("result")
    val result: LoginResult
)

// ===== 회원가입 =====
data class SignUpRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("loginId")
    val loginId: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("preference")
    val preference: String,

    @SerializedName("tendencies")
    val tendencies: List<String>
)

data class SignUpResult(
    @SerializedName("name")
    val name: String,

    @SerializedName("loginId")
    val loginId: String,

    @SerializedName("encodedPassword")
    val encodedPassword: String,

    @SerializedName("preference")
    val preference: String,

    @SerializedName("tendencies")
    val tendencies: List<String>
)

data class SignUpResponse(
    @SerializedName("isSuccess")
    val isSuccess: Boolean,

    @SerializedName("code")
    val code: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("result")
    val result: SignUpResult
)

interface AuthApiService {
    @POST("/api/member/signin")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("/api/member/signup")
    suspend fun signUp(@Body request: SignUpRequest): SignUpResponse
}
