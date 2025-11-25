package com.example.closetcast.api

import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName

// ===== 로그인 =====
data class SignInRequestDto(
    @SerializedName("loginId")
    val loginId: String,

    @SerializedName("password")
    val password: String
)

data class SignInResponseDto(
    @SerializedName("memberId")
    val memberId: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("loginId")
    val loginId: String,

    @SerializedName("token")
    val token: String
)

data class ApiResponseSignInResponseDto(
    @SerializedName("isSuccess")
    val isSuccess: Boolean,

    @SerializedName("code")
    val code: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("result")
    val result: SignInResponseDto
)

// ===== 회원가입 =====
data class SignUpRequestDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("loginId")
    val loginId: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("preference")
    val preference: List<String>,

    @SerializedName("tendencies")
    val tendencies: List<String>
)

data class SignUpResponseDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("loginId")
    val loginId: String,

    @SerializedName("encodedPassword")
    val encodedPassword: String,

    @SerializedName("preference")
    val preference: List<String>,

    @SerializedName("tendencies")
    val tendencies: List<String>
)

data class ApiResponseSignUpResponseDto(
    @SerializedName("isSuccess")
    val isSuccess: Boolean,

    @SerializedName("code")
    val code: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("result")
    val result: SignUpResponseDto
)

// ===== 유저 정보 업데이트 =====
data class MemberUpdateRequestDto(
    @SerializedName("password")
    val password: String,

    @SerializedName("preference")
    val preference: List<String>,

    @SerializedName("tendencies")
    val tendencies: List<String>,

    @SerializedName("clothes")
    val clothes: List<String>
)

data class MemberUpdateResponseDto(
    @SerializedName("memberId")
    val memberId: Long,

    @SerializedName("password")
    val password: String,

    @SerializedName("preference")
    val preference: List<String>,

    @SerializedName("tendencies")
    val tendencies: List<String>,

    @SerializedName("clothes")
    val clothes: List<String>
)

data class ApiResponseMemberUpdateResponseDto(
    @SerializedName("isSuccess")
    val isSuccess: Boolean,

    @SerializedName("code")
    val code: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("result")
    val result: MemberUpdateResponseDto
)

// ===== 사용자 조회 =====
data class MemberDto(
    @SerializedName("memberId")
    val memberId: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("loginId")
    val loginId: String,

    @SerializedName("preference")
    val preference: List<String>,

    @SerializedName("tendencies")
    val tendencies: List<String>,

    @SerializedName("clothes")
    val clothes: List<String>
)

data class ApiResponseMemberDto(
    @SerializedName("isSuccess")
    val isSuccess: Boolean,

    @SerializedName("code")
    val code: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("result")
    val result: MemberDto
)

data class ApiResponseListMemberDto(
    @SerializedName("isSuccess")
    val isSuccess: Boolean,

    @SerializedName("code")
    val code: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("result")
    val result: List<MemberDto>
)

interface AuthApiService {
    @POST("/api/member/signin")
    suspend fun signIn(@Body request: SignInRequestDto): ApiResponseSignInResponseDto
    @POST("/api/member/signup")
    suspend fun signUp(@Body request: SignUpRequestDto): ApiResponseSignUpResponseDto

    @PATCH("/api/member/update/{memberId}")
    suspend fun updateMember(
        @Path("memberId") memberId: Long,
        @Body request: MemberUpdateRequestDto
    ): ApiResponseMemberUpdateResponseDto

    @GET("/api/member/read/{memberId}")
    suspend fun readMember(@Path("memberId") memberId: Long): ApiResponseMemberDto

    @GET("/api/member/read")
    suspend fun readListMember(): ApiResponseListMemberDto

    @DELETE("/api/member/delete/{memberId}")
    suspend fun deleteMember(@Path("memberId") memberId: Long): ApiResponseMemberDto
}

