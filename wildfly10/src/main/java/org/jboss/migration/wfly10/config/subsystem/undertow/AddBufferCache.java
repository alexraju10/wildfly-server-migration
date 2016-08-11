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
package org.jboss.migration.wfly10.config.subsystem.undertow;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.subsystem.WildFly10Subsystem;
import org.jboss.migration.wfly10.config.subsystem.WildFly10SubsystemManagement;
import org.jboss.migration.wfly10.config.subsystem.WildFly10SubsystemMigrationTask;
import org.jboss.migration.wfly10.config.subsystem.WildFly10SubsystemMigrationTaskFactory;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;

/**
 * A task which adds Undertow's default buffer cache.
 * @author emmartins
 */
public class AddBufferCache implements WildFly10SubsystemMigrationTaskFactory {

    public static final AddBufferCache INSTANCE = new AddBufferCache();

    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder().setName("add-undertow-default-buffer-cache").build();

    private AddBufferCache() {
    }

    private static final String BUFFER_CACHE = "buffer-cache";
    private static final String BUFFER_CACHE_NAME = "default";

    @Override
    public ServerMigrationTask getServerMigrationTask(ModelNode config, WildFly10Subsystem subsystem, WildFly10SubsystemManagement subsystemManagement) {
        return new WildFly10SubsystemMigrationTask(config, subsystem, subsystemManagement) {
            @Override
            public ServerMigrationTaskName getName() {
                return SERVER_MIGRATION_TASK_NAME;
            }
            @Override
            protected ServerMigrationTaskResult run(ModelNode config, WildFly10Subsystem subsystem, WildFly10SubsystemManagement subsystemManagement, ServerMigrationTaskContext context, TaskEnvironment taskEnvironment) throws Exception {
                if (config == null) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                if (!config.hasDefined(BUFFER_CACHE, BUFFER_CACHE_NAME)) {
                    final PathAddress pathAddress = subsystemManagement.getSubsystemPathAddress(subsystem.getName()).append(PathElement.pathElement(BUFFER_CACHE, BUFFER_CACHE_NAME));
                    final ModelNode addOp = Util.createEmptyOperation(ADD, pathAddress);
                    subsystemManagement.getConfigurationManagement().executeManagementOperation(addOp);
                    context.getLogger().infof("Undertow's default buffer cache added.");
                    return ServerMigrationTaskResult.SUCCESS;
                } else {
                    return ServerMigrationTaskResult.SKIPPED;
                }
            }
        };
    }
}
