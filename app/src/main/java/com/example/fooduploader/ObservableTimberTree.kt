package com.example.fooduploader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class ObservableTimberTree : Timber.Tree() {

    private var _logLiveData = MutableLiveData("")
    val logLiveData: LiveData<String>
        get() = _logLiveData
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.ROOT)

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val formattedMessage = dateFormat.format(Calendar.getInstance().time) + " | " + message
        if (!_logLiveData.hasActiveObservers())
            _logLiveData.value = _logLiveData.value.plus('\n').plus(message)
        else
            _logLiveData.value = formattedMessage
    }
}