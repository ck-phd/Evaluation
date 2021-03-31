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
package net.ssehub.ckphd.evaluation.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import net.ssehub.ckphd.evaluation.utilities.Logger.MessageType;

/**
 * This class provides utility methods for reading and writing files.
 * 
 * @author Christian Kroeher
 *
 */
public class FileUtilities {
    
    /**
     * The identifier if this class, e.g. for printing messages.
     */
    private static final String ID = "FileUtilities";
    
    /**
     * The single instance of this class.
     */
    private static FileUtilities instance = null;
    
    /**
     * This enumeration defines the different options how to write content to a file. This class differentiates:
     * <ul>
     * <li>CREATE: Creates a new file and writes the new content to that empty file; if the file already exists, an
     *             exception will be thrown</li>
     * <li>OVERWRITE: Overwrites the content of an existing file with the new content; if the file does not exist, an
     *                exception will be thrown</li>
     * <li>APPEND: Appends the new content to the end of the content of an existing file; if the file does not exist, an
     *             exception will be thrown</li>
     * </ul>
     * 
     * @author Christian Kroeher
     *
     */
    public enum WriteOption { CREATE, OVERWRITE, APPEND }
    
    /**
     * The reference to the global {@link Logger}.
     */
    private Logger logger = Logger.getInstance();
    
    /**
     * Constructs new {@link FileUtilities} instance.
     */
    private FileUtilities() {}
    
    /**
     * Returns the single instance of the {@link FileUtilities}.
     * 
     * @return the single instance of the {@link FileUtilities}
     */
    public static FileUtilities getInstance() {
        if (instance == null) {
            instance = new FileUtilities();
        }
        return instance;
    }
    
    /**
     * Writes the given content to the file specified by the given path and file name. Depending on the given
     * {@link WriteOption}, this method creates a new file, overwrites an existing file, or appends the given content to
     * the end of an existing file.
     * 
     * @param path the fully-qualified path to the file to write to
     * @param fileName the name of the file (including the file extension) to write to
     * @param fileContent the content to write
     * @param writeOption the write option defining how to write
     * @throws FileUtilitiesException if creating, writing, overwriting or appending the desired file fails
     */
    public void writeFile(String path, String fileName, String fileContent, WriteOption writeOption)
            throws FileUtilitiesException {
        File file = createFileObject(path, fileName);
        switch(writeOption) {
        case CREATE:
            writeFile(file, fileContent);
            break;
        case OVERWRITE:
            overwriteFile(file, fileContent);
            break;
        case APPEND:
            appendFile(file, fileContent);
            break;
        default:
            // should never be reached
            throw new FileUtilitiesException("Unknown write option \"" + writeOption + "\" for writing file \"" 
                    + file.getAbsolutePath());
        }
    }
    
    /**
     * Creates the given file and writes the given file content to that empty file.
     * 
     * @param file the {@link File} to be created and to which the given file content shall be written
     * @param fileContent the {@link String} defining the content that shall be written to the new file 
     * @throws FileUtilitiesException if the file already exists or writing the file content fails
     */
    private void writeFile(File file, String fileContent) throws FileUtilitiesException {
        if (file.exists()) {
            throw new FileUtilitiesException("Creating new file \"" + file.getAbsolutePath()
                    + "\" denied as the file already exists");
        }
        // Create parent directories, if they do not exist
        Path parentDirectory = file.toPath().getParent();
        try {
            if (!Files.exists(parentDirectory)) {
                Files.createDirectories(parentDirectory);
            }
        } catch (SecurityException e) {
            throw new FileUtilitiesException("Security manager denies checking parent directories of new file \""
                    + file.getAbsolutePath() + "\"", e);
        } catch (IOException e) {
            throw new FileUtilitiesException("Creating parent directories for new file \""
                    + file.getAbsolutePath() + "\" failed", e);
        }
        // Create and write file
        try {
            Files.write(file.toPath(), fileContent.getBytes());
        } catch (IOException e) {
            throw new FileUtilitiesException("Writing content to file \"" + file.getAbsolutePath()
                + "\" failed", e);
        }
    }
    
    /**
     * Overwrites the content of the given file with the given file content.
     * 
     * @param file the existing {@link File} to be overwritten
     * @param fileContent the {@link String} defining the new content that shall be written to the existing file 
     * @throws FileUtilitiesException if the file does not exist or writing the new file content fails
     */
    private void overwriteFile(File file, String fileContent) throws FileUtilitiesException {
        if (!file.exists()) {
            throw new FileUtilitiesException("Overwriting file \"" + file.getAbsolutePath()
                    + "\" impossible as the file does not exist");
        }
        // Overwrite file
        try {
            Files.write(file.toPath(), fileContent.getBytes());
        } catch (IOException e) {
            throw new FileUtilitiesException("Overwriting content of file \"" + file.getAbsolutePath()
                + "\" failed", e);
        }
    }
    
    /**
     * Appends the given file content to the end of the content of the given file.
     * 
     * @param file the existing {@link File} to which the new file content shall be appended
     * @param fileContent the {@link String} defining the new content that shall be appended to the existing file 
     * @throws FileUtilitiesException if the file does not exist or writing the new file content fails
     */
    private void appendFile(File file, String fileContent) throws FileUtilitiesException {
        if (!file.exists()) {
            throw new FileUtilitiesException("Appending file \"" + file.getAbsolutePath()
                    + "\" impossible as the file does not exist");
        }
        // Append file
        try {
            Files.write(file.toPath(), fileContent.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new FileUtilitiesException("Appending content to file \"" + file.getAbsolutePath()
                + "\" failed", e);
        }
    }
    
    /**
     * Reads the content of the given {@link File} and returns a {@link List} of {@link String}s, in which each string
     * contains a single line of the content of the file.
     * 
     * @param file the {@link File} that shall be read
     * @return a {@link List} of {@link String}s representing the line-wise content of the given file
     * @throws FileUtilitiesException if the given file is <code>null</code> or reading the file fails
     */
    public List<String> readFile(File file) throws FileUtilitiesException {
        List<String> fileLines = null;
        if (file != null) {            
            logger.log(ID, "Reading file \"" + file.getAbsolutePath() + "\"", null, MessageType.DEBUG);
            FileReader fileReader = null;
            BufferedReader bufferedReader = null;       
            try {
                fileLines = new ArrayList<String>();
                fileReader = new FileReader(file);
                bufferedReader = new BufferedReader(fileReader);
                String fileLine;
                while ((fileLine = bufferedReader.readLine()) != null) {
                    fileLines.add(fileLine);
                }
            } catch (IOException | OutOfMemoryError e) {
                throw new FileUtilitiesException("Reading content from file \"" + file.getAbsolutePath() + "\" failed",
                        e);
            } finally {
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e) {
                        throw new FileUtilitiesException("Closing file reader for \"" + file.getAbsolutePath()
                        + "\" failed", e);
                    }
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        throw new FileUtilitiesException("Closing buffered reader for \"" + file.getAbsolutePath()
                        + "\" failed", e);
                    }
                }
            }
        } else {
            throw new FileUtilitiesException("The given file to read is \"null\"");
        }
        return fileLines;
    }
    
    /**
     * Creates a new {@link File} based on the given path and file name.
     * 
     * @param path the {@link String} denoting the path to the file that shall be created
     * @param fileName the {@link String} defining the name of the file that shall be created
     * @return a new {@link File} with the given path and file name
     * @throws FileUtilitiesException if the given path or file name is empty
     */
    private File createFileObject(String path, String fileName) throws FileUtilitiesException {
        logger.log(ID, "Creating file", "Path: " + path + System.lineSeparator() + "File name: " + fileName,
                MessageType.DEBUG);
        File file = null;
        if (path != null && !path.isEmpty()) {
            if (fileName != null && !fileName.isEmpty()) {
                file = new File(path, fileName);
            } else {
                throw new FileUtilitiesException("Creating file failed as the given file name is empty");
            }
        } else {
            throw new FileUtilitiesException("Creating file failed as the given file path is empty");
        }
        return file;
    }
}
