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
package org.jboss.migration.wfly8;

import org.jboss.migration.core.AbstractServer;
import org.jboss.migration.core.ProductInfo;
import org.jboss.migration.core.ServerPath;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The WildFly 8 {@link org.jboss.migration.core.Server}.
 * @author emmartins
 */
public class WildFly8Server extends AbstractServer {

    public WildFly8Server(ProductInfo productInfo, Path baseDir) {
        super(productInfo, baseDir);
    }

    public Collection<ServerPath<WildFly8Server>> getStandaloneConfigs() {
        // FIXME scan the config dir instead
        List<ServerPath<WildFly8Server>> standaloneConfigs = new ArrayList<>();
        final Path standaloneConfigurationDir = getStandaloneConfigurationDir();
        standaloneConfigs.add(new ServerPath<>(standaloneConfigurationDir.resolve("standalone.xml"), this));
        standaloneConfigs.add(new ServerPath<>(standaloneConfigurationDir.resolve("standalone-ha.xml"), this));
        standaloneConfigs.add(new ServerPath<>(standaloneConfigurationDir.resolve("standalone-full.xml"), this));
        standaloneConfigs.add(new ServerPath<>(standaloneConfigurationDir.resolve("standalone-full-ha.xml"), this));
        return Collections.unmodifiableList(standaloneConfigs);
    }

    public Path getStandaloneDir() {
        return getBaseDir().resolve("standalone");
    }

    public Path getStandaloneConfigurationDir() {
        return getStandaloneDir().resolve("configuration");
    }

    public Path getModulesDir() {
        return getModulesDir(getBaseDir());
    }

    public Path getModulesSystemLayersBaseDir() {
        return getModulesSystemLayersBaseDir(getBaseDir());
    }

    public static Path getModulesDir(Path baseDir) {
        return baseDir.resolve("modules");
    }

    public static Path getModulesSystemLayersBaseDir(Path baseDir) {
        return getModulesDir(baseDir).resolve("system").resolve("layers").resolve("base");
    }
}
