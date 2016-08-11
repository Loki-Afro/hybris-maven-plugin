package com.divae.ageto.hybris.install.extensions;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author Marvin Haagen
 */
public enum ExtensionFactory {

    ;

    private static final Map<String, Extension> EXTENSIONS = Maps.newHashMap();

    public static List<Extension> getExtensions(final File hybrisInstallDirectory) {
        final List<String> extensionNames = Extensions.getExtensionNames(hybrisInstallDirectory);
        final Map<String, File> extensionPaths = getExtensionPaths(hybrisInstallDirectory);
        final List<Extension> extensions = Lists.newArrayList();

        for (String extensionName : extensionNames) {
            Extension extension = createExtension(extensionName, extensionPaths);
            extensions.add(extension);
        }

        return extensions;
    }

    public static final List<Extension> getTransitiveExtensions(final File hybrisInstallDirectory) {
        Set<Extension> extensions = Sets.newHashSet();
        List<Extension> extensionList = Lists.newArrayList();

        for (Extension extension : getExtensions(hybrisInstallDirectory)) {
            collectExtension(extension, extensions);
        }

        for (Extension extension : extensions) {
            extensionList.add(extension);
        }

        return extensionList;
    }

    private static void collectExtension(Extension extension, Set<Extension> extensions) {
        extensions.add(extension);

        for (Extension dependency : extension.getDependencies()) {
            collectExtension(dependency, extensions);
        }
    }

    private static Map<String, File> getExtensionPaths(final File hybrisInstallDirectory) {
        final File hybrisBinDirectory = new File(hybrisInstallDirectory, "bin");
        final Map<String, File> extensionPaths = Maps.newHashMap();
        try {
            Files.walkFileTree(hybrisBinDirectory.toPath(), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().equals("extensioninfo.xml")) {
                        extensionPaths.put(ExtensionInfo.getExtensionName(file.toFile()), file.getParent().toFile());
                    }
                    return FileVisitResult.CONTINUE;

                }
            });
        } catch (IOException e) {
            Throwables.propagate(e);
        }
        return extensionPaths;
    }

    private static List<Extension> getDependencies(final String extensionName, final Map<String, File> extensionPaths) {
        File extensionPath = extensionPaths.get(extensionName);
        File extensionInfo = new File(extensionPath, "extensioninfo.xml");
        List<String> dependencyNames = ExtensionInfo.getDependencyNames(extensionInfo);
        List<Extension> extensions = Lists.newArrayList();

        for (String dependencyName : dependencyNames) {
            if (!EXTENSIONS.containsKey(dependencyName)) {
                createExtension(dependencyName, extensionPaths);
            }
            extensions.add(EXTENSIONS.get(dependencyName));
        }

        return extensions;
    }

    private static Extension createExtension(final String extensionName, final Map<String, File> extensionPaths) {
        final Extension extension = new Extension(extensionPaths.get(extensionName), extensionName, null,
                getDependencies(extensionName, extensionPaths));
        EXTENSIONS.put(extensionName, extension);
        return extension;
    }

}