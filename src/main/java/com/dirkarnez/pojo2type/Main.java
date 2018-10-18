package com.dirkarnez.pojo2type;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

class JavaClassLoaderTest extends ClassLoader {
    public Class<?> load(File f) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(f);
            byte rawBytes[] = toByteArray(fileInputStream);
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

    private byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read;
        byte[] byteArray = new byte[1000];
        while ( (read = inputStream.read(byteArray, 0, byteArray.length) ) != -1) {
            out.write( byteArray, 0, read );
        }
        out.flush();
        return out.toByteArray();
    }
}

public class Main {
    public static void main(String[] args) {
        List<File> listOfSrc = getFilesInDirectory("C:\\Users\\User\\Desktop\\model", ".java");
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

        List<File> listOfClass = getFilesInDirectory("C:\\Users\\User\\Desktop\\model", ".class");

        JavaClassLoaderTest j = new JavaClassLoaderTest();
        for (File clazz : listOfClass) {
            try {
                writeTypeScriptInterface(j.load(clazz));
                System.out.println();
                clazz.delete();
//                if (classFile.delete()) {
//                    System.out.println("File deleted successfully");
//                } else {
//                    System.out.println("Failed to delete the file");
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static List<File> getFilesInDirectory(String pathName, String extension) {
        File srcFolder = new File(pathName);
        List<File> listOfFile = new ArrayList<>();
        for (File file : srcFolder.listFiles()) {
            String fileName = file.getName();
            String fileExt = fileName.substring(fileName.lastIndexOf("."));
            if (file.isFile() && extension.equals(fileExt)) {
                listOfFile.add(file);
            }
        }
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
