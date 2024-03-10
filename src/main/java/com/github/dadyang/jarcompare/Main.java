package com.github.dadyang.jarcompare;

/**
 * the launcher to run jar compare process
 *
 * @author dadiyang
 * @since 2024/3/10
 */
public class Main {
    public static void main(String[] args) throws Exception {
        String oldJarPath = "./examples/commons-io-2.7.jar";
        String newJarPath = "./examples/commons-io-2.8.0.jar";
        new JarCompare().compare(oldJarPath, newJarPath);
    }
}
