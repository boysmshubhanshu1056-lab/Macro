
package com.beaste.macro

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import rikka.shizuku.Shizuku

private val BG = Color(0xFF10121A)
private val TILE = Color(0xFF202431)
private val PURPLE = Color(0xFF9254FF)
private val CYAN = Color(0xFF00D9FF)
private val YELLOW = Color(0xFFFFD84D)

class MainActivity : ComponentActivity() {

    companion object {
        private const val REQUEST_SHIZUKU = 301
    }

    private var refresh by mutableIntStateOf(0)

    private val permissionListener =
        Shizuku.OnRequestPermissionResultListener {
                code, result ->

            if (code == REQUEST_SHIZUKU) {
                refresh++
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Shizuku.addRequestPermissionResultListener(
            permissionListener
        )

        setContent {
            BeastEApp(
                refresh = refresh,
                onConnect = {
                    if (!Shizuku.pingBinder()) {
                        refresh++
                    } else if (
                        Shizuku.checkSelfPermission() ==
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        ShizukuController.bind()
                        refresh++
                    } else {
                        Shizuku.requestPermission(
                            REQUEST_SHIZUKU
                        )
                    }
                },
                onAccessibility = {
                    startActivity(
                        Intent(
                            Settings.ACTION_ACCESSIBILITY_SETTINGS
                        )
                    )
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refresh++
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(
            permissionListener
        )
        super.onDestroy()
    }
}

@Composable
private fun BeastEApp(
    refresh: Int,
    onConnect: () -> Unit,
    onAccessibility: () -> Unit
) {
    val context =
        androidx.compose.ui.platform.LocalContext.current

    var selected by remember {
        mutableStateOf(MacroSettings.selected(context))
    }

    var speed by remember {
        mutableFloatStateOf(
            MacroSettings.speed(context).toFloat()
        )
    }

    var enabled by remember {
        mutableStateOf(MacroSettings.enabled(context))
    }

    val permission = remember(refresh) {
        ShizukuController.hasPermission()
    }

    val connected = remember(refresh) {
        ShizukuController.connected
    }

    val accessibility = remember(refresh) {
        accessibilityEnabled(context)
    }

    MaterialTheme {
        Column(
            Modifier
                .fillMaxSize()
                .background(BG)
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                "BeastE",
                color = CYAN,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                "KEYBOARD MACRO",
                color = Color.Gray,
                fontSize = 11.sp
            )

            Spacer(Modifier.height(30.dp))

            Text(
                "SELECT YOUR KEYS",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(14.dp))

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                MacroSettings.availableKeys.forEach { key ->

                    val active = key in selected

                    Box(
                        Modifier
                            .size(52.dp)
                            .background(
                                if (active) Color.White else TILE,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(enabled = !enabled) {
                                selected =
                                    if (active) selected - key
                                    else selected + key

                                MacroSettings.setSelected(
                                    context,
                                    selected
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            key,
                            color = if (active) Color.Black
                            else YELLOW,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(26.dp))

            Text(
                "REPEAT INTERVAL",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                "${speed.toInt()} ms",
                color = CYAN,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold
            )

            Slider(
                value = speed,
                onValueChange = {
                    speed = it
                    MacroSettings.setSpeed(
                        context,
                        it.toInt()
                    )
                },
                valueRange = 10f..500f,
                enabled = !enabled
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Text("10 ms", color = Color.Gray)
                Text("500 ms", color = Color.Gray)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (!permission || !connected) {
                        onConnect()
                    } else {
                        enabled = !enabled
                        MacroSettings.setEnabled(
                            context,
                            enabled
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (enabled)
                        Color(0xFFB3261E)
                    else PURPLE
                )
            ) {
                Text(
                    when {
                        !permission -> "GRANT SHIZUKU"
                        !connected -> "CONNECT INPUT SERVICE"
                        enabled -> "DISABLE MACRO"
                        else -> "ENABLE MACRO"
                    },
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))

            Status(
                "Shizuku permission",
                if (permission) "GRANTED" else "NOT GRANTED",
                permission
            )

            Status(
                "UserService",
                if (connected) "CONNECTED" else "NOT CONNECTED",
                connected
            )

            Status(
                "Keyboard listener",
                if (accessibility) "ENABLED" else "NOT ENABLED",
                accessibility
            )

            OutlinedButton(
                onClick = onAccessibility,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("OPEN ACCESSIBILITY SETTINGS")
            }

            Spacer(Modifier.height(10.dp))

            Text(
                "Requires Shizuku running and BeastE Keyboard Listener enabled.",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun Status(
    label: String,
    value: String,
    good: Boolean
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {
        Text(
            label,
            color = Color.LightGray,
            fontSize = 13.sp
        )

        Text(
            value,
            color = if (good) CYAN else YELLOW,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun accessibilityEnabled(
    context: android.content.Context
): Boolean {

    val expected =
        "${context.packageName}/com.beaste.macro.MacroAccessibilityService"

    val enabled = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    return enabled.split(':').any {
        it.equals(expected, ignoreCase = true)
    }
}
