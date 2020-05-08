/*
 * Copyright 2020 University of Hildesheim, Software Systems Engineering
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package net.ssehub.ckphd.evaluation.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;

import net.ssehub.ckphd.evaluation.core.ExecutionException;
import net.ssehub.ckphd.evaluation.core.Repository;
import net.ssehub.ckphd.evaluation.core.SetupException;

/**
 * This class contains unit tests for the {@link Repository} class.
 * 
 * @author Christian Kroeher
 *
 */
public class RepositoryTests {
    
    /**
     * The {@link String} denoting the name of the root directory of the extracted repository.
     */
    private static final String TEST_REPOSITORY_DIRECTORY_NAME = "TestRepository";

    /**
     * Tests whether the creation of a new {@link Repository} instance with <code>null</code> as parameter throws an
     * exception.
     */
    @Test
    public void testNullParameter() {
        try {
            Repository repository = new Repository(null);
            assertNull(repository, "Variable should be \"null\"");
        } catch (SetupException e) {
            assertNotNull(e, "SetupException expected");
            assertEquals("The given archive file is \"null\"", e.getMessage(), "Wrong exception message");
        }
    }
    
    /**
     * Tests whether the creation of a new {@link Repository} instance with a non-existing {@link File} as parameter
     * throws an exception.
     */
    @Test
    public void testNonExistingFileParameter() {
        File archiveFile = new File("./this/does/not/exist");
        try {
            Repository repository = new Repository(archiveFile);
            assertNull(repository, "Variable should be \"null\"");
        } catch (SetupException e) {
            assertNotNull(e, "SetupException expected");
            assertEquals("The archive file \"" + archiveFile.getAbsolutePath() + "\" does not exist", e.getMessage(),
                    "Wrong exception message");
        }
    }
    
    /**
     * Tests whether the creation of a new {@link Repository} instance with a {@link File} denoting an existing
     * directory as parameter throws an exception.
     */
    @Test
    public void testDirectoryParameter() {
        File archiveFile = AllTests.TEST_DATA_DIRECTORY;
        try {
            Repository repository = new Repository(archiveFile);
            assertNull(repository, "Variable should be \"null\"");
        } catch (SetupException e) {
            assertNotNull(e, "SetupException expected");
            assertEquals("The archive file \"" + archiveFile.getAbsolutePath() + "\" is not a file", e.getMessage(),
                    "Wrong exception message");
        }
    }
    
    /**
     * Tests whether the creation of a new {@link Repository} instance with a {@link File} denoting an existing
     * file, but not an archive, as parameter throws an exception.
     */
    @Test
    public void testNonArchiveFileParameter() {
        File archiveFile = new File("./README.md");
        try {
            Repository repository = new Repository(archiveFile);
            assertNull(repository, "Variable should be \"null\"");
        } catch (SetupException e) {
            assertNotNull(e, "SetupException expected");
            assertEquals("The archive file \"" + archiveFile.getAbsolutePath() + "\" is not a zip archive",
                    e.getMessage(), "Wrong exception message");
        }
    }
    
    /**
     * Tests whether the creation of a new {@link Repository} instance with a {@link File} denoting an existing, but
     * corrupted archive file, as parameter throws an exception.
     */
    @Test
    public void testCorruptedArchiveFileParameter() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository_corrupted.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        try {
            Repository repository = new Repository(archiveFile);
            assertNull(repository, "Variable should be \"null\"");
        } catch (SetupException e) {
            assertNotNull(e, "SetupException expected");
            assertEquals("Extracting archive file \"" + archiveFile.getAbsolutePath() + "\" failed", e.getMessage(),
                    "Wrong exception message");
        } finally {            
            // Ensure to delete the (partially) extracted archive files again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
        }
    }
    
    /**
     * Tests whether the creation of a new {@link Repository} instance with a {@link File} denoting an existing, valid
     * archive file as parameter is successful (no exception nor other error).
     */
    @Test
    public void testArchiveFileParameter() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        try {
            Repository repository = new Repository(archiveFile);
            assertNotNull(repository, "Repository instance expected");            
        } catch (SetupException e) {
            assertNull(e, "SetupException should be \"null\", but was \"" + e.getMessage() + "\"");
        } finally {            
            // Ensure to delete the extracted archive files again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
        }
    }
    
    /**
     * Tests whether the creation of a new {@link Repository} instance with a {@link File} denoting an existing, valid
     * archive file as parameter results in the availability of the actual repository that instance represents.
     */
    @Test
    public void testRepositoryExistence() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        try {
            assertFalse(expectedExtractedRepositoryDirectory.exists(), "No repository (directory) expected");
            @SuppressWarnings("unused")
            Repository repository = new Repository(archiveFile);
            assertTrue(expectedExtractedRepositoryDirectory.exists(), "Repository (directory) expected");            
        } catch (SetupException e) {
            assertNull(e, "SetupException should be \"null\", but was \"" + e.getMessage() + "\"");
        } finally {            
            // Ensure to delete the extracted archive files again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
        }
    }
    
    /**
     * Tests whether {@link Repository#addHook(String)} throws an exception, if <code>null</code> is given as a
     * parameter.
     */
    @Test
    public void testNullHookAddition() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        // This file should actually not be available, which will be tested below
        File potentialHookFile = new File(expectedExtractedRepositoryDirectory, "/.git/hooks/pre-commit"); 
        try {
            Repository repository = new Repository(archiveFile);
            assertFalse(potentialHookFile.exists(), "No hook file expected");
            repository.addHook(null);
            assertFalse(potentialHookFile.exists(), "No hook file expected");
        } catch (SetupException e) {
            assertNull(e, "SetupException should be \"null\", but was \"" + e.getMessage() + "\"");
        } catch (ExecutionException e) {
            assertNotNull(e, "ExecutionException expected");
            assertEquals("The given hook actions are \"null\"", e.getMessage(), "Wrong exception message");
        } finally {            
            // Ensure to delete the extracted archive files again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
        }
    }
    
    /**
     * Tests whether {@link Repository#addHook(String)} throws an exception, if an empty string is given as a
     * parameter.
     */
    @Test
    public void testEmptyHookAddition() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        // This file should actually not be available, which will be tested below
        File potentialHookFile = new File(expectedExtractedRepositoryDirectory, "/.git/hooks/pre-commit"); 
        try {
            Repository repository = new Repository(archiveFile);
            assertFalse(potentialHookFile.exists(), "No hook file expected");
            repository.addHook("");
            assertFalse(potentialHookFile.exists(), "No hook file expected");
        } catch (SetupException e) {
            assertNull(e, "SetupException should be \"null\", but was \"" + e.getMessage() + "\"");
        } catch (ExecutionException e) {
            assertNotNull(e, "ExecutionException expected");
            assertEquals("The given hook actions are empty", e.getMessage(), "Wrong exception message");
        } finally {            
            // Ensure to delete the extracted archive files again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
        }
    }
    
    /**
     * Tests whether {@link Repository#addHook(String)} throws an exception, if a blank string is given as a
     * parameter.
     */
    @Test
    public void testBlankHookAddition() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        // This file should actually not be available, which will be tested below
        File potentialHookFile = new File(expectedExtractedRepositoryDirectory, "/.git/hooks/pre-commit"); 
        try {
            Repository repository = new Repository(archiveFile);
            assertFalse(potentialHookFile.exists(), "No hook file expected");
            repository.addHook("    ");
            assertFalse(potentialHookFile.exists(), "No hook file expected");
        } catch (SetupException e) {
            assertNull(e, "SetupException should be \"null\", but was \"" + e.getMessage() + "\"");
        } catch (ExecutionException e) {
            assertNotNull(e, "ExecutionException expected");
            assertEquals("The given hook actions are empty", e.getMessage(), "Wrong exception message");
        } finally {            
            // Ensure to delete the extracted archive files again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
        }
    }
    
    /**
     * Tests whether {@link Repository#addHook(String)} adds a non-empty, valid string as hook actions to the extracted
     * repository.
     */
    @Test
    public void testHookAddition() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        File expectedHookFile = new File(expectedExtractedRepositoryDirectory, "/.git/hooks/pre-commit");
        String hookActions = "git --version";
        String expectedHookFileContent = "#!/bin/sh" + System.lineSeparator() + hookActions; 
        try {
            Repository repository = new Repository(archiveFile);
            assertFalse(expectedHookFile.exists(), "No hook file expected");
            repository.addHook(hookActions);
            assertTrue(expectedHookFile.exists(), "Hook file expected");
            String actualHookFileContent = Files.readString(expectedHookFile.toPath());
            assertEquals(expectedHookFileContent, actualHookFileContent, "Wrong hook file content");
        } catch (SetupException e) {
            assertNull(e, "SetupException should be \"null\", but was \"" + e.getMessage() + "\"");
        } catch (ExecutionException e) {
            assertNull(e, "ExecutionException should be \"null\", but was \"" + e.getMessage() + "\"");
        } catch (IOException e) {
            assertNull(e, "Reading expected hook file content failed: " + e.getMessage() + "\"");
        } finally {            
            // Ensure to delete the extracted archive files again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
        }
    }
    
    /**
     * Tests whether {@link Repository#applyCommit(File)} throws an exception, if <code>null</code> is given as a
     * parameter.
     */
    @Test
    public void testNullCommitApplication() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        try {
            Repository repository = new Repository(archiveFile);
            repository.applyCommit(null);
            fail("A parameter value of \"null\" should lead to an ExecutionException");
        } catch (SetupException e) {
            assertNull(e, "SetupException should be \"null\", but was \"" + e.getMessage() + "\"");
        } catch (ExecutionException e) {
            assertNotNull(e, "ExecutionException should be \"null\", but was \"" + e.getMessage() + "\"");
            assertEquals("The given commit file is \"null\"", e.getMessage(), "Wrong exception message");
        } finally {            
            // Ensure to delete the extracted archive files again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
        }
    }
    
    /**
     * Tests whether {@link Repository#applyCommit(File)} throws an exception, if a {@link File} denoting a directory
     * is given as a parameter.
     */
    @Test
    public void testDirectoryCommitApplication() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        File testCommitFile = AllTests.TEST_DATA_DIRECTORY;
        try {
            Repository repository = new Repository(archiveFile);
            repository.applyCommit(testCommitFile);
            fail("A parameter value denoting a directory should lead to an ExecutionException");
        } catch (SetupException e) {
            assertNull(e, "SetupException should be \"null\", but was \"" + e.getMessage() + "\"");
        } catch (ExecutionException e) {
            assertNotNull(e, "ExecutionException should be \"null\", but was \"" + e.getMessage() + "\"");
            assertEquals("The given commit file \"" + testCommitFile.getAbsolutePath() + "\" is not a file",
                    e.getMessage(), "Wrong exception message");
        } finally {            
            // Ensure to delete the extracted archive files again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
        }
    }
    
    /**
     * Tests whether {@link Repository#applyCommit(File)} throws an exception, if a {@link File} denoting a non-existing
     * file is given as a parameter.
     */
    @Test
    public void testNonExistingCommitApplication() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        File testCommitFile = new File("./this/does/not/exist");
        try {
            Repository repository = new Repository(archiveFile);
            repository.applyCommit(testCommitFile);
            fail("A parameter value denoting a non-existing file should lead to an ExecutionException");
        } catch (SetupException e) {
            assertNull(e, "SetupException should be \"null\", but was \"" + e.getMessage() + "\"");
        } catch (ExecutionException e) {
            assertNotNull(e, "ExecutionException should be \"null\", but was \"" + e.getMessage() + "\"");
            assertEquals("The given commit file \"" + testCommitFile.getAbsolutePath() + "\" does not exist",
                    e.getMessage(), "Wrong exception message");
        } finally {            
            // Ensure to delete the extracted archive files again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
        }
    }
    
    /**
     * Tests whether {@link Repository#applyCommit(File)} applies an existing, valid (commit) file (content) to the
     * extracted repository.
     */
    @Test
    public void testCommitApplication() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        File testCommitFile = new File(archiveFile.getParentFile(), "testcommit");
        // This file will be created in the repository by applying the test commit
        File expectedNewFile = new File(expectedExtractedRepositoryDirectory, "EmptyTextFile.txt");
        try {
            Repository repository = new Repository(archiveFile);
            assertFalse(expectedNewFile.exists(), "No new file expected");
            repository.applyCommit(testCommitFile);
            assertTrue(expectedNewFile.exists(), "New file expected");
        } catch (SetupException e) {
            assertNull(e, "SetupException should be \"null\", but was \"" + e.getMessage() + "\"");
        } catch (ExecutionException e) {
            assertNull(e, "ExecutionException should be \"null\", but was \"" + e.getMessage() + "\"");
        } finally {            
            // Ensure to delete the extracted archive files again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
        }
    }
    
    /**
     * Tests whether {@link Repository#delete()} deletes an extracted repository successfully.
     */
    @Test
    public void testRepositoryDeletion() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        try {
            Repository repository = new Repository(archiveFile);
            assertTrue(expectedExtractedRepositoryDirectory.exists(), "Repository should be available");
            assertTrue(repository.delete(), "Successful repository deletion expected");
            assertFalse(expectedExtractedRepositoryDirectory.exists(), "Repository should be deleted");
        } catch (SetupException e) {
            assertNull(e, "SetupException should be \"null\", but was \"" + e.getMessage() + "\"");
        } finally {            
            // Ensure to delete the extracted archive files again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
        }
    }
    
}
