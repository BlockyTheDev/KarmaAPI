package ml.karmaconfigs.api.bukkit;

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

import ml.karmaconfigs.api.bukkit.scheduler.BukkitAsyncScheduler;
import ml.karmaconfigs.api.bukkit.scheduler.BukkitSyncScheduler;
import ml.karmaconfigs.api.common.Console;
import ml.karmaconfigs.api.common.Logger;
import ml.karmaconfigs.api.common.karma.APISource;
import ml.karmaconfigs.api.common.karma.KarmaAPI;
import ml.karmaconfigs.api.common.karma.KarmaSource;
import ml.karmaconfigs.api.common.timer.scheduler.Scheduler;
import ml.karmaconfigs.api.common.timer.worker.AsyncScheduler;
import ml.karmaconfigs.api.common.timer.worker.SyncScheduler;
import ml.karmaconfigs.api.common.utils.KarmaLogger;
import ml.karmaconfigs.api.common.utils.placeholder.GlobalPlaceholderEngine;
import ml.karmaconfigs.api.common.utils.placeholder.util.Placeholder;
import ml.karmaconfigs.api.common.utils.string.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * Karma plugin
 */
public abstract class KarmaPlugin extends JavaPlugin implements KarmaSource {

    /**
     * The plugin console
     */
    private final Console console;

    /**
     * The plugin logger
     */
    private KarmaLogger logger;

    /**
     * The plugin async scheduler
     */
    private Scheduler async;

    /**
     * The plugin sync scheduler
     */
    private Scheduler sync;

    /**
     * Initialize the KarmaPlugin
     */
    public KarmaPlugin() {
        KarmaAPI.install();
        console = new Console(this, (msg) -> Bukkit.getServer().getConsoleSender().sendMessage(StringUtils.toColor(msg)));

        async = new AsyncScheduler<>(this);
        sync = new SyncScheduler<>(this);

        if (!APISource.hasProvider(name())) {
            APISource.addProvider(this);
        }
    }

    /**
     * Initialize the KarmaPlugin
     *
     * @param defineDefault if this source should be defined
     *                      as the default source
     * @throws SecurityException if the default module is already
     * set
     */
    public KarmaPlugin(final boolean defineDefault) throws SecurityException {
        KarmaAPI.install();
        console = new Console(this, (msg) -> Bukkit.getServer().getConsoleSender().sendMessage(StringUtils.toColor(msg)));

        async = new AsyncScheduler<>(this);
        sync = new SyncScheduler<>(this);

        if (!APISource.hasProvider(name())) {
            APISource.addProvider(this);
            if (defineDefault) {
                APISource.defineDefault(this);
            }
        }
    }

    /**
     * Enable the KarmaPlugin
     */
    public abstract void enable();

    /**
     * On plugin enable
     */
    @Override
    public final void onEnable() {
        async = new BukkitAsyncScheduler<>(this);
        sync = new BukkitSyncScheduler<>(this);

        enable();

        logger = new Logger(this);
    }

    /**
     * Get the source async scheduler
     *
     * @return the source async scheduler
     */
    @Override
    public Scheduler async() {
        return async;
    }

    /**
     * Get the source sync scheduler
     *
     * @return the source sync scheduler
     */
    @Override
    public Scheduler sync() {
        return sync;
    }

    /**
     * Get the source out
     *
     * @return the source out
     */
    @Override
    public Console console() {
        return console;
    }

    /**
     * Get the plugin file logger
     *
     * @return the plugin file logger
     */
    @Override
    public KarmaLogger logger() {
        return logger;
    }

    /**
     * Create a player placeholder
     *
     * @param key the placeholder key
     * @param onRequest on placeholder request
     * @return the placeholder
     */
    public static Placeholder<String> createTextPlaceholder(final String key, final BiConsumer<Player, String> onRequest) {
        return new Placeholder<String>() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public String getValue(@Nullable Object container) {
                if (container instanceof Player) {
                    Player player = (Player) container;
                    String original = "placeholder " + key + " value";
                    onRequest.accept(player, original);

                    return original;
                } else {
                    return StringUtils.toColor("&ccontainer not a player");
                }
            }

            @Override
            public Class<?> getType() {
                return Player.class;
            }
        };
    }

    /**
     * Create a player placeholder
     *
     * @param key the placeholder key
     * @param onRequest on placeholder request
     * @return the placeholder
     */
    public static Placeholder<Integer> createIntegerPlaceholder(final String key, final BiConsumer<Player, Integer> onRequest) {
        return new Placeholder<Integer>() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public Integer getValue(@Nullable Object container) {
                if (container instanceof Player) {
                    Player player = (Player) container;
                    int original = Integer.MAX_VALUE;
                    onRequest.accept(player, original);

                    return original;
                } else {
                    return Integer.MIN_VALUE;
                }
            }

            @Override
            public Class<?> getType() {
                return Player.class;
            }
        };
    }

    /**
     * Create a player placeholder
     *
     * @param key the placeholder key
     * @param onRequest on placeholder request
     * @return the placeholder
     */
    public static Placeholder<Double> createDoublePlaceholder(final String key, final BiConsumer<Player, Double> onRequest) {
        return new Placeholder<Double>() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public Double getValue(@Nullable Object container) {
                if (container instanceof Player) {
                    Player player = (Player) container;
                    double original = Double.MAX_VALUE;
                    onRequest.accept(player, original);

                    return original;
                } else {
                    return Double.MIN_VALUE;
                }
            }

            @Override
            public Class<?> getType() {
                return Player.class;
            }
        };
    }

    /**
     * Create a player placeholder
     *
     * @param key the placeholder key
     * @param onRequest on placeholder request
     * @return the placeholder
     */
    public static Placeholder<Float> createFloatPlaceholder(final String key, final BiConsumer<Player, Float> onRequest) {
        return new Placeholder<Float>() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public Float getValue(@Nullable Object container) {
                if (container instanceof Player) {
                    Player player = (Player) container;
                    float original = Float.MAX_VALUE;
                    onRequest.accept(player, original);

                    return original;
                } else {
                    return Float.MIN_VALUE;
                }
            }

            @Override
            public Class<?> getType() {
                return Player.class;
            }
        };
    }

    /**
     * Create a player placeholder
     *
     * @param <T> the placeholder type
     * @param key the placeholder key
     * @param onRequest on placeholder request
     * @return the placeholder
     */
    public static <T> Placeholder<T> createAnyPlaceholder(final String key, final BiConsumer<Player, T> onRequest) {
        return new Placeholder<T>() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public T getValue(@Nullable Object container) {
                if (container instanceof Player) {
                    Player player = (Player) container;
                    T original = null;
                    onRequest.accept(player, original);

                    return original;
                } else {
                    return null;
                }
            }

            @Override
            public Class<?> getType() {
                return Player.class;
            }
        };
    }

    /**
     * Register globally a player placeholder
     *
     * @param placeholders the player placeholder
     */
    public static void registerPlayerPlaceholder(final Placeholder<?>... placeholders) {
        GlobalPlaceholderEngine engine = new GlobalPlaceholderEngine(KarmaAPI.source(false));
        engine.protect();

        engine.registerUnsafe(placeholders);
    }
}