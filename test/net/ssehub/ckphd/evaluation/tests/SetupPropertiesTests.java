/*
 * Copyright 2021 University of Hildesheim, Software Systems Engineering
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import net.ssehub.ckphd.evaluation.core.SetupException;
import net.ssehub.ckphd.evaluation.utilities.FileUtilities;
import net.ssehub.ckphd.evaluation.utilities.FileUtilitiesException;
import net.ssehub.ckphd.evaluation.utilities.Setup;
import net.ssehub.ckphd.evaluation.utilities.Setup.CommitHookType;
import net.ssehub.ckphd.evaluation.utilities.Setup.StreamType;
import net.ssehub.ckphd.evaluation.utilities.Setup.TaskType;

/**
 * This class contains unit tests for the creation of {@link Setup} instances. This focuses on testing the correct
 * provision of setup properties, if creating an instance was successful. Hence, only valid test configuration files are
 * used in these unit tests. Testing incomplete or invalid configuration files is part of the 
 * {@link SetupCreationTests}.
 * 
 * @author Christian Kroeher
 *
 */
@RunWith(Parameterized.class)
public class SetupPropertiesTests {

    /**
     * The constant string representing the absolute path to the main test data directory as defined by
     * {@link AllTests#TEST_DATA_DIRECTORY}.
     */
    private static final String TEST_DATA_DIRECTORY_PATH = AllTests.TEST_DATA_DIRECTORY.getAbsolutePath();
    
    /**
     * The constant string representing the absolute path to the directory containing the test configuration files
     * used for unit testing in this class.
     */
    private static final String TEST_CONFIGURATIONS_DIRECTORY_NAME = "configurations";
    
    /**
     * The constant set of expected test value sets. Each subset contains the expected values for a specific test
     * configuration file as follows:
     * <ul>
     * <li>Name of the test Configuration file</li>
     * <li>The type of task to execute</li>
     * <li>The file denoting the output directory</li>
     * <li>The type of stream to use for logging standard information</li>
     * <li>The type of stream to use for logging debug information</li>
     * <li>If the task type is {@link TaskType#GENERATION}, the file denoting the repository from which a commit
     *     sequence shall be generated; <code>null</code> otherwise</li>
     * <li>If the task type is {@link TaskType#GENERATION}, the start commit (hash/number) for generating the commit
     *     sequence; <code>null</code> otherwise</li>
     * <li>If the task type is {@link TaskType#GENERATION}, the end commit (hash/number) for generating the commit
     *     sequence; <code>null</code> otherwise</li>
     * <li>If the task type is {@link TaskType#EVALUATION}, the file denoting an archive file containing a repository
     *     for evaluation; <code>null</code> otherwise</li>
     * <li>If the task type is {@link TaskType#EVALUATION}, the file denoting a directory containing the commit sequence
     *     to apply to the repository in the archive file during evaluation; <code>null</code> otherwise</li>
     * <li>If the task type is {@link TaskType#EVALUATION}, the type of commit hook (file) to create for evaluation;
     *     <code>null</code> otherwise</li>
     * <li>If the task type is {@link TaskType#EVALUATION}, the content to write to the commit hook file for evaluation;
     *     <code>null</code> otherwise</li>
     * </ul>
     */
    private static final Object[][] EXPECTED_VALUES = {
            {"correct_evaluation-01.cfg", TaskType.EVALUATION, new File("./testdata"), StreamType.SYSTEM,
                StreamType.NONE, null, null, null, new File("./testdata/TestRepository.zip"), new File("./testdata"),
                CommitHookType.PRE,
                "There is nothing to test beside being not empty. Actual content cannot be tested in advance."},
            {"correct_evaluation-02.cfg", TaskType.EVALUATION, new File("./testdata"), StreamType.SYSTEM,
                StreamType.NONE, null, null, null, new File("./testdata/TestRepository.zip"), new File("./testdata"),
                CommitHookType.POST,
                "There is nothing to test beside being not empty. Actual content cannot be tested in advance."},
            {"correct_evaluation-03.cfg", TaskType.EVALUATION, new File("./testdata"), StreamType.SYSTEM,
                StreamType.NONE, null, null, null, new File("./testdata/TestRepository.zip"), new File("./testdata"),
                CommitHookType.POST, "command in first line\ncommand in second line"},
            {"correct_evaluation-04.cfg", TaskType.EVALUATION, new File("./testdata"), StreamType.SYSTEM,
                StreamType.NONE, null, null, null, new File("./testdata/TestRepository.zip"), new File("./testdata"),
                CommitHookType.POST, "print the following string: \"String\""},
            {"correct_generation-01.cfg", TaskType.GENERATION, new File("./testdata"), StreamType.SYSTEM,
                StreamType.NONE, new File("./testdata/TestRepository"), "2a79fe77210128198ae05d3731b8693c75fb75e0",
                "2a79fe77210128198ae05d3731b8693c75fb75e0", null, null, null, null},
            {"correct_generation-02.cfg", TaskType.GENERATION, new File("./testdata"), StreamType.SYSTEM,
                StreamType.FILE, new File("./testdata/TestRepository"), "2a79fe77210128198ae05d3731b8693c75fb75e0",
                "2a79fe77210128198ae05d3731b8693c75fb75e0", null, null, null, null},
            {"correct_generation-03.cfg", TaskType.GENERATION, new File("./testdata"), StreamType.FILE,
                StreamType.NONE, new File("./testdata/TestRepository"), "2a79fe77210128198ae05d3731b8693c75fb75e0",
                "2a79fe77210128198ae05d3731b8693c75fb75e0", null, null, null, null},
            {"correct_generation-04.cfg", TaskType.GENERATION, new File("./testdata"), StreamType.NONE,
                StreamType.NONE, new File("./testdata/TestRepository"), "2a79fe77210128198ae05d3731b8693c75fb75e0",
                "2a79fe77210128198ae05d3731b8693c75fb75e0", null, null, null, null},
            {"correct_generation-05.cfg", TaskType.GENERATION, new File("./testdata"), StreamType.SYSTEM,
                StreamType.SYSTEM, new File("./testdata/TestRepository"), "2a79fe77210128198ae05d3731b8693c75fb75e0",
                "2a79fe77210128198ae05d3731b8693c75fb75e0", null, null, null, null},
            {"correct_generation-06.cfg", TaskType.GENERATION, new File("./testdata"), StreamType.FILE,
                StreamType.FILE, new File("./testdata/TestRepository"), "2a79fe77210128198ae05d3731b8693c75fb75e0",
                "2a79fe77210128198ae05d3731b8693c75fb75e0", null, null, null, null}
    };
    
    /**
     * The {@link File} denoting the directory, which contains the test configuration files.
     * 
     * @see #setUp()
     */
    private static File testConfigurationsDirectory;
    
    /**
     * The local reference to the global {@link FileUtilities} instance.
     */
    private static FileUtilities fileUtilities = FileUtilities.getInstance();
    
    /**
     * The expected name of the configuration file used for the current test iteration.
     */
    private String expectedConfigurationFileName;
    
    /**
     * The expected task type to execute by this tool.
     */
    private TaskType expectedTaskType;
    
    /**
     * The expected (parent) output directory to store the results of executing the respective task of this tool. 
     */
    private File expectedOutputDirectory;
    
    /**
     * The expected stream for printing standard logging information (information, warnings, errors) to. As this
     * configuration is optional, the default value is {@link StreamType#SYSTEM}.
     */
    private StreamType expectedStandardOutputStream = StreamType.SYSTEM;
    
    /**
     * The expected stream for printing debug information to. As this configuration is optional, the default value is
     * {@link StreamType#NONE}.
     */
    private StreamType expectedDebugOutputStream = StreamType.NONE;
    
    /**
     * The expected directory denoting the repository from which a commit sequence shall be generated. This attribute
     * is only available, if {@link Setup#taskType} is {@link TaskType#GENERATION}.
     */
    private File expectedRepositoryDirectory;
    
    /**
     * The expected commit (hash/number) defining the start of the commit sequence to generate from the repository
     * denoted by the configured {@link #repositoryDirectory}. This attribute is only available, if
     * {@link Setup#taskType} is {@link TaskType#GENERATION}.
     */
    private String expectedStartCommit;
    
    /**
     * The expected commit (hash/number) defining the end of the commit sequence to generate from the repository
     * denoted by the configured {@link #repositoryDirectory}. This attribute is only available, if
     * {@link Setup#taskType} is {@link TaskType#GENERATION}.
     */
    private String expectedEndCommit;
    
    /**
     * The expected file denoting an archive, which contains a repository. That repository will be extracted from the
     * archive and used for evaluation by creating a specific commit hook and applying a commit sequence to it. This
     * attribute is only available, if {@link Setup#taskType} is {@link TaskType#EVALUATION}.
     */
    private File expectedRepositoryArchive;
    
    /**
     * The expected directory containing the commit sequence to apply to the repository contained in the configured
     * {@link #repositoryArchive}. This commit sequence consists of a set of commit or diff files to be applied
     * iteratively to that repository. This attribute is only available, if {@link Setup#taskType} is
     * {@link TaskType#EVALUATION}.
     */
    private File expectedCommitSequenceDirectory;
    
    /**
     * The expected hook type defining the type of commit hook to created for the repository contained in the
     * configured {@link #repositoryArchive}. This attribute is only available, if {@link Setup#taskType} is
     * {@link TaskType#EVALUATION}.
     */
    private CommitHookType expectedCommitHookType;
    
    /**
     * The expected hook content to write to the commit hook (file) defined by the configured {@link #hookType} and
     * for the repository contained in the configured {@link #repositoryArchive}. This attribute is only available, if
     * {@link Setup#taskType} is {@link TaskType#EVALUATION}.
     */
    private String expectedHookContent;
    
    /**
     * The actual {@link Setup} object as created during {@link #createActualSetup()} based on the test configuration
     * file specified by {@link #expectedConfigurationFileName}.
     * 
     * @see #actualSetupException
     */
    private Setup actualSetup;
    
    /**
     * Constructs a new {@link SetupPropertiesTests} instance. Each instance represents a single test iteration for each
     * subset of the {@link #EXPECTED_VALUES}.
     * 
     * @param configurationFileName the name of the test configuration file
     * @param taskType the expected task type to execute by the tool
     * @param outputDirectory the output directory for storing results of the tool
     * @param standardOutputStreamType the stream type for printing standard logging information
     * @param debugOutputStreamType the stream type for printing debug information
     * @param repositoryDirectory the repository directory for generation
     * @param startCommit the start commit (hash/number) for creating a commit sequence during generation
     * @param endCommit the end commit (hash/number) for creating a commit sequence during generation
     * @param repositoryArchive the archive file containing a repository for evaluation
     * @param commitSequenceDirectory the directory containing the commit sequence for evaluation
     * @param commitHookType the hook type defining the type of commit hook to create for evaluation
     * @param hookContent the content to write to the commit hook (file) for evaluation
     */
    //checkstyle: stop parameter number check
    public SetupPropertiesTests(String configurationFileName, TaskType taskType, File outputDirectory,
            StreamType standardOutputStreamType, StreamType debugOutputStreamType, File repositoryDirectory,
            String startCommit, String endCommit, File repositoryArchive, File commitSequenceDirectory,
            CommitHookType commitHookType, String hookContent) {
        expectedConfigurationFileName = configurationFileName;
        expectedTaskType = taskType;
        expectedOutputDirectory = outputDirectory;
        expectedStandardOutputStream = standardOutputStreamType;
        expectedDebugOutputStream = debugOutputStreamType;
        expectedRepositoryDirectory = repositoryDirectory;
        expectedStartCommit = startCommit;
        expectedEndCommit = endCommit;
        expectedRepositoryArchive = repositoryArchive;
        expectedCommitSequenceDirectory = commitSequenceDirectory;
        expectedCommitHookType = commitHookType;
        expectedHookContent = hookContent;
        actualSetup = createSetup();
    }
    //checkstyle: resume parameter number check

    /**
     * Sets the {@link #testConfigurationsDirectory} before running any tests.
     */
    @BeforeClass
    public static void setUp() {
        try {
            testConfigurationsDirectory = fileUtilities.getCheckedFileObject(TEST_DATA_DIRECTORY_PATH,
                    TEST_CONFIGURATIONS_DIRECTORY_NAME, true);
        } catch (FileUtilitiesException e) {
            e.printStackTrace();
            fail("Missing test configurations directory: see printed stack trace");
        }
    }
    
    /**
     * Returns the {@link List} of expected values as defined by the individual subsets in {@link #EXPECTED_VALUES}.
     * 
     * @return the list of expected values
     */
    @Parameters
    public static List<Object[]> getExpectedValues() {
        List<Object[]> expectedValues = new ArrayList<Object[]>();
        for (int i = 0; i < EXPECTED_VALUES.length; i++) {
            expectedValues.add(EXPECTED_VALUES[i]);
        }
        return expectedValues;
    }
    
    /**
     * Creates a new {@link Setup} instance based on the test configuration file identified by the 
     * {@link #expectedConfigurationFileName} located in the {@link #testConfigurationsDirectory}. If this configuration
     * file cannot be found in that directory or the setup creation fails, this method will fail the entire test
     * iteration.
 
     * @return a new {@link Setup} instance; never <code>null</code>
     */
    private Setup createSetup() {
        File[] testConfigurations = testConfigurationsDirectory.listFiles();
        File expectedConfigurationFile = null;
        int testConfigurationsCounter = 0;
        while (expectedConfigurationFile == null && testConfigurationsCounter < testConfigurations.length) {
            if (testConfigurations[testConfigurationsCounter].getName().equals(expectedConfigurationFileName)) {
                expectedConfigurationFile = testConfigurations[testConfigurationsCounter];
            }
            testConfigurationsCounter++;
        }
        if (expectedConfigurationFile == null) {
            fail("Creating actual setup failed: configuration file \"" + expectedConfigurationFileName 
                    + "\" not available in \"" + testConfigurationsDirectory.getAbsolutePath() + "\"");
        }
        Setup createdSetup = null;
        try {
            createdSetup = new Setup(new String[] {expectedConfigurationFile.getAbsolutePath()});
        } catch (SetupException e) {
            e.printStackTrace();
            fail("Creating actual setup failed: see printed stack trace");
        }
        return createdSetup;
    }
    
    /**
     * Tests whether the task type provided by the {@link #actualSetup} equals the {@link #expectedTaskType}.
     */
    @Test
    public void testCorrectTaskType() {
        assertEquals(expectedTaskType, actualSetup.getTaskType(), toAssertMessage("Wrong task type"));
    }
    
    /**
     * Tests whether the output directory provided by the {@link #actualSetup} equals the
     * {@link #expectedOutputDirectory} regarding their absolute file path.
     */
    @Test
    public void testCorrectOutputDirectory() {
        assertEquals(expectedOutputDirectory.getAbsolutePath(), actualSetup.getOutputDirectory().getAbsolutePath(),
                toAssertMessage("Wrong output directory"));
    }
    
    /**
     * Tests whether the stream type for printing standard logging information provided by the {@link #actualSetup}
     * equals the {@link #expectedStandardOutputStream}.
     */
    @Test
    public void testCorrectStandardOutputStream() {
        assertEquals(expectedStandardOutputStream, actualSetup.getStandardOutputStreamType(),
                toAssertMessage("Wrong standard output stream type"));
    }
    
    /**
     * Tests whether the stream type for printing debug information provided by the {@link #actualSetup} equals the
     * {@link #expectedDebugOutputStream}.
     */
    @Test
    public void testCorrectDebugOutputStream() {
        assertEquals(expectedDebugOutputStream, actualSetup.getDebugOutputStreamType(),
                toAssertMessage("Wrong debug output stream type"));
    }
    
    /**
     * Tests whether the repository directory provided by the {@link #actualSetup} for generation equals the
     * {@link #expectedRepositoryDirectory} regarding their absolute file path.
     */
    @Test
    public void testCorrectRepositoryDirectory() {
        if (expectedRepositoryDirectory == null) {
            assertNull(actualSetup.getRepositoryDirectory(), "Expected repository directory is \"null\"");
        } else {            
            assertEquals(expectedRepositoryDirectory.getAbsolutePath(),
                    actualSetup.getRepositoryDirectory().getAbsolutePath(),
                    toAssertMessage("Wrong repository directory"));
        }
    }
    
    /**
     * Tests whether the start commit (hash/number) provided by the {@link #actualSetup} for generation equals the
     * {@link #expectedStartCommit}.
     */
    @Test
    public void testCorrectStartCommit() {
        assertEquals(expectedStartCommit, actualSetup.getStartCommit(), toAssertMessage("Wrong start commit"));
    }
    
    /**
     * Tests whether the end commit (hash/number) provided by the {@link #actualSetup} for generation equals the
     * {@link #expectedStartCommit}.
     */
    @Test
    public void testCorrectEndCommit() {
        assertEquals(expectedEndCommit, actualSetup.getEndCommit(), toAssertMessage("Wrong end commit"));
    }
    
    /**
     * Tests whether the repository archive provided by the {@link #actualSetup} for evaluation equals the
     * {@link #expectedRepositoryArchive} regarding their absolute file path.
     */
    @Test
    public void testCorrectRepositoryArchive() {
        if (expectedRepositoryArchive == null) {
            assertNull(actualSetup.getRepositoryArchive(), "Expected repository archive is \"null\"");
        } else {            
            assertEquals(expectedRepositoryArchive.getAbsolutePath(),
                    actualSetup.getRepositoryArchive().getAbsolutePath(), toAssertMessage("Wrong repository archive"));
        }
    }
    
    /**
     * Tests whether the commit sequence directory provided by the {@link #actualSetup} for evaluation equals the
     * {@link #expectedCommitSequenceDirectory} regarding their absolute file path.
     */
    @Test
    public void testCorrectCommitSequenceDirectory() {
        if (expectedCommitSequenceDirectory == null) {
            assertNull(actualSetup.getCommitSequenceDirectory(), "Expected repository archive is \"null\"");
        } else {            
            assertEquals(expectedCommitSequenceDirectory.getAbsolutePath(),
                    actualSetup.getCommitSequenceDirectory().getAbsolutePath(),
                    toAssertMessage("Wrong commit sequence directory"));
        }
    }
    
    /**
     * Tests whether the hook type provided by the {@link #actualSetup} for determining the type of commit hook (file)
     * to create during evaluation equals the {@link #expectedCommitHookType}.
     */
    @Test
    public void testCorrectCommitHookType() {
        assertEquals(expectedCommitHookType, actualSetup.getCommitHookType(),
                toAssertMessage("Wrong commit hook type"));
    }
    
    /**
     * Tests whether the hook content provided by the {@link #actualSetup} for writing it to the created commit hook
     * (file) during evaluation equals the {@link #expectedHookContent}.
     */
    @Test
    public void testCorrectHookContent() {
        assertEquals(expectedHookContent, actualSetup.getCommitHookContent(),
                toAssertMessage("Wrong commit hook content"));
    }
    
    /**
     * Extends the given message by the name of the configuration file used in the current test iteration.
     * 
     * @param message the actual assertion message; must not be <code>null</code>
     * @return the extended message; never <code>null</code>
     */
    private String toAssertMessage(String message) {
        return message + " [cfg=\"" + expectedConfigurationFileName + "\"]";
    }
    
}
