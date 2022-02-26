package xyz.pcrab.models

import io.ktor.sessions.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import redis.clients.jedis.JedisPool
import java.io.ByteArrayOutputStream

class RedisStorage : SessionStorage {
    private val pool = JedisPool("redis", 6379)

    override suspend fun invalidate(id: String) {
        pool.resource.use { jedis ->
            jedis.del(id)
        }
    }

    override suspend fun <R> read(id: String, consumer: suspend (ByteReadChannel) -> R): R {
        pool.resource.use { jedis ->
            return jedis[id.toByteArray()]?.let { data -> consumer(ByteReadChannel(data)) }
                ?: throw NoSuchElementException("Session $id not found")
        }
    }

    override suspend fun write(id: String, provider: suspend (ByteWriteChannel) -> Unit) {
        coroutineScope {
            val channel = writer(Dispatchers.Unconfined, autoFlush = true) {
                provider(channel)
            }.channel
            val data = channel.readAvailable()
            pool.resource.use { jedis ->
                jedis[id.toByteArray()] = data
            }
        }
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