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
    fun getElementName(index: Int): String
    fun getElementIndex(name: String): Int
}

interface KOutput {
    // top-level api (use)
    fun <T> write(obj : T, saver: KSerialSaver<T>) = saver.save(obj, this)

    // object delimiter api (override if needed to prefix/suffix repsentation)
    fun writeBegin(desc: KSerialClassDesc) {}
    fun writeEnd(desc: KSerialClassDesc) {}

    // core api (usually override)
    fun writeElement(desc: KSerialClassDesc, index: Int) {}
    fun writeValue(value: Any?)

    // type-specific api (override for performance)
    fun writeNotNullValue(value: Any) = writeValue(value)
    fun writeBooleanValue(value: Boolean) = writeNotNullValue(value)
    fun writeByteValue(value: Byte) = writeNotNullValue(value)
    fun writeShortValue(value: Short) = writeNotNullValue(value)
    fun writeIntValue(value: Int) = writeNotNullValue(value)
    fun writeLongValue(value: Long) = writeNotNullValue(value)
    fun writeFloatValue(value: Float) = writeNotNullValue(value)
    fun writeDoubleValue(value: Double) = writeNotNullValue(value)
    fun writeCharValue(value: Char) = writeNotNullValue(value)
    fun writeStringValue(value: String) = writeNotNullValue(value)
}

interface KInput {
    companion object {
        val READ_DONE = -1
        val READ_ALL = -2
    }

    // top-level api (use)
    fun <T> read(loader: KSerialLoader<T>) : T = loader.load(this)

    // object delimiter api (override if needed to prefix/suffix repsentation)
    fun readBegin(desc: KSerialClassDesc) {}
    fun readEnd(desc: KSerialClassDesc) {}

    // core api (usually override)
    fun readElement(desc: KSerialClassDesc): Int = READ_ALL
    fun readValue(): Any?

    // type-specific api (override for performance)
    fun readNotNullValue(): Any = readValue()!!
    fun readBooleanValue(): Boolean = readNotNullValue() as Boolean
    fun readByteValue(): Byte = readNotNullValue() as Byte
    fun readShortValue(): Short = readNotNullValue() as Short
    fun readIntValue(): Int = readNotNullValue() as Int
    fun readLongValue(): Long = readNotNullValue() as Long
    fun readFloatValue(): Float = readNotNullValue() as Float
    fun readDoubleValue(): Double = readNotNullValue() as Double
    fun readCharValue(): Char = readNotNullValue() as Char
    fun readStringValue(): String = readNotNullValue() as String
}

interface KSerialSaver<in T> {
    fun save(obj : T, output: KOutput)
}

interface KSerialLoader<out T> {
    fun load(input: KInput): T
}

interface KSerializer<T>: KSerialSaver<T>, KSerialLoader<T>
