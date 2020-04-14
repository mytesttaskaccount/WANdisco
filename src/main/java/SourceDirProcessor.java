import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.WeakHashMap;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class SourceDirProcessor {
    private File sourceDir;
    private File targetDir;

    public SourceDirProcessor(File sourceDir, File targetDir) {
        this.sourceDir = sourceDir;
        this.targetDir = targetDir;
    }

    public void process() {
        processFilesInSourceDir(sourceDir);
    }

    private void processFilesInSourceDir(File rootDirectory) {
        if (rootDirectory == null || !rootDirectory.isDirectory()) {
            return;
        }

        File[] files = rootDirectory.listFiles();
        if (files == null) {
            return;
        }

        for (File innerFile : files) {
            if (innerFile.isDirectory()) {
                replicateDir(innerFile, sourceDir, targetDir);
                processFilesInSourceDir(innerFile);
            } else {
                // Process file
                replicateFile(innerFile, sourceDir, targetDir);
            }
        }
    }


    private void replicateDir(File sourceReplDir, File sourceDirectory, File targetDir) {
        if (!sourceReplDir.isDirectory() || !sourceDirectory.isDirectory() || !targetDir.isDirectory()) {
            return;
        }

        String targetDirPath = sourceReplDir.getAbsolutePath().substring(sourceDirectory.getAbsolutePath().length());
        File targetFile = new File(targetDir.getAbsolutePath() + targetDirPath);
        try {
            Files.createDirectories(targetFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void replicateFile(File sourceFile, File sourceDirectory, File targetDir) {
        if (sourceFile == null || targetDir == null || !sourceFile.exists() && !targetDir.isDirectory()) {
            return;
        }
        if (!sourceFile.getAbsolutePath().startsWith(sourceDirectory.getAbsolutePath())) {
            throw new RuntimeException("source file does not belong source directory");
        }
        String targetFilePath = sourceFile.getAbsolutePath().substring(sourceDirectory.getAbsolutePath().length());
        File targetFile = new File(targetDir.getAbsolutePath() + targetFilePath);

        if (!compare2Files(sourceFile, targetFile)) {
            try {
                Path sourceFilePath = sourceFile.toPath();
                Files.createDirectories(targetFile.getParentFile().toPath());
                if (!Files.isSymbolicLink(sourceFilePath)) {
                    // sometimes system does not know what is symbolic links and if source file is symbolic link,
                    // AccessDeniedException will be thrown
                    try{
                        Files.copy(sourceFilePath, targetFile.toPath(), REPLACE_EXISTING);
                    } catch (AccessDeniedException e) {
                        System.out.println("Could not copy " + sourceFilePath.toString() + ": access is denied." +
                            " It may be produces because of symlink");
                    }
                } else {
                    // create symlink
                    targetFile.delete();
                    Files.createLink(targetFile.toPath(), Files.readSymbolicLink(sourceFilePath));
                }
            } catch (IOException e) {
                // file is busy?
                e.printStackTrace();
            }
        } else {
            // files are equal, do nothing
        }
    }


    private boolean compare2Files(File f1, File f2) {
        if (f1 == null || f2 == null || !f1.exists() || !f2.exists()
                || f1.isDirectory() || f2.isDirectory()) {
            return false;
        }
        try (FileInputStream fis1 = new FileInputStream(f1);
             FileInputStream fis2 = new FileInputStream(f2)) {
            while (true) {
                int available1 = fis1.available();
                int available2 = fis2.available();
                if (available1 == 0 || available2 == 0) {
                    // EOF
                    return available1 == available2;
                }
                int available = Math.min(available1, available2);
                byte[] b1 = new byte[available];
                byte[] b2 = new byte[available];
                fis1.read(b1);
                fis2.read(b2);
                if (!Arrays.equals(b1, b2)) {
                    return false;
                }
            }

        } catch (IOException e) {
            // something happened...
            e.printStackTrace();
        }
        return false;
    }
}
