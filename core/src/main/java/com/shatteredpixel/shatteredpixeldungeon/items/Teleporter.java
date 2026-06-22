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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AlchemistsToolkit;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfArcana;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfHaste;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatsword;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.SummoningTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.DeviceCompat;

import java.util.ArrayList;

/**
 * Debug item for testing - only available in INDEV mode
 * Provides quick access to common testing functions
 */
public class Teleporter extends Item {

	private static final String AC_DESCEND = "DESCEND";
	private static final String AC_ASCEND = "ASCEND";
	private static final String AC_REVEAL = "REVEAL";
	private static final String AC_LEVELUP = "LEVELUP";
	private static final String AC_SPAWN = "SPAWN";
	private static final String AC_GOLD = "GOLD";
	private static final String AC_ITEMS = "ITEMS";

	{
		image = ItemSpriteSheet.SCROLL_HOLDER;
		unique = true;
		defaultAction = AC_REVEAL;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_DESCEND);
		actions.add(AC_ASCEND);
		actions.add(AC_REVEAL);
		actions.add(AC_LEVELUP);
		actions.add(AC_SPAWN);
		actions.add(AC_GOLD);
		actions.add(AC_ITEMS);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (!DeviceCompat.isDebug()) {
			GLog.w(Messages.get(this, "not_debug"));
			return;
		}

		curUser = hero;

		switch (action) {
			case AC_DESCEND:
				descendLevel();
				break;
			case AC_ASCEND:
				ascendLevel();
				break;
			case AC_REVEAL:
				revealMap();
				break;
			case AC_LEVELUP:
				levelUpMax();
				break;
			case AC_SPAWN:
				spawnEnemies();
				break;
			case AC_GOLD:
				addGold();
				break;
			case AC_ITEMS:
				spawnItems();
				break;
		}
	}

	private void descendLevel() {
		if (Dungeon.depth >= 26) {
			GLog.w(Messages.get(this, "max_depth"));
			return;
		}
		
		// Create a proper level transition for descending without damage
		LevelTransition transition = new LevelTransition(
			Dungeon.level, 
			curUser.pos, 
			LevelTransition.Type.REGULAR_EXIT,
			Dungeon.depth + 1,
			Dungeon.branch,
			LevelTransition.Type.REGULAR_ENTRANCE
		);
		
		InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
		InterlevelScene.curTransition = transition;
		Game.switchScene(InterlevelScene.class);
	}

	private void ascendLevel() {
		if (Dungeon.depth <= 1) {
			GLog.w(Messages.get(this, "min_depth"));
			return;
		}
		
		InterlevelScene.mode = InterlevelScene.Mode.RETURN;
		InterlevelScene.returnDepth = Math.max(1, Dungeon.depth - 1);
		InterlevelScene.returnBranch = 0;
		InterlevelScene.returnPos = -2;
		Game.switchScene(InterlevelScene.class);
	}

	private void revealMap() {
		int length = Dungeon.level.length();
		int[] map = Dungeon.level.map;
		boolean[] mapped = Dungeon.level.mapped;
		boolean[] discoverable = Dungeon.level.discoverable;

		boolean noticed = false;

		for (int i = 0; i < length; i++) {
			int terr = map[i];

			if (discoverable[i]) {
				mapped[i] = true;
				if ((Terrain.flags[terr] & Terrain.SECRET) != 0) {
					Dungeon.level.discover(i);

					if (Dungeon.level.heroFOV[i]) {
						GameScene.discoverTile(i, terr);
						ScrollOfMagicMapping.discover(i);
						noticed = true;
					}
				}
			}
		}

		GameScene.updateFog();
		GLog.i(Messages.get(this, "map_revealed"));
		
		if (noticed) {
			Sample.INSTANCE.play(Assets.Sounds.SECRET);
		}

		SpellSprite.show(curUser, SpellSprite.MAP);
	}

	private void levelUpMax() {
		int levelsToGain = 30 - curUser.lvl;
		
		if (levelsToGain <= 0) {
			GLog.w(Messages.get(this, "already_max"));
			return;
		}

		for (int i = 0; i < levelsToGain; i++) {
			Potion exp = new PotionOfExperience();
			exp.apply(curUser);
		}

		GLog.p(Messages.get(this, "leveled_up", levelsToGain));
	}

	private void spawnEnemies() {
		SummoningTrap trap = new SummoningTrap();
		trap.pos = curUser.pos;
		trap.activate();
		
		GLog.w(Messages.get(this, "enemies_spawned"));
		curUser.next();
	}

	private void addGold() {
		int goldToAdd = 1000;
		Dungeon.gold += goldToAdd;
		GLog.p(Messages.get(this, "gold_added", goldToAdd));
	}

	private void spawnItems() {
		// Add 99 Scrolls of Upgrade
		Item upgradeScrolls = new ScrollOfUpgrade().quantity(99);
		upgradeScrolls.collect();
		
		// Add 99 Scrolls of Enchantment
		Item enchantScrolls = new ScrollOfEnchantment().quantity(99);
		enchantScrolls.collect();
		
		// Add 99 Scrolls of Transmutation
		Item transmuteScrolls = new ScrollOfTransmutation().quantity(99);
		transmuteScrolls.collect();
		
		// Add 99 Potions of Strength
		Item strengthPotions = new PotionOfStrength().quantity(99);
		strengthPotions.collect();
		
		// Add Level 99 Greatsword
		Greatsword greatsword = new Greatsword();
		greatsword.identify();
		for (int i = 0; i < 99; i++) {
			greatsword.upgrade();
		}
		if (!greatsword.doPickUp(curUser)) {
			Dungeon.level.drop(greatsword, curUser.pos).sprite.drop();
		}
		
		// Add Level 99 Plate Armor
		PlateArmor plateArmor = new PlateArmor();
		plateArmor.identify();
		for (int i = 0; i < 99; i++) {
			plateArmor.upgrade();
		}
		if (!plateArmor.doPickUp(curUser)) {
			Dungeon.level.drop(plateArmor, curUser.pos).sprite.drop();
		}
		
		// Add Level 99 Ring of Accuracy
		RingOfAccuracy ringAccuracy = new RingOfAccuracy();
		ringAccuracy.identify();
		for (int i = 0; i < 99; i++) {
			ringAccuracy.upgrade();
		}
		if (!ringAccuracy.doPickUp(curUser)) {
			Dungeon.level.drop(ringAccuracy, curUser.pos).sprite.drop();
		}
		
		// Add Level 99 Ring of Arcana
		RingOfArcana ringArcana = new RingOfArcana();
		ringArcana.identify();
		for (int i = 0; i < 99; i++) {
			ringArcana.upgrade();
		}
		if (!ringArcana.doPickUp(curUser)) {
			Dungeon.level.drop(ringArcana, curUser.pos).sprite.drop();
		}
		
		// Add Level 99 Ring of Haste
		RingOfHaste ringHaste = new RingOfHaste();
		ringHaste.identify();
		for (int i = 0; i < 99; i++) {
			ringHaste.upgrade();
		}
		if (!ringHaste.doPickUp(curUser)) {
			Dungeon.level.drop(ringHaste, curUser.pos).sprite.drop();
		}
		
		// Add Level 99 Ring of Energy
		RingOfEnergy ringEnergy = new RingOfEnergy();
		ringEnergy.identify();
		for (int i = 0; i < 99; i++) {
			ringEnergy.upgrade();
		}
		if (!ringEnergy.doPickUp(curUser)) {
			Dungeon.level.drop(ringEnergy, curUser.pos).sprite.drop();
		}
		
		// Add Max Level Alchemist's Toolkit
		AlchemistsToolkit toolkit = new AlchemistsToolkit();
		toolkit.identify();
		for (int i = 0; i < 10; i++) {
			toolkit.upgrade();
		}
		if (!toolkit.doPickUp(curUser)) {
			Dungeon.level.drop(toolkit, curUser.pos).sprite.drop();
		}
		
		GLog.p(Messages.get(this, "items_spawned"));
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public String info() {
		return Messages.get(this, "desc");
	}
}
