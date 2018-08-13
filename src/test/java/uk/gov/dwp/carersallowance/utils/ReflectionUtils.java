package uk.gov.dwp.carersallowance.utils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;

public class ReflectionUtils {
    /**
     * This method bypasses the security model for private methods so it can be tested
     * This makes testing significantly easier for parts of the internal implementation
     * that should not be exposed.  Not ideal, but until a better method comes along ...
     */
    public static Object callPrivateMethod(Object instance, String methodName, Class<?>[] parameterTypes, Object[] parameterValues, Class<?> expectedReturnType) throws IOException, ParseException {
        assert instance != null;
        assert methodName != null;
        assert parameterValues != null;

        if(parameterTypes == null) {
            parameterTypes = new Class<?>[parameterValues.length];
            for(int index = 0; index < parameterValues.length; index++) {
                if(parameterValues[index] == null) {
                    throw new IllegalArgumentException("Cannot infer class type of parameter value: index = " + index);
                }
                parameterTypes[index] = parameterValues[index].getClass();
            }
        }

        try {
            Method method = instance.getClass().getDeclaredMethod(methodName, parameterTypes);
            if(expectedReturnType == null) {
                expectedReturnType = method.getReturnType();
            }

            method.setAccessible(true);
            Object result = method.invoke(instance, parameterValues);
            if(expectedReturnType == Void.TYPE) {
                return Void.TYPE;
            }

            if(result == null) {
                return null;
            }

            if(expectedReturnType.isInstance(result) == false) {
                throw new ClassCastException("result class(" + result.getClass().getName() + ") was not of expected type(" + expectedReturnType.getName() + ")");
            }
            return result;

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unable to execute method by reflection", e);
        } catch (RuntimeException e) {
            throw e;
        }
    }
}