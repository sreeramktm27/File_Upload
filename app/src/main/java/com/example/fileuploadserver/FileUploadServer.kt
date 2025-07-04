package com.example.fileuploadserver

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import fi.iki.elonen.NanoHTTPD
import java.io.File

class FileUploadServer(
    private val context: Context,
    port: Int,
    private val folderUri: Uri
) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        return when (session.method) {
            Method.GET -> serveUploadPage(session)
            Method.POST -> handleFileUpload(session)
            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "❌ Endpoint not found")
        }
//        return when {
//            session.method == Method.GET && session.uri == "/upload" -> {
//                val html = context.assets.open("upload_form.html").bufferedReader().use { it.readText() }
//                newFixedLengthResponse(Response.Status.OK, "text/html", html)
//            }
//
//            session.method == Method.POST && session.uri == "/upload" -> {
//                val files = HashMap<String, String>()
//                return try {
//                    session.parseBody(files)
//
//                    val uploadedFileName = session.parameters["file"]?.firstOrNull()
//                        ?: "upload_${System.currentTimeMillis()}.bin"
//                    val tempFilePath = files["file"]
//                        ?: return newFixedLengthResponse("Upload failed: Missing temp file")
//
//                    val parentDocumentUri = DocumentsContract.buildDocumentUriUsingTree(
//                        folderUri,
//                        DocumentsContract.getTreeDocumentId(folderUri)
//                    )
//                    val docUri = DocumentsContract.createDocument(
//                        context.contentResolver,
//                        parentDocumentUri,
//                        "application/octet-stream",
//                        uploadedFileName
//                    ) ?: return newFixedLengthResponse("Upload failed: Couldn't create file")
//
//                    context.contentResolver.openOutputStream(docUri)?.use { out ->
//                        File(tempFilePath).inputStream().use { it.copyTo(out) }
//                    }
//
//                    newFixedLengthResponse("✅ File uploaded successfully!")
//
//                } catch (e: Exception) {
//                    newFixedLengthResponse("❌ Upload failed: ${e.message}")
//                }
//            }
//
//            else -> {
//                newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found")
//            }
//        }
    }

    private fun serveUploadPage(session: IHTTPSession): Response {
        if (session.uri != "/upload") {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "❌ Page not found")
        }

        return try {
            val html = context.assets.open("upload_form.html")
                .bufferedReader()
                .use { it.readText() }

            newFixedLengthResponse(Response.Status.OK, "text/html", html)
        } catch (e: Exception) {
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "❌ Failed to load HTML: ${e.message}")
        }
    }

    private fun handleFileUpload(session: IHTTPSession): Response {
        if (session.uri != "/upload") {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "❌ Endpoint not found")
        }

        val filesMap = HashMap<String, String>()
        return try {
            session.parseBody(filesMap)

            val treeDocUri = DocumentsContract.buildDocumentUriUsingTree(
                folderUri,
                DocumentsContract.getTreeDocumentId(folderUri)
            )

            var count = 0
            for ((key, tempPath) in filesMap) {
                if (!key.startsWith("file")) continue

                val fileName = session.parameters[key]?.firstOrNull()
                    ?: "upload_${System.currentTimeMillis()}_$count.bin"
                count++

                val mimeType = getMimeType(fileName)
                val docUri = DocumentsContract.createDocument(
                    context.contentResolver, treeDocUri, mimeType, fileName
                ) ?: continue

                context.contentResolver.openOutputStream(docUri)?.use { out ->
                    File(tempPath).inputStream().use { input ->
                        input.copyTo(out)
                    }
                }
            }

            // Redirect to upload page (Prevents accidental re-POST on refresh)
            newFixedLengthResponse("✅ File uploaded successfully!")

        } catch (e: Exception) {
            e.printStackTrace()
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "❌ Upload failed: ${e.message}")
        }
    }

    private fun getMimeType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
            ?: "application/octet-stream"
    }
}
