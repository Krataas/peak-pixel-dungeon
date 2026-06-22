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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.utils.Random;

/**
 * Holy - Rare weapon enchantment
 * Small chance to grant Blessed buff (6 turns) on attack
 * Deals bonus damage (1-3) against demon enemies
 * Scales with weapon upgrades, Ring of Arcana, and talents
 */
public class Holy extends Weapon.Enchantment {
	
	private static ItemSprite.Glowing GOLD = new ItemSprite.Glowing( 0xFFD700 );
	
	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		
		int level = Math.max( 0, weapon.buffedLvl() );
		
		// Base proc chance: 10% + 1% per level, scales with Ring of Arcana
		float procChance = (0.10f + 0.01f * level) * procChanceMultiplier(attacker);
		
		if (Random.Float() < procChance) {
			// Apply Blessed buff to attacker for 6 turns
			Buff.affect(attacker, Bless.class, 6f);
		}
		
		// Check if defender is a demon and deal bonus damage
		// In Shattered PD, demons typically have "demon" or "demonic" in their properties
		// We check alignment and specific mob types that are demonic
		if (isDemon(defender)) {
			// Bonus damage: 1-3, scales with weapon level
			int bonusDamage = Random.IntRange(1, 3 + level / 3);
			damage += bonusDamage;
		}
		
		return damage;
	}
	
	/**
	 * Check if a character is a demon
	 * This checks for common demon types in Shattered Pixel Dungeon
	 */
	private boolean isDemon(Char ch) {
		if (ch == null) return false;
		
		// Check class name for demon-type enemies
		String className = ch.getClass().getSimpleName().toLowerCase();
		
		// Common demon types in Shattered PD
		return className.contains("demon") 
			|| className.contains("succubus")
			|| className.contains("scorpio") // Demon spawner boss
			|| className.contains("yog") // Demon god final boss
			|| className.contains("burning") // Burning Fist (demonic)
			|| className.contains("rotting"); // Rotting Fist (demonic)
	}
	
	@Override
	public Glowing glowing() {
		return GOLD;
	}
}
