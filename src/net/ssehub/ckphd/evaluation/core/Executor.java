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
package net.ssehub.ckphd.evaluation.core;

import java.io.File;

import net.ssehub.ckphd.evaluation.utilities.Logger;
import net.ssehub.ckphd.evaluation.utilities.Logger.MessageType;

/**
 * This is the main class of this project and responsible for executing any {@link Evaluation}.
 * 
 * @author Christian Kroeher
 *
 */
public class Executor {
    
    /**
     * The identifier of this class, e.g., for printing messages.
     */
    private static final String ID = "Executor";
    
    /**
     * The {@link String} describing the expected number and order of arguments passed as part of the call of this tool.
     */
    private static final String ARGUMENTS_DESCRIPTION = "There must be exactly three arguments in the following order:"
            + System.lineSeparator() + "1. The fully-qualified path to the repository archive file (*.zip)"
            + System.lineSeparator() + "2. The fully-qualified path to the commit sequence directory"
            + System.lineSeparator() + "3. The commit hook actions";
    
    /**
     * The reference to the global {@link Logger}.
     */
    private static Logger logger = Logger.getInstance();

    /**
     * Starts the overall process of this project.
     * 
     * @param args the set of {@link String}s representing the user-defined input parameters passed as part of the call
     *        of this tool. 
     */
    public static void main(String[] args) {
        if (args.length == 3) {
            File repositoryArchiveFile = new File(args[0]);
            File commitSequenceDirectory = new File(args[1]);
            logger.log(ID, "Start", "Repository archive file: \"" + repositoryArchiveFile.getAbsolutePath() + "\""
                    + System.lineSeparator() + "Commit sequence directory: \""
                        + commitSequenceDirectory.getAbsolutePath() + "\""
                    + System.lineSeparator() + "Commit hook actions: \"" + args[2] + "\"", MessageType.INFO);
            // Determine and save the current time in milliseconds for calculating the execution duration below 
            long startTimeMillis = System.currentTimeMillis();
            try {
                Evaluation evaluation = new Evaluation(repositoryArchiveFile, commitSequenceDirectory);
                evaluation.run(args[2]);
            } catch (SetupException e) {
                logger.logException(ID, "An exception occurred during setup", e);
            } catch (ExecutionException e) {
                logger.logException(ID, "An exception occurred during execution", e);
            } finally {
                // Determine end date and time and display them along with the duration of the overall process execution
                long durationMillis = System.currentTimeMillis() - startTimeMillis;
                int durationSeconds = (int) ((durationMillis / 1000) % 60);
                int durationMinutes = (int) ((durationMillis / 1000) / 60);
                logger.log(ID, "Finished", "Duration: " + durationMinutes + " min. and " + durationSeconds + " sec.",
                        MessageType.INFO);
            }
        } else {
            logger.log(ID, "Wrong number of arguments", ARGUMENTS_DESCRIPTION, MessageType.ERROR);
        }
    }

}
