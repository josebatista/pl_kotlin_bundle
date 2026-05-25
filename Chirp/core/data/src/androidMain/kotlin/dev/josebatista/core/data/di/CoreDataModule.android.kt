package dev.josebatista.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.josebatista.core.data.auth.createDataStore
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformCoreDataModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<DataStore<Preferences>> { createDataStore(androidContext()) }
}
