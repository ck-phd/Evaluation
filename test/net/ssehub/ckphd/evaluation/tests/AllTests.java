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

import java.io.File;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Definition of this test suite.
 */
@RunWith(Suite.class)
@SuiteClasses({
    RepositoryTests.class
    })

/**
 * This class summarizes all individual test classes into a single test suite and provides common attributes and methods
 * for those tests.
 * 
 * @author Christian Kroeher
 *
 */
public class AllTests {

    /**
     * The {@link File} denoting the directory, in which the test data is located.
     */
    public static final File TEST_DATA_DIRECTORY = new File("./testdata");
    
    /**
     * Deletes the given {@link File} and all nested elements recursively. If the deletion of one of element fails, the
     * entire deletion process is aborted resulting in potentially undeleted elements.
     * 
     * @param file the {@link File} to delete; should never be <code>null</code>
     * @return <code>true</code>, if deleting the given file and all nested elements was successful; <code>false</code>
     *         otherwise
     */
    public static boolean delete(File file) {
        boolean deletionSuccessful = true;
        if (file.isDirectory()) {
            File[] nestedFiles = file.listFiles();
            int nestedFilesCounter = 0;
            while (deletionSuccessful && nestedFilesCounter < nestedFiles.length) {
                deletionSuccessful = delete(nestedFiles[nestedFilesCounter]);
                nestedFilesCounter++;
            }
        }
        deletionSuccessful = file.delete();
        return deletionSuccessful;
    }
    
}
