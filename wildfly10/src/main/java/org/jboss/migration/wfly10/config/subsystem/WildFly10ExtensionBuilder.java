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

import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public class WildFly10ExtensionBuilder {

    private final List<WildFly10SubsystemBuilder> subsystems = new ArrayList<>();

    private String name;

    public WildFly10ExtensionBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public WildFly10ExtensionBuilder addSubsystem(WildFly10SubsystemBuilder subsystem) {
        subsystems.add(subsystem);
        return this;
    }

    public WildFly10ExtensionBuilder addSupportedSubsystem(String subsystemName) {
        return addSubsystem(subsystemName, "supported-subsystem", null);
    }

    public WildFly10ExtensionBuilder addUpdatedSubsystem(String subsystemName, WildFly10SubsystemMigrationTaskFactory... tasks) {
        return addSubsystem(subsystemName, "update-subsystem", tasks);
    }

    public WildFly10ExtensionBuilder addNewSubsystem(String subsystemName) {
        return addNewSubsystem(subsystemName, AddSubsystem.INSTANCE);
    }

    public WildFly10ExtensionBuilder addNewSubsystem(String subsystemName, AddSubsystem addSubsystem) {
        return addSubsystem(subsystemName, "add-subsystem", AddExtension.INSTANCE, addSubsystem);
    }

    public WildFly10ExtensionBuilder addSubsystem(String subsystemName, String taskName, WildFly10SubsystemMigrationTaskFactory... tasks) {
        final WildFly10SubsystemBuilder subsystemBuilder = new WildFly10SubsystemBuilder()
                .setName(subsystemName)
                .setTaskName(taskName);
        if (tasks != null) {
            for (WildFly10SubsystemMigrationTaskFactory task : tasks) {
                subsystemBuilder.addTask(task);
            }
        }
        return addSubsystem(subsystemBuilder);
    }

    public WildFly10Extension build() {
        final WildFly10Extension extension = new WildFly10Extension(name);
        for (WildFly10SubsystemBuilder subsystem : subsystems) {
            extension.subsystems.add(subsystem.setExtension(extension).build());
        }
        return extension;
    }
}
