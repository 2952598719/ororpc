package top.orosirian;

import java.lang.reflect.Method;

public class Utils {

    public static String getMethodSignature(Class<?> clazz, Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append(clazz.getName()).append("#").append(method.getName()).append("(");
        Class<?>[] parameterTypes = method.getParameterTypes();
        for(int i = 0; i <= parameterTypes.length - 1; i++) {
            builder.append(parameterTypes[i].getName());
            if(i <= parameterTypes.length - 2) {
                builder.append(",");
            } else {
                builder.append(")");
            }
        }
        return builder.toString();
    }
    
}
