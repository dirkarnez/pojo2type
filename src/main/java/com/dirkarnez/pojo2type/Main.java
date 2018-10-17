package com.dirkarnez.pojo2type;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Field;
import java.util.Arrays;

class JavaClassLoaderTest extends ClassLoader {
    public Class<?> load(File f) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(f);
            byte rawBytes[] = new byte[fileInputStream.available()];
            fileInputStream.read(rawBytes);
            return this.defineClass(null, rawBytes, 0, rawBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

public class Main {
    public static void main(String[] args) {
        File f = new File("C:\\Users\\User\\Desktop\\MyPojo.java");

        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        final StandardJavaFileManager manager = compiler.getStandardFileManager(
                diagnostics, null, null);


        final File file = new File(f.toURI());


        final Iterable<? extends JavaFileObject> sources = manager.getJavaFileObjectsFromFiles(Arrays.asList(file));
        final JavaCompiler.CompilationTask task = compiler.getTask(null, manager, diagnostics,
                null, null, sources);
        System.out.format("%b\n", task.call());

        for (final Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {

            System.out.format("%s, line %d in %s",
                    diagnostic.getMessage(null),
                    diagnostic.getLineNumber(),
                    diagnostic.getSource().getName());
        }
        try {
            manager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File classFile = new File("C:\\Users\\User\\Desktop\\MyPojo.class");

        try {
            writeTypeScriptInterface(new JavaClassLoaderTest().load(classFile));
            if (classFile.delete()) {
                System.out.println("File deleted successfully");
            } else {
                System.out.println("Failed to delete the file");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeTypeScriptInterface(Class<?> cls) {

        Field[] fields = cls.getDeclaredFields();
        System.out.println("interface " + cls.getSimpleName() + " {");
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (i > 0) {
                System.out.println(",");
            }
            System.out.print("\t" + field.getName() + ": " + mapTypescriptTypeName(field.getType()));
        }
        System.out.println("\n}");

    }

    private static String mapTypescriptTypeName(Class<?> type2Map) {
        if (type2Map.equals(String.class)) {
            return "string";
        } else if (type2Map.getSuperclass().equals(Number.class)) {
            return "number";
        } else {
            return type2Map.getSimpleName();
        }
    }
}
