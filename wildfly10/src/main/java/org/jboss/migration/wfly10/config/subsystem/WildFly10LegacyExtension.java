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

import org.jboss.migration.core.ServerMigrationTaskContext;

import java.io.IOException;
import java.util.Set;

/**
 * @author emmartins
 */
public class WildFly10LegacyExtension extends WildFly10Extension {

    public WildFly10LegacyExtension(String name) {
        super(name);
    }

    @Override
    public void migrate(WildFly10SubsystemManagement subsystemManagement, ServerMigrationTaskContext context) throws IOException {
        super.migrate(subsystemManagement, context);
        // remove extension if none of its subsystems are in config
        final WildFly10ExtensionManagement extensionManagement = subsystemManagement.getConfigurationManagement().getExtensionManagement();
        if (extensionManagement.getExtensions().contains(getName())) {
            final Set<String> subsystems = extensionManagement.getSubsystems();
            boolean remove = true;
            for (WildFly10Subsystem subsystem : getSubsystems()) {
                if (subsystems.contains(subsystem.getName())) {
                    remove = false;
                    break;
                }
            }
            if (remove) {
                extensionManagement.removeExtension(getName());
                context.getLogger().debugf("Extension %s removed.", getName());
            }
        }
    }
}
