package com.example.activitytrackerapp.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.activitytrackerapp.data.database.ActivityTrackerDatabase
import com.example.activitytrackerapp.utils.BackupManager
import kotlinx.coroutines.launch
import java.io.File
import android.net.Uri

@Composable
fun BackupTestScreen(database: ActivityTrackerDatabase, context: Context) {
    val scope = rememberCoroutineScope()
    var backupFilePath by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = {
            scope.launch {
                val file = BackupManager.createBackup(context, database)
                backupFilePath = file.absolutePath
            }
        }) {
            Text("Create Backup")
        }

        Button(onClick = {
            scope.launch {
                backupFilePath?.let { path ->
                    val file = File(path)
                    val success = BackupManager.restoreBackup(context, Uri.fromFile(file), database)
                    println("Restore success? $success")
                }
            }
        }) {
            Text("Restore Backup")
        }

        backupFilePath?.let {
            Text("Backup saved at: $it")
        }
    }
}
