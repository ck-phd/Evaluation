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
     * Runs an {@link Evaluation} for initial testing.
     *  
     * @param args not used
     */
    public static void main(String[] args) {
        File repositoryArchiveFile = new File("./data/test/repository.zip");
        File commitSequenceDirectory = new File("./data/test/commit-sequence_1");
        String hookActions = "git diff --cached -U100000 --no-renames > C:/Users/kroeher/Desktop/temp.txt";
        try {
            Evaluation e = new Evaluation(repositoryArchiveFile, commitSequenceDirectory);
            e.run(hookActions);
        } catch (SetupException | ExecutionException e) {
            e.printStackTrace();
        }
    }

}
