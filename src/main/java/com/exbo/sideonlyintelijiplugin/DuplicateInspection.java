package com.exbo.sideonlyintelijiplugin;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;


public class DuplicateInspection extends AbstractBaseJavaLocalInspectionTool {
    static final String SIDE_ONLY_ANNOTATION = "net.exbo.sideonly.annotation.SideOnly";

    @Override
    public ProblemDescriptor @Nullable [] checkMethod(@NotNull PsiMethod method, @NotNull InspectionManager manager, boolean isOnTheFly) {
        PsiAnnotation sideAnnotation = method.getAnnotation(SIDE_ONLY_ANNOTATION);
        if (sideAnnotation != null) {
            ProblemsHolder problemsHolder = new ProblemsHolder(manager, method.getContainingFile(), isOnTheFly);
            checkDuplicate(sideAnnotation, problemsHolder);
//            HintManager.getInstance().showInformationHint(manager.);
//            HintManager.getInstance().showInformationHint(
//                    manager,
//                    method.toString(),
//                    null
//            )
//            System.out.println(method + " " + method.getParent() + " " + method.getParent() + " ");

            return problemsHolder.getResultsArray();
        }
        return ProblemDescriptor.EMPTY_ARRAY;
    }

    @Override
    public ProblemDescriptor @Nullable [] checkClass(@NotNull PsiClass aClass, @NotNull InspectionManager manager, boolean isOnTheFly) {
        PsiAnnotation sideAnnotation = aClass.getAnnotation(SIDE_ONLY_ANNOTATION);
        if (sideAnnotation != null) {
            ProblemsHolder problemsHolder = new ProblemsHolder(manager, aClass.getContainingFile(), isOnTheFly);
            checkDuplicate(sideAnnotation, problemsHolder);
            return problemsHolder.getResultsArray();
        }
        return ProblemDescriptor.EMPTY_ARRAY;
    }

    private void checkDuplicate(PsiAnnotation annotation, ProblemsHolder holder) {
        // todo fix
        List<PsiNameValuePair> sideStates = List.of(annotation.getParameterList().getAttributes()[0]);
        PsiAnnotationMemberValue annotationMemberValue = sideStates.get(0).getDetachedValue();

        List<PsiElement> values = List.of();
        if (annotationMemberValue instanceof PsiArrayInitializerMemberValue) {
            values = List.of(((PsiArrayInitializerMemberValue) annotationMemberValue).getInitializers());
        } else if (annotationMemberValue instanceof PsiReferenceExpression) {
            values = List.of(annotationMemberValue);
        }

        if (values.size() > Set.of(values).size()) {
            holder.registerProblem(Objects.requireNonNull(sideStates.get(0)), "Duplicate values");
        }
    }
}
