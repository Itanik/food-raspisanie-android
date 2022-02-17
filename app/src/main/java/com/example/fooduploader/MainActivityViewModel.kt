package com.example.fooduploader

import android.content.res.AssetManager
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

    private var _status = MutableLiveData<String?>()
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
    }

    fun selectMenu() {
        _status.value = "Выбор меню"
    }

    fun selectTable() {
        _status.value = "Выбор таблицы"
    }

    fun upload() {
        _status.value = "Загрузка"
    }
}