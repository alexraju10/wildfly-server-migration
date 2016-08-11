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
package org.jboss.migration.eap6.to.eap7.standalone;

import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.eap.EAP6Server;
import org.jboss.migration.wfly10.WildFly10Server;
import org.jboss.migration.wfly10.config.standalone.WildFly10StandaloneConfigFilesMigration;
import org.jboss.migration.wfly10.config.standalone.WildFly10StandaloneServerMigration;

import java.util.ArrayList;
import java.util.List;

/**
 * Migration of a standalone server, from EAP 6 to EAP 7.
 * @author emmartins
 */
public class EAP6ToEAP7StandaloneMigration extends WildFly10StandaloneServerMigration<EAP6Server> {

    private final WildFly10StandaloneConfigFilesMigration<EAP6Server> configFilesMigration;

    public EAP6ToEAP7StandaloneMigration(WildFly10StandaloneConfigFilesMigration<EAP6Server> configFilesMigration) {
        this.configFilesMigration = configFilesMigration;
    }

    @Override
    protected List<ServerMigrationTask> getSubtasks(EAP6Server source, WildFly10Server target, ServerMigrationTaskContext context) {
        List<ServerMigrationTask> subtasks = new ArrayList<>();
        subtasks.add(configFilesMigration.getServerMigrationTask(source.getStandaloneConfigs(), target.getStandaloneConfigurationDir(), target));
        return subtasks;
    }
}