package net.runelite.client.plugins.vetion;

import com.google.inject.Provides;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@PluginDescriptor(
        name = "Vetion",
        description = "Tracks Vet'ion's special attacks",
        tags = {"bosses", "combat", "pve", "overlay"}
)
public class VetionPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private VetionConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private VetionOverlay overlay;

    @Getter
    private Map<Actor, Instant> vetions;

    @Provides
    VetionConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(VetionConfig.class);
    }

    @Override
    protected void startUp()
    {
        //eqTimes = new HashMap<>();
        vetions = new HashMap<>();
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        vetions = null;
        //eqTimes = null;
    }

    /*
    @Subscribe
    public void onNpcSpawned(NpcSpawned event)
    {
        NPC npc = event.getNpc();
        if (isNpcVetion(npc.getId()))
        {
            vetions.put(npc.getIndex(), npc);
        }
    }
    */

    @Subscribe
    public void onAnimationChanged(AnimationChanged event)
    {
        if (config.eartquakeTimerActive() && event.getActor().getAnimation() == AnimationID.VETION_EARTHQUAKE)
        {
            System.out.println("EQ");
            Actor vet = event.getActor();
            vetions.remove(vet, Instant.now());
            vetions.put(vet, Instant.now());
        }
        //System.out.println(event.getActor().getName() + ", Anim: " + event.getActor().getAnimation());
    }

    private boolean isNpcVetion(int npcId)
    {
        return npcId == NpcID.VETION ||
                npcId == NpcID.VETION_REBORN;
    }

}
