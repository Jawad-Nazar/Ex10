package com.example.exercise_10

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

// DataStore
val Context.dataStore by preferencesDataStore(name = "user_prefs")

// Preference Keys
val FONT_SIZE_KEY = intPreferencesKey("font_size")
val FONT_STYLE_KEY = stringPreferencesKey("font_style") // "Default", "Serif", "Monospace"
val FONT_WEIGHT_KEY = stringPreferencesKey("font_weight") // "Normal", "Bold"
val FONT_ITALIC_KEY = stringPreferencesKey("font_italic") // "false", "true"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SensorApp()
        }
    }
}

@Composable
fun SensorApp() {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(SensorManager::class.java) }
    val coroutineScope = rememberCoroutineScope()

    // Sensor data states
    var accelerometerData by remember { mutableStateOf("Accelerometer: N/A") }
    var gyroscopeData by remember { mutableStateOf("Gyroscope: N/A") }
    var magnetometerData by remember { mutableStateOf("Magnetometer: N/A") }
    var temperatureData by remember { mutableStateOf("Temperature: N/A") }
    var lightData by remember { mutableStateOf("Light (Lux): N/A") }

    // Font preferences flows
    val fontSizeFlow = context.dataStore.data.map { it[FONT_SIZE_KEY] ?: 16 }
    val fontSize by produceState(initialValue = 16, key1 = fontSizeFlow) { value = fontSizeFlow.first() }

    val fontStyleFlow = context.dataStore.data.map { it[FONT_STYLE_KEY] ?: "Default" }
    var fontStyle by remember { mutableStateOf("Default") }

    val fontWeightFlow = context.dataStore.data.map { it[FONT_WEIGHT_KEY] ?: "Normal" }
    var fontWeightStr by remember { mutableStateOf("Normal") }

    val fontItalicFlow = context.dataStore.data.map { it[FONT_ITALIC_KEY] ?: "false" }
    var isItalic by remember { mutableStateOf(false) }

    // Update font style, weight, italic from DataStore
    LaunchedEffect(fontStyleFlow, fontWeightFlow, fontItalicFlow) {
        fontStyle = fontStyleFlow.first()
        fontWeightStr = fontWeightFlow.first()
        isItalic = fontItalicFlow.first() == "true"
    }

    // Compute FontFamily, FontWeight, FontStyle
    val fontFamily = when (fontStyle) {
        "Serif" -> FontFamily.Serif
        "Monospace" -> FontFamily.Monospace
        else -> FontFamily.Default
    }

    val fontWeight = if (fontWeightStr == "Bold") FontWeight.Bold else FontWeight.Normal
    val fontStyleItalic = if (isItalic) FontStyle.Italic else FontStyle.Normal

    // Sensor listener
    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    when (it.sensor.type) {
                        Sensor.TYPE_ACCELEROMETER -> {
                            val x = String.format("%.2f", it.values[0])
                            val y = String.format("%.2f", it.values[1])
                            val z = String.format("%.2f", it.values[2])
                            accelerometerData = "Accelerometer: x=$x, y=$y, z=$z"
                        }
                        Sensor.TYPE_GYROSCOPE -> {
                            val x = String.format("%.2f", it.values[0])
                            val y = String.format("%.2f", it.values[1])
                            val z = String.format("%.2f", it.values[2])
                            gyroscopeData = "Gyroscope: x=$x, y=$y, z=$z"
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            val x = String.format("%.2f", it.values[0])
                            val y = String.format("%.2f", it.values[1])
                            val z = String.format("%.2f", it.values[2])
                            magnetometerData = "Magnetometer: x=$x, y=$y, z=$z µT"
                        }
                        Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                            val temp = String.format("%.1f", it.values[0])
                            temperatureData = "Temperature: $temp °C"
                        }
                        Sensor.TYPE_LIGHT -> {
                            val lux = String.format("%.1f", it.values[0])
                            lightData = "Light: $lux lux"
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    // Register all sensors
    DisposableEffect(sensorManager) {
        val sensors = listOf(
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_AMBIENT_TEMPERATURE,
            Sensor.TYPE_LIGHT
        )

        sensors.forEach { type ->
            sensorManager.getDefaultSensor(type)?.let {
                sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI)
            }
        }

        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Sensor Readings
        Text(
            text = accelerometerData,
            fontSize = fontSize.sp,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            fontStyle = fontStyleItalic,
            modifier = Modifier.padding(8.dp)
        )
        Text(
            text = gyroscopeData,
            fontSize = fontSize.sp,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            fontStyle = fontStyleItalic,
            modifier = Modifier.padding(8.dp)
        )
        Text(
            text = magnetometerData,
            fontSize = fontSize.sp,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            fontStyle = fontStyleItalic,
            modifier = Modifier.padding(8.dp)
        )
        Text(
            text = temperatureData,
            fontSize = fontSize.sp,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            fontStyle = fontStyleItalic,
            modifier = Modifier.padding(8.dp)
        )
        Text(
            text = lightData,
            fontSize = fontSize.sp,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            fontStyle = fontStyleItalic,
            modifier = Modifier.padding(8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Font Style Selection
        Text("Select Font Style:", fontSize = 18.sp, modifier = Modifier.padding(8.dp))
        val fontStyles = listOf("Default", "Serif", "Monospace")
        fontStyles.forEach { style ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(4.dp)
            ) {
                RadioButton(
                    selected = (style == fontStyle),
                    onClick = {
                        fontStyle = style
                        coroutineScope.launch {
                            context.dataStore.edit { it[FONT_STYLE_KEY] = style }
                        }
                    }
                )
                Spacer(Modifier.width(8.dp))
                Text(style, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Font Weight (Bold)
        Text("Font Weight:", fontSize = 18.sp, modifier = Modifier.padding(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = fontWeightStr == "Bold",
                onCheckedChange = { checked ->
                    val newWeight = if (checked) "Bold" else "Normal"
                    fontWeightStr = newWeight
                    coroutineScope.launch {
                        context.dataStore.edit { it[FONT_WEIGHT_KEY] = newWeight }
                    }
                }
            )
            Text("Bold", fontSize = 16.sp)
        }

        // Font Italic
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isItalic,
                onCheckedChange = { checked ->
                    isItalic = checked
                    coroutineScope.launch {
                        context.dataStore.edit { it[FONT_ITALIC_KEY] = checked.toString() }
                    }
                }
            )
            Text("Italic", fontSize = 16.sp)
        }

        Spacer(Modifier.height(16.dp))

        // Toggle Font Size Button
        Button(onClick = {
            coroutineScope.launch {
                context.dataStore.edit { settings ->
                    settings[FONT_SIZE_KEY] = if (fontSize == 16) 24 else 16
                }
            }
        }) {
            Text("Toggle Font Size (${if (fontSize == 16) 24 else 16}sp)")
        }
    }
}}