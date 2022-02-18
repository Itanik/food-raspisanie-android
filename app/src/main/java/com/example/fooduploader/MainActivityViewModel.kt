package com.example.fooduploader

import android.content.ContentResolver
import android.content.res.AssetManager
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.fooduploader.data.Credentials
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import timber.log.Timber

class MainActivityViewModel : ViewModel() {
    private val logger = ObservableTimberTree()
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
        Timber.i("Selected menu file: $name")
        _status.value = R.string.status_select_menu
    }

    fun setTableBtnName(name: String?) {
        _selectTableBtnName.value = name
        Timber.i("Selected table file: $name")
        _status.value = R.string.status_select_table
    }

    fun isUploadPermitted() = menuUri != null || tableUri != null

    suspend fun upload(resolver: ContentResolver) {
        _status.postValue(R.string.status_uploading)

        try {
            val connectionResult = ftp.connect()


        } catch (e: Exception) {

        } finally {
            ftp.disconnect()
        }

    }
}