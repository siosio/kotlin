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

@file:JvmName("KSerializationUtil")

package org.jetbrains.kotlin.descriptors

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.types.KotlinType

val Annotations.serializer: KotlinType?
    get() = findAnnotation(KotlinBuiltIns.FQ_NAMES.kSerializable)?.let { annotation ->
        annotation.allValueArguments.entries.singleOrNull { it.key.name.asString() == "serializer" }?.value?.let { value ->
            value.value as? KotlinType
        }
    }

// serializer that was declared for original class
@get:JvmName("getClassSerializer")
val KotlinType?.classSerializer: KotlinType?
    get() = this?.let {
        // lookup type's class descriptor
        val descriptor = it.constructor.declarationDescriptor as? ClassDescriptor ?: return null
        // serializer annotation on class?
        descriptor.annotations.serializer?.let { return it }
        // default serializable?
        if (descriptor.isDefaultSerializable) return descriptor.companionObjectDescriptor?.defaultType
        return null
    }

// serializer that was declared for this specific type
val KotlinType?.typeSerializer: KotlinType?
    get() = this?.let {
        // serializer annotation on this type?
        return it.annotations.serializer
    }

// serializer that was declared specifically for this property via its own annotation or via annotation on its type
@get:JvmName("getPropertySerializer")
val PropertyDescriptor.propertySerializer: KotlinType?
    get() = annotations.serializer ?: type.typeSerializer

@get:JvmName("isDefaultSerializable")
val ClassDescriptor.isDefaultSerializable: Boolean
    get() = annotations.hasAnnotation(KotlinBuiltIns.FQ_NAMES.kSerializable) && annotations.serializer == null

val ClassDescriptor.serializableProperties: List<PropertyDescriptor>
    get() = unsubstitutedMemberScope.getContributedDescriptors(DescriptorKindFilter.VARIABLES)
            .filterIsInstance<PropertyDescriptor>()

fun getSerializableClassDescriptor(companionDescriptor: ClassDescriptor) : ClassDescriptor? {
    if (!companionDescriptor.isCompanionObject) return null
    val classDescriptor = (companionDescriptor.containingDeclaration as? ClassDescriptor) ?: return null
    if (!classDescriptor.isDefaultSerializable) return null
    return classDescriptor
}

fun isSerializerCompanion(companionDescriptor: ClassDescriptor) : Boolean = getSerializableClassDescriptor(companionDescriptor) != null

// todo: serialization: do an actual check better that just number of parameters
fun ClassDescriptor.checkSaveMethodParameters(parameters: List<ValueParameterDescriptor>) : Boolean =
        parameters.size == 2

fun ClassDescriptor.checkSaveMethodResult(type: KotlinType) : Boolean =
        KotlinBuiltIns.isUnit(type)

// todo: serialization: do an actual check better that just number of parameters
fun ClassDescriptor.checkLoadMethodParameters(parameters: List<ValueParameterDescriptor>) : Boolean =
        parameters.size == 1

// todo: serialization: do an actual check
fun ClassDescriptor.checkLoadMethodResult(type: KotlinType) : Boolean =
        true
