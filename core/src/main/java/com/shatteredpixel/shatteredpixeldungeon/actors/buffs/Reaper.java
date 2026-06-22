/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

/**
 * Reaper debuff - causes instant death if HP falls below a threshold
 * Threshold increases with stacks, lasts 15 turns per stack
 */
public class Reaper extends Buff implements Hero.Doom {
	
	{
		type = buffType.NEGATIVE;
		announced = true;
	}
	
	protected int stacks = 0;      // Number of stacks (affects threshold, doesn't decrement)
	protected int duration = 0;    // Remaining turns (decrements each turn)
	
	private static final float BASE_THRESHOLD = 0.25f; // 25% base
	private static final float THRESHOLD_PER_STACK = 0.05f; // +5% per stack
	private static final float MAX_THRESHOLD = 0.75f; // Cap at 75%
	private static final int DURATION_PER_STACK = 10; // 10 turns per stack
	
	@Override
	public boolean attachTo(Char target) {
		if (super.attachTo(target)) {
			// Schedule first action for next turn to ensure buff is visible
			spend(TICK);
			return true;
		}
		return false;
	}
	
	/**
	 * Set stacks to a specific level (only if higher) and add corresponding duration
	 */
	public void set(int level) {
		if (this.stacks < level) {
			int stacksToAdd = level - this.stacks;
			this.stacks = level;
			this.duration += stacksToAdd * DURATION_PER_STACK;
		}
	}
	
	/**
	 * Add stacks and corresponding duration (like Bleed)
	 */
	public void extend(int amount) {
		this.stacks += amount;
		this.duration += amount * DURATION_PER_STACK;
	}
	
	/**
	 * Get current stack count
	 */
	public int stacks() {
		return stacks;
	}
	
	/**
	 * Calculate HP threshold for instant death based on stacks
	 * Bosses have 75% reduced threshold (making Reaper less effective)
	 */
	public float getThreshold() {
		float threshold = BASE_THRESHOLD + (stacks * THRESHOLD_PER_STACK);
		threshold = Math.min(threshold, MAX_THRESHOLD); // Cap at max
		
		// Nerf Reaper against bosses (75% reduction)
		if (target != null && target.properties().contains(com.shatteredpixel.shatteredpixeldungeon.actors.Char.Property.BOSS)) {
			threshold *= 0.25f; // 75% reduction (multiply by 0.25)
		}
		
		return threshold;
	}
	
	@Override
	public boolean act() {
		if (target.isAlive()) {
			
			// Check if target's HP is below the death threshold
			float hpPercent = target.HP / (float)target.HT;
			float threshold = getThreshold();
			
			if (hpPercent <= threshold && stacks > 0) {
				// Instant death - deal damage equal to current HP
				target.damage(target.HP, this);
				
				// Visual effect when Reaper kills (similar to Grim execution)
				if (!target.isAlive()) {
					target.sprite.emitter().burst(ShadowParticle.UP, 5);
				}
				
				if (!target.isAlive() && target == Dungeon.hero) {
					// Death message will be handled by Hero.Doom.onDeath()
					Dungeon.fail(this);
				}
			}
			
			// Decrease duration each turn (stacks remain constant)
			duration--;
			
			if (duration <= 0) {
				detach();
			}
			
			spend(TICK);
		} else {
			detach();
		}
		
		return true;
	}
	
	@Override
	public void onDeath() {
		Badges.validateDeathFromGrimOrDisintTrap();
		Dungeon.fail(this);
		GLog.n(Messages.get(this, "ondeath"));
	}
	
	@Override
	public int icon() {
		return BuffIndicator.REAPER; // Position 86 - sprite needs to be added to buffs.png
	}
	
	@Override
	public String iconTextDisplay() {
		return Integer.toString(stacks) + ":" + Integer.toString(duration);
	}
	
	@Override
	public float iconFadePercent() {
		// Show fade based on remaining duration
		int maxDuration = stacks * DURATION_PER_STACK;
		if (maxDuration == 0) return 0;
		return Math.max(0, (maxDuration - duration) / (float)maxDuration);
	}
	
	@Override
	public String desc() {
		// Show actual threshold (accounts for boss reduction)
		float threshold = getThreshold() * 100f;
		return Messages.get(this, "desc", Math.round(threshold * 100f) / 100f, stacks, duration);
	}
	
	private static final String STACKS = "stacks";
	private static final String DURATION = "duration";
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(STACKS, stacks);
		bundle.put(DURATION, duration);
	}
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		stacks = bundle.getInt(STACKS);
		duration = bundle.getInt(DURATION);
	}
}
