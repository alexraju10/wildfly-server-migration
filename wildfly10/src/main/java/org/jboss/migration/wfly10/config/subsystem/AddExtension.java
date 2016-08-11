/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.migration.wfly10.config.subsystem;

import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.env.TaskEnvironment;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MODULE;

/**
 * A task which creates an extension if its missing from the server's config.
 * @author emmartins
 */
public class AddExtension implements WildFly10SubsystemMigrationTaskFactory {
    public static final AddExtension INSTANCE = new AddExtension();

    private AddExtension() {
    }

    @Override
    public ServerMigrationTask getServerMigrationTask(final ModelNode config, final WildFly10Subsystem subsystem, final WildFly10SubsystemManagement subsystemManagement) {
        return new WildFly10SubsystemMigrationTask(config, subsystem, subsystemManagement) {

            private final ServerMigrationTaskName taskName = new ServerMigrationTaskName.Builder().setName("add-extension").addAttribute("name", subsystem.getExtension().getName()).build();

            @Override
            public ServerMigrationTaskName getName() {
                return taskName;
            }

            @Override
            protected ServerMigrationTaskResult run(ModelNode config, WildFly10Subsystem subsystem, WildFly10SubsystemManagement subsystemManagement, ServerMigrationTaskContext context, TaskEnvironment taskEnvironment) throws Exception {
                final String extensionName = subsystem.getExtension().getName();
                final WildFly10ExtensionManagement extensionManagement = subsystemManagement.getConfigurationManagement().getExtensionManagement();
                if (!extensionManagement.getExtensions().contains(extensionName)) {
                    context.getLogger().debugf("Adding Extension %s...", extensionName);
                    final ModelNode op = Util.createAddOperation(extensionManagement.getExtensionPathAddress(extensionName));
                    op.get(MODULE).set(extensionName);
                    subsystemManagement.getConfigurationManagement().executeManagementOperation(op);
                    context.getLogger().infof("Extension %s added.",extensionName);
                    return ServerMigrationTaskResult.SUCCESS;
                } else {
                    context.getLogger().infof("Skipped adding extension %s, already exists in config.", extensionName);
                    return ServerMigrationTaskResult.SKIPPED;
                }
            }
        };
    }
}
