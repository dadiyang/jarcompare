package com.github.dadyang.jarcompare;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * the main class to compare jars
 *
 * @author dadiyang
 * @since 2024/3/10
 */
public class JarCompare {
    /**
     * by default, only class, java and xml(Mybatis) files were needed to be compared
     */
    private List<String> neededSuffix = Arrays.asList(".class", ".java", ".xml");

    public JarCompare() {
    }

    public JarCompare(List<String> neededSuffix) {
        this.neededSuffix = neededSuffix;
    }

    /**
     * compare the jars
     */
    public void compare(String oldJarPath, String newJarPath) throws Exception {
        String resultPath = "./resultPath";
        File resultDir = new File(resultPath);
        if (!resultDir.exists()) {
            resultDir.mkdirs();
        }
        String oldPath = resultDir + "/theOld";
        String newPath = resultDir + "/theNew";
        // caution! delete the outdated data the prevent dirty data
        FileUtils.deleteDirectory(new File(oldPath));
        FileUtils.deleteDirectory(new File(newPath));

        JarExtractor.extractTo(oldJarPath, oldPath);
        JarExtractor.extractTo(newJarPath, newPath);

        FileIterator fileIterator = new FileIterator();

        Map<String, String> oldFilesMap = fileIterator.iterator(new File(oldPath), oldPath, neededSuffix);
        Map<String, String> newFilesMap = fileIterator.iterator(new File(newPath), newPath, neededSuffix);

        Map<String, String> diff = new HashMap<>();
        for (Map.Entry<String, String> oldFe : oldFilesMap.entrySet()) {
            if (!newFilesMap.containsKey(oldFe.getKey())) {
                diff.put(oldFe.getKey(), "~~THE FILE HAS BEEN DELETED~~");
            }
        }
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .oldTag(f -> "~~")
                .newTag(f -> "**")
                .build();

        for (Map.Entry<String, String> newFe : newFilesMap.entrySet()) {
            if (!oldFilesMap.containsKey(newFe.getKey())) {
                diff.put(newFe.getKey(), "**A NEW FILE**");
                continue;
            }
            String old;
            String nw;
            if (newFe.getKey().endsWith(".class")) {
                old = new Decompiler().decompile(oldFilesMap.get(newFe.getKey()));
                nw = new Decompiler().decompile(newFe.getValue());
            } else {
                old = String.join("\n", IOUtils.readLines(Files.newInputStream(Paths.get(oldFilesMap.get(newFe.getKey()))), StandardCharsets.UTF_8));
                nw = String.join("\n", IOUtils.readLines(Files.newInputStream(Paths.get(newFe.getValue())), StandardCharsets.UTF_8));
            }
            List<DiffRow> rows = generator.generateDiffRows(
                    Arrays.asList(old.split("\n")),
                    Arrays.asList(nw.split("\n")));
            // remove the first line, the package declaration, because it will be replaced with the path of the class file by jd-core
            rows.remove(0);
            if (rows.stream().noneMatch(r -> r.getTag() != DiffRow.Tag.EQUAL)) {
                continue;
            }
            StringBuilder diffContent = new StringBuilder("|rowNo|original|new|\n");
            diffContent.append("|---|--------|---|\n");
            int rowNum = 0;
            for (DiffRow row : rows) {
                rowNum++;
                if (row.getTag() != DiffRow.Tag.EQUAL) {
                    diffContent.append("|").append(rowNum).append("|").append(row.getOldLine()).append("|").append(row.getNewLine()).append("|\n");
                }
            }
            diff.put(newFe.getKey(), diffContent.toString());
        }
        StringBuilder resultContent = new StringBuilder();
        resultContent.append("THERE ARE [").append(oldFilesMap.size()).append("] FILES ON THE OLD PATH [").append(oldPath).append("]\n\n");
        resultContent.append("AND ").append(newFilesMap.size()).append(" FILES ON THE NEW PATH [").append(newPath).append("]. \n\n");
        resultContent.append("[").append(diff.size()).append("] FILE(S) ARE DISCREPANT: \n\n");
        for (Map.Entry<String, String> entry : diff.entrySet()) {
            resultContent.append("## ").append(entry.getKey()).append("\n");
            resultContent.append(entry.getValue()).append("\n");
        }
        File resultFile = new File(resultPath + "/compareResult.md");
        try (FileWriter fileWriter = new FileWriter(resultFile);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(resultContent.toString());
        }
        System.out.println(resultContent);
        System.out.println("RESULT FILE HAS BEEN SAVED TO [" + resultFile.getCanonicalFile() + "], PLS USE ANY **MARKDOWN** READER TO OPEN IT");
    }
}