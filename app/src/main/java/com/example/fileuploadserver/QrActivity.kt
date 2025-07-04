package com.example.fileuploadserver

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class QrActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageView = ImageView(this)
        val textView = TextView(this)
        textView.setPadding(0, 20, 0, 0)
        textView.textSize = 16f
        textView.setTextColor(Color.BLACK)
        textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

        val layout = androidx.appcompat.widget.LinearLayoutCompat(this).apply {
            orientation = androidx.appcompat.widget.LinearLayoutCompat.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(16, 16, 16, 16)
            addView(imageView)
            addView(textView)
        }

        setContentView(layout)

        val url = intent.getStringExtra("text") ?: return

        val qrCode = generateQRCode(url)
        imageView.setImageBitmap(qrCode)
        textView.text = "Scan to Upload:\n$url"
    }

    private fun generateQRCode(text: String): Bitmap {
        val size = 600
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }
}