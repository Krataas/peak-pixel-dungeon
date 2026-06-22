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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Reaper;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

public class HealthBar extends Component {

	private static final int COLOR_BG	= 0xFF222222; // Dark gray background
	private static final int COLOR_HP	= 0xFFEE0000; // Red HP for player/boss
	private static final int COLOR_SHLD = 0xFFFFFFFF;
	private static final int COLOR_REAPER = 0x80000000; // Semi-transparent black overlay for Reaper death zone
	private static final int COLOR_BLEED = 0xFFFF0088; // Bright pink for bleeding (distinct from red HP)
	private static final int COLOR_POISON = 0xFFBB00FF; // Bright purple for poison
	private static final int COLOR_BURN = 0xFFFF8800; // Bright orange for burning
	private static final int COLOR_OOZE = 0xFF88FF00; // Bright lime green for ooze
	private static final int COLOR_CORRODE = 0xFFFFFF00; // Bright yellow for corrosion
	private static final int COLOR_DEFER = 0xFFFFFFFF; // Bright white for deferred damage
	
	private static final int HEIGHT	= 2;
	
	private ColorBlock Bg;
	private ColorBlock ReaperOverlay;
	private ColorBlock Shld;
	private ColorBlock Hp;
	private ColorBlock BleedOverlay;
	private ColorBlock PoisonOverlay;
	private ColorBlock BurnOverlay;
	private ColorBlock OozeOverlay;
	private ColorBlock CorrodeOverlay;
	private ColorBlock DeferOverlay;
	
	private float health;
	private float shield;
	private float reaperThreshold = -1f; // -1 means no Reaper active
	private float bleedPct = 0f;
	private float poisonPct = 0f;
	private float burnPct = 0f;
	private float oozePct = 0f;
	private float corrodePct = 0f;
	private float deferPct = 0f;
	
	@Override
	protected void createChildren() {
		Bg = new ColorBlock( 1, 1, COLOR_BG );
		add( Bg );

		Shld = new ColorBlock( 1, 1, COLOR_SHLD );
		add( Shld );
		
		Hp = new ColorBlock( 1, 1, COLOR_HP );
		add( Hp );

		// Damage overlays render on top
		BleedOverlay = new ColorBlock( 1, 1, COLOR_BLEED );
		add( BleedOverlay );
		
		PoisonOverlay = new ColorBlock( 1, 1, COLOR_POISON );
		add( PoisonOverlay );
		
		BurnOverlay = new ColorBlock( 1, 1, COLOR_BURN );
		add( BurnOverlay );
		
		OozeOverlay = new ColorBlock( 1, 1, COLOR_OOZE );
		add( OozeOverlay );
		
		CorrodeOverlay = new ColorBlock( 1, 1, COLOR_CORRODE );
		add( CorrodeOverlay );
		
		DeferOverlay = new ColorBlock( 1, 1, COLOR_DEFER );
		add( DeferOverlay );

		ReaperOverlay = new ColorBlock( 1, 1, COLOR_REAPER );
		add( ReaperOverlay );
		
		height = HEIGHT;
	}
	
	@Override
	protected void layout() {
		
		Bg.x = Shld.x = Hp.x = x;
		Bg.y = Shld.y = Hp.y = y;
		
		Bg.size( width, height );
		
		//logic here rounds up to the nearest pixel
		float pixelWidth = width;
		if (camera() != null) pixelWidth *= camera().zoom;
		Shld.size( width * (float)Math.ceil(shield * pixelWidth)/pixelWidth, height );
		Hp.size( width * (float)Math.ceil(health * pixelWidth)/pixelWidth, height );
		
		// Position Reaper death zone overlay (fills from left to threshold)
		if (reaperThreshold >= 0) {
			ReaperOverlay.visible = true;
			ReaperOverlay.x = x;
			ReaperOverlay.y = y;
			ReaperOverlay.size(width * reaperThreshold, height); // Fill from left to threshold
		} else {
			ReaperOverlay.visible = false;
		}
		
		// Position damage overlays starting from current HP position, extending right
		float rightEdge = x + (width * health); // Start from current HP end
		
		// Bleed overlay
		if (bleedPct > 0) {
			BleedOverlay.visible = true;
			float bleedWidth = width * bleedPct;
			BleedOverlay.x = rightEdge - bleedWidth;
			BleedOverlay.y = y;
			BleedOverlay.size(bleedWidth, height);
			rightEdge -= bleedWidth; // Next overlay starts here
		} else {
			BleedOverlay.visible = false;
		}
		
		// Poison overlay
		if (poisonPct > 0) {
			PoisonOverlay.visible = true;
			float poisonWidth = width * poisonPct;
			PoisonOverlay.x = rightEdge - poisonWidth;
			PoisonOverlay.y = y;
			PoisonOverlay.size(poisonWidth, height);
			rightEdge -= poisonWidth;
		} else {
			PoisonOverlay.visible = false;
		}
		
		// Burn overlay
		if (burnPct > 0) {
			BurnOverlay.visible = true;
			float burnWidth = width * burnPct;
			BurnOverlay.x = rightEdge - burnWidth;
			BurnOverlay.y = y;
			BurnOverlay.size(burnWidth, height);
			rightEdge -= burnWidth;
		} else {
			BurnOverlay.visible = false;
		}
		
		// Ooze overlay
		if (oozePct > 0) {
			OozeOverlay.visible = true;
			float oozeWidth = width * oozePct;
			OozeOverlay.x = rightEdge - oozeWidth;
			OozeOverlay.y = y;
			OozeOverlay.size(oozeWidth, height);
			rightEdge -= oozeWidth;
		} else {
			OozeOverlay.visible = false;
		}
		
		// Corrosion overlay
		if (corrodePct > 0) {
			CorrodeOverlay.visible = true;
			float corrodeWidth = width * corrodePct;
			CorrodeOverlay.x = rightEdge - corrodeWidth;
			CorrodeOverlay.y = y;
			CorrodeOverlay.size(corrodeWidth, height);
			rightEdge -= corrodeWidth;
		} else {
			CorrodeOverlay.visible = false;
		}
		
		// Deferred damage overlay
		if (deferPct > 0) {
			DeferOverlay.visible = true;
			float deferWidth = width * deferPct;
			DeferOverlay.x = rightEdge - deferWidth;
			DeferOverlay.y = y;
			DeferOverlay.size(deferWidth, height);
			rightEdge -= deferWidth;
		} else {
			DeferOverlay.visible = false;
		}
	}
	
	public void level( float value ) {
		level( value, 0f );
	}

	public void level( float health, float shield ){
		this.health = health;
		this.shield = shield;
		layout();
	}

	public void level(Char c){
		float health = c.HP;
		float shield = c.shielding();
		float max = Math.max(health+shield, c.HT);

		// Check for Reaper threshold
		Reaper reaper = c.buff(Reaper.class);
		if (reaper != null && reaper.stacks() > 0) {
			reaperThreshold = reaper.getThreshold();
		} else {
			reaperThreshold = -1f;
		}

		// Calculate damage from each buff
		float totalDamage = 0f;
		float bleedDmg = 0f, poisonDmg = 0f, burnDmg = 0f, oozeDmg = 0f, corrodeDmg = 0f, deferDmg = 0f;
		
		Bleeding bleed = c.buff(Bleeding.class);
		if (bleed != null) {
			bleedDmg = bleed.damage();
			totalDamage += bleedDmg;
		}
		
		Poison poison = c.buff(Poison.class);
		if (poison != null) {
			poisonDmg = poison.damage();
			totalDamage += poisonDmg;
		}
		
		Burning burn = c.buff(Burning.class);
		if (burn != null) {
			burnDmg = burn.damage();
			totalDamage += burnDmg;
		}
		
		Ooze ooze = c.buff(Ooze.class);
		if (ooze != null) {
			oozeDmg = ooze.damage();
			totalDamage += oozeDmg;
		}
		
		Corrosion corrode = c.buff(Corrosion.class);
		if (corrode != null) {
			corrodeDmg = corrode.damage();
			totalDamage += corrodeDmg;
		}
		
		Viscosity.DeferedDamage defer = c.buff(Viscosity.DeferedDamage.class);
		if (defer != null) {
			deferDmg = defer.damage();
			totalDamage += deferDmg;
		}
		
		// Calculate damage percentages accounting for shield absorption
		if (totalDamage > 0) {
			float shieldAbsorbed = Math.min(totalDamage, shield);
			float remainingDamage = totalDamage - shieldAbsorbed;
			
			if (remainingDamage > 0) {
				// Some damage gets through shield - show damage indicators proportionally
				float damageRatio = remainingDamage / totalDamage;
				bleedPct = (bleedDmg * damageRatio) / c.HT;
				poisonPct = (poisonDmg * damageRatio) / c.HT;
				burnPct = (burnDmg * damageRatio) / c.HT;
				oozePct = (oozeDmg * damageRatio) / c.HT;
				corrodePct = (corrodeDmg * damageRatio) / c.HT;
				deferPct = (deferDmg * damageRatio) / c.HT;
				
				// Reduce shield display by absorbed amount
				shield = Math.max(0, shield - shieldAbsorbed);
			} else {
				// Shield absorbs all damage - no damage indicators
				bleedPct = poisonPct = burnPct = oozePct = corrodePct = deferPct = 0f;
				
				// Reduce shield display by absorbed amount
				shield = shield - shieldAbsorbed;
			}
		} else {
			// No damage - clear all indicators
			bleedPct = poisonPct = burnPct = oozePct = corrodePct = deferPct = 0f;
		}

		level(health/max, (health+shield)/max);
	}
}
