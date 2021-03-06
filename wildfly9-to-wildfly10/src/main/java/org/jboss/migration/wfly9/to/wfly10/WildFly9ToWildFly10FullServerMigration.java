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
package org.jboss.migration.wfly9.to.wfly10;

import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.WildFly10Server;
import org.jboss.migration.wfly10.full.WildFly10FullServerMigration;
import org.jboss.migration.wfly10.standalone.config.WildFly10StandaloneConfigFilesMigration;
import org.jboss.migration.wfly9.WildFly9Server;

/**
 * Server migration, from WildFly 9 to WildFly 10.
 * @author emmartins
 */
public class  WildFly9ToWildFly10FullServerMigration implements WildFly10FullServerMigration<WildFly9Server> {

    private final WildFly9ToWildFly10FullStandaloneMigration standaloneMigration;

    public WildFly9ToWildFly10FullServerMigration() {
        standaloneMigration = new WildFly9ToWildFly10FullStandaloneMigration(new WildFly10StandaloneConfigFilesMigration<WildFly9Server>(new WildFly9ToWildFly10FullStandaloneConfigFileMigration()));
    }

    @Override
    public ServerMigrationTaskResult run(final WildFly9Server source, final WildFly10Server target, ServerMigrationTaskContext context) {
        context.execute(standaloneMigration.getServerMigrationTask(source, target));
        return ServerMigrationTaskResult.SUCCESS;
    }

    @Override
    public Class<WildFly9Server> getSourceType() {
        return WildFly9Server.class;
    }
}
