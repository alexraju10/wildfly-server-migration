/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.migration.wfly10.config;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.wfly10.WildFly10Server;
import org.jboss.migration.wfly10.config.subsystem.WildFly10ExtensionManagement;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * @author emmartins
 */
public interface WildFly10ConfigurationManagement {
    void start();
    void stop();
    boolean isStarted();
    ModelNode executeManagementOperation(ModelNode operation) throws IOException;
    WildFly10Server getServer();
    List<ModelNode> getSecurityRealms() throws IOException;
    WildFly10ExtensionManagement getExtensionManagement();
    Path resolvePath(String path)  throws IOException;
    ModelControllerClient getModelControllerClient();
}