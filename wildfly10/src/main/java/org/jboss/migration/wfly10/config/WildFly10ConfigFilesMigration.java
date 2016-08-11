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

package org.jboss.migration.wfly10.config;

import org.jboss.migration.core.Server;
import org.jboss.migration.core.ServerMigrationContext;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.core.console.ConsoleWrapper;
import org.jboss.migration.core.console.UserConfirmation;
import org.jboss.migration.wfly10.WildFly10Server;

import java.nio.file.Path;
import java.util.Collection;

import static org.jboss.migration.core.logger.ServerMigrationLogger.ROOT_LOGGER;

/**
 * Migration of multiple standalone config files.
 * @author emmartins
 */
public class WildFly10ConfigFilesMigration<S extends Server> {

    private final ServerMigrationTaskName taskName;
    private final WildFly10ConfigFileMigration configFileMigration;

    public WildFly10ConfigFilesMigration(WildFly10ConfigFileMigration configFileMigration) {
        this.configFileMigration = configFileMigration;
        this.taskName = new ServerMigrationTaskName.Builder().setName(configFileMigration.getConfigType()+"-configurations").build();
    }

    public ServerMigrationTask getServerMigrationTask(final Collection<ServerPath<S>> sourceConfigs, final Path targetConfigDir, final WildFly10Server target) {
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return getServerMigrationTaskName();
            }
            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                WildFly10ConfigFilesMigration.this.run(sourceConfigs, targetConfigDir, target, context);
                return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
            }
        };
    }

    /**
     *
     * @return
     */
    protected ServerMigrationTaskName getServerMigrationTaskName() {
        return taskName;
    }

    protected void run(final Collection<ServerPath<S>> sourceConfigs, final Path targetConfigDir, final WildFly10Server target, final ServerMigrationTaskContext taskContext) throws Exception {
        taskContext.getLogger().infof("Retrieving source's %s configurations...", configFileMigration.getConfigType());
        for (ServerPath sourceConfig : sourceConfigs) {
            taskContext.getLogger().infof("%s", sourceConfig);
        }
        final ServerMigrationContext serverMigrationContext = taskContext.getServerMigrationContext();
        final ConsoleWrapper consoleWrapper = serverMigrationContext.getConsoleWrapper();
        consoleWrapper.printf("%n");
        if (serverMigrationContext.isInteractive()) {
            final UserConfirmation.ResultHandler resultHandler = new UserConfirmation.ResultHandler() {
                @Override
                public void onNo() throws Exception {
                    confirmAllConfigs(sourceConfigs, targetConfigDir, target, taskContext);
                }
                @Override
                public void onYes() throws Exception {
                    migrateAllConfigs(sourceConfigs, targetConfigDir, target, taskContext);
                }
                @Override
                public void onError() throws Exception {
                    // repeat
                    run(sourceConfigs, targetConfigDir, target, taskContext);
                }
            };
            new UserConfirmation(consoleWrapper, "Migrate all configurations?", ROOT_LOGGER.yesNo(), resultHandler).execute();
        } else {
            migrateAllConfigs(sourceConfigs, targetConfigDir, target, taskContext);
        }
    }

    protected void migrateAllConfigs(Collection<ServerPath<S>> sourceConfigs, final Path targetConfigDir, WildFly10Server target, final ServerMigrationTaskContext taskContext) throws Exception {
        for (ServerPath<S> sourceConfig : sourceConfigs) {
            taskContext.execute(configFileMigration.getServerMigrationTask(sourceConfig, targetConfigDir, target));
        }
    }

    protected void confirmAllConfigs(Collection<ServerPath<S>> sourceConfigs, final Path targetConfigDir, WildFly10Server target, final ServerMigrationTaskContext taskContext) throws Exception {
        for (ServerPath<S> sourceConfig : sourceConfigs) {
            confirmConfig(sourceConfig, targetConfigDir, target, taskContext);
        }
    }

    protected void confirmConfig(final ServerPath<S> sourceConfig, final Path targetConfigDir, final WildFly10Server target, final ServerMigrationTaskContext taskContext) throws Exception {
        final UserConfirmation.ResultHandler resultHandler = new UserConfirmation.ResultHandler() {
            @Override
            public void onNo() throws Exception {
            }
            @Override
            public void onYes() throws Exception {
                taskContext.execute(configFileMigration.getServerMigrationTask(sourceConfig, targetConfigDir, target));
            }
            @Override
            public void onError() throws Exception {
                // repeat
                confirmConfig(sourceConfig, targetConfigDir, target, taskContext);
            }
        };
        final ConsoleWrapper consoleWrapper = taskContext.getServerMigrationContext().getConsoleWrapper();
        consoleWrapper.printf("%n");
        new UserConfirmation(consoleWrapper, "Migrate configuration "+sourceConfig.getPath()+" ?", ROOT_LOGGER.yesNo(), resultHandler).execute();
    }
}
