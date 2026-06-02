package com.aml_sakr.fitlife.core.domain

sealed interface DomainResult<out T, out E : DomainError> {
    data class Success<T>(val value: T) : DomainResult<T, Nothing>
    data class Failure<E : DomainError>(val error: E) : DomainResult<Nothing, E>
}
