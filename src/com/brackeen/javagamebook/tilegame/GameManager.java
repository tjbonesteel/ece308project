package com.brackeen.javagamebook.tilegame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Timer;
import java.util.Queue;

import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFormat;

import com.brackeen.javagamebook.graphics.*;
import com.brackeen.javagamebook.sound.*;
import com.brackeen.javagamebook.input.*;
import com.brackeen.javagamebook.test.GameCore;
import com.brackeen.javagamebook.tilegame.sprites.*;

/**
 * GameManager manages all parts of the game.
 */
public class GameManager extends GameCore {

	public static void main(String[] args) {
		new GameManager().run();
	}

	// uncompressed, 44100Hz, 16-bit, mono, signed, little-endian
	private static final AudioFormat PLAYBACK_FORMAT = new AudioFormat(44100, 16, 1, true, false);

	private static final int DRUM_TRACK = 1;

	public static final float GRAVITY = 0.002f;

	private Point pointCache = new Point();
	private TileMap map;
	private MidiPlayer midiPlayer;
	private SoundManager soundManager;
	private ResourceManager resourceManager;
	private Sound prizeSound;
	private Sound boopSound;
	private Sound gunShotSound;
	private InputManager inputManager;
	private TileMapRenderer renderer;
	private boolean pressed = true;
	private int bulletCount = 0;
	private int oldXTile = 0;
	private GameAction shoot;
	private GameAction moveLeft;
	private GameAction moveRight;
	private GameAction jump;
	private GameAction exit;
	private int healthFlag = 1;

	private static long healthTime;
	private static long enemyTime = System.currentTimeMillis();
	private static long time = System.currentTimeMillis();
	private static long bulletTime;
	private static long creatureBulletTime;
	private static long currentTime = System.currentTimeMillis();

	public void init() {
		super.init();

		// set up input manager
		initInput();

		// start resource manager
		resourceManager = new ResourceManager(screen.getFullScreenWindow().getGraphicsConfiguration());

		// load resources
		renderer = new TileMapRenderer();
		renderer.setBackground(resourceManager.loadImage("background.png"));

		// load first map
		map = resourceManager.loadNextMap();

		// load sounds
		soundManager = new SoundManager(PLAYBACK_FORMAT);
		prizeSound = soundManager.getSound("sounds/prize.wav");
		boopSound = soundManager.getSound("sounds/boop2.wav");
		gunShotSound = soundManager.getSound("sounds/gun-gunshot-01.wav");

		// start music

		midiPlayer = new MidiPlayer();
		Sequence sequence = midiPlayer.getSequence("sounds/music.midi");
		midiPlayer.play(sequence, true);
		toggleDrumPlayback();
	}

	/**
	 * Closes any resources used by the GameManager.
	 */
	public void stop() {
		super.stop();
		midiPlayer.close();
		soundManager.close();
	}

	private void initInput() {
		shoot = new GameAction("shoot");
		moveLeft = new GameAction("moveLeft");
		moveRight = new GameAction("moveRight");
		jump = new GameAction("jump", GameAction.DETECT_INITAL_PRESS_ONLY);
		exit = new GameAction("exit", GameAction.DETECT_INITAL_PRESS_ONLY);

		inputManager = new InputManager(screen.getFullScreenWindow());
		inputManager.setCursor(InputManager.INVISIBLE_CURSOR);

		inputManager.mapToKey(moveLeft, KeyEvent.VK_LEFT);
		inputManager.mapToKey(moveRight, KeyEvent.VK_RIGHT);
		inputManager.mapToKey(jump, KeyEvent.VK_SPACE);
		inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
		inputManager.mapToKey(shoot, KeyEvent.VK_F);
	}

	private void checkInput(long elapsedTime) {

		time = System.currentTimeMillis();
		if (exit.isPressed()) {
			stop();
		}

		Player player = (Player) map.getPlayer();
		if (player.isAlive()) {

			float velocityX = 0;

			if (moveLeft.isPressed()) {
				velocityX -= player.getMaxSpeed();
				player.setFacingRight(false);
				player.xTile = TileMapRenderer.pixelsToTiles(player.getX());
				if (player.xTile != oldXTile) {
					player.addHealth(1);
					oldXTile = player.xTile;
				}
			}
			if (moveRight.isPressed()) {
				velocityX += player.getMaxSpeed();
				player.setFacingRight(true);
				player.xTile = TileMapRenderer.pixelsToTiles(player.getX());
				if (player.xTile != oldXTile) {
					player.addHealth(1);
					oldXTile = player.xTile;
				}
			}
			if (jump.isPressed()) {
				player.jump(false);
			}
			if (shoot.isPressed()) {
				if (time - bulletTime < 10) {
					if (bulletCount <= 10) {
						currentTime = System.currentTimeMillis();

						player.setState(2, elapsedTime, 1);
						soundManager.play(gunShotSound);
						resourceManager.addBullet(map, player.getX(), player.getY(), player.getPlayerFacingRight(),
								true);
					} else {
						if (time - currentTime > 1000) {
							bulletCount = 0;
						}
					}
				} else if (time - bulletTime > 250) {
					// increment bullet counter here
					bulletCount++;
					bulletTime = System.currentTimeMillis();

				}
			} else {
				bulletCount = 0;
				bulletTime = System.currentTimeMillis();
				currentTime = 0;
				pressed = true;
			}

			if (oldXTile == player.xTile && healthFlag == 1) {
				healthTime = System.currentTimeMillis();
				healthFlag = 0;

			}
			if (oldXTile == player.xTile && time - healthTime >= 1000 && healthFlag == 0) {
				player.addHealth(5);
				healthFlag = 1;
			}
			if (oldXTile != player.xTile) {
				healthTime = 0;
				healthFlag = 1;
			}
			player.setState(0, elapsedTime, 0);
			player.setVelocityX(velocityX);

			creatureShoot();
		}

	}
	
	private void creatureShoot() {
		time = System.currentTimeMillis();
		Iterator<Sprite> iter = map.getSprites();
		// Queue q = null;
		Player player = (Player) map.getPlayer();

		while (iter.hasNext()) {
			Sprite s = iter.next();
			//Grub objects are the dudes that move slowly on the ground
			if (s instanceof Grub) {
				Grub shooter = null;
				shooter = (Grub) s;
				// System.out.println("Shooter x: " + shooter.getX() + "Y: " + shooter.getY());
				// System.out.println("Player x: " + player.getX() + "Y: " + player.getY());
				
				if (Math.abs(player.getX() -  shooter.getX()) < 500 && shooter.isAlive()) {
						if (time - creatureBulletTime < 10) {
							//soundManager.play(gunShotSound);
							resourceManager.addBullet(map, shooter.getX(), shooter.getY(), false,false);
						} else if(time - creatureBulletTime > 500){
							creatureBulletTime = System.currentTimeMillis();
						}
				} else {
					creatureBulletTime = System.currentTimeMillis();
				}
				//System.out.println("Fire Bullet");
				// creatureQueue creat = new creatureQueue(shooter.getX(),
				// shooter.getY(), true);
				// q.add(creat);
				// resourceManager.addBullet(map, shooter.getY(),
				// shooter.getX(), true, true);
			}
		}
	}
	

	public void draw(Graphics2D g) {
		renderer.draw(g, map, screen.getWidth(), screen.getHeight());
	}

	/**
	 * Gets the current map.
	 */
	public TileMap getMap() {
		return map;
	}

	/**
	 * Turns on/off drum playback in the midi music (track 1).
	 */
	public void toggleDrumPlayback() {
		Sequencer sequencer = midiPlayer.getSequencer();
		if (sequencer != null) {
			sequencer.setTrackMute(DRUM_TRACK, !sequencer.getTrackMute(DRUM_TRACK));
		}
	}

	/**
	 * Gets the tile that a Sprites collides with. Only the Sprite's X or Y
	 * should be changed, not both. Returns null if no collision is detected.
	 */
	public Point getTileCollision(Sprite sprite, float newX, float newY) {
		float fromX = Math.min(sprite.getX(), newX);
		float fromY = Math.min(sprite.getY(), newY);
		float toX = Math.max(sprite.getX(), newX);
		float toY = Math.max(sprite.getY(), newY);

		// get the tile locations
		int fromTileX = TileMapRenderer.pixelsToTiles(fromX);
		int fromTileY = TileMapRenderer.pixelsToTiles(fromY);
		int toTileX = TileMapRenderer.pixelsToTiles(toX + sprite.getWidth() - 1);
		int toTileY = TileMapRenderer.pixelsToTiles(toY + sprite.getHeight() - 1);

		// check each tile for a collision
		for (int x = fromTileX; x <= toTileX; x++) {
			for (int y = fromTileY; y <= toTileY; y++) {
				if (x < 0 || x >= map.getWidth() || map.getTile(x, y) != null) {
					// collision found, return the tile
					pointCache.setLocation(x, y);
					return pointCache;
				}
			}
		}

		// no collision found
		return null;
	}

	/**
	 * Checks if two Sprites collide with one another. Returns false if the two
	 * Sprites are the same. Returns false if one of the Sprites is a Creature
	 * that is not alive.
	 */
	public boolean isCollision(Sprite s1, Sprite s2) {
		// if the Sprites are the same, return false
		if (s1 == s2) {
			return false;
		}

		// if one of the Sprites is a dead Creature, return false
		if (s1 instanceof Creature && !((Creature) s1).isAlive()) {
			return false;
		}
		if (s2 instanceof Creature && !((Creature) s2).isAlive()) {
			return false;
		}
		Bullet b = null;
		if (s1 instanceof Player && s2 instanceof Bullet) {
			b = (Bullet) s2;
			if (b.isPlayerBullet()) {
				return false;
			}
		}
		if (s1 instanceof Bullet && s2 instanceof Player) {
			b = (Bullet) s1;
			if (b.isPlayerBullet()) {
				return false;
			}
		}

		// get the pixel location of the Sprites
		int s1x = Math.round(s1.getX());
		int s1y = Math.round(s1.getY());
		int s2x = Math.round(s2.getX());
		int s2y = Math.round(s2.getY());

		// check if the two sprites' boundaries intersect
		return (s1x < s2x + s2.getWidth() && s2x < s1x + s1.getWidth() && s1y < s2y + s2.getHeight()
				&& s2y < s1y + s1.getHeight());
	}

	/**
	 * Gets the Sprite that collides with the specified Sprite, or null if no
	 * Sprite collides with the specified Sprite.
	 */
	public Sprite getSpriteCollision(Sprite sprite) {

		// run through the list of Sprites
		Iterator i = map.getSprites();
		while (i.hasNext()) {
			Sprite otherSprite = (Sprite) i.next();
			if (isCollision(sprite, otherSprite)) {
				// collision found, return the Sprite
				return otherSprite;
			}
		}

		// no collision found
		return null;
	}

	/**
	 * Updates Animation, position, and velocity of all Sprites in the current
	 * map.
	 */
	public void update(long elapsedTime) {
		Creature player = (Creature) map.getPlayer();

		// player is dead! start map over
		if (player.getState() == Creature.STATE_DEAD) {
			map = resourceManager.reloadMap();
			return;
		}

		// bullet remove

		// get keyboard/mouse input
		checkInput(elapsedTime);

		// update player
		updateCreature(player, elapsedTime);

		player.update(elapsedTime);

		// update other sprites
		Iterator i = map.getSprites();
		while (i.hasNext()) {
			Sprite sprite = (Sprite) i.next();
			if (sprite instanceof Creature) {
				Creature creature = (Creature) sprite;
				if (creature.getState() == Creature.STATE_DEAD) {
					i.remove();
				} else {
					updateCreature(creature, elapsedTime);
				}
				
			} else if (sprite instanceof Bullet) {
				Bullet bullet = (Bullet) sprite;
				checkBulletCollision(bullet);
				if (bullet.getState() == Bullet.STATE_MISS) {
					System.out.println("Remove");
					i.remove();
				} else {
					updateBullet(bullet, elapsedTime);
				}
			}
			// normal update
			sprite.update(elapsedTime);
		}
	}

	public void updateBullet(Bullet bullet, float elapsedTime) {
		float oldX = bullet.getX();
		float dx = bullet.getVelocityX();
		float newX = oldX + dx * elapsedTime;
		bullet.setX(newX);
	}

	/**
	 * Updates the creature, applying gravity for creatures that aren't flying,
	 * and checks collisions.
	 */
	private void updateCreature(Creature creature, long elapsedTime) {

		// apply gravity
		if (!creature.isFlying()) {
			creature.setVelocityY(creature.getVelocityY() + GRAVITY * elapsedTime);
		}

		// resourceManager.addBullet(map, creature.getX(), creature.getY(),
		// false, false);

		// change x
		float dx = creature.getVelocityX();
		float oldX = creature.getX();
		float newX = oldX + dx * elapsedTime;
		Point tile = getTileCollision(creature, newX, creature.getY());
		if (tile == null) {
			creature.setX(newX);
		} else {
			// line up with the tile boundary
			if (dx > 0) {
				creature.setX(TileMapRenderer.tilesToPixels(tile.x) - creature.getWidth());
			} else if (dx < 0) {
				creature.setX(TileMapRenderer.tilesToPixels(tile.x + 1));
			}
			creature.collideHorizontal();
		}
		if (creature instanceof Player) {
			checkPlayerCollision((Player) creature, false);
		}

		// change y
		float dy = creature.getVelocityY();
		float oldY = creature.getY();
		float newY = oldY + dy * elapsedTime;
		tile = getTileCollision(creature, creature.getX(), newY);
		if (tile == null) {
			creature.setY(newY);
		} else {
			// line up with the tile boundary
			if (dy > 0) {
				creature.setY(TileMapRenderer.tilesToPixels(tile.y) - creature.getHeight());
			} else if (dy < 0) {
				creature.setY(TileMapRenderer.tilesToPixels(tile.y + 1));
			}
			creature.collideVertical();
		}
		
		
		
		if (creature instanceof Player) {
			boolean canKill = (oldY < creature.getY());
			checkPlayerCollision((Player) creature, canKill);
		}
		
		
	}

	/**
	 * Checks for Player collision with other Sprites. If canKill is true,
	 * collisions with Creatures will kill them.
	 */
	public void checkPlayerCollision(Player player, boolean canKill) {
		if (!player.isAlive()) {
			return;
		}

		// check for player collision with other sprites
		Sprite collisionSprite = getSpriteCollision(player);
		if (collisionSprite instanceof PowerUp) {
			acquirePowerUp((PowerUp) collisionSprite);
		} else if (collisionSprite instanceof Creature) {
			Creature badguy = (Creature) collisionSprite;
			if (canKill) {
				// kill the badguy and make player bounce
				soundManager.play(boopSound);
				badguy.setState(Creature.STATE_DYING);
				player.setY(badguy.getY() - player.getHeight());
				player.addHealth(10);
				// player.jump(true);
			} else {
				// player dies!
				player.setState(Creature.STATE_DYING);

			}
		}
	}

	public void checkBulletCollision(Bullet bullet) {
		Sprite collisionSprite = getSpriteCollision(bullet);
		Point point = getTileCollision(bullet, bullet.getX(), bullet.getY());
		if (collisionSprite instanceof Creature) {
			Creature badguy = (Creature) collisionSprite;
			// soundManager.play(bulletHit); //Don't have this sound file yet
			badguy.setState(Creature.STATE_DYING);
			bullet.setState(Bullet.STATE_MISS);
		} else if (point != null) {
			bullet.setState(Bullet.STATE_MISS);
		}
	}

	/**
	 * Gives the player the speicifed power up and removes it from the map.
	 */
	public void acquirePowerUp(PowerUp powerUp) {
		// remove it from the map
		map.removeSprite(powerUp);

		if (powerUp instanceof PowerUp.Star) {
			// do something here, like give the player points
			soundManager.play(prizeSound);
		} else if (powerUp instanceof PowerUp.Music) {
			// change the music
			soundManager.play(prizeSound);
			toggleDrumPlayback();
		} else if (powerUp instanceof PowerUp.Goal) {
			// advance to next map
			soundManager.play(prizeSound, new EchoFilter(2000, .7f), false);
			map = resourceManager.loadNextMap();
		}
	}

}
