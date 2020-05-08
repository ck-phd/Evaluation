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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;

import org.junit.Test;

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
     * file, but no archive as parameter throws an exception.
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
     * corrupted archive file as parameter throws an exception.
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
        }
        // Ensure to delete the extracted archive files again
        AllTests.delete(expectedExtractedRepositoryDirectory);
    }
    
    /**
     * Tests whether the creation of a new {@link Repository} instance with a {@link File} denoting an existing and
     * sound archive file as parameter is successful (no exception or other error).
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
        }
        // Ensure to delete the extracted archive files again
        AllTests.delete(expectedExtractedRepositoryDirectory);
    }
}
