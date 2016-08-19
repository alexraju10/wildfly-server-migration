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

package org.jboss.migration.wfly10.config.domain.servergroup;

import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Migration of a domain config's server group.
 *  @author emmartins
 */
public class WildFly10DomainConfigFileServerGroupMigration {

    public static final String SERVER_MIGRATION_TASK_NAME_NAME = "server-group";
    public static final String SERVER_MIGRATION_TASK_NAME_ATTR_NAME = "name";

    private final List<SubtaskFactory> subtaskFactories;

    private WildFly10DomainConfigFileServerGroupMigration(List<SubtaskFactory> subtaskFactories) {
        this.subtaskFactories = subtaskFactories;
    }

    public ServerMigrationTask getServerMigrationTask(final String serverGroup, final WildFly10ServerGroupsManagement management) {
        final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder()
                .setName(SERVER_MIGRATION_TASK_NAME_NAME)
                .addAttribute(SERVER_MIGRATION_TASK_NAME_ATTR_NAME, serverGroup)
                .build();

        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return SERVER_MIGRATION_TASK_NAME;
            }

            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                for (SubtaskFactory subtaskFactory : subtaskFactories) {
                    final ServerMigrationTask subtask = subtaskFactory.getSubtask(serverGroup, management);
                    if (subtask != null) {
                        context.execute(subtask);
                    }
                }
                return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
            }
        };
    }

    public interface SubtaskFactory {
        ServerMigrationTask getSubtask(String serverGroup, WildFly10ServerGroupsManagement management);
    }

    public static class Builder {

        private final List<SubtaskFactory> subtaskFactories = new ArrayList<>();

        public Builder addSubtaskFactory(SubtaskFactory subtaskFactory) {
            subtaskFactories.add(subtaskFactory);
            return this;
        }

        public WildFly10DomainConfigFileServerGroupMigration build() {
            return new WildFly10DomainConfigFileServerGroupMigration(Collections.unmodifiableList(subtaskFactories));
        }
    }
}