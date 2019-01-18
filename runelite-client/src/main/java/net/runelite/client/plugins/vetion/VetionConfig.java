package net.runelite.client.plugins.vetion;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("vetion")
public interface VetionConfig extends Config
{
    @ConfigItem(
            keyName = "earthquakeTimerActive",
            name = "Vet'ion Earthquake Timer",
            description = "Configures whether or not a timer is shown to track the cooldown of Vet'ion's earthquake attack",
            position = 0
    )
    default boolean eartquakeTimerActive()
    {
        return true;
    }
}