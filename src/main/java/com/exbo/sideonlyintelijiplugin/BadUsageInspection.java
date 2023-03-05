package com.exbo.sideonlyintelijiplugin;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class BadUsageInspection extends AbstractBaseJavaLocalInspectionTool {
    public ProblemDescriptor @Nullable [] checkMethodCall(@NotNull PsiMethodCallExpression methodCall,
                                                          @NotNull InspectionManager manager,
                                                          boolean isOnTheFly) {
        System.out.println(methodCall);
        ProblemsHolder problemsHolder = new ProblemsHolder(manager, methodCall.getContainingFile(), isOnTheFly);
        problemsHolder.registerProblem(methodCall, "No", ProblemHighlightType.GENERIC_ERROR);
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
