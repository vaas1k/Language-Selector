package vegabobo.languageselector

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import rikka.shizuku.Shizuku
import vegabobo.languageselector.service.UserService
import vegabobo.languageselector.service.UserServiceProvider
import vegabobo.languageselector.ui.screen.Navigation
import vegabobo.languageselector.ui.screen.main.OperationMode
import vegabobo.languageselector.ui.theme.LanguageSelector

object ShizukuArgs {
    val userServiceArgs =
        Shizuku.UserServiceArgs(
            ComponentName(BuildConfig.APPLICATION_ID, UserService::class.java.name),
        )
            .daemon(false)
            .processNameSuffix("service")
            .debuggable(BuildConfig.DEBUG)
            .version(BuildConfig.VERSION_CODE)
}


@AndroidEntryPoint
class MainActivity : ComponentActivity(), Shizuku.OnRequestPermissionResultListener {

    override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
        if (grantResult == PackageManager.PERMISSION_GRANTED)
            UserServiceProvider.bind()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LanguageSelector { Navigation() }
        }

        Shizuku.addRequestPermissionResultListener(this)
    }

    override fun onResume() {
        super.onResume()
        if (UserServiceProvider.opMode != OperationMode.NONE && !UserServiceProvider.isConnected())
            UserServiceProvider.bind()
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(this)
        super.onDestroy()
    }

}