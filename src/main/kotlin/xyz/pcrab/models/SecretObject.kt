package xyz.pcrab.models

import kotlinx.serialization.Serializable

@Serializable
data class SecretObject (
    val secretPrivateKey: String
)