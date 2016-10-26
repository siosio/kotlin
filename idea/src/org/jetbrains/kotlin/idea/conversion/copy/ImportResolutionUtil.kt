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

package org.jetbrains.kotlin.idea.conversion.copy

import com.intellij.psi.*
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.descriptors.DeclarationDescriptorWithVisibility
import org.jetbrains.kotlin.idea.caches.resolve.analyzeFully
import org.jetbrains.kotlin.idea.caches.resolve.getJavaMemberDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.getResolutionFacade
import org.jetbrains.kotlin.idea.caches.resolve.resolveToDescriptor
import org.jetbrains.kotlin.idea.codeInsight.referenceExpression
import org.jetbrains.kotlin.idea.core.isVisible
import org.jetbrains.kotlin.idea.imports.canBeReferencedViaImport
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.idea.util.application.runWriteAction
import org.jetbrains.kotlin.platform.JavaToKotlinClassMap
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import java.util.*


class ImportResolutionUtil(dataForConversion: DataForConversion, val targetFile: KtFile) {

    private val file = dataForConversion.file
    private val project = targetFile.project

    private val importList = file.importList!!
    private val psiElementFactory = PsiElementFactory.SERVICE.getInstance(project)

    private val bindingContext by lazy { targetFile.analyzeFully() }
    private val resolutionFacade = targetFile.getResolutionFacade()

    private val shortNameCache = PsiShortNamesCache.getInstance(project)
    private val scope = file.resolveScope

    val failedToResolveReferenceNames = HashSet<String>()
    var ambiguityInResolution = false
    var couldNotResolve = false

    val addedImports = ArrayList<PsiImportStatementBase>()

    private fun canBeImported(p: Pair<PsiElement, DeclarationDescriptorWithVisibility?>): Boolean {
        val desc = p.second
        return desc != null
               && desc.canBeReferencedViaImport()
               && desc.isVisible(targetFile, null, bindingContext, resolutionFacade)
    }

    private fun addImport(importStatement: PsiImportStatementBase, shouldAddToTarget: Boolean = false) {
        importList.add(importStatement)
        if (shouldAddToTarget)
            addedImports.add(importStatement)
    }

    fun addImportsFromTargetFile() {

        fun tryConvertKotlinImport(it: KtImportDirective) {
            val importPath = it.importPath
            val importedReference = it.importedReference
            if (importPath != null && !importPath.hasAlias()) {
                if (importedReference is KtDotQualifiedExpression) {
                    val receiver = importedReference
                            .receiverExpression
                            .referenceExpression()
                            ?.mainReference
                            ?.resolve()
                    val selector = importedReference
                            .selectorExpression
                            ?.referenceExpression()
                            ?.mainReference
                            ?.resolve()

                    val isPackageReceiver = receiver is PsiPackage
                    val isClassReceiver = receiver is PsiClass
                    val isClassSelector = selector is PsiClass

                    if (importPath.isAllUnder) {
                        if (isClassReceiver)
                            psiElementFactory.createImportStaticStatement(receiver as PsiClass, "*")
                        else if (isPackageReceiver)
                            psiElementFactory.createImportStatementOnDemand((receiver as PsiPackage).qualifiedName)
                    }
                    else {
                        if (isPackageReceiver && isClassSelector)
                            psiElementFactory.createImportStatement(selector as PsiClass)
                        else if (isClassReceiver)
                            psiElementFactory.createImportStaticStatement(receiver as PsiClass, importPath.importedName!!.asString())
                    }
                }
            }
        }
        runWriteAction {
            targetFile.importDirectives.forEach(::tryConvertKotlinImport)
        }
    }

    fun tryResolveReferences() {

        val elementsWithUnresolvedRef = PsiTreeUtil.collectElements(file) {
            it.reference != null
            && it.reference is PsiQualifiedReference
            && it.reference?.resolve() == null
        }

        fun tryResolveReference(reference: PsiQualifiedReference): Boolean {
            if (reference.resolve() != null) return true
            val referenceName = reference.referenceName!!
            if (failedToResolveReferenceNames.contains(referenceName)) return false
            val classes = shortNameCache.getClassesByName(referenceName, scope)
                    .map { it to it.resolveToDescriptor(resolutionFacade) }
                    .filter(this::canBeImported)

            classes.find { (psiClass, descriptor) -> JavaToKotlinClassMap.INSTANCE.mapPlatformClass(descriptor!!).isNotEmpty() }
                    ?.let { (psiClass, descriptor) -> addImport(psiElementFactory.createImportStatement(psiClass)) }

            if (reference.resolve() != null) return true
            classes.singleOrNull()?.let { (psiClass, descriptor) ->
                addImport(psiElementFactory.createImportStatement(psiClass), true)
            }

            if (reference.resolve() != null) {
                //println("Succeed resolving import ${(reference.resolve()!! as PsiClass).qualifiedName}")
                return true
            }
            else {
                if (classes.isNotEmpty()) {
                    //println("Failed resolving import ${reference.canonicalText} due ambiguity")
                    //classes.forEach { println(it.first.qualifiedName) }
                    ambiguityInResolution = true
                    return false
                }
            }

            val members = (shortNameCache.getMethodsByName(referenceName, scope).asList() +
                           shortNameCache.getFieldsByName(referenceName, scope).asList())
                    .map { it as PsiMember }
                    .map { it to it.getJavaMemberDescriptor(resolutionFacade) as? DeclarationDescriptorWithVisibility }
                    .filter(this::canBeImported)

            members.singleOrNull()?.let { (psiMember, descriptor) ->
                addImport(psiElementFactory.createImportStaticStatement(psiMember.containingClass!!, psiMember.name!!), true)
            }

            if (reference.resolve() != null) {
                //val member = (reference.resolve()!! as PsiMember)
                //println("Succeed resolving import static ${member.containingClass!!.qualifiedName}.${member.name}")
                return false
            }
            else {
                if (members.isNotEmpty()) {
                    //println("Failed resolving import static ${reference.canonicalText} due ambiguity")
                    //members.forEach { println("${it.first.containingClass!!.qualifiedName}.${it.first.name}") }
                    ambiguityInResolution = true
                }
                else {
                    //println("Failed resolving import ${reference.canonicalText}")
                    couldNotResolve = true
                }
            }
            return false
        }

        runWriteAction {
            elementsWithUnresolvedRef.forEach {
                val reference = it.reference as PsiQualifiedReference
                if (!tryResolveReference(reference)) failedToResolveReferenceNames += reference.referenceName!!
            }
        }
    }
}