package com.example.sanaesencia_app.di

import android.content.Context
import com.example.sanaesencia_app.data.local.AppDatabase
import com.example.sanaesencia_app.data.repository.SanaesenciaRepositoryImpl
import com.example.sanaesencia_app.domain.repository.SanaesenciaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AppContainer {
    @Volatile private var repositoryInstance: SanaesenciaRepository? = null

    fun repository(context: Context): SanaesenciaRepository = repositoryInstance ?: synchronized(this) {
        repositoryInstance ?: SanaesenciaRepositoryImpl(AppDatabase.getDatabase(context)).also { repositoryInstance = it }
    }
}
