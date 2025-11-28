package com.example.closetcast

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.closetcast.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


data class TempSignUpInfo(
    val name: String = "",
    val loginId: String = "",
    val password: String = ""
)

// UI와 완전히 분리된 영역에서 로그인, 회원가입, 로그아웃 등 인증 관련 비즈니스 로직과 상태 관리
class AuthViewModel : ViewModel() {
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _isLoggedIn = mutableStateOf(false)
    val isLoggedIn: State<Boolean> = _isLoggedIn

    private val _userInfo = mutableStateOf<String?>(null)
    val userInfo: State<String?> = _userInfo


    fun login(loginId: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                withContext(Dispatchers.IO) {
                    Log.d("AuthViewModel", "========== 로그인 시작 ==========")
                    Log.d("AuthViewModel", "로그인 ID: $loginId")

                    // DTO Setup
                    val loginRequest = SignInRequestDto(loginId = loginId, password = password)
                    Log.d("AuthViewModel", "RequestBody: $loginRequest")

                    // DTO Network call
                    val response = RetrofitClient.authApiService.signIn(loginRequest)

                    Log.d("AuthViewModel", "========== 응답 받음 ==========")
                    Log.d("AuthViewModel", "응답 성공 여부: ${response.isSuccess}")
                    Log.d("AuthViewModel", "응답 메시지: ${response.message}")

                    if (response.isSuccess && response.result != null) {
                        val result = response.result!!
                        val token = result.token

                        Log.d("AuthViewModel", "========== 로그인 성공 ==========")
                        Log.d("AuthViewModel", "사용자: ${result.name}")
                        Log.d("AuthViewModel", "토큰: $token")

                        RetrofitClient.setToken(token)

                        withContext(Dispatchers.Main) {
                            _userInfo.value = result.name
                            _isLoggedIn.value = true
                            _isLoading.value = false
                        }
                    } else {
                        val errorMessage = response.message ?: "Error Response"
                        Log.w("AuthViewModel", "Error Response: $errorMessage")

                        withContext(Dispatchers.Main) {
                            _error.value = errorMessage
                            _isLoading.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMessage = "Error during login: ${e.message}"
                Log.e("AuthViewModel", errorMessage, e)
                _error.value = errorMessage
                _isLoading.value = false
            }
        }
    }

    fun signUp(name: String, loginId: String, password: String, preference: List<String>, tendencies: List<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // 회원가입 요청 DTO 생성
                val signUpRequest = SignUpRequestDto(
                    name = name,
                    loginId = loginId,
                    password = password,
                    preference = preference,
                    tendencies = tendencies
                )

                // API 호출 및 결과 받아오기
                val response = RetrofitClient.authApiService.signUp(signUpRequest)

                if (response.isSuccess) {
                    // 성공 시, 응답에서 필요한 데이터 활용
                    val result = response.result

                    withContext(Dispatchers.Main) {
                        // 예시: 사용자 정보 업데이트 및 로그인 상태를 true로 변경
                        _userInfo.value = result.name

                        _isLoading.value = false
                    }

                } else {
                    withContext(Dispatchers.Main) {
                        _error.value = response.message
                        _isLoading.value = false
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = e.localizedMessage
                    _isLoading.value = false
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                // 로그인 상태 초기화
                _isLoggedIn.value = false
                _userInfo.value = null
                _isLoading.value = false
                _error.value = null
            }

            // RetrofitClient에 저장된 토큰 삭제
            RetrofitClient.setToken("")

            // TokenManager를 사용하는 경우라면 아래 코드 추가 (현재 프로젝트에선 미사용 중)
            // TokenManager(context).clearTokens()
        }
    }

    fun resetAuthState() {
        _isLoggedIn.value = false
        _error.value = null
    }

    // ===== 1. 유저 정보 업데이트 (필수) =====
    fun updateMember(
        memberId: Long,
        password: String,
        preference: List<String>,
        tendencies: List<String>,
        clothes: List<String>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val request = MemberUpdateRequestDto(
                    password = password,
                    preference = preference,
                    tendencies = tendencies,
                    clothes = clothes
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.authApiService.updateMember(memberId, request)
                }

                withContext(Dispatchers.Main) {
                    if (response.isSuccess) {
                        _userInfo.value = response.result.memberId.toString()
                        _isLoading.value = false
                        // 성공 토스트 메시지는 화면에서 처리
                    } else {
                        _error.value = response.message ?: "Update failed"
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Error during update: ${e.localizedMessage}"
                    _isLoading.value = false
                }
            }
        }
    }

    // ===== 2. 특정 사용자 조회 =====
    fun readMember(memberId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.authApiService.readMember(memberId)
                }

                withContext(Dispatchers.Main) {
                    if (response.isSuccess) {
                        val member = response.result
                        _userInfo.value = "${member.name} (${member.loginId})"
                        _isLoading.value = false
                    } else {
                        _error.value = response.message ?: "Failed to read member"
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Error reading member: ${e.localizedMessage}"
                    _isLoading.value = false
                }
            }
        }
    }

    // ===== 3. 모든 사용자 조회 (관리자용) =====
    private val _memberList = mutableStateOf<List<MemberDto>>(emptyList())
    val memberList: State<List<MemberDto>> = _memberList

    fun readListMember() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.authApiService.readListMember()
                }

                withContext(Dispatchers.Main) {
                    if (response.isSuccess) {
                        _memberList.value = response.result
                        _isLoading.value = false
                    } else {
                        _error.value = response.message ?: "Failed to read member list"
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Error reading member list: ${e.localizedMessage}"
                    _isLoading.value = false
                }
            }
        }
    }

    // ===== 4. 회원탈퇴 =====
    fun deleteMember(memberId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.authApiService.deleteMember(memberId)
                }

                withContext(Dispatchers.Main) {
                    if (response.isSuccess) {
                        // 탈퇴 성공 시 로그아웃 처리
                        logout()
                    } else {
                        _error.value = response.message ?: "Failed to delete member"
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Error deleting member: ${e.localizedMessage}"
                    _isLoading.value = false
                }
            }
        }
    }

}
