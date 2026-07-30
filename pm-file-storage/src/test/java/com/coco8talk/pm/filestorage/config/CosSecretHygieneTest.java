package com.coco8talk.pm.filestorage.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

class CosSecretHygieneTest {

    private static final Pattern SECRET_ID =
            Pattern.compile("\\bAKID[A-Za-z0-9]{20,}\\b");
    private static final Pattern SECRET_KEY = Pattern.compile(
            "(?im)^\\s*(?:TENCENT_COS_SECRET_KEY|secretKey)\\s*[:=]\\s*(?!\\$\\{)[^\\s#]+");

    @Test
    void cosConfigurationContainsNoLiteralCredentials() throws IOException {
        Path repositoryRoot = repositoryRoot();
        List<Path> files = filesToScan(repositoryRoot);
        List<String> violations = new ArrayList<>();

        for (Path file : files) {
            String content = Files.readString(file);
            String relativePath = repositoryRoot.relativize(file).toString();
            if (SECRET_ID.matcher(content).find()) {
                violations.add(relativePath + " [SECRET_ID]");
            }
            if (content.lines()
                    .anyMatch(line -> SECRET_KEY.matcher(line).find())) {
                violations.add(relativePath + " [SECRET_KEY]");
            }
        }

        if (!violations.isEmpty()) {
            fail(String.join(System.lineSeparator(), violations));
        }
    }

    private static List<Path> filesToScan(Path repositoryRoot)
            throws IOException {
        List<Path> files = new ArrayList<>();
        Path resources = repositoryRoot.resolve(
                "pm-file-storage/src/main/resources");
        try (Stream<Path> paths = Files.walk(resources)) {
            paths.filter(Files::isRegularFile).forEach(files::add);
        }

        addIfPresent(files,
                repositoryRoot.resolve("config/nacos/pm-file-storage.yaml"));
        addIfPresent(files,
                repositoryRoot.resolve("pm-file-storage/.env.example"));
        return files;
    }

    private static void addIfPresent(List<Path> files, Path file) {
        if (Files.isRegularFile(file)) {
            files.add(file);
        }
    }

    private static Path repositoryRoot() {
        Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        if (Files.isDirectory(workingDirectory.resolve("pm-file-storage"))) {
            return workingDirectory;
        }
        return workingDirectory.getParent();
    }
}
