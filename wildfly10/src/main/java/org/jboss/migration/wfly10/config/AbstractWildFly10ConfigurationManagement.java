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

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.wfly10.WildFly10Server;
import org.jboss.migration.wfly10.config.subsystem.AbstractWildFly10ExtensionManagement;
import org.jboss.migration.wfly10.config.subsystem.WildFly10ExtensionManagement;

import java.io.IOException;
import java.util.Set;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author emmartins
 */
public abstract class AbstractWildFly10ConfigurationManagement implements WildFly10ConfigurationManagement {

    private final WildFly10Server server;
    private final WildFly10ExtensionManagement extensionManagement;
    private ModelControllerClient modelControllerClient;

    protected AbstractWildFly10ConfigurationManagement(WildFly10Server server) {
        this.server = server;
        this.extensionManagement = new AbstractWildFly10ExtensionManagement(this) {
            @Override
            protected PathAddress getParentPathAddress() {
                return null;
            }
            @Override
            public Set<String> getSubsystems() throws IOException {
                return AbstractWildFly10ConfigurationManagement.this.getSubsystems();
            }
        };
    }

    @Override
    public void start() {
        if (isStarted()) {
            throw new IllegalStateException("server started");
        }
        modelControllerClient = startConfiguration();
    }

    protected abstract ModelControllerClient startConfiguration();

    @Override
    public void stop() {
        if (!isStarted()) {
            throw new IllegalStateException("server not started");
        }
        stopConfiguration();
        modelControllerClient = null;
    }

    protected abstract void stopConfiguration();

    @Override
    public boolean isStarted() {
        return modelControllerClient != null;
    }

    @Override
    public WildFly10Server getServer() {
        return server;
    }

    protected void processResult(ModelNode result) {
        if(!SUCCESS.equals(result.get(OUTCOME).asString())) {
            throw new RuntimeException(result.get(FAILURE_DESCRIPTION).asString());
        }
    }

    @Override
    public ModelNode executeManagementOperation(ModelNode operation) throws IOException {
        final ModelControllerClient modelControllerClient = getModelControllerClient();
        if (modelControllerClient == null) {
            throw new IllegalStateException("configuration not started");
        }
        final ModelNode result = modelControllerClient.execute(operation);
        //ServerMigrationLogger.ROOT_LOGGER.infof("Op result %s", result.toString());
        processResult(result);
        return  result;
    }

    @Override
    public ModelControllerClient getModelControllerClient() {
        return modelControllerClient;
    }

    @Override
    public WildFly10ExtensionManagement getExtensionManagement() {
        return extensionManagement;
    }

    protected abstract Set<String> getSubsystems() throws IOException;
}
