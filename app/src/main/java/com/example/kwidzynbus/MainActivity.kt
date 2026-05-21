package com.example.kwidzynbus

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONObject

// =====================================================
// MAIN ACTIVITY
// =====================================================

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BusScreen()
                }
            }
        }
    }
}

// =====================================================
// DATA CLASS
// =====================================================

data class BusSchedule(
    val title: String,
    val stops: List<BusStop>
)

data class BusStop(
    val stop: String,
    val times: List<String>
)

// =====================================================
// JSON LOADER
// =====================================================

fun loadSchedule(
    context: Context,
    fileName: String,
    direction: String
): BusSchedule {

    var title = ""

    val result = mutableListOf<BusStop>()

    try {

        val inputStream = context.assets.open(fileName)

        val jsonString =
            inputStream.bufferedReader().use { it.readText() }

        val jsonObject = JSONObject(jsonString)

        if (jsonObject.has(direction)) {

            val directionObject =
                jsonObject.getJSONObject(direction)


            // =========================
            // TITLE
            // =========================

            title =
                directionObject.getString("title")

            // =========================
            // STOPS
            // =========================

            val directionArray =
                directionObject.getJSONArray("stops")

            for (i in 0 until directionArray.length()) {

                val item =
                    directionArray.getJSONObject(i)

                val stop =
                    item.getString("stop")

                val timesJson =
                    item.getJSONArray("times")

                val timesList =
                    mutableListOf<String>()

                for (j in 0 until timesJson.length()) {

                    timesList.add(
                        timesJson.getString(j)
                    )
                }

                result.add(
                    BusStop(
                        stop = stop,
                        times = timesList
                    )
                )
            }
        }

    } catch (e: Exception) {

        e.printStackTrace()
    }

    return BusSchedule(
        title = title,
        stops = result
    )
}
// =====================================================
// ZOOMABLE IMAGE
// =====================================================

@Composable
fun ZoomableImage(
    imageRes: Int,
    contentDescription: String
) {

    var scale by remember { mutableFloatStateOf(1f) }

    var offsetX by remember { mutableFloatStateOf(0f) }

    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {

                detectTransformGestures { _, pan, zoom, _ ->

                    scale *= zoom

                    scale = scale.coerceIn(1f, 5f)

                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
    ) {

        Image(
            painter = painterResource(id = imageRes),
            contentDescription = contentDescription,

            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                ),

            contentScale = ContentScale.Fit
        )
    }
}

// =====================================================
// MAIN SCREEN
// =====================================================

@Composable
fun BusScreen() {

    val context = LocalContext.current

    // =====================================================
    // NAVIGATION
    // =====================================================

    var currentScreen by remember {
        mutableStateOf("home")
    }

    var selectedCategory by remember {
        mutableStateOf<String?>(null)
    }

    var selectedLine by remember {
        mutableStateOf<String?>(null)
    }

    var selectedDirection by remember {
        mutableStateOf("direction1")
    }

// =====================================================
// BACK HANDLER
// =====================================================

    BackHandler {

        if (selectedLine != null) {

            selectedLine = null
            return@BackHandler
        }

        if (selectedCategory != null) {

            selectedCategory = null
            return@BackHandler
        }

        if (currentScreen != "home") {

            currentScreen = "home"
            return@BackHandler
        }
    }

    // =====================================================
    // DATA
    // =====================================================

    val categories = mapOf(

        "Linia 1" to listOf(
            "1P" to "schedule_1p.json"
        ),

        "Linia 2" to listOf(
            "2P" to "schedule_2p.json",
            "2S" to "schedule_2s.json"
        ),

        "Linia 3" to listOf(
            "3P" to "schedule_3p.json",
            "3S" to "schedule_3s.json"
        ),

        "Linia 4" to listOf(
            "4P" to "schedule_4p.json"
        ),

        "Linia 5" to listOf(
            "5N" to "schedule_5n.json",
            "5P" to "schedule_5p.json",
            "5S" to "schedule_5s.json"
        ),

        "Linia 6" to listOf(
            "6A" to "schedule_6a_p.json",
            "6B" to "schedule_6b_p.json"
        ),

        "Linia 7" to listOf(
            "7N" to "schedule_7n.json",
            "7P" to "schedule_7p.json",
            "7S" to "schedule_7s.json"
        ),

        "Linia 8" to listOf(
            "8P" to "schedule_8p.json"
        ),

        "INNE" to listOf(
            "INNE" to "schedule_inne_sp.json"
        )
    )

    val selectedFile =
        categories.values
            .flatten()
            .find { it.first == selectedLine }
            ?.second
            ?: "schedule_1p.json"

    val schedule = loadSchedule(
        context = context,
        fileName = selectedFile,
        direction = selectedDirection
    )

    // =====================================================
    // BACKGROUND
    // =====================================================

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Image(
            painter = painterResource(R.drawable.bus_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(alpha = 0.45f)
                )
        )

        // =====================================================
        // HOME SCREEN
        // =====================================================

        if (currentScreen == "home") {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),

                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "🚌 Kwidzyn Bus",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(48.dp))

                HomeButton(
                    text = "Rozkład jazdy",
                    color = Color(0xCE16CE62)
                ) {
                    currentScreen = "schedule"
                }

                Spacer(modifier = Modifier.height(20.dp))

                HomeButton(
                    text = "Przebieg tras",
                    color = Color(0xDC2097D9)
                ) {
                    currentScreen = "map"
                }

                Spacer(modifier = Modifier.height(20.dp))

                HomeButton(
                    text = "Ulgi",
                    color = Color(0xAEDC9121)
                ) {
                    currentScreen = "prices"
                }
            }
        }


        // =====================================================
        // MAP SCREEN
        // =====================================================

        if (currentScreen == "map") {

            Column {

                TopBar(title = "Przebieg tras") {
                    currentScreen = "home"
                }

                ZoomableImage(
                    imageRes = R.drawable.mapa,
                    contentDescription = "Mapa"
                )
            }
        }

        // =====================================================
        // PRICES SCREEN
        // =====================================================

        if (currentScreen == "prices") {

            Column {

                TopBar(title = "Ulgi") {
                    currentScreen = "home"
                }

                ZoomableImage(
                    imageRes = R.drawable.ulgi,
                    contentDescription = "Ulgi"
                )
            }
        }

        // =====================================================
        // SCHEDULE SCREEN
        // =====================================================

        if (currentScreen == "schedule") {

            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                TopBar(title = "Rozkład jazdy") {
                    currentScreen = "home"
                }

        // =====================================================
        // CATEGORY LIST
        // =====================================================

                if (selectedCategory == null) {

                    LazyColumn(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        items(categories.keys.toList()) { category ->

                            Button(
                                onClick = {
                                    selectedCategory = category
                                },

                                modifier = Modifier.fillMaxWidth(),

                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xC14CAF50)
                                ),

                                shape = RoundedCornerShape(20.dp)
                            ) {

                                Text(
                                    text = category,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // =====================================================
                // LINE LIST
                // =====================================================

                else if (selectedLine == null) {

                    Column {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            IconButton(
                                onClick = {
                                    selectedCategory = null
                                }
                            ) {

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }

                            Text(
                                text = selectedCategory ?: "",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        LazyColumn(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            items(categories[selectedCategory] ?: emptyList()) { line ->

                                Button(
                                    onClick = {
                                        selectedLine = line.first
                                    },

                                    modifier = Modifier.fillMaxWidth(),

                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xD703A9F4)
                                    ),

                                    shape = RoundedCornerShape(20.dp)
                                ) {

                                    Text(
                                        text = line.first,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // =====================================================
                // TIMETABLE
                // =====================================================

                else {

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            IconButton(
                                onClick = {
                                    selectedLine = null
                                }
                            ) {

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }

                            Text(
                                text = selectedLine ?: "",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        // =====================================================
                        // DIRECTIONS
                        // =====================================================

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Button(
                                onClick = {
                                    selectedDirection = "direction1"
                                },

                                modifier = Modifier.weight(1f),

                                colors = ButtonDefaults.buttonColors(
                                    containerColor =
                                        if (selectedDirection == "direction1")
                                            Color(0xFF00C853)
                                        else
                                            Color(0xFF455A64)
                                )
                            ) {
                                Text(
                                    text = "▶ Kierunek 1",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    selectedDirection = "direction2"
                                },

                                modifier = Modifier.weight(1f),

                                colors = ButtonDefaults.buttonColors(
                                    containerColor =
                                        if (selectedDirection == "direction2")
                                            Color(0xFF00C853)
                                        else
                                            Color(0xFF455A64)
                                )
                            ) {
                                Text(
                                    text = "◀ Kierunek 2",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // =====================================================
                        // STOPS
                        // =====================================================

                        if (schedule.title.isNotEmpty()) {

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),

                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xCC000000)
                                ),

                                shape = RoundedCornerShape(20.dp)
                            ) {

                                Text(
                                    text = schedule.title,
                                    color = Color(0xFFFFF59D),

                                    modifier = Modifier.padding(16.dp),

                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),

                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            items(schedule.stops) { busStop ->

                                var expanded by remember {
                                    mutableStateOf(false)
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expanded = !expanded
                                        },

                                    colors = CardDefaults.cardColors(
                                        containerColor =
                                            Color(0xFF263238)
                                    ),

                                    shape = RoundedCornerShape(20.dp)
                                ) {

                                    Column(
                                        modifier = Modifier.padding(20.dp)
                                    ) {

                                        Text(
                                            text = busStop.stop,
                                            color = Color.White,
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        if (expanded) {

                                            Spacer(
                                                modifier = Modifier.height(10.dp)
                                            )

                                            Text(
                                                text = busStop.times
                                                    .chunked(4)
                                                    .joinToString("\n") {
                                                        it.joinToString("   ")
                                                    },

                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =====================================================
// HOME BUTTON
// =====================================================

@Composable
fun HomeButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,

        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),

        colors = ButtonDefaults.buttonColors(
            containerColor = color
        ),

        shape = RoundedCornerShape(24.dp)
    ) {

        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

// =====================================================
// TOP BAR
// =====================================================

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TopBar(
    title: String,
    onBack: () -> Unit
) {

    TopAppBar(

        title = {
            Text(
                text = title,
                color = Color.White
            )
        },

        navigationIcon = {

            IconButton(
                onClick = {
                    onBack()
                }
            ) {

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },

        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF102027)
        )
    )
}
