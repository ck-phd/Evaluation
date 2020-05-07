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
     * Writes the given content to the file specified by the given path and file name.
     * 
     * @param path the {@link String} denoting the path to the file that shall be (over)written
     * @param fileName the {@link String} defining the name (including the file extension, if needed) of the file that
     *        shall be (over)written
     * @param fileContent the {@link String} defining the content that shall be written to the file
     * @param override specifies whether to override an existing file (<code>true</code>) or not (<code>false</code>)
     * @throws FileUtilitiesException if creating or (over)writing the desired file fails 
     */
    public void writeFile(String path, String fileName, String fileContent, boolean override)
            throws FileUtilitiesException {
        logger.log(ID, "Writing file", "Path: " + path + System.lineSeparator() + "File name: " + fileName
                + System.lineSeparator() + "Content: " + fileContent, MessageType.DEBUG);
        File file = createFile(path, fileName);
        if (override || !file.exists()) {
            Path parentDirectory = file.toPath().getParent();
            try {
                if (!Files.exists(parentDirectory)) {
                    Files.createDirectories(parentDirectory);
                }
                Files.write(file.toPath(), fileContent.getBytes());
            } catch (IOException e) {
                throw new FileUtilitiesException("Writing content to file \"" + file.getAbsolutePath()
                        + "\" failed", e);
            }
        } else {
            throw new FileUtilitiesException("Writing file \"" + file.getAbsolutePath() + "\" denied as the file"
                    + " already exists and overriding is not configured");
        }        
    }
    
    /**
     * Reads the content of the given {@link File} and returns a {@link List} of {@link String}s, in which each string
     * contains a single line of the content of the file.
     * 
     * @param file the {@link File} that shall be read
     * @return a {@link List} of {@link String}s representing the line-wise content of the given file
     * @throws FileUtilitiesException if reading the file fails
     */
    public List<String> readFile(File file) throws FileUtilitiesException {
        logger.log(ID, "Reading file \"" + file.getAbsolutePath() + "\"", null, MessageType.DEBUG);
        List<String> fileLines = null;
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
            throw new FileUtilitiesException("Reading content from file \"" + file.getAbsolutePath() + "\" failed", e);
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
    private File createFile(String path, String fileName) throws FileUtilitiesException {
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
