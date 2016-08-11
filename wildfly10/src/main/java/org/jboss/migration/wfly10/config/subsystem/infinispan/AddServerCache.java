/*
 * Copyright 2016 Red Hat, Inc.
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
package org.jboss.migration.wfly10.config.subsystem.infinispan;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.WildFly10ConfigurationManagement;
import org.jboss.migration.wfly10.config.subsystem.WildFly10Subsystem;
import org.jboss.migration.wfly10.config.subsystem.WildFly10SubsystemManagement;
import org.jboss.migration.wfly10.config.subsystem.WildFly10SubsystemMigrationTask;
import org.jboss.migration.wfly10.config.subsystem.WildFly10SubsystemMigrationTaskFactory;

import static org.jboss.as.controller.PathElement.pathElement;

/**
 * A task which adds the 'server' cache, present on EAP 7 default configs, to Infinispan subsystem configuration.
 * @author emmartins
 */
public class AddServerCache implements WildFly10SubsystemMigrationTaskFactory {

    public static final AddServerCache INSTANCE = new AddServerCache();

    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder().setName("add-infinispan-server-cache").build();

    private AddServerCache() {
    }

    private static final String CACHE_CONTAINER = "cache-container";
    private static final String CACHE_NAME = "server";
    private static final String DEFAULT_CACHE_ATTR_NAME = "default-cache";
    private static final String DEFAULT_CACHE_ATTR_VALUE = "default";
    private static final String MODULE_ATTR_NAME = "module";
    private static final String MODULE_ATTR_VALUE = "org.wildfly.clustering.server";
    private static final String LOCAL_CACHE = "local-cache";
    private static final String LOCAL_CACHE_NAME = "default";
    private static final String COMPONENT = "component";
    private static final String COMPONENT_NAME = "transaction";
    private static final String MODE_ATTR_NAME = "mode";
    private static final String MODE_ATTR_VALUE = "BATCH";

    @Override
    public ServerMigrationTask getServerMigrationTask(ModelNode config, WildFly10Subsystem subsystem, WildFly10SubsystemManagement subsystemManagement) {
        return new WildFly10SubsystemMigrationTask(config, subsystem, subsystemManagement) {
            @Override
            public ServerMigrationTaskName getName() {
                return SERVER_MIGRATION_TASK_NAME;
            }
            @Override
            protected ServerMigrationTaskResult run(ModelNode config, WildFly10Subsystem subsystem, WildFly10SubsystemManagement subsystemManagement, ServerMigrationTaskContext context, TaskEnvironment taskEnvironment) throws Exception {
                if (config == null) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                if (!config.hasDefined(CACHE_CONTAINER)) {
                    context.getLogger().infof("No Cache container");
                    return ServerMigrationTaskResult.SKIPPED;
                }
                if (config.hasDefined(CACHE_CONTAINER, CACHE_NAME)) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                final PathAddress subsystemPathAddress = subsystemManagement.getSubsystemPathAddress(subsystem.getName());
                final WildFly10ConfigurationManagement configurationManagement = subsystemManagement.getConfigurationManagement();
                /*
        <cache-container name="server" default-cache="default" module="org.wildfly.clustering.server">
                <local-cache name="default">
                    <transaction mode="BATCH"/>
                </local-cache>
            </cache-container>
         */
                final Operations.CompositeOperationBuilder compositeOperationBuilder = Operations.CompositeOperationBuilder.create();
                final PathAddress cachePathAddress = subsystemPathAddress.append(pathElement(CACHE_CONTAINER, CACHE_NAME));
                final ModelNode cacheAddOperation = Util.createAddOperation(cachePathAddress);
                cacheAddOperation.get(DEFAULT_CACHE_ATTR_NAME).set(DEFAULT_CACHE_ATTR_VALUE);
                cacheAddOperation.get(MODULE_ATTR_NAME).set(MODULE_ATTR_VALUE);
                compositeOperationBuilder.addStep(cacheAddOperation);
                // add local cache
                final PathAddress localCachePathAddress = cachePathAddress.append(LOCAL_CACHE, LOCAL_CACHE_NAME);
                final ModelNode localCacheAddOperation = Util.createAddOperation(localCachePathAddress);
                compositeOperationBuilder.addStep(localCacheAddOperation);
                // set local cache trnsaction mode as batch
                final PathAddress localCacheTransactionPathAddress = localCachePathAddress.append(COMPONENT, COMPONENT_NAME);
                final ModelNode localCacheTransactionAddOperation = Util.createAddOperation(localCacheTransactionPathAddress);
                localCacheTransactionAddOperation.get(MODE_ATTR_NAME).set(MODE_ATTR_VALUE);
                compositeOperationBuilder.addStep(localCacheTransactionAddOperation);
                configurationManagement.executeManagementOperation(compositeOperationBuilder.build().getOperation());
                context.getLogger().infof("Server cache added to Infinispan subsystem configuration.");
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
    }
}
