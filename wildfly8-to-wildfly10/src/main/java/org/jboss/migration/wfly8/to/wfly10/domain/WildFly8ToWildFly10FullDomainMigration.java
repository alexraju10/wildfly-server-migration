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

package org.jboss.migration.wfly8.to.wfly10.domain;

import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.wfly10.WildFly10Server;
import org.jboss.migration.wfly10.config.domain.WildFly10DomainConfigFilesMigration;
import org.jboss.migration.wfly10.config.domain.WildFly10DomainMigration;
import org.jboss.migration.wfly8.WildFly8Server;

import java.util.ArrayList;
import java.util.List;

/**
 * Migration of a domain, from WildFly 8 to WildFly 10
 * @author emmartins
 */
public class WildFly8ToWildFly10FullDomainMigration extends WildFly10DomainMigration<WildFly8Server> {

    private final WildFly10DomainConfigFilesMigration<WildFly8Server> configFilesMigration;

    public WildFly8ToWildFly10FullDomainMigration(WildFly10DomainConfigFilesMigration<WildFly8Server> configFilesMigration) {
        this.configFilesMigration = configFilesMigration;
    }

    @Override
    protected List<ServerMigrationTask> getSubtasks(WildFly8Server source, WildFly10Server target, ServerMigrationTaskContext context) {
        List<ServerMigrationTask> subtasks = new ArrayList<>();
        subtasks.add(configFilesMigration.getServerMigrationTask(source.getDomainDomainConfigs(), target.getDomainConfigurationDir(), target));
        return subtasks;
    }
}