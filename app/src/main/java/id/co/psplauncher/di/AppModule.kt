package id.co.psplauncher.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.co.psplauncher.data.network.RemoteDataSource
import id.co.psplauncher.data.network.absensi.AbsensiApi
import id.co.psplauncher.data.network.auth.AuthApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAuthApi(remoteDataSource: RemoteDataSource) : AuthApi =
        remoteDataSource.buildApi(AuthApi::class.java)

    @Singleton
    @Provides
    fun provideAbsensiApi(remoteDataSource: RemoteDataSource): AbsensiApi {
        return remoteDataSource.buildApi(AbsensiApi::class.java)
    }
}