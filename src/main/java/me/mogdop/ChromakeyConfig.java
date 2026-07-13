package me.mogdop;

import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ChromakeyConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("mogdops-chromakey-mod.properties");
    
    // Параметр конфига. По умолчанию равен false (сообщения не показываются)
    public static boolean showColorChangeMessage = false;

    public static void load() {
        Properties properties = new Properties();
        if (Files.exists(CONFIG_PATH)) {
            try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
                properties.load(in);
                showColorChangeMessage = Boolean.parseBoolean(properties.getProperty("showColorChangeMessage", "false"));
            } catch (IOException e) {
                ChromakeyMod.LOGGER.error("Failed to load config", e);
            }
        } else {
            save(); // Если файла нет, создаем его со значениями по умолчанию
        }
    }

    public static void save() {
        Properties properties = new Properties();
        properties.setProperty("showColorChangeMessage", String.valueOf(showColorChangeMessage));
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(out, "MogDop's Chromakey Mod Configuration");
            }
        } catch (IOException e) {
            ChromakeyMod.LOGGER.error("Failed to save config", e);
        }
    }
}