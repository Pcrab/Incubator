package xyz.pcrab.models

import io.ktor.sessions.*
import io.ktor.utils.io.*
import io.lettuce.core.RedisClient
import io.lettuce.core.api.sync.RedisCommands
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope.coroutineContext
import java.io.ByteArrayOutputStream

val sync: RedisCommands<String, String> = RedisClient.create("redis://localhost:6379").connect().sync()

class RedisStorage : SessionStorage {
    private fun read(id: String): ByteArray? {
        return sync.get(id)?.toByteArray()
    }

    private fun write(id: String, data: ByteArray?) {
        if (data != null) {
            sync.set(id, data.joinToString())
        } else {
            throw Exception("need data to write")
        }
    }

    override suspend fun invalidate(id: String) {
        write(id, null)
    }

    override suspend fun <R> read(id: String, consumer: suspend (ByteReadChannel) -> R): R {
        val data = read(id) ?: throw NoSuchElementException("Session $id not found")
        println(data)
        return consumer(ByteReadChannel(data))
    }

    override suspend fun write(id: String, provider: suspend (ByteWriteChannel) -> Unit) {
        return provider(CoroutineScope(Dispatchers.IO).reader(coroutineContext, autoFlush = true) {
            write(id, channel.readAvailable())
        }.channel)
    }
}

suspend fun ByteReadChannel.readAvailable(): ByteArray {
    val data = ByteArrayOutputStream()
    val temp = ByteArray(1024)
    while (!isClosedForRead) {
        val read = readAvailable(temp)
        if (read <= 0) break
        data.write(temp, 0, read)
    }
    return data.toByteArray()
}