package ml.karmaconfigs.api.common.version;

/*
 * This file is part of KarmaAPI, licensed under the MIT License.
 *
 *  Copyright (c) karma (KarmaDev) <karmaconfigs@gmail.com>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

import ml.karmaconfigs.api.common.karma.APISource;
import ml.karmaconfigs.api.common.karma.KarmaSource;
import ml.karmaconfigs.api.common.karmafile.KarmaFile;
import ml.karmaconfigs.api.common.timer.scheduler.LateScheduler;
import ml.karmaconfigs.api.common.timer.scheduler.worker.AsyncLateScheduler;
import ml.karmaconfigs.api.common.utils.URLUtils;
import ml.karmaconfigs.api.common.utils.string.ComparatorBuilder;
import ml.karmaconfigs.api.common.utils.string.StringUtils;
import ml.karmaconfigs.api.common.utils.file.FileUtilities;
import ml.karmaconfigs.api.common.utils.string.VersionComparator;
import ml.karmaconfigs.api.common.version.util.VersionCheckType;
import ml.karmaconfigs.api.common.version.util.VersionResolver;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Karma version updater
 */
public abstract class VersionUpdater {

    /**
     * A map containing source => version fetch
     */
    private static final Map<KarmaSource, VersionFetchResult> results = new ConcurrentHashMap<>();

    /**
     * The updater source
     */
    private KarmaSource source;

    /**
     * The url where to check for updates
     */
    private URL checkURL;

    /**
     * The version check type
     */
    private VersionCheckType versionType;
    /**
     * The version resolver
     */
    private VersionResolver versionResolver;

    /**
     * Instantiate a new version updater
     *
     * @return a new version updater instance
     */
    static VersionUpdater instance() {
        return new VersionUpdater() {

        };
    }

    /**
     * Create a new version updater builder
     *
     * @param owner the updater source
     * @return a new version updater builder
     */
    public static VersionBuilder createNewBuilder(final KarmaSource owner) {
        return VersionBuilder.instance(owner);
    }

    /**
     * Fetch for updates
     *
     * @param force force the update instead
     *              of returning the cached result
     * @return the fetch result
     */
    public LateScheduler<VersionFetchResult> fetch(boolean force) {
        AsyncLateScheduler<VersionFetchResult> asyncLateScheduler = new AsyncLateScheduler<>();

        if (force || !results.containsKey(this.source) || results.getOrDefault(this.source, null) == null) {
            APISource.asyncScheduler().queue(() -> {
                try {
                    boolean updated;
                    URLConnection connection = this.checkURL.openConnection();
                    InputStream file = connection.getInputStream();
                    Path temp = Files.createTempFile("kfetcher_", StringUtils.generateString().create());
                    File tempFile = FileUtilities.getFixedFile(temp.toFile());
                    tempFile.deleteOnExit();
                    if (!tempFile.exists())
                        Files.createFile(temp);
                    Files.copy(file, temp, StandardCopyOption.REPLACE_EXISTING);
                    KarmaFile kFile = new KarmaFile(tempFile);
                    String version = kFile.getString("VERSION", this.source.version());
                    String update = kFile.getString("UPDATE", "");
                    String[] changelog = (String[]) kFile.getStringList("CHANGELOG", new String[0]).toArray((Object[]) new String[0]);

                    ComparatorBuilder builder;
                    VersionComparator comparator;
                    switch (this.versionType) {
                        case ID:
                            updated = this.source.version().equals(version);
                            break;
                        case RESOLVABLE_ID:
                            builder = VersionComparator.createBuilder()
                                    .currentVersion(versionResolver.resolve(source.version()))
                                    .checkVersion(versionResolver.resolve(version));
                            comparator = StringUtils.compareTo(builder);

                            updated = comparator.isUpToDate();
                            break;
                        default:
                            builder = VersionComparator.createBuilder()
                                    .currentVersion(source.version())
                                    .checkVersion(version);
                            comparator = StringUtils.compareTo(builder);

                            updated = comparator.isUpToDate();
                            break;
                    }
                    VersionFetchResult result = new VersionFetchResult(updated, version, this.source.version(), update, changelog, this.versionResolver);
                    results.put(this.source, result);
                    asyncLateScheduler.complete(result);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    asyncLateScheduler.complete(null, ex);
                }
            });
        } else {
            asyncLateScheduler.complete(results.get(this.source));
        }
        return asyncLateScheduler;
    }

    /**
     * Get the last update fetch result
     *
     * @return the last update fetch result
     */
    public LateScheduler<VersionFetchResult> get() {
        AsyncLateScheduler<VersionFetchResult> asyncLateScheduler = new AsyncLateScheduler<>();
        APISource.asyncScheduler().queue(() -> {
            VersionFetchResult result = results.getOrDefault(this.source, null);
            if (result == null) {
                fetch(true).whenComplete((Consumer<VersionFetchResult>) asyncLateScheduler::complete);
            } else {
                asyncLateScheduler.complete(result);
            }
        });
        return asyncLateScheduler;
    }

    /**
     * Version updater builder
     */
    public static abstract class VersionBuilder {

        /**
         * The updater source
         */
        private final KarmaSource source;

        /**
         * The updater check type
         */
        private VersionCheckType versionType = VersionCheckType.NUMBER;

        /**
         * The updater version resolver
         */
        private VersionResolver versionResolver;

        /**
         * Initialize the version updater builder
         *
         * @param owner the updater source
         */
        VersionBuilder(final KarmaSource owner) {
            this.source = owner;
        }

        /**
         * Create a new version builder instance
         *
         * @param owner the updater source
         * @return a new version builder instance
         */
        static VersionBuilder instance(final KarmaSource owner) {
            return new VersionBuilder(owner) {

            };
        }

        /**
         * Set the version updater version type
         *
         * @param type the updater version type
         * @return this instance
         */
        public final VersionBuilder withVersionType(VersionCheckType type) {
            this.versionType = type;
            return this;
        }

        /**
         * Set the version updater version resolver
         *
         * @param resolver the version resolver
         * @return this instance
         * @throws IllegalStateException if the version type is not
         * resolvable ID
         */
        public final VersionBuilder withVersionResolver(final VersionResolver resolver) throws IllegalStateException {
            if (this.versionType == VersionCheckType.RESOLVABLE_ID) {
                this.versionResolver = resolver;
            } else {
                throw new IllegalStateException("Cannot set version resolver for non-resolvable version check type builder");
            }
            return this;
        }

        /**
         * Get the version updater source
         *
         * @return the version updater source
         */
        protected KarmaSource getSource() {
            return this.source;
        }

        /**
         * Get the version updater check type
         *
         * @return the version updater check type
         */
        protected VersionCheckType getType() {
            return this.versionType;
        }

        /**
         * Get the version updater version resolver
         *
         * @return the version updater version resolver
         */
        protected VersionResolver getResolver() {
            return this.versionResolver;
        }

        /**
         * Build the version builder
         *
         * @return a new version updater instance
         * @throws IllegalStateException if something goes wrong
         */
        public VersionUpdater build() throws IllegalStateException {
            if (!StringUtils.isNullOrEmpty(URLUtils.getOrNull(source.updateURL())) && this.source.updateURL().endsWith(".kupdter")) {
                VersionUpdater analyzer = VersionUpdater.instance();
                analyzer.source = this.source;
                analyzer.checkURL = URLUtils.getOrNull(source.updateURL());
                analyzer.versionType = this.versionType;
                if (this.versionType.equals(VersionCheckType.RESOLVABLE_ID) && this.versionResolver == null)
                    throw new IllegalStateException("Cannot build a version updater with null version resolver and using RESOLVABLE_ID version type");
                analyzer.versionResolver = this.versionResolver;
                return analyzer;
            }
            throw new IllegalStateException("Cannot build a version updater with null/invalid version check URL ( update url must be a .kupdter file )");
        }
    }
}
