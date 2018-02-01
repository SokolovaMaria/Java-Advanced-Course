package ru.ifmo.ctddev.sokolova.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static java.nio.file.Files.delete;
import static ru.ifmo.ctddev.sokolova.implementor.Constants.newline;
import static ru.ifmo.ctddev.sokolova.implementor.Constants.path_separator;

public class Implementor implements JarImpler, Impler {

    /**
     * Provides terminal interface
     * To get implementation of a class or interface, pass 2 arguments in the terminal:
     * args[0] - full name of class/interface
     * args[1] - path to location of the implemented class
     *
     * To generate *.jar file with the realisation of class/interface
     * args[0] - "-jar"
     * args[1] - full name of class/interface
     * args[2] - path to location of the implemented class
     *
     * @param args arguments passed in hte terminal to run {@link Implementor}
     * @throws ImplerException for {@link ClassNotFoundException} {@link IOException}
     */
    public static void main(String[] args) throws ImplerException {
        try {
            if (args[0].equals("-jar")) {
                Class<?> c = ClassLoader.getSystemClassLoader().loadClass(args[1]);
                try {
                    new Implementor().implementJar(c, Paths.get(args[2]));
                } catch (ImplerException e) {
                    throw new ImplerException(e.getMessage());
                }
            } else {
                Class<?> c = ClassLoader.getSystemClassLoader().loadClass(args[0]);
                try {
                    new Implementor().implement(c, Paths.get(args[1]));
                } catch (ImplerException e) {
                    throw new ImplerException(e.getMessage());
                }
            }
        } catch (ClassNotFoundException e) {
            throw new ImplerException("Class is not found: " + e.getMessage());
        }
    }

    /**
     * An absolute path to the location of the file with the realisation of class/interface
     */
    private Path finalPath;

    /**
     * Produces code implementing class or interface specified by provided <tt>token</tt>.
     * <p>
     * Generated class full name should be same as full name of the type token with <tt>Impl</tt> suffix
     * added. Generated source code should be placed in the correct subdirectory of the specified
     * <tt>root</tt> directory and have correct file name. For example, the implementation of the
     * interface {@link java.util.List} should go to <tt>$root/java/util/ListImpl.java</tt>
     *
     *
     * @param token type token to create implementation for.
     * @param root root directory.
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException when implementation cannot be
     * generated.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token == null || token.isPrimitive()) {
            throw new ImplerException("Class is primitive");
        }
        if (Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException("Unable to implement finals");
        }
        if (token == Enum.class) {
            throw new ImplerException("Enum can not be extended");
        }
        Path genFile = Paths.get(root.toString(), token.getCanonicalName().replace(".", File.separator).concat(Constants.SUFFIX));
        try {
            Files.createDirectories(genFile.getParent());
            Files.deleteIfExists(genFile);
            finalPath = Files.createFile(genFile);
            class UnicodeFilter extends FilterWriter {

                public UnicodeFilter(Writer writer) {
                    super(writer);
                }

                @Override
                public void write(int i) throws IOException {
                        out.write(String.format("\\u%04X", i));
                }

                @Override
                public void write(char[] chars, int i, int i1) throws IOException {
                    for (int j = i; j < i + i1; j++) {
                        write(chars[i]);
                    }
                }

                @Override
                public void write(String s, int i, int i1) throws IOException {
                    for (char ch: s.substring(i, i + i1).toCharArray()) {
                        write(ch);
                    }
                }
            }
            try (UnicodeFilter pw = new UnicodeFilter (new PrintWriter(finalPath.toString(), "UTF-8"))) {
                pw.write(implementClass(token));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    /**
     * Produces <tt>.jar</tt> file implementing class or interface specified by provided <tt>token</tt>.
     * <p>
     * Generated class full name should be same as full name of the type token with <tt>Impl</tt> suffix
     * added.
     *
     * @param token type token to create implementation for.
     * @param jarFile target <tt>.jar</tt> file.
     * @throws ImplerException when implementation cannot be generated.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        implement(token, jarFile);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if ((compiler.run(null, null, null, "-encoding", "WINDOWS-1251", finalPath.toString()))!=0) {
           throw new ImplerException("Failed to helloUDPClient_compile_run.sh generated file");
        }
        String fileAbsPath = finalPath.toString();
        String className = fileAbsPath.substring(0, fileAbsPath.length() - 4).concat("class");
        String jarName = jarFile.toString().concat(path_separator).concat(token.getSimpleName()).concat("Impl.jar");
        System.out.println("jarName " + jarName);
        File file = new File(className);
        try(InputStream implementedClass = new FileInputStream(file);
            JarOutputStream createdJar = new JarOutputStream(new FileOutputStream(jarName))) {
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            JarEntry jarEntry = new JarEntry(className);
            createdJar.putNextEntry(jarEntry);
            byte[] buffer = new byte[1024];
            int pos;
            while ((pos = implementedClass.read(buffer, 0, buffer.length)) > 0) {
                createdJar.write(buffer, 0, pos);
            }
            createdJar.closeEntry();
            Files.deleteIfExists(finalPath);
        } catch (IOException e) {
            throw new ImplerException("Failed to create jar" + e.getMessage());
        }
    }

    /**
     * Gets constructors and methods that should be implemented, for further code generation
     *
     * @param token is the full name of the class/interface, that will be implemented
     * @return full code with token implementation
     * @throws ImplerException if token has no non-private constructors
     */
    private String implementClass(Class<?> token) throws ImplerException {
        getConstructors(token);
        if (!hasNonPrivateConstructor()) {
            throw new ImplerException("Class has no non-private constructors");
        }
        getMethods(token);
        return classToString(token);
    }

    /**
     * Gets all implementable c
     */
    private Constructor[] constructors;

    /**
     * Gets the list of declared conctructors, declared for this class
     *
     * @param token is the full name of the class/interface, that will be implemented
     */
    private void getConstructors(Class<?> token) {
        constructors = token.getDeclaredConstructors();
    }

    /**
     * Checks weather class has non-private constructors
     *
     * @return true if there are non-private methods, false otherwise
     */
    private boolean hasNonPrivateConstructor() {
        if (constructors.length == 0) {
            return true;
        }
        for (Constructor constructor : constructors) {
            if (!Modifier.isPrivate(constructor.getModifiers())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Methods, that should be implemented are put to {@link TreeSet} with the overrided {@link Comparator}
     */
    private final NavigableSet<Method> methods = new TreeSet<>(new Comparator<Method>() {
        @Override
        public int compare(Method m1, Method m2) {
            return toComparingString(m1).compareTo(toComparingString(m2));
        }

        private String toComparingString(Method m1) {
            return m1.getName() + Generator.parameterListToString(m1.getParameterTypes());
        }
    });

    /**
     * If token is a class, then gets all non-static and non-private methods of this class and all its superclasses
     * If token is an interface, then takes all its abstract methods
     *
     * @param token class that is implemented
     */
    private void getMethods(Class<?> token) {
        if (token.isInterface()) {
            for (Method m : token.getMethods()) {
                if (Modifier.isAbstract(m.getModifiers())) {
                    methods.add(m);
                }
            }
            return;
        }

        while (Modifier.isAbstract(token.getModifiers())) {
            for (Method m : token.getDeclaredMethods()) {
                if (Modifier.isAbstract(m.getModifiers())) {
                    methods.add(m);
                }
            }
            token = token.getSuperclass();
        }
    }

    /**
     * Turns class implementation to string
     *
     * @param token class/interface to implement
     * @return full code with token implementation
     */
    private String classToString(Class<?> token) {
        String name = token.getSimpleName() + "Impl";
        StringBuilder implementation = new StringBuilder();
        implementation.append(Generator.packageToString(token.getPackage()));
        implementation.append(Generator.classToString(name, token).concat("{").concat(newline));
        for (Constructor constructor : constructors) {
            implementation.append(Generator.constructorToString(name, constructor).concat(newline));
        }
        for (Method m : methods) {
            implementation.append(Generator.methodToString(m).concat(newline));
        }
        implementation.append("}");
        return implementation.toString();
    }

}
