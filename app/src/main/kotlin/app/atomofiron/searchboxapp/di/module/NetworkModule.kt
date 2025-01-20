package app.atomofiron.searchboxapp.di.module

import app.atomofiron.searchboxapp.injectable.network.UpdateApi
import dagger.Module
import dagger.Provides
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.engine.cio.CIO
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import javax.inject.Singleton

@Module
open class NetworkModule {

    @Provides
    @Singleton
    open fun httpClient() = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                namingStrategy = JsonNamingStrategy.SnakeCase
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    @Provides
    @Singleton
    open fun updateApi(client: HttpClient) = UpdateApi(client)
}
