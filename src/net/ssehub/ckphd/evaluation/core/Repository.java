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
import net.ssehub.ckphd.evaluation.utilities.FileUtilitiesException;
import net.ssehub.ckphd.evaluation.utilities.Logger;
import net.ssehub.ckphd.evaluation.utilities.Logger.MessageType;

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
     * The reference to the global {@link Logger}.
     */
    private Logger logger = Logger.getInstance();
    
    /**
     * The {@link File} denoting the root directory of the repository this instance represents.
     */
    private File repositoryDirectory;
    
    /**
     * Constructs a new {@link Repository} instance.
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
            logger.log(ID, "Setting up the repository", "Repository archive file: \"" + archiveFile.getAbsolutePath()
                    + "\"", MessageType.INFO);
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
     * @throws ExecutionException if the given hook actions are <code>null</code> or adding the hook fails 
     */
    public void addHook(String hookActions) throws ExecutionException {
        if (hookActions == null) {
            throw new ExecutionException("The given hook actions are \"null\"");
        } else {            
            String preCommitHookFilePath = repositoryDirectory.getAbsolutePath() + GIT_HOOKS_DIRECTORY_RELATIVE_PATH;
            String preCommitHookContent = SHEBANG + System.lineSeparator() + hookActions;
            logger.log(ID, "Adding pre-commmit hook \"" + preCommitHookFilePath + "\"", "Content:"
                    + System.lineSeparator() + preCommitHookContent, MessageType.INFO);
            try {
                FileUtilities.getInstance().writeFile(preCommitHookFilePath, GIT_PRE_COMMIT_HOOK_FILE_NAME,
                        preCommitHookContent, true);
            } catch (FileUtilitiesException e) {
                throw new ExecutionException("Adding pre-commit hook failed", e);
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
                    logger.log(ID, "Deleting \"" + nestedFiles[i].getAbsolutePath() + "\" failed", null,
                            MessageType.ERROR);
                }
            }
        }
        deletionSuccessful = file.delete();
        return deletionSuccessful;
    }
    
}
