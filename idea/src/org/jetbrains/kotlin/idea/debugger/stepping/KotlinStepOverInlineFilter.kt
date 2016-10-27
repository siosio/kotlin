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

package org.jetbrains.kotlin.idea.debugger.stepping

import com.intellij.debugger.engine.DebugProcessImpl
import com.intellij.debugger.engine.SuspendContextImpl
import com.intellij.openapi.project.Project
import com.intellij.util.Range
import com.sun.jdi.LocalVariable
import com.sun.jdi.Location
import org.jetbrains.kotlin.idea.debugger.getLastLineNumberForLocation
import org.jetbrains.kotlin.idea.util.application.runReadAction

class KotlinStepOverInlineFilter(
        val project: Project,
        val stepOverLines: Set<Int>, val fromLine: Int,
        val inlineFunRangeVariables: List<LocalVariable>) : KotlinMethodFilter {
    override fun locationMatches(context: SuspendContextImpl, location: Location): Boolean {
        val frameProxy = context.frameProxy ?: return true

        val currentLine = runReadAction {
            getLastLineNumberForLocation(location, project)
        }?: location.lineNumber()

        if (!(stepOverLines.contains(currentLine))) {
            return currentLine != fromLine
        }

        val visibleInlineVariables = getInlineRangeLocalVariables(frameProxy)

        // Our ranges check missed exit from inline function. This is when breakpoint was in last statement of inline functions.
        // This can be observed by inline local range-variables. Absence of any means step out was done.
        return inlineFunRangeVariables.any { !visibleInlineVariables.contains(it) }
    }

    override fun locationMatches(process: DebugProcessImpl, location: Location): Boolean {
        throw IllegalStateException() // Should not be called from Kotlin hint
    }

    override fun getCallingExpressionLines(): Range<Int>? {
        throw IllegalStateException() // Should not be called from Kotlin hint
    }
}