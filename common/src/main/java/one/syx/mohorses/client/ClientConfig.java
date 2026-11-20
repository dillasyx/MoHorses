package one.syx.mohorses.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

/// The client-side settings in `config/mohorses-client.properties`, the same file on every loader. Read once; a
/// missing file is written with the defaults and what each setting means.
public final class ClientConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FILE = "mohorses-client.properties";

    /// When a full amulet shows the animal inside in its tooltip
    public enum AmuletPreview {
        /// While Alt is held (a hint line says so otherwise)
        ALT,
        /// Always
        ALWAYS,
        /// Never
        NEVER
    }

    private static AmuletPreview amuletPreview;

    private ClientConfig() {
    }

    public static AmuletPreview amuletPreview() {
        if (amuletPreview == null)
            load();

        return amuletPreview;
    }

    private static void load() {
        amuletPreview = AmuletPreview.ALT;
        Path file = Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(FILE);

        if (!Files.exists(file)) {
            write(file);

            return;
        }

        Properties properties = new Properties();

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
            amuletPreview = AmuletPreview.valueOf(properties.getProperty("amuletPreview", "alt").trim().toUpperCase(Locale.ROOT));
        }
        catch (IOException | IllegalArgumentException e) {
            LOGGER.warn("Could not read {}, using the defaults", file, e);
        }
    }

    private static void write(Path file) {
        try {
            Files.createDirectories(file.getParent());

            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                writer.write("""
                        # Mo' Horses client settings

                        # When a full amulet shows the animal inside in its tooltip:
                        #   alt    = while Alt is held (default)
                        #   always = always
                        #   never  = never
                        amuletPreview=alt
                        """);
            }
        }
        catch (IOException e) {
            LOGGER.warn("Could not write {}", file, e);
        }
    }
}
