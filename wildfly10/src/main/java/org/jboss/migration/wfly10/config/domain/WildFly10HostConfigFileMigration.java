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

package org.jboss.migration.wfly10.config.domain;

import org.jboss.migration.core.Server;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.wfly10.WildFly10Server;
import org.jboss.migration.wfly10.config.WildFly10ConfigFileMigration;
import org.jboss.migration.wfly10.config.domain.management.EmbeddedWildFly10HostController;
import org.jboss.migration.wfly10.config.domain.management.WildFly10HostController;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Abstract implementation of a domain config file migration.
 * @author emmartins
 */
public abstract class WildFly10HostConfigFileMigration<S extends Server> extends WildFly10ConfigFileMigration<S, WildFly10HostController> {

    public WildFly10HostConfigFileMigration() {
        super("host");
    }

    @Override
    protected WildFly10HostController startConfiguration(Path targetConfigFilePath, WildFly10Server target, ServerMigrationTaskContext context) throws IOException {
        context.getServerMigrationContext().getConsoleWrapper().printf("%n%n");
        final String config = targetConfigFilePath.getFileName().toString();
        context.getLogger().infof("Starting host configuration %s", config);
        final WildFly10HostController host = new EmbeddedWildFly10HostController(null, config, target);
        host.start();
        return host;
    }
}
