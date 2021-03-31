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

import net.ssehub.ckphd.evaluation.utilities.ArchiveUtilities;
import net.ssehub.ckphd.evaluation.utilities.ArchiveUtilitiesException;
import net.ssehub.ckphd.evaluation.utilities.FileUtilities;
import net.ssehub.ckphd.evaluation.utilities.FileUtilities.WriteOption;
import net.ssehub.ckphd.evaluation.utilities.FileUtilitiesException;
import net.ssehub.ckphd.evaluation.utilities.Logger;
import net.ssehub.ckphd.evaluation.utilities.ProcessUtilities;
import net.ssehub.ckphd.evaluation.utilities.ProcessUtilities.ExecutionResult;
import net.ssehub.ckphd.evaluation.utilities.ProcessUtilitiesException;

/**
 * This class represents a software repository.
 * 
 * @author Christian Kroeher
 *
 */
public class Repository {
    
    /**
     * The identifier of this class, e.g., for printing messages.
     */
    private static final String ID = "Repository";
    
    /**
     * The SHEBANG, which will be added as the first line a Git pre-commit hook.
     */
    private static final String SHEBANG = "#!/bin/sh";
    
    /**
     * The relative path to the Git hooks directory within a Git repository.
     */
    private static final String GIT_HOOKS_DIRECTORY_RELATIVE_PATH = "/.git/hooks";

    /**
     * The name of the Git pre-commit hook file.
     */
    private static final String GIT_PRE_COMMIT_HOOK_FILE_NAME = "pre-commit";
    
    /**
     * The constant part of the command for applying a patch to a Git repository. That patch has to be appended to this
     * command by adding the absolute path to the file that contains the patch information. 
     */
    private static final String[] GIT_APPLY_COMMAND_PART = {"git", "apply"};
    
    /**
     * The command for adding all changes (including untracked files) to the next commit to a Git repository.
     */
    private static final String[] GIT_ADD_COMMAND = {"git", "add", "."};
    
    /**
     * The constant part of the command for committing all changes to a Git repository. The commit message has to be
     * appended to this command by adding a text. 
     */
    private static final String[] GIT_COMMIT_COMMAND_PART = {"git", "commit", "-a", "-m"};
    
    /**
     * The reference to the global {@link Logger}.
     */
    private Logger logger = Logger.INSTANCE;
    
    /**
     * The reference to the global {@link ProcessUtilities}.
     */
    private ProcessUtilities processUtilities = ProcessUtilities.getInstance();
    
    /**
     * The {@link File} denoting the root directory of the repository this instance represents.
     */
    private File repositoryDirectory;
    
    /**
     * Constructs a new {@link Repository} instance. As part of that construction, the repository in the given archive
     * file will be extracted to the same location (directory) as that file.
     * 
     * @param archiveFile the {@link File} denoting an archive in which the actual repository is stored
     * @throws SetupException if {@link #setup(File)} fails
     */
    public Repository(File archiveFile) throws SetupException {
        setup(archiveFile);
    }
    
    /**
     * Sets up this instance by extracting the given archive file and setting the extracted root element of that archive
     * as the {@link #repositoryDirectory}.
     * 
     * @param archiveFile the {@link File} denoting an archive in which the actual repository is stored
     * @throws SetupException if the given archive file is not a zip file or its extraction fails
     *         ({@link #repositoryDirectory} will not be set)
     */
    private void setup(File archiveFile) throws SetupException {
        if (archiveFile == null) {
            throw new SetupException("The given archive file is \"null\"");
        } else if (!archiveFile.exists()) {
            throw new SetupException("The archive file \"" + archiveFile.getAbsolutePath() + "\" does not exist");
        } else if (!archiveFile.isFile()) {
            throw new SetupException("The archive file \"" + archiveFile.getAbsolutePath() + "\" is not a file");
        } else if (!archiveFile.getName().endsWith(".zip")) {
            throw new SetupException("The archive file \"" + archiveFile.getAbsolutePath() + "\" is not a zip archive");
        } else {
            logger.logDebug(ID, "Setting up repository",
                    "Repository archive file: \"" + archiveFile.getAbsolutePath());
            try {
                repositoryDirectory = ArchiveUtilities.getInstance().extract(archiveFile);
            } catch (ArchiveUtilitiesException e) {
                throw new SetupException("Extracting archive file \"" + archiveFile.getAbsolutePath() + "\" failed", e);
            }
        }
    }
    
    /**
     * Adds a hook to the repository this instance represents. The content of that hook will be constructed by the 
     * {@link #SHEBANG} followed by the content of the given {@link String}.
     * 
     * @param hookActions the {@link String} defining the actions to be performed as part of the hook this method will
     *        add to this repository; may be <i>blank</i>
     * @throws ExecutionException if the given hook actions are <code>null</code>, <i>blank</i>, or adding the hook
     *         fails 
     */
    public void addHook(String hookActions) throws ExecutionException {
        if (hookActions == null) {
            throw new ExecutionException("The given hook actions are \"null\"");
        } else if (hookActions.isBlank()) {
            throw new ExecutionException("The given hook actions are empty");
        } else {
            String preCommitHookFilePath = repositoryDirectory.getAbsolutePath() + GIT_HOOKS_DIRECTORY_RELATIVE_PATH;
            String preCommitHookContent = SHEBANG + System.lineSeparator() + hookActions;
            logger.logDebug(ID, "Adding pre-commmit hook \"" + preCommitHookFilePath + "\"",
                    "Content:" + System.lineSeparator() + preCommitHookContent);
            try {
                FileUtilities.getInstance().writeFile(preCommitHookFilePath, GIT_PRE_COMMIT_HOOK_FILE_NAME,
                        preCommitHookContent, WriteOption.CREATE);
            } catch (FileUtilitiesException e) {
                throw new ExecutionException("Adding pre-commit hook failed", e);
            }
        }
    }
    
    /**
     * Applies the changes in the given commit {@link File} to the current state of this repository.
     * 
     * @param commitFile the {@link File} denoting a commit file, which contains a patch as created from the respective
     *        version control system
     * @throws ExecutionException if the given commit file is not an existing file, the application of its changes, or
     *         the commit of these changes fails
     */
    public void applyCommit(File commitFile) throws ExecutionException {
        if (commitFile == null) {
            throw new ExecutionException("The given commit file is \"null\"");
        } else if (!commitFile.exists()) {
            throw new ExecutionException("The given commit file \"" + commitFile.getAbsolutePath()
                    + "\" does not exist");
        } else if (!commitFile.isFile()) {
            throw new ExecutionException("The given commit file \"" + commitFile.getAbsolutePath()
                    + "\" is not a file");
        } else {
            String[] command;
            ExecutionResult commandResult;
            // First, apply the changes in the commit file to the files in the repository
            command = processUtilities.extendCommand(GIT_APPLY_COMMAND_PART, commitFile.getAbsolutePath());
            logger.logDebug(ID, "Applying changes from \"" + commitFile.getAbsolutePath() + "\"",
                    "Command: " + processUtilities.getCommandString(command));
            try {
                commandResult = processUtilities.executeCommand(command, repositoryDirectory);
                if (!commandResult.executionSuccessful()) {
                    throw new ExecutionException("Applying changes from \"" + commitFile.getAbsolutePath()
                            + "\" failed: " + commandResult.getErrorOutputData());
                }
            } catch (ProcessUtilitiesException e) {
                throw new ExecutionException("Applying changes from \"" + commitFile.getAbsolutePath() + "\" failed",
                        e);
            }
            // Second, add the changes to the next commit
            command = GIT_ADD_COMMAND;
            logger.logDebug(ID, "Adding changes for committing",
                    "Command: " + processUtilities.getCommandString(command));
            try {
                commandResult = processUtilities.executeCommand(command, repositoryDirectory);
                if (!commandResult.executionSuccessful()) {
                    throw new ExecutionException("Adding changes to next commit failed: "
                            + commandResult.getErrorOutputData());
                }
            } catch (ProcessUtilitiesException e) {
                throw new ExecutionException("Adding changes to next commit failed", e);
            }
            // Third, commit the changes to the repository
            command = processUtilities.extendCommand(GIT_COMMIT_COMMAND_PART, commitFile.getName());
            logger.logDebug(ID, "Committing changes to repository",
                    "Command: " + processUtilities.getCommandString(command));
            try {
                commandResult = processUtilities.executeCommand(command, repositoryDirectory);
                if (!commandResult.executionSuccessful()) {
                    throw new ExecutionException("Committing changes to repository failed: "
                            + commandResult.getErrorOutputData());
                }
            } catch (ProcessUtilitiesException e) {
                throw new ExecutionException("Committing changes to repository", e);
            }
        }
    }
    
    /**
     * Deletes the repository this instance represents.
     *  
     * @return <code>true</code>, if deleting the repository was successful; <code>false</code> otherwise
     */
    public boolean delete() {
        boolean deletionSuccessful = false;
        if (repositoryDirectory == null) {
            deletionSuccessful = true;
        } else {
            deletionSuccessful = delete(repositoryDirectory);
        }
        return deletionSuccessful;
    }
    
    /**
     * Deletes the given {@link File} and all nested elements recursively.
     * 
     * @param file the {@link File} to delete; should never be <code>null</code>
     * @return <code>true</code>, if deleting the given file and all nested elements was successful; <code>false</code>
     *         otherwise
     */
    private boolean delete(File file) {
        boolean deletionSuccessful = false;
        if (file.isDirectory()) {
            File[] nestedFiles = file.listFiles();
            for (int i = 0; i < nestedFiles.length; i++) {
                deletionSuccessful = delete(nestedFiles[i]);
                if (!deletionSuccessful) {
                    logger.logError(ID, "Deleting \"" + nestedFiles[i].getAbsolutePath() + "\" failed");
                }
            }
        }
        deletionSuccessful = file.delete();
        return deletionSuccessful;
    }
    
}
