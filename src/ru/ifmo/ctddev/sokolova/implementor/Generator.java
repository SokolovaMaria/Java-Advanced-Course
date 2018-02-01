package ru.ifmo.ctddev.sokolova.implementor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;

import static ru.ifmo.ctddev.sokolova.implementor.Constants.ARG;
import static ru.ifmo.ctddev.sokolova.implementor.Constants.newline;

/**
 * Created by maria on 05.03.17.
 */
public class Generator {
    /**
     * Returns {@link java.lang.String} representation of package definition
     *
     * @param p {@link java.lang.Package} to be defined
     * @return {@link java.lang.String} representing package definition
     */
    public static String packageToString (Package p) {
        return "package " + p.getName() + ";";
    }

    /**
     * Returns class header definition
     *
     * @param name {@link java.lang.String} name of {@link java.lang.Class} to define
     * @param c    interface/class given class is extended/implemented from
     * @return first line of class definition
     */
    public static String classToString(String name, Class<?> c) {
        StringBuilder res = new StringBuilder();
        res.append(Modifier.toString(c.getModifiers() & ~(Modifier.INTERFACE | Modifier.ABSTRACT)).concat(" class ").concat(name));
        if (c.isInterface()) {
            res.append(" implements ");
        } else {
            res.append(" extends ");
        }
        res.append(c.getCanonicalName());
        return res.toString();
    }

    /**
     * Returns {@link java.lang.String} representation of {@link java.lang.reflect.Method} m
     * default implementation
     *
     * @param m - method to represent
     * @return a {@link java.lang.String} representation of given method
     */
    public static String methodToString(Method m) {
        StringBuilder current = new StringBuilder();
        current.append(annotationsToString(m.getAnnotations()));
        int modifier = m.getModifiers() & ~(Modifier.ABSTRACT | Modifier.TRANSIENT);
        current.append(Modifier.toString(modifier).concat(" ") .concat(m.getReturnType().getCanonicalName()).concat(" ").concat(m.getName()));
        current.append(("(").concat(parametersToString(m.getParameters())).concat(")"));

        current.append(("{").concat(newline));
        current.append(("return ").concat(returnDefaultValues(m.getReturnType()).concat(";")));
        current.append(newline.concat("}"));
        return current.toString();
    }

    /**
     * Transfers the list of {@link Parameter} to string
     *
     * @param p list of parameters
     * @return string representation of parameter array
     */
    private static  String parametersToString(Parameter[] p) {
        String names = "";
        for (int i = 0; i < p.length; i++) {
            String type = p[i].getType().getCanonicalName();
            if (type.length() != 0) type += " ";
            names += (type + p[i].getName() );
            if (i != p.length - 1) {
                names += ", ";
            }
        }
        return names;
    }

    /**
     * Returns default values for return statements
     *
     * @param token class/interface to get return values for
     * @return false, null or 0 depending on the type of the return value
     */
    private static String returnDefaultValues(Class<?> token) {
        if (token.equals(boolean.class)) {
            return " false";
        } else if (token.equals(void.class)) {
            return "";
        } else if (Object.class.isAssignableFrom(token)) {
            return " null";
        } else {
            return " 0";
        }
    }


    /**
     * Returns {@link java.lang.String} representing of constructor only invoking given
     * constructor of superclass
     *
     * @param name {@link java.lang.String} name of class to generate {@link java.lang.reflect.Constructor}
     * @param constructor {@link java.lang.reflect.Constructor} of superclass to use
     * @return {@link java.lang.String} representation of constructor
     */
    public static String constructorToString(String name, Constructor constructor) {
        StringBuilder builder = new StringBuilder();

        builder.append(annotationsToString(constructor.getAnnotations()));
        builder.append(Modifier.toString(constructor.getModifiers() & ~(Modifier.INTERFACE | Modifier.ABSTRACT | Modifier.TRANSIENT)) + " ");
        builder.append(name);
        builder.append("(");
        builder.append(parametersToString(constructor.getParameters()));
        builder.append(")");

        Class<?>[] exceptions = constructor.getExceptionTypes();
        if (exceptions.length > 0) {
            builder.append(" throws ");
            builder.append(parameterListToString(exceptions));
        }

        builder.append("{");
        builder.append(newline);
        builder.append("super(");
        builder.append(getDefaultArgList(constructor.getParameterTypes()));
        builder.append(");");
        builder.append(newline);
        builder.append("}");
        return builder.toString();
    }

    /**
     * Returns {@link java.lang.String} representation of default parameter names
     *
     * @param p array of parameters
     * @return {@link java.lang.String} of default parameter names
     */
    private static String getDefaultArgList(Class[] p) {
        String args = "";
        for (int i = 0; i < p.length; i++) {
            args += (ARG + i);
            if (i != p.length - 1) {
                args += ", ";
            }
        }
        return args;
    }


    /**
     * Returns {@link java.lang.String} representation of parameter list separated with commas.
     *
     * @param parameterTypes array of {@link java.lang.Class} representing types of parameters
     * @return {@link java.lang.String} of given parameter list
     */
    public static String parameterListToString (Class<?>[] parameterTypes) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parameterTypes.length; i++) {
            builder.append(parameterTypes[i].getCanonicalName());
            if (i < parameterTypes.length - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    /**
     * Puts all annotations to string
     *
     * @param annotations array of annotations, provided for token
     * @return string representation of this array
     */
    private static String annotationsToString(Annotation[] annotations) {
        StringBuilder as = new StringBuilder();
        for (Annotation a : annotations) {
            as.append(a.toString());
            as.append("\n");
        }
        return as.toString();
    }
}
