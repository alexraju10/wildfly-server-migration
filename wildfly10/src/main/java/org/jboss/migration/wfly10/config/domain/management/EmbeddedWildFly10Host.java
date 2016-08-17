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

package org.jboss.migration.wfly10.config.domain.management;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.migration.wfly10.config.AbstractWildFly10ConfigurationManagement;
import org.jboss.migration.wfly10.config.securityrealms.AbstractWildFly10SecurityRealmsManagement;
import org.jboss.migration.wfly10.config.securityrealms.WildFly10SecurityRealmsManagement;
import org.jboss.migration.wfly10.config.subsystem.AbstractWildFly10ExtensionManagement;
import org.jboss.migration.wfly10.config.subsystem.AbstractWildFly10SubsystemManagement;
import org.jboss.migration.wfly10.config.subsystem.WildFly10ExtensionManagement;
import org.jboss.migration.wfly10.config.subsystem.WildFly10SubsystemManagement;

import java.io.IOException;
import java.util.Set;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author emmartins
 */
public class EmbeddedWildFly10Host extends AbstractWildFly10ConfigurationManagement implements WildFly10Host {

    private final String host;
    private final WildFly10HostController hostController;
    private final WildFly10SubsystemManagement subsystemManagement;
    private final WildFly10SecurityRealmsManagement securityRealmsManagement;
    private final WildFly10ExtensionManagement extensionManagement;

    public EmbeddedWildFly10Host(WildFly10HostController hostController, String host) {
        super(hostController.getServer());
        this.hostController = hostController;
        this.host = host;
        final PathAddress hostPathAddress = pathAddress(pathElement(HOST, host));
        this.extensionManagement = new AbstractWildFly10ExtensionManagement(this) {
            @Override
            public Set<String> getSubsystems() throws IOException {
                return getSubsystems();
            }
            @Override
            protected PathAddress getParentPathAddress() {
                return hostPathAddress;
            }
        };
        this.subsystemManagement = new AbstractWildFly10SubsystemManagement(this) {
            @Override
            protected PathAddress getParentPathAddress() {
                return hostPathAddress;
            }
        };
        this.securityRealmsManagement = new AbstractWildFly10SecurityRealmsManagement(this) {
            private final PathAddress parentPathAddress = hostPathAddress.append(pathElement(CORE_SERVICE, MANAGEMENT));
            @Override
            protected PathAddress getParentPathAddress() {
                return parentPathAddress;
            }
        };
    }

    @Override
    protected ModelControllerClient startConfiguration() {
        return hostController.getModelControllerClient();
    }

    @Override
    protected void stopConfiguration() {
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
    public WildFly10ExtensionManagement getExtensionManagement() {
        return extensionManagement;
    }
}
