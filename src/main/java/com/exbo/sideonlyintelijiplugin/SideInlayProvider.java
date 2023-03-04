package com.exbo.sideonlyintelijiplugin;

import com.intellij.codeInsight.hints.*;
import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.RecursivelyUpdatingRootPresentation;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.codeInsight.hints.InlayHintsUtils;

import javax.swing.*;

@SuppressWarnings("UnstableApiUsage")
public class SideInlayProvider implements InlayHintsProvider<NoSettings> {
    @Override
    public boolean isVisibleInSettings() {
        return true;
    }

    @NotNull
    @Override
    public SettingsKey<NoSettings> getKey() {
        return new SettingsKey<>("MySidelkjwlekrjwlkejrlwer");
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getName() {
        return "LKajsdlkajsd";
    }

    @Nullable
    @Override
    public String getPreviewText() {
        return "LKajsdlkajsd";
    }

    @NotNull
    @Override
    public ImmediateConfigurable createConfigurable(@NotNull NoSettings noSettings) {
        return new ImmediateConfigurable() {
            @NotNull
            @Override
            public JComponent createComponent(@NotNull ChangeListener changeListener) {
                return new JPanel();
            }
        };
    }

    @NotNull
    @Override
    public NoSettings createSettings() {
        return new NoSettings();
    }

    @Nullable
    @Override
    public InlayHintsCollector getCollectorFor(@NotNull PsiFile psiFile, @NotNull Editor editor, @NotNull NoSettings noSettings, @NotNull InlayHintsSink inlayHintsSink) {
        return new Collector(editor);
    }

    @Override
    public boolean isLanguageSupported(@NotNull Language language) {
        return true;
    }


    private class Collector extends FactoryInlayHintsCollector {
        public Collector(@NotNull Editor editor) {
            super(editor);
        }

        @Override
        public boolean collect(@NotNull PsiElement psiElement, @NotNull Editor editor, @NotNull InlayHintsSink inlayHintsSink) {
            System.out.println(psiElement);
            if (psiElement instanceof PsiMethod) {
                InlayPresentation presentation = getFactory().text("@SideOnly(CLIENT)");
                BlockConstraints block = new BlockConstraints(false, 100, 1, 8);
                RecursivelyUpdatingRootPresentation root = new RecursivelyUpdatingRootPresentation(presentation);
                inlayHintsSink.addBlockElement(editor.getDocument().getLineNumber(psiElement.getTextOffset()), true, root, block);
//                inlayHintsSink.addBlockElement(psiElement.getTextOffset(), true, presentation, block);
                return true;
            }
            return true;
        }
    }

}
