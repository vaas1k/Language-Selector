package vegabobo.languageselector.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import kotlinx.coroutines.flow.MutableStateFlow
import vegabobo.languageselector.IUserService

class Connection : ServiceConnection {

    val service = MutableStateFlow<IUserService?>(null)
    var SERVICE: IUserService?
        get() = service.value
        set(value) {
            service.value = value
        }

    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        binder ?: return
        val newService = IUserService.Stub.asInterface(binder)
        try {
            binder.linkToDeath({ service.compareAndSet(newService, null) }, 0)
        } catch (e: RemoteException) {
            return // already dead
        }
        SERVICE = newService
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        SERVICE = null
    }
}
