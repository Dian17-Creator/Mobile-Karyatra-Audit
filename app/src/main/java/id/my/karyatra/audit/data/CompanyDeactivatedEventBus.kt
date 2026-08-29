package id.my.karyatra.audit.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object CompanyDeactivatedEventBus {
    private val _deactivatedEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val deactivatedEvent: SharedFlow<String> = _deactivatedEvent.asSharedFlow()

    fun emitDeactivated(message: String) {
        _deactivatedEvent.tryEmit(message)
    }
}
