package dev.beefers.vendetta.manager.network.utils

import io.ktor.http.HttpStatusCode

sealed interface ApiResponse<D> {
    data class Success<D>(val data: D) : ApiResponse<D>
    data class Error<D>(val error: ApiError) : ApiResponse<D>
    data class Failure<D>(val error: ApiFailure) : ApiResponse<D>
}

class ApiError(code: HttpStatusCode, body: String?) : Error("HTTP Code $code, Body: $body")
class ApiFailure(error: Throwable, body: String?) : Error(body, error)

val <D> ApiResponse<D>.dataOrNull
    get() = if (this is ApiResponse.Success) data else null

val <D> ApiResponse<D>.dataOrThrow
    get() = when (this) {
        is ApiResponse.Success -> data
        is ApiResponse.Error -> throw error
        is ApiResponse.Failure -> throw error
    }

inline fun <D> ApiResponse<D>.ifSuccessful(block: (D) -> Unit) {
    if (this is ApiResponse.Success) block(data)
}