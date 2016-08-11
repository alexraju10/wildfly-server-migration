/*
 * Copyright 2015 Red Hat, Inc.
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
package org.jboss.migration.wfly10.dist.full;

import org.jboss.migration.core.Server;
import org.jboss.migration.core.logger.ServerMigrationLogger;

import java.util.ServiceLoader;

/**
 * The WildFly 10 Full server migration provider.
 * @author emmartins
 */
public class WildFly10FullServerMigrations {

    private static final ServiceLoader<WildFly10FullServerMigration> SERVICE_LOADER = ServiceLoader.load(WildFly10FullServerMigration.class);

    private WildFly10FullServerMigrations() {
    }

    static WildFly10FullServerMigration getMigrationFrom(Server sourceServer) {
        ServerMigrationLogger.ROOT_LOGGER.debugf("Retrieving server migration for source %s", sourceServer.getClass());
        for (WildFly10FullServerMigration serverMigration : SERVICE_LOADER) {
            if (serverMigration.getSourceType().isInstance(sourceServer)) {
                ServerMigrationLogger.ROOT_LOGGER.debugf("Found server migration for source %s: %s", sourceServer.getClass(), serverMigration.getClass());
                return serverMigration;
            }
        }
        ServerMigrationLogger.ROOT_LOGGER.debugf("Failed to retrieve server migration for source %s", sourceServer.getClass());
        return null;
    }
}
