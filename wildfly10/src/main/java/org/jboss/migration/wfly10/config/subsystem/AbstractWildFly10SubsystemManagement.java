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

package org.jboss.migration.wfly10.config.subsystem;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.logger.ServerMigrationLogger;
import org.jboss.migration.wfly10.config.WildFly10ConfigurationManagement;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author emmartins
 */
public abstract class AbstractWildFly10SubsystemManagement implements WildFly10SubsystemManagement {

    private final WildFly10ConfigurationManagement configurationManagement;

    public AbstractWildFly10SubsystemManagement(WildFly10ConfigurationManagement configurationManagement) {
        this.configurationManagement = configurationManagement;
    }

    protected abstract PathAddress getParentPathAddress();

    protected PathAddress getPathAddress(PathElement... elements) {
        final PathAddress parentAddress = getParentPathAddress();
        return parentAddress != null ? parentAddress.append(elements) : pathAddress(elements);
    }

    @Override
    public PathAddress getSubsystemPathAddress(String subsystem) {
        return getPathAddress(pathElement(SUBSYSTEM, subsystem));
    }

    @Override
    public ModelNode getSubsystem(String subsystem) throws IOException {
        if (!getSubsystems().contains(subsystem)) {
            return null;
        }
        final PathAddress address = getSubsystemPathAddress(subsystem);
        final ModelNode op = Util.createEmptyOperation(READ_RESOURCE_OPERATION, address);
        op.get(RECURSIVE).set(true);
        final ModelNode result = configurationManagement.executeManagementOperation(op);
        ServerMigrationLogger.ROOT_LOGGER.debugf("Op result %s", result.toString());
        return result.get(RESULT);
    }

    @Override
    public Set<String> getSubsystems() throws IOException {
        final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_NAMES_OPERATION, getParentPathAddress());
        op.get(CHILD_TYPE).set(SUBSYSTEM);
        final ModelNode opResult = configurationManagement.executeManagementOperation(op);
        ServerMigrationLogger.ROOT_LOGGER.debugf("Get subsystems Op result %s", opResult.toString());
        Set<String> result = new HashSet<>();
        for (ModelNode resultNode : opResult.get(RESULT).asList()) {
            result.add(resultNode.asString());
        }
        return result;
    }

    @Override
    public void removeSubsystem(String subsystem) throws IOException {
        final PathAddress address = getSubsystemPathAddress(subsystem);
        final ModelNode op = Util.createRemoveOperation(address);
        configurationManagement.executeManagementOperation(op);
    }

    @Override
    public WildFly10ConfigurationManagement getConfigurationManagement() {
        return configurationManagement;
    }
}