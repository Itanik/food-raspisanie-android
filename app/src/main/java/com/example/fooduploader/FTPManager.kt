package com.example.fooduploader

import com.example.fooduploader.data.Credentials
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPClientConfig
import timber.log.Timber
import java.io.InputStream

class FTPManager(private val credentials: Credentials) {
    private val client: FTPClient by lazy {
        FTPClient().apply {
            configure(FTPClientConfig())
        }
    }

    fun connect(): Int {
        client.apply {
            connect(credentials.host)
            enterLocalPassiveMode()
            login(credentials.user, credentials.password)
            Timber.i("Connected to ${credentials.host}")
            Timber.i(replyString)
            return replyCode
        }
    }

    fun disconnect() {
        client.disconnect()
        Timber.d("Disconnected from server")
    }

    fun getTableFilesList(): List<String> =
        client.listFiles(foodPath).map { it.name }.filter { it.endsWith("-sm.xlsx") }

    fun uploadFile(path: String, file: InputStream): Boolean {
        Timber.i("Starting to upload file on: $path")
        client.setFileType(FTP.BINARY_FILE_TYPE)
        return client.storeFile(path, file)
    }

    companion object {
        const val foodPath = "/food/"
        const val foodJsonFileName = "food_files.json"
        const val menuJsonFileName = "menu_file.json"
        const val foodImageFileName = "menu."
    }
}