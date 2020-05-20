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

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

import net.ssehub.ckphd.evaluation.core.Evaluation;
import net.ssehub.ckphd.evaluation.core.ExecutionException;
import net.ssehub.ckphd.evaluation.core.SetupException;
import net.ssehub.ckphd.evaluation.utilities.FileUtilities;
import net.ssehub.ckphd.evaluation.utilities.FileUtilitiesException;

/**
 * This class contains unit tests for the {@link Evaluation} class.
 * 
 * @author Christian Kroeher
 *
 */
public class EvaluationTests {

    /*
     * TODO Implement the following tests:
     * 
     * Further, we need something like a "ScenarioTest", where a full sequence of commits is applied iteratively and
     * the hook actions enable the tests to check, whether the correct commit in the correct order are applied.
     */
    
    /**
     * The {@link String} denoting the name of the root directory of the extracted repository.
     */
    private static final String TEST_REPOSITORY_DIRECTORY_NAME = "TestRepository";
    
    /**
     * Tests whether the creation of a new {@link Evaluation} instance with <code>null</code> as parameters throws an
     * exception.
     */
    @Test
    public void testNullParameters() {
        try {
            Evaluation evaluation = new Evaluation(null, null);
            assertNull(evaluation, "Variable should be \"null\"");
        } catch (SetupException e) {
            assertNotNull(e, "SetupException expected");
            assertEquals("The given archive file is \"null\"", e.getMessage(), "Wrong exception message");
        }
    }
    
    /**
     * Tests whether the creation of a new {@link Evaluation} instance with a non-existing {@link File} as archive file
     * parameter and <code>null</code> as commit sequence directory parameter throws an exception.
     */
    @Test
    public void testNonExistingArchiveFileParameter() {
        File archiveFile = new File("./this/does/not/exist");
        try {
            Evaluation evaluation = new Evaluation(archiveFile, null);
            assertNull(evaluation, "Variable should be \"null\"");
        } catch (SetupException e) {
            assertNotNull(e, "SetupException expected");
            assertEquals("The archive file \"" + archiveFile.getAbsolutePath() + "\" does not exist", e.getMessage(),
                    "Wrong exception message");
        }
    }
    
    /**
     * Tests whether the creation of a new {@link Evaluation} instance with a {@link File} denoting an existing
     * directory as archive file parameter and <code>null</code> as commit sequence directory parameter throws an
     * exception.
     */
    @Test
    public void testDirectoryArchiveFileParameter() {
        File archiveFile = AllTests.TEST_DATA_DIRECTORY;
        try {
            Evaluation evaluation = new Evaluation(archiveFile, null);
            assertNull(evaluation, "Variable should be \"null\"");
        } catch (SetupException e) {
            assertNotNull(e, "SetupException expected");
            assertEquals("The archive file \"" + archiveFile.getAbsolutePath() + "\" is not a file", e.getMessage(),
                    "Wrong exception message");
        }
    }
    
    /**
     * Tests whether the creation of a new {@link Evaluation} instance with a {@link File} denoting an existing
     * file, but not an archive, as archive file parameter and <code>null</code> as commit sequence directory parameter
     * throws an exception.
     */
    @Test
    public void testNonArchiveFileParameter() {
        File archiveFile = new File("./README.md");
        try {
            Evaluation evaluation = new Evaluation(archiveFile, null);
            assertNull(evaluation, "Variable should be \"null\"");
        } catch (SetupException e) {
            assertNotNull(e, "SetupException expected");
            assertEquals("The archive file \"" + archiveFile.getAbsolutePath() + "\" is not a zip archive",
                    e.getMessage(), "Wrong exception message");
        }
    }
    
    /**
     * Tests whether the creation of a new {@link Evaluation} instance with a {@link File} denoting an existing, but
     * corrupted archive file, as archive file parameter and <code>null</code> as commit sequence directory parameter
     * throws an exception.
     */
    @Test
    public void testCorruptedArchiveFileParameter() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository_corrupted.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        try {
            Evaluation evaluation = new Evaluation(archiveFile, null);
            assertNull(evaluation, "Variable should be \"null\"");
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
     * Tests whether the creation of a new {@link Evaluation} instance with a {@link File} denoting an existing, valid
     * archive file as archive file parameter and <code>null</code> as commit sequence directory parameter
     * throws an exception.
     */
    @Test
    public void testArchiveFileNullCommitSequenceDirectoryParameter() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        try {
            Evaluation evaluation = new Evaluation(archiveFile, null);
            assertNull(evaluation, "Variable should be \"null\"");            
        } catch (SetupException e) {
            assertNotNull(e, "SetupException expected");
            assertEquals("The given commit sequence directory is \"null\"", e.getMessage(), "Wrong exception message");
        } finally {            
            // Ensure to delete the extracted archive files again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
        }
    }
    
    /**
     * Tests whether the creation of a new {@link Evaluation} instance with a {@link File} denoting an existing, valid
     * archive file as archive file parameter and a non-existing {@link File} as commit sequence directory parameter
     * throws an exception.
     */
    @Test
    public void testArchiveFileNonExistingCommitSequenceDirectoryParameter() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        File commitSequenceDirectory = new File("./this/does/not/exist");
        try {
            Evaluation evaluation = new Evaluation(archiveFile, commitSequenceDirectory);
            assertNull(evaluation, "Variable should be \"null\"");            
        } catch (SetupException e) {
            assertNotNull(e, "SetupException expected");
            assertEquals("The commit sequence directory \"" + commitSequenceDirectory.getAbsolutePath()
                    + "\" does not exist", e.getMessage(), "Wrong exception message");
        } finally {            
            // Ensure to delete the extracted archive files again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
        }
    }
    
    /**
     * Tests whether the creation of a new {@link Evaluation} instance with a {@link File} denoting an existing, valid
     * archive file as archive file parameter and a {@link File} denoting an existing file as commit sequence directory
     * parameter throws an exception.
     */
    @Test
    public void testArchiveFileFileCommitSequenceDirectoryParameter() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        File commitSequenceDirectory = new File("./README.md");
        try {
            Evaluation evaluation = new Evaluation(archiveFile, commitSequenceDirectory);
            assertNull(evaluation, "Variable should be \"null\"");            
        } catch (SetupException e) {
            assertNotNull(e, "SetupException expected");
            assertEquals("The commit sequence directory \"" + commitSequenceDirectory.getAbsolutePath()
                    + "\" is not a directory", e.getMessage(), "Wrong exception message");
        } finally {            
            // Ensure to delete the extracted archive files again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
        }
    }
    
    /**
     * Tests whether the creation of a new {@link Evaluation} instance with a {@link File} denoting an existing, valid
     * archive file as archive file parameter and a {@link File} denoting an existing, but empty directory as commit
     * sequence directory parameter throws an exception.
     */
    @Test
    public void testArchiveFileEmptyCommitSequenceDirectoryParameter() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        File commitSequenceDirectory = new File(AllTests.TEST_DATA_DIRECTORY, "emptyDir");
        assertTrue(commitSequenceDirectory.mkdir(), "Could not create empty commit sequence directory");
        try {
            Evaluation evaluation = new Evaluation(archiveFile, commitSequenceDirectory);
            assertNull(evaluation, "Variable should be \"null\"");            
        } catch (SetupException e) {
            assertNotNull(e, "SetupException expected");
            assertEquals("The commit sequence directory \"" + commitSequenceDirectory.getAbsolutePath()
                    + "\" is empty", e.getMessage(), "Wrong exception message");
        } finally {            
            // Ensure to delete the extracted archive files and the empty commit sequence directory again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
            assertTrue(AllTests.delete(commitSequenceDirectory),
                    "Deleting the extracted empty commit sequence directory failed");
        }
    }
    
    /**
     * Tests whether the creation of a new {@link Evaluation} instance with a {@link File} denoting an existing, valid
     * archive file as archive file parameter and a {@link File} denoting an existing, non-empty directory, but without
     * commit sequences as commit sequence directory parameter throws an exception.
     */
    @Test
    public void testArchiveFileNonCommitSequenceDirectoryParameter() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        File commitSequenceDirectory = AllTests.TEST_DATA_DIRECTORY;
        try {
            Evaluation evaluation = new Evaluation(archiveFile, commitSequenceDirectory);
            assertNull(evaluation, "Variable should be \"null\"");            
        } catch (SetupException e) {
            assertNotNull(e, "SetupException expected");
            assertEquals("Setting up the commit sequence failed", e.getMessage(), "Wrong exception message");
        } finally {            
            // Ensure to delete the extracted archive files again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
        }
    }
    
    /**
     * Tests whether the creation of a new {@link Evaluation} instance with a {@link File} denoting an existing, valid
     * archive file as archive file parameter and a {@link File} denoting a directory, which only contains the commit
     * sequence list, but no commit file as commit sequence directory parameter throws an exception.
     */
    @Test
    public void testArchiveFileMissingCommitFileCommitSequenceDirectoryParameter() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        File commitSequenceDirectory = new File(AllTests.TEST_DATA_DIRECTORY, "test_commit-sequence_incomplete-1");
        File commitSequenceFile = new File(commitSequenceDirectory, "CommitSequence_1.txt");
        List<String> commitSequenceList = null;
        try {
            commitSequenceList = FileUtilities.getInstance().readFile(commitSequenceFile);
            assertNotNull(commitSequenceList, "Commit sequence list expected");
        } catch (FileUtilitiesException e) {
            assertNull(e, "No FileUtilitiesException expected");
        }
        try {
            Evaluation evaluation = new Evaluation(archiveFile, commitSequenceDirectory);
            assertNull(evaluation, "Variable should be \"null\"");            
        } catch (SetupException e) {
            assertNotNull(e, "SetupException expected");
            assertEquals("The number of commits in the commit sequence file \"" 
                    + commitSequenceFile.getAbsolutePath() + "\" (" + commitSequenceList.size()
                    + ") does not match the number of commit files in \""
                    + commitSequenceDirectory.getAbsolutePath() + "\" (" 
                    + (commitSequenceDirectory.list().length - 1) + ")", e.getMessage(), "Wrong exception message");
        } finally {            
            // Ensure to delete the extracted archive files again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
        }
    }
    
    /**
     * Tests whether the creation of a new {@link Evaluation} instance with a {@link File} denoting an existing, valid
     * archive file as archive file parameter and a {@link File} denoting a directory, which only contains a commit
     * file, but no commit sequence file as commit sequence directory parameter throws an exception.
     */
    @Test
    public void testArchiveFileMissingCommitSequenceFileCommitSequenceDirectoryParameter() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        File commitSequenceDirectory = new File(AllTests.TEST_DATA_DIRECTORY, "test_commit-sequence_incomplete-2");
        try {
            Evaluation evaluation = new Evaluation(archiveFile, commitSequenceDirectory);
            assertNull(evaluation, "Variable should be \"null\"");            
        } catch (SetupException e) {
            assertNotNull(e, "SetupException expected");
            assertEquals("Setting up the commit sequence failed", e.getMessage(), "Wrong exception message");
        } finally {            
            // Ensure to delete the extracted archive files again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
        }
    }
    
    /**
     * Tests whether the creation of a new {@link Evaluation} instance with a {@link File} denoting an existing, valid
     * archive file as archive file parameter and a {@link File} denoting a directory, which contains the commit
     * sequence list and a commit file, which is not listed in the commit sequence, as commit sequence directory
     * parameter throws an exception.
     */
    @Test
    public void testArchiveFileWrongCommitFileCommitSequenceDirectoryParameter() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        File commitSequenceDirectory = new File(AllTests.TEST_DATA_DIRECTORY, "test_commit-sequence_incomplete-3");
        File commitSequenceFile = new File(commitSequenceDirectory, "CommitSequence_1.txt");
        List<String> commitSequenceList = null;
        try {
            commitSequenceList = FileUtilities.getInstance().readFile(commitSequenceFile);
            assertNotNull(commitSequenceList, "Commit sequence list expected");
        } catch (FileUtilitiesException e) {
            assertNull(e, "No FileUtilitiesException expected");
        }
        String commit = commitSequenceList.get(0);
        try {
            Evaluation evaluation = new Evaluation(archiveFile, commitSequenceDirectory);
            assertNull(evaluation, "Variable should be \"null\"");            
        } catch (SetupException e) {
            assertNotNull(e, "SetupException expected");
            assertEquals("The commit file for commit \"" + commit + "\" is not available", e.getMessage(),
                    "Wrong exception message");
        } finally {            
            // Ensure to delete the extracted archive files again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
        }
    }
    
    /**
     * Tests whether the creation of a new {@link Evaluation} instance with a {@link File} denoting an existing, valid
     * archive file as archive file parameter and a {@link File} denoting an existing directory with valid content as
     * commit sequence directory is successful (no exception nor other error).
     */
    @Test
    public void testArchiveFileValidCommitSequenceDirectoryParameter() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        File commitSequenceDirectory = new File(AllTests.TEST_DATA_DIRECTORY, "test_commit-sequence_valid");
        try {
            Evaluation evaluation = new Evaluation(archiveFile, commitSequenceDirectory);
            assertNotNull(evaluation, "Evaluation instance expected");            
        } catch (SetupException e) {
            assertNull(e, "SetupException should be \"null\", but was \"" + e.getMessage() + "\"");
        } finally {            
            // Ensure to delete the extracted archive files again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
        }
    }
    
    /**
     * Tests whether {@link Evaluation#run(String)} throws an exception, if <code>null</code> is given as a parameter.
     */
    @Test
    public void testNullHookRun() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        File commitSequenceDirectory = new File(AllTests.TEST_DATA_DIRECTORY, "test_commit-sequence_valid");
        try {
            assertFalse(expectedExtractedRepositoryDirectory.exists(), "No repository directory expected");
            Evaluation evaluation = new Evaluation(archiveFile, commitSequenceDirectory);
            assertTrue(expectedExtractedRepositoryDirectory.exists(), "Repository directory expected");
            evaluation.run(null);
            fail("ExecutionException expected");
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
     * Tests whether {@link Evaluation#run(String)} throws an exception, if an empty string is given as a parameter.
     */
    @Test
    public void testEmptyHookRun() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        File commitSequenceDirectory = new File(AllTests.TEST_DATA_DIRECTORY, "test_commit-sequence_valid");
        try {
            assertFalse(expectedExtractedRepositoryDirectory.exists(), "No repository directory expected");
            Evaluation evaluation = new Evaluation(archiveFile, commitSequenceDirectory);
            assertTrue(expectedExtractedRepositoryDirectory.exists(), "Repository directory expected");
            evaluation.run("");
            fail("ExecutionException expected");
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
     * Tests whether {@link Evaluation#run(String)} throws an exception, if a blank string is given as a parameter.
     */
    @Test
    public void testBlankHookRun() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        File commitSequenceDirectory = new File(AllTests.TEST_DATA_DIRECTORY, "test_commit-sequence_valid");
        try {
            assertFalse(expectedExtractedRepositoryDirectory.exists(), "No repository directory expected");
            Evaluation evaluation = new Evaluation(archiveFile, commitSequenceDirectory);
            assertTrue(expectedExtractedRepositoryDirectory.exists(), "Repository directory expected");
            evaluation.run("    ");
            fail("ExecutionException expected");
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
     * Tests whether {@link Evaluation#run(String)} throws an exception, if a string denoting an invalid bash script
     * command is given as a parameter.
     */
    @Test
    public void testInvalidHookRun() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        File commitSequenceDirectory = new File(AllTests.TEST_DATA_DIRECTORY, "test_commit-sequence_valid");
        String hookContent = "abc";
        try {
            assertFalse(expectedExtractedRepositoryDirectory.exists(), "No repository directory expected");
            Evaluation evaluation = new Evaluation(archiveFile, commitSequenceDirectory);
            assertTrue(expectedExtractedRepositoryDirectory.exists(), "Repository directory expected");
            evaluation.run(hookContent);
            fail("ExecutionException expected");
        } catch (SetupException e) {
            assertNull(e, "SetupException should be \"null\", but was \"" + e.getMessage() + "\"");
        } catch (ExecutionException e) {
            assertNotNull(e, "ExecutionException expected");
            assertEquals("Committing changes to repository failed: .git/hooks/pre-commit: line 2: " + hookContent 
                    + ": command not found" + System.lineSeparator(), e.getMessage(), "Wrong exception message");
        } finally {            
            // Ensure to delete the extracted archive files again
            assertTrue(AllTests.delete(expectedExtractedRepositoryDirectory),
                    "Deleting the extracted archive files failed");
        }
    }
    
    /**
     * Tests whether {@link Evaluation#run(String)} is executed successfully (no errors or exceptions), if a valid bash
     * script command is given as a parameter.
     */
    @Test
    public void testValidHookRun() {
        File archiveFile = new File(AllTests.TEST_DATA_DIRECTORY, "repository.zip");
        File expectedExtractedRepositoryDirectory = new File(archiveFile.getParentFile(),
                TEST_REPOSITORY_DIRECTORY_NAME);
        File commitSequenceDirectory = new File(AllTests.TEST_DATA_DIRECTORY, "test_commit-sequence_valid");
        String hookContent = "exit 0";
        try {
            Evaluation evaluation = new Evaluation(archiveFile, commitSequenceDirectory);
            assertTrue(expectedExtractedRepositoryDirectory.exists(), "Repository directory expected");
            evaluation.run(hookContent);
            assertFalse(expectedExtractedRepositoryDirectory.exists(), "No repository directory expected");
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
    
}
