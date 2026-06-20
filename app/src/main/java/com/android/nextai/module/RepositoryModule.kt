package com.android.nextai.module
import com.android.nextai.data.repository.ChatRepositoryImpl
import com.android.nextai.data.repository.ProviderRepositoryImpl
import com.android.nextai.domain.repository.ChatRepository
import com.android.nextai.domain.repository.ProviderRepository
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
    abstract fun bindChatRepository(
        chatRepository: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindProviderRepository(
        providerRepository: ProviderRepositoryImpl
    ): ProviderRepository
}