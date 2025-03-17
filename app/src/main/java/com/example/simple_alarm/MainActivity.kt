package com.example.simple_alarm

import android.app.AlarmManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getSystemService
import java.util.*
import android.provider.Settings


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Kiểm tra và yêu cầu quyền báo thức chính xác
        checkAndRequestAlarmPermission(this)
        setContent {
            AlarmApp(context = this)
        }
    }
}

fun checkAndRequestAlarmPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            context.startActivity(intent)
        }
    }
}

@Composable
fun AlarmApp(context: Context) {
    var selectedHour by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }
    var label by remember { mutableStateOf("Báo thức") }
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Chọn giờ báo thức", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { showTimePicker = true }) {
            Text(text = "Chọn thời gian")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Báo thức đã chọn: $selectedHour:$selectedMinute")

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Nhập nhãn báo thức") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { setAlarm(context, selectedHour, selectedMinute, label) }) {
            Text(text = "Đặt báo thức")
        }

        if (showTimePicker) {
            TimePickerDialog(
                onTimeSelected = { hour, minute ->
                    selectedHour = hour
                    selectedMinute = minute
                    showTimePicker = false
                },
                onDismiss = { showTimePicker = false }
            )
        }
    }
}

@Composable
fun TimePickerDialog(onTimeSelected: (Int, Int) -> Unit, onDismiss: () -> Unit) {
    val calendar = Calendar.getInstance()
    var hour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onTimeSelected(hour, minute) }) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Hủy")
            }
        },
        title = { Text("Chọn thời gian") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Giờ: $hour - Phút: $minute")

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = { hour = (hour + 1) % 24 }) { Text("+ Giờ") }
                    Button(onClick = { hour = (hour - 1 + 24) % 24 }) { Text("- Giờ") }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = { minute = (minute + 1) % 60 }) { Text("+ Phút") }
                    Button(onClick = { minute = (minute - 1 + 60) % 60 }) { Text("- Phút") }
                }
            }
        }
    )
}

fun setAlarm(context: Context, hour: Int, minute: Int, label: String) {
    val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
        putExtra(AlarmClock.EXTRA_HOUR, hour)
        putExtra(AlarmClock.EXTRA_MINUTES, minute)
        putExtra(AlarmClock.EXTRA_MESSAGE, label)
        putExtra(AlarmClock.EXTRA_SKIP_UI, false) // Mở giao diện báo thức của hệ thống
    }
    try {
        context.startActivity(intent)
        Toast.makeText(context, "Báo thức '$label' đã được đặt vào $hour:$minute", Toast.LENGTH_SHORT).show()
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Không có ứng dụng báo thức nào được hỗ trợ!", Toast.LENGTH_SHORT).show()
    }
}
