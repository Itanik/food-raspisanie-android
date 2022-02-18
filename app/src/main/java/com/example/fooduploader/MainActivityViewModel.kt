package com.example.fooduploader

import android.content.ContentResolver
import android.content.res.AssetManager
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.example.fooduploader.FTPManager.Companion.foodImageFileName
import com.example.fooduploader.FTPManager.Companion.foodJsonFileName
import com.example.fooduploader.FTPManager.Companion.foodPath
import com.example.fooduploader.FTPManager.Companion.menuJsonFileName
import com.example.fooduploader.data.Credentials
import com.example.fooduploader.data.Food
import com.example.fooduploader.data.Menu
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.commons.net.ftp.FTPReply
import timber.log.Timber

class MainActivityViewModel : ViewModel() {
    private val logger = ObservableTimberTree(viewModelScope)
    private lateinit var credentials: Credentials
    private val ftp by lazy { FTPManager(credentials) }

    var menuUri: Uri? = null
    var tableUri: Uri? = null

    private var _status = MutableLiveData(R.string.status_disconnected)
    val status get() = _status
    private var _selectMenuBtnName = MutableLiveData<String?>()
    val selectMenuBtnName get() = _selectMenuBtnName
    private var _selectTableBtnName = MutableLiveData<String?>()
    val selectTableBtnName get() = _selectTableBtnName

    fun initLogger(lifecycleOwner: LifecycleOwner, debugLog: RecyclerView) {
        Timber.plant(logger)
        val logAdapter = LogListAdapter()
        debugLog.adapter = logAdapter
        logger.logLiveData.observe(lifecycleOwner) {
            logAdapter.submitList(logAdapter.currentList.plus(it))
        }
    }

    fun readCredentials(assets: AssetManager) {
        val credJson = assets.open("credentials.json").bufferedReader().use {
            it.readText()
        }
        credentials = Json.decodeFromString(credJson)
        Timber.i("Credentials successfully read")
    }

    fun setMenuBtnName(name: String?) {
        _selectMenuBtnName.value = name
        if (name != null) {
            Timber.i("Selected menu file: $name")
            updateFileStatus()
        } else {
            Timber.i("Menu file not selected")
        }
    }

    fun setTableBtnName(name: String?) {
        _selectTableBtnName.value = name
        if (name != null) {
            Timber.i("Selected table file: $name")
            updateFileStatus()
        } else {
            Timber.i("Table file not selected")
        }
    }

    private fun updateFileStatus() {
        _status.value = when {
            _selectMenuBtnName.value != null && _selectTableBtnName.value != null -> R.string.status_selected_menu_table
            _selectMenuBtnName.value != null -> R.string.status_selected_menu
            _selectTableBtnName.value != null -> R.string.status_selected_table
            else -> R.string.status_disconnected
        }
    }

    fun isUploadPermitted() = menuUri != null || tableUri != null

    suspend fun upload(resolver: ContentResolver) {
        _status.postValue(R.string.status_uploading)

        try {
            val connectionResult = ftp.connect()
            if (!FTPReply.isPositiveCompletion(connectionResult)) throw Exception("Server refuse connection")

            Timber.i("Connection successful")
            if (menuUri != null) {
                val path = uploadMenuFile(menuUri!!, resolver)
                uploadMenuJson(path)
                resetMenuFile()
            }
            if (tableUri != null) {
                uploadTable(tableUri!!, resolver)
                uploadUpdatedFoodFilesJson()
                resetTableFile()
            }
            _status.postValue(R.string.status_success)
        } catch (e: Exception) {
            Timber.e(e)
            _status.postValue(R.string.status_error)
        } finally {
            ftp.disconnect()
        }

    }

    private fun resetTableFile() {
        _selectTableBtnName.postValue(null)
        tableUri = null
    }

    private fun resetMenuFile() {
        _selectMenuBtnName.postValue(null)
        menuUri = null
    }

    private fun uploadUpdatedFoodFilesJson() {
        val foodFiles = ftp.getTableFilesList()
        Timber.i("Total number of food files: ${foodFiles.size}")
        val foodData = foodFiles.map {
            Food(it, foodPath.plus(it))
        }
        val format = Json { prettyPrint = true }
        val json = format.encodeToString(foodData)
        json.byteInputStream().use { inStream ->
            if (ftp.uploadFile(foodPath.plus(foodJsonFileName), inStream)) {
                Timber.i("Successfully uploaded $foodJsonFileName!")
            } else
                throw Exception("Failure! $foodJsonFileName does not uploaded")
        }
    }

    private fun uploadTable(tableUri: Uri, resolver: ContentResolver) {
        val fileName = resolver.extractFileName(tableUri)
        resolver.openInputStream(tableUri).use { inStream ->
            if (ftp.uploadFile(foodPath.plus(fileName), inStream!!))
                Timber.i("Successfully uploaded $fileName!")
            else
                throw Exception("Failure! $fileName does not uploaded")
        }
    }

    private fun uploadMenuJson(path: String) {
        val format = Json { prettyPrint = true }
        val json = format.encodeToString(Menu(path.plus("?").plus(System.currentTimeMillis())))
        json.byteInputStream().use { inStream ->
            if (ftp.uploadFile(foodPath.plus(menuJsonFileName), inStream)) {
                Timber.i("Successfully uploaded $menuJsonFileName!")
            } else
                throw Exception("Failure! $menuJsonFileName does not uploaded")
        }
    }

    private fun uploadMenuFile(menuUri: Uri, resolver: ContentResolver): String {
        val fileName = resolver.extractFileName(menuUri)
        val newName = foodImageFileName.plus(fileName!!.extension)
        val newPath = foodPath.plus(newName)
        resolver.openInputStream(menuUri).use { inStream ->
            if (ftp.uploadFile(newPath, inStream!!))
                Timber.i("Successfully uploaded $newName!")
            else
                Timber.e("Failure! $newName does not uploaded")
        }
        return newPath
    }

    private val String.extension: String get() = this.split('.').last()
}