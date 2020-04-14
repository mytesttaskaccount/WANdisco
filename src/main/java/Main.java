import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 2) {
            System.out.println("Please provide 2 paths of source and target folders");
            System.exit(-1);
        }
        File sourceDir = new File(args[0]);
        File targetDir = new File(args[1]);

        if (!sourceDir.isDirectory()) {
            System.out.println("Source file " + sourceDir.getAbsolutePath() + " is not a directory");
            System.exit(-2);
        }

        if (targetDir.exists()) {
            if (!targetDir.isDirectory()) {
                System.out.println("Target file " + targetDir.getAbsolutePath() + " is not a directory");
                System.exit(-3);
            }
        } else {
            try {
                Files.createDirectories(targetDir.toPath());
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Target directory " + targetDir.getAbsolutePath() + " could not be created");
                System.exit(-4);
            }
        }
        while (true) {
            new SourceDirProcessor(sourceDir, targetDir).process();
            new TargetDirProcessor(sourceDir, targetDir).process();
            Thread.sleep(1000);
        }
    }


}
