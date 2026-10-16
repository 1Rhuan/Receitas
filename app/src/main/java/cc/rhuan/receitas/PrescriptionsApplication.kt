package cc.rhuan.receitas

import android.app.Application
import android.util.Log
import cc.rhuan.receitas.data.local.AppDatabase
import cc.rhuan.receitas.notifications.ReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retryWhen

@HiltAndroidApp
class PrescriptionsApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, error ->
        Log.e("PrescriptionsApplication", "Failed to observe changes", error)
    })
    override fun onCreate() {
        super.onCreate()
        ReminderScheduler.schedule(this)
        applicationScope.launch {
            val db = AppDatabase.getDatabase(this@PrescriptionsApplication)
            combine(db.prescriptionDao().getAllPrescriptions(), db.medicationDao().getAllMedications(), db.trackingDao().observeTracking(), db.trackingDao().observeEvents()) { prescriptions, medications, tracking, events -> listOf(prescriptions, medications, tracking, events) }
                .distinctUntilChanged().retryWhen { error, attempt ->
                    if (error is CancellationException) throw error
                    Log.e("PrescriptionsApplication", "Failed to observe reminder changes", error)
                    if (attempt < 3) { delay(1000L * (attempt + 1)); true } else false
                }.catch { error ->
                    if (error is CancellationException) throw error
                    Log.e("PrescriptionsApplication", "Observation unavailable; periodic checks remain scheduled", error)
                }.collect { ReminderScheduler.checkNow(this@PrescriptionsApplication) }
        }
    }
}
