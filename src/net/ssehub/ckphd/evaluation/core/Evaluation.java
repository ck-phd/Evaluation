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
import java.util.HashMap;
import java.util.List;

import net.ssehub.ckphd.evaluation.utilities.FileUtilities;
import net.ssehub.ckphd.evaluation.utilities.FileUtilitiesException;
import net.ssehub.ckphd.evaluation.utilities.Logger;
import net.ssehub.ckphd.evaluation.utilities.Logger.MessageType;

/**
 * This class executes the actual evaluation.
 * 
 * @author Christian Kroeher
 *
 */
public class Evaluation {
    
    /**
     * The identifier of this class, e.g., for printing messages.
     */
    private static final String ID = "Evaluation";
    
    /**
     * The prefix a the file containing the commit sequence, e.g., "CommitSequence_1.txt".
     */
    private static final String COMMIT_SEQUENCE_FILE_NAME_PREFIX = "CommitSequence";
    
    /**
     * The reference to the global {@link Logger}.
     */
    private Logger logger = Logger.getInstance();
    
    /**
     * The reference to the global {@link FileUtilities}.
     */
    private FileUtilities fileUtilities = FileUtilities.getInstance();
    
    /**
     * The {@link Repository} instance to which the commits of the {@link #commitSequenceList} will be applied during
     * this evaluation.
     */
    private Repository repository;
    
    /**
     * The {@link List} of {@link String}s representing the commit sequence. Each string in this list contains exactly
     * one commit hash or number of that sequence, while the order of these strings in that list correspond to their
     * historical order in the repository (<b>from the most recent commit at index <i>0</i> to the oldest, potentially
     * the initial commit at index <i>size - 1</i></b>).
     */
    private List<String> commitSequenceList;
    
    /**
     * The {@link HashMap} mapping each commit hash or number (<code>key</code>) in the {@link #commitSequenceList} to
     * the corresponding commit {@link File} (<code>value</code>) in the commit sequence directory. 
     */
    private HashMap<String, File> commitCommitFileMap;

    /**
     * Constructs a new {@link Evaluation} instance.
     * 
     * @param repositoryArchiveFile the {@link File} denoting an archive in which the actual repository is stored
     * @param commitSequenceDirectory the {@link File} denoting the directory, which contains the commit sequence file
     *        and the corresponding commit files to use in this evaluation
     * @throws SetupException if {@link #setup(File)} fails
     */
    public Evaluation(File repositoryArchiveFile, File commitSequenceDirectory) throws SetupException {
        setup(repositoryArchiveFile, commitSequenceDirectory);
    }
    
    /**
     * Executions this instance by adding the given hook actions to the repository and then applying the commits in the
     * commit sequence in reverse order to that repository.
     * 
     * @param hookActions the {@link String} defining the actions to be performed as part of the hook that will be added
     *        to the repository; may be <i>blank</i>
     * @throws ExecutionException if adding the hook actions or applying a commit fails
     */
    public void run(String hookActions) throws ExecutionException {
        repository.addHook(hookActions);
        File commitFile;
        for (int i = commitSequenceList.size() - 1; i >= 0; i--) {
            commitFile = commitCommitFileMap.get(commitSequenceList.get(i));
            repository.applyCommit(commitFile);
        }
    }
    
    /**
     * Sets up this instance by creating the {@link #repository}, extracting the commit sequence and checking, if the
     * defined commits in that sequence are available as expected.
     * 
     * @param repositoryArchiveFile the {@link File} denoting an archive in which the actual repository is stored
     * @param commitSequenceDirectory the {@link File} denoting the directory, which contains the commit sequence file
     *        and the corresponding commit files to use in this evaluation
     * @throws SetupException if the given commit sequence directory is not a non-empty directory, the commit sequence
     *         file, or any of the commit files for that sequence are missing
     */
    private void setup(File repositoryArchiveFile, File commitSequenceDirectory) throws SetupException {
        // First, create the repository instance to which the commits during this evaluation will be applied
        repository = new Repository(repositoryArchiveFile);
        // Second, check and set the commit sequence
        if (commitSequenceDirectory == null) {
            throw new SetupException("The given commit sequence directory is \"null\"");
        } else if (!commitSequenceDirectory.exists()) {
            throw new SetupException("The commit sequence directory \"" + commitSequenceDirectory.getAbsolutePath()
                    + "\" does not exist");
        } else if (!commitSequenceDirectory.isDirectory()) {
            throw new SetupException("The commit sequence directory \"" + commitSequenceDirectory.getAbsolutePath()
                    + "\" is not a directory");
        } else if (commitSequenceDirectory.list().length == 0) {
            throw new SetupException("The commit sequence directory \"" + commitSequenceDirectory.getAbsolutePath()
                    + "\" is empty");
        } else {
            logger.log(ID, "Setting up the evaluation", "Commit sequence directory: \""
                    + commitSequenceDirectory.getAbsolutePath() + "\"", MessageType.INFO);
            // Set the commit sequence to use for this evaluation
            File commitSequenceFile = getCommitSequenceFile(commitSequenceDirectory.listFiles());
            try {
                commitSequenceList = fileUtilities.readFile(commitSequenceFile);
            } catch (FileUtilitiesException e) {
                throw new SetupException("Setting up the commit sequence failed", e);
            }
            // Check, if for each commit in the commit sequence a corresponding commit file exists
            if (commitSequenceList.size() == commitSequenceDirectory.list().length - 1) {
                /*
                 * Note: "commitSequenceDirectory.list().length - 1", because the commit sequence directory contains the
                 * commit files and the commit sequence file.
                 */
                commitCommitFileMap = new HashMap<String, File>();
                File commitFile;
                for (String commit : commitSequenceList) {
                    commitFile = getCommitFile(commit, commitSequenceDirectory.listFiles()); 
                    if (commitFile == null) {
                        throw new SetupException("The commit file for commit \"" + commit + "\" is not available");
                    } else {
                        commitCommitFileMap.put(commit, commitFile);
                    }
                }
            } else {
                throw new SetupException("The number of commits in the commit sequence file \"" 
                        + commitSequenceFile.getAbsolutePath() + "\" (" + commitSequenceList.size()
                        + ") does not match the number of commit files in \""
                        + commitSequenceDirectory.getAbsolutePath() + "\" (" 
                        + (commitSequenceDirectory.list().length - 1) + ")");
            }
        }
    }
    
    /**
     * Returns the {@link File} starting with the {@link #COMMIT_SEQUENCE_FILE_NAME_PREFIX}, which denotes the commit
     * sequence file, from the given {@link File} array.
     * 
     * @param files the {@link File} array in which the commit sequence file should be found
     * @return the {@link File} denoting the commit sequence file or <code>null</code>, if no such file is available in
     *         the given file array
     */
    private File getCommitSequenceFile(File[] files) {
        File commitSequenceFile = null;
        if (files != null) {
            int filesCounter = 0;
            while (commitSequenceFile == null && filesCounter < files.length) {
                if (files[filesCounter].getName().startsWith(COMMIT_SEQUENCE_FILE_NAME_PREFIX)) {
                    commitSequenceFile = files[filesCounter]; 
                }
                filesCounter++;
            }
        }
        return commitSequenceFile;
    }
    
    /**
     * Returns the {@link File} with the file name equal to the given {@link String}, which denotes a commit hash or
     * number, from the given {@link File} array.
     * 
     * @param commit the {@link String} denoting a commit hash or number for which the corresponding commit file should
     *        be found
     * @param files the {@link File} array in which the commit file should be found
     * @return the {@link File} denoting the commit file or <code>null</code>, if no such file is available in the given
     *         file array
     */
    private File getCommitFile(String commit, File[] files) {
        File commitFile = null;
        if (commit != null && !commit.isBlank()) {
            int filesCounter = 0;
            while (commitFile == null && filesCounter < files.length) {
                if (files[filesCounter].getName().equals(commit)) {
                    commitFile = files[filesCounter];
                }
            }
        }
        return commitFile;
    }
}
