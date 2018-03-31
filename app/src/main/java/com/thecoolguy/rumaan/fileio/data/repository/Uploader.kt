package com.thecoolguy.rumaan.fileio.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Blob
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.thecoolguy.rumaan.fileio.utils.Utils
import java.io.FileInputStream


object Uploader {
    val TAG = Uploader::class.simpleName
    fun uploadFile(context: Context, fileUri: Uri, fileInputStream: FileInputStream) {
        val localFile = Utils.getFileDetails(context, fileUri)

        Fuel.upload("https://file.io")
                .blob { _, _ ->
                    Blob(localFile.name, localFile.size, {
                        fileInputStream
                    })
                }
                .name {
                    "file"
                }
                .progress { readBytes, totalBytes ->
                    val p = (readBytes.toFloat() / totalBytes * 100).toInt()
                    Log.d(TAG, "Progress: $p")
                }
                .responseObject(Response.Deserializer()) { _, _, result ->
                    when (result) {
                        is Result.Success -> {
                            val res = result.get()
                            Log.d(TAG, res.toString())
                        }
                        is Result.Failure -> {
                            val exception = result.getException()
                        }

                    }
                }

    }
}

data class Response(val success: Boolean,
                    val key: String,
                    val link: String,
                    val expiry: String,
                    val message: String,
                    val error: Int) {
    class Deserializer : ResponseDeserializable<Response> {
        override fun deserialize(content: String): Response? {
            return Gson().fromJson(content, Response::class.java)
        }
    }
}