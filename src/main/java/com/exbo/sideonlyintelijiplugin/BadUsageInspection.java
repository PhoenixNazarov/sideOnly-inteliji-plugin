package com.exbo.sideonlyintelijiplugin;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.intellij.psi.search.GlobalSearchScope.moduleWithDependenciesAndLibrariesScope;


public class BadUsageInspection extends AbstractBaseJavaLocalInspectionTool {
    public ProblemDescriptor @Nullable [] checkMethodCall(@NotNull PsiMethodCallExpression methodCall,
                                                          @NotNull InspectionManager manager,
                                                          boolean isOnTheFly) {
        PsiElement element = methodCall.resolveMethod();
        PsiElement parentElement = methodCall.getParent().getParent().getParent();
        if (parentElement instanceof PsiMethod) {
            ProblemsHolder problemsHolder = new ProblemsHolder(manager, methodCall.getContainingFile(), isOnTheFly);
            checkError(element, parentElement, problemsHolder, methodCall);
            return problemsHolder.getResultsArray();
        }
        return ProblemDescriptor.EMPTY_ARRAY;
    }

    public ProblemDescriptor @Nullable [] checkClassCall(@NotNull PsiNewExpression newExpression,
                                                         @NotNull InspectionManager manager,
                                                         boolean isOnTheFly) {
        PsiJavaCodeReferenceElement aClass = newExpression.getClassReference();
        PsiMethod constructor = newExpression.resolveMethod();
        if (constructor != null) {
            PsiElement parentElement = newExpression.getParent().getParent().getParent();
            if (parentElement instanceof PsiMethod) {
                ProblemsHolder problemsHolder = new ProblemsHolder(manager, newExpression.getContainingFile(), isOnTheFly);
                checkError(constructor, parentElement, problemsHolder, newExpression);
                return problemsHolder.getResultsArray();
            }
        } else if (aClass != null) {
            PsiElement element = aClass.resolve();
            PsiElement parentElement = newExpression.getParent().getParent().getParent();
            if (parentElement instanceof PsiMethod) {
                ProblemsHolder problemsHolder = new ProblemsHolder(manager, newExpression.getContainingFile(), isOnTheFly);
                checkError(element, parentElement, problemsHolder, newExpression);
                return problemsHolder.getResultsArray();
            }
        }
        return ProblemDescriptor.EMPTY_ARRAY;
    }

    public ProblemDescriptor @Nullable [] checkAssignmentCall(@NotNull PsiAssignmentExpression psiAssignmentExpression,
                                                              @NotNull InspectionManager manager,
                                                              boolean isOnTheFly) {
        PsiElement element = psiAssignmentExpression.getLExpression();
        if (element instanceof PsiReferenceExpression) {
            PsiReferenceExpression referenceExpression = (PsiReferenceExpression) element;
            ProblemsHolder problemsHolder = new ProblemsHolder(manager, psiAssignmentExpression.getContainingFile(), isOnTheFly);
            checkError(element, referenceExpression.resolve(), problemsHolder, psiAssignmentExpression);
            return problemsHolder.getResultsArray();
        }
        return ProblemDescriptor.EMPTY_ARRAY;
    }

    public ProblemDescriptor @Nullable [] checkDeclarationCall(@NotNull PsiDeclarationStatement statement,
                                                               @NotNull InspectionManager manager,
                                                               boolean isOnTheFly) {
//        PsiElement element = ;
        PsiElement[] elements = statement.getDeclaredElements();
        if (elements.length >= 1) {
            PsiElement element = elements[0];
            if (element instanceof PsiLocalVariable) {
                PsiLocalVariable variable = (PsiLocalVariable) element;
                PsiElement parent = variable.getInitializer();
                if (parent != null) {
                    PsiReference mParent = parent.getReference();
                    if (mParent != null) {
                        ProblemsHolder problemsHolder = new ProblemsHolder(manager, statement.getContainingFile(), isOnTheFly);
                        checkError(parent, mParent.resolve(), problemsHolder, statement);
                        return problemsHolder.getResultsArray();
                    }
                }
                System.out.println();
            }
        }
        return ProblemDescriptor.EMPTY_ARRAY;
    }

    private void checkError(PsiElement usageElement, PsiElement scopeElement, ProblemsHolder problemsHolder, PsiElement markElement) {
        List<String> parentAnnotations = PsiUtils.getElementSideValues(usageElement);
        if (parentAnnotations != null) {
            List<String> currentAnnotations = PsiUtils.getElementSideValues(scopeElement);
            if (currentAnnotations != null) {
                if (!new HashSet<>(currentAnnotations).containsAll(parentAnnotations) || parentAnnotations.size() == 0) {
                    problemsHolder.registerProblem(markElement,
                            currentAnnotations + "\n" + parentAnnotations,
                            ProblemHighlightType.GENERIC_ERROR);
                }
            }
        }
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
            public void visitNewExpression(PsiNewExpression expression) {
                super.visitNewExpression(expression);
                addDescriptors(checkClassCall(expression, holder.getManager(), isOnTheFly));
            }

            @Override
            public void visitAssignmentExpression(PsiAssignmentExpression expression) {
                super.visitAssignmentExpression(expression);
                addDescriptors(checkAssignmentCall(expression, holder.getManager(), isOnTheFly));
            }

            @Override
            public void visitDeclarationStatement(PsiDeclarationStatement statement) {
                super.visitDeclarationStatement(statement);
                addDescriptors(checkDeclarationCall(statement, holder.getManager(), isOnTheFly));
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
