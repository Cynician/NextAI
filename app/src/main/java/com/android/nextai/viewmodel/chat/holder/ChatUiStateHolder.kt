package com.android.nextai.viewmodel.chat.holder

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ChatUiStateHolder @Inject constructor(
    @ApplicationContext context: Context,
) {

}