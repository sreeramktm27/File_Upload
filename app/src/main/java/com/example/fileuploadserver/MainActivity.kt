package com.example.fileuploadserver

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var selectedFolderUri: Uri? = null

    // Launch system folder picker
    private val folderPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            selectedFolderUri = it
            Toast.makeText(this, "✅ Folder selected!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val selectFolderBtn = findViewById<Button>(R.id.selectFolderBtn)
        val startBtn = findViewById<Button>(R.id.startBtn)
        val stopBtn = findViewById<Button>(R.id.stopBtn)

        selectFolderBtn.setOnClickListener {
            folderPickerLauncher.launch(null)
        }

        startBtn.setOnClickListener {
            selectedFolderUri?.let { uri ->
                val serviceIntent = Intent(this, FileServerService::class.java).apply {
                    putExtra("folderUri", uri)
                }
                ContextCompat.startForegroundService(this, serviceIntent)

                val uploadUrl = "http://${getLocalIpAddress()}:8080/upload"
                val qrIntent = Intent(this, QrActivity::class.java).apply {
                    putExtra("text", uploadUrl)
                }
                startActivity(qrIntent)

            } ?: Toast.makeText(this, "⚠️ Please select a folder first", Toast.LENGTH_SHORT).show()
        }

        stopBtn.setOnClickListener {
            stopService(Intent(this, FileServerService::class.java))
            Toast.makeText(this, "🛑 File server stopped", Toast.LENGTH_SHORT).show()
        }
    }

    // Get device's local IP for displaying upload URL
    private fun getLocalIpAddress(): String {
        val wm = applicationContext.getSystemService(WIFI_SERVICE) as android.net.wifi.WifiManager
        return android.text.format.Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
    }
}