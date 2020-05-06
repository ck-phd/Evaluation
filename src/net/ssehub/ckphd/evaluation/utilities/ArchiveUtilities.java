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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.ssehub.ckphd.evaluation.utilities.Logger.MessageType;

/**
 * This class provides utility methods for extracting the content of archive files to a specific location.
 * 
 * @author Christian Kroeher
 *
 */
public class ArchiveUtilities {

    /**
     * The identifier if this class, e.g. for printing messages.
     */
    private static final String ID = "ArchiveUtilities";
    
    /**
     * The single instance of this class.
     */
    private static ArchiveUtilities instance = null;
    
    /**
     * The reference to the global {@link Logger}.
     */
    private Logger logger = Logger.getInstance();
    
    /**
     * Constructs a new {@link ArchiveUtilities} instance.
     */
    private ArchiveUtilities() {}
    
    /**
     * Returns the single instance of the {@link ArchiveUtilities}.
     * 
     * @return the single instance of the {@link ArchiveUtilities}
     */
    public static ArchiveUtilities getInstance() {
        if (instance == null) {
            instance = new ArchiveUtilities();
        }
        return instance;
    }
    
    /**
     * Extracts the entries of the archive denoted by the given {@link File}. The extracted entries will be stored in
     * the parent directory of the given archive.
     * 
     * @param archiveFile the {@link File} denoting the archive from which the entries shall be extracted; should never
     *        be <code>null</code>, must <i>exist</i>, and must be an <i>archive</i>
     * @return the {@link File} denoting the root or first entry in the given archive or <code>null</code>, if the
     *         extraction fails
     * @throws ArchiveUtilitiesException if opening the given archive file, reading or writing the entries fails
     */
    public File extract(File archiveFile) throws ArchiveUtilitiesException {
        File extractedRootElement = null;
        FileInputStream fileInputStream = null;
        ZipInputStream zipInputStream = null;
        try {
            fileInputStream = new FileInputStream(archiveFile.getAbsolutePath());
            zipInputStream = new ZipInputStream(fileInputStream);
            ZipEntry rootZipEntry = zipInputStream.getNextEntry(); // Used for the return value of this method
            ZipEntry zipEntry = rootZipEntry;
            logger.log(ID, "Extracting entries from archive file \"" + archiveFile.getAbsolutePath() + "\"", "",
                    MessageType.INFO);
            do {
                extract(zipEntry, zipInputStream, archiveFile.getParentFile());
                zipEntry = zipInputStream.getNextEntry();
            } while (zipEntry != null);            
            extractedRootElement = new File(archiveFile.getParentFile(), rootZipEntry.getName());
        } catch (FileNotFoundException | SecurityException e) {
            throw new ArchiveUtilitiesException("Creating file input stream for archive file \"" 
                    + archiveFile.getAbsolutePath() + "\" failed", e);
        } catch (IOException e) {
            throw new ArchiveUtilitiesException("Reading entry from archive file \"" + archiveFile.getAbsolutePath()
                    + "\" failed", e);
        } finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (IOException e) {
                    throw new ArchiveUtilitiesException("Closing zip input stream for archive file \"" 
                            + archiveFile.getAbsolutePath() + "\" failed", e);
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    throw new ArchiveUtilitiesException("Closing file input stream for archive file \"" 
                            + archiveFile.getAbsolutePath() + "\" failed", e);
                }
            }
        }
        return extractedRootElement;
    }
    
    /**
     * Extracts the given {@link ZipEntry} via the given {@link ZipInputStream} to the given destination {@link File}
     * (directory).
     * 
     * @param zipEntry the {@link ZipEntry} to extract
     * @param zipInputStream the {@link ZipInputStream} of the given zip entry; should never be <code>null</code>
     * @param destinationDirectory the {@link File} denoting the destination directory for extracting the given zip
     *        entry; should never be <code>null</code> and must always be an existing directory
     * @throws ArchiveUtilitiesException if extracting the given zip entry fails
     */
    private void extract(ZipEntry zipEntry, ZipInputStream zipInputStream, File destinationDirectory)
            throws ArchiveUtilitiesException {
        if (zipEntry != null) {
            File zipEntryFile = new File(destinationDirectory, zipEntry.getName());
            if (zipEntry.isDirectory()) {
                if (!zipEntryFile.mkdir()) {                    
                    throw new ArchiveUtilitiesException("Creating the directory \"" + zipEntryFile.getAbsolutePath() 
                            + "\" for archive entry \"" + zipEntry.getName() + "\" failed");
                }
            } else {
                FileOutputStream zipEntryFileOutputStream = null;
                BufferedOutputStream zipEntryBufferedOutputStream = null;
                try {
                    zipEntryFileOutputStream = new FileOutputStream(zipEntryFile);
                    zipEntryBufferedOutputStream = new BufferedOutputStream(zipEntryFileOutputStream);
                    byte[] bytesIn = new byte[4096];
                    int read = 0;
                    while ((read = zipInputStream.read(bytesIn)) != -1) {
                        zipEntryBufferedOutputStream.write(bytesIn, 0, read);
                    }
                } catch (IOException e) {
                    throw new ArchiveUtilitiesException("Writing archive entry \"" + zipEntryFile.getAbsolutePath() 
                            + "\" failed", e);
                } finally {
                    if (zipEntryBufferedOutputStream != null) {                        
                        try {
                            zipEntryBufferedOutputStream.close();
                        } catch (IOException e) {
                            throw new ArchiveUtilitiesException("Closing buffered output stream for archive entry \"" 
                                    + zipEntryFile.getAbsolutePath() + "\" failed", e);
                        }
                    }
                    if (zipEntryFileOutputStream != null) {
                        try {
                            zipEntryFileOutputStream.close();
                        } catch (IOException e) {
                            throw new ArchiveUtilitiesException("Closing file output stream for archive entry \""
                                    + zipEntryFile.getAbsolutePath() + "\" failed", e);
                        }
                    }
                }
            }
        }
    }
    
}
