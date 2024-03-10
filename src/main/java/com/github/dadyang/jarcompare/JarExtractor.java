package com.github.dadyang.jarcompare;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author dadiyang
 * @since 2024/3/10
 */
public class JarExtractor {
    /**
     * to extract a jar to target dir
     *
     * @param targetDir taregt dir
     * @param jarFile   jar file
     * @return the full paths of all the files extracted from the jar
     * @throws IOException
     */
    public static Set<String> extractTo(String jarFile, String targetDir) throws IOException {
        return extractTo(new File(jarFile), new File(targetDir));
    }

    /**
     * to extract a jar to target dir
     *
     * @param targetDir taregt dir
     * @param jarFile   jar file
     * @return the full paths of all the files extracted from the jar
     * @throws IOException
     */
    public static Set<String> extractTo(File jarFile, File targetDir) throws IOException {
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        if (!targetDir.isDirectory()) {
            throw new IllegalArgumentException(targetDir.getAbsolutePath() + " not a dir!");
        }
        targetDir.mkdirs();
        Set<String> files = new HashSet<>();
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                File targetFile = new File(targetDir, entryName);

                if (entry.isDirectory()) {
                    targetFile.mkdirs();
                } else {
                    // ensure the dir exists
                    targetFile.getParentFile().mkdirs();
                    // read content of the JAR file
                    try (InputStream in = jar.getInputStream(entry);
                         OutputStream out = new FileOutputStream(targetFile)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                    String targetFilePath = targetFile.getAbsoluteFile().getAbsolutePath();
                    files.add(targetFilePath);
                    System.out.println("A file was extracted to: " + targetFilePath);
                }
            }
        }
        return files;
    }

}
