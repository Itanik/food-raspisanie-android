package com.example.fooduploader

import android.content.res.AssetManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import timber.log.Timber

class MainActivityViewModel : ViewModel() {
    private val logger = ObservableTimberTree()
    private lateinit var credentials: Credentials

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
}