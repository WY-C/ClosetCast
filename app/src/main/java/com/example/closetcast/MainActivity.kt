package com.example.closetcast

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClosetCastTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("sign_up"){
            SignUpScreen(navController = navController)
        }
        composable("main") {
            MainScreen(navController = navController)
        }
        composable(
            "style_and_sensitivity?isSignUpProcess={isSignUpProcess}",
            arguments = listOf(navArgument("isSignUpProcess") {
                type = NavType.BoolType
                defaultValue = false
            })
        ) { backStackEntry ->
            StyleAndSensitivityScreen(
                navController = navController,
                isSignUpProcess = backStackEntry.arguments?.getBoolean("isSignUpProcess") ?: false
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
fun LoginScreen(navController: NavController) {
    var id by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

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

            OutlinedTextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("ID") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (id.isNotBlank() && password.length >= 6) {
                        Toast.makeText(context, "Login Success!", Toast.LENGTH_SHORT).show()
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        Toast.makeText(context, "Please Check your ID or Password.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(text = "Login", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { navController.navigate("sign_up") }) {
                Text("Sign Up")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    var id by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordCheck by rememberSaveable { mutableStateOf("") }
    var nickname by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current

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

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Nickname") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("ID") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = passwordCheck,
                onValueChange = { passwordCheck = it },
                label = { Text("Password Check") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (nickname.isNotBlank() && id.isNotBlank() && password.length >= 6 && password == passwordCheck) {
                        navController.navigate("style_and_sensitivity?isSignUpProcess=true")
                    } else {
                        Toast.makeText(context, "Please Check your ID or Password.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(text = "Next", fontSize = 18.sp)
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
    val temperature: Int,
    val weatherCondition: String,
    val minTemp: Int,
    val maxTemp: Int
)

data class HourlyForecast(
    val time: String,
    val temperature: Int,
    val weatherIcon: ImageVector
)

data class DailyForecast(
    val day: String,
    val minTemp: Int,
    val maxTemp: Int,
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
        Text(text = "어제보다 1° 낮아요", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "최저: ${currentWeather.minTemp}° 최고: ${currentWeather.maxTemp}°", fontSize = 16.sp)
    }
}

@Composable
fun HourlyForecastCard(hourlyForecasts: List<HourlyForecast>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "시간별 예보", fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
        Text(text = "3일 예보", fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                Text(text = "최저: ${forecast.minTemp}°", fontSize = 16.sp)
                Text(text = "최고: ${forecast.maxTemp}°", fontSize = 16.sp)
            }
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
            // 아우터 Card
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
            // 상의 Card
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
                            painter = painterResource(getImageResourceForClothingName(recommendation.outer)),
                            contentDescription = recommendation.outer,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = recommendation.top, style = MaterialTheme.typography.bodyLarge)
            }

            // 하의 Card (여기 추가)
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
                            painter = painterResource(getImageResourceForClothingName(recommendation.outer)),
                            contentDescription = recommendation.outer,
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
            Text("다른 추천 보기")
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
        else -> R.drawable.default_clothing // 기본 이미지
    }
}

fun getRecommendationForTemperature(temp: Int): ClothingRecommendation {
    val topList: List<String>
    val outerList: List<String>
    val bottomList: List<String>

    when {
        temp >= 28 -> {
            outerList = listOf("None")
            topList = listOf("Short sleeve", "Sleeveless")
            bottomList = listOf("Shorts")
        }
        temp in 23..27 -> {
            outerList = listOf("None")
            topList = listOf("Short sleeve", "Shirt")
            bottomList = listOf("Shorts", "Cotton pants")
        }
        temp in 20..22 -> {
            outerList = listOf("Spring/Fall Jacket", "Blazer", "Cardigan", "Denim")
            topList = listOf("Shirt", "Long sleeve", "Sweater")
            bottomList = listOf("Jeans", "Cotton pants")
        }
        temp in 17..19 -> {
            outerList = listOf("Sweatshirt", "Hoodie")
            topList = listOf("Shirt", "Sweater")
            bottomList = listOf("Jeans", "Cotton pants")
        }
        temp in 12..16 -> {
            outerList = listOf("Leather Jacket", "Blouson", "Stadium Jacket", "Light Jacket", "Windbreaker")
            topList = listOf("Sweater", "Shirt")
            bottomList = listOf("Jeans", "Cotton pants")
        }
        temp in 9..11 -> {
            outerList = listOf("Coat", "Trench coat", "Fleece")
            topList = listOf("Sweater")
            bottomList = listOf("Jeans", "Cotton pants")
        }
        temp in 5..8 -> {
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
                title = { Text("Password 수정") },
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
                title = { Text("회원탈퇴") },
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
            Text("정말로 탈퇴하시겠습니까?", fontSize = 20.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    Toast.makeText(context, "회원탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    navController.navigate("login") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("회원탈퇴")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val bottomBarNavController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val items = listOf(
        BottomNavItem.Weather,
        BottomNavItem.Clothing
    )

    // TODO: 샘플 데이터가 아닌 실시간 데이터를 받아와야 함.
    val sampleWeatherData = WeatherData(
        current = CurrentWeather("동작구 상도1동", 12, "맑음", 11, 17),
        hourly = listOf(
            HourlyForecast("오후 2시", 12, Icons.Default.WbSunny),
            HourlyForecast("오후 3시", 12, Icons.Default.WbSunny),
            HourlyForecast("오후 4시", 11, Icons.Default.WbSunny),
            HourlyForecast("오후 5시", 11, Icons.Default.WbSunny),
            HourlyForecast("오후 6시", 11, Icons.Default.WbSunny),
            HourlyForecast("오후 7시", 11, Icons.Default.WbSunny)
        ),
        daily = listOf(
            DailyForecast("오늘", 11, 17, Icons.Default.WbSunny),
            DailyForecast("내일", 10, 18, Icons.Default.WbSunny),
            DailyForecast("모레", 9, 16, Icons.Default.WbSunny)
        )
    )


    val drawerItems = listOf(
        "Password 수정" to "change_password",
        "나의 옷장 수정" to "clothes_setting",
        "개인정보 수정" to "style_and_sensitivity?isSignUpProcess=false"
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
                NavigationDrawerItem(
                    label = { Text("로그아웃") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("login") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("회원탈퇴") },
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
                composable(BottomNavItem.Weather.route) { WeatherScreen(sampleWeatherData) }
                composable(BottomNavItem.Clothing.route) { ClothingRecommendationScreen(sampleWeatherData) }
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
fun StyleAndSensitivityScreen(navController: NavController, isSignUpProcess: Boolean) {
    var heatSensitive by rememberSaveable { mutableStateOf(false) }
    var coldSensitive by rememberSaveable { mutableStateOf(false) }
    val styles = listOf("Minimal", "Casual", "Street", "Classic", "Dandy", "Retro")
    val selectedStyles = rememberSaveable { mutableStateListOf<String>() }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            if (!isSignUpProcess) {
                TopAppBar(
                    title = { Text("개인정보 수정") },
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
                    if (isSignUpProcess) {
                        Toast.makeText(context, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        navController.navigate("login") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                        }
                    } else {
                        Toast.makeText(context, "수정이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(if (isSignUpProcess) "Done" else "수정 완료")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            // Style Preference Section
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

            // Sensitivity Section
            Text("Your sensitivity tendency", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
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
                title = { Text("나의 옷장 수정") },
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
                    Toast.makeText(context, "수정이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("수정 완료")
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

            // 아우터 Section
            Text("아우터", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            val (outerwear, setOuterwear) = remember { mutableStateOf(mapOf("Puffer Jacket" to false, "coat" to false, "Fleece" to false, "jacket" to false, "windbreaker" to false)) }
            outerwear.keys.forEach { item ->
                ClothingItem(name = item, isSelected = outerwear[item] ?: false, onToggle = {
                    setOuterwear(outerwear + (item to it))
                })
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 상의 Section
            Text("상의", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            val (tops, setTops) = remember { mutableStateOf(mapOf("sweater" to false, "hoodie" to false, "shirt" to false, "long sleeve" to false, "short sleeve" to false)) }
            tops.keys.forEach { item ->
                ClothingItem(name = item, isSelected = tops[item] ?: false, onToggle = {
                    setTops(tops + (item to it))
                })
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 하의 Section
            Text("하의", style = MaterialTheme.typography.headlineSmall)
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
        StyleAndSensitivityScreen(navController = rememberNavController(), isSignUpProcess = true)
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
        current = CurrentWeather("동작구 상도1동", 12, "맑음", 11, 17),
        hourly = listOf(
            HourlyForecast("오후 2시", 12, Icons.Default.WbSunny),
            HourlyForecast("오후 3시", 12, Icons.Default.WbSunny),
            HourlyForecast("오후 4시", 11, Icons.Default.WbSunny),
            HourlyForecast("오후 5시", 11, Icons.Default.WbSunny),
            HourlyForecast("오후 6시", 11, Icons.Default.WbSunny),
            HourlyForecast("오후 7시", 11, Icons.Default.WbSunny)
        ),
        daily = listOf(
            DailyForecast("오늘", 11, 17, Icons.Default.WbSunny),
            DailyForecast("내일", 10, 18, Icons.Default.WbSunny),
            DailyForecast("모레", 9, 16, Icons.Default.WbSunny)
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
        current = CurrentWeather("동작구 상도1동", 12, "맑음", 11, 17),
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
