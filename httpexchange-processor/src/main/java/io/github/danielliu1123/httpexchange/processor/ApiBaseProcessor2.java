package io.github.danielliu1123.httpexchange.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Generated;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.javapoet.AnnotationSpec;
import org.springframework.javapoet.JavaFile;
import org.springframework.javapoet.MethodSpec;
import org.springframework.javapoet.ParameterSpec;
import org.springframework.javapoet.ParameterizedTypeName;
import org.springframework.javapoet.TypeName;
import org.springframework.javapoet.TypeSpec;
import org.springframework.javapoet.TypeVariableName;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author Freeman
 */
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes({
    "org.springframework.web.service.annotation.HttpExchange",
    "org.springframework.web.service.annotation.GetExchange",
    "org.springframework.web.service.annotation.PostExchange",
    "org.springframework.web.service.annotation.PutExchange",
    "org.springframework.web.service.annotation.DeleteExchange",
    "org.springframework.web.service.annotation.PatchExchange",
})
public class ApiBaseProcessor2 extends AbstractProcessor {

    private static final String GENERATED_CLASS_SUFFIX = "Base";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getRootElements()) {
            processElement(annotations, element);
        }
        return true;
    }

    private void processElement(Set<? extends TypeElement> annotations, Element element) {
        if (isInterface(element) && !isGenericType(element)) {
            processAnnotations(annotations, element);
        } else {
            processNonInterfaceElement(annotations, element);
        }
    }

    private void processNonInterfaceElement(Set<? extends TypeElement> annotations, Element element) {
        for (Element enclosedElement : element.getEnclosedElements()) {
            processElement(annotations, enclosedElement);
        }
    }

    private void processAnnotations(Set<? extends TypeElement> annotations, Element element) {
        TypeSpec.Builder classBuilder = createClassBuilder(element);
        boolean isNeedGenerateJavaFile = hasAnnotationMatched(annotations, element);

        for (Element enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.METHOD) {
                isNeedGenerateJavaFile =
                        processMethodElement(annotations, classBuilder, enclosedElement, Collections.emptyMap())
                                || isNeedGenerateJavaFile;
            } else if (enclosedElement.getKind() == ElementKind.INTERFACE) {
                processElement(annotations, enclosedElement);
            }
        }

        if (element instanceof TypeElement typeElement) {
            processInheritedMethods(typeElement, classBuilder, annotations);
        }

        if (isNeedGenerateJavaFile) {
            generateJavaFile(element, classBuilder);
        }
    }

    private void processInheritedMethods(
            TypeElement typeElement, TypeSpec.Builder classBuilder, Set<? extends TypeElement> annotations) {
        for (TypeMirror superInterface : typeElement.getInterfaces()) {
            Element superInterfaceElement = processingEnv.getTypeUtils().asElement(superInterface);
            Map<String, TypeName> typeMapping = createTypeMapping(superInterface);
            for (Element enclosedElement : superInterfaceElement.getEnclosedElements()) {
                if (enclosedElement.getKind() == ElementKind.METHOD) {
                    processMethodElement(annotations, classBuilder, enclosedElement, typeMapping);
                }
            }
        }
    }

    private Map<String, TypeName> createTypeMapping(TypeMirror parentInterface) {
        Map<String, TypeName> typeMapping = new HashMap<>();
        DeclaredType declaredParentInterface = (DeclaredType) parentInterface;
        List<? extends TypeMirror> typeArguments = declaredParentInterface.getTypeArguments();

        List<? extends TypeParameterElement> typeParameters =
                ((TypeElement) declaredParentInterface.asElement()).getTypeParameters();
        for (int i = 0; i < typeParameters.size(); i++) {
            typeMapping.put(typeParameters.get(i).getSimpleName().toString(), TypeName.get(typeArguments.get(i)));
        }

        return typeMapping;
    }

    private boolean processMethodElement(
            Set<? extends TypeElement> annotations,
            TypeSpec.Builder classBuilder,
            Element methodElement,
            Map<String, TypeName> typeMapping) {
        for (AnnotationMirror annotation : methodElement.getAnnotationMirrors()) {
            if (!methodElement.getModifiers().contains(Modifier.DEFAULT)
                    && isAnnotationMatched(annotations, annotation)) {
                ExecutableElement executableElement = (ExecutableElement) methodElement;
                MethodSpec methodSpec = buildMethodSpec(executableElement, typeMapping);
                classBuilder.addMethod(methodSpec);
                return true;
            }
        }
        return false;
    }

    private MethodSpec buildMethodSpec(ExecutableElement methodElement, Map<String, TypeName> typeMapping) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(
                        methodElement.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .returns(resolveGenericType(methodElement.getReturnType(), typeMapping))
                .addAnnotation(Override.class);

        for (VariableElement parameter : methodElement.getParameters()) {
            TypeName parameterType = resolveGenericType(parameter.asType(), typeMapping);
            methodBuilder.addParameter(ParameterSpec.builder(
                            parameterType, parameter.getSimpleName().toString())
                    .build());
        }

        methodBuilder.addStatement("throw new $T($T.NOT_IMPLEMENTED)", ResponseStatusException.class, HttpStatus.class);

        return methodBuilder.build();
    }

    private TypeName resolveGenericType(TypeMirror typeMirror, Map<String, TypeName> typeMapping) {
        // Directly return the type if it is not a generic type.
        if (typeMirror.getKind() != TypeKind.TYPEVAR && typeMirror.getKind() != TypeKind.DECLARED) {
            return TypeName.get(typeMirror);
        }

        TypeName typeName = TypeName.get(typeMirror);

        // Check if the type is a type variable and replace it with the actual type.
        if (typeName instanceof TypeVariableName typeVariableName) {
            return typeMapping.getOrDefault(typeVariableName.name, typeName);
        }

        // Handle parameterized types (generics).
        else if (typeName instanceof ParameterizedTypeName parameterizedTypeName) {
            List<TypeName> typeArguments = new ArrayList<>();
            DeclaredType declaredType = (DeclaredType) typeMirror;

            for (TypeMirror argMirror : declaredType.getTypeArguments()) {
                typeArguments.add(resolveGenericType(argMirror, typeMapping));
            }

            return ParameterizedTypeName.get(parameterizedTypeName.rawType, typeArguments.toArray(new TypeName[0]));
        }
        return typeName;
    }

    @SneakyThrows
    private void generateJavaFile(Element element, TypeSpec.Builder classBuilder) {
        String originalPackageName = processingEnv
                .getElementUtils()
                .getPackageOf(element)
                .getQualifiedName()
                .toString();
        JavaFile javaFile = JavaFile.builder(originalPackageName, classBuilder.build())
                .indent("    ")
                .build();
        javaFile.writeTo(processingEnv.getFiler());
    }

    private static boolean isInterface(Element element) {
        return element.getKind() == ElementKind.INTERFACE;
    }

    private static boolean isGenericType(Element element) {
        return element instanceof TypeElement te && !te.getTypeParameters().isEmpty();
    }

    private static boolean hasAnnotationMatched(Set<? extends TypeElement> annotations, Element element) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (isAnnotationMatched(annotations, annotationMirror)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAnnotationMatched(Set<? extends TypeElement> annotations, AnnotationMirror annotation) {
        return annotations.stream().anyMatch(a -> a.getQualifiedName()
                .toString()
                .equals(annotation.getAnnotationType().toString()));
    }

    private TypeSpec.Builder createClassBuilder(Element element) {
        TypeSpec.Builder result = TypeSpec.classBuilder(element.getSimpleName() + GENERATED_CLASS_SUFFIX)
                .addModifiers(Modifier.ABSTRACT)
                .addSuperinterface(TypeName.get(element.asType()))
                .addAnnotation(AnnotationSpec.builder(Generated.class)
                        .addMember("value", "$S", ApiBaseProcessor.class.getName())
                        .addMember("comments", "$S", "Generated by httpexchange-processor, DO NOT modify!")
                        .build())
                .addJavadoc(
                        """
                                Generated default implementation for the server-side.

                                <p>
                                How to use:
                                <pre>{@code
                                @RestController
                                public class $L extends $L {
                                    // ...
                                }
                                }</pre>
                                """,
                        element.getSimpleName() + "Impl",
                        element.getSimpleName() + GENERATED_CLASS_SUFFIX);
        if (element.getModifiers().contains(Modifier.PUBLIC)) {
            result.addModifiers(Modifier.PUBLIC);
        }
        return result;
    }
}
