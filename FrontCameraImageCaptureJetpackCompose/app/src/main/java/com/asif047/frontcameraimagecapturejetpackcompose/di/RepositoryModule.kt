package com.asif047.frontcameraimagecapturejetpackcompose.di

import com.asif047.frontcameraimagecapturejetpackcompose.data.UploadRepositoryImpl
import com.asif047.frontcameraimagecapturejetpackcompose.domain.UploadRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUploadRepository(
        uploadRepositoryImpl: UploadRepositoryImpl
    ): UploadRepository
}
