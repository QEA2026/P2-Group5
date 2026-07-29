package com.revature.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConnectionUtilTest {

    private String originalUserDir;

    @BeforeEach
    void saveUserDir() {
        originalUserDir = System.getProperty("user.dir");
    }

    @AfterEach
    void restoreUserDir() {
        System.setProperty("user.dir", originalUserDir);
    }

    @Test
    void resolvesDatabaseFileWhenItExistsInCurrentDirectory(@TempDir Path tempDir) throws IOException {
        Path dbDirectory = Files.createDirectory(tempDir.resolve("db"));
        Path dbFile = Files.createFile(dbDirectory.resolve("expense_manager.db"));
        System.setProperty("user.dir", tempDir.toString());

        assertEquals(dbFile, ConnectionUtil.resolveDatabasePath());
    }

    @Test
    void resolvesDatabaseDirectoryWhenItExistsButFileDoesNot(@TempDir Path tempDir) throws IOException {
        Path dbDirectory = Files.createDirectory(tempDir.resolve("db"));
        System.setProperty("user.dir", tempDir.toString());

        assertEquals(dbDirectory.resolve("expense_manager.db"), ConnectionUtil.resolveDatabasePath());
    }

    @Test
    void walksUpParentDirectoriesToFindDatabase(@TempDir Path tempDir) throws IOException {
        Path dbDirectory = Files.createDirectory(tempDir.resolve("db"));
        Path dbFile = Files.createFile(dbDirectory.resolve("expense_manager.db"));
        Path nestedWorkingDirectory = Files.createDirectories(tempDir.resolve("child").resolve("grandchild"));
        System.setProperty("user.dir", nestedWorkingDirectory.toString());

        assertEquals(dbFile, ConnectionUtil.resolveDatabasePath());
    }

    @Test
    void fallsBackToCurrentDirectoryWhenNoDatabaseFoundAnywhere(@TempDir Path tempDir) throws IOException {
        Path emptyWorkingDirectory = Files.createDirectory(tempDir.resolve("no-db-anywhere"));
        System.setProperty("user.dir", emptyWorkingDirectory.toString());

        assertEquals(
            emptyWorkingDirectory.resolve("db").resolve("expense_manager.db"),
            ConnectionUtil.resolveDatabasePath()
        );
    }
}
