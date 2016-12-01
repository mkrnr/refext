package de.exciteproject.refext.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static void copyFiles(List<File> filesToCopy, File inputDirectory, File outputDirectory) throws IOException {
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        for (File fileToCopy : filesToCopy) {
            File destinationFile = new File(outputDirectory.getAbsolutePath()
                    + fileToCopy.getAbsolutePath().replaceFirst(inputDirectory.getAbsolutePath(), ""));
            org.apache.commons.io.FileUtils.copyFile(fileToCopy, destinationFile);
        }
    }

    public static File getTempFile(String filePrefix, boolean deleteOnExit) {
        String tempFileName = filePrefix + "-" + System.nanoTime();
        File tempFile = null;
        try {
            tempFile = File.createTempFile(tempFileName, ".txt");
            if (deleteOnExit) {
                tempFile.deleteOnExit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFile;
    }

    /**
     * Returns a list of all files contained in directory. Omits directories.
     * Java 8 streaming approach was shown to be faster than e.g. commons-io
     * here: <https://github.com/brettryan/io-recurse-tests>
     *
     * @param directory
     * @return
     * @throws IOException
     */
    public static List<File> listFilesRecursively(File directory) throws IOException {

        List<File> files = new ArrayList<File>();

        // add list of files in directory to files
        Files.walk(Paths.get(directory.getAbsolutePath())).filter(Files::isRegularFile).map(c -> c.toFile())
                .forEachOrdered(files::add);
        return files;
    }

    public static String readFile(File file) throws IOException {
        return FileUtils.readFile(file, Charset.defaultCharset());
    }

    public static String readFile(File file, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        return new String(encoded, encoding);
    }

    public static void renameFiles(File fileDirectory, String regex, String replacement) {
        for (File file : fileDirectory.listFiles()) {
            File newFile = new File(file.getAbsolutePath().replaceFirst(regex, replacement));
            file.renameTo(newFile);
        }
    }

    public static void resetDirectory(File directory) {
        if (directory.exists() && !directory.isDirectory()) {
            throw new IllegalArgumentException("input is not a directory: " + directory.getAbsolutePath());
        }

        if (directory.exists()) {
            try {
                org.apache.commons.io.FileUtils.deleteDirectory(directory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        directory.mkdirs();
    }
}
