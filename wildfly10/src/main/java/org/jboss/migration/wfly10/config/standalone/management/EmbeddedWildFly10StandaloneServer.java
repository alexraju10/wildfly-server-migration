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
import org.jboss.migration.wfly10.WildFly10Server;
import org.jboss.migration.wfly10.config.AbstractWildFly10ConfigurationManagement;
import org.jboss.migration.wfly10.config.management.interfaces.AbstractWildFly10ManagementInterfacesManagement;
import org.jboss.migration.wfly10.config.management.interfaces.WildFly10ManagementInterfacesManagement;
import org.jboss.migration.wfly10.config.securityrealms.AbstractWildFly10SecurityRealmsManagement;
import org.jboss.migration.wfly10.config.securityrealms.WildFly10SecurityRealmsManagement;
import org.jboss.migration.wfly10.config.subsystem.AbstractWildFly10SubsystemManagement;
import org.jboss.migration.wfly10.config.subsystem.WildFly10SubsystemManagement;
import org.wildfly.core.embedded.EmbeddedServerFactory;
import org.wildfly.core.embedded.ServerStartException;
import org.wildfly.core.embedded.StandaloneServer;

import java.io.IOException;
import java.util.Set;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CORE_SERVICE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MANAGEMENT;

/**
 * @author emmartins
 */
public class EmbeddedWildFly10StandaloneServer extends AbstractWildFly10ConfigurationManagement implements WildFly10StandaloneServer {

    private final String config;
    private StandaloneServer standaloneServer;
    private final WildFly10SubsystemManagement subsystemManagement;
    private final WildFly10SecurityRealmsManagement securityRealmsManagement;
    private final WildFly10ManagementInterfacesManagement managementInterfacesManagement;

    public EmbeddedWildFly10StandaloneServer(String config, WildFly10Server server) {
        super(server);
        this.config = config;
        this.subsystemManagement = new AbstractWildFly10SubsystemManagement(this) {
            @Override
            protected PathAddress getParentPathAddress() {
                return null;
            }
        };
        final PathAddress managementCoreServicePathAddress = pathAddress(pathElement(CORE_SERVICE, MANAGEMENT));
        this.securityRealmsManagement = new AbstractWildFly10SecurityRealmsManagement(this) {
            @Override
            protected PathAddress getParentPathAddress() {
                return managementCoreServicePathAddress;
            }
        };
        this.managementInterfacesManagement = new AbstractWildFly10ManagementInterfacesManagement(this) {
            @Override
            protected PathAddress getParentPathAddress() {
                return managementCoreServicePathAddress;
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
    public WildFly10SecurityRealmsManagement getSecurityRealmsManagement() {
        return securityRealmsManagement;
    }

    @Override
    public WildFly10SubsystemManagement getSubsystemManagement() {
        return subsystemManagement;
    }

    @Override
    protected Set<String> getSubsystems() throws IOException {
        return getSubsystemManagement().getSubsystems();
    }

    @Override
    public WildFly10ManagementInterfacesManagement getManagementInterfacesManagement() {
        return managementInterfacesManagement;
    }
}
