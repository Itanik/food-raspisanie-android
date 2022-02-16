package com.example.fooduploader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber

class ObservableTimberTree : Timber.Tree() {

    private var _logLiveData = MutableLiveData("")
    val logLiveData: LiveData<String>
        get() = _logLiveData

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        _logLiveData.value = message
    }
}