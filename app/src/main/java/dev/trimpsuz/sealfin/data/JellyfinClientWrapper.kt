package dev.trimpsuz.sealfin.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.HttpClientOptions
import org.jellyfin.sdk.api.client.extensions.authenticateUserByName
import org.jellyfin.sdk.api.client.extensions.userApi
import org.jellyfin.sdk.createJellyfin
import org.jellyfin.sdk.model.ClientInfo
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class JellyfinClientWrapper @Inject constructor(
    private val dataStore: DataStoreManager,
    @ApplicationContext private val androidContext: Context
) {
    private val jellyfin = createJellyfin {
        clientInfo = ClientInfo(name = "Sealfin", version = "0.1")
        context = androidContext
    }

    suspend fun authenticate(
        serverUrl: String,
        username: String,
        password: String
    ): Result<Pair<String, String>> {
        return try {
            val api = jellyfin.createApi(baseUrl = serverUrl)
            val authResult = api.userApi.authenticateUserByName(
                username = username,
                password = password
            )

            val token = authResult.content.accessToken
                ?: return Result.failure(Exception("No access token returned"))
            val userId = authResult.content.user?.id
                ?: return Result.failure(Exception("No user id returned"))

            Result.success(token to userId.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun createApiClient(serverUrl: String, accessToken: String? = null): ApiClient {
        return if (accessToken != null) {
            jellyfin.createApi(
                baseUrl = serverUrl,
                accessToken = accessToken,
                httpClientOptions = HttpClientOptions(
                    requestTimeout = 30_000L.toDuration(DurationUnit.MILLISECONDS),
                    connectTimeout = 6_000L.toDuration(DurationUnit.MILLISECONDS),
                    socketTimeout = 10_000L.toDuration(DurationUnit.MILLISECONDS),
                ),
            )
        } else {
            jellyfin.createApi(baseUrl = serverUrl)
        }
    }
}
