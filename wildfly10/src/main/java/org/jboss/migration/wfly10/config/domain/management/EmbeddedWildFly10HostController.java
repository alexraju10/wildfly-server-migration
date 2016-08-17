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
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.logger.ServerMigrationLogger;
import org.jboss.migration.wfly10.WildFly10Server;
import org.jboss.migration.wfly10.config.AbstractWildFly10ConfigurationManagement;
import org.jboss.migration.wfly10.config.subsystem.AbstractWildFly10SubsystemManagement;
import org.jboss.migration.wfly10.config.subsystem.WildFly10SubsystemManagement;
import org.wildfly.core.embedded.EmbeddedProcessFactory;
import org.wildfly.core.embedded.EmbeddedProcessStartException;
import org.wildfly.core.embedded.HostController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author emmartins
 */
public class EmbeddedWildFly10HostController extends AbstractWildFly10ConfigurationManagement implements WildFly10HostController {

    private final String domainConfig;
    private final String hostConfig;
    private HostController hostController;

    public EmbeddedWildFly10HostController(String domainConfig, String hostConfig, WildFly10Server server) {
        super(server);
        this.domainConfig = domainConfig;
        this.hostConfig = hostConfig;
    }

    @Override
    protected ModelControllerClient startConfiguration() {
        final List<String> cmds = new ArrayList<>();
        if (domainConfig != null) {
            cmds.add("--domain-config="+ domainConfig);
        }
        if (hostConfig != null) {
            cmds.add("--host-config="+ hostConfig);
        }
        cmds.add("--admin-only");
        hostController = EmbeddedProcessFactory.createHostController(getServer().getBaseDir().toString(), null, null, cmds.toArray(new String[cmds.size()]));
        try {
            hostController.start();
        } catch (EmbeddedProcessStartException e) {
            throw new RuntimeException(e);
        }
        return hostController.getModelControllerClient();
    }

    @Override
    protected void stopConfiguration() {
        hostController.stop();
        hostController = null;
    }

    @Override
    protected Set<String> getSubsystems() throws IOException {
        Set<String> subsystems = new HashSet<>();
        for (String profile : getProfiles()) {
            subsystems.addAll(getSubsystemManagement(profile).getSubsystems());
        }
        return subsystems;
    }

    @Override
    public WildFly10SubsystemManagement getSubsystemManagement(final String profile) {
        return new AbstractWildFly10SubsystemManagement(this) {
            @Override
            protected PathAddress getParentPathAddress() {
                return PathAddress.pathAddress(PathElement.pathElement(PROFILE, profile));
            }
        };
    }

    @Override
    public Set<String> getProfiles() throws IOException {
        final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_NAMES_OPERATION, null);
        op.get(CHILD_TYPE).set(PROFILE);
        final ModelNode opResult = executeManagementOperation(op);
        ServerMigrationLogger.ROOT_LOGGER.debugf("Get profiles Op result %s", opResult.toString());
        Set<String> result = new HashSet<>();
        for (ModelNode resultNode : opResult.get(RESULT).asList()) {
            result.add(resultNode.asString());
        }
        return result;
    }

    @Override
    public Set<String> getHosts() throws IOException {
        final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_NAMES_OPERATION, null);
        op.get(CHILD_TYPE).set(HOST);
        final ModelNode opResult = executeManagementOperation(op);
        ServerMigrationLogger.ROOT_LOGGER.debugf("Get hosts Op result %s", opResult.toString());
        Set<String> result = new HashSet<>();
        for (ModelNode resultNode : opResult.get(RESULT).asList()) {
            result.add(resultNode.asString());
        }
        return result;
    }
}
