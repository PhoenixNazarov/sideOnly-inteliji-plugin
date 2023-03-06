package com.exbo.sideonlyintelijiplugin;

import com.intellij.codeInsight.hints.*;
import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.RecursivelyUpdatingRootPresentation;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;


@SuppressWarnings("UnstableApiUsage")
public class SideInlayProvider implements InlayHintsProvider<NoSettings> {
    @Override
    public boolean isVisibleInSettings() {
        return true;
    }

    @NotNull
    @Override
    public SettingsKey<NoSettings> getKey() {
        return new SettingsKey<>("SideOnlyProviderKey");
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getName() {
        return "SideOnlyProvider";
    }

    @Nullable
    @Override
    public String getPreviewText() {
        return "Write SideOnly Annotation";
    }

    @NotNull
    @Override
    public ImmediateConfigurable createConfigurable(@NotNull NoSettings noSettings) {
        return changeListener -> new JPanel();
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


    private static class Collector extends FactoryInlayHintsCollector {
        public Collector(@NotNull Editor editor) {
            super(editor);
        }

        @Override
        public boolean collect(@NotNull PsiElement psiElement, @NotNull Editor editor, @NotNull InlayHintsSink inlayHintsSink) {

            int offset = psiElement.getTextRange().getStartOffset();
            int line = editor.getDocument().getLineNumber(offset);
            int lineStart = editor.getDocument().getLineStartOffset(line);
            int indent = offset - lineStart;

            if (psiElement instanceof PsiMethod) {
                List<String> list = PsiUtils.getMethodSideValues((PsiMethod) psiElement);
                if (list != null && !PsiUtils.hasElementAnnotation((PsiModifierListOwner) psiElement)) {
                    InlayPresentation presentation = getFactory().text("@SideOnly(" + list + ")");
                    ;
                    BlockConstraints block = new BlockConstraints(false, 100, 1, indent);
                    RecursivelyUpdatingRootPresentation root = new RecursivelyUpdatingRootPresentation(presentation);
                    inlayHintsSink.addBlockElement(editor.getDocument().getLineNumber(psiElement.getTextOffset()), true, root, block);
                }
            }

            if (psiElement instanceof PsiClass) {
                List<String> list = PsiUtils.getClassSideValues((PsiClass) psiElement);
                if (list != null && !PsiUtils.hasElementAnnotation((PsiModifierListOwner) psiElement)) {
                    InlayPresentation presentation = getFactory().text("@SideOnly" + list + ")");
                    BlockConstraints block = new BlockConstraints(false, 100, 1, indent);
                    RecursivelyUpdatingRootPresentation root = new RecursivelyUpdatingRootPresentation(presentation);
                    inlayHintsSink.addBlockElement(editor.getDocument().getLineNumber(psiElement.getTextOffset()), true, root, block);
                }
            }

            return true;
        }
    }

}
