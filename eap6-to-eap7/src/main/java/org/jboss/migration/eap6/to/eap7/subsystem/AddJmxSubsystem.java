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

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.wfly10.config.WildFly10ConfigurationManagement;
import org.jboss.migration.wfly10.config.subsystem.AddSubsystem;
import org.jboss.migration.wfly10.config.subsystem.WildFly10Subsystem;
import org.jboss.migration.wfly10.config.subsystem.WildFly10SubsystemManagement;

/**
 * A task which adds the jmx subsystem to host configs.
 * @author emmartins
 */
public class AddJmxSubsystem extends AddSubsystem {

    public static final AddJmxSubsystem INSTANCE = new AddJmxSubsystem();

    private AddJmxSubsystem() {
    }

    private static final String EXPOSE_MODEL = "expose-model";
    private static final String RESOLVED = "resolved";
    private static final String EXPRESSION = "expression";
    private static final String REMOTING_CONNECTOR = "remoting-connector";
    private static final String JMX = "jmx";


    @Override
    protected void addSubsystem(WildFly10Subsystem subsystem, WildFly10SubsystemManagement subsystemManagement, ServerMigrationTaskContext context) throws Exception {
        super.addSubsystem(subsystem, subsystemManagement, context);
        // add jmx subsystem default config
            /*
            <profile>
        <subsystem xmlns="urn:jboss:domain:jmx:1.3">
            <expose-resolved-model/>
            <expose-expression-model/>
            <remoting-connector/>
        </subsystem>
    </profile>
             */
        final WildFly10ConfigurationManagement configurationManagement = subsystemManagement.getConfigurationManagement();
        final PathAddress subsystemPathAddress = subsystemManagement.getSubsystemPathAddress(subsystem.getName());
        final ModelNode exposeResolvedModelAddOperation = Util.createAddOperation(subsystemPathAddress.append(PathElement.pathElement(EXPOSE_MODEL, RESOLVED)));
        configurationManagement.executeManagementOperation(exposeResolvedModelAddOperation);
        final ModelNode exposeExpressionModelAddOperation = Util.createAddOperation(subsystemPathAddress.append(PathElement.pathElement(EXPOSE_MODEL, EXPRESSION)));
        configurationManagement.executeManagementOperation(exposeExpressionModelAddOperation);
        final ModelNode remotingConnectorAddOperation = Util.createAddOperation(subsystemPathAddress.append(PathElement.pathElement(REMOTING_CONNECTOR, JMX)));
        configurationManagement.executeManagementOperation(remotingConnectorAddOperation);
    }
}
