package com.example.closetcast

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TOKEN_KEY = "jwt_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
    }

    // 토큰 저장
    fun saveToken(token: String) {
        sharedPreferences.edit().putString(TOKEN_KEY, token).apply()
    }

    // 토큰 불러오기
    fun getToken(): String? {
        return sharedPreferences.getString(TOKEN_KEY, null)
    }

    // Refresh 토큰 저장
    fun saveRefreshToken(refreshToken: String) {
        sharedPreferences.edit().putString(REFRESH_TOKEN_KEY, refreshToken).apply()
    }

    // Refresh 토큰 불러오기
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(REFRESH_TOKEN_KEY, null)
    }

    // 토큰 삭제 (로그아웃)
    fun clearTokens() {
        sharedPreferences.edit().remove(TOKEN_KEY).remove(REFRESH_TOKEN_KEY).apply()
    }
}
