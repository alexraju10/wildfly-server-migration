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
import org.jboss.migration.wfly10.config.domain.management.EmbeddedWildFly10Host;
import org.jboss.migration.wfly10.config.domain.management.WildFly10Host;
import org.jboss.migration.wfly10.config.domain.management.WildFly10HostController;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * @author emmartins
 */
public abstract class WildFly10HostConfigFileHostsMigration<S extends Server> {

    public ServerMigrationTask getServerMigrationTask(final ServerPath<S> sourceConfig, final Path targetConfigFilePath, final WildFly10HostController configurationManagement) {
        final String config = targetConfigFilePath.getFileName().toString();
        final ServerMigrationTaskName hostsTaskName = new ServerMigrationTaskName.Builder().setName("hosts").build();
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return hostsTaskName;
            }
            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                final Set<String> hosts = configurationManagement.getHosts();
                context.getServerMigrationContext().getConsoleWrapper().printf("%n%n");
                if (hosts == null || hosts.isEmpty()) {
                    context.getLogger().infof("No hosts found to migrate, in host configuration %s", config);
                    return ServerMigrationTaskResult.SKIPPED;
                }
                context.getLogger().infof("Hosts found in host configuration %s: %s", config, hosts);
                for (final String host : hosts) {
                    final ServerMigrationTaskName hostTaskName = new ServerMigrationTaskName.Builder().setName("host").addAttribute("name", host).build();
                    final ServerMigrationTask profileTask = new ServerMigrationTask() {
                        @Override
                        public ServerMigrationTaskName getName() {
                            return hostTaskName;
                        }
                        @Override
                        public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                            context.getServerMigrationContext().getConsoleWrapper().printf("%n%n");
                            context.getLogger().infof("Migrating host %s in host configuration %s ...", host, config);
                            final WildFly10Host hostConfiguration = new EmbeddedWildFly10Host(configurationManagement, host);
                            hostConfiguration.start();
                            try {
                                for (ServerMigrationTask subtask : getSubtasks(sourceConfig, targetConfigFilePath, hostConfiguration)) {
                                    context.execute(subtask);
                                }
                            } finally {
                                hostConfiguration.stop();
                            }
                            return ServerMigrationTaskResult.SUCCESS;
                        }
                    };
                    context.execute(profileTask);
                }
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
    }

    /**
     * Retrieves the subtasks to process the host config file's management resources.
     * @param targetConfigFilePath
     * @param configurationManagement
     * @return
     */
    protected abstract List<ServerMigrationTask> getSubtasks(ServerPath<S> sourceConfig, Path targetConfigFilePath, WildFly10Host configurationManagement);
}
