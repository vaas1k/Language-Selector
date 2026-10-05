package vegabobo.languageselector.service

import android.app.IActivityManager
import android.app.IActivityTaskManager
import android.app.ILocaleManager
import android.os.LocaleList
import android.os.Process
import rikka.shizuku.SystemServiceHelper
import vegabobo.languageselector.IUserService
import kotlin.system.exitProcess


class UserService : IUserService.Stub() {

    override fun exit() {
        destroy()
    }

    override fun destroy() {
        exitProcess(0)
    }

    override fun getUid(): Int {
        return Process.myUid()
    }

    private val localeManager by lazy {
        ILocaleManager.Stub.asInterface(SystemServiceHelper.getSystemService("locale"))
    }

    override fun setApplicationLocales(packageName: String?, locales: LocaleList?) {
        localeManager.setApplicationLocales(packageName, currentUserId(), locales, true)
    }

    override fun getApplicationLocales(packageName: String?): LocaleList {
        return localeManager.getApplicationLocales(packageName, currentUserId())
    }

    override fun getSystemLocales(): LocaleList {
        return localeManager.systemLocales
    }

    private val activityManager by lazy {
        IActivityManager.Stub.asInterface(SystemServiceHelper.getSystemService("activity"))
    }

    // ActivityManager.getCurrentUser() is hidden and android.jar shadows any android.app.ActivityManager stub
    private fun currentUserId(): Int {
        return activityManager.currentUserId
    }

    override fun forceStopPackage(packageName: String?) {
        activityManager.forceStopPackage(packageName, currentUserId())
    }

    private val activityTaskManager by lazy {
        IActivityTaskManager.Stub.asInterface(SystemServiceHelper.getSystemService("activity_task"))
    }

    override fun getFirstRunningTaskPackage(): String {
        val runningTask = activityTaskManager.getTasks(1, false, false, -1).firstOrNull()
        return runningTask?.topActivity?.packageName ?: ""
    }
}
