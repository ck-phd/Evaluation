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
package net.ssehub.ckphd.evaluation.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.ssehub.ckphd.evaluation.core.Executor;
import net.ssehub.ckphd.evaluation.core.SetupException;
import net.ssehub.ckphd.evaluation.utilities.ProcessUtilities.ExecutionResult;

/**
 * This class represents the global setup for a particular evaluation. For creating this setup, it provides the
 * necessary methods for parsing a user-defined configuration file, checking the correctness and consistency of the
 * configuration, and providing individual configuration options to the core of this project. It does <b>not</b> perform
 * the actual configuration. This is subject to the {@link Executor} and the respective classes involved in the
 * execution.
 * 
 * @author Christian Kroeher
 *
 */
public class Setup {

    /**
     * The type of the task to execute by this tool.
     * 
     * @author Christian Kroeher
     *
     */
    public static enum TaskType {
        /**
         * This task type defines executing an evaluation, which applies a given commit sequence iteratively to a
         * prepared repository. This preparation consists of adding a pre- or post-commit hook to the repository with a
         * user-defined content before any commit is applied. In that way, the user is able to call specific actions
         * before or after applying each commit of the given commit sequence to the repository. In order to prepare an
         * evaluation, the generation task enables the creation of the necessary commit sequence.
         * 
         * @see TaskType#GENERATION
         */
        EVALUATION("evaluation"),
        
        /**
         * This task type defines executing a generation of commit (or diff) files based on a given start and end commit
         * (hash/number) from a given repository. Each file contains the full content of artifacts changed by the
         * respective commit including the changes. The resulting files represent a commit sequence that can be used in
         * an evaluation.
         * 
         * @see TaskType#EVALUATION
         */
        GENERATION("generation");
        
        /**
         * The label of a task (simple name).
         */
        private String taskTypeLabel;
        
        /**
         * Constructs a new {@link TaskType} instance.
         * 
         * @param label the simple name of the task to create
         */
        private TaskType(String label) {
            taskTypeLabel = label;
        }
        
        /**
         * Returns the simple name of this task instance.
         * 
         * @return the simple name of this task instance; never <code>null</code>
         */
        @Override
        public String toString() {
            return taskTypeLabel;
        }
        
    }; 
    
    /**
     * The type of streams supported by this tool for printing logging information to.
     * 
     * @author Christian Kroeher
     *
     */
    public static enum StreamType {
        /**
         * This stream type defines printing all logging information to {@link System#out}. This is the default stream
         * type for any standard logging information (information, warnings, errors).
         */
        SYSTEM,
        
        /**
         * This stream type defines printing all logging information to a file. This file is created by the tool during
         * applying a setup and stored at the respective output directory.
         */
        FILE,
        
        /**
         * This stream type defines printing no logging information at all. This is the default stream type for any
         * debug logging information as logging such information is optional.
         */
        NONE
    };
    
    /**
     * The type of commit hook to be created by this tool during {@link TaskType#EVALUATION}.
     * 
     * @author Christian Kroeher
     *
     */
    public static enum CommitHookType {
        /**
         * This commit hook type defines creating a pre-commit hook during {@link TaskType#EVALUATION}.
         */
        PRE,
        
        /**
         * This commit hook type defines creating a post-commit hook during {@link TaskType#EVALUATION}.
         */
        POST
    };
    
    /**
     * The configuration property set provided by this tool to customize its execution.
     * 
     * @author Christian Kroeher
     *
     */
    private enum Configuration {
        /**
         * This property enables the definition of the {@link TaskType} to execute.
         */
        CORE_TASK(
            "core.task",
            "> generation >> Use to generate commit sequence from repository from start to end commit"
                + System.lineSeparator()
                + "> evaluation >> Use to start evalutation with given repository, commit sequence, and commit hook"
        ),
        
        /**
         * This property enables the definition of the output directory to store any results created by this tool.
         */
        CORE_OUTPUT_DIRECTORY(
            "core.output_directory",
            "Use fully-qualified path to existing directory to save execution results to"),
        
        /**
         * This optional property enables the definition of the {@link StreamType} for logging standard information. If
         * this property is not defined, the default value of {@link Setup#standardOutputStream} will be used.
         */
        LOGGING_STANDARD_STREAM(
            "logging.standard_stream",
            "> system >> Use to print standard log information to System.out (typically console) [default]"
                + System.lineSeparator()
                + "> file >> Use to print standard log information to log file created by this tool"
                + System.lineSeparator()
                + "> none >> Use to disable printing standard log information"
        ),
        
        /**
         * This optional property enables the definition of the {@link StreamType} for logging debug information. If
         * this property is not defined, the default value of {@link Setup#debugOutputStream} will be used.
         */
        LOGGING_DEBUG_STREAM(
            "logging.debug_stream",
            "> system >> Use to print debug information to System.out (typically console)"
                + System.lineSeparator()
                + "> file >> Use to print debug information to log file created by this tool"
                + System.lineSeparator()
                + "> none >> Use to disable printing debug information [default]"
        ),
        
        /**
         * This property enables the definition of the directory denoting a repository from which commits shall be
         * generated. This property is only required for {@link TaskType#GENERATION}.
         */
        GENERATION_REPOSITORY_DIRECTORY(
            "generation.repository_directory",
            "Use fully-qualified path to existing repository (directory) to generate commit sequence from"
        ),
        
        /**
         * This property enables the definition of the start commit (hash) for generating commits from the defined
         * {@link Configuration#GENERATION_REPOSITORY_DIRECTORY}. This property is only required for
         * {@link TaskType#GENERATION}.
         */
        GENERATION_START_COMMIT(
            "generation.start_commit",
            "Use existing commit (hash/number) in specified \"generation.repository_directory\" to start generating"
                + " commit sequence from"
        ),
        
        /**
         * This property enables the definition of the end commit (hash) for generating commits from the defined
         * {@link Configuration#GENERATION_REPOSITORY_DIRECTORY}. This property is only required for
         * {@link TaskType#GENERATION}.
         */
        GENERATION_END_COMMIT(
            "generation.end_commit",
            "Use existing commit (hash/number) in specified \"generation.repository_directory\" to end generating"
                + " commit sequence at"
        ),
        
        /**
         * This property enables the definition of the archive file (*.zip) containing the repository to use for an
         * evaluation. This property is only required for {@link TaskType#EVALUATION}.
         */
        EVALUATION_REPOSITORY_ARCHIVE(
            "evaluation.repository_archive",
            "Use fully-qualified path to existing archive file (*.zip) containing the repository for evaluation"
        ),
        
        /**
         * This property enables the definition of the directory containing the commit sequence (set of individual
         * commit files) to apply iteratively to the repository in the defined
         * {@link Configuration#EVALUATION_REPOSITORY_ARCHIVE}. This property is only required for
         * {@link TaskType#EVALUATION}.
         */
        EVALUATION_COMMIT_SEQUENCE_DIRECTORY(
            "evaluation.commits_directory",
            "Use fully-qualified path to existing directory containing the commit sequence to apply to the repository"
                + " in \"evaluation.repository_archive\""
        ),
        
        /**
         * This property enables the definition of the {@link CommitHookType} for preparing the repository in the
         * defined {@link Configuration#EVALUATION_REPOSITORY_ARCHIVE}. This property is only required for
         * {@link TaskType#EVALUATION}.
         */
        EVALUATION_COMMIT_HOOK_TYPE(
            "evaluation.commit_hook_type",
            "> pre >> Use to create a pre-commit hook with the content of \"evaluation.commit_hook_content\" for the"
                + " repository in \"evaluation.repository_archive\""
                + System.lineSeparator()
                + "> post >> Use to create a post-commit hook with the content of \"evaluation.commit_hook_content\""
                + " for the repository in \"evaluation.repository_archive\""
        ),
        
        /**
         * This property enables the definition of the commit hook content written to the commit hook file during
         * preparation of the repository in the defined {@link Configuration#EVALUATION_REPOSITORY_ARCHIVE}. This
         * property is only required for {@link TaskType#EVALUATION}.
         */
        EVALUATION_COMMIT_HOOK_CONTENT(
            "evaluation.commit_hook_content",
            "Use string containing all instructions to be written to the created pre- or post-commit hook");
        
        /**
         * The key of a configuration property. This is the string, which is used as a property identifier in a
         * configuration file.
         */
        private String configKey;
        
        /**
         * The textual description of a configuration property. This is the string to be printed, if a required property
         * is missing or the validation of its user-defined value fails.
         */
        private String configDescription;
        
        /**
         * Constructs a new {@link Configuration} instance.
         * 
         * @param key the property identifier as used in a configuration file for this new property
         * @param description a textual description (of valid values, etc.) of this new property
         */
        private Configuration(String key, String description) {
            configKey = key;
            configDescription = description;
        }
        
        /**
         * Returns the textual description (of valid values, etc.) of this property.
         * 
         * @return the textual description of this property
         */
        public String getDescription() {
            return configDescription;
        }
        
        /**
         * Returns the property identifier of this property as used in a configuration file.
         * 
         * @return the property identifier of this property
         */
        @Override
        public String toString() {
            return configKey;
        }
        
    }
    
    /**
     * The local reference to the global {@link ProcessUtilities} instance.
     */
    private ProcessUtilities processUtilities = ProcessUtilities.getInstance();
    
    /**
     * The local reference to the global {@link FileUtilities} instance.
     */
    private FileUtilities fileUtilities = FileUtilities.getInstance();
    
    /**
     * The (fully-qualified) path to the configuration file used to create this setup instance. This path is provided by
     * the arguments passed to the constructor {@link Setup#Setup(String[])}.
     */
    private String configurationFilePath;
    
    /**
     * The configured task type to execute by this tool.
     */
    private TaskType taskType;
    
    /**
     * The configured (parent) output directory to store the results of executing the respective task of this tool. 
     */
    private File outputDirectory;
    
    /**
     * The configured stream for printing standard logging information (information, warnings, errors) to. As this
     * configuration is optional, the default value is {@link StreamType#SYSTEM}.
     */
    private StreamType standardOutputStream = StreamType.SYSTEM;
    
    /**
     * The configured stream for printing debug information to. As this configuration is optional, the default value is
     * {@link StreamType#NONE}.
     */
    private StreamType debugOutputStream = StreamType.NONE;
    
    /**
     * The configured directory denoting the repository from which a commit sequence shall be generated. This attribute
     * is only available, if {@link Setup#taskType} is {@link TaskType#GENERATION}.
     */
    private File repositoryDirectory;
    
    /**
     * The configured commit (hash/number) defining the start of the commit sequence to generate from the repository
     * denoted by the configured {@link #repositoryDirectory}. This attribute is only available, if
     * {@link Setup#taskType} is {@link TaskType#GENERATION}.
     */
    private String startCommit;
    
    /**
     * The configured commit (hash/number) defining the end of the commit sequence to generate from the repository
     * denoted by the configured {@link #repositoryDirectory}. This attribute is only available, if
     * {@link Setup#taskType} is {@link TaskType#GENERATION}.
     */
    private String endCommit;
    
    /**
     * The configured file denoting an archive, which contains a repository. That repository will be extracted from the
     * archive and used for evaluation by creating a specific commit hook and applying a commit sequence to it. This
     * attribute is only available, if {@link Setup#taskType} is {@link TaskType#EVALUATION}.
     */
    private File repositoryArchive;
    
    /**
     * The configured directory containing the commit sequence to apply to the repository contained in the configured
     * {@link #repositoryArchive}. This commit sequence consists of a set of commit or diff files to be applied
     * iteratively to that repository. This attribute is only available, if {@link Setup#taskType} is
     * {@link TaskType#EVALUATION}.
     */
    private File commitSequenceDirectory;
    
    /**
     * The configured hook type defining the type of commit hook to created for the repository contained in the
     * configured {@link #repositoryArchive}. This attribute is only available, if {@link Setup#taskType} is
     * {@link TaskType#EVALUATION}.
     */
    private CommitHookType commitHookType;
    
    /**
     * The configured hook content to write to the commit hook (file) defined by the configured {@link #commitHookType}
     * and for the repository contained in the configured {@link #repositoryArchive}. This attribute is only available,
     * if {@link Setup#taskType} is {@link TaskType#EVALUATION}.
     */
    private String commitHookContent;
    
    /**
     * Constructs a new {@link Setup} instance.
     * 
     * @param args the user-defined argument set as passed to the main-method of this tool; must not be
     *        <code>null</code>
     * @throws SetupException if the given argument set has not exactly one element, that element does not denote an
     *         existing file, or parsing the content of that file fails due to being not a valid configuration file for
     *         this tool
     */
    public Setup(String[] args) throws SetupException {
        parse(args);
    }
    
    /**
     * Parses the given arguments and tries to create the actual setup of this tool.
     * 
     * @param args the user-defined argument set as passed to the main-method of this tool; must not be
     *        <code>null</code>
     * @throws SetupException if the given argument set has not exactly one element, that element does not denote an
     *         existing file, or parsing the content of that file fails due to being not a valid configuration file for
     *         this tool
     */
    private void parse(String[] args) throws SetupException {
        if (args.length == 0) {
            throw new SetupException("Missing argument: pass (path to) configuration file");
        }
        if (args.length > 1) {
            throw new SetupException("Too much arguments: pass only one (path to) configuration file");
        }
        File configurationFile;
        try {
            configurationFile = fileUtilities.getCheckedFileObject(args[0], false);
        } catch (FileUtilitiesException e) {
            throw new SetupException("Creating configuration file object for \"" + args[0] + "\" failed", e);
        }
        configurationFilePath = configurationFile.getAbsolutePath();
        try {
            Properties configurationProperties = parse(configurationFile);
            // TODO check whether properties are unique? No configuration defined twice or more!
            setProperties(configurationProperties);
        } catch (SetupException e) {
            throw new SetupException("Setup based on configuration file \"" + configurationFilePath + "\" failed", e);
        }
    }
    
    /**
     * Parses the content of the given file to create the properties to return.
     * 
     * @param configurationFile the configuration file to parse for creating the returned properties
     * @return the properties parsed from the given configuration file; never <code>null</code>
     * @throws SetupException if reading the file content or loading the inherent properties fails
     */
    private Properties parse(File configurationFile) throws SetupException {
        Properties configurationProperties = null;
        InputStream configurationFileInputStream = null;
        try {            
            configurationFileInputStream = new FileInputStream(configurationFile);
            configurationProperties = new Properties();
            configurationProperties.load(configurationFileInputStream);
        } catch (FileNotFoundException e) {
            throw new SetupException("File does not exist, is not a regular file, or cannot be read", e);
        } catch (SecurityException e) {
            throw new SetupException("Security manager denies reading file", e);
        } catch (IOException e) {
            throw new SetupException("Error while reading file", e);
        } catch (IllegalArgumentException e) {
            throw new SetupException("Input stream for file contains malformed Unicode escape sequence", e);
        } finally {
            if (configurationFileInputStream != null) {
                try {
                    configurationFileInputStream.close();
                } catch (IOException e) {
                    throw new SetupException("Closing input stream for file failed", e);
                }
            }
        }
        return configurationProperties;
    }
    
    /**
     * Sets the individual configuration properties of this setup instance based on the given properties.
     * 
     * @param configurationProperties the properties to use for setting the individual configuration properties
     * @throws SetupException if the given properties are <code>null</code> or <i>empty</i>, mandatory properties are
     *         missing, or properties have invalid values
     */
    private void setProperties(Properties configurationProperties) throws SetupException {
        if (configurationProperties == null || configurationProperties.isEmpty()) {
            throw new SetupException("No configuration properties available");
        }
        // Set core properties
        setTask(getProperty(configurationProperties, Configuration.CORE_TASK));
        setOutputDirectory(getProperty(configurationProperties, Configuration.CORE_OUTPUT_DIRECTORY));
        // Set (optional) standard logging stream property
        try {
            setStandardLoggingStream(getProperty(configurationProperties, Configuration.LOGGING_STANDARD_STREAM));
        } catch (SetupException e) {
            if (!e.getMessage().startsWith("Missing")) {
                // Logging property is not missing (would be ok as it is optional), but has no value
                throw e;
            }
        }
        // Set (optional) debug logging stream property
        try {
            setDebugLoggingStream(getProperty(configurationProperties, Configuration.LOGGING_DEBUG_STREAM));
        } catch (SetupException e) {
            if (!e.getMessage().startsWith("Missing")) {
                // Logging property is not missing (would be ok as it is optional), but has no value
                throw e;
            }
        }
        // Set task-specific properties 
        switch(taskType) {
        case EVALUATION:
            setEvaluationProperties(getProperty(configurationProperties, Configuration.EVALUATION_REPOSITORY_ARCHIVE),
                    getProperty(configurationProperties, Configuration.EVALUATION_COMMIT_SEQUENCE_DIRECTORY),
                    getProperty(configurationProperties, Configuration.EVALUATION_COMMIT_HOOK_TYPE),
                    getProperty(configurationProperties, Configuration.EVALUATION_COMMIT_HOOK_CONTENT));
            break;
        case GENERATION:
            setGenerationProperties(getProperty(configurationProperties, Configuration.GENERATION_REPOSITORY_DIRECTORY),
                    getProperty(configurationProperties, Configuration.GENERATION_START_COMMIT),
                    getProperty(configurationProperties, Configuration.GENERATION_END_COMMIT));
            break;
        default:
            // should never be reached
            throw new SetupException("Unknown task \"" + taskType.name() + "\"");
        }
    }
    
    /**
     * Returns the property value in the given properties of the property with the given property key.
     *  
     * @param configurationProperties the configuration properties in which to search for a property with the given
     *        property key; must not be <code>null</code>
     * @param propertyKey the key of the property to search for in the given configuration properties; must not be
     *        <code>null</code>
     * @return the value of the property identified by the given property key in the given properties; never
     *         <code>null</code>
     * @throws SetupException if the property does not exist or its value is only an empty string 
     */
    private String getProperty(Properties configurationProperties, Configuration propertyKey) throws SetupException {
        String propertyValue = null;
        if (!configurationProperties.containsKey(propertyKey.configKey)) {
            throw new SetupException("Missing property \"" + propertyKey.configKey + "\": "
                    + propertyKey.getDescription());
        }
        propertyValue = configurationProperties.getProperty(propertyKey.configKey);
        if (propertyValue.isBlank()) {
            throw new SetupException("Empty property \"" + propertyKey.configKey + "\": "
                    + propertyKey.getDescription());
        }
        return propertyValue;
    }
    
    /**
     * Sets the {@link #taskType} of this setup instance.
     * 
     * @param taskPropertyValue the value of the property defining the task to execute; must not be <code>null</code>
     * @throws SetupException if the given value does not represent a supported {@link TaskType}
     */
    private void setTask(String taskPropertyValue) throws SetupException {
        // Parameter never null nor empty: see setProperties() and getProperty()
        if (taskPropertyValue.equals(TaskType.EVALUATION.toString())) {
            taskType = TaskType.EVALUATION;
        } else if (taskPropertyValue.equals(TaskType.GENERATION.toString())) {
            taskType = TaskType.GENERATION;
        } else {
            throw new SetupException("Setting task \"" + taskPropertyValue + "\" failed: " 
                    + Configuration.CORE_TASK.getDescription());
        }
    }
    
    /**
     * Sets the {@link #outputDirectory} of this setup instance.
     * 
     * @param outputDirectoryPropertyValue the value of the property defining the output directory; must not be
     *        <code>null</code>
     * @throws SetupException if the given value does not represent a valid and existing directory
     */
    private void setOutputDirectory(String outputDirectoryPropertyValue) throws SetupException {
        // Parameter never null nor empty: see setProperties() and getProperty()
        try {
            outputDirectory = fileUtilities.getCheckedFileObject(outputDirectoryPropertyValue, true);
        } catch (FileUtilitiesException e) {
            throw new SetupException("Setting output directory \"" + outputDirectoryPropertyValue + "\" failed: " 
                    + Configuration.CORE_OUTPUT_DIRECTORY.getDescription(), e);
        }
    }
    
    /**
     * Sets the {@link #standardOutputStream} of this setup instance.
     * 
     * @param standardStreamPropertyValue the value of the property defining the standard logging information stream;
     *        must not be <code>null</code>
     */
    private void setStandardLoggingStream(String standardStreamPropertyValue) {
        // Parameters never null nor empty: see setProperties() and getProperty()
        if (standardStreamPropertyValue.equalsIgnoreCase(StreamType.NONE.name())) {
            standardOutputStream = StreamType.NONE;
        } else if (standardStreamPropertyValue.equalsIgnoreCase(StreamType.SYSTEM.name())) {
            standardOutputStream = StreamType.SYSTEM;
        } else if (standardStreamPropertyValue.equalsIgnoreCase(StreamType.FILE.name())) {
            standardOutputStream = StreamType.FILE;
        }
    }
    
    /**
     * Sets the {@link #standardOutputStream} of this setup instance.
     * 
     * @param debugStreamPropertyValue the value of the property defining the debug information stream;  must not be
     *        <code>null</code>
     */
    private void setDebugLoggingStream(String debugStreamPropertyValue) {
        // Parameters never null nor empty: see setProperties() and getProperty()
        if (debugStreamPropertyValue.equalsIgnoreCase(StreamType.NONE.name())) {
            debugOutputStream = StreamType.NONE;
        } else if (debugStreamPropertyValue.equalsIgnoreCase(StreamType.SYSTEM.name())) {
            debugOutputStream = StreamType.SYSTEM;
        } else if (debugStreamPropertyValue.equalsIgnoreCase(StreamType.FILE.name())) {
            debugOutputStream = StreamType.FILE;
        }
    }
    
    /**
     * Sets the {@link #repositoryArchive}, the {@link #commitSequenceDirectory}, the {@link #commitHookType}, and the
     * {@link #commitHookContent} of this setup instance.
     * 
     * @param repositoryArchivePropertyValue the value of the property defining the repository archive for evaluation;
     *        must not be <code>null</code>
     * @param commitSequenceDirectoryPropertyValue the value of the property defining the commit sequence directory for
     *        evaluation; must not be <code>null</code>
     * @param commitHookTypePropertyValue the value of the property defining the commit hook type for evaluation; must
     *        not be <code>null</code>
     * @param commitHookContentPropertyValue the value of the property defining the commit hook content for evaluation;
     *        must not be <code>null</code>
     * @throws SetupException if the given values do not represent a valid archive file containing a repository, a valid
     *         directory, a valid {@link CommitHookType}, or a non-empty hook content, respectively
     */
    private void setEvaluationProperties(String repositoryArchivePropertyValue,
            String commitSequenceDirectoryPropertyValue, String commitHookTypePropertyValue,
            String commitHookContentPropertyValue) throws SetupException {
        // Parameters never null nor empty: see setProperties() and getProperty()
        // Check, if repository archive exists and is a file
        try {
            repositoryArchive = fileUtilities.getCheckedFileObject(repositoryArchivePropertyValue, false);
        } catch (FileUtilitiesException e) {
            throw new SetupException("Setting repository archive \"" + repositoryArchivePropertyValue
                    + "\" failed: " + Configuration.EVALUATION_REPOSITORY_ARCHIVE.getDescription(), e);
        }
        // Check, if repository archive is an archive
        if (!repositoryArchive.getName().endsWith(".zip")) {
            throw new SetupException("Repository archive \"" + repositoryArchivePropertyValue
                    + "\" is not a zip archive: " + Configuration.EVALUATION_REPOSITORY_ARCHIVE.getDescription());
        }
        // Check, if repository archive can be extracted
        String temporaryDirectoryName = "evaluation-setup-check_" + System.currentTimeMillis();
        File temporaryDirectory = null;
        boolean containsGitRepository = false;
        try {
            temporaryDirectory = fileUtilities.createDirectory(System.getProperty("java.io.tmpdir"),
                    temporaryDirectoryName);
            File extractedRepository = ArchiveUtilities.getInstance().extract(repositoryArchive, temporaryDirectory);
            // Check, if repository archive contains a Git repository (just the execution)
            containsGitRepository = isGitRepository(extractedRepository);
        } catch (FileUtilitiesException | ArchiveUtilitiesException | SetupException e) {
            throw new SetupException("Validating repository archive \"" + repositoryArchive.getAbsolutePath()
                    + "\" failed: " + Configuration.EVALUATION_REPOSITORY_ARCHIVE.getDescription(), e);
        } finally {
            if (temporaryDirectory != null) {
                try {
                    fileUtilities.delete(temporaryDirectory);
                } catch (FileUtilitiesException e) {
                    throw new SetupException("Deleting temporary directory \"" + temporaryDirectory.getAbsolutePath()
                            + "\" for checking repository archive failed", e);
                }
            }
        }
        // Check, if repository archive contains a Git repository (actual result check)
        if (!containsGitRepository) {
            throw new SetupException("Repository archive \"" + repositoryArchive.getAbsolutePath()
                    + "\" does not contain a Git repository: "
                    + Configuration.EVALUATION_REPOSITORY_ARCHIVE.getDescription());
        }
        // Check, if commit sequence directory exists and is a directory
        try {
            commitSequenceDirectory = fileUtilities.getCheckedFileObject(commitSequenceDirectoryPropertyValue, true);
        } catch (FileUtilitiesException e) {
            throw new SetupException("Setting commit sequence directory \"" + commitSequenceDirectoryPropertyValue
                    + "\" failed: " + Configuration.EVALUATION_COMMIT_SEQUENCE_DIRECTORY.getDescription(), e);
        }
        // TODO Check, if commit sequence directory contains at least one diff-file/commit
        // Check, if commit hook type is supported
        if (commitHookTypePropertyValue.equalsIgnoreCase(CommitHookType.PRE.name())) {
            commitHookType = CommitHookType.PRE;
        } else if (commitHookTypePropertyValue.equalsIgnoreCase(CommitHookType.POST.name())) {
            commitHookType = CommitHookType.POST;
        } else {
            throw new SetupException("Setting commit hook type \"" + commitHookTypePropertyValue + "\" failed: "
                    + Configuration.EVALUATION_COMMIT_HOOK_TYPE.getDescription());
        }
        // Check for empty commit hook content (empty property value) already done by getProperty() before this method.
        // Hence, only action is to remove potential leading and trailing quotes.
        if (commitHookContentPropertyValue.charAt(0) == '\"'
                && commitHookContentPropertyValue.charAt(commitHookContentPropertyValue.length() - 1) == '\"') {
            commitHookContent =
                    commitHookContentPropertyValue.substring(1, commitHookContentPropertyValue.length() - 1);
        } else {            
            commitHookContent = commitHookContentPropertyValue;
        }
    }
    
    /**
     * Sets the {@link #repositoryDirectory}, the {@link #startCommit}, and the {@link #endCommit} of this setup
     * instance.
     * 
     * @param repositoryDirectoryPropertyValue the value of the property defining the repository directory for
     *        generation; must not be <code>null</code>
     * @param startCommitPropertyValue the value of the property defining the start commit (hash/number) for generation;
     *        must not be <code>null</code>
     * @param endCommitPropertyValue the value of the property defining the end commit (hash/number) for generation;
     *        must not be <code>null</code>
     * @throws SetupException if the given values do not represent a valid repository directory, a valid start or end
     *         commit in that repository, respectively
     */
    private void setGenerationProperties(String repositoryDirectoryPropertyValue, String startCommitPropertyValue,
            String endCommitPropertyValue) throws SetupException {
        // Parameters never null nor empty: see setProperties() and getProperty()
        try {            
            repositoryDirectory = fileUtilities.getCheckedFileObject(repositoryDirectoryPropertyValue, true);
        } catch (FileUtilitiesException e) {
            throw new SetupException("Setting repository directory \"" + repositoryDirectoryPropertyValue
                    + "\" failed: " + Configuration.GENERATION_REPOSITORY_DIRECTORY.getDescription(), e);
        }
        // Check, if repository directory is a Git repository
        if (!isGitRepository(repositoryDirectory)) {
            throw new SetupException("Repository directory \"" + repositoryDirectory.getAbsolutePath()
                    + "\" is not a Git repository: " + Configuration.GENERATION_REPOSITORY_DIRECTORY.getDescription());
            
        }
        // Check, if start commit is available in repository
        try {
            if (!containsCommit(repositoryDirectory, startCommitPropertyValue)) {
                throw new SetupException("Start commit \"" + startCommitPropertyValue
                        + "\" is not available in repository \"" + repositoryDirectory.getAbsolutePath() + "\": "
                        + Configuration.GENERATION_START_COMMIT.getDescription());
            }
            startCommit = startCommitPropertyValue;
        } catch (ProcessUtilitiesException e) {
            throw new SetupException("Validating start commit \"" + startCommitPropertyValue + "\" failed", e);
        }
        // Check, if end commit is available in repository
        try {
            if (!containsCommit(repositoryDirectory, endCommitPropertyValue)) {
                throw new SetupException("End commit \"" + endCommitPropertyValue
                        + "\" is not available in repository \"" + repositoryDirectory.getAbsolutePath() + "\":"
                        + Configuration.GENERATION_END_COMMIT.getDescription());
            }
            endCommit = endCommitPropertyValue;
        } catch (ProcessUtilitiesException e) {
            throw new SetupException("Validating end commit \"" + endCommitPropertyValue + "\" failed", e);
        }
    }
    
    /**
     * Checks whether the given file is a directory, which represents a Git repository.
     * 
     * @param file the file to check for being a Git repository
     * @return <code>true</code>, if the given is a Git repository; <code>false</code> otherwise
     * @throws SetupException if executing the Git status command for checking the file for being a Git repository fails
     */
    private boolean isGitRepository(File file) throws SetupException {
        boolean isGitRepository = false;
        if (file != null && file.isDirectory()) {            
            String[] gitStatusCommand = {"git", "status"};
            ExecutionResult gitStatusResult;
            try {
                gitStatusResult = processUtilities.executeCommand(gitStatusCommand, file);
            } catch (ProcessUtilitiesException e) {
                throw new SetupException("Validating Git repository \"" + file.getAbsolutePath() + "\" failed", e);
            }
            if (gitStatusResult.getProcessExitValue() == 0) {
                isGitRepository = true;
            }
        }
        return isGitRepository;
    }
    
    /**
     * Checks whether the given commit (hash/number) is an existing commit in the Git repository denoted by the given
     * file. This check does not include checking whether the given file is a Git repository.
     * 
     * @param repository the file denoting a Git repository
     * @param commit the commit (hash/number) to check for being available in the given Git repository
     * @return <code>true</code>, if the given commit is available in the given Git repository; <code>false</code>
     *         otherwise
     * @throws ProcessUtilitiesException if executing the Git show command for the given commit fails
     */
    private boolean containsCommit(File repository, String commit) throws ProcessUtilitiesException {
        boolean repositoryContainsCommit = false;
        String[] gitShowCommand = {"git", "show", commit};
        ExecutionResult gitShowResult = processUtilities.executeCommand(gitShowCommand, repository);
        if (gitShowResult.getProcessExitValue() == 0) {
            repositoryContainsCommit = true;
        }
        return repositoryContainsCommit;
    }
    
    /**
     * Returns the configured {@link #taskType} of this setup instance.
     * 
     * @return the type of task to execute; never <code>null</code>
     */
    public TaskType getTaskType() {
        return taskType;
    }
    
    /**
     * Returns the configured {@link #outputDirectory} of this setup instance.
     * 
     * @return the (main) directory for storing results of executing the configured task of this tool; never
     *         <code>null</code> and always a valid directory
     */
    public File getOutputDirectory() {
        return outputDirectory;
    }
    
    /**
     * Returns the (configured) {@link #standardOutputStream} of this setup instance.
     * 
     * @return the type of stream to use for printing standard logging information (information, warnings, errors);
     *         never <code>null</code>
     */
    public StreamType getStandardOutputStreamType() {
        return standardOutputStream;
    }
    
    /**
     * Returns the (configured) {@link #debugOutputStream} of this setup instance.
     * 
     * @return the type of stream to use for printing debug information; never <code>null</code>
     */
    public StreamType getDebugOutputStreamType() {
        return debugOutputStream;
    }
    
    /**
     * Returns the configured {@link #repositoryDirectory} of this setup instance.
     * 
     * @return the directory denoting a valid Git repository for generating a commit sequence or <code>null</code>, if
     *         the configured task type is {@link TaskType#EVALUATION}.
     * @see #getTaskType()
     * @see #getStartCommit()
     * @see #getEndCommit()
     */
    public File getRepositoryDirectory() {
        return repositoryDirectory;
    }
    
    /**
     * Returns the configured {@link #startCommit} of this setup instance.
     * 
     * @return the valid start commit for generating a commit sequence or <code>null</code>, if the configured task type
     *         is {@link TaskType#EVALUATION}.
     * @see #getTaskType()
     * @see #getRepositoryDirectory()
     * @see #getEndCommit()
     */
    public String getStartCommit() {
        return startCommit;
    }
    
    /**
     * Returns the configured {@link #endCommit} of this setup instance.
     * 
     * @return the valid end commit for generating a commit sequence or <code>null</code>, if the configured task type
     *         is {@link TaskType#EVALUATION}.
     * @see #getTaskType()
     * @see #getRepositoryDirectory()
     * @see #getStartCommit()
     */
    public String getEndCommit() {
        return endCommit;
    }
    
    /**
     * Returns the configured {@link #repositoryArchive} of this setup instance.
     * 
     * @return the valid archive file containing a repository for evaluation or <code>null</code>, if the configured
     *         task type is {@link TaskType#GENERATION}.
     * @see #getTaskType()
     * @see #getCommitSequenceDirectory()
     * @see #getCommitHookType()
     * @see #getCommitHookContent()
     */
    public File getRepositoryArchive() {
        return repositoryArchive;
    }
    
    /**
     * Returns the configured {@link #commitSequenceDirectory} of this setup instance.
     * 
     * @return the valid directory containing a commit sequence for evaluation or <code>null</code>, if the configured
     *         task type is {@link TaskType#GENERATION}.
     * @see #getTaskType()
     * @see #getRepositoryArchive()
     * @see #getCommitHookType()
     * @see #getCommitHookContent()
     */
    public File getCommitSequenceDirectory() {
        return commitSequenceDirectory;
    }
    
    /**
     * Returns the configured {@link #commitHookType} of this setup instance.
     * 
     * @return the valid commit hook type supported by this tool for evaluation or <code>null</code>, if the configured
     *         task type is {@link TaskType#GENERATION}.
     * @see #getTaskType()
     * @see #getRepositoryArchive()
     * @see #getCommitSequenceDirectory()
     * @see #getCommitHookContent()
     */
    public CommitHookType getCommitHookType() {
        return commitHookType;
    }
    
    /**
     * Returns the configured {@link #commitHookContent} of this setup instance.
     * 
     * @return the non-empty string to write to the commit hook (file) for evaluation or <code>null</code>, if the
     *         configured task type is {@link TaskType#GENERATION}.
     * @see #getTaskType()
     * @see #getRepositoryArchive()
     * @see #getCommitSequenceDirectory()
     * @see #getCommitHookType()
     */
    public String getCommitHookContent() {
        return commitHookContent;
    }
    
}
