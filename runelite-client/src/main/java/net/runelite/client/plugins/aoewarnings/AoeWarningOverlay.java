/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.aoewarnings;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import static java.time.temporal.ChronoUnit.MILLIS;

public class AoeWarningOverlay extends Overlay
{
	private static final int FILL_START_ALPHA = 25;
	private static final int OUTLINE_START_ALPHA = 255;

	private final Client client;
	private final AoeWarningPlugin plugin;
	private final AoeWarningConfig config;

	@Inject
	public AoeWarningOverlay(@Nullable Client client, AoeWarningPlugin plugin, AoeWarningConfig config)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.UNDER_WIDGETS);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.enabled())
		{
			return null;
		}
		Instant now = Instant.now();
		Map<Projectile, AoeProjectile> projectiles = plugin.getProjectiles();
		for (Iterator<AoeProjectile> it = projectiles.values().iterator(); it.hasNext();)
		{
			AoeProjectile aoeProjectile = it.next();

			//Extra 200 MS is to compensate for the potential delay induced by the switch to game tick checking
			if (now.isAfter(aoeProjectile.getStartTime().plus(aoeProjectile.getAoeProjectileInfo().getLifeTime())
					.plus(300, MILLIS)))
			{
				it.remove();
				continue;
			}

			//Point point = Perspective.localToCanvas(client, aoeProjectile.getTargetPoint(), client.getPlane());
			/*
			graphics.setStroke(new BasicStroke(2));
			graphics.setColor(Color.RED);
			graphics.fillOval(
					point.getX(),
					point.getY(),
					10,
					10);
			*/
			Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, aoeProjectile.getTargetPoint(), aoeProjectile.getAoeProjectileInfo().getAoeSize());
			if (tilePoly == null)
			{
				continue;
			}

			// how far through the projectiles lifetime between 0-1.
			double progress = (System.currentTimeMillis() - aoeProjectile.getStartTime().toEpochMilli()) / (double) aoeProjectile.getAoeProjectileInfo().getLifeTime().toMillis();

			int fillAlpha, outlineAlpha;
			if (config.isFadeEnabled())
			{
				fillAlpha = (int) ((1 - progress) * FILL_START_ALPHA);//alpha drop off over lifetime
				outlineAlpha = (int) ((1 - progress) * OUTLINE_START_ALPHA);
			}
			else
			{
				fillAlpha = FILL_START_ALPHA;
				outlineAlpha = OUTLINE_START_ALPHA;
			}

			if (fillAlpha < 0)
			{
				fillAlpha = 0;
			}
			if (outlineAlpha < 0)
			{
				outlineAlpha = 0;
			}

			if (fillAlpha > 255)
			{
				fillAlpha = 255;
			}
			if (outlineAlpha > 255)
			{
				outlineAlpha = 255;//Make sure we don't pass in an invalid alpha
			}

			if (config.isOutlineEnabled())
			{
				graphics.setColor(new Color(255, 0, 0, outlineAlpha));
				graphics.drawPolygon(tilePoly);
			}

			graphics.setColor(new Color(255, 0, 0, fillAlpha));
			graphics.fillPolygon(tilePoly);
		}
		return null;
	}
}