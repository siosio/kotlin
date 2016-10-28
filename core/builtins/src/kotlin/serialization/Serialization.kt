/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kotlin.serialization

import kotlin.reflect.KClass

annotation class KSerializable(
    val name: String = "", // use the actual name by default
    val serializer: KClass<out KSerializer<*>> = KSerializer::class // tag to use default serializer by default
)

interface KSerialClassDesc {
    val name: String
    val isArray: Boolean
    fun getElementCount(value: Any?): Int
    fun getElementName(index: Int): String
    fun getElementIndex(name: String): Int
}

interface KOutput {
    // top-level api (use)
    fun <T> write(saver: KSerialSaver<T>, obj : T) = saver.save(this, obj)

    // object delimiter api (override if needed to prefix/suffix representation, nothing by default)
    fun writeBegin(desc: KSerialClassDesc) {}
    fun writeEnd(desc: KSerialClassDesc) {}

    // override for a special representation of nulls if needed (empty object by default)
    fun writeNull(desc: KSerialClassDesc) {
        writeBegin(desc)
        writeEnd(desc)
    }

    // core api (must override)
    fun writeValue(desc: KSerialClassDesc, index: Int, value: Any?)

    // type-specific api (override for performance)
    fun writeNotNullValue(desc: KSerialClassDesc, index: Int, value: Any) = writeValue(desc, index, value)
    fun writeBooleanValue(desc: KSerialClassDesc, index: Int, value: Boolean) = writeNotNullValue(desc, index, value)
    fun writeByteValue(desc: KSerialClassDesc, index: Int, value: Byte) = writeNotNullValue(desc, index, value)
    fun writeShortValue(desc: KSerialClassDesc, index: Int, value: Short) = writeNotNullValue(desc, index, value)
    fun writeIntValue(desc: KSerialClassDesc, index: Int, value: Int) = writeNotNullValue(desc, index, value)
    fun writeLongValue(desc: KSerialClassDesc, index: Int, value: Long) = writeNotNullValue(desc, index, value)
    fun writeFloatValue(desc: KSerialClassDesc, index: Int, value: Float) = writeNotNullValue(desc, index, value)
    fun writeDoubleValue(desc: KSerialClassDesc, index: Int, value: Double) = writeNotNullValue(desc, index, value)
    fun writeCharValue(desc: KSerialClassDesc, index: Int, value: Char) = writeNotNullValue(desc, index, value)
    fun writeStringValue(desc: KSerialClassDesc, index: Int, value: String) = writeNotNullValue(desc, index, value)

    // recursive serialization (override for recursion)
    fun <T> writeSerializableValue(desc: KSerialClassDesc, index: Int, value: T, saver: KSerialSaver<T>) = writeValue(desc, index, value)
}

interface KInput {
    companion object {
        val READ_DONE = -1
        val READ_ALL = -2
    }

    // top-level api (use)
    fun <T> read(loader: KSerialLoader<T>) : T = loader.load(this)

    // object delimiter api (override if needed to prefix/suffix representation)
    fun readBegin(desc: KSerialClassDesc): Boolean = true // shall return 'false' if null is encountered
    fun readEnd(desc: KSerialClassDesc) {}

    // unordered read api, override to read props in arbitrary order
    fun readElement(desc: KSerialClassDesc): Int = READ_ALL

    // core api (must override)
    fun readValue(desc: KSerialClassDesc, index: Int): Any?

    // type-specific api (override for performance)
    fun readNotNullValue(desc: KSerialClassDesc, index: Int): Any = readValue(desc, index)!!
    fun readBooleanValue(desc: KSerialClassDesc, index: Int): Boolean = readNotNullValue(desc, index) as Boolean
    fun readByteValue(desc: KSerialClassDesc, index: Int): Byte = readNotNullValue(desc, index) as Byte
    fun readShortValue(desc: KSerialClassDesc, index: Int): Short = readNotNullValue(desc, index) as Short
    fun readIntValue(desc: KSerialClassDesc, index: Int): Int = readNotNullValue(desc, index) as Int
    fun readLongValue(desc: KSerialClassDesc, index: Int): Long = readNotNullValue(desc, index) as Long
    fun readFloatValue(desc: KSerialClassDesc, index: Int): Float = readNotNullValue(desc, index) as Float
    fun readDoubleValue(desc: KSerialClassDesc, index: Int): Double = readNotNullValue(desc, index) as Double
    fun readCharValue(desc: KSerialClassDesc, index: Int): Char = readNotNullValue(desc, index) as Char
    fun readStringValue(desc: KSerialClassDesc, index: Int): String = readNotNullValue(desc, index) as String

    // recursive serialization (override for recursion)
    fun <T> readSerializableValue(desc: KSerialClassDesc, index: Int, loader: KSerialLoader<T>) = readValue(desc, index)
}

interface KSerialSaver<in T> {
    fun save(output: KOutput, obj : T)
}

interface KSerialLoader<out T> {
    fun load(input: KInput): T
}

interface KSerializer<T>: KSerialSaver<T>, KSerialLoader<T>
