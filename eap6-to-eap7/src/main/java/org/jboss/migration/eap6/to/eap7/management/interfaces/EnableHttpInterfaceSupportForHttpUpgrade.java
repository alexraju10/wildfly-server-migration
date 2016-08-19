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

package org.jboss.migration.eap6.to.eap7.management.interfaces;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.config.WildFly10ConfigurationManagement;
import org.jboss.migration.wfly10.config.management.interfaces.WildFly10ConfigFileManagementInterfacesMigration;
import org.jboss.migration.wfly10.config.management.interfaces.WildFly10ManagementInterfacesManagement;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Updates the management interface http-interface to support http upgrade.
 * @author emmartins
 */
public class EnableHttpInterfaceSupportForHttpUpgrade implements ServerMigrationTask {

    public static final String SERVER_MIGRATION_TASK_NAME_NAME = "enable-http-upgrade-support";
    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder().setName(SERVER_MIGRATION_TASK_NAME_NAME).build();

    public interface EnvironmentProperties {
        /**
         * the prefix for the name of the management-https socket binding related properties
         */
        String PROPERTIES_PREFIX = WildFly10ConfigFileManagementInterfacesMigration.EnvironmentProperties.PROPERTIES_PREFIX + SERVER_MIGRATION_TASK_NAME_NAME + ".";
        /**
         * Boolean property which if true skips the task execution
         */
        String SKIP = PROPERTIES_PREFIX + "skip";
    }

    private static final String INTERFACE_NAME = "http-interface";

    private final WildFly10ManagementInterfacesManagement managementInterfacesManagement;


    public EnableHttpInterfaceSupportForHttpUpgrade(final WildFly10ManagementInterfacesManagement managementInterfacesManagement) {
        this.managementInterfacesManagement = managementInterfacesManagement;
    }

    @Override
    public ServerMigrationTaskName getName() {
        return SERVER_MIGRATION_TASK_NAME;
    }

    @Override
    public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
        if (!context.getServerMigrationContext().getMigrationEnvironment().getPropertyAsBoolean(EnvironmentProperties.SKIP, Boolean.FALSE)) {
            final WildFly10ConfigurationManagement configuration = managementInterfacesManagement.getConfigurationManagement();
            final boolean configurationStarted = configuration.isStarted();
            if (!configurationStarted) {
                configuration.start();
            }
            try {
                if (managementInterfacesManagement.getManagementInterfaces().contains(INTERFACE_NAME)) {
                    // http interface found, turn on http upgrade
                    final PathAddress pathAddress = managementInterfacesManagement.getManagementInterfacePathAddress(INTERFACE_NAME);
                    //pathAddress(pathElement(CORE_SERVICE, MANAGEMENT), pathElement(MANAGEMENT_INTERFACE, "http-interface"));
                    final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                    writeAttrOp.get(NAME).set("http-upgrade-enabled");
                    writeAttrOp.get(VALUE).set(true);
                    configuration.executeManagementOperation(writeAttrOp);
                    context.getLogger().infof("Activated HTTP Management Interface's support for HTTP Upgrade.");
                    return ServerMigrationTaskResult.SUCCESS;
                }
            } finally {
                if (!configurationStarted) {
                    configuration.stop();
                }
            }
        }
        return ServerMigrationTaskResult.SKIPPED;
    }

    public static class SubtaskFactory implements WildFly10ConfigFileManagementInterfacesMigration.SubtaskFactory {
        @Override
        public ServerMigrationTask getSubtask(WildFly10ManagementInterfacesManagement managementInterfacesManagement) {
            return new EnableHttpInterfaceSupportForHttpUpgrade(managementInterfacesManagement);
        }
    }
}