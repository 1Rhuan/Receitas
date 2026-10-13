package cc.rhuan.receitas

import android.app.Application
import cc.rhuan.receitas.data.local.AppDatabase
import cc.rhuan.receitas.notifications.ReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

@HiltAndroidApp
class ReceitasApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override fun onCreate() {
        super.onCreate()
        ReminderScheduler.schedule(this)
        applicationScope.launch {
            val db = AppDatabase.getDatabase(this@ReceitasApplication)
            combine(db.prescriptionDao().getAllPrescriptions(), db.medicationDao().getAllMedications()) { prescriptions, medications -> prescriptions to medications }
                .distinctUntilChanged().collect { ReminderScheduler.checkNow(this@ReceitasApplication) }
        }
    }
}
