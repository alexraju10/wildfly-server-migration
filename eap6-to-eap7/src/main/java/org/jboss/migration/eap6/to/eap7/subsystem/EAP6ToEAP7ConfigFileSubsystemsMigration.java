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
import org.jboss.migration.wfly10.config.subsystem.WildFly10ConfigFileSubsystemsMigration;
import org.jboss.migration.wfly10.config.subsystem.WildFly10Extension;
import org.jboss.migration.wfly10.config.subsystem.WildFly10ExtensionBuilder;
import org.jboss.migration.wfly10.config.subsystem.WildFly10ExtensionNames;
import org.jboss.migration.wfly10.config.subsystem.WildFly10LegacyExtensionBuilder;
import org.jboss.migration.wfly10.config.subsystem.WildFly10SubsystemNames;
import org.jboss.migration.wfly10.config.subsystem.ee.AddConcurrencyUtilitiesDefaultConfig;
import org.jboss.migration.wfly10.config.subsystem.ee.AddDefaultBindingsConfig;
import org.jboss.migration.wfly10.config.subsystem.ejb3.AddInfinispanPassivationStoreAndDistributableCache;
import org.jboss.migration.wfly10.config.subsystem.ejb3.DefinePassivationDisabledCacheRef;
import org.jboss.migration.wfly10.config.subsystem.ejb3.RefHttpRemotingConnectorInEJB3Remote;
import org.jboss.migration.wfly10.config.subsystem.infinispan.AddEjbCache;
import org.jboss.migration.wfly10.config.subsystem.infinispan.AddServerCache;
import org.jboss.migration.wfly10.config.subsystem.infinispan.FixHibernateCacheModuleName;
import org.jboss.migration.wfly10.config.subsystem.jberet.AddBatchJBeretSubsystem;
import org.jboss.migration.wfly10.config.subsystem.messaging.AddHttpAcceptorsAndConnectors;
import org.jboss.migration.wfly10.config.subsystem.remoting.AddHttpConnectorIfMissing;
import org.jboss.migration.wfly10.config.subsystem.securitymanager.AddSecurityManagerSubsystem;
import org.jboss.migration.wfly10.config.subsystem.singleton.AddSingletonSubsystem;
import org.jboss.migration.wfly10.config.subsystem.undertow.AddBufferCache;
import org.jboss.migration.wfly10.config.subsystem.undertow.AddWebsockets;
import org.jboss.migration.wfly10.config.subsystem.undertow.MigrateHttpListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emmartins
 */
public class EAP6ToEAP7ConfigFileSubsystemsMigration extends WildFly10ConfigFileSubsystemsMigration<EAP6Server> {

    public static final List<WildFly10Extension> SUPPORTED_EXTENSIONS = initSupportedExtensions();

    private static List<WildFly10Extension> initSupportedExtensions() {

        List<WildFly10Extension> supportedExtensions = new ArrayList<>();

        // note: order may be relevant since extension/subsystem migration process order follows it

        // jacorb is legacy
        supportedExtensions.add(new WildFly10LegacyExtensionBuilder()
                .setName(WildFly10ExtensionNames.JACORB)
                .addMigratedSubsystem(WildFly10SubsystemNames.JACORB)
                .build()
        );

        // web is legacy
        supportedExtensions.add(new WildFly10LegacyExtensionBuilder()
                .setName(WildFly10ExtensionNames.WEB)
                .addMigratedSubsystem(WildFly10SubsystemNames.WEB)
                .build()
        );
        // messaging is legacy
        supportedExtensions.add(new WildFly10LegacyExtensionBuilder()
                .setName(WildFly10ExtensionNames.MESSAGING)
                .addMigratedSubsystem(WildFly10SubsystemNames.MESSAGING)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.INFINISPAN)
                .addUpdatedSubsystem(WildFly10SubsystemNames.INFINISPAN, AddServerCache.INSTANCE, AddEjbCache.INSTANCE, FixHibernateCacheModuleName.INSTANCE)
                .build()
        );
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JGROUPS)
                .addSupportedSubsystem(WildFly10SubsystemNames.JGROUPS)
                .build()
        );
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.CONNECTOR)
                .addSupportedSubsystem(WildFly10SubsystemNames.DATASOURCES)
                .addSupportedSubsystem(WildFly10SubsystemNames.JCA)
                .addSupportedSubsystem(WildFly10SubsystemNames.RESOURCE_ADAPTERS)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.DEPLOYMENT_SCANNER)
                .addSupportedSubsystem(WildFly10SubsystemNames.DEPLOYMENT_SCANNER)
                .build()
        );

        // On EAP 6, the EE subsystem config has no Concurrency Utilities and Default Bindings, thus the need for a customized  migration EE Extension, which adds these
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.EE)
                .addUpdatedSubsystem(WildFly10SubsystemNames.EE, AddConcurrencyUtilitiesDefaultConfig.INSTANCE, AddDefaultBindingsConfig.INSTANCE)
                .build()
        );

        // set ejb3 remote to http remoting
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.EJB3)
                .addUpdatedSubsystem(WildFly10SubsystemNames.EJB3, RefHttpRemotingConnectorInEJB3Remote.INSTANCE, DefinePassivationDisabledCacheRef.INSTANCE, AddInfinispanPassivationStoreAndDistributableCache.INSTANCE)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JAXRS)
                .addSupportedSubsystem(WildFly10SubsystemNames.JAXRS)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JDR)
                .addSupportedSubsystem(WildFly10SubsystemNames.JDR)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JMX)
                .addSupportedSubsystem(WildFly10SubsystemNames.JMX)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JPA)
                .addSupportedSubsystem(WildFly10SubsystemNames.JPA)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JSF)
                .addSupportedSubsystem(WildFly10SubsystemNames.JSF)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JSR77)
                .addSupportedSubsystem(WildFly10SubsystemNames.JSR77)
                .build()
        );
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.LOGGING)
                .addSupportedSubsystem(WildFly10SubsystemNames.LOGGING)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.MAIL)
                .addSupportedSubsystem(WildFly10SubsystemNames.MAIL)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.MODCLUSTER)
                .addSupportedSubsystem(WildFly10SubsystemNames.MODCLUSTER)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.NAMING)
                .addSupportedSubsystem(WildFly10SubsystemNames.NAMING)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.POJO)
                .addSupportedSubsystem(WildFly10SubsystemNames.POJO)
                .build()
        );

        // http connector must be added to remoting, if missing
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.REMOTING)
                .addUpdatedSubsystem(WildFly10SubsystemNames.REMOTING, AddHttpConnectorIfMissing.INSTANCE)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.SAR)
                .addSupportedSubsystem(WildFly10SubsystemNames.SAR)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.SECURITY)
                .addSupportedSubsystem(WildFly10SubsystemNames.SECURITY)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.TRANSACTIONS)
                .addSupportedSubsystem(WildFly10SubsystemNames.TRANSACTIONS)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.WEBSERVICES)
                .addSupportedSubsystem(WildFly10SubsystemNames.WEBSERVICES)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.WELD)
                .addSupportedSubsystem(WildFly10SubsystemNames.WELD)
                .build()
        );

        // batch jberet did not exist in source server, need tasks to create extension and subsystem default config
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.BATCH_JBERET)
                .addNewSubsystem(WildFly10SubsystemNames.BATCH_JBERET, AddBatchJBeretSubsystem.INSTANCE)
                .build()
        );

        /* If missing then EAP 7 automatically adds bean-validation subsystem to configs
        // bean-validation did not exist in source server, need tasks to create extension and subsystem default config
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.BEAN_VALIDATION)
                .addNewSubsystem(WildFly10SubsystemNames.BEAN_VALIDATION)
                .build()
        );
        */

        // singleton did not exist in source server, need tasks to create extension and subsystem default config
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.SINGLETON)
                .addNewSubsystem(WildFly10SubsystemNames.SINGLETON, AddSingletonSubsystem.INSTANCE)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.IO)
                .addSupportedSubsystem(WildFly10SubsystemNames.IO)
                .build()
        );

        // request-controller did not exist in source server, need tasks to create extension and subsystem default config
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.REQUEST_CONTROLLER)
                .addNewSubsystem(WildFly10SubsystemNames.REQUEST_CONTROLLER)
                .build()
        );

        // add new subsystem security manager
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.SECURITY_MANAGER)
                .addNewSubsystem(WildFly10SubsystemNames.SECURITY_MANAGER, AddSecurityManagerSubsystem.INSTANCE)
                .build()
        );

        // wrt undertow add buffer cache and web sockets, and migrate http listener
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.UNDERTOW)
                .addUpdatedSubsystem(WildFly10SubsystemNames.UNDERTOW, AddBufferCache.INSTANCE, MigrateHttpListener.INSTANCE, AddWebsockets.INSTANCE)
                .build()
        );

        // add default http-acceptors and http-connectors to messaging
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.MESSAGING_ACTIVEMQ)
                .addUpdatedSubsystem(WildFly10SubsystemNames.MESSAGING_ACTIVEMQ, AddHttpAcceptorsAndConnectors.INSTANCE)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.IIOP_OPENJDK)
                .addSupportedSubsystem(WildFly10SubsystemNames.IIOP_OPENJDK)
                .build()
        );

        return Collections.unmodifiableList(supportedExtensions);
    }

    public static final EAP6ToEAP7ConfigFileSubsystemsMigration INSTANCE = new EAP6ToEAP7ConfigFileSubsystemsMigration();

    private EAP6ToEAP7ConfigFileSubsystemsMigration() {
        super(SUPPORTED_EXTENSIONS);
    }
}
