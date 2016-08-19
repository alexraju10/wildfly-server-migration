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

package org.jboss.migration.wfly10.config.domain.servergroup;

import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;

import java.util.Set;

/**
 * Migration of domain config's server groups.
 *  @author emmartins
 */
public class WildFly10DomainConfigFileServerGroupsMigration {

    public static final String SERVER_MIGRATION_TASK_NAME_NAME = "server-groups";
    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder().setName(SERVER_MIGRATION_TASK_NAME_NAME).build();

    private final WildFly10DomainConfigFileServerGroupMigration serverGroupMigration;

    public WildFly10DomainConfigFileServerGroupsMigration(WildFly10DomainConfigFileServerGroupMigration serverGroupMigration) {
        this.serverGroupMigration = serverGroupMigration;
    }

    public interface EnvironmentProperties {
        /**
         * the prefix for the name of all properties
         */
        String PROPERTIES_PREFIX = SERVER_MIGRATION_TASK_NAME_NAME + ".";
        /**
         * Boolean property which if true skips migration
         */
        String SKIP = PROPERTIES_PREFIX + "skip";
    }

    public ServerMigrationTask getServerMigrationTask(final WildFly10ServerGroupsManagement management) {
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return SERVER_MIGRATION_TASK_NAME;
            }

            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                if (!context.getServerMigrationContext().getMigrationEnvironment().getPropertyAsBoolean(EnvironmentProperties.SKIP, Boolean.FALSE)) {
                    context.getServerMigrationContext().getConsoleWrapper().printf("%n%n");
                    context.getLogger().infof("Migrating server groups...");
                    final Set<String> serverGroups = management.getServerGroups();
                    context.getServerMigrationContext().getConsoleWrapper().printf("%n%n");
                    if (serverGroups == null || serverGroups.isEmpty()) {
                        context.getLogger().infof("No server groups found to migrate in domain configuration.");
                        return ServerMigrationTaskResult.SKIPPED;
                    }
                    context.getLogger().infof("Server groups found in domain configuration: %s", serverGroups);
                    for (final String serverGroup : serverGroups) {
                        final ServerMigrationTask serverGroupMigrationTask = serverGroupMigration.getServerMigrationTask(serverGroup, management);
                        if (serverGroupMigrationTask != null) {
                            context.execute(serverGroupMigrationTask);
                        }

                    }
                    context.getLogger().info("Server groups migration done.");
                }
                return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
            }
        };
    }
}