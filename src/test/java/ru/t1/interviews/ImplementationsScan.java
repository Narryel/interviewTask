package ru.t1.interviews;

import com.google.common.reflect.ClassPath;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ImplementationsScan implements ParameterResolver {
    private final Object lock = true;
    private static volatile Set<Class<?>> loadedClasses;

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return extractListTypeParameter(parameterContext) != null;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        var reqInterface = extractListTypeParameter(parameterContext);

        return scan().stream()
                .filter(clazz -> !clazz.isInterface()
                        && clazz.isAnnotationPresent(Injectable.class)
                        && reqInterface.isAssignableFrom(clazz))
                .map(clazz -> {
                    try {
                        return (Object) clazz.getConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

    Class<?> extractListTypeParameter(ParameterContext parameterContext) {
        var parameterType = parameterContext.getParameter().getParameterizedType();

        if (!(parameterType instanceof ParameterizedType
                && ((ParameterizedType) parameterType).getRawType().getTypeName().equals(List.class.getTypeName()))
        ) {
            return null;
        }

        var classes = Arrays.stream(((ParameterizedType) parameterType).getActualTypeArguments())
                .filter(arg -> arg instanceof Class && ((Class<?>) arg).isInterface())
                .map(arg -> (Class<?>) arg)
                .toArray(Class[]::new);

        return classes.length == 1 ? classes[0] : null;
    }

    @SneakyThrows
    private Set<Class<?>> scan() {
        if (loadedClasses != null) return loadedClasses;

        synchronized (lock) {
            if (loadedClasses != null) return loadedClasses;

            var cl = ClassLoader.getSystemClassLoader();

            loadedClasses = ClassPath.from(cl)
                    .getTopLevelClassesRecursive(this.getClass().getPackageName())
                    .stream()
                    .map(classInfo -> {
                        try {
                            return cl.loadClass(classInfo.getName());
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toUnmodifiableSet());

            return loadedClasses;
        }
    }
}
