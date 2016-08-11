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

package org.jboss.migration.wfly10.config.standalone.management;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.logger.ServerMigrationLogger;
import org.jboss.migration.wfly10.WildFly10Server;
import org.jboss.migration.wfly10.config.AbstractWildFly10ConfigurationManagement;
import org.jboss.migration.wfly10.config.subsystem.AbstractWildFly10SubsystemManagement;
import org.jboss.migration.wfly10.config.subsystem.WildFly10SubsystemManagement;
import org.wildfly.core.embedded.EmbeddedServerFactory;
import org.wildfly.core.embedded.ServerStartException;
import org.wildfly.core.embedded.StandaloneServer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author emmartins
 */
public class EmbeddedWildFly10StandaloneServer extends AbstractWildFly10ConfigurationManagement implements WildFly10StandaloneServer {

    private final String config;
    private StandaloneServer standaloneServer;
    private final WildFly10SubsystemManagement subsystemManagement;

    public EmbeddedWildFly10StandaloneServer(String config, WildFly10Server server) {
        super(server);
        this.config = config;
        this.subsystemManagement = new AbstractWildFly10SubsystemManagement(this) {
            @Override
            protected PathAddress getParentPathAddress() {
                return null;
            }
        };
    }

    @Override
    protected ModelControllerClient startConfiguration() {
        final String[] cmds = {"--server-config="+config,"--admin-only"};
        standaloneServer = EmbeddedServerFactory.create(getServer().getBaseDir().toString(), null, null, cmds);
        try {
            standaloneServer.start();
        } catch (ServerStartException e) {
            throw new RuntimeException(e);
        }
        return standaloneServer.getModelControllerClient();
    }

    @Override
    protected void stopConfiguration() {
        standaloneServer.stop();
        standaloneServer = null;
    }

    @Override
    public List<ModelNode> getSecurityRealms() throws IOException {
        final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_RESOURCES_OPERATION, pathAddress(pathElement(CORE_SERVICE, MANAGEMENT)));
        op.get(CHILD_TYPE).set(SECURITY_REALM);
        op.get(RECURSIVE).set(true);
        final ModelNode opResult = executeManagementOperation(op);
        ServerMigrationLogger.ROOT_LOGGER.debugf("Get security realms Op result %s", opResult.toString());
        return opResult.get(RESULT).asList();
    }

    @Override
    public Path resolvePath(String pathName) throws IOException {
        final PathAddress address = pathAddress(pathElement(PATH, pathName));
        final ModelNode op = Util.createEmptyOperation(READ_RESOURCE_OPERATION, address);
        final ModelNode opResult = executeManagementOperation(op);
        ServerMigrationLogger.ROOT_LOGGER.debugf("Resolve path Op result %s", opResult.toString());
        String path = opResult.get(RESULT).get(PATH).asString();
        if (!opResult.get(RESULT).hasDefined(RELATIVE_TO)) {
            return Paths.get(path);
        } else {
            return resolvePath(opResult.get(RESULT).get(RELATIVE_TO).asString()).resolve(path);
        }
    }

    @Override
    public WildFly10SubsystemManagement getSubsystemManagement() {
        return subsystemManagement;
    }

    @Override
    protected Set<String> getSubsystems() throws IOException {
        return getSubsystemManagement().getSubsystems();
    }
}
