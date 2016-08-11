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
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.WildFly10Server;

import java.util.List;

/**
 * Abstract implementation for the domain migration.
 * @author emmartins
 */
public abstract class WildFly10DomainMigration<S extends Server> {

    public interface EnvironmentProperties {
        /**
         * the prefix for the name of domain migration related properties
         */
        String PROPERTIES_PREFIX = "domain.";
        /**
         * Boolean property which if true skips migration of domain
         */
        String SKIP = PROPERTIES_PREFIX + "skip";
    }

    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder().setName("domain").build();

    /**
     *
     * @param source
     * @param target
     * @return
     */
    public ServerMigrationTask getServerMigrationTask(final S source, final WildFly10Server target) {
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return getServerMigrationTaskName();
            }
            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                return WildFly10DomainMigration.this.run(source, target, context);
            }
        };
    }

    /**
     *
     * @return
     */
    protected ServerMigrationTaskName getServerMigrationTaskName() {
        return SERVER_MIGRATION_TASK_NAME;
    }

    /**
     *
     * @param source
     * @param target
     * @param context
     * @return
     */
    protected ServerMigrationTaskResult run(final S source, WildFly10Server target, ServerMigrationTaskContext context) {
        if (!context.getServerMigrationContext().getMigrationEnvironment().getPropertyAsBoolean(EnvironmentProperties.SKIP, Boolean.FALSE)) {
            final List<ServerMigrationTask> subtasks = getSubtasks(source, target, context);
            if (subtasks != null) {
                for (ServerMigrationTask subtask : subtasks) {
                    context.execute(subtask);
                }
            }
        }
        return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
    }

    /**
     *
     * @param source
     * @param target
     * @param context
     * @return
     */
    protected abstract List<ServerMigrationTask> getSubtasks(S source, WildFly10Server target, ServerMigrationTaskContext context);
}
