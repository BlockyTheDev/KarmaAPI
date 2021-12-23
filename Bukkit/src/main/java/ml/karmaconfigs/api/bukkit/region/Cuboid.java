package ml.karmaconfigs.api.bukkit.region;

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

import com.bergerkiller.bukkit.common.events.EntityMoveEvent;
import ml.karmaconfigs.api.bukkit.KarmaPlugin;
import ml.karmaconfigs.api.bukkit.region.corner.util.Corner;
import ml.karmaconfigs.api.bukkit.region.wall.util.Wall;
import ml.karmaconfigs.api.bukkit.region.wall.util.WallType;
import ml.karmaconfigs.api.common.ResourceDownloader;
import ml.karmaconfigs.api.common.karma.KarmaAPI;
import ml.karmaconfigs.api.common.karma.KarmaSource;
import ml.karmaconfigs.api.common.karma.loader.BruteLoader;
import ml.karmaconfigs.api.common.karma.loader.component.NameComponent;
import ml.karmaconfigs.api.common.utils.URLUtils;
import ml.karmaconfigs.api.common.utils.enums.Level;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/**
 * Karma cuboid region
 *
 * Code from:
 * https://www.spigotmc.org/threads/region-cuboid.329859/
 */
@SuppressWarnings("unused")
public abstract class Cuboid implements Serializable {

    private static boolean tried = false;
    private static Plugin hooked = null;
    private static Listener dummy = null;
    private static Listener blocks = null;

    private final static Set<Cuboid> regions = new HashSet<>();

    /**
     * Initialize the cuboid region
     *
     * @param plugin the cuboid owner
     */
    public Cuboid(final KarmaPlugin plugin) {
        KarmaSource source = KarmaAPI.source(false);
        PluginManager manager = plugin.getServer().getPluginManager();

        if (!manager.isPluginEnabled(hooked)) {
            if (hooked != null && dummy != null && blocks != null) {
                HandlerList.unregisterAll(dummy);
                HandlerList.unregisterAll(blocks);
            }

            boolean proceed = true;
            if (!manager.isPluginEnabled("BKCommonLib") && !tried) {
                tried = true;

                source.console().send("KarmaAPI region API needs BKCommonLib to work but we didn't found it. We will download it", Level.GRAVE);

                File pluginsFolder = new File(plugin.getServer().getWorldContainer(), "plugins");
                File destination = new File(pluginsFolder, "BKCommonLib.jar");

                ResourceDownloader downloader = new ResourceDownloader(destination,  URLUtils.getOrBackup(
                        "https://karmadev.es/dl/bkcommonlib/plugin.jar", "https://karmarepo.000webhostapp.com/dl/bkcommonlib/plugin.jar").toString());
                downloader.download();

                try {
                    Plugin result = manager.loadPlugin(destination);
                    if (result != null) {
                        source.console().send("&aDownloaded and enabled BKCommonLib");
                    } else {
                        source.console().send("&cBKCommonLib has been downloaded but couldn't be enabled");
                        proceed = false;
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    proceed = false;
                }
            }

            if (proceed) {
                /*source.console().send("KarmaAPI region API needs ClassGraph. We will download it if its not downloaded yet", Level.GRAVE);
                BruteLoader loader = new BruteLoader((URLClassLoader) plugin.getClass().getClassLoader());

                URL url = URLUtils.getOrNull("https://repo1.maven.org/maven2/io/github/classgraph/classgraph/4.8.138/classgraph-4.8.138.jar");
                if (url != null) {
                    loader.downloadAndInject(
                            url,
                            NameComponent.forFile("ClassGraph", "jar")
                    );*/

                    dummy = new DummyListener(plugin);
                    blocks = new BlockDummyListener(plugin);
                    hooked = plugin;

                    manager.registerEvents(dummy, plugin);
                    manager.registerEvents(blocks, plugin);
               /*} else {
                    source.console().send("Failed to download ClassGraph because its download URL was null", Level.GRAVE);
                }*/
            }
        }

        regions.add(this);
    }

    /**
     * Get the region unique id
     *
     * @return the region unique id
     */
    public abstract UUID getUniqueId();

    /**
     * Get the region name
     *
     * @return the region name
     */
    public abstract String getName();

    /**
     * Get the region internal name
     *
     * @return the region internal name
     */
    public abstract String getInternalName();

    /**
     * Get if the region already exists
     *
     * @param plugin the region owner
     * @return if the region already exists
     */
    public abstract boolean exists(final KarmaPlugin plugin);

    /**
     * Get the region blocks
     *
     * @return the region blocks
     */
    public abstract Iterator<Block> getBlocks();

    /**
     * Get the region center location
     *
     * @return the region center location
     */
    public abstract Location getCenter();

    /**
     * Get the region size
     *
     * @return the region size
     */
    public abstract double getSize();

    /**
     * Get the region size
     *
     * @return the region size
     */
    public abstract double getSizeSquared();

    /**
     * Get the top location
     *
     * @return the top location
     */
    public abstract Location getTop();

    /**
     * Get the bottom location
     *
     * @return the bottom location
     */
    public abstract Location getBottom();

    /**
     * Get the region world
     *
     * @return the region world
     */
    public abstract World getWorld();

    /**
     * Get the amount of blocks inside the region
     *
     * @return the region blocks
     */
    public abstract int getBlocksAmount();

    /**
     * Get the region height
     *
     * @return the region height
     */
    public abstract int getHeight();

    /**
     * Get the region width
     *
     * @return the region width
     */
    public abstract int getWidth();

    /**
     * Get the region length
     *
     * @return the region length
     */
    public abstract int getLength();

    /**
     * Get if the entity is inside the
     * region
     *
     * @param entity the entity
     * @return if the entity is inside the
     * region
     */
    public abstract boolean isInside(final Entity entity);

    /**
     * Get if the block is inside the
     * region
     *
     * @param block the block
     * @return if the block is inside the
     * region
     */
    public abstract boolean isInside(final Block block);

    /**
     * Get if the entity is inside the
     * region
     *
     * @param entity the entity
     * @param marge the marge out of
     *              region frontier
     * @return if the entity is inside
     * the region
     */
    public abstract boolean isInside(final Entity entity, final double marge);

    /**
     * Get the region token, this is another
     * unique identifier for the region ( UUID
     * is also used as identifier ).
     *
     * The difference between this one and UUID one
     * is that this token is randomly generated, and
     * UUID name-base generated
     *
     * @return the region token
     */
    public abstract String getToken();

    /**
     * Get the top corners of the region
     *
     * @return the top region corners
     */
    public abstract Corner getTopCorners();

    /**
     * Get the bottom corners of the region
     *
     * @return the bottom region corners
     */
    public abstract Corner getBottomCorners();

    /**
     * Get the region walls
     *
     * @param type the wall type
     * @return the region walls
     */
    public abstract Wall getWalls(final WallType type);

    /**
     * Save the region to memory
     *
     * WARNING: THIS METHOD SHOULD CONTAIN
     * A {@link Cuboid#exists(KarmaPlugin)} CHECK
     * BEFORE BEING PROCESSED, OTHERWISE EXISTING
     * REGION WILL BE OVERWRITTEN
     *
     * @param owner the region owner
     */
    public abstract void saveToMemory(final KarmaPlugin owner);

    /**
     * Get all the regions
     *
     * @return the regions
     */
    public static Set<Cuboid> getRegions() {
        return regions;
    }
}