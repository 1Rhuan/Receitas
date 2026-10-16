package cc.rhuan.receitas.data.local

import cc.rhuan.receitas.R

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    // Version 2 used structured doses. Keep the old table and dose history as legacy data.
    val FROM_2_TO_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE medications RENAME TO legacy_medications_v2")
            db.execSQL("""CREATE TABLE medications (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL,
                activeIngredient TEXT, form TEXT NOT NULL, dosage TEXT NOT NULL, prescribedPosology TEXT NOT NULL,
                prescribedQuantity TEXT NOT NULL, source TEXT NOT NULL, sourceName TEXT, hasRecurringPickup INTEGER NOT NULL,
                lastPickupDate TEXT, pickupIntervalDays INTEGER, pickupReminderDaysBefore INTEGER NOT NULL,
                currentStock INTEGER, stockAlertEnabled INTEGER NOT NULL, minStockThreshold INTEGER NOT NULL, availabilityNote TEXT)""")
            db.execSQL("""INSERT INTO medications SELECT id, title, NULL, form, dosage,
                doseAmount || ' a cada ' || frequencyHours || ' horas por ' || durationDays || ' dias', '',
                source, sourceName, hasRecurringPickup, lastPickupDate, pickupIntervalDays, pickupReminderDaysBefore,
                currentStock, stockAlertEnabled, minStockThreshold, availabilityNote FROM legacy_medications_v2""")
        }
    }
    val FROM_3_TO_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            val prescriptionColumns = listOf("state TEXT NOT NULL DEFAULT 'ACTIVE'", "previousPrescriptionId TEXT",
                "doctorNameSnapshot TEXT NOT NULL DEFAULT ''", "doctorRegistrationSnapshot TEXT",
                "doctorSpecialtySnapshot TEXT NOT NULL DEFAULT ''", "validityOrigin TEXT NOT NULL DEFAULT 'USER_ENTERED'", "expiresOn TEXT")
            prescriptionColumns.forEach { db.execSQL("ALTER TABLE prescriptions ADD COLUMN " + it) }
            db.execSQL("""UPDATE prescriptions SET
                doctorNameSnapshot = COALESCE((SELECT name FROM doctors WHERE doctors.id = prescriptions.doctorId OR doctors.registration = prescriptions.doctorId LIMIT 1), ''),
                doctorRegistrationSnapshot = (SELECT registration FROM doctors WHERE doctors.id = prescriptions.doctorId OR doctors.registration = prescriptions.doctorId LIMIT 1),
                doctorSpecialtySnapshot = COALESCE((SELECT specialty FROM doctors WHERE doctors.id = prescriptions.doctorId OR doctors.registration = prescriptions.doctorId LIMIT 1), '')""")
            db.execSQL("UPDATE prescriptions SET doctorId = (SELECT id FROM doctors WHERE doctors.id = prescriptions.doctorId OR doctors.registration = prescriptions.doctorId LIMIT 1)")
            db.execSQL("""CREATE TABLE prescriptions_v4 (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, type TEXT,
                validityDays INTEGER, issueDate TEXT NOT NULL, renewalReminderEnabled INTEGER NOT NULL,
                renewalReminderDaysBefore INTEGER NOT NULL, doctorId TEXT, medicationIds TEXT NOT NULL, notes TEXT, photoUri TEXT,
                state TEXT NOT NULL, previousPrescriptionId TEXT, doctorNameSnapshot TEXT NOT NULL,
                doctorRegistrationSnapshot TEXT, doctorSpecialtySnapshot TEXT NOT NULL, validityOrigin TEXT NOT NULL, expiresOn TEXT,
                FOREIGN KEY(doctorId) REFERENCES doctors(id) ON DELETE SET NULL,
                FOREIGN KEY(previousPrescriptionId) REFERENCES prescriptions(id) ON DELETE SET NULL)""")
            db.execSQL("INSERT INTO prescriptions_v4 SELECT id, title, type, validityDays, issueDate, renewalReminderEnabled, renewalReminderDaysBefore, doctorId, medicationIds, notes, photoUri, state, previousPrescriptionId, doctorNameSnapshot, doctorRegistrationSnapshot, doctorSpecialtySnapshot, validityOrigin, expiresOn FROM prescriptions")
            db.execSQL("DROP TABLE prescriptions")
            db.execSQL("ALTER TABLE prescriptions_v4 RENAME TO prescriptions")
            db.execSQL("CREATE INDEX index_prescriptions_doctorId ON prescriptions(doctorId)")
            db.execSQL("CREATE INDEX index_prescriptions_previousPrescriptionId ON prescriptions(previousPrescriptionId)")
            // Normalize legacy aliases while preserving the confirmed validityDays.
            db.execSQL("UPDATE prescriptions SET type = CASE type WHEN 'ANTIMICROBIAL' THEN 'ANTIBIOTICO' WHEN 'SPECIAL_CONTROL' THEN 'CONTROLADO' WHEN 'PFPB' THEN 'FARMACIA_POPULAR' ELSE type END")
            db.execSQL("""UPDATE prescriptions SET expiresOn = date(issueDate, '+' || COALESCE(validityDays,
                CASE type WHEN 'ANTIBIOTICO' THEN 10 WHEN 'CONTROLADO' THEN 30 ELSE 180 END) || ' days')""")
            db.execSQL("""CREATE TABLE prescription_medications (prescriptionId TEXT NOT NULL, medicationId TEXT NOT NULL,
                posology TEXT NOT NULL, quantity TEXT NOT NULL, titleSnapshot TEXT NOT NULL, dosageSnapshot TEXT NOT NULL, formSnapshot TEXT NOT NULL, PRIMARY KEY(prescriptionId, medicationId),
                FOREIGN KEY(prescriptionId) REFERENCES prescriptions(id) ON DELETE CASCADE,
                FOREIGN KEY(medicationId) REFERENCES medications(id) ON DELETE RESTRICT)""")
            db.execSQL("CREATE INDEX index_prescription_medications_medicationId ON prescription_medications(medicationId)")
            db.query("SELECT id, medicationIds FROM prescriptions").use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getString(0)
                    cursor.getString(1)?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }?.distinct()?.forEach { med ->
                        db.execSQL("INSERT OR IGNORE INTO prescription_medications SELECT ?, id, prescribedPosology, prescribedQuantity, title, dosage, form FROM medications WHERE id = ?", arrayOf(id, med))
                    }
                }
            }
            db.execSQL("""CREATE TABLE pickup_tracking (id TEXT NOT NULL PRIMARY KEY, medicationId TEXT NOT NULL,
                prescriptionId TEXT, enabled INTEGER NOT NULL, archived INTEGER NOT NULL, intervalAmount INTEGER NOT NULL,
                intervalUnit TEXT NOT NULL, reminderDaysBefore INTEGER NOT NULL, source TEXT NOT NULL, sourceName TEXT,
                quantity TEXT NOT NULL, note TEXT, FOREIGN KEY(medicationId) REFERENCES medications(id) ON DELETE CASCADE,
                FOREIGN KEY(prescriptionId) REFERENCES prescriptions(id) ON DELETE SET NULL)""")
            db.execSQL("CREATE UNIQUE INDEX index_pickup_tracking_medicationId ON pickup_tracking(medicationId)")
            db.execSQL("CREATE INDEX index_pickup_tracking_prescriptionId ON pickup_tracking(prescriptionId)")
            // No inferred recipe: the user selects it after migration.
            db.execSQL("""INSERT INTO pickup_tracking SELECT 'tracking:' || id, id, NULL, hasRecurringPickup, 0,
                CASE WHEN pickupIntervalDays BETWEEN 1 AND 3650 THEN pickupIntervalDays ELSE 30 END,
                'DAYS', MAX(0, pickupReminderDaysBefore), source, sourceName, prescribedQuantity, availabilityNote FROM medications""")
            db.execSQL("""CREATE TABLE pickup_events (id TEXT NOT NULL PRIMARY KEY, trackingId TEXT NOT NULL,
                occurredOn TEXT NOT NULL, recordedAt INTEGER NOT NULL,
                FOREIGN KEY(trackingId) REFERENCES pickup_tracking(id) ON DELETE CASCADE)""")
            db.execSQL("CREATE UNIQUE INDEX index_pickup_events_trackingId_occurredOn ON pickup_events(trackingId, occurredOn)")
            db.execSQL("INSERT INTO pickup_events SELECT 'legacy:' || id, 'tracking:' || id, lastPickupDate, 0 FROM medications WHERE lastPickupDate IS NOT NULL")
            listOf("phase TEXT", "targetType TEXT", "cycleKey TEXT", "resolvedAt INTEGER", "resolutionReason TEXT").forEach { db.execSQL("ALTER TABLE notifications ADD COLUMN " + it) }
            db.query("SELECT id, actionTaken FROM notifications").use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getString(0)
                    val parts = id.split(':')
                    if (parts.size < 4 || parts.first() !in listOf("prescription", "pickup")) continue
                    val phase = when (parts.last()) { "renewal" -> "RENEWAL"; "today" -> "EXPIRES_TODAY"; "expired" -> "EXPIRED"; "upcoming" -> "PICKUP_UPCOMING"; "due" -> "PICKUP_DUE"; else -> null }
                    db.execSQL("UPDATE notifications SET phase = ?, targetType = ?, cycleKey = ?, resolutionReason = ? WHERE id = ?",
                        // This is a legacy stored value, not a display string.
                        arrayOf(phase, parts.first().uppercase(), parts.dropLast(1).joinToString(":"), if (cursor.getString(1) == "Lembrete encerrado") "OBSOLETE" else null, id))
                }
            }
        }
    }
}
