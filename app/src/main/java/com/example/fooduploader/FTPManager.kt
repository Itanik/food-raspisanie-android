package com.example.fooduploader

import com.example.fooduploader.data.Credentials
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
        client.logout()
        client.disconnect()
        Timber.d("Disconnected from server")
    }

    fun getTableFilesList(): List<String> =
        client.listFiles(foodPath).map { it.name }.filter { it.endsWith("-sm.xlsx") }

    private fun uploadFile(path: String, file: InputStream): Boolean =
        client.storeFile(path, file)

    companion object {
        private const val foodPath = "/food/"
        private const val foodJsonFileName = "food_files.json"
        private const val menuJsonFileName = "menu_file.json"
        private const val foodImageFileName = "menu."
    }
}