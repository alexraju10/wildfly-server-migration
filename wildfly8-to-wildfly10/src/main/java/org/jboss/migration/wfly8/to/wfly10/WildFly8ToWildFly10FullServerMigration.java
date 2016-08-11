/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.migration.wfly8.to.wfly10;

import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.WildFly10Server;
import org.jboss.migration.wfly10.config.domain.WildFly10DomainConfigFilesMigration;
import org.jboss.migration.wfly10.config.standalone.WildFly10StandaloneConfigFilesMigration;
import org.jboss.migration.wfly10.dist.full.WildFly10FullServerMigration;
import org.jboss.migration.wfly8.WildFly8Server;
import org.jboss.migration.wfly8.to.wfly10.domain.WildFly8ToWildFly10FullDomainConfigFileMigration;
import org.jboss.migration.wfly8.to.wfly10.domain.WildFly8ToWildFly10FullDomainMigration;
import org.jboss.migration.wfly8.to.wfly10.standalone.WildFly8ToWildFly10FullStandaloneConfigFileMigration;
import org.jboss.migration.wfly8.to.wfly10.standalone.WildFly8ToWildFly10FullStandaloneMigration;

/**
 * Server migration, from WildFly 8 to WildFly 10.
 * @author emmartins
 */
public class WildFly8ToWildFly10FullServerMigration implements WildFly10FullServerMigration<WildFly8Server> {

    private final WildFly8ToWildFly10FullStandaloneMigration standaloneMigration;
    private final WildFly8ToWildFly10FullDomainMigration domainMigration;

    public WildFly8ToWildFly10FullServerMigration() {
        standaloneMigration = new WildFly8ToWildFly10FullStandaloneMigration(new WildFly10StandaloneConfigFilesMigration<WildFly8Server>(new WildFly8ToWildFly10FullStandaloneConfigFileMigration()));
        domainMigration = new WildFly8ToWildFly10FullDomainMigration(new WildFly10DomainConfigFilesMigration<>(new WildFly8ToWildFly10FullDomainConfigFileMigration()));
    }

    @Override
    public ServerMigrationTaskResult run(final WildFly8Server source, final WildFly10Server target, ServerMigrationTaskContext context) {
        context.execute(standaloneMigration.getServerMigrationTask(source, target));
        context.execute(domainMigration.getServerMigrationTask(source, target));
        return ServerMigrationTaskResult.SUCCESS;
    }

    @Override
    public Class<WildFly8Server> getSourceType() {
        return WildFly8Server.class;
    }
}
