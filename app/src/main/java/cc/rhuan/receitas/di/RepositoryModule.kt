package cc.rhuan.receitas.di

import cc.rhuan.receitas.data.repository.DoctorRepository
import cc.rhuan.receitas.data.repository.MedicationRepository
import cc.rhuan.receitas.data.repository.NotificationRepository
import cc.rhuan.receitas.data.repository.PrescriptionRepository
import cc.rhuan.receitas.data.repository.RoomDoctorRepository
import cc.rhuan.receitas.data.repository.RoomMedicationRepository
import cc.rhuan.receitas.data.repository.RoomNotificationRepository
import cc.rhuan.receitas.data.repository.RoomPrescriptionRepository
import cc.rhuan.receitas.data.repository.PrescriptionRegistrationRepository
import cc.rhuan.receitas.data.repository.RoomPrescriptionRegistrationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRegistrationRepository(impl: RoomPrescriptionRegistrationRepository): PrescriptionRegistrationRepository = impl

    @Provides
    @Singleton
    fun provideDoctorRepository(impl: RoomDoctorRepository): DoctorRepository {
        return impl
    }

    @Provides
    @Singleton
    fun providePrescriptionRepository(impl: RoomPrescriptionRepository): PrescriptionRepository {
        return impl
    }

    @Provides
    @Singleton
    fun provideMedicationRepository(impl: RoomMedicationRepository): MedicationRepository {
        return impl
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(impl: RoomNotificationRepository): NotificationRepository {
        return impl
    }
}
