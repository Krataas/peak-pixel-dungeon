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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Reaper;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.utils.Random;

/**
 * ReaperCurse - Armor curse that applies Reaper stacks when wearer is hit
 * The curse itself is permanent (lasts as long as armor is cursed)
 * Each hit applies 1 Reaper stack to the wearer
 */
public class ReaperCurse extends Armor.Glyph {
	
	private static ItemSprite.Glowing DARK_RED = new ItemSprite.Glowing( 0x4A0000 );
	
	@Override
	public int proc(Armor armor, Char attacker, Char defender, int damage) {
		
		// 10% chance to apply 1 Reaper stack when wearer is hit
		// Scales with Ring of Arcana via procChanceMultiplier
		float procChance = 1/10f * procChanceMultiplier(defender);
		
		if (Random.Float() < procChance) {
			Reaper reaper = Buff.affect(defender, Reaper.class);
			reaper.extend(1); // Add 1 stack
		}
		
		return damage;
	}
	
	@Override
	public Glowing glowing() {
		return DARK_RED;
	}
	
	@Override
	public boolean curse() {
		return true;
	}
}
