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

package org.jboss.migration.wfly10.config.securityrealms;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.Server;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.ServerPath;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Migration of security realms fully compatible with WildFly 10.
 * @author emmartins
 */
public class WildFly10ConfigFileSecurityRealmsMigration<S extends Server> {

    public interface EnvironmentProperties {
        /**
         * the prefix for the name of security realms related properties
         */
        String PROPERTIES_PREFIX = "security-realms.";
        /**
         * Boolean property which if true skips migration of security realms
         */
        String SKIP = PROPERTIES_PREFIX + "skip";
    }

    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder().setName("security-realms").build();

    public static final String SERVER_MIGRATION_TASK_SECURITY_REALM_NAME = "security-realm";
    public static final String MIGRATION_REPORT_TASK_ATTR_AUTHENTICATION_PROPERTIES_SOURCE = "Authentication Properties Source: ";
    public static final String MIGRATION_REPORT_TASK_ATTR_AUTHENTICATION_PROPERTIES_TARGET = "Authentication Properties Target: ";
    public static final String MIGRATION_REPORT_TASK_ATTR_AUTHORIZATION_PROPERTIES_SOURCE = "Authorization Properties Source: ";
    public static final String MIGRATION_REPORT_TASK_ATTR_AUTHORIZATION_PROPERTIES_TARGET = "Authorization Properties Target: ";

    public ServerMigrationTask getServerMigrationTask(final ServerPath<S> source, final WildFly10SecurityRealmsManagement securityRealmsManagement) {
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return SERVER_MIGRATION_TASK_NAME;
            }

            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                WildFly10ConfigFileSecurityRealmsMigration.this.run(source, securityRealmsManagement, context);
                return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
            }
        };
    }

    protected void run(ServerPath<S> source, final WildFly10SecurityRealmsManagement securityRealmsManagement, ServerMigrationTaskContext context) throws IOException {
        if (context.getServerMigrationContext().getMigrationEnvironment().getPropertyAsBoolean(EnvironmentProperties.SKIP, Boolean.FALSE)) {
            return;
        }
        /*if (context.isInteractive()) {
            final UserConfirmation.ResultHandler resultHandler = new UserConfirmation.ResultHandler() {
                @Override
                public void onNo() {
                    context.getLogger().info("Security realms migration skipped by user.");
                }
                @Override
                public void onYes() {
                    try {
                        migrateSecurityRealms(source, target, context);
                    } catch (IOException e) {
                        throw new ServerMigrationFailedException(e);
                    }
                }
                @Override
                public void onError() {
                    // repeat
                    try {
                        run(source, target, context);
                    } catch (IOException e) {
                        throw new ServerMigrationFailedException(e);
                    }
                }
            };
            new UserConfirmation(context.getConsoleWrapper(), "Migrate security realms?", ROOT_LOGGER.yesNo(), resultHandler).execute();
        } else {
        */
        // by default security realms are migrated
        migrateSecurityRealms(source, securityRealmsManagement, context);
        //}
    }

    protected void migrateSecurityRealms(ServerPath<S> source, final WildFly10SecurityRealmsManagement securityRealmsManagement, ServerMigrationTaskContext context) throws IOException {
        context.getServerMigrationContext().getConsoleWrapper().printf("%n%n");
        context.getLogger().infof("Security realms migration starting...");
            for (String securityRealm : securityRealmsManagement.getSecurityRealms()) {
                migrateSecurityRealm(securityRealm, source, securityRealmsManagement, context);
            }
            context.getLogger().infof("Security realms migration done.");
    }

    protected void migrateSecurityRealm(final String securityRealmName, final ServerPath<S> source, final WildFly10SecurityRealmsManagement securityRealmsManagement, final ServerMigrationTaskContext context) throws IOException {
        final ServerMigrationTaskName securityRealmMigrationTaskName = new ServerMigrationTaskName.Builder().setName(SERVER_MIGRATION_TASK_SECURITY_REALM_NAME).addAttribute("name", securityRealmName).build();
        final ServerMigrationTask securityRealmMigrationTask = new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return securityRealmMigrationTaskName;
            }
            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                context.getLogger().infof("Migrating security realm: %s", securityRealmName);
                final ModelNode securityRealmConfig = securityRealmsManagement.getSecurityRealm(securityRealmName);
                if (securityRealmConfig.hasDefined(AUTHENTICATION, PROPERTIES)) {
                    copyPropertiesFile(AUTHENTICATION, securityRealmName, securityRealmConfig, source, securityRealmsManagement, context);
                }
                if (securityRealmConfig.hasDefined(AUTHORIZATION, PROPERTIES)) {
                    copyPropertiesFile(AUTHORIZATION, securityRealmName, securityRealmConfig, source, securityRealmsManagement, context);
                }
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
        context.execute(securityRealmMigrationTask);
    }

    private void copyPropertiesFile(String propertiesName, String securityRealmName, ModelNode securityRealmConfig, ServerPath<S> source, WildFly10SecurityRealmsManagement securityRealmsManagement, ServerMigrationTaskContext context) throws IOException {
        final Server sourceServer = source.getServer();
        final Server targetServer = securityRealmsManagement.getConfigurationManagement().getServer();
        final ModelNode properties = securityRealmConfig.get(propertiesName);
        if (properties.hasDefined(PATH)) {
            final String path = properties.get(PATH).asString();
            context.getLogger().debugf("Properties path: %s", path);
            String relativeTo = null;
            if (properties.hasDefined(RELATIVE_TO)) {
                relativeTo = properties.get(RELATIVE_TO).asString();
            }
            context.getLogger().debugf("Properties relative_to: %s", String.valueOf(relativeTo));
            if (relativeTo == null) {
                // path is absolute, if in source server base dir then relativize and copy to target server base dir, and update the config with the new path
                final Path sourcePath = Paths.get(path);
                context.getLogger().infof("Source Properties file path: %s", sourcePath);
                if (sourcePath.startsWith(sourceServer.getBaseDir())) {
                    final Path targetPath = sourceServer.getBaseDir().resolve(targetServer.getBaseDir().relativize(sourcePath));
                    context.getLogger().infof("Target Properties file path: %s", targetPath);
                    context.getServerMigrationContext().getMigrationFiles().copy(sourcePath, targetPath);
                    final PathAddress pathAddress = securityRealmsManagement.getSecurityRealmPathAddress(securityRealmName).append(PathElement.pathElement(propertiesName, PROPERTIES));
                    final ModelNode op = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                    op.get(NAME).set(PATH);
                    op.get(VALUE).set(targetPath.toString());
                    securityRealmsManagement.getConfigurationManagement().executeManagementOperation(op);
                } else {
                    context.getLogger().infof("Source Properties file path is not in source server base dir, skipping file copy");
                }
            } else {
                // path is relative to relative_to
                final Path resolvedSourcePath = sourceServer.resolvePath(relativeTo);
                if (resolvedSourcePath == null) {
                    throw new IOException("failed to resolve source path "+relativeTo);
                }
                final Path sourcePath = resolvedSourcePath.normalize().resolve(path);
                context.getLogger().infof("Source Properties file path: %s", sourcePath);
                final Path resolvedTargetPath = targetServer.resolvePath(relativeTo);
                if (resolvedTargetPath == null) {
                    throw new IOException("failed to resolve target path "+relativeTo);
                }
                final Path targetPath = resolvedTargetPath.normalize().resolve(path);
                context.getLogger().infof("Target Properties file path: %s", targetPath);
                if (!sourcePath.equals(targetPath)) {
                    context.getServerMigrationContext().getMigrationFiles().copy(sourcePath, targetPath);
                } else {
                    context.getLogger().infof("Resolved paths for Source and Target Properties files is the same.");
                }
            }
        }
    }
}