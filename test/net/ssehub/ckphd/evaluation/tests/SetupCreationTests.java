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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

/**
 * This class contains unit tests for the creation of {@link Setup} instances. This focuses on throwing the correct
 * exception depending on the given input. Testing the correct provision of setup properties, if creating an instance
 * was successful, is part of the {@link SetupPropertiesTests}.
 * 
 * @author Christian Kroeher
 *
 */
@RunWith(Parameterized.class)
public class SetupCreationTests {
    
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
     * <li><code>true</code>, if a {@link Setup} instance must be created, <code>false</code> otherwise</li>
     * <li>Start of setup exception message; <code>null</code>, if no exception is expected, but never <b>empty</b> as
     *     this leads to false-positive tests</li>
     * <li>Zero-based, positive integer value defining the depth of the stack trace where the exception message (see
     *     above) is located: "0" means top/main message, "1" means message of the excpetion's cause, etc. This value
     *     is ignored, if the exception message is defined as <code>null</code></li>
     * </ul>
     */
    private static final Object[][] EXPECTED_VALUES = {
            {null, false, "No file object for path \"null\"", 1},
            {"", false, "No file object for empty path", 1},
            {"    ", false, "No file object for empty path", 1},
            {"empty.cfg", false, "No configuration properties available", 1},
            {"false-content.cfg", false, "Missing property \"core.task\": ", 1},
            {"incomplete-01.cfg", false, "Empty property \"core.task\": ", 1},
            {"incomplete-02.cfg", false, "Setting task \"\"Hallo\"\" failed: ", 1},
            {"incomplete-03.cfg", false, "Setting task \"\"evaluation\"\" failed: ", 1},
            {"incomplete-04.cfg", false, "Missing property \"core.output_directory\": ", 1},
            {"incomplete-05.cfg", false, "Setting output directory \"this/does/not/exist\" failed: ", 1},
            {"incomplete-06.cfg", false, "Setting output directory \"./testdata/readme.txt\" failed: ", 1},
            {"incomplete-07.cfg", false, "Missing property \"evaluation.repository_archive\": ", 1},
            {"incomplete-08.cfg", false, "Empty property \"logging.standard_stream\": ", 1},
            {"incomplete-09.cfg", false, "Missing property \"evaluation.repository_archive\": ", 1},
            {"incomplete-10.cfg", false, "Setting repository archive \"should_be_a_path\" failed: ", 1},
            {"incomplete-11.cfg", false, "Repository archive \"./testdata/readme.txt\" is not a zip archive: ", 1},
            {"incomplete-12.cfg", false, "Validating repository archive ", 1},
            {"incomplete-13.cfg", false, "Setting commit sequence directory \"should_be_a_path\" failed: ", 1},
            {"incomplete-14.cfg", false, "Setting commit hook type \"should_be_a_type\" failed: ", 1},
            {"correct_evaluation-01.cfg", true, null, 0},
            {"incomplete-15.cfg", false, "Missing property \"generation.repository_directory\": ", 1},
            {"incomplete-16.cfg", false, "Setting repository directory \"should_be_a_path\" failed: ", 1},
            {"incomplete-17.cfg", false, "Start commit \"should_be_a_hash\" is not available in repository ", 1},
            {"incomplete-18.cfg", false, "End commit \"should_be_a_hash\" is not available in repository ", 1},
            {"correct_generation-01.cfg", true, null, 0},
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
     * The specification of whether a {@link Setup} instance is expected to be created (<code>true</code>) or not
     * (<code>false</code>) in the current test iteration.
     */
    private boolean expectedSetupInstance;
    
    /**
     * The expected start of a {@link SetupException} message. As such exception may contain absolute paths or
     * runtime-dependent names, only the start of these messages can be tested independent of the execution environment.
     * <br>
     * <br>
     * A value of <code>null</code> defines that no exception is expected.
     */
    private String expectedSetupExceptionMessageStart;
    
    /**
     * The zero-based, positive integer value defining the depth of the stack trace where the
     * {@link #expectedSetupExceptionMessageStart} is located: "0" means top/main message, "1" means message of the
     * excpetion's cause, etc. This value is ignored, if the exception message is defined as <code>null</code>.
     */
    private int expectedSetupExceptionMessageDepth;
    
    /**
     * The actual {@link Setup} instance as created during {@link #createActualSetup()} based on the test configuration
     * file specified by {@link #expectedConfigurationFileName}. May be <code>null</code>.
     * 
     * @see #actualSetupException
     */
    private Setup actualSetup;
    
    /**
     * The actual {@link SetupException} instance thrown during {@link #createActualSetup()}. If creating the
     * {@link #actualSetup} fails (its value is <code>null</code>), this attribute must not be <code>null</code>, and
     * vice-versa. 
     */
    private SetupException actualSetupException;
    
    /**
     * Constructs a new {@link SetupCreationTests} instance. Each instance represents a single test iteration for each
     * subset of the {@link #EXPECTED_VALUES}.
     * 
     * @param configurationFileName the name of the test configuration file
     * @param setupInstance <code>true</code>, if {@link #actualSetup} must not be <code>null</code>; <code>false</code>
     *        otherwise
     * @param setupExceptionMessageStart the start of a {@link SetupException} message; may be <code>null</code>, if no
     *        exception is expected
     * @param setupExceptionMessageDepth the zero-based, positive integer value defining the depth of the stack trace
     *        where the given setup exception message start is located
     */
    public SetupCreationTests(String configurationFileName, boolean setupInstance, String setupExceptionMessageStart,
            int setupExceptionMessageDepth) {
        expectedConfigurationFileName = configurationFileName;
        expectedSetupInstance = setupInstance;
        expectedSetupExceptionMessageStart = setupExceptionMessageStart;
        expectedSetupExceptionMessageDepth = setupExceptionMessageDepth;
        actualSetupException = createActualSetup();
    }

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
     * Creates the {@link #actualSetup} by constructing a new {@link Setup} instance. This construction is based on the
     * value of the current {@link #expectedConfigurationFileName}:
     * <ul>
     * <li>If the value is <code>null</code>, the path to the configuration file passed to the constructor is 
     *     <code>null</code></li>
     * <li>If the value is a name of an existing test configuration file in the {@link #testConfigurationsDirectory},
     *     the path to that configuration file is passed to the constructor</li>
     * <li>Any other value will be directly passed to the constructor</li>
     * </ul>
     * 
     * @return a {@link SetupException} as thrown by the constructor of the {@link Setup} class; may be
     *         <code>null</code>
     */
    private SetupException createActualSetup() {
        SetupException setupException = null;
        String configurationFilePath = null;
        // configurationFilePath must be null, if expectedConfigurationFileName is null (test for null args)
        if (expectedConfigurationFileName != null) {
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
                // no such test configuration file, must be test for empty/blank args or non-existing configuration
                configurationFilePath = expectedConfigurationFileName;
            } else {
                // use test configuration file as specified by the expected configuration file name
                configurationFilePath = expectedConfigurationFile.getAbsolutePath();
            }
        }
        // Finally, try to create the actual setup instance
        try {
            actualSetup = new Setup(new String[] {configurationFilePath});
        } catch (SetupException e) {
            setupException = e;
        }
        return setupException;
    }
    
    /**
     * Tests whether a {@link Setup} instance is created as expected for the current test configuration file.
     */
    @Test
    public void testSetupInstanceExists() {
        if (expectedSetupInstance) {
            assertNotNull(actualSetup, toAssertMessage("Setup instance expected"));
        } else {
            assertNull(actualSetup, toAssertMessage("No setup instance expected"));
        }
    }
    
    /**
     * Tests whether constructing a {@link Setup} instance throws a {@link SetupException} as expected for the current
     * configuration file.
     */
    @Test
    public void testCorrectException() {
        if (expectedSetupExceptionMessageStart == null) {
            assertNull(actualSetupException, "Unexpected setup exception as no setup exception message is expected");
        } else {            
            assertEquals(expectedSetupExceptionMessageStart,
                    getExceptionMessagePart(expectedSetupExceptionMessageDepth,
                            expectedSetupExceptionMessageStart.length(), actualSetupException),
                    toAssertMessage("Wrong setup exception"));
        }
    }
    
    /**
     * Extracts the substring of the message at the given depth of the stack trace of the given exception from index 0
     * to the given length. 
     * 
     * @param depth the zero-based, positive integer value defining the depth of the stack trace where the message for
     *        creating the substring is located; a negative value results in using the top-level message of the given
     *        exception 
     * @param length the end index of the substring of the exception message; must be greater than 0
     * @param exception the exception providing the message to extract the substring from
     * @return the start of the exception message at the given depth until the given length, the full message, if the
     *         given length is greater than the exception message length, or <code>null</code>, if the given exception
     *         is <code>null</code>
     */
    private String getExceptionMessagePart(int depth, int length, Throwable exception) {
        String exceptionMessagePart = null;
        if (exception != null) {
            if (depth <= 0) {
                if (exception.getMessage().length() >= length) {                    
                    exceptionMessagePart = exception.getMessage().substring(0, length);
                } else {
                    exceptionMessagePart = exception.getMessage();
                }
            } else {
                exceptionMessagePart = getExceptionMessagePart((depth - 1), length, exception.getCause());
            }
        }
        return exceptionMessagePart;
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
