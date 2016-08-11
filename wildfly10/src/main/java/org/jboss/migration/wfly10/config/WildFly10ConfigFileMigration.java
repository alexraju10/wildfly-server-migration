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
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.core.console.ConsoleWrapper;
import org.jboss.migration.wfly10.WildFly10Server;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Abstract implementation of a config file migration.
 * @author emmartins
 */
public abstract class WildFly10ConfigFileMigration<S extends Server, T extends WildFly10ConfigurationManagement> {

    public static final String MIGRATION_REPORT_TASK_ATTR_SOURCE = "source";

    private final String configType;

    protected WildFly10ConfigFileMigration(String configType) {
        this.configType = configType;
    }

    public String getConfigType() {
        return configType;
    }

    protected ServerMigrationTask getServerMigrationTask(final ServerPath<S> sourceConfig, final Path targetConfigDir, final WildFly10Server target) {
        final ServerMigrationTaskName taskName = new ServerMigrationTaskName.Builder().setName(getConfigType()+"-configuration").addAttribute(MIGRATION_REPORT_TASK_ATTR_SOURCE, sourceConfig.getPath().toString()).build();
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return taskName;
            }
            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                final ConsoleWrapper consoleWrapper = context.getServerMigrationContext().getConsoleWrapper();
                consoleWrapper.printf("%n%n");
                context.getLogger().infof("Migrating %s configuration %s", getConfigType(), sourceConfig.getPath());
                final Path targetConfigFilePath = getTargetConfigFilePath(sourceConfig, targetConfigDir, target, context);
                processXMLConfiguration(sourceConfig, targetConfigFilePath, target, context);
                processManagementResources(sourceConfig, targetConfigFilePath, target, context);
                consoleWrapper.printf("%n%n");
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
    }

    /**
     * Retrieves the path of the target config file.
     * @param sourceConfig
     * @param targetConfigDir
     * @param target
     * @param context
     * @return
     */
    protected Path getTargetConfigFilePath(ServerPath<S> sourceConfig, Path targetConfigDir, WildFly10Server target, ServerMigrationTaskContext context) {
        final Path targetConfigFilePath = targetConfigDir.resolve(sourceConfig.getPath().getFileName());
        context.getLogger().debugf("Target %s configuration file is %s", getConfigType(), targetConfigFilePath);
        return targetConfigFilePath;
    }

    /**
     * Process the XML configuration, which by default copies the source xml, and then executes subtasks to modify it
     * @param sourceConfig
     * @param targetConfigFilePath
     * @param target
     * @param context
     * @throws IOException
     */
    protected void processXMLConfiguration(ServerPath<S> sourceConfig, Path targetConfigFilePath, WildFly10Server target, ServerMigrationTaskContext context) throws IOException {
        // copy xml from source to target
        context.getServerMigrationContext().getMigrationFiles().copy(sourceConfig.getPath(), targetConfigFilePath);
        // execute xml config subtasks
        for (ServerMigrationTask subtask : getXMLConfigurationSubtasks(sourceConfig, targetConfigFilePath, target)) {
            context.execute(subtask);
        }
    }

    /**
     * Retrieves the subtasks to process the config file's xml configuration.
     * @param sourceConfig
     * @param targetConfigFilePath
     * @param target
     * @return
     */
    protected abstract List<ServerMigrationTask> getXMLConfigurationSubtasks(ServerPath<S> sourceConfig, Path targetConfigFilePath, WildFly10Server target);

    /**
     * Process the config file Management Resources, which by default starts an embedded server, and then executes subtasks which modify the magement resources
     * @param sourceConfig
     * @param targetConfigFilePath
     * @param target
     * @param context
     * @throws IOException
     */
    protected void processManagementResources(ServerPath<S> sourceConfig, Path targetConfigFilePath, WildFly10Server target, ServerMigrationTaskContext context) throws IOException {
        final T configurationManagement = startConfiguration(sourceConfig.getPath(), target, context);
        try {
            // execute management resources subtasks
            for (ServerMigrationTask subtask : getManagementResourcesSubtasks(sourceConfig, targetConfigFilePath, configurationManagement)) {
                context.execute(subtask);
            }
        } finally {
            configurationManagement.stop();
        }
    }

    /**
     * Retrieves the subtasks to process the config file's management resources.
     * @param targetConfigFilePath
     * @param configurationManagement
     * @return
     */
    protected abstract List<ServerMigrationTask> getManagementResourcesSubtasks(ServerPath<S> sourceConfig, Path targetConfigFilePath, T configurationManagement);

    /**
     * Creates and starts the config.
     * @param targetConfigFilePath
     * @param target
     * @param context
     * @return
     * @throws IOException
     */
    protected abstract T startConfiguration(Path targetConfigFilePath, WildFly10Server target, ServerMigrationTaskContext context) throws IOException;
}
