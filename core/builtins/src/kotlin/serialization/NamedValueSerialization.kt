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

open class NamedValueOutput : KOutput {
    // ------- API (override it) -------

    fun writeNamed(name: String, value: Any) { throw SerializationException("value is not supported") }
    fun writeNamedNull(name: String) { throw SerializationException("null is not supported") }

    fun writeNamedNullable(name: String, value: Any?) {
        if (value == null) writeNamedNull(name) else writeNamed(name, value)
    }
    
    fun writeNamedBoolean(name: String, value: Boolean) = writeNamed(name, value)
    fun writeNamedByte(name: String, value: Byte) = writeNamed(name, value)
    fun writeNamedShort(name: String, value: Short) = writeNamed(name, value)
    fun writeNamedInt(name: String, value: Int) = writeNamed(name, value)
    fun writeNamedLong(name: String, value: Long) = writeNamed(name, value)
    fun writeNamedFloat(name: String, value: Float) = writeNamed(name, value)
    fun writeNamedDouble(name: String, value: Double) = writeNamed(name, value)
    fun writeNamedChar(name: String, value: Char) = writeNamed(name, value)
    fun writeNamedString(name: String, value: String) = writeNamed(name, value)

    fun <T: Any> writeNamedSerializable(name: String, saver: KSerialSaver<T>, value: T)
            = writeNamed(name, value)

    fun <T: Any> writeNamedNullableSerializable(name: String, saver: KSerialSaver<T>, value: T?)
            = if (value == null) writeNamedNull(name) else writeNamedSerializable(name, saver, value)

    // ------- implementation -------

    private var lastDesc: KSerialClassDesc? = null
    private var lastIndex: Int = -1

    override fun writeElement(desc: KSerialClassDesc, index: Int) {
        lastDesc = desc
        lastIndex = index
    }

    override fun writeValue(value: Any) { writeElementValue(lastDesc!!, lastIndex, value) }

    override fun writeNullableValue(value: Any?) { writeNullableElementValue(lastDesc!!, lastIndex, value) }

    override fun writeBooleanValue(value: Boolean) { writeNullableElementValue(lastDesc!!, lastIndex, value) }
    override fun writeByteValue(value: Byte) { writeNullableElementValue(lastDesc!!, lastIndex, value) }
    override fun writeShortValue(value: Short) { writeNullableElementValue(lastDesc!!, lastIndex, value) }
    override fun writeIntValue(value: Int) { writeNullableElementValue(lastDesc!!, lastIndex, value) }
    override fun writeLongValue(value: Long) { writeNullableElementValue(lastDesc!!, lastIndex, value) }
    override fun writeFloatValue(value: Float) { writeNullableElementValue(lastDesc!!, lastIndex, value) }
    override fun writeDoubleValue(value: Double) { writeNullableElementValue(lastDesc!!, lastIndex, value) }
    override fun writeCharValue(value: Char) { writeNullableElementValue(lastDesc!!, lastIndex, value) }
    override fun writeStringValue(value: String) { writeNullableElementValue(lastDesc!!, lastIndex, value) }

    override fun <T: Any> writeSerializableValue(saver: KSerialSaver<T>, value: T) {
        writeSerializableElementValue(lastDesc!!, lastIndex, saver, value)
    }

    override fun <T: Any> writeNullableSerializableValue(saver: KSerialSaver<T>, value: T?) {
        writeNullableSerializableElementValue(lastDesc!!, lastIndex, saver, value)
    }

    override fun writeNullValue() {
        writeNullableElementValue(lastDesc!!, lastIndex, null)
    }

    // ---------------

    private fun name(desc: KSerialClassDesc, index: Int) = desc.getElementName(index)

    override fun writeElementValue(desc: KSerialClassDesc, index: Int, value: Any) = writeNamed(name(desc, index), value)
    override fun writeNullableElementValue(desc: KSerialClassDesc, index: Int, value: Any?) = writeNamedNullable(name(desc, index), value)
    override fun writeBooleanElementValue(desc: KSerialClassDesc, index: Int, value: Boolean) = writeNamedBoolean(name(desc, index), value)
    override fun writeByteElementValue(desc: KSerialClassDesc, index: Int, value: Byte) = writeNamedByte(name(desc, index), value)
    override fun writeShortElementValue(desc: KSerialClassDesc, index: Int, value: Short) = writeNamedShort(name(desc, index), value)
    override fun writeIntElementValue(desc: KSerialClassDesc, index: Int, value: Int) = writeNamedInt(name(desc, index), value)
    override fun writeLongElementValue(desc: KSerialClassDesc, index: Int, value: Long) = writeNamedLong(name(desc, index), value)
    override fun writeFloatElementValue(desc: KSerialClassDesc, index: Int, value: Float) = writeNamedFloat(name(desc, index), value)
    override fun writeDoubleElementValue(desc: KSerialClassDesc, index: Int, value: Double) = writeNamedDouble(name(desc, index), value)
    override fun writeCharElementValue(desc: KSerialClassDesc, index: Int, value: Char) = writeNamedChar(name(desc, index), value)
    override fun writeStringElementValue(desc: KSerialClassDesc, index: Int, value: String) = writeNamedString(name(desc, index), value)

    override fun <T: Any> writeSerializableElementValue(desc: KSerialClassDesc, index: Int, saver: KSerialSaver<T>, value: T)
            = writeNamedSerializable(name(desc, index), saver, value)

    override fun <T: Any> writeNullableSerializableElementValue(desc: KSerialClassDesc, index: Int, saver: KSerialSaver<T>, value: T?)
            = writeNamedNullableSerializable(name(desc, index), saver, value)
}

object NamedValueInput : KInput {
    private var lastDesc: KSerialClassDesc? = null
    private var lastIndex: Int = -1

    // todo:

}