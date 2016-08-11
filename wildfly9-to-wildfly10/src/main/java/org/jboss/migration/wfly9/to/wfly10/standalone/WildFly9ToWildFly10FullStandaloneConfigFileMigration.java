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
package org.jboss.migration.wfly9.to.wfly10.standalone;

import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.wfly10.WildFly10Server;
import org.jboss.migration.wfly10.config.standalone.WildFly10StandaloneConfigFileDeploymentsMigration;
import org.jboss.migration.wfly10.config.standalone.WildFly10StandaloneConfigFileMigration;
import org.jboss.migration.wfly10.config.standalone.WildFly10StandaloneConfigFileSecurityRealmsMigration;
import org.jboss.migration.wfly10.config.standalone.management.WildFly10StandaloneServer;
import org.jboss.migration.wfly9.WildFly9Server;
import org.jboss.migration.wfly9.to.wfly10.subsystem.WildFly9ToWildFly10ConfigFileSubsystemsMigration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Standalone config file migration, from WildFly 9 to WildFly 10
 * @author emmartins
 */
public class WildFly9ToWildFly10FullStandaloneConfigFileMigration extends WildFly10StandaloneConfigFileMigration<WildFly9Server> {

    @Override
    protected List<ServerMigrationTask> getXMLConfigurationSubtasks(ServerPath<WildFly9Server> sourceConfig, Path targetConfigFilePath, WildFly10Server target) {
        final List<ServerMigrationTask> tasks = new ArrayList<>();
        tasks.add(WildFly9ToWildFly10ConfigFileSubsystemsMigration.INSTANCE.getXmlConfigServerMigrationTask(sourceConfig, targetConfigFilePath, target));
        return tasks;
    }

    @Override
    protected List<ServerMigrationTask> getManagementResourcesSubtasks(final ServerPath<WildFly9Server> sourceConfig, final Path targetConfigFilePath, final WildFly10StandaloneServer standaloneServer) {
        final List<ServerMigrationTask> tasks = new ArrayList<>();
        tasks.add(WildFly9ToWildFly10ConfigFileSubsystemsMigration.INSTANCE.getManagementResourcesServerMigrationTask(targetConfigFilePath, standaloneServer.getSubsystemManagement()));
        tasks.add(new WildFly10StandaloneConfigFileSecurityRealmsMigration().getServerMigrationTask(sourceConfig, standaloneServer));
        tasks.add(new WildFly10StandaloneConfigFileDeploymentsMigration().getServerMigrationTask(sourceConfig, standaloneServer));
        return tasks;
    }
}
