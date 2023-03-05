package com.exbo.sideonlyintelijiplugin;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInspection.*;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.siyeh.ig.psiutils.MethodUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UCallExpression;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.intellij.psi.search.GlobalSearchScope.moduleWithDependenciesAndLibrariesScope;


public class BadUsageInspection extends AbstractBaseJavaLocalInspectionTool {
    public ProblemDescriptor @Nullable [] checkMethodCall(@NotNull PsiMethodCallExpression methodCall,
                                                          @NotNull InspectionManager manager,
                                                          boolean isOnTheFly) {
        System.out.println(methodCall.resolveMethod());

        List<String> parentAnnotations = PsiUtils.getMethodSideValues(methodCall.resolveMethod());
        if (parentAnnotations.size() != 0) {
            PsiElement parent = methodCall.getParent().getParent().getParent(); // statement -> block -> method
            if (parent instanceof PsiMethod) {
                List<String> currentAnnotations = PsiUtils.getMethodSideValues((PsiMethod) parent);
                System.out.println(parentAnnotations + " " + currentAnnotations);
                if (!new HashSet<>(currentAnnotations).containsAll(parentAnnotations)) {
                    ProblemsHolder problemsHolder = new ProblemsHolder(manager, methodCall.getContainingFile(), isOnTheFly);
                    problemsHolder.registerProblem(methodCall,
                            currentAnnotations + "\n" + parentAnnotations,
                            ProblemHighlightType.GENERIC_ERROR);
                    return problemsHolder.getResultsArray();
                }
            }
        }

        System.out.println();
        return ProblemDescriptor.EMPTY_ARRAY;
    }

    public ProblemDescriptor @Nullable [] checkClassCall(@NotNull PsiClassInitializer initializer,
                                                          @NotNull InspectionManager manager,
                                                          boolean isOnTheFly) {
        System.out.println(initializer);
        ProblemsHolder problemsHolder = new ProblemsHolder(manager, initializer.getContainingFile(), isOnTheFly);
        problemsHolder.registerProblem(initializer,
                "",
                ProblemHighlightType.GENERIC_ERROR);
        return problemsHolder.getResultsArray();
    }


    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);
                addDescriptors(checkMethodCall(expression, holder.getManager(), isOnTheFly));
            }

            @Override
            public void visitClassInitializer(PsiClassInitializer initializer) {
                super.visitClassInitializer(initializer);
                addDescriptors(checkClassCall(initializer, holder.getManager(), isOnTheFly));
            }

            private void addDescriptors(final ProblemDescriptor[] descriptors) {
                if (descriptors != null) {
                    for (ProblemDescriptor descriptor : descriptors) {
                        holder.registerProblem(descriptor);
                    }
                }
            }
        };
    }
}
