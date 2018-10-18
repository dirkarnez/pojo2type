package com.dirkarnez.pojo2type;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        List<File> listOfSrc = getFilesInDirectory("C:\\Users\\User\\Desktop\\model", "java");
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager manager = compiler.getStandardFileManager(diagnostics, null, null);

        JavaCompiler.CompilationTask task = compiler.getTask(null, manager, diagnostics,
                null, null, manager.getJavaFileObjectsFromFiles(listOfSrc));
        System.out.format("%b\n", task.call());

        try {
            manager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<File> listOfClass = getFilesInDirectory("C:\\Users\\User\\Desktop\\model", "class");

        listOfClass.stream().forEach(classFile -> {
            try {
                writeTypeScriptInterface(new JavaClassLoaderTest().load(classFile));
                System.out.println();
                classFile.delete();
//                if (classFile.delete()) {
//                    System.out.println("File deleted successfully");
//                } else {
//                    System.out.println("Failed to delete the file");
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    private static List<File> getFilesInDirectory(String pathName, String extension) {
        File srcFolder = new File(pathName);
        List<File> listOfFile = Arrays.asList(srcFolder.listFiles());
                listOfFile.stream()
                .filter(file -> {
                    String fileName = file.getName();
                    return file.isFile() && extension.equals(fileName.substring(fileName.lastIndexOf(".")));
                })
                .collect(Collectors.toList());

        return listOfFile;
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
