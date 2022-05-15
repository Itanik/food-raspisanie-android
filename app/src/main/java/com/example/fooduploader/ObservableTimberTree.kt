package com.example.fooduploader

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class ObservableTimberTree(private val coroutineScope: CoroutineScope) : Timber.DebugTree() {

    private var _logChannel: Channel<String> = Channel()
    val logFlow: Flow<String>
        get() = _logChannel.receiveAsFlow()
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.ROOT)

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val formattedMessage = dateFormat.format(Calendar.getInstance().time) + " | " + message
        coroutineScope.launch(Dispatchers.Default) {
            _logChannel.send(formattedMessage)
        }
        super.log(priority, tag, message, t)
    }
}