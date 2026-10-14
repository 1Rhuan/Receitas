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
class ReceitasApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, error ->
        Log.e("ReceitasApplication", "Falha no acompanhamento de alterações", error)
    })
    override fun onCreate() {
        super.onCreate()
        ReminderScheduler.schedule(this)
        applicationScope.launch {
            val db = AppDatabase.getDatabase(this@ReceitasApplication)
            combine(db.prescriptionDao().getAllPrescriptions(), db.medicationDao().getAllMedications(), db.trackingDao().observeTracking(), db.trackingDao().observeEvents()) { prescriptions, medications, tracking, events -> listOf(prescriptions, medications, tracking, events) }
                .distinctUntilChanged().retryWhen { error, attempt ->
                    if (error is CancellationException) throw error
                    Log.e("ReceitasApplication", "Não foi possível observar alterações para os lembretes", error)
                    if (attempt < 3) { delay(1000L * (attempt + 1)); true } else false
                }.catch { error ->
                    if (error is CancellationException) throw error
                    Log.e("ReceitasApplication", "Observação indisponível; a verificação periódica permanece agendada", error)
                }.collect { ReminderScheduler.checkNow(this@ReceitasApplication) }
        }
    }
}
