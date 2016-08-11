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
package org.jboss.migration.wfly10.config.subsystem;

import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.env.TaskEnvironment;

import java.util.List;

/**
 * @author emmartins
 */
public class WildFly10Subsystem {

    private final String name;
    private final String namespaceWithoutVersion;
    private final WildFly10Extension extension;
    protected final List<WildFly10SubsystemMigrationTaskFactory> subsystemMigrationTasks;
    protected final ServerMigrationTaskName serverMigrationTaskName;

    public WildFly10Subsystem(String name, String namespaceWithoutVersion, String taskName, List<WildFly10SubsystemMigrationTaskFactory> subsystemMigrationTasks, WildFly10Extension extension) {
        this.name = name;
        this.namespaceWithoutVersion = namespaceWithoutVersion;
        this.extension = extension;
        this.subsystemMigrationTasks = subsystemMigrationTasks;
        this.serverMigrationTaskName = new ServerMigrationTaskName.Builder()
                .setName(taskName)
                .addAttribute("name", getName())
                .build();
    }

    public WildFly10Extension getExtension() {
        return extension;
    }

    public String getName() {
        return name;
    }

    public String getNamespaceWithoutVersion() {
        return namespaceWithoutVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final WildFly10Subsystem subsystem = (WildFly10Subsystem) o;
        return name.equals(subsystem.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    public ServerMigrationTask getServerMigrationTask(final WildFly10SubsystemManagement subsystemManagement) {
        if (subsystemMigrationTasks == null || subsystemMigrationTasks.isEmpty()) {
            return null;
        }
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return serverMigrationTaskName;
            }
            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                if (skipExecution(context)) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                final ModelNode subsystemConfig = subsystemManagement.getSubsystem(name);
                for (final WildFly10SubsystemMigrationTaskFactory subsystemMigrationTaskFactory : subsystemMigrationTasks) {
                    context.execute(subsystemMigrationTaskFactory.getServerMigrationTask(subsystemConfig, WildFly10Subsystem.this, subsystemManagement));
                }
                return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
            }
        };
    }

    protected boolean skipExecution(ServerMigrationTaskContext context) {
        return new TaskEnvironment(context.getServerMigrationContext().getMigrationEnvironment(), EnvironmentProperties.getSubsystemTaskPropertiesPrefix(name)).isSkippedByEnvironment();
    }

}
