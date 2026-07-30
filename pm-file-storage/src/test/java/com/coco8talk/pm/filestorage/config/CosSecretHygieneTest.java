package com.coco8talk.pm.filestorage.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
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
            String content = Files.readString(
                    file, StandardCharsets.ISO_8859_1);
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

    @Test
    void scanIncludesTrackedFileOutsideFileStorageModule()
            throws IOException {
        Path repositoryRoot = repositoryRoot();

        assertThat(filesToScan(repositoryRoot))
                .contains(repositoryRoot.resolve("pom.xml"));
    }

    private static List<Path> filesToScan(Path repositoryRoot)
            throws IOException {
        Process process = new ProcessBuilder(
                "git", "ls-files", "--cached", "--others",
                "--exclude-standard", "-z")
                .directory(repositoryRoot.toFile())
                .redirectErrorStream(true)
                .start();
        byte[] output = process.getInputStream().readAllBytes();
        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("git ls-files interrupted", exception);
        }
        if (exitCode != 0) {
            throw new IOException("git ls-files failed");
        }

        return Pattern.compile("\\x00")
                .splitAsStream(new String(output, StandardCharsets.UTF_8))
                .filter(path -> !path.isEmpty())
                .map(repositoryRoot::resolve)
                .filter(Files::isRegularFile)
                .toList();
    }

    private static Path repositoryRoot() {
        Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        if (Files.isDirectory(workingDirectory.resolve("pm-file-storage"))) {
            return workingDirectory;
        }
        return workingDirectory.getParent();
    }
}
