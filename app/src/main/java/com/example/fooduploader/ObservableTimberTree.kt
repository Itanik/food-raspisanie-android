package com.example.fooduploader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class ObservableTimberTree(private val coroutineScope: CoroutineScope) : Timber.DebugTree() {

    private var _logLiveData = MutableLiveData("")
    val logLiveData: LiveData<String>
        get() = _logLiveData
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.ROOT)

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val formattedMessage = dateFormat.format(Calendar.getInstance().time) + " | " + message
        coroutineScope.launch(Dispatchers.Main) {
            _logLiveData.value = formattedMessage
        }
        super.log(priority, tag, message, t)
    }
}