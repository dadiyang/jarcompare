package com.github.dadyang.jarcompare;

import java.io.File;
import java.util.*;

/**
 * @author dadiyang
 * @since 2024/3/10
 */
public class FileIterator {
    public Map<String, String> iterator(File filePath, String filePathPre, List<String> endWiths) {
        Map<String, String> rs = new HashMap<>();
        if (filePath.isDirectory()) {
            for (File file : Objects.requireNonNull(filePath.listFiles())) {
                if (file.isDirectory()) {
                    rs.putAll(iterator(file, filePathPre, endWiths));
                } else {
                    put(file, filePathPre, endWiths, rs);
                }
            }
        } else {
            put(filePath, filePathPre, endWiths, rs);
        }
        return rs;
    }

    private static void put(File filePath, String filePathPre, List<String> endWiths, Map<String, String> rs) {
        boolean needed = false;
        if (endWiths != null && !endWiths.isEmpty()) {
            for (String endWith : endWiths) {
                if (filePath.getAbsolutePath().endsWith(endWith)) {
                    needed = true;
                    break;
                }
            }
        } else {
            needed = true;
        }
        if (needed) {
            rs.put(filePath.getAbsoluteFile().getAbsolutePath().replaceFirst(filePathPre, ""), filePath.getAbsoluteFile().getAbsolutePath());
        }
    }
}