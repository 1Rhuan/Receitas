package cc.rhuan.receitas.di

import android.content.Context
import cc.rhuan.receitas.data.local.AppDatabase
import cc.rhuan.receitas.data.local.dao.DoctorDao
import cc.rhuan.receitas.data.local.dao.NotificationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideDoctorDao(database: AppDatabase): DoctorDao {
        return database.doctorDao()
    }

    @Provides
    fun provideNotificationDao(database: AppDatabase): NotificationDao {
        return database.notificationDao()
    }
}
