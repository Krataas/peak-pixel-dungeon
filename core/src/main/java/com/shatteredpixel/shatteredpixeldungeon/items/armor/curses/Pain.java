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

package com.shatteredpixel.shatteredpixeldungeon.items.armor.curses;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * Pain curse - Similar to Viscosity but amplifies deferred damage by 1.5x
 * Damage is taken over time but with increased total damage (more deadly)
 */
public class Pain extends Armor.Glyph {
	
	private static ItemSprite.Glowing BLOOD_RED = new ItemSprite.Glowing( 0x8B0000 );
	
	@Override
	public int proc(Armor armor, Char attacker, Char defender, int damage) {
		
		// 10% chance to apply Pain effect when wearer is hit
		float procChance = 1/10f * procChanceMultiplier(defender);
		
		if (Random.Float() < procChance) {
			Buff.affect(defender, PainTracker.class).level = armor.buffedLvl();
		}
		
		return damage;
	}
	
	@Override
	public Glowing glowing() {
		return BLOOD_RED;
	}
	
	@Override
	public boolean curse() {
		return true;
	}
	
	/**
	 * Tracker buff - defers damage similar to Viscosity but amplifies it by 1.5x
	 */
	public static class PainTracker extends Buff {
		
		{
			actPriority = Actor.BUFF_PRIO - 1;
		}
		
		private int level = 0;
		
		/**
		 * Defers a portion of damage and amplifies it by 1.5x
		 * Returns the immediate damage to apply
		 */
		public int deferDamage(int dmg) {
			int level = Math.max(0, this.level);
			
		// Calculate deferred percentage (same formula as Viscosity)
		float percent = (level+1)/(float)(level+6);
		// Note: Can't use procChanceMultiplier here as PainTracker is a Buff, not Glyph
		// percent *= genericProcChanceMultiplier(target);  // REMOVED: method doesn't exist
			
			int deferredAmount = (int)Math.ceil(dmg * percent);
			
			if (deferredAmount > 0) {
				// Apply 1.5x multiplier to deferred damage
				int amplifiedDamage = (int)(deferredAmount * 1.5f);
				
				PainDamage deferred = Buff.affect(target, PainDamage.class);
				deferred.extend(amplifiedDamage);
				
				// Show visual feedback with amplified amount
				target.sprite.showStatus(CharSprite.WARNING, Messages.get(Pain.class, "deferred", amplifiedDamage));
			}
			
			detach();
			return dmg - deferredAmount; // Return immediate damage (original reduction)
		}
		
		private static final String LEVEL = "level";
		
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(LEVEL, level);
		}
		
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			level = bundle.getInt(LEVEL);
		}
	}
	
	/**
	 * Deferred damage buff - releases damage over time (10% per turn)
	 * Similar to Viscosity's DeferedDamage but for Pain curse
	 */
	public static class PainDamage extends Buff implements Hero.Doom {
		
		{
			type = buffType.NEGATIVE;
		}
		
		protected int damage = 0;
		
		public int damage() {
			return damage;
		}
		
		@Override
		public boolean attachTo(Char target) {
			if (super.attachTo(target)){
				postpone(TICK);
				return true;
			}
			return false;
		}
		
		public void extend(int amount) {
			damage += amount;
		}
		
		@Override
		public int icon() {
			return BuffIndicator.DEFERRED;
		}
		
		@Override
		public String iconTextDisplay() {
			return Integer.toString(damage);
		}
		
		@Override
		public boolean act() {
			if (target.isAlive()) {
				// Release 10% of pending damage per turn (minimum 1)
				int damageThisTick = Math.max(1, (int)(damage * 0.1f));
				target.damage(damageThisTick, this);
				
				if (target == Dungeon.hero && !target.isAlive()) {
					Badges.validateDeathFromFriendlyMagic();
					Dungeon.fail(this);
				}
				
				spend(TICK);
				damage -= damageThisTick;
				
				if (damage <= 0) {
					detach();
				}
			} else {
				detach();
			}
			
			return true;
		}
		
		@Override
		public void onDeath() {
			Badges.validateDeathFromFriendlyMagic();
			Dungeon.fail(this);
		}
		
		@Override
		public String desc() {
			return Messages.get(this, "desc", damage);
		}
		
		private static final String DAMAGE = "damage";
		
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(DAMAGE, damage);
		}
		
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			damage = bundle.getInt(DAMAGE);
		}
	}
}
