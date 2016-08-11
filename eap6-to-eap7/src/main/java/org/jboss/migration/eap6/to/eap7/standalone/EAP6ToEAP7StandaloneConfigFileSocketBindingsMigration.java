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
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.eap6.to.eap7.UpdateManagementHttpsSocketBinding;
import org.jboss.migration.wfly10.config.standalone.management.WildFly10StandaloneServer;

/**
 * Migration of EAP 6 socket bindings config.
 * @author emmartins
 */
public class EAP6ToEAP7StandaloneConfigFileSocketBindingsMigration {

    public static final String SERVER_MIGRATION_TASK_NAME_NAME = "socket-bindings";
    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder().setName(SERVER_MIGRATION_TASK_NAME_NAME).build();

    public interface EnvironmentProperties {
        /**
         * the prefix for the name of socket bindings related properties
         */
        String PROPERTIES_PREFIX = SERVER_MIGRATION_TASK_NAME_NAME + ".";
        /**
         * Boolean property which if true skips migration of socket bindings
         */
        String SKIP = PROPERTIES_PREFIX + "skip";
    }

    public ServerMigrationTask getServerMigrationTask(final WildFly10StandaloneServer target) {
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return SERVER_MIGRATION_TASK_NAME;
            }

            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                if (!context.getServerMigrationContext().getMigrationEnvironment().getPropertyAsBoolean(EnvironmentProperties.SKIP, Boolean.FALSE)) {
                    context.getServerMigrationContext().getConsoleWrapper().printf("%n%n");
                    context.getLogger().infof("Migrating socket bindings...");
                    context.execute(new UpdateManagementHttpsSocketBinding(target));
                    context.getLogger().info("Socket bindings migration done.");
                }
                return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
            }
        };
    }
}