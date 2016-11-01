/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file eBooleancept in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either eBooleanpress or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kotlin.internal

import kotlin.serialization.KInput
import kotlin.serialization.KOutput
import kotlin.serialization.KSerializer

object BooleanSerializer : KSerializer<Boolean> {
    override fun save(output: KOutput, obj: Boolean) = output.writeBooleanValue(obj)
    override fun load(input: KInput): Boolean = input.readBooleanValue()
}

object ByteSerializer : KSerializer<Byte> {
    override fun save(output: KOutput, obj: Byte) = output.writeByteValue(obj)
    override fun load(input: KInput): Byte = input.readByteValue()
}

object ShortSerializer : KSerializer<Short> {
    override fun save(output: KOutput, obj: Short) = output.writeShortValue(obj)
    override fun load(input: KInput): Short = input.readShortValue()
}

object IntSerializer : KSerializer<Int> {
    override fun save(output: KOutput, obj: Int) = output.writeIntValue(obj)
    override fun load(input: KInput): Int = input.readIntValue()
}

object LongSerializer : KSerializer<Long> {
    override fun save(output: KOutput, obj: Long) = output.writeLongValue(obj)
    override fun load(input: KInput): Long = input.readLongValue()
}

object FloatSerializer : KSerializer<Float> {
    override fun save(output: KOutput, obj: Float) = output.writeFloatValue(obj)
    override fun load(input: KInput): Float = input.readFloatValue()
}

object DoubleSerializer : KSerializer<Double> {
    override fun save(output: KOutput, obj: Double) = output.writeDoubleValue(obj)
    override fun load(input: KInput): Double = input.readDoubleValue()
}

object CharSerializer : KSerializer<Char> {
    override fun save(output: KOutput, obj: Char) = output.writeCharValue(obj)
    override fun load(input: KInput): Char = input.readCharValue()
}

object StringSerializer : KSerializer<String> {
    override fun save(output: KOutput, obj: String) = output.writeStringValue(obj)
    override fun load(input: KInput): String =input.readStringValue()
}
