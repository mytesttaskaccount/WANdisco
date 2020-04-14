import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class TargetDirProcessor {

    private File sourceDir;
    private File targetDir;

    public TargetDirProcessor(File sourceDir, File targetDir) {
        this.sourceDir = sourceDir;
        this.targetDir = targetDir;
    }

    public void process() {
        processFilesInTargetDir(targetDir);
    }

    private void processFilesInTargetDir(File rootDirectory) {
        if (rootDirectory == null || !rootDirectory.isDirectory()) {
            return;
        }

        File[] files = rootDirectory.listFiles();
        if (files == null) {
            return;
        }

        for (File innerFile : files) {
            if (innerFile.isDirectory()) {
                processFilesInTargetDir(innerFile);
            }
            removeUnnecessary(innerFile, targetDir, sourceDir);
        }
    }






    private void removeUnnecessary(File sourceFile, File sourceDirectory, File targetDir) {
        if (sourceFile == null || targetDir == null || !sourceFile.exists() && !targetDir.isDirectory()) {
            return;
        }
        if (!sourceFile.getAbsolutePath().startsWith(sourceDirectory.getAbsolutePath())) {
            throw new RuntimeException("source file " + sourceFile.getAbsolutePath() + " does not belong source directory" + sourceDirectory.getAbsolutePath());
        }
        String targetFilePath = sourceFile.getAbsolutePath().substring(sourceDirectory.getAbsolutePath().length());
        File targetFile = new File(targetDir.getAbsolutePath() + targetFilePath);

        if (!targetFile.exists()) {
            sourceFile.delete();
        }
    }
}
