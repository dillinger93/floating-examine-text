package com.floatingexaminetext;

import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ClientTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Provides;

@PluginDescriptor(
    name = "Floating Examine Text",
    description = "Shows examine text directly on examined objects or NPCs",
    tags = {"examine", "overlay", "qol", "chat"}
)

@Singleton
public class FloatingExamineTextPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private FloatingTextOverlay overlay;

    @Inject
    private FloatingExamineTextConfig config;

    @Inject
    private ItemManager itemManager;

    private String pendingText;
    private LocalPoint pendingLocalPoint;

    private final List<FloatingPopup> popups = new ArrayList<>();

    @Provides
    FloatingExamineTextConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(FloatingExamineTextConfig.class);
    }

    @Override
    protected void startUp()
    {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        popups.clear();
        pendingText = null;
        pendingLocalPoint = null;
    }

    public List<FloatingPopup> getPopups()
    {
        return popups;
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        MenuAction action = event.getMenuAction();

        if (action == MenuAction.EXAMINE_OBJECT)
        {
            pendingLocalPoint = LocalPoint.fromScene(
                event.getParam0(),
                event.getParam1(),
                client.getTopLevelWorldView()
            );
            return;
        }

        var actor = event.getMenuEntry().getActor();
        if (actor != null && event.getMenuOption().equalsIgnoreCase("Examine"))
        {
            pendingLocalPoint = actor.getLocalLocation();
            return;
        }

        if (action == MenuAction.EXAMINE_ITEM_GROUND)
        {
            int sceneX = event.getParam0();
            int sceneY = event.getParam1();
            pendingLocalPoint = LocalPoint.fromScene(sceneX, sceneY, client.getTopLevelWorldView());

            Tile tile = client.getScene().getTiles()[client.getPlane()][sceneX][sceneY];
            if (tile != null)
            {
                List<TileItem> items = tile.getGroundItems();
                if (items != null && !items.isEmpty())
                {
                    TileItem tileItem = items.get(0);
                    int itemId = tileItem.getId();
                    int qty = tileItem.getQuantity();
                    pendingText = itemManager.getItemComposition(itemId).getName() +
                                  (qty > 1 ? " x" + qty : "");
                }
            }
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        if (pendingLocalPoint == null)
        {
            return;
        }

        pendingText = Text.removeTags(event.getMessage());
        commitPopup();
    }

    private void commitPopup()
    {
        if (pendingText == null || pendingLocalPoint == null)
        {
            return;
        }

        popups.add(new FloatingPopup(pendingText, pendingLocalPoint));

        pendingText = null;
        pendingLocalPoint = null;
    }

    @Subscribe
    public void onClientTick(ClientTick tick)
    {
        long lifetime = config.popupDuration();
        popups.removeIf(p -> p.getAgeMillis() > lifetime);
    }
}
