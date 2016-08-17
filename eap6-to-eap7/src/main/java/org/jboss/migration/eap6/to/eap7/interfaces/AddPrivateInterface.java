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

package org.jboss.migration.eap6.to.eap7.interfaces;

import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ValueExpression;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.config.WildFly10ConfigurationManagement;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Adds private interface to config.
 * @author emmartins
 */
public class AddPrivateInterface implements ServerMigrationTask {

    public static final String SERVER_MIGRATION_TASK_NAME_NAME = "add-private-interface";
    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder().setName(SERVER_MIGRATION_TASK_NAME_NAME).build();

    public interface EnvironmentProperties {
        /**
         * the prefix for the name of the task related env properties
         */
        String PROPERTIES_PREFIX = EAP6ToEAP7ConfigFileInterfacesMigration.EnvironmentProperties.PROPERTIES_PREFIX + SERVER_MIGRATION_TASK_NAME_NAME + ".";
        /**
         * Boolean property which if true skips the task execution
         */
        String SKIP = PROPERTIES_PREFIX + "skip";
    }

    private static final String INTERFACE_NAME = "private";

    private final WildFly10ConfigurationManagement configuration;


    public AddPrivateInterface(final WildFly10ConfigurationManagement configuration) {
        this.configuration = configuration;
    }

    @Override
    public ServerMigrationTaskName getName() {
        return SERVER_MIGRATION_TASK_NAME;
    }

    @Override
    public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
        if (context.getServerMigrationContext().getMigrationEnvironment().getPropertyAsBoolean(EnvironmentProperties.SKIP, Boolean.FALSE)) {
            context.getLogger().debugf("Task skipped by environment.");
            return ServerMigrationTaskResult.SKIPPED;
        }
        final boolean configurationStarted = configuration.isStarted();
        if (!configurationStarted) {
            configuration.start();
        }
        try {
            final ModelNode getInterfacesOp = Util.createEmptyOperation(READ_CHILDREN_NAMES_OPERATION, null);
            getInterfacesOp.get(CHILD_TYPE).set(INTERFACE);
            boolean found = false;
            for (ModelNode modelNode : configuration.executeManagementOperation(getInterfacesOp).get(RESULT).asList()) {
                if (modelNode.asString().equals(INTERFACE_NAME)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                context.getLogger().debugf("Skipping task to add private interface, the configuration already has it.");
                return ServerMigrationTaskResult.SKIPPED;
            }
            final ModelNode addInterfaceOp = Util.createAddOperation(pathAddress(pathElement(INTERFACE, INTERFACE_NAME)));
            addInterfaceOp.get(INET_ADDRESS).set(new ValueExpression("${jboss.bind.address.private:127.0.0.1}"));
            configuration.executeManagementOperation(addInterfaceOp);
            context.getLogger().infof("Interface %s added.", INTERFACE_NAME);
            return ServerMigrationTaskResult.SUCCESS;
        } finally {
            if (!configurationStarted) {
                configuration.stop();
            }
        }
    }
}