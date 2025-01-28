package app.atomofiron.fileseeker.service

import app.atomofiron.common.util.human
import app.atomofiron.searchboxapp.model.network.GithubError
import app.atomofiron.searchboxapp.model.network.GithubRelease
import app.atomofiron.searchboxapp.model.network.Loading
import app.atomofiron.searchboxapp.utils.Rslt
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentLength
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.io.readByteArray
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import java.io.File

private const val RELEASES = "https://api.github.com/repos/atomofiron/android-file-seeker/releases"

private fun HttpStatusCode.ok() = value in 200..299


class UpdateApi {

    @OptIn(ExperimentalSerializationApi::class)
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                namingStrategy = JsonNamingStrategy.SnakeCase
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun releases(): Rslt<List<GithubRelease>> {
        val response = try {
            client.get(RELEASES)
        } catch (t: Throwable) {
            return Rslt.Err(t.human())
        }
        return when {
            response.status.ok() -> Rslt.Ok(response.body<List<GithubRelease>>())
            else -> Rslt.Err(response.body<GithubError>().run { "[$status] $message" })
        }
    }

    fun download(url: String, dst: File): Flow<Loading> = flow {
        emit(Loading.Progress.Indeterminate)
        try {
            if (!dst.exists()) {
                dst.parentFile?.mkdirs()
                dst.createNewFile()
            }
            client.prepareGet(url).execute { response ->
                val contentLength = response.contentLength() ?: 0
                val channel = response.body<ByteReadChannel>()
                while (!channel.isClosedForRead) {
                    val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                    while (!packet.exhausted()) {
                        dst.appendBytes(packet.readByteArray())
                    }
                    emit(Loading.Progress(dst.length(), contentLength))
                }
                when (val cause = channel.closedCause) {
                    null -> emit(Loading.Completed)
                    else -> emit(Loading.Error(cause.human()))
                }
            }
        } catch (t: Throwable) {
            emit(Loading.Error(t.human()))
        } catch (e: Exception) {
            emit(Loading.Error(e.human()))
        }
    }
}
