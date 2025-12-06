package com.example.closetcast

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.closetcast.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val _memberId = mutableStateOf<Long?>(null)
    val memberId: State<Long?> = _memberId

    private val _memberProfile = mutableStateOf(MemberProfile())
    val memberProfile: State<MemberProfile> = _memberProfile

    private val _signUpSuccess = MutableStateFlow(false)
    val signUpSuccess: StateFlow<Boolean> = _signUpSuccess

    private val _passwordChangeSuccess = MutableStateFlow(false)
    val passwordChangeSuccess: StateFlow<Boolean> = _passwordChangeSuccess

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess

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

                    if (response.isSuccess) {
                        val result = response.result
                        val token = result.token

                        Log.d("AuthViewModel", "========== 로그인 성공 ==========")
                        Log.d("AuthViewModel", "사용자: ${result.name}")
                        Log.d("AuthViewModel", "토큰: $token")
                        Log.d("AuthViewModel", "memberId: ${result.memberId}")


                        RetrofitClient.setToken(token)

                        // ✅ 1) 메인 스레드에서 로그인 상태/ID 저장
                        withContext(Dispatchers.Main) {
                            _memberId.value = result.memberId
                            _userInfo.value = result.name
                            _isLoggedIn.value = true
                            _isLoading.value = false
                        }

                        // ✅ 2) 로그인 직후 바로 서버에서 상세 정보 읽어오기
                        //     (preference, tendencies, clothes 등)
                        readMember(result.memberId)
                    } else {
                        val errorMessage = response.message
                        Log.w("AuthViewModel", "Error Response: $errorMessage")

                        withContext(Dispatchers.Main) {
                            _error.value = errorMessage
                            _isLoading.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMessage = "Please check your ID or Password"
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

                withContext(Dispatchers.Main) {
                    if (response.isSuccess) {
                        val result = response.result
                        withContext(Dispatchers.Main) {
                            _userInfo.value = result.name
                            _isLoggedIn.value = true  // 로그인 상태 true로 설정
                            // ✅ 회원가입 성공 시 memberId도 저장
                            _memberId.value = result.memberId
                            _isLoading.value = false
                            _signUpSuccess.value = true
                        }
                    } else {
                        // ✅ code가 String이므로 "403" 문자열로 체크
                        _error.value = when (response.code) {
                            "403" -> "The member already exists."
                            "400" -> "Invalid input data."
                            else -> response.message
                        }
                        _isLoading.value = false
                        _signUpSuccess.value = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "The Id already exists."
                    _isLoading.value = false
                    _signUpSuccess.value = false
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
                _memberId.value = null
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

    fun resetUpdateSuccess() {
        _updateSuccess.value = false
    }

    fun resetError() {
        _error.value = null
    }

    // ===== 1. 유저 정보 업데이트 (필수) =====
    fun updateMember(
        memberId: Long,
        password: String?,        // 현재 비밀번호 (없으면 null)
        newPassword: String?,     // 새 비밀번호 (없으면 null)
        preference: List<String>?,
        tendencies: List<String>?,
        clothes: List<String>?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val request = MemberUpdateRequestDto(
                    password = password,
                    newPassword = newPassword,
                    preference = preference,
                    tendencies = tendencies,
                    clothes = clothes
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.authApiService.updateMember(memberId, request)
                }

                withContext(Dispatchers.Main) {
                    if (response.isSuccess) {
                        // ✅ 로컬 memberProfile 을 요청 값으로 업데이트
                        val current = _memberProfile.value

                        _memberProfile.value = current.copy(
                            // password 는 별도 화면에서만 바꾸니 여기선 그대로 두고,
                            preference = preference ?: current.preference,
                            tendencies = tendencies ?: current.tendencies,
                            clothes    = clothes ?: current.clothes
                        )

                        if (password != null || newPassword != null) {
                            _passwordChangeSuccess.value = true
                        }

                        // 필요하면 userInfo 등 다른 상태도 갱신
                        _userInfo.value = memberId.toString()
                        _isLoading.value = false
                        _updateSuccess.value = true

                    } else {
                        // ✅ HTTP 코드에 따라 메시지 가공 (예시는 403이 “현재 비밀번호 오류”인 경우)
                        _error.value = when (response.code) {
                            "403" -> "Request is not appropriate."  // 일반적 메시지
                            "400" -> "Request is not appropriate."
                            else -> response.message ?: "Failed to update info."
                        }
                        _isLoading.value = false
                        _passwordChangeSuccess.value = false
                        _updateSuccess.value = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Your current Password does not match"
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
                        val r = response.result
                        _memberProfile.value = MemberProfile(
                            preference = r.preference,
                            tendencies = r.tendencies,
                            clothes = r.clothes
                        )
                        _isLoading.value = false
                    } else {
                        _error.value = response.message
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = e.message
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
                        _error.value = response.message
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
                        _error.value = response.message
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
