package io.github.programmerchenyu.base;

import io.github.programmerchenyu.beans.exception.BeanCreationException;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author 爱吃小鱼的橙子
 */
public class ClassScanner {
    public static List<Class<?>> scanClasses(String basePackage) {
        List<Class<?>> classes = new LinkedList<>();
        String packageName = basePackage;
        String packagePath = packageName.replace('.', '/');
        Enumeration<URL> resources;
        try {
            resources = Thread.currentThread().getContextClassLoader().getResources(packagePath);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();
                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(resource.getFile(), "UTF-8");
                    findAndAddClassesInPackageByFile(packageName, filePath, classes);
                } else if ("jar".equals(protocol)) {
                    JarURLConnection jarURLConnection = (JarURLConnection) resource.openConnection();
                    try (JarFile jarFile = jarURLConnection.getJarFile()) {
                        Enumeration<JarEntry> entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String entryName = entry.getName();
                            if (entryName.startsWith(packagePath) && entryName.endsWith(".class") && !entry.isDirectory()) {
                                String className = entryName.substring(0, entryName.lastIndexOf('.')).replace('/', '.');
                                try {
                                    classes.add(Class.forName(className));
                                } catch (ClassNotFoundException e) {
                                    throw new BeanCreationException("Class not found during scan", e);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new BeanCreationException("Error scanning package " + basePackage, e);
        }
        return classes;
    }

    private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, List<Class<?>> classes) {
        File directory = new File(packagePath);
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        File[] files = directory.listFiles(file -> (file.isFile() && file.getName().endsWith(".class")) || file.isDirectory());
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), classes);
            } else {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(Class.forName(className));
                } catch (ClassNotFoundException e) {
                    throw new BeanCreationException("Class not found: " + className, e);
                }
            }
        }
    }
}
