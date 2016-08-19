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

package org.jboss.migration.eap6.to.eap7.domain.servergroup;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.config.WildFly10ConfigurationManagement;
import org.jboss.migration.wfly10.config.domain.servergroup.WildFly10DomainConfigFileServerGroupMigration;
import org.jboss.migration.wfly10.config.domain.servergroup.WildFly10ServerGroupsManagement;

import java.util.HashSet;
import java.util.Set;

import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.JVM;

/**
 * Update JVMConfig
 * @author emmartins
 */
public class UpdateJVMConfig implements ServerMigrationTask {

    public static final String SERVER_MIGRATION_TASK_NAME_NAME = "update-jvm";
    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder().setName(SERVER_MIGRATION_TASK_NAME_NAME).build();

    public interface EnvironmentProperties {
        /**
         * the prefix for the name of the task related env properties
         */
        String PROPERTIES_PREFIX = EAP6ToEAP7DomainConfigServerGroupsMigration.EnvironmentProperties.PROPERTIES_PREFIX + SERVER_MIGRATION_TASK_NAME_NAME + ".";
        /**
         * Boolean property which if true skips the task execution
         */
        String SKIP = PROPERTIES_PREFIX + "skip";
    }

    private final String serverGroup;
    private final WildFly10ServerGroupsManagement serverGroupsManagement;

    public UpdateJVMConfig(String serverGroup, WildFly10ServerGroupsManagement serverGroupsManagement) {
        this.serverGroup = serverGroup;
        this.serverGroupsManagement = serverGroupsManagement;
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
        final WildFly10ConfigurationManagement configuration = serverGroupsManagement.getConfigurationManagement();
        final boolean configurationStarted = configuration.isStarted();
        if (!configurationStarted) {
            configuration.start();
        }
        try {
            final PathAddress pathAddress = serverGroupsManagement.getServerGroupPathAddress(serverGroup);
            final ModelNode config = serverGroupsManagement.getServerGroup(serverGroup);
            if (config == null) {
                return ServerMigrationTaskResult.SKIPPED;
            }
            if (!config.hasDefined(JVM)) {
                return ServerMigrationTaskResult.SKIPPED;
            }
            final Set<String> updatedJVMs = new HashSet<>();
            for (String jvmName : config.get(JVM).keys()) {
                final PathAddress jvmPathAddress = pathAddress.append(pathElement(JVM, jvmName));
                if (config.hasDefined(JVM, jvmName, "permgen-size")) {
                    final ModelNode op = Util.getUndefineAttributeOperation(jvmPathAddress, "permgen-size");
                    configuration.executeManagementOperation(op);
                    updatedJVMs.add(jvmName);
                }
                if (config.hasDefined(JVM, jvmName, "max-permgen-size")) {
                    final ModelNode op = Util.getUndefineAttributeOperation(jvmPathAddress, "max-permgen-size");
                    configuration.executeManagementOperation(op);
                    updatedJVMs.add(jvmName);
                }
            }
            context.getLogger().infof("Server group %s updated JVM configs: %s", serverGroup, updatedJVMs);
            return ServerMigrationTaskResult.SUCCESS;
        } finally {
            if (!configurationStarted) {
                configuration.stop();
            }
        }
    }

    public static class SubtaskFactory implements WildFly10DomainConfigFileServerGroupMigration.SubtaskFactory {
        @Override
        public ServerMigrationTask getSubtask(String serverGroup, WildFly10ServerGroupsManagement serverGroupsManagement) {
            return new UpdateJVMConfig(serverGroup, serverGroupsManagement);
        }
    }
}