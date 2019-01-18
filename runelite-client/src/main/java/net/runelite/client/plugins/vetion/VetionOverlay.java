package net.runelite.client.plugins.vetion;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;

import javax.inject.Inject;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;

public class VetionOverlay extends Overlay{

    private static final Color RED_ALPHA = new Color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue(), 100);
    private static final Duration MAX_TIME = Duration.ofSeconds(9);
    private final VetionPlugin plugin;
    private Client client;

    @Inject
    private VetionOverlay(Client client, VetionPlugin plugin)
    {
        this.plugin = plugin;
        this.client = client;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        plugin.getVetions().forEach((actor, timer) ->
        {
            LocalPoint localPos = actor.getLocalLocation();
            if (localPos != null)
            {
                Point position = Perspective.localToCanvas(client, localPos, client.getPlane(),
                        actor.getLogicalHeight() + 16);
                if (position != null)
                {
                    position = new Point(position.getX(), position.getY());

                    final ProgressPieComponent progressPie = new ProgressPieComponent();
                    progressPie.setDiameter(25);
                    progressPie.setFill(RED_ALPHA);
                    progressPie.setBorderColor(Color.RED);
                    progressPie.setPosition(position);

                    final Duration duration = Duration.between(timer, Instant.now());
                    progressPie.setProgress(1 - (duration.compareTo(MAX_TIME) < 0
                            ? (double) duration.toMillis() / MAX_TIME.toMillis()
                            : 1));

                    progressPie.render(graphics);
                }
            }
        });

        return null;
    }
}