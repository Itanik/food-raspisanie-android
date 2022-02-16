package com.example.fooduploader

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

class MainActivityViewModel : ViewModel() {
    private val logger = ObservableTimberTree()

    fun initLogger(lifecycleOwner: LifecycleOwner, debugLog: RecyclerView) {
        val logAdapter = LogListAdapter()
        debugLog.adapter = logAdapter
        Timber.plant(logger)
        logger.logLiveData.observe(lifecycleOwner) {
            logAdapter.submitList(logAdapter.currentList.plus(it))
        }
    }
}