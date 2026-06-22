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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.utils.Random;

public class Dreadful extends Weapon.Enchantment {

	private static ItemSprite.Glowing DARK_PURPLE = new ItemSprite.Glowing( 0x6A0DAD );
	
	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		int level = Math.max( 0, weapon.buffedLvl() );

		// lvl 0 - 20%
		// lvl 1 - 33%
		// lvl 2 - 43%
		float procChance = (level+1f)/(level+5f) * procChanceMultiplier(attacker);
		
		if (Random.Float() < procChance && !defender.isImmune(Terror.class)) {

			float powerMulti = Math.max(1f, procChance);

			// Base duration of 10 turns, scales with proc chance
			float duration = 10f * powerMulti;
			
			// Cap at Terror.DURATION (20f)
			duration = Math.min(duration, Terror.DURATION);
			
			Terror existing = defender.buff(Terror.class);
			if (existing != null){
				// If already terrified, add to existing duration up to cap
				duration = Math.min(duration, Terror.DURATION - existing.cooldown());
			}

			if (duration > 0) {
				Terror terror = Buff.affect(defender, Terror.class, duration);
				terror.object = attacker.id();
			}
			
			// Dark purple splash effect
			Splash.at( defender.sprite.center(), 0x9D4EDD, 5);
		}

		return damage;
	}
	
	@Override
	public Glowing glowing() {
		return DARK_PURPLE;
	}

}
