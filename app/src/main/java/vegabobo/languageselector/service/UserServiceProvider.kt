package vegabobo.languageselector.service

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.DeadObjectException
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import rikka.shizuku.Shizuku
import vegabobo.languageselector.BuildConfig
import vegabobo.languageselector.IUserService
import vegabobo.languageselector.ShizukuArgs
import vegabobo.languageselector.ui.screen.main.OperationMode

object UserServiceProvider {

    private val tag = this.javaClass.simpleName
    private val mainHandler = Handler(Looper.getMainLooper())

    val connection = Connection()
    @Volatile
    var opMode = OperationMode.NONE
    @Volatile
    var uid = -1
    @Volatile
    private var uidOf: IUserService? = null

    fun bind(): Boolean {
        if (opMode == OperationMode.ROOT) {
            val intent = Intent().setComponent(
                ComponentName(BuildConfig.APPLICATION_ID, RootUserService::class.java.name)
            )
            // libsu enforces main thread
            mainHandler.post { RootService.bind(intent, connection) }
            return true
        }
        val isShizukuAvail = try {
            Shizuku.pingBinder() &&
                    Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
        if (!isShizukuAvail) {
            if (Looper.myLooper() != Looper.getMainLooper() && Shell.isAppGrantedRoot() == null)
                Shell.getShell()
            if (Shell.isAppGrantedRoot() != true)
                return false
            opMode = OperationMode.ROOT
            return bind()
        }
        opMode = OperationMode.SHIZUKU
        // Shizuku delivers callbacks on main, keep its (non thread-safe) connection set there too
        mainHandler.post {
            try {
                Shizuku.bindUserService(ShizukuArgs.userServiceArgs, connection)
            } catch (e: Exception) {
                Log.e(tag, "Cannot bind Shizuku UserService", e)
            }
        }
        return true
    }

    private suspend fun awaitService(): IUserService {
        connection.SERVICE?.let {
            if (it.asBinder().isBinderAlive) return it.cacheUid()
            connection.service.compareAndSet(it, null)
        }
        check(bind()) { "Neither root nor Shizuku is available" }
        return withTimeout(20_000) { connection.service.filterNotNull().first() }
            .cacheUid()
    }

    // The service may be bound outside awaitService (MainActivity), so cache per instance
    private fun IUserService.cacheUid(): IUserService {
        if (uidOf !== this) {
            UserServiceProvider.uid = getUid()
            uidOf = this
            Log.i(tag, "connected via $opMode, uid ${UserServiceProvider.uid}")
        }
        return this
    }

    // Blocking, never call from main thread (connection callbacks are delivered there)
    fun getService(): IUserService {
        return runBlocking { awaitService() }
    }

    fun run(
        onFail: () -> Unit = {},
        onConnected: suspend IUserService.() -> Unit,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            repeat(2) {
                val service = try {
                    awaitService()
                } catch (e: Exception) {
                    Log.e(tag, "Service unavailable.", e)
                    onFail()
                    return@launch
                }
                try {
                    onConnected(service)
                    return@launch
                } catch (e: DeadObjectException) {
                    Log.w(tag, "UserService died, rebinding", e)
                    connection.service.compareAndSet(service, null)
                } catch (e: Exception) {
                    Log.e(tag, "UserService call failed", e)
                    onFail()
                    return@launch
                }
            }
            onFail()
        }
    }

    fun isConnected(): Boolean {
        return connection.SERVICE?.asBinder()?.isBinderAlive == true
    }
}
