package com.chatgptlite.wanted.di

import com.chatgptlite.wanted.data.llm.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun conversationRepository(
        repo: ConversationRepositoryImpl
    ): ConversationRepository

    @Binds
    abstract fun messageRepository(
        repo: MessageRepositoryImpl
    ): MessageRepository

    @Binds
    abstract fun openAIRepository(
        repo: OpenAIRepositoryImpl
    ): OpenAIRepository

    @Binds
    abstract fun mlcRepository(
        repo: MlcLLMRepositoryImpl
<<<<<<< HEAD
    ): OpenAIRepository
=======
    ): MlcLLMRepository
>>>>>>> 4b4a9d75465b1d09f58be13761be645a366765ee

}
