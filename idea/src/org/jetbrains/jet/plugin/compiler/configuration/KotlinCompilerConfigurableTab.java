/*
 * Copyright 2010-2013 JetBrains s.r.o.
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

package org.jetbrains.jet.plugin.compiler.configuration;

import com.intellij.compiler.options.ComparingUtils;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableEP;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.RawCommandLineEditor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.cli.common.arguments.CommonCompilerArguments;
import org.jetbrains.jet.cli.common.arguments.K2JSCompilerArguments;
import org.jetbrains.jet.compiler.AdditionalCompilerSettings;
import org.jetbrains.jet.plugin.JetBundle;

import javax.swing.*;

import static org.jetbrains.jet.cli.common.arguments.CommonArgumentConstants.SUPPRESS_WARNINGS;

public class KotlinCompilerConfigurableTab implements SearchableConfigurable, Configurable.NoScroll{
    private final CommonCompilerArguments commonCompilerSettings;
    private final K2JSCompilerArguments k2jsCompilerSettings;
    private final AdditionalCompilerSettings additionalCompilerSettings;
    private final ConfigurableEP extPoint;
    private JPanel contentPane;
    private JCheckBox generateNoWarningsCheckBox;
    private RawCommandLineEditor additionalArgsOptionsField;
    private JLabel additionalArgsLabel;
    private JCheckBox generateSourceMapsCheckBox;
    private TextFieldWithBrowseButton outputPrefixFile;
    private TextFieldWithBrowseButton outputPostfixFile;
    private JLabel labelForOutputPrefixFile;
    private JLabel labelForOutputPostfixFile;

    public KotlinCompilerConfigurableTab(ConfigurableEP ep) {
        this.extPoint = ep;
        this.commonCompilerSettings = KotlinCommonCompilerSettings.getInstance(ep.getProject()).getSettings();
        this.k2jsCompilerSettings = Kotlin2JsCompilerSettings.getInstance(ep.getProject()).getSettings();
        this.additionalCompilerSettings = KotlinAdditionalCompilerSettings.getInstance(ep.getProject()).getSettings();

        additionalArgsOptionsField.attachLabel(additionalArgsLabel);
        additionalArgsOptionsField.setDialogCaption(JetBundle.message("kotlin.compiler.option.additional.command.line.parameters.dialog.title"));
        
        setupFileChooser(labelForOutputPrefixFile, outputPrefixFile,
                         JetBundle.message("kotlin.compiler.js.option.output.prefix.browse.title"));
        setupFileChooser(labelForOutputPostfixFile, outputPostfixFile,
                         JetBundle.message("kotlin.compiler.js.option.output.postfix.browse.title"));
    }

    @NotNull
    @Override
    public String getId() {
        return extPoint.id;
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return contentPane;
    }

    @Override
    public boolean isModified() {
        return ComparingUtils.isModified(generateNoWarningsCheckBox, isGenerateNoWarnings()) ||
               ComparingUtils.isModified(additionalArgsOptionsField, additionalCompilerSettings.getAdditionalArguments()) ||
               ComparingUtils.isModified(generateSourceMapsCheckBox, k2jsCompilerSettings.sourcemap) ||
               isModified(outputPrefixFile, k2jsCompilerSettings.outputPrefix) ||
               isModified(outputPostfixFile, k2jsCompilerSettings.outputPostfix);
    }

    @Override
    public void apply() throws ConfigurationException {
        setGenerateNoWarnings(generateNoWarningsCheckBox.isSelected());
        additionalCompilerSettings.setAdditionalArguments(additionalArgsOptionsField.getText());
        k2jsCompilerSettings.sourcemap = generateSourceMapsCheckBox.isSelected();
        k2jsCompilerSettings.outputPrefix = StringUtil.nullize(outputPrefixFile.getText(), true);
        k2jsCompilerSettings.outputPostfix = StringUtil.nullize(outputPostfixFile.getText(), true);
    }

    @Override
    public void reset() {
        generateNoWarningsCheckBox.setSelected(isGenerateNoWarnings());
        additionalArgsOptionsField.setText(additionalCompilerSettings.getAdditionalArguments());
        generateSourceMapsCheckBox.setSelected(k2jsCompilerSettings.sourcemap);
        outputPrefixFile.setText(k2jsCompilerSettings.outputPrefix);
        outputPostfixFile.setText(k2jsCompilerSettings.outputPostfix);
    }

    @Override
    public void disposeUIResources() {
    }

    @Nls
    @Override
    public String getDisplayName() {
        return extPoint.displayName;
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    private boolean isGenerateNoWarnings() {
        return commonCompilerSettings.suppressAllWarnings();
    }

    private void setGenerateNoWarnings(boolean selected) {
        commonCompilerSettings.suppress = selected ? SUPPRESS_WARNINGS : null;
    }

    private static void setupFileChooser(
            @NotNull JLabel label,
            @NotNull TextFieldWithBrowseButton fileChooser,
            @NotNull String title
    ) {
        label.setLabelFor(fileChooser);

        fileChooser.addBrowseFolderListener(title, null, null,
                                            new FileChooserDescriptor(true, false, false, false, false, false),
                                            TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT, false);
    }

    private static boolean isModified(@NotNull TextFieldWithBrowseButton chooser, @Nullable String currentValue) {
        return !StringUtil.equals(StringUtil.nullize(chooser.getText(), true), currentValue);
    }
}
