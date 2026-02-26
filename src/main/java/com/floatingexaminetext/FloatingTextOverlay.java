package com.floatingexaminetext;

import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.FontManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.Font;

@Singleton
public class FloatingTextOverlay extends Overlay
{
    private final FloatingExamineTextPlugin plugin;
    private final Client client;
    private final FloatingExamineTextConfig config;
    private final FontManager fontManager;

    @Inject
    public FloatingTextOverlay(
        FloatingExamineTextPlugin plugin,
        Client client,
        FloatingExamineTextConfig config,
        FontManager fontManager
    )
    {
        this.plugin = plugin;
        this.client = client;
        this.config = config;
        this.fontManager = fontManager;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D g)
    {
        Font baseFont = fontManager.getRunescapeSmallFont();
        g.setFont(baseFont.deriveFont((float) config.fontSize()));

        for (FloatingPopup popup : plugin.getPopups())
        {
            LocalPoint lp = popup.getLocalPoint();
            if (lp == null)
                continue;

            Point canvas = Perspective.getCanvasTextLocation(client, g, lp, popup.getText(), 0);
            if (canvas == null)
                continue;

            long age = popup.getAgeMillis();
            float alpha = computeAlpha(age);
            if (alpha <= 0f)
                continue;

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            int yOffset = computeFloatOffset(age);

            OverlayUtil.renderTextLocation(
                g,
                new Point(canvas.getX(), canvas.getY() + config.yOffset() - yOffset),
                popup.getText(),
                config.fontColor()
            );
        }

        g.setComposite(AlphaComposite.SrcOver);
        return null;
    }

    private float computeAlpha(long age)
    {
        long totalLifetime = config.popupDuration();
        long fadeDelay = config.fadeStartDelay();

        if (age <= fadeDelay)
            return 1f;

        long fadeDuration = totalLifetime - fadeDelay;
        if (fadeDuration <= 0)
            return 0f;

        float t = (float)(age - fadeDelay) / fadeDuration;
        t = Math.max(0f, Math.min(1f, t));

        return (float) Math.pow(1f - t, 2);
    }

    private int computeFloatOffset(long age)
    {
        long fadeDelay = config.fadeStartDelay();
        long totalLifetime = config.popupDuration();
        long fadeDuration = totalLifetime - fadeDelay;

        if (age <= fadeDelay || fadeDuration <= 0)
            return 0;

        float t = (float)(age - fadeDelay) / fadeDuration;
        t = Math.max(0f, Math.min(1f, t));

        return (int)(t * 10);
    }
}
