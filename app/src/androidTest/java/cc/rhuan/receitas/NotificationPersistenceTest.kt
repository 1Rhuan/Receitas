package cc.rhuan.receitas

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import cc.rhuan.receitas.data.local.AppDatabase
import cc.rhuan.receitas.data.local.entity.NotificationEntity
import cc.rhuan.receitas.data.repository.RoomNotificationRepository
import cc.rhuan.receitas.domain.model.NotificationType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import java.util.UUID

class NotificationPersistenceTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var db: AppDatabase
    private val id = "audit:" + UUID.randomUUID()
    @Before fun setup() { db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build() }
    @After fun cleanup() {
        db.close()
        context.getSharedPreferences("delivered_reminders", Context.MODE_PRIVATE).edit().remove(id).remove("dismissed:" + id).commit()
    }
    @Test fun unsupportedPhaseDoesNotBreakTheAlertList() = runBlocking {
        db.notificationDao().insertNotification(NotificationEntity(id, "Alerta", "Texto", "Hoje", type = NotificationType.MEDICATION_PICKUP, phase = "UNKNOWN", targetType = "PICKUP"))
        db.notificationDao().insertNotification(NotificationEntity(id + ":valid", "Alerta", "Texto", "Hoje", type = NotificationType.MEDICATION_PICKUP, phase = "PICKUP_DUE", targetType = "PICKUP"))
        val items = RoomNotificationRepository(context, db.notificationDao()).getNotifications().first()
        assertEquals(listOf(id + ":valid"), items.map { it.id })
        assertNotNull(db.notificationDao().getNotificationById(id))
    }
    @Test fun deletingFromUiDispatcherDurablySuppressesTheCycle() = runBlocking {
        db.notificationDao().insertNotification(NotificationEntity(id, "Alerta", "Texto", "Hoje", type = NotificationType.MEDICATION_PICKUP, phase = "PICKUP_DUE", targetType = "PICKUP"))
        withContext(Dispatchers.Main) { RoomNotificationRepository(context, db.notificationDao()).deleteNotification(id) }
        assertNull(db.notificationDao().getNotificationById(id))
        assertTrue(context.getSharedPreferences("delivered_reminders", Context.MODE_PRIVATE).getBoolean("dismissed:" + id, false))
    }
}
