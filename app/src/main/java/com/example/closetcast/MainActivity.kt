package com.example.closetcast

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.closetcast.ui.theme.ClosetCastTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    // ✅ 새로운 방식: Activity Result Contract 사용
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        Log.d("MainActivity", "========== Auth request result. ==========")
        Log.d("MainActivity", "FINE_LOCATION: $fineLocationGranted")
        Log.d("MainActivity", "COARSE_LOCATION: $coarseLocationGranted")

        if (fineLocationGranted || coarseLocationGranted) {
            Log.d("MainActivity", "Location permission approved.")
            // 권한 허용 시 처리 로직
        } else {
            Log.d("MainActivity", "Location permission denied.")
            // 권한 거부 시 처리 로직 (사용자 안내 등)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 권한 요청
        requestLocationPermissions()

        enableEdgeToEdge()
        setContent {
            ClosetCastTheme {
                val authViewModel: AuthViewModel = viewModel()
                Surface {
                    AppNavigation(authViewModel = authViewModel)
                }
            }
        }
    }

    private fun requestLocationPermissions() {
        Log.d("MainActivity", "Start to request location permissions")

        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )


        // 이미 권한이 있는지 확인
        val hasFineLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        Log.d("MainActivity", "Current Auth - FINE: $hasFineLocation, COARSE: $hasCoarseLocation")

        // 권한이 없으면 요청
        if (!hasFineLocation || !hasCoarseLocation) {
            Log.d("MainActivity", "Authorization Required")
            requestPermissionLauncher.launch(permissions)
        } else {
            Log.d("MainActivity", "Already Authorized.")
        }
    }
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable("signup") {
            SignUpScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable("main") {
            MainScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // ✅ 스타일 & 민감도: 항상 프로필 수정용 (회원가입 인자 제거)
        composable(
            route = "styleandsensitivity?from={from}",
            arguments = listOf(
                navArgument("from") {
                    type = NavType.StringType
                    defaultValue = "profile"  // 기본값: 프로필 편집
                }
            )
        ) { backStackEntry ->
            val fromSource = backStackEntry.arguments?.getString("from") ?: "profile"
            StyleAndSensitivityScreen(
                navController = navController,
                authViewModel = authViewModel,
                from = fromSource  // argument 전달
            )
        }

        composable(
            route = "clothessetting?from={from}",
            arguments = listOf(
                navArgument("from") {
                    type = NavType.StringType
                    defaultValue = "profile"  // 기본값: 프로필 편집
                }
            )
        ) { backStackEntry ->
            val fromSource = backStackEntry.arguments?.getString("from") ?: "profile"
            ClothesSetting(
                navController = navController,
                authViewModel = authViewModel,
                from = fromSource  // argument 전달
            )
        }

        composable("changepassword") {
            ChangePasswordScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable("withdraw") {
            WithdrawScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel) {
    var loginId by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current
    val isLoading by authViewModel.isLoading
    val error by authViewModel.error
    val isLoggedIn by authViewModel.isLoggedIn

    // ✅ LoginScreen 진입 시 인증 상태 리셋 (처음 한 번만
    LaunchedEffect(Unit) {
        authViewModel.resetAuthState()
    }

    // ✅ 로그인 성공 시 자동 메인 화면 이동
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Scaffold { innerPadding ->
        // 1) 파란 그라데이션 배경
        val backgroundBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF7CB5FF),
                Color(0xFF001ECB)
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            // 2) 흰 카드 안에 기존 Column 내용 그대로 이동
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ClosetCast Login",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = loginId,
                        onValueChange = { loginId = it },
                        label = { Text("LoginId") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = {
                            if (loginId.isNotEmpty() && password.isNotEmpty()) {
                                authViewModel.login(loginId, password)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please enter LoginId and Password",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading && loginId.isNotEmpty() && password.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5B7FFF) // 파란 버튼
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(text = "Login", fontSize = 18.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = { navController.navigate("signup") }) {
                        Text("Sign Up", color = Color(0xFF5B7FFF))
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var name by rememberSaveable { mutableStateOf("") }
    var loginId by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordCheck by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current
    val isLoading by authViewModel.isLoading
    val error by authViewModel.error

    val signUpSuccess by authViewModel.signUpSuccess.collectAsState()   // StateFlow<Boolean> 이라고 가정

    LaunchedEffect(signUpSuccess) {
        if (signUpSuccess) {
            Toast.makeText(
                context,
                "SignUp Success. Please Set your style preference and Sensitivity.",
                Toast.LENGTH_SHORT
            ).show()
            navController.navigate("styleandsensitivity?from=signup") {
                popUpTo("signup") { inclusive = true }
            }
        }
    }

    Scaffold { innerPadding ->
        // ✅ 파란 그라데이션 배경
        val backgroundBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF7CB5FF),
                Color(0xFF001ECB)
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            // ✅ 흰색 Card 안에 모든 내용 감싸기
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),  // ✅ 긴 내용용 스크롤 추가
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ClosetCast Sign Up",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Name 입력
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // LoginId 입력
                    OutlinedTextField(
                        value = loginId,
                        onValueChange = { loginId = it },
                        label = { Text("LoginId") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password 입력
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Check 입력
                    OutlinedTextField(
                        value = passwordCheck,
                        onValueChange = { passwordCheck = it },
                        label = { Text("Password Check") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ✅ 에러 메시지 표시
                    if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }

                    // ✅ 회원가입 버튼 - 파란색
                    Button(
                        onClick = {
                            if (name.isNotBlank() &&
                                loginId.isNotBlank() &&
                                password.length >= 6 &&
                                password == passwordCheck
                            ) {
                                authViewModel.signUp(
                                    name = name,
                                    loginId = loginId,
                                    password = password,
                                    preference = emptyList(),
                                    tendencies = emptyList()
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    "Password must be at least 6 characters and match.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading &&
                                name.isNotBlank() &&
                                loginId.isNotBlank() &&
                                password.isNotEmpty() &&
                                passwordCheck.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5B7FFF)  // ✅ 파란색 버튼
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White  // ✅ 로더 흰색
                            )
                        } else {
                            Text(
                                text = "Sign Up",
                                fontSize = 18.sp,
                                color = Color.White  // ✅ 텍스트 흰색
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = { navController.popBackStack() }) {
                        Text(
                            "Already have an account? Login",
                            color = Color(0xFF5B7FFF)  // ✅ 텍스트 파란색
                        )
                    }
                }
            }
        }
    }
}

// Sealed class for Bottom Navigation items
sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Weather : BottomNavItem("weather", "Weather", Icons.Filled.WbSunny)
    object Clothing : BottomNavItem("clothing", "Clothes Recommendation", Icons.Filled.Checkroom)
}

data class CurrentWeather(
    val location: String,
    val temperature: Double,
    val apparentTemperature: Double,
    val weatherCondition: String,
    val minTemp: Double,
    val maxTemp: Double
)

data class HourlyForecast(
    val time: String,
    val temperature: Double,
    val weatherIcon: ImageVector
)

data class DailyForecast(
    val day: String,
    val minTemp: Double,
    val maxTemp: Double,
    val weatherIcon: ImageVector
)

data class WeatherData(
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>
)

data class ClothingRecommendation(val outer: String, val top: String, val bottom: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    Log.d("MainScreen", "authViewModel memberId: ${authViewModel.memberId.value}")
    val bottomBarNavController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val items = listOf(
        BottomNavItem.Weather,
        BottomNavItem.Clothing
    )

    val weatherViewModel = viewModel<WeatherViewModel>()
    val weatherData by weatherViewModel.weatherData
    val isLoading by weatherViewModel.isLoading
    val errorMessage by weatherViewModel.error

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF7CB5FF),
            Color(0xFF001ECB)
        )
    )

    // 앱 시작 시 위치 정보 요청
    LaunchedEffect(Unit) {
        Log.d("MainScreen", "LaunchedEffect started")
        try {
            val helper = LocationHelper(context)
            helper.requestCurrentLocation { lat, lng ->
                Log.d("MainScreen", "Location updated: lat=$lat, lng=$lng")
                weatherViewModel.fetchWeather(lat, lng)
            }
        } catch (e: Exception) {
            Log.e("MainScreen", "Failed to get location", e)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                // ✅ 여기 이 부분만 새로운 사이드바 UI로 교체
                AppDrawer(
                    navController = navController,
                    authViewModel = authViewModel,
                    drawerState = drawerState,
                    scope = scope
                )
            }
        ) {
            // ✅ 이 아래는 원래 코드 그대로 - 절대 수정하지 않음
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            val navBackStackEntry by bottomBarNavController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route
                            val title = when (currentRoute) {
                                BottomNavItem.Weather.route -> BottomNavItem.Weather.title
                                BottomNavItem.Clothing.route -> BottomNavItem.Clothing.title
                                else -> "ClosetCast"
                            }
                            Text(text = title, color = Color.White)
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    drawerState.apply {
                                        if (isClosed) open() else close()
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White
                        )
                    )
                },
                bottomBar = {
                    NavigationBar {
                        val navBackStackEntry by bottomBarNavController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        items.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = screen.title) },
                                label = { Text(screen.title) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
                                    bottomBarNavController.navigate(screen.route) {
                                        popUpTo(bottomBarNavController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                },
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground
            ) { innerPadding ->
                NavHost(
                    bottomBarNavController,
                    startDestination = BottomNavItem.Weather.route,
                    Modifier.padding(innerPadding)
                ) {
                    composable(BottomNavItem.Weather.route) {
                        if (isLoading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else if (errorMessage != null) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                            }
                        } else if (weatherData != null) {
                            WeatherScreen(weatherData!!)
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Updating the location...")
                            }
                        }
                    }
                    composable(BottomNavItem.Clothing.route) {
                        if (weatherData != null) {
                            ClothingRecommendationScreen(
                                weatherData = weatherData!!,
                                authViewModel = authViewModel,
                                weatherViewModel = weatherViewModel
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Updating the Weather info...")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ✅ 새로운 사이드바 컴포저블 (MainScreen 바깥에 추가)
@Composable
fun AppDrawer(
    navController: NavController,
    authViewModel: AuthViewModel,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.75f),
        drawerContainerColor = Color.White,  // 흰색 배경
        drawerTonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 메뉴 제목
            Text(
                "Menu",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )

            // Edit Password
            DrawerMenuItem(
                label = "Edit Password",
                isWarning = false,
                onClick = {
                    navController.navigate("changepassword")
                    scope.launch { drawerState.close() }
                }
            )
            Spacer(Modifier.height(12.dp))

            // Edit My Closet
            DrawerMenuItem(
                label = "Edit My Closet",
                isWarning = false,
                onClick = {
                    navController.navigate("clothessetting")
                    scope.launch { drawerState.close() }
                }
            )
            Spacer(Modifier.height(12.dp))

            // Edit Personal Information
            DrawerMenuItem(
                label = "Edit Personal Information",
                isWarning = false,
                onClick = {
                    navController.navigate("styleandsensitivity?from=profile")  // 명시적으로 profile 지정
                    scope.launch { drawerState.close() }
                }
            )
            Spacer(Modifier.height(24.dp))

            // Logout
            DrawerMenuItem(
                label = "Logout",
                isWarning = false,
                onClick = {
                    authViewModel.logout()
                    scope.launch { drawerState.close() }
                    navController.navigate("login") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
            )
            Spacer(Modifier.height(12.dp))

            // Account Withdrawal (빨간색)
            DrawerMenuItem(
                label = "Account Withdrawal",
                isWarning = true,
                onClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate("withdraw")
                }
            )
        }
    }
}

// ✅ 개별 메뉴 아이템 (MainScreen 바깥에 추가)
@Composable
fun DrawerMenuItem(
    label: String,
    isWarning: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F4FF)  // 연한 파란 배경
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isWarning) Color(0xFFE63946) else Color.Black,  // 경고는 빨간색
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WeatherScreen(weatherData: WeatherData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CurrentWeatherCard(weatherData.current)
        Spacer(modifier = Modifier.height(16.dp))
        HourlyForecastCard(weatherData.hourly)
        Spacer(modifier = Modifier.height(16.dp))
        DailyForecastCard(weatherData.daily)
    }
}

@Composable
fun CurrentWeatherCard(currentWeather: CurrentWeather) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "The temperature is now... ", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = currentWeather.location, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "${currentWeather.temperature}°", fontSize = 64.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Min: ${currentWeather.minTemp}° Max: ${currentWeather.maxTemp}°", fontSize = 16.sp, color = Color.White)
        Spacer(modifier = Modifier.height(12.dp))
        ApparentTemperatureCard(currentWeather.apparentTemperature)
    }
}

@Composable
fun HourlyForecastCard(hourlyForecasts: List<HourlyForecast>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Hourly Forecast", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            hourlyForecasts
                .take(6)
                .forEach { forecast ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = forecast.time, fontSize = 16.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Icon(forecast.weatherIcon, contentDescription = null, modifier = Modifier.size(32.dp),tint = Color.White )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${forecast.temperature}°", fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DailyForecastCard(dailyForecasts: List<DailyForecast>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "3-Day Forecast", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))

        dailyForecasts.forEach { forecast ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 요일 / 날짜
                Text(
                    text = forecast.day,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                    , color = Color.White
                )

                // 아이콘
                Box(
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        forecast.weatherIcon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }

                // Min
                Text(
                    text = "Min: ${forecast.minTemp}°",
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                    , color = Color.White
                )

                // Max
                Text(
                    text = "Max: ${forecast.maxTemp}°",
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                    , color = Color.White
                )
            }
        }
    }
}

@Composable
fun ApparentTemperatureCard(apparentTemperature: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Feels Like",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$apparentTemperature°",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ClothingRecommendationCard(
    recommendation: ClothingRecommendation,
    isLoading: Boolean = false,
    recommendationType: RecommendationType = RecommendationType.TEMPERATURE_BASED,
    onRefreshClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✨ IMPROVED: 추천 유형에 따라 다른 메시지 표시
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (recommendationType) {
                    RecommendationType.TEMPERATURE_BASED ->
                        MaterialTheme.colorScheme.secondaryContainer
                    RecommendationType.AI_BASED ->
                        MaterialTheme.colorScheme.primaryContainer
                }
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (recommendationType) {
                        RecommendationType.TEMPERATURE_BASED -> "🌡️ Temperature Based"
                        RecommendationType.AI_BASED -> "✨ AI Recommendation"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = when (recommendationType) {
                        RecommendationType.TEMPERATURE_BASED ->
                            MaterialTheme.colorScheme.onSecondaryContainer
                        RecommendationType.AI_BASED ->
                            MaterialTheme.colorScheme.primary
                    },
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (recommendationType) {
                        RecommendationType.TEMPERATURE_BASED ->
                            "The clothes based on current temperature."
                        RecommendationType.AI_BASED ->
                            "Recommended based on current weather and your closet."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when (recommendationType) {
                        RecommendationType.TEMPERATURE_BASED ->
                            MaterialTheme.colorScheme.onSecondaryContainer
                        RecommendationType.AI_BASED ->
                            MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    textAlign = TextAlign.Center
                )
            }
        }

        // ✅ 의류 추천 카드들 (Row로 원 + 텍스트 가로 배치)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),  // ✅ 항목 간 간격
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Outer Wear Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)  // ✅ 원과 텍스트 사이 간격
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)  // ✅ 크기 조정
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (recommendation.outer.lowercase() != "none") {
                        Image(
                            painter = painterResource(
                                getImageResourceForClothingName(recommendation.outer)
                            ),
                            contentDescription = recommendation.outer,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Outer",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recommendation.outer,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            // Top Wear Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (recommendation.top.lowercase() != "none") {
                        Image(
                            painter = painterResource(
                                getImageResourceForClothingName(recommendation.top)
                            ),
                            contentDescription = recommendation.top,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Top",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recommendation.top,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            // Bottom Wear Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (recommendation.bottom.lowercase() != "none") {
                        Image(
                            painter = painterResource(
                                getImageResourceForClothingName(recommendation.bottom)
                            ),
                            contentDescription = recommendation.bottom,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Bottom",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recommendation.bottom,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = { onRefreshClick() },
            enabled = !isLoading,
            border = BorderStroke(2.dp, Color.White)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            } else {
                Text("Other Recommendations", color = Color.White)
            }
        }
    }
}



fun getImageResourceForClothingName(name: String): Int {
    return when (name.trim().uppercase()) {
        "PUFFER JACKET" -> R.drawable.puffer_coat
        "PUFFER_JACKET" -> R.drawable.puffer_coat
        "COAT" -> R.drawable.trench_coat
        "FLEECE" -> R.drawable.fleece
        "JACKET" -> R.drawable.jacket
        "WINDBREAKER" -> R.drawable.windbreaker
        "SWEATER" -> R.drawable.sweater
        "HOODIE" -> R.drawable.hoodie
        "SHIRT" -> R.drawable.shirts
        "SHORT SLEEVE" -> R.drawable.short_sleeves
        "SHORT_SLEEVE" -> R.drawable.short_sleeves
        "LONG SLEEVE" -> R.drawable.long_sleeves
        "LONG_SLEEVE" -> R.drawable.long_sleeves
        "JEANS" -> R.drawable.jeans
        "COTTON PANTS" -> R.drawable.trouser
        "COTTON_PANTS" -> R.drawable.trouser
        "SHORTS" -> R.drawable.shorts
        else -> R.drawable.default_clothing // default image
    }
}

fun getRecommendationForTemperature(temp: Double): ClothingRecommendation {
    // 기온에 따라 옷 종류 중 하나를 추천해 주는 하드 코딩 방식, 사용자가 어떤 옷을 입고있는지 상관 X
    val topList: List<String>
    val outerList: List<String>
    val bottomList: List<String>

    when {
        temp >= 28.0 -> {
            outerList = listOf("None")
            topList = listOf("Short Sleeve")
            bottomList = listOf("Shorts")
        }
        temp in 23.0..28.0 -> {
            outerList = listOf("None")
            topList = listOf("Short sleeve", "Long Sleeve")
            bottomList = listOf("Shorts", "Cotton pants")
        }
        temp in 20.0..23.0 -> {
            outerList = listOf("WindBreaker")
            topList = listOf("Shirt", "Long sleeve", "Short Sleeve")
            bottomList = listOf("Jeans", "Cotton pants")
        }
        temp in 17.0..20.0 -> {
            outerList = listOf("Jacket", "WindBreaker")
            topList = listOf("Shirt", "Hoodie", "Long Sleeve")
            bottomList = listOf("Jeans", "Cotton pants")
        }
        temp in 12.0..17.0 -> {
            outerList = listOf("Jacket", "Windbreaker", "Coat")
            topList = listOf("Sweater", "Shirt", "Hoodie", "Long Sleeve")
            bottomList = listOf("Jeans", "Cotton pants")
        }
        temp in 9.0..12.0 -> {
            outerList = listOf("Coat", "Fleece", "Jacket")
            topList = listOf("Sweater", "Hoodie", "Long Sleeve")
            bottomList = listOf("Jeans", "Cotton pants")
        }
        temp in 4.0..9.0 -> {
            outerList = listOf("Coat", "Puffer Jacket", "Fleece")
            topList = listOf("Sweater", "Hoodie")
            bottomList = listOf("Jeans", "Cotton pants")
        }
        else -> {
            outerList = listOf("Puffer Jacket")
            topList = listOf("Sweater", "Hoodie")
            bottomList = listOf("Jeans", "Cotton pants")
        }
    }

    val outer = outerList[Random.nextInt(outerList.size)]
    val top = topList[Random.nextInt(topList.size)]
    val bottom = bottomList[Random.nextInt(bottomList.size)]

    return ClothingRecommendation(outer, top, bottom)
}

enum class RecommendationType {
    TEMPERATURE_BASED,  // 기온 기반 (하드코딩)
    AI_BASED           // AI 기반 (API)
}

@Composable
fun ClothingRecommendationScreen(
    weatherData: WeatherData,
    authViewModel: AuthViewModel,
    weatherViewModel: WeatherViewModel
) {
    var isLoading by remember { mutableStateOf(false) }
    var recommendation by remember {
        mutableStateOf(getRecommendationForTemperature(weatherData.current.temperature))
    }

    // ✨ NEW: 추천 유형 상태 추가
    var recommendationType by remember {
        mutableStateOf(RecommendationType.TEMPERATURE_BASED)
    }

    val memberId by authViewModel.memberId

    ClothingRecommendationCard(
        recommendation = recommendation,
        isLoading = isLoading,
        recommendationType = recommendationType,  // ✨ 전달
        onRefreshClick = {
            Log.d("ClothingRecommendationScreen", "memberId: $memberId")
            if (memberId != null) {
                isLoading = true

                weatherViewModel.getRecommend(memberId!!) { newRecommendation ->
                    recommendation = newRecommendation
                    isLoading = false
                    recommendationType = RecommendationType.AI_BASED  // ✨ AI 유형으로 설정
                }
            } else {
                Log.d("ClothingRecommendationScreen", "memberId is null!")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController,
    authViewModel: AuthViewModel   // ✅ ViewModel 주입
) {
    var oldPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current
    val isLoading by authViewModel.isLoading
    val error by authViewModel.error
    val memberId by authViewModel.memberId
    val passwordChangeSuccess by authViewModel.passwordChangeSuccess.collectAsState()

    // ✅ 화면 진입 시 에러 초기화
    LaunchedEffect(Unit) {
        authViewModel.resetError()
    }
    LaunchedEffect(passwordChangeSuccess) {
        if (passwordChangeSuccess) {
            navController.popBackStack()
            Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Password",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous",
                            tint = Color.White  // ✅ 아이콘 흰색
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent  // ✅ 투명 배경
                )
            )
        }
    ) { innerPadding ->
        // ✅ 파란 그라데이션 배경
        val backgroundBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF7CB5FF),
                Color(0xFF001ECB)
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            // ✅ 흰색 Card 안에 내용
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 에러 메시지
                    if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }

                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            Log.d("ChangePassword", "memberId = $memberId")
                            when {
                                oldPassword.isEmpty() ->
                                    Toast.makeText(context, "Enter the current Password", Toast.LENGTH_SHORT).show()

                                newPassword.isEmpty() ->
                                    Toast.makeText(context, "Enter the new Password", Toast.LENGTH_SHORT).show()

                                confirmPassword.isEmpty() ->
                                    Toast.makeText(context, "Enter the confirm Password", Toast.LENGTH_SHORT).show()

                                newPassword.length < 6 ->
                                    Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()

                                newPassword != confirmPassword ->
                                    Toast.makeText(context, "New Password and Confirm Password do not match", Toast.LENGTH_SHORT).show()

                                oldPassword == newPassword ->
                                    Toast.makeText(context, "New Password cannot be the same as the current Password", Toast.LENGTH_SHORT).show()

                                memberId == null ->
                                    Toast.makeText(context, "Cannot fetch member ID", Toast.LENGTH_SHORT).show()

                                else -> {
                                    authViewModel.updateMember(
                                        memberId = memberId!!,
                                        password = oldPassword,
                                        newPassword = newPassword,
                                        preference = null,
                                        tendencies = null,
                                        clothes = null
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading &&
                                oldPassword.isNotEmpty() &&
                                newPassword.isNotEmpty() &&
                                confirmPassword.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5B7FFF)  // ✅ 파란색 버튼
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                "Change Password",
                                fontSize = 18.sp,
                                color = Color.White  // ✅ 텍스트 흰색
                            )
                        }
                    }
                }
            }
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF7CB5FF),
            Color(0xFF001ECB)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Account Withdrawal",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            // ✅ 흰색 Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Are you sure you want to withdraw your account?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "You can't restore your account after withdrawal.",
                        fontSize = 14.sp,
                        color = Color(0xFFE63946),  // ✅ 경고 빨간색
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // ✅ 빨간색 경고 버튼
                    Button(
                        onClick = {
                            val memberId = authViewModel.memberId.value
                            if (memberId == null) {
                                Toast.makeText(
                                    context,
                                    "User ID not found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                authViewModel.deleteMember(memberId)
                                navController.navigate("login") {
                                    popUpTo(0)  // 모든 이전 스택 제거
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE63946)  // ✅ 빨간색 경고 버튼
                        )
                    ) {
                        Text(
                            "Account Withdrawal",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StyleSelectionItem(
    style: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onSelect)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = null // Click is handled by the parent Column
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.secondaryContainer
                    )
            ) {
                // 스타일별 이미지 추가
                Image(
                    painter = painterResource(getStyleImageResource(style)),
                    contentDescription = style,
                    modifier = Modifier
                        .fillMaxSize(),          // Box 크기에 꽉 차게
                    contentScale = ContentScale.Crop   // 비율 유지하며 잘라서 채우기
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = style, style = MaterialTheme.typography.bodyLarge)
    }
}

fun getStyleImageResource(style: String): Int {
    return when (style.trim().lowercase()) {
        "minimal" -> R.drawable.minimal
        "casual" -> R.drawable.casual
        "street" -> R.drawable.street
        "classic" -> R.drawable.classic
        "dandy" -> R.drawable.dandy
        "retro" -> R.drawable.retro
        else -> R.drawable.default_clothing
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleAndSensitivityScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    from: String = "profile"  // argument 받기
)  {
    val styles = listOf("Minimal", "Casual", "Street", "Classic", "Dandy", "Retro")

    val context = LocalContext.current
    val isLoading by authViewModel.isLoading
    val error by authViewModel.error
    val memberId by authViewModel.memberId
    val memberProfile by authViewModel.memberProfile
    val updateSuccess by authViewModel.updateSuccess.collectAsState()

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF7CB5FF),
            Color(0xFF001ECB)
        )
    )

    // ✅ 서버에 저장된 값 (항상 "수정 모드"로 사용)
    val savedPreference = memberProfile.preference
    val savedTendencies = memberProfile.tendencies

    // 스타일 선택 초기값
    val selectedStyles = rememberSaveable(memberProfile) {
        mutableStateListOf<String>().apply {
            addAll(
                savedPreference.map { pref ->
                    pref.lowercase().replaceFirstChar { it.titlecase() }
                }.filter { it in styles }
            )
        }
    }

    // 민감도 스위치 초기값
    var heatSensitive by rememberSaveable(memberProfile) {
        mutableStateOf("HOT" in savedTendencies)
    }
    var coldSensitive by rememberSaveable(memberProfile) {
        mutableStateOf("COLD" in savedTendencies)
    }

    DisposableEffect(Unit) {
        authViewModel.resetUpdateSuccess()
        authViewModel.resetError()
        onDispose { }
    }

    // ✅ 수정 성공 시에만 메인으로 이동
    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            // ✅ from argument에 따라 분기
            if (from == "signup") {
                // 회원가입 플로우: 옷장 설정으로 이동
                navController.navigate("clothessetting?from=signup") {
                    popUpTo("styleandsensitivity") { inclusive = true }
                }
                Toast.makeText(
                    context,
                    "Style preference and sensitivity set. Now select your clothes.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // 프로필 수정: 이전 화면으로 돌아가기
                Toast.makeText(
                    context,
                    "Update your personal Information completed.",
                    Toast.LENGTH_SHORT
                ).show()
                navController.popBackStack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Personal Information",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ✅ 에러 메시지
                if (error != null) {
                    Text(
                        text = error!!,
                        color = Color(0xFFE63946),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }

                // ✅ 스타일 선택 Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Choose your style preference",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            maxItemsInEachRow = 2
                        ) {
                            styles.forEach { style ->
                                StyleSelectionItem(style, selectedStyles.contains(style)) {
                                    if (selectedStyles.contains(style)) {
                                        selectedStyles.remove(style)
                                    } else {
                                        selectedStyles.add(style)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ 민감도 선택 Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Choose your sensitivity tendency",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sensitive to Heat", fontSize = 16.sp)
                            Switch(checked = heatSensitive, onCheckedChange = { heatSensitive = it })
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sensitive to Cold", fontSize = 16.sp)
                            Switch(checked = coldSensitive, onCheckedChange = { coldSensitive = it })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ✅ Done 버튼 - 화면 내부에 포함
                Button(
                    onClick = {
                        val preference = selectedStyles
                            .map { it.uppercase() }
                            .ifEmpty { listOf("CASUAL") }

                        val tendencies = buildList {
                            if (heatSensitive) add("HOT")
                            if (coldSensitive) add("COLD")
                        }

                        if (memberId == null) {
                            Toast.makeText(
                                context,
                                "Cannot fetch member info",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        authViewModel.updateMember(
                            memberId = memberId!!,
                            password = null,
                            newPassword = null,
                            preference = preference,
                            tendencies = tendencies,
                            clothes = null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B7FFF)  // ✅ 파란색 버튼
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Done", fontSize = 18.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}



@Composable
fun ClothingItem(name: String, isSelected: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Switch(checked = isSelected, onCheckedChange = onToggle)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = name, fontSize = 16.sp)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothesSetting(
    navController: NavController,
    authViewModel: AuthViewModel,
    from: String = "profile"  // argument 받기
) {
    val context = LocalContext.current
    val isLoading by authViewModel.isLoading
    val error by authViewModel.error
    val memberId by authViewModel.memberId
    val memberProfile by authViewModel.memberProfile

    // 1) 사용자가 가진 옷 set (서버 포맷 → Set)
    val ownedClothes = remember(memberProfile.clothes) {
        memberProfile.clothes
            .map { it.trim().uppercase() }
            .toSet()
    }

    // 2) 초기값을 ownedClothes 기준으로 true/false 세팅
    val (outerwear, setOuterwear) = remember(ownedClothes) {
        mutableStateOf(
            mapOf(
                "Puffer Jacket" to ("PUFFER_JACKET" in ownedClothes),
                "Coat"          to ("COAT" in ownedClothes),
                "Fleece"        to ("FLEECE" in ownedClothes),
                "Jacket"        to ("JACKET" in ownedClothes),
                "Windbreaker"   to ("WINDBREAKER" in ownedClothes)
            )
        )
    }

    val (tops, setTops) = remember(ownedClothes) {
        mutableStateOf(
            mapOf(
                "Sweater"      to ("SWEATER" in ownedClothes),
                "Hoodie"       to ("HOODIE" in ownedClothes),
                "Shirt"        to ("SHIRT" in ownedClothes),
                "Long sleeve"  to ("LONG_SLEEVE" in ownedClothes),
                "Short sleeve" to ("SHORT_SLEEVE" in ownedClothes)
            )
        )
    }

    val (bottoms, setBottoms) = remember(ownedClothes) {
        mutableStateOf(
            mapOf(
                "Jeans"        to ("JEANS" in ownedClothes),
                "Cotton pants" to ("COTTON_PANTS" in ownedClothes),
                "Shorts"       to ("SHORTS" in ownedClothes)
            )
        )
    }

    // ✅ 화면 진입 시 에러 초기화
    LaunchedEffect(Unit) {
        authViewModel.resetError()
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF7CB5FF),
            Color(0xFF001ECB)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit My Closet",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 에러 메시지
                if (error != null) {
                    Text(
                        text = when {
                            error?.contains("Your current Password does not match") == true ->
                                "Please select at least one item in each category."
                            else -> error!!
                        },
                        color = Color(0xFFE63946),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }

                // ✅ 옷 선택 Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Outer Wear Section
                        Text(
                            "Outer Wear",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        outerwear.keys.forEach { item ->
                            ClothingItem(
                                name = item,
                                isSelected = outerwear[item] ?: false,
                                onToggle = { checked ->
                                    setOuterwear(outerwear + (item to checked))
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Top Wear Section
                        Text(
                            "Top Wear",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        tops.keys.forEach { item ->
                            ClothingItem(
                                name = item,
                                isSelected = tops[item] ?: false,
                                onToggle = { checked ->
                                    setTops(tops + (item to checked))
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Bottom Wear Section
                        Text(
                            "Bottom Wear",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        bottoms.keys.forEach { item ->
                            ClothingItem(
                                name = item,
                                isSelected = bottoms[item] ?: false,
                                onToggle = { checked ->
                                    setBottoms(bottoms + (item to checked))
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ✅ Edit Complete 버튼 (bottomBar 대신 내부에 배치)
                Button(
                    onClick = {
                        if (memberId == null) {
                            Toast.makeText(
                                context,
                                "Cannot fetch the login info.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        // 각 카테고리별 최소 1개 이상 선택 검증
                        val selectedOuter = outerwear.count { it.value }
                        val selectedTops = tops.count { it.value }
                        val selectedBottoms = bottoms.count { it.value }

                        if (selectedOuter == 0) {
                            Toast.makeText(
                                context,
                                "Please select outer wear at least 1.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }
                        if (selectedTops == 0) {
                            Toast.makeText(
                                context,
                                "Please select top at least 1.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }
                        if (selectedBottoms == 0) {
                            Toast.makeText(
                                context,
                                "Please select bottom at least 1.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        // 서버로 보낼 리스트 생성
                        val rawClothes = mutableListOf<String>()
                        outerwear.forEach { (name, hasItem) ->
                            if (hasItem) rawClothes.add(name)
                        }
                        tops.forEach { (name, hasItem) ->
                            if (hasItem) rawClothes.add(name)
                        }
                        bottoms.forEach { (name, hasItem) ->
                            if (hasItem) rawClothes.add(name)
                        }

                        val selectedClothes = rawClothes.map { name ->
                            name.trim()
                                .replace(' ', '_')
                                .uppercase()
                        }

                        authViewModel.updateMember(
                            memberId = memberId!!,
                            password = null,
                            newPassword = null,
                            preference = null,
                            tendencies = null,
                            clothes = selectedClothes
                        )

                        // ✅ from argument에 따라 분기
                        if (from == "signup") {
                            // 회원가입 플로우: 메인 화면으로 이동 (회원가입 스택 비우기)
                            Toast.makeText(context, "Sign up completed.", Toast.LENGTH_SHORT).show()
                            navController.navigate("main") {
                                popUpTo("login") { inclusive = true }  // 로그인부터 회원가입까지 모두 제거
                            }
                        } else {
                            // 프로필 수정: 이전 화면으로 돌아가기
                            Toast.makeText(context, "Edit completed.", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B7FFF)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Edit Complete", fontSize = 18.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
