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

/**
 * This is just a dummy class for checking the correct configuration of the project.
 * 
 * @author Christian Kroeher
 *
 */
public class Main {

    /**
     * Prints a message.
     *  
     * @param args not used
     */
    public static void main(String[] args) {
        try {
            Repository r = new Repository(new File("./data/test/repository.zip"), "ECHO Thats my hook!!!");
            r.delete();
        } catch (SetupException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
