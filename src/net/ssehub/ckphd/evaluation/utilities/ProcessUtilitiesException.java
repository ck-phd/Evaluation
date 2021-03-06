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

/**
 * This class represents the specific type of exception, which will be thrown by the {@link ProcessUtilities}, if their
 * execution fails.
 * 
 * @author Christian Kroeher
 *
 */
public class ProcessUtilitiesException extends Exception {

    /**
     * The serial version UID of this class required by the extended {@link Exception}.
     */
    private static final long serialVersionUID = -6089522307555237312L;
    
    /**
     * Constructs a new {@link ProcessUtilitiesException} instance.
     * 
     * @param message the description of the problem causing this exception
     */
    public ProcessUtilitiesException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link ProcessUtilitiesException} instance.
     * 
     * @param message the description of the problem causing this exception
     * @param cause the exception causing this exception
     */
    public ProcessUtilitiesException(String message, Throwable cause) {
        super(message, cause);
    }

}
