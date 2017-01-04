package tc.oc.bukkit.logging;

import java.util.logging.Logger;
import javax.annotation.Nullable;

import net.kencochrane.raven.log4j2.SentryAppender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.bukkit.plugin.java.JavaPlugin;
import tc.oc.minecraft.logging.BetterRaven;
import tc.oc.minecraft.logging.RavenConfiguration;

public class RavenPlugin extends JavaPlugin {

    private RavenConfiguration configuration;

    private RavenConfiguration getRavenConfiguration() {
        if(configuration == null) {
            configuration = new BukkitRavenConfiguration(getConfig());
        }
        return configuration;
    }

    private BetterRaven raven;

    public @Nullable BetterRaven getRaven() {
        if(raven == null && getRavenConfiguration().enabled()) {
            getLogger().info("Installing Raven");
            buildRaven();
            installBukkit();
            installMojang();
        }
        return raven;
    }

    private void buildRaven() {
        this.raven = new BukkitRavenFactory().createRavenInstance(getRavenConfiguration());
    }

    private void installBukkit() {
        raven.listen(Logger.getLogger(""));
    }

    private void installMojang() {
        org.apache.logging.log4j.Logger mojangLogger = LogManager.getRootLogger();
        if(mojangLogger instanceof org.apache.logging.log4j.core.Logger) {
            // No way to do this through the public API
            SentryAppender appender = new SentryAppender(this.raven);
            appender.addFilter(ThresholdFilter.createFilter("ERROR", "NEUTRAL", "DENY"));
            appender.addFilter(new ForwardedEventFilter());
            appender.start();
            ((org.apache.logging.log4j.core.Logger) mojangLogger).addAppender(appender);
        } else {
            this.getLogger().warning("Raven cannot integrate with Mojang logger with unexpected class " + mojangLogger.getClass());
        }
    }
}
