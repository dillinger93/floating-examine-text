package com.floatingexaminetext;

import net.runelite.api.coords.LocalPoint;

public class FloatingPopup
{
    private final String text;
    private final LocalPoint localPoint;
    private final long startTime;

    public FloatingPopup(String text, LocalPoint localPoint)
    {
        this.text = text;
        this.localPoint = localPoint;
        this.startTime = System.currentTimeMillis();
    }

    public String getText()
    {
        return text;
    }

    public LocalPoint getLocalPoint()
    {
        return localPoint;
    }

    public long getAgeMillis()
    {
        return System.currentTimeMillis() - startTime;
    }
}
