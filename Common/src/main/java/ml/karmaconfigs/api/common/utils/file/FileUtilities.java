package ml.karmaconfigs.api.common.utils.file;

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
import ml.karmaconfigs.api.common.utils.string.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Collections;
import java.util.List;

/**
 * Karma file utilities
 */
public class FileUtilities {

    /**
     * Enable debug
     */
    public static boolean DEBUG = true;

    /**
     * Create a file
     *
     * @param file the file to create
     */
    public static void create(final @NotNull File file) {
        if (!file.isDirectory())
            try {
                if (!file.getParentFile().exists()) {
                    Files.createDirectories(file.getParentFile().toPath());
                    if (DEBUG)
                        APISource.getConsole().send("&b[ KarmaAPI ] &3Created directory {0}", getPrettyFile(file.getParentFile()));
                }
                if (!file.exists()) {
                    Files.createFile(file.toPath());
                    if (DEBUG)
                        APISource.getConsole().send("&b[ KarmaAPI ] &3Created file {0}", getPrettyFile(file));
                }
            } catch (Throwable ignored) {
            }
    }

    /**
     * Create a file and catch any exception
     *
     * @param file the file to create
     * @throws IOException any exception
     */
    public static void createWithException(final @NotNull File file) throws IOException {
        if (!file.isDirectory()) {
            if (!file.getParentFile().exists()) {
                Files.createDirectories(file.getParentFile().toPath());
                if (DEBUG)
                    APISource.getConsole().send("&b[ KarmaAPI ] &3Created directory {0}", getPrettyFile(file.getParentFile()));
            }
            if (!file.exists()) {
                Files.createFile(file.toPath());
                if (DEBUG)
                    APISource.getConsole().send("&b[ KarmaAPI ] &3Created file {0}", getPrettyFile(file));
            }
        }
    }

    /**
     * Create a file and return if the file
     * could be created
     *
     * @param file the file to create
     * @return if the file could be created
     */
    public static boolean createWithResults(final @NotNull File file) {
        if (!file.isDirectory())
            try {
                if (!file.getParentFile().exists()) {
                    Files.createDirectories(file.getParentFile().toPath());
                    if (DEBUG)
                        APISource.getConsole().send("&b[ KarmaAPI ] &3Created directory {0}", getPrettyFile(file.getParentFile()));
                }
                if (!file.exists()) {
                    Files.createFile(file.toPath());
                    if (DEBUG)
                        APISource.getConsole().send("&b[ KarmaAPI ] &3Created file {0}", getPrettyFile(file));
                    return true;
                }
            } catch (Throwable ignored) {
            }
        return false;
    }

    /**
     * Get if the file is a karma file
     *
     * @param file the file
     * @return if the file is a karma file
     */
    public static boolean isKarmaFile(final File file) {
        try {
            UserDefinedFileAttributeView view = Files.<UserDefinedFileAttributeView>getFileAttributeView(file.toPath(), UserDefinedFileAttributeView.class);
            ByteBuffer buf = ByteBuffer.allocate(view.size("filetp"));
            view.read("filetp", buf);
            buf.flip();
            return Charset.defaultCharset().decode(buf).toString().equals("KarmaFile");
        } catch (Throwable ex) {
            return false;
        }
    }

    /**
     * Get if the file is valid
     *
     * @param file the file
     * @return if the file is a valid file
     */
    public static boolean isValidFile(final File file) {
        try {
            String filePath = file.getCanonicalPath();
            return !StringUtils.isNullOrEmpty(filePath);
        } catch (Throwable ex) {
            return false;
        }
    }

    /**
     * Get if the file is a valid file
     *
     * @param path the file
     * @return if the file is a valid file
     */
    public static boolean isValidFile(final String path) {
        File file = new File(path);
        try {
            String filePath = file.getCanonicalPath();
            return !StringUtils.isNullOrEmpty(filePath);
        } catch (Throwable ex) {
            return false;
        }
    }

    /**
     * Get the file
     *
     * @param file the file
     * @return the file
     */
    public static String getParentFile(final @NotNull File file) {
        return getParentFile(file, ' ');
    }

    /**
     * Get the file path
     *
     * @param file the file path
     * @return the file path
     */
    public static String getFile(final File file) {
        return getFile(file, ' ');
    }

    /**
     * Get the pretty file
     *
     * @param file the file
     * @return the pretty file
     */
    public static String getPrettyParentFile(final @NotNull File file) {
        return getParentFile(file, '/');
    }

    /**
     * Get the pretty file path
     *
     * @param file the file
     * @return the pretty file path
     */
    public static String getPrettyFile(final File file) {
        return getFile(file, '/');
    }

    /**
     * Get the parent file replacing %20 ( space char )
     *
     * @param file the file
     * @param barReplace the %20 replace
     * @return the parent file
     */
    public static String getParentFile(final @NotNull File file, final char barReplace) {
        if (Character.isSpaceChar(barReplace))
            return file.getParentFile().getAbsolutePath().replaceAll("%20", " ");

        return file.getParentFile().getAbsolutePath().replaceAll("%20", " ").replace(File.separatorChar, barReplace);
    }

    /**
     * Get the file replacing the %20 ( space char )
     *
     * @param file the file
     * @param barReplace the %20 replace
     * @return the file
     */
    public static String getFile(final @NotNull File file, final char barReplace) {
        if (Character.isSpaceChar(barReplace))
            return file.getAbsolutePath().replaceAll("%20", " ");

        return file.getAbsolutePath().replaceAll("%20", " ").replace(File.separatorChar, barReplace);
    }

    /**
     * Get the file extension
     *
     * @param file the file
     * @return the file extension
     */
    public static String getExtension(final @NotNull File file) {
        if (!file.isDirectory() && file.getName().contains(".")) {
            String name = file.getName();
            String[] nameData = name.split("\\.");
            return nameData[nameData.length - 1];
        }
        return "dir";
    }

    /**
     * Get the file extension
     *
     * @param name the file name
     * @return the file extension
     */
    public static String getExtension(final @NotNull String name) {
        if (name.contains(".")) {
            String[] nameData = name.split("\\.");
            return nameData[nameData.length - 1];
        }
        return "";
    }

    /**
     * Get the file name
     *
     * @param file the file name
     * @param extension include extension
     * @return the file name
     */
    public static String getName(final @NotNull File file, final boolean extension) {
        if (extension)
            return file.getName();

        return StringUtils.replaceLast(file.getName(), "." + getExtension(file), "");
    }

    /**
     * Get the file type
     *
     * @param file the file tpy
     * @return the file type
     */
    public static String getFileType(final File file) {
        try {
            String mimetype = Files.probeContentType(file.toPath());
            if (mimetype != null) {
                if (mimetype.contains("/"))
                    return mimetype.split("/")[0];
                return mimetype;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * Get the file complete type
     *
     * @param file the file complete type
     * @return the file type
     */
    public static String getFileCompleteType(final File file) {
        if (!file.isDirectory()) {
            try {
                String mimetype = Files.probeContentType(file.toPath());
                if (mimetype != null)
                    return mimetype;
            } catch (Throwable ignored) {
            }

            return getExtension(file);
        }

        return "directory";
    }

    /**
     * Read all the file lines
     *
     * @param file the file
     * @return all the file lines
     */
    public static List<String> readAllLines(final File file) {
        try {
            return Files.readAllLines(getFixedFile(file).toPath());
        } catch (Throwable ex) {
            return Collections.emptyList();
        }
    }

    /**
     * Get the karma source jar file
     *
     * @param source the karma source
     * @return the source jar file
     */
    public static File getSourceFile(final KarmaSource source) {
        File file = new File(source.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
        return getFixedFile(file);
    }

    /**
     * Fix a file
     *
     * @param file the file
     * @return the fixed file
     */
    public static File getFixedFile(final @NotNull File file) {
        return new File(getFile(file, ' '));
    }

    /**
     * Get the project folder
     *
     * @param delimiter the project complete path delimiter
     * @return the project folder
     */
    public static File getProjectFolder(final String delimiter) {
        File jar = getFixedFile(new File(FileUtilities.class.getProtectionDomain().getCodeSource().getLocation().getPath()));
        File folder = getFixedFile(new File(jar.getAbsolutePath().replace(jar.getName(), "")));

        if (!StringUtils.isNullOrEmpty(delimiter)) {
            if (!folder.getName().equals(delimiter)) {
                String path = folder.getAbsolutePath();
                if (path.contains(delimiter)) {
                    String[] path_data = path.split(File.separator.replace("\\", "\\\\"));
                    int plugins_amount = 0;
                    for (String data : path_data) {
                        if (data.equals(delimiter))
                            plugins_amount++;
                    }
                    int plugins_count = 0;
                    StringBuilder builder = new StringBuilder();
                    for (String data : path_data) {
                        if (data.equals(delimiter))
                            plugins_count++;
                        builder.append(data).append(File.separatorChar);
                        if (plugins_count >= plugins_amount)
                            break;
                    }
                    return getFixedFile(new File(builder.toString()));
                }
            }
        }

        return getFixedFile(folder);
    }

    /**
     * Get the project parent folder
     *
     * @return the project parent folder
     */
    public static File getProjectParent() {
        return getFixedFile(APISource.getSource().getDataPath().toFile().getParentFile());
    }

    /**
     * Get the project folder
     *
     * @param source the source
     * @return the project folder
     * @deprecated Use {@link KarmaSource#getDataPath()} then {@link Path#toFile()} instead
     */
    @Deprecated
    public static File getProjectFolder(final KarmaSource source) {
        return getFixedFile(source.getDataPath().toFile());
    }
}
