package com.chatgptlite.wanted.data.llm

import com.chatgptlite.wanted.models.TextCompletionsParam
import kotlinx.coroutines.flow.Flow

interface AIRepository {
    fun textCompletionsWithStream(params: TextCompletionsParam): Flow<String>
}

interface OpenAIRepository: AIRepository

interface MlcLLMRepository: AIRepository