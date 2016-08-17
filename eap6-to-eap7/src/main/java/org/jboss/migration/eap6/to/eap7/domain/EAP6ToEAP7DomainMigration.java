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
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.eap.EAP6Server;
import org.jboss.migration.wfly10.WildFly10Server;
import org.jboss.migration.wfly10.config.domain.WildFly10DomainConfigFilesMigration;
import org.jboss.migration.wfly10.config.domain.WildFly10DomainMigration;
import org.jboss.migration.wfly10.config.domain.WildFly10HostConfigFilesMigration;

import java.util.ArrayList;
import java.util.List;

/**
 * Migration of a domain, from EAP 6 to EAP 7.
 * @author emmartins
 */
public class EAP6ToEAP7DomainMigration extends WildFly10DomainMigration<EAP6Server> {

    private final WildFly10DomainConfigFilesMigration<EAP6Server> domainConfigFilesMigration;
    private final WildFly10HostConfigFilesMigration<EAP6Server> hostConfigFilesMigration;

    public EAP6ToEAP7DomainMigration(WildFly10DomainConfigFilesMigration<EAP6Server> domainConfigFilesMigration, WildFly10HostConfigFilesMigration<EAP6Server> hostConfigFilesMigration) {
        this.domainConfigFilesMigration = domainConfigFilesMigration;
        this.hostConfigFilesMigration = hostConfigFilesMigration;
    }

    @Override
    protected List<ServerMigrationTask> getSubtasks(EAP6Server source, WildFly10Server target, ServerMigrationTaskContext context) {
        List<ServerMigrationTask> subtasks = new ArrayList<>();
        subtasks.add(domainConfigFilesMigration.getServerMigrationTask(source.getDomainDomainConfigs(), target.getDomainConfigurationDir(), target));
        subtasks.add(hostConfigFilesMigration.getServerMigrationTask(source.getDomainHostConfigs(), target.getDomainConfigurationDir(), target));
        return subtasks;
    }
}