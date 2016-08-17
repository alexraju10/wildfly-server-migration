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
import org.jboss.migration.eap6.to.eap7.subsystem.AddJmxSubsystem;
import org.jboss.migration.eap6.to.eap7.subsystem.EAP6ToEAP7HostSubsystemsMigration;
import org.jboss.migration.wfly10.config.domain.WildFly10HostConfigFileHostsMigration;
import org.jboss.migration.wfly10.config.domain.management.WildFly10Host;
import org.jboss.migration.wfly10.config.securityrealms.WildFly10ConfigFileSecurityRealmsMigration;
import org.jboss.migration.wfly10.config.subsystem.WildFly10Extension;
import org.jboss.migration.wfly10.config.subsystem.WildFly10ExtensionBuilder;
import org.jboss.migration.wfly10.config.subsystem.WildFly10ExtensionNames;
import org.jboss.migration.wfly10.config.subsystem.WildFly10SubsystemNames;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public class EAP6ToEAP7HostConfigFileHostsMigration extends WildFly10HostConfigFileHostsMigration<EAP6Server> {

    private static final WildFly10Extension jmx = new WildFly10ExtensionBuilder()
            .setName(WildFly10ExtensionNames.JMX)
            .addNewSubsystem(WildFly10SubsystemNames.JMX, AddJmxSubsystem.INSTANCE)
            .build();

    @Override
    protected List<ServerMigrationTask> getSubtasks(ServerPath<EAP6Server> sourceConfig, Path targetConfigFilePath, WildFly10Host configuration) {
        final List<ServerMigrationTask> tasks = new ArrayList<>();
        tasks.add(EAP6ToEAP7HostSubsystemsMigration.INSTANCE.getManagementResourcesServerMigrationTask(targetConfigFilePath, configuration.getSubsystemManagement()));
        tasks.add(new WildFly10ConfigFileSecurityRealmsMigration().getServerMigrationTask(sourceConfig, configuration.getSecurityRealmsManagement()));
        return tasks;
    }
}
