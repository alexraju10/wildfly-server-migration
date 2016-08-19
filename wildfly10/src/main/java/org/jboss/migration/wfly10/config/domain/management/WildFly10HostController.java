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

package org.jboss.migration.wfly10.config.domain.management;

import org.jboss.migration.wfly10.config.WildFly10ConfigurationManagement;
import org.jboss.migration.wfly10.config.domain.servergroup.WildFly10ServerGroupsManagement;
import org.jboss.migration.wfly10.config.subsystem.WildFly10SubsystemManagement;

import java.io.IOException;
import java.util.Set;

/**
 * @author emmartins
 */
public interface WildFly10HostController extends WildFly10ConfigurationManagement {
    WildFly10SubsystemManagement getSubsystemManagement(String profile);
    Set<String> getProfiles() throws IOException;
    Set<String> getHosts() throws IOException;
    WildFly10ServerGroupsManagement getServerGroupsManagement();
}