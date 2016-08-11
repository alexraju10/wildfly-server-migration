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

package org.jboss.migration.wfly10.config.domain;

import org.jboss.migration.core.Server;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.wfly10.config.WildFly10ConfigFileSubsystemsMigration;
import org.jboss.migration.wfly10.config.domain.management.WildFly10HostController;

import java.nio.file.Path;
import java.util.Set;

/**
 * @author emmartins
 */
public class WildFly10DomainConfigFileProfilesMigration<S extends Server> {

    public ServerMigrationTask getServerMigrationTask(final ServerPath<S> sourceConfig, final Path targetConfigFilePath, final WildFly10HostController configurationManagement, final WildFly10ConfigFileSubsystemsMigration subsystemsMigration) {
        final String config = targetConfigFilePath.getFileName().toString();
        final ServerMigrationTaskName profilesTaskName = new ServerMigrationTaskName.Builder().setName("profiles").build();
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return profilesTaskName;
            }
            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                final Set<String> profiles = configurationManagement.getProfiles();
                context.getServerMigrationContext().getConsoleWrapper().printf("%n%n");
                if (profiles == null || profiles.isEmpty()) {
                    context.getLogger().infof("No profiles found to migrate, in domain configuration %s", config);
                    return ServerMigrationTaskResult.SKIPPED;
                }
                context.getLogger().infof("Profiles found in domain configuration %s: %s", config, profiles);
                for (final String profile : profiles) {
                    final ServerMigrationTaskName profileTaskName = new ServerMigrationTaskName.Builder().setName("profile").addAttribute("name", profile).build();
                    final ServerMigrationTask profileTask = new ServerMigrationTask() {
                        @Override
                        public ServerMigrationTaskName getName() {
                            return profileTaskName;
                        }
                        @Override
                        public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                            context.getServerMigrationContext().getConsoleWrapper().printf("%n%n");
                            context.getLogger().infof("Migrating profile %s in domain configuration %s ...", profile, config);
                            context.execute(subsystemsMigration.getManagementResourcesServerMigrationTask(targetConfigFilePath, configurationManagement.getSubsystemManagement(profile)));
                            return ServerMigrationTaskResult.SUCCESS;
                        }
                    };
                    context.execute(profileTask);
                }
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
    }
}
