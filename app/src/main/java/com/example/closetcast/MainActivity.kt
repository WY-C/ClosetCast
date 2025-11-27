package com.example.closetcast

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.launch
import kotlin.random.Random

// ===================== 위치 관리 유틸리티 =====================
class LocationManager(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun requestLocationUpdates(onLocationReceived: (Double, Double) -> Unit) {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            try {
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (location != null) {
                    onLocationReceived(location.latitude, location.longitude)
                } else {
                    // GPS 기반 위치 없으면 네트워크 기반 위치 시도
                    val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (networkLocation != null) {
                        onLocationReceived(networkLocation.latitude, networkLocation.longitude)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class MainActivity : ComponentActivity() {

    // ✅ 새로운 방식: Activity Result Contract 사용
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        Log.d("MainActivity", "========== 권한 요청 결과 ==========")
        Log.d("MainActivity", "FINE_LOCATION: $fineLocationGranted")
        Log.d("MainActivity", "COARSE_LOCATION: $coarseLocationGranted")

        if (fineLocationGranted || coarseLocationGranted) {
            Log.d("MainActivity", "위치 권한 허용됨")
            // 권한 허용 시 처리 로직
        } else {
            Log.d("MainActivity", "위치 권한 거부됨")
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
        Log.d("MainActivity", "위치 권한 요청 시작")

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

        Log.d("MainActivity", "현재 권한 상태 - FINE: $hasFineLocation, COARSE: $hasCoarseLocation")

        // 권한이 없으면 요청
        if (!hasFineLocation || !hasCoarseLocation) {
            Log.d("MainActivity", "권한 요청 팝업 표시")
            requestPermissionLauncher.launch(permissions)
        } else {
            Log.d("MainActivity", "이미 권한이 있음")
        }
    }
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("signup"){
            SignUpScreen(navController = navController)
        }
        composable("main") {
            MainScreen(navController = navController)
        }
        composable(
            route = "styleandsensitivity?isSignUpProcess={isSignUpProcess}&name={name}&loginId={loginId}&password={password}",
            arguments = listOf(
                navArgument("isSignUpProcess") {
                    type = NavType.BoolType
                    defaultValue = false
                },
                navArgument("name") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("loginId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("password") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val isSignUpProcess = backStackEntry.arguments?.getBoolean("isSignUpProcess") ?: false
            val name = backStackEntry.arguments?.getString("name")
            val loginId = backStackEntry.arguments?.getString("loginId")
            val password = backStackEntry.arguments?.getString("password")

            StyleAndSensitivityScreen(
                navController = navController,
                authViewModel = authViewModel,
                isSignUpProcess = isSignUpProcess,
                signUpName = name,
                signUpLoginId = loginId,
                signUpPassword = password
            )
        }
        composable("clothes_setting") {
            ClothesSetting(navController = navController)
        }
        composable("change_password") {
            ChangePasswordScreen(navController = navController)
        }
        composable("withdraw") {
            WithdrawScreen(navController = navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "ClosetCast Login", fontSize = 28.sp)
            Spacer(modifier = Modifier.height(24.dp))

            // LoginId 입력
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

            // Password 입력
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

            // ✅ 에러 메시지 표시
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

            // ✅ 로그인 버튼 - AuthViewModel.login() 호출
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
                enabled = !isLoading && loginId.isNotEmpty() && password.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(text = "Login", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate("signup") }) {
                Text("Sign Up")
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
    val isLoggedIn by authViewModel.isLoggedIn


    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "ClosetCast Sign Up", fontSize = 28.sp)
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
                    textAlign = TextAlign.Center
                )
            }

            // ✅ 회원가입 버튼 - AuthViewModel.signUp() 호출
            Button(
                onClick = {
                    if (name.isNotBlank() &&
                        loginId.isNotBlank() &&
                        password.length >= 6 &&
                        password == passwordCheck) {

                        // ✅ 스타일-민감도 화면으로 전환
                        navController.navigate(
                            "styleandsensitivity?isSignUpProcess=true" +
                                    "&name=${Uri.encode(name)}" +
                                    "&loginId=${Uri.encode(loginId)}" +
                                    "&password=${Uri.encode(password)}"
                        )
                    } else {
                        Toast.makeText(
                            context,
                            "입력 정보를 확인해주세요. 비밀번호는 6자 이상이어야 합니다.",
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
                        passwordCheck.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(text = "Sign Up", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text("Already have an account? Login")
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
        Text(text = currentWeather.location, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "${currentWeather.temperature}°", fontSize = 64.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "1° lower than yesterday", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Min: ${currentWeather.minTemp}° Max: ${currentWeather.maxTemp}°", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        ApparentTemperatureCard(currentWeather.apparentTemperature)
    }
}

@Composable
fun HourlyForecastCard(hourlyForecasts: List<HourlyForecast>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Hourly Forecast", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            hourlyForecasts.forEach { forecast ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = forecast.time, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Icon(forecast.weatherIcon, contentDescription = null, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${forecast.temperature}°", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun DailyForecastCard(dailyForecasts: List<DailyForecast>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "3-Day Forecast", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        dailyForecasts.forEach { forecast ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = forecast.day, fontSize = 16.sp)
                Icon(forecast.weatherIcon, contentDescription = null, modifier = Modifier.size(32.dp))
                Text(text = "Min: ${forecast.minTemp}°", fontSize = 16.sp)
                Text(text = "Max: ${forecast.maxTemp}°", fontSize = 16.sp)
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

data class ClothingRecommendation(val outer: String, val top: String, val bottom: String)

@Composable
fun ClothingRecommendationCard(recommendation: ClothingRecommendation) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Outer Wear Card
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Outer", style = MaterialTheme.typography.titleMedium)
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (recommendation.outer.lowercase() != "none") {
                        Image(
                            painter = painterResource(getImageResourceForClothingName(recommendation.outer)),
                            contentDescription = recommendation.outer,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = recommendation.outer, style = MaterialTheme.typography.bodyLarge)
            }
            // Top Wear Card
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Top", style = MaterialTheme.typography.titleMedium)
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (recommendation.top.lowercase() != "none") {
                        Image(
                            painter = painterResource(getImageResourceForClothingName(recommendation.top)),
                            contentDescription = recommendation.top,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = recommendation.top, style = MaterialTheme.typography.bodyLarge)
            }

            // Bottom Wear Card (여기 추가)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Bottom", style = MaterialTheme.typography.titleMedium)
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (recommendation.bottom.lowercase() != "none") {
                        Image(
                            painter = painterResource(getImageResourceForClothingName(recommendation.bottom)),
                            contentDescription = recommendation.bottom,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = recommendation.bottom, style = MaterialTheme.typography.bodyLarge)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = { /* TODO */ }) {
            Text("View Other Recommendations")
        }
    }
}

fun getImageResourceForClothingName(name: String): Int {
    return when (name.trim().lowercase()) {
        "puffer jacket" -> R.drawable.puffer_coat
        "coat" -> R.drawable.trench_coat
        "fleece" -> R.drawable.fleece
        "sweater" -> R.drawable.sweater
        "hoodie" -> R.drawable.hoodie
        "short sleeve" -> R.drawable.short_sleeves
        "long sleeve" -> R.drawable.long_sleeves
        "jeans" -> R.drawable.jeans
        "cotton pants" -> R.drawable.trouser
        "shorts" -> R.drawable.shorts
        else -> R.drawable.default_clothing // default image
    }
}

fun getRecommendationForTemperature(temp: Double): ClothingRecommendation {
    val topList: List<String>
    val outerList: List<String>
    val bottomList: List<String>

    when {
        temp >= 28.0 -> {
            outerList = listOf("None")
            topList = listOf("Short sleeve", "Sleeveless")
            bottomList = listOf("Shorts")
        }
        temp in 23.0..27.0 -> {
            outerList = listOf("None")
            topList = listOf("Short sleeve", "Shirt")
            bottomList = listOf("Shorts", "Cotton pants")
        }
        temp in 20.0..22.0 -> {
            outerList = listOf("Spring/Fall Jacket", "Blazer", "Cardigan", "Denim")
            topList = listOf("Shirt", "Long sleeve", "Sweater")
            bottomList = listOf("Jeans", "Cotton pants")
        }
        temp in 17.0..19.0 -> {
            outerList = listOf("Sweatshirt", "Hoodie")
            topList = listOf("Shirt", "Sweater")
            bottomList = listOf("Jeans", "Cotton pants")
        }
        temp in 12.0..16.0 -> {
            outerList = listOf("Leather Jacket", "Blouson", "Stadium Jacket", "Light Jacket", "Windbreaker")
            topList = listOf("Sweater", "Shirt")
            bottomList = listOf("Jeans", "Cotton pants")
        }
        temp in 9.0..11.0 -> {
            outerList = listOf("Coat", "Trench coat", "Fleece")
            topList = listOf("Sweater")
            bottomList = listOf("Jeans", "Cotton pants")
        }
        temp in 5.0..8.0 -> {
            outerList = listOf("Coat", "Puffer Jacket", "Fleece")
            topList = listOf("Sweatshirt", "Sweater")
            bottomList = listOf("Jeans", "Cotton pants")
        }
        else -> {
            outerList = listOf("Puffer Jacket (Short, Long)", "Heavy outer")
            topList = listOf("Sweater", "Hoodie")
            bottomList = listOf("Jeans", "Cotton pants")
        }
    }

    val outer = outerList[Random.nextInt(outerList.size)]
    val top = topList[Random.nextInt(topList.size)]
    val bottom = bottomList[Random.nextInt(bottomList.size)]

    return ClothingRecommendation(outer, top, bottom)
}


@Composable
fun ClothingRecommendationScreen(weatherData: WeatherData) {
    val recommendation = getRecommendationForTemperature(weatherData.current.temperature)
    ClothingRecommendationCard(recommendation)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(navController: NavController) {
    var oldPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Password") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                label = { Text("Current Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    // Add password change logic here
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change Password")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(navController: NavController) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Withdrawal") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Are you sure you want to withdraw?", fontSize = 20.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    Toast.makeText(context, "Account withdrawal completed.", Toast.LENGTH_SHORT).show()
                    navController.navigate("login") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Account Withdrawal")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {

    val bottomBarNavController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val authViewModel: AuthViewModel = viewModel()

    val items = listOf(
        BottomNavItem.Weather,
        BottomNavItem.Clothing
    )

    val weatherViewModel = viewModel<WeatherViewModel>()
    val weatherData by weatherViewModel.weatherData
    val isLoading by weatherViewModel.isLoading
    val errorMessage by weatherViewModel.error

    // 앱 시작 시 위치 정보 요청
    LaunchedEffect(Unit) {
        Log.d("MainScreen", "LaunchedEffect 시작")
        try {
            val helper = LocationHelper(context)
            helper.requestCurrentLocation { lat, lng ->
                Log.d("MainScreen", "위치 정보 받음: lat=$lat, lng=$lng")
                weatherViewModel.fetchWeather(lat, lng)
            }
        } catch (e: Exception) {
            Log.e("MainScreen", "위치 정보 요청 실패", e)
        }
    }

    val drawerItems = listOf(
        "Edit Password" to "changepassword",
        "Edit My Closet" to "clothessetting",
        "Edit Personal Information" to "styleandsensitivity?isSignUpProcess=false"
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))

                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.first) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(item.second)
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // ✅ 로그아웃 버튼 - AuthViewModel.logout() 호출
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
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

                NavigationDrawerItem(
                    label = { Text("Account Withdrawal") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("withdraw")
                    }
                )
            }
        }
    ) {
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
                        Text(title)
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
                                contentDescription = "Menu"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
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
            }
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
                        ClothingRecommendationScreen(weatherData!!)
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("날씨 정보를 불러오는 중...")
                        }
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
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = style, style = MaterialTheme.typography.bodyLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StyleAndSensitivityScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    isSignUpProcess: Boolean = false,
    signUpName: String? = null,
    signUpLoginId: String? = null,
    signUpPassword: String? = null
) {
    var heatSensitive by rememberSaveable { mutableStateOf(false) }
    var coldSensitive by rememberSaveable { mutableStateOf(false) }

    val styles = listOf("Minimal", "Casual", "Street", "Classic", "Dandy", "Retro")
    val selectedStyles = rememberSaveable { mutableStateListOf<String>() }

    val context = LocalContext.current

    val isLoading by authViewModel.isLoading
    val error by authViewModel.error

    // ✅ 회원가입 완료 추적 (버튼 클릭 여부)
    var signUpRequested by rememberSaveable { mutableStateOf(false) }

    // ✅ 회원가입 성공 시 로그인 화면으로 이동
    // isLoading이 false가 되고, error가 null이고, signUpRequested가 true면 성공!
    LaunchedEffect(isLoading, error) {
        if (signUpRequested && !isLoading && error == null && isSignUpProcess) {
            Toast.makeText(context, "회원가입 성공! 로그인해주세요.", Toast.LENGTH_SHORT).show()
            navController.navigate("login") {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            if (!isSignUpProcess) {
                TopAppBar(
                    title = { Text("Edit Personal Information") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                        }
                    }
                )
            }
        },
        bottomBar = {
            Button(
                onClick = {
                    if (isSignUpProcess && signUpName != null && signUpLoginId != null && signUpPassword != null) {
                        val preference = selectedStyles.map { it.uppercase() }.ifEmpty { listOf("MINIMAL") }
                        val tendencies = buildList {
                            if (heatSensitive) add("HOT")
                            if (coldSensitive) add("COLD")
                            if (isEmpty()) add("NORMAL")
                        }

                        // ✅ 회원가입 요청 플래그 설정
                        signUpRequested = true

                        authViewModel.signUp(
                            name = signUpName,
                            loginId = signUpLoginId,
                            password = signUpPassword,
                            preference = preference,
                            tendencies = tendencies
                        )
                    } else {
                        Toast.makeText(context, "Edit completed.", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isSignUpProcess) "Done" else "Edit Complete")
                }
            }
        }
    ) { innerPadding ->
        // ... 나머지 UI는 그대로 ...
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

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

            Text("Choose your style preference", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                maxItemsInEachRow = 2
            ) {
                styles.forEach { style ->
                    StyleSelectionItem(
                        style = style,
                        isSelected = selectedStyles.contains(style),
                        onSelect = {
                            if (selectedStyles.contains(style)) {
                                selectedStyles.remove(style)
                            } else {
                                selectedStyles.add(style)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Your sensitivity tendency",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 8.dp).widthIn(max = 400.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Sensitive to Heat", fontSize = 18.sp)
                    Switch(
                        checked = heatSensitive,
                        onCheckedChange = { heatSensitive = it }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Sensitive to Cold", fontSize = 18.sp)
                    Switch(
                        checked = coldSensitive,
                        onCheckedChange = { coldSensitive = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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
        Checkbox(checked = isSelected, onCheckedChange = onToggle)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = name, fontSize = 16.sp)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothesSetting(navController: NavController) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit My Closet") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    Toast.makeText(context, "Edit completed.", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("Edit Complete")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("What clothes do you have?", fontSize = 24.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(24.dp))

            // Outer Wear Section
            Text("Outer Wear", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            val (outerwear, setOuterwear) = remember { mutableStateOf(mapOf("Puffer Jacket" to false, "coat" to false, "Fleece" to false, "jacket" to false, "windbreaker" to false)) }
            outerwear.keys.forEach { item ->
                ClothingItem(name = item, isSelected = outerwear[item] ?: false, onToggle = {
                    setOuterwear(outerwear + (item to it))
                })
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Top Wear Section
            Text("Top Wear", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            val (tops, setTops) = remember { mutableStateOf(mapOf("sweater" to false, "hoodie" to false, "shirt" to false, "long sleeve" to false, "short sleeve" to false)) }
            tops.keys.forEach { item ->
                ClothingItem(name = item, isSelected = tops[item] ?: false, onToggle = {
                    setTops(tops + (item to it))
                })
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Wear Section
            Text("Bottom Wear", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            val (bottoms, setBottoms) = remember { mutableStateOf(mapOf("jeans" to false, "cotton pants" to false, "shorts" to false)) }
            bottoms.keys.forEach { item ->
                ClothingItem(name = item, isSelected = bottoms[item] ?: false, onToggle = {
                    setBottoms(bottoms + (item to it))
                })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    ClosetCastTheme {
        LoginScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    ClosetCastTheme {
        SignUpScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun StyleAndSensitivityScreenPreview() {
    ClosetCastTheme {
        StyleAndSensitivityScreen(
            navController = rememberNavController(),
            authViewModel = viewModel(),
            isSignUpProcess = true,
            signUpName = "preview",
            signUpLoginId = "preview",
            signUpPassword = "preview"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ClosetCastTheme {
        MainScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun ClothesSettingPreview() {
    ClosetCastTheme {
        ClothesSetting(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    // #TODO : 왜 Preview에서도 샘플 데이터가 쓰이는진 모르겠지만 여기도 수정 필요
    val sampleWeatherData = WeatherData(
        current = CurrentWeather("Dongjak-gu, Sangdo 1-dong", 12.0, 10.0,"Clear", 11.0, 17.0),
        hourly = listOf(
            HourlyForecast("2 PM", 12.0, Icons.Default.WbSunny),
            HourlyForecast("3 PM", 12.0, Icons.Default.WbSunny),
            HourlyForecast("4 PM", 11.0, Icons.Default.WbSunny),
            HourlyForecast("5 PM", 11.0, Icons.Default.WbSunny),
            HourlyForecast("6 PM", 11.0, Icons.Default.WbSunny),
            HourlyForecast("7 PM", 11.0, Icons.Default.WbSunny)
        ),
        daily = listOf(
            DailyForecast("Today", 11.0, 17.0, Icons.Default.WbSunny),
            DailyForecast("Tomorrow", 10.0, 18.0, Icons.Default.WbSunny),
            DailyForecast("Day After Tomorrow", 9.0, 16.0, Icons.Default.WbSunny)
        )
    )
    ClosetCastTheme {
        WeatherScreen(sampleWeatherData)
    }
}

@Preview(showBackground = true)
@Composable
fun ChangePasswordScreenPreview() {
    ClosetCastTheme {
        ChangePasswordScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun ClothingRecommendationScreenPreview() {
    val sampleWeatherData = WeatherData(
        current = CurrentWeather("Dongjak-gu, Sangdo 1-dong", 12.0, 10.0,"Clear", 11.0, 17.0),
        hourly = listOf(),
        daily = listOf()
    )
    ClosetCastTheme {
        ClothingRecommendationScreen(sampleWeatherData)
    }
}

@Preview(showBackground = true)
@Composable
fun WithdrawScreenPreview() {
    ClosetCastTheme {
        WithdrawScreen(navController = rememberNavController())
    }
}
