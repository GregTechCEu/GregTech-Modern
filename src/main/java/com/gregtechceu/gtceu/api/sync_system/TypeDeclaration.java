package com.gregtechceu.gtceu.api.sync_system;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

public class TypeDeclaration {

    @Getter
    private final Type rawType;
    @Getter
    private final @Nullable Class<?> classValue;
    @Getter
    private final TypeDeclaration[] genericTypeArgs;
    private final @Nullable TypeDeclaration arrayComponentType;

    public TypeDeclaration(Type type) {
        this.rawType = type;

        if (type instanceof ParameterizedType parameterizedType) {
            this.classValue = (Class<?>) parameterizedType.getRawType();
            this.genericTypeArgs = Arrays.stream(parameterizedType.getActualTypeArguments()).map(TypeDeclaration::new)
                    .toArray(TypeDeclaration[]::new);
            this.arrayComponentType = null;
        } else if (type instanceof GenericArrayType genericArrayType) {
            this.classValue = null;
            this.arrayComponentType = new TypeDeclaration(genericArrayType.getGenericComponentType());
            this.genericTypeArgs = new TypeDeclaration[0];
        } else {
            this.classValue = (Class<?>) type;
            this.genericTypeArgs = new TypeDeclaration[0];
            this.arrayComponentType = classValue.isArray() ? new TypeDeclaration(classValue.getComponentType()) : null;
        }
    }

    public boolean isArray() {
        return (classValue != null && classValue.isArray()) || (rawType instanceof GenericArrayType);
    }

    public TypeDeclaration getArrayComponentType() {
        if (arrayComponentType == null) throw new IllegalStateException(
                "Attempted to get array component for non-array type %s".formatted(rawType));
        return arrayComponentType;
    }

    @Override
    public String toString() {
        return rawType.toString();
    }
}
