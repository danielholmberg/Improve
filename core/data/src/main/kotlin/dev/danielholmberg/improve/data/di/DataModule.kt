package dev.danielholmberg.improve.data.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.danielholmberg.improve.data.repository.AuthRepositoryImpl
import dev.danielholmberg.improve.data.repository.CompanyRepositoryImpl
import dev.danielholmberg.improve.data.repository.ContactRepositoryImpl
import dev.danielholmberg.improve.data.repository.DriveRepositoryImpl
import dev.danielholmberg.improve.data.repository.FeedbackRepositoryImpl
import dev.danielholmberg.improve.data.repository.NoteRepositoryImpl
import dev.danielholmberg.improve.data.repository.StorageRepositoryImpl
import dev.danielholmberg.improve.data.repository.TagRepositoryImpl
import dev.danielholmberg.improve.domain.repository.AuthRepository
import dev.danielholmberg.improve.domain.repository.CompanyRepository
import dev.danielholmberg.improve.domain.repository.ContactRepository
import dev.danielholmberg.improve.domain.repository.DriveRepository
import dev.danielholmberg.improve.domain.repository.FeedbackRepository
import dev.danielholmberg.improve.domain.repository.NoteRepository
import dev.danielholmberg.improve.domain.repository.StorageRepository
import dev.danielholmberg.improve.domain.repository.TagRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository

    @Binds
    @Singleton
    abstract fun bindCompanyRepository(impl: CompanyRepositoryImpl): CompanyRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindFeedbackRepository(impl: FeedbackRepositoryImpl): FeedbackRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(impl: StorageRepositoryImpl): StorageRepository

    @Binds
    @Singleton
    abstract fun bindDriveRepository(impl: DriveRepositoryImpl): DriveRepository

    companion object {

        @Provides
        @Singleton
        fun provideFirebaseDatabase(): FirebaseDatabase =
            FirebaseDatabase.getInstance()

        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth =
            FirebaseAuth.getInstance()

        @Provides
        @Singleton
        fun provideFirebaseStorage(): FirebaseStorage =
            FirebaseStorage.getInstance()
    }
}
