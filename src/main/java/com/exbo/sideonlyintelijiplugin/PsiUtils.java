package com.exbo.sideonlyintelijiplugin;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtilCore;

import java.util.*;

public class PsiUtils {
    private static final String SIDE_ONLY_ANNOTATION = "net.exbo.sideonly.annotation.SideOnly";

    // List before SideOnly value must be arrayed
    public static List<String> getMethodSideValues(PsiMethod psiMethod) {
        return getAllAnnotations(psiMethod);
    }

    public static List<String> getClassSideValues(PsiClass psiClass) {
        return getAllAnnotations(psiClass);
    }

    public static List<String> getElementSideValues(PsiElement element) {
        return getAllAnnotations(element);
    }

    public static boolean hasElementAnnotation(PsiModifierListOwner element) {
        return AnnotationUtil.findAnnotation(element, Set.of(SIDE_ONLY_ANNOTATION)) != null;
    }

    private static List<String> getAllAnnotations(PsiElement element) {
        Set<String> annotations = new HashSet<>();
        boolean first = true;
        while (element != null) {
            if (element instanceof PsiModifierListOwner) {
                final PsiModifierListOwner modifierListOwner = (PsiModifierListOwner) element;

                PsiAnnotation directAnnotation = AnnotationUtil.findAnnotation(modifierListOwner, Set.of(SIDE_ONLY_ANNOTATION), false);
                if (directAnnotation != null) {
                    List<String> sides = getSideAnnotationParams(directAnnotation);
                    if (first) {
                        first = false;
                        annotations.addAll(sides);
                    } else {
                        annotations.retainAll(sides);
                    }
                }
                for (PsiModifierListOwner superOwner : AnnotationUtil.getSuperAnnotationOwners(modifierListOwner)) {
                    PsiAnnotation annotation = AnnotationUtil.findAnnotation(superOwner, Set.of(SIDE_ONLY_ANNOTATION), false);
                    if (annotation != null) {
                        List<String> sides = getSideAnnotationParams(annotation);
                        if (first) {
                            first = false;
                            annotations.addAll(sides);
                        } else {
                            annotations.retainAll(sides);
                        }
                    }
                }
            }

            if (element instanceof PsiClassOwner) {
                final PsiClassOwner classOwner = (PsiClassOwner) element;
                final String packageName = classOwner.getPackageName();
                final PsiPackage aPackage = JavaPsiFacade.getInstance(element.getProject()).findPackage(packageName);
                if (aPackage == null) {
                    return !first ? new ArrayList<>(annotations) : null;
                }
                final PsiAnnotation annotation = AnnotationUtil.findAnnotation(aPackage, Set.of(SIDE_ONLY_ANNOTATION));
                if (annotation != null) {
                    if (first) {
                        first = false;
                        annotations.addAll(getSideAnnotationParams(annotation));
                    } else {
                        annotations.retainAll(getSideAnnotationParams(annotation));
                    }
                    // Check that annotation actually belongs to the same library/source root
                    // which could be important in case of split-packages
                    final VirtualFile annotationFile = PsiUtilCore.getVirtualFile(annotation);
                    final VirtualFile currentFile = classOwner.getVirtualFile();
                    if (annotationFile != null && currentFile != null) {
                        final ProjectFileIndex projectFileIndex = ProjectFileIndex.getInstance(element.getProject());
                        final VirtualFile annotationClassRoot = projectFileIndex.getClassRootForFile(annotationFile);
                        final VirtualFile currentClassRoot = projectFileIndex.getClassRootForFile(currentFile);
                        if (!Objects.equals(annotationClassRoot, currentClassRoot)) {
                            return new ArrayList<>(annotations);
                        }
                    }
                } else {
                    return !first ? new ArrayList<>(annotations) : null;
                }
            }
            element = element.getContext();
        }
        return !first ? new ArrayList<>(annotations) : null;
    }


    private static List<String> getSideAnnotationParams(PsiAnnotation annotation) {
        List<String> values = List.of();
        PsiAnnotationMemberValue annotationMemberValue = annotation.getParameterList().getAttributes()[0].getDetachedValue();
        if (annotationMemberValue instanceof PsiArrayInitializerMemberValue) {
            values = new ArrayList<>();
            for (PsiAnnotationMemberValue i : ((PsiArrayInitializerMemberValue) annotationMemberValue).getInitializers()) {
                values.add(i.toString());
            }
        } else if (annotationMemberValue instanceof PsiReferenceExpression) {
            values = List.of(annotationMemberValue.toString());
        }
        return values;
    }
}
