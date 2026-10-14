package cc.rhuan.receitas

import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import android.util.Log
import androidx.core.content.edit
import javax.inject.Inject
import cc.rhuan.receitas.data.repository.NotificationRepository
import cc.rhuan.receitas.notifications.ReminderSynchronizer
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import cc.rhuan.receitas.notifications.ReminderScheduler
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cc.rhuan.receitas.ui.navigation.MainScreen
import cc.rhuan.receitas.ui.theme.ReceitasTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var notificationRepository: NotificationRepository
    private val openedNotification = mutableStateOf<String?>(null)

    private fun handleNotificationIntent(intent: Intent?) {
        val id = intent?.getStringExtra(ReminderSynchronizer.EXTRA_NOTIFICATION_ID) ?: return
        openedNotification.value = id
        intent.removeExtra(ReminderSynchronizer.EXTRA_NOTIFICATION_ID)
        lifecycleScope.launch {
            try { notificationRepository.markAsRead(id) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { Log.e("MainActivity", "Não foi possível marcar o alerta como lido", e) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) ReminderScheduler.checkNow(this)
    }

    override fun onResume() {
        super.onResume()
        ReminderScheduler.checkNow(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleNotificationIntent(intent)
        val permissionPreferences = getSharedPreferences("notification_permission", MODE_PRIVATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
            !permissionPreferences.getBoolean("requested", false)) {
            permissionPreferences.edit { putBoolean("requested", true) }
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        enableEdgeToEdge()
        setContent {
            ReceitasTheme {
                MainScreen(openedNotification.value) { openedNotification.value = null }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ReceitasTheme {
        cc.rhuan.receitas.ui.feature.home.HomeScreenContent(cc.rhuan.receitas.ui.feature.home.HomeUiState())
    }
}
