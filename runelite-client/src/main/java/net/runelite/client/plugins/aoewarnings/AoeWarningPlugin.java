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

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "AoE projectile warning plugin",
	description = "Configuration for the AoE Projectile Warnings plugin",
	tags = {"combat", "pve", "overlay"}
)
public class AoeWarningPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	AoeWarningOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	AoeWarningConfig config;

	private final Map<Projectile, AoeProjectile> projectiles = new HashMap<>();


	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
	}
	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
	}

	@Provides
	AoeWarningConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AoeWarningConfig.class);
	}

	public Map<Projectile, AoeProjectile> getProjectiles()
	{
		return projectiles;
	}

	/**
	 * Called when a projectile is set to move towards a point. For
	 * projectiles that target the ground, like AoE projectiles from
	 * Lizardman Shamans, this is only called once
	 *
	 * //@param event Projectile moved event
	 *
	 * The event is currently broken for ground-targeted projectiles

	@Subscribe
	public void onProjectileMoved(ProjectileMoved event)
	{
		Projectile projectile = event.getProjectile();

		int projectileId = projectile.getId();
		AoeProjectileInfo aoeProjectileInfo = AoeProjectileInfo.getById(projectileId);
		if (aoeProjectileInfo != null && isConfigEnabledForProjectileId(projectileId))
		{
			LocalPoint targetPoint = event.getPosition();
			AoeProjectile aoeProjectile = new AoeProjectile(Instant.now(), targetPoint, aoeProjectileInfo);
			projectiles.put(projectile, aoeProjectile);
		}
	}
	 */

	@Subscribe
	public void onGameTick(GameTick event)
	{
		List<Projectile> projectileList = client.getProjectiles();
		if (projectileList.isEmpty())
		{
			return;
		}

		for (Projectile projectile : projectileList)
		{
			if (projectiles.containsKey(projectile)) {
				continue;
			}
			LocalPoint targetPoint = getEndTile(projectile);
			int projectileId = projectile.getId();
			AoeProjectileInfo aoeProjectileInfo = AoeProjectileInfo.getById(projectileId);
			if (aoeProjectileInfo != null && isConfigEnabledForProjectileId(projectileId))
			{
				AoeProjectile aoeProjectile = new AoeProjectile(Instant.now(), targetPoint, aoeProjectileInfo);
				projectiles.put(projectile, aoeProjectile);
			}
		}
	}

	private boolean isConfigEnabledForProjectileId(int projectileId)
	{
		AoeProjectileInfo projectileInfo = AoeProjectileInfo.getById(projectileId);
		if (projectileInfo == null)
		{
			return false;
		}

		switch (projectileInfo)
		{
			case LIZARDMAN_SHAMAN_AOE:
				return config.isShamansEnabled();
			case CRAZY_ARCHAEOLOGIST_AOE:
				return config.isArchaeologistEnabled();
			case ICE_DEMON_RANGED_AOE:
			case ICE_DEMON_ICE_BARRAGE_AOE:
				return config.isIceDemonEnabled();
			case VASA_AWAKEN_AOE:
			case VASA_RANGED_AOE:
				return config.isVasaEnabled();
			case TEKTON_METEOR_AOE:
				return config.isTektonEnabled();
			case VORKATH_BOMB:
			case VORKATH_POISON_POOL:
			case VORKATH_SPAWN:
			case VORKATH_TICK_FIRE:
				return config.isVorkathEnabled();
			case VETION_LIGHTNING:
				return config.isVetionEnabled();
			case CHAOS_FANATIC:
				return config.isChaosFanaticEnabled();
			case GALVEK_BOMB:
			case GALVEK_MINE:
				return config.isGalvekEnabled();
			case DAWN_FREEZE:
			case DUSK_CEILING:
				return config.isGargBossEnabled();
			case OLM_FALLING_CRYSTAL:
			case OLM_BURNING:
				return config.isOlmEnabled();
			case CORPOREAL_BEAST:
			case CORPOREAL_BEAST_DARK_CORE:
				return config.isCorpEnabled();
			case WINTERTODT_SNOW_FALL:
				return config.isWintertodtEnabled();
		}

		return false;
	}

	private LocalPoint getEndTile(Projectile projectile) {
		int cycleOffset = AoeProjectileInfo.getById(projectile.getId()).getCycleOffset();
		/*
		int endX = (int)Math.round((projectile.getX1() + (projectile.getVelocityX() *
				((projectile.getEndCycle() - projectile.getStartMovementCycle()) + 0))));
		int endY = (int)Math.round((projectile.getY1() + (projectile.getVelocityY() *
				((projectile.getEndCycle() - projectile.getStartMovementCycle()) + 0))));
		endX = Math.round(endX / (Perspective.LOCAL_TILE_SIZE)) * (Perspective.LOCAL_TILE_SIZE) +
				(Perspective.LOCAL_TILE_SIZE / 2);
		endY = Math.round(endY / (Perspective.LOCAL_TILE_SIZE)) * (Perspective.LOCAL_TILE_SIZE) +
				(Perspective.LOCAL_TILE_SIZE / 2);
		endX = endX + (Perspective.LOCAL_TILE_SIZE / 2);
		endY = endY + (Perspective.LOCAL_TILE_SIZE / 2);
		*/
		//double endX = (projectile.getVelocityX() * (projectile.getEndCycle() + 1 - client.getGameCycle())) + projectile.getX();
		//double endY = (projectile.getVelocityY() * (projectile.getEndCycle() + 1 - client.getGameCycle())) + projectile.getY();
		double endX = (projectile.getVelocityX() * (projectile.getEndCycle() + cycleOffset - projectile.getStartMovementCycle())) + projectile.getX1();
		double endY = (projectile.getVelocityY() * (projectile.getEndCycle() + cycleOffset - projectile.getStartMovementCycle())) + projectile.getY1();

		//endX = Math.round(endX / 128) * 128;
		//endY = Math.round(endY / 128) * 128;
		System.out.println("Before mod: endX: " + endX + ", endY: " + endY);
		endX = closestTile(endX);
		endY = closestTile(endY);
		System.out.println("End Cycle: " + + projectile.getEndCycle() + ", Current Cycle: " + client.getGameCycle() + ", endX: " + endX + ", endY: " + endY);
		LocalPoint endTile = new LocalPoint((int)endX, (int)endY);
		return endTile;
	}

	int closestTile(double coord) {
		long base = Math.round(coord / 128) * 128;
		long up = base + 64;
		long down = base - 64;
		long a = Math.min(Math.abs(up - coord), Math.abs(down - coord)) == Math.abs(up - coord) ? up : down;
		return (int)a;
	}
}