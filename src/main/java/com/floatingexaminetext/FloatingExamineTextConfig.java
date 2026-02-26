package com.floatingexaminetext;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.Color;

@ConfigGroup("floatingexaminetext")
public interface FloatingExamineTextConfig extends Config
{
    @ConfigItem(
        keyName = "popupDuration",
        name = "Popup Duration",
        description = "How long the popup stays visible in milliseconds"
    )
    default int popupDuration() { return 3000; }

    @ConfigItem(
        keyName = "fadeStartDelay",
        name = "Fade Delay",
        description = "How long the popup stays fully visible before fading"
    )
    default int fadeStartDelay() { return 2400; }

    @ConfigItem(
        keyName = "fontSize",
        name = "Font Size",
        description = "Size of the popup font"
    )
    default int fontSize() { return 16; }

    @ConfigItem(
        keyName = "fontColor",
        name = "Font Color",
        description = "Color of the popup text"
    )
    default Color fontColor() { return Color.WHITE; }

    @ConfigItem(
        keyName = "yOffset",
        name = "Y Offset",
        description = "Vertical offset of popup text"
    )
    default int yOffset() { return -10; }
}
