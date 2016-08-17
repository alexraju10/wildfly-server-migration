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

package org.jboss.migration.eap6.to.eap7.subsystem;

import org.jboss.migration.eap.EAP6Server;
import org.jboss.migration.wfly10.config.WildFly10ConfigFileSubsystemsMigration;
import org.jboss.migration.wfly10.config.subsystem.WildFly10Extension;
import org.jboss.migration.wfly10.config.subsystem.WildFly10ExtensionBuilder;
import org.jboss.migration.wfly10.config.subsystem.WildFly10ExtensionNames;
import org.jboss.migration.wfly10.config.subsystem.WildFly10SubsystemNames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emmartins
 */
public class EAP6ToEAP7HostSubsystemsMigration extends WildFly10ConfigFileSubsystemsMigration<EAP6Server> {

    public static final List<WildFly10Extension> SUPPORTED_EXTENSIONS = initSupportedExtensions();

    private static List<WildFly10Extension> initSupportedExtensions() {

        List<WildFly10Extension> supportedExtensions = new ArrayList<>();

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JMX)
                .addNewSubsystem(WildFly10SubsystemNames.JMX, AddJmxSubsystem.INSTANCE)
                .build()
        );

        return Collections.unmodifiableList(supportedExtensions);
    }

    public static final EAP6ToEAP7HostSubsystemsMigration INSTANCE = new EAP6ToEAP7HostSubsystemsMigration();

    private EAP6ToEAP7HostSubsystemsMigration() {
        super(SUPPORTED_EXTENSIONS);
    }
}
