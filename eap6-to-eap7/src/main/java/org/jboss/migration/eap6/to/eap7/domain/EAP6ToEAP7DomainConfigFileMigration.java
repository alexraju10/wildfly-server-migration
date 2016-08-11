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

package org.jboss.migration.eap6.to.eap7.domain;

import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.eap.EAP6Server;
import org.jboss.migration.eap6.to.eap7.subsystem.EAP6ToEAP7ConfigFileSubsystemsMigration;
import org.jboss.migration.wfly10.WildFly10Server;
import org.jboss.migration.wfly10.config.domain.WildFly10DomainConfigFileMigration;
import org.jboss.migration.wfly10.config.domain.WildFly10DomainConfigFileProfilesMigration;
import org.jboss.migration.wfly10.config.domain.management.WildFly10HostController;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Standalone config file migration, from EAP 6 to EAP 7
 * @author emmartins
 */
public class EAP6ToEAP7DomainConfigFileMigration extends WildFly10DomainConfigFileMigration<EAP6Server> {

    private final WildFly10DomainConfigFileProfilesMigration profilesMigration = new WildFly10DomainConfigFileProfilesMigration();

    @Override
    protected List<ServerMigrationTask> getXMLConfigurationSubtasks(ServerPath<EAP6Server> sourceConfig, Path targetConfigFilePath, WildFly10Server target) {
        final List<ServerMigrationTask> tasks = new ArrayList<>();
        tasks.add(EAP6ToEAP7ConfigFileSubsystemsMigration.INSTANCE.getXmlConfigServerMigrationTask(sourceConfig, targetConfigFilePath, target));
        return tasks;
    }

    @Override
    protected List<ServerMigrationTask> getManagementResourcesSubtasks(ServerPath<EAP6Server> sourceConfig, Path targetConfigFilePath, WildFly10HostController configurationManagement) {
        final List<ServerMigrationTask> tasks = new ArrayList<>();
        tasks.add(profilesMigration.getServerMigrationTask(sourceConfig, targetConfigFilePath, configurationManagement, EAP6ToEAP7ConfigFileSubsystemsMigration.INSTANCE));
        return tasks;
    }
}