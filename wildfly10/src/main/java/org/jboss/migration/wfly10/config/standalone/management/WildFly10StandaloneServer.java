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

package org.jboss.migration.wfly10.config.standalone.management;

import org.jboss.migration.wfly10.config.WildFly10ConfigurationManagement;
import org.jboss.migration.wfly10.config.securityrealms.WildFly10SecurityRealmsManagement;
import org.jboss.migration.wfly10.config.subsystem.WildFly10SubsystemManagement;

/**
 * @author emmartins
 */
public interface WildFly10StandaloneServer extends WildFly10ConfigurationManagement {
    WildFly10SubsystemManagement getSubsystemManagement();
    WildFly10SecurityRealmsManagement getSecurityRealmsManagement();
}