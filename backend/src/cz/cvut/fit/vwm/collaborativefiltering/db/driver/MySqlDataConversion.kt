package cz.cvut.fit.vwm.collaborativefiltering.db.driver

import org.jetbrains.squash.drivers.JDBCDataConversion
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.*
import kotlin.reflect.KClass

/**
 * Almost the same as source (https://github.com/orangy/squash/tree/master/squash-mysql),
 * but added BigInteger conversion.
 */
class MySqlDataConversion : JDBCDataConversion() {
    override fun convertValueToDatabase(value: Any?): Any? {
        if (value is UUID) {
            val bb = ByteBuffer.wrap(ByteArray(16))
            bb.putLong(value.mostSignificantBits)
            bb.putLong(value.leastSignificantBits)
            return bb.array()
        }
        return super.convertValueToDatabase(value)
    }

    override fun convertValueFromDatabase(value: Any?, type: KClass<*>): Any? {
        return when {
            value is ByteArray && type == UUID::class -> {
                val bb = ByteBuffer.wrap(value)
                return UUID(bb.getLong(0), bb.getLong(8))
            }
            value is BigInteger && type.javaObjectType == Int::class.javaObjectType -> value.toInt()
            value is BigInteger && type.javaObjectType == Long::class.javaObjectType -> value.toLong()
            else -> super.convertValueFromDatabase(value, type)
        }
    }
}