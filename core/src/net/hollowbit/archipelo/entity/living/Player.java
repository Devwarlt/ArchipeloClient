package net.hollowbit.archipelo.entity.living;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import net.hollowbit.archipelo.entity.EntitySnapshot;
import net.hollowbit.archipelo.entity.EntityType;
import net.hollowbit.archipelo.entity.LivingEntity;
import net.hollowbit.archipelo.entity.living.player.MovementLog;
import net.hollowbit.archipelo.entity.living.player.MovementLogEntry;
import net.hollowbit.archipelo.items.Item;
import net.hollowbit.archipelo.items.ItemType;
import net.hollowbit.archipelo.world.Map;
import net.hollowbit.archipeloshared.CollisionRect;
import net.hollowbit.archipeloshared.Controls;
import net.hollowbit.archipeloshared.Direction;

public class Player extends LivingEntity {
	
	public static final int SPEED = 60;//Pixels per second
	public static final float ROLLING_DURATION = 0.4f;
	public static final float ROLLING_SPEED_SCALE = 3.0f;
	public static final float SPRINTING_SPEED_SCALE = 1.4f;
	public static final float ROLL_DOUBLE_CLICK_DURATION = 0.3f;
	
	//Equipped Inventory Index
	public static final int EQUIP_SIZE = 10;
	public static final int EQUIP_INDEX_BODY = 0;
	public static final int EQUIP_INDEX_BOOTS = 1;
	public static final int EQUIP_INDEX_PANTS = 2;
	public static final int EQUIP_INDEX_SHIRT = 3;
	public static final int EQUIP_INDEX_GLOVES = 4;
	public static final int EQUIP_INDEX_SHOULDERPADS = 5;
	public static final int EQUIP_INDEX_FACE = 6;
	public static final int EQUIP_INDEX_HAIR = 7;
	public static final int EQUIP_INDEX_HAT = 8;
	public static final int EQUIP_INDEX_USABLE = 9;
	
	Direction rollingDirection = Direction.UP;
	float rollingStateTime;
	float rollTimer;
	float rollDoubleClickTimer = 0;
	boolean isSprinting;
	boolean[] controls;
	boolean isCurrentPlayer;
	Item[] equippedInventory;
	boolean loaded = false;
	MovementLog movementLog;
	
	/**
	 * This method is used when creating a player that is the current one.
	 * No point in initializing variables if they won't be used, right?
	*/
	public void createCurrentPlayer () {
		controls = new boolean[Controls.TOTAL];
		movementLog = new MovementLog();
	}
	
	@Override
	public void create (EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		super.create(fullSnapshot, map, entityType);
		Json json = new Json();
		this.equippedInventory = json.fromJson(Item[].class, fullSnapshot.getString("equipped-inventory", ""));
		loaded = true;
	}
	
	@Override
	public void load() {
		super.load();
	}
	
	@Override
	public void unload() {
		super.unload();
	}
	
	@Override
	public void update (float deltatime) {
		super.update(deltatime);
		
		if (isRolling()) {
			rollingStateTime += deltatime;
			rollTimer -= deltatime;
			if (rollTimer < 0) {
				rollTimer = 0;
				rollingStateTime = 0;
			}
		}
	}
	
	public void updatePlayer (float deltatime, boolean[] controls) {
		//Tick timer for roll double-click
		if (rollDoubleClickTimer >= 0) {
			rollDoubleClickTimer -= deltatime;
			if (rollDoubleClickTimer < 0)
				rollDoubleClickTimer = 0;
		}
		
		Vector2 pos = new Vector2(goal.x, goal.y);
		
		Direction directionMoved = null;
		double speedMoved = 0;//Use double now for accuracy and cast later
		
		//Direction
		if (controls[Controls.UP]) {
			if (controls[Controls.LEFT]) {//Up left
				if (location.getDirection() != Direction.UP_LEFT && !controls[Controls.LOCK])
					location.setDirection(Direction.UP_LEFT);
				
				if (rollingDirection != Direction.UP_LEFT)
					rollingDirection = Direction.UP_LEFT;
				
				if (isMoving(controls)) {
					directionMoved = Direction.UP_LEFT;
					speedMoved = getSpeed() / LivingEntity.DIAGONAL_FACTOR;
					pos.add((float) (-deltatime * speedMoved), (float) (deltatime * speedMoved));
				}
			} else if (controls[Controls.RIGHT]) {//Up right
				if (location.getDirection() != Direction.UP_RIGHT && !controls[Controls.LOCK])
					location.setDirection(Direction.UP_RIGHT);
				
				if (rollingDirection != Direction.UP_RIGHT)
					rollingDirection = Direction.UP_RIGHT;
				
				if (isMoving(controls)) {
					directionMoved = Direction.UP_RIGHT;
					speedMoved = getSpeed() / LivingEntity.DIAGONAL_FACTOR;
					pos.add((float) (deltatime * speedMoved), (float) (deltatime * speedMoved));
				}
			} else {//Up
				if (location.getDirection() != Direction.UP && !controls[Controls.LOCK])
					location.setDirection(Direction.UP);
				
				if (rollingDirection != Direction.UP)
					rollingDirection = Direction.UP;
				
				if (isMoving(controls)) {
					directionMoved = Direction.UP;
					speedMoved = getSpeed();
					pos.add(0, (float) (deltatime * speedMoved));
				}
			}
		} else if (controls[Controls.DOWN]) {
			if (controls[Controls.LEFT]) {//Down left
				if (location.getDirection() != Direction.DOWN_LEFT && !controls[Controls.LOCK])
					location.setDirection(Direction.DOWN_LEFT);
				
				if (rollingDirection != Direction.DOWN_LEFT)
					rollingDirection = Direction.DOWN_LEFT;
				
				if (isMoving(controls)) {
					directionMoved = Direction.DOWN_LEFT;
					speedMoved = getSpeed() / LivingEntity.DIAGONAL_FACTOR;
					pos.add((float) (-deltatime * speedMoved), (float) (-deltatime * speedMoved));
				}
			} else if (controls[Controls.RIGHT]) {//Down right
				if (location.getDirection() != Direction.DOWN_RIGHT && !controls[Controls.LOCK])
					location.setDirection(Direction.DOWN_RIGHT);
				
				if (rollingDirection != Direction.DOWN_RIGHT)
					rollingDirection = Direction.DOWN_RIGHT;
				
				if (isMoving(controls)) {
					directionMoved = Direction.DOWN_RIGHT;
					speedMoved = getSpeed() / LivingEntity.DIAGONAL_FACTOR;
					pos.add((float) (deltatime * speedMoved), (float) (-deltatime * speedMoved));
				}
			} else {//Down
				if (location.getDirection() != Direction.DOWN && !controls[Controls.LOCK])
					location.setDirection(Direction.DOWN);
				
				if (rollingDirection != Direction.DOWN)
					rollingDirection = Direction.DOWN;
				
				if (isMoving(controls)) {
					directionMoved = Direction.DOWN;
					speedMoved = getSpeed();
					pos.add(0, (float) (-deltatime * speedMoved));
				}
			}
		} else if (controls[Controls.LEFT]) {//Left
			if (location.getDirection() != Direction.LEFT && !controls[Controls.LOCK])
				location.setDirection(Direction.LEFT);
			
			if (rollingDirection != Direction.LEFT)
				rollingDirection = Direction.LEFT;
			
			if (isMoving(controls)) {
				directionMoved = Direction.LEFT;
				speedMoved = getSpeed();
				pos.add((float) (-deltatime * speedMoved), 0);
			}
		} else if (controls[Controls.RIGHT]) {//Right
			if (location.getDirection() != Direction.RIGHT && !controls[Controls.LOCK])
				location.setDirection(Direction.RIGHT);
			
			if (rollingDirection != Direction.RIGHT)
				rollingDirection = Direction.RIGHT;
			
			if (isMoving(controls)) {
				directionMoved = Direction.RIGHT;
				speedMoved = getSpeed();
				pos.add((float) (deltatime * speedMoved), 0);
			}
		}
		
		boolean collidesWithMap = false;
		for (CollisionRect rect : getCollisionRects(pos)) {//Checks to make sure no collision rect is intersecting with map
			if (location.getMap().collidesWithMap(rect)) {
				collidesWithMap = true;
				break;
			}
		}
		
		if (!collidesWithMap || doesCurrentPositionCollideWithMap()) {
			goal.set(pos);
			
			if (isMoving(controls)) {
				//Add new log entry to movement log manager
				movementLog.add(new MovementLogEntry(directionMoved, (float) speedMoved));
			}
		}
	}

	@Override
	public void render (SpriteBatch batch) {
		if (!loaded)
			return;
		
		boolean drawUseableOnBottom = location.getDirection() == Direction.DOWN_LEFT || location.getDirection() == Direction.LEFT || location.getDirection() == Direction.UP_LEFT || location.getDirection() == Direction.UP; 
		
		if (isMoving) {
			if (isRolling()) {
				batch.draw(this.getEntityType().getAnimationFrame("rolling", location.getDirection(), rollingStateTime), location.getX(), location.getY());
				if (drawUseableOnBottom && equippedInventory[EQUIP_INDEX_USABLE] != null) {
					batch.setColor(new Color(equippedInventory[EQUIP_INDEX_USABLE].color));
					batch.draw(ItemType.getItemTypeByItem(equippedInventory[EQUIP_INDEX_USABLE]).getRollFrame(location.getDirection(), rollingStateTime, equippedInventory[EQUIP_INDEX_USABLE].style), location.getX(), location.getY());
					batch.setColor(1, 1, 1, 1);
				}
				for (int i = 0; i < EQUIP_SIZE - 1; i++) {//Loop through each part of equipable
					if (equippedInventory[i] == null)
						continue;
					batch.setColor(new Color(equippedInventory[i].color));
					if (i == EQUIP_INDEX_HAIR) {
						//If a hat is equipped, use the hair texture for when wearing hats
						if (equippedInventory[EQUIP_INDEX_HAT] == null)
							batch.draw(ItemType.getItemTypeByItem(equippedInventory[i]).getRollFrame(location.getDirection(), rollingStateTime, 0), location.getX(), location.getY());
					} else if (i == EQUIP_INDEX_FACE) {
						batch.setColor(1, 1, 1, 1);
						batch.draw(ItemType.getItemTypeByItem(equippedInventory[i]).getRollFrame(location.getDirection(), rollingStateTime, 0), location.getX(), location.getY());//Draw mouth
						batch.setColor(new Color(equippedInventory[i].color));
						batch.draw(ItemType.getItemTypeByItem(equippedInventory[i]).getRollFrame(location.getDirection(), rollingStateTime, 1), location.getX(), location.getY());//Draw eye's iris with color
						batch.setColor(new Color(equippedInventory[EQUIP_INDEX_HAIR].color));
						batch.draw(ItemType.getItemTypeByItem(equippedInventory[i]).getRollFrame(location.getDirection(), rollingStateTime, 2), location.getX(), location.getY());//Draw eyebrows with hair color
						batch.setColor(1, 1, 1, 1);
					} else
						batch.draw(ItemType.getItemTypeByItem(equippedInventory[i]).getRollFrame(location.getDirection(), rollingStateTime, equippedInventory[i].style), location.getX(), location.getY());
					batch.setColor(1, 1, 1, 1);
				}
				if (!drawUseableOnBottom && equippedInventory[EQUIP_INDEX_USABLE] != null) {
					batch.setColor(new Color(equippedInventory[EQUIP_INDEX_USABLE].color));
					batch.draw(ItemType.getItemTypeByItem(equippedInventory[EQUIP_INDEX_USABLE]).getRollFrame(location.getDirection(), rollingStateTime, equippedInventory[EQUIP_INDEX_USABLE].style), location.getX(), location.getY());
					batch.setColor(1, 1, 1, 1);
				}
			} else {
				if (isSprinting) {
					batch.draw(this.getEntityType().getAnimationFrame("default", location.getDirection(), movingStateTime), location.getX(), location.getY());
					if (drawUseableOnBottom && equippedInventory[EQUIP_INDEX_USABLE] != null) {
						batch.setColor(new Color(equippedInventory[EQUIP_INDEX_USABLE].color));
						batch.draw(ItemType.getItemTypeByItem(equippedInventory[EQUIP_INDEX_USABLE]).getRollFrame(location.getDirection(), rollingStateTime, equippedInventory[EQUIP_INDEX_USABLE].style), location.getX(), location.getY());
						batch.setColor(1, 1, 1, 1);
					}
					for (int i = 0; i < EQUIP_SIZE - 1; i++) {//Loop through each part of equipable
						if (equippedInventory[i] == null)
							continue;
						batch.setColor(new Color(equippedInventory[i].color));
						if (i == EQUIP_INDEX_HAIR) {
							//If a hat is equipped, use the hair texture for when wearing hats
							if (equippedInventory[EQUIP_INDEX_HAT] == null)
								batch.draw(ItemType.getItemTypeByItem(equippedInventory[i]).getSprintFrame(location.getDirection(), movingStateTime, 0), location.getX(), location.getY());
						} else if (i == EQUIP_INDEX_FACE) {
							batch.setColor(1, 1, 1, 1);
							batch.draw(ItemType.getItemTypeByItem(equippedInventory[i]).getSprintFrame(location.getDirection(), movingStateTime, 0), location.getX(), location.getY());//Draw mouth
							batch.setColor(new Color(equippedInventory[i].color));
							batch.draw(ItemType.getItemTypeByItem(equippedInventory[i]).getSprintFrame(location.getDirection(), movingStateTime, 1), location.getX(), location.getY());//Draw eye's iris with color
							batch.setColor(new Color(equippedInventory[EQUIP_INDEX_HAIR].color));
							batch.draw(ItemType.getItemTypeByItem(equippedInventory[i]).getSprintFrame(location.getDirection(), movingStateTime, 2), location.getX(), location.getY());//Draw eyebrows with hair color
							batch.setColor(1, 1, 1, 1);
						} else
							batch.draw(ItemType.getItemTypeByItem(equippedInventory[i]).getSprintFrame(location.getDirection(), movingStateTime, equippedInventory[i].style), location.getX(), location.getY());
						batch.setColor(1, 1, 1, 1);
					}
					if (!drawUseableOnBottom && equippedInventory[EQUIP_INDEX_USABLE] != null) {
						batch.setColor(new Color(equippedInventory[EQUIP_INDEX_USABLE].color));
						batch.draw(ItemType.getItemTypeByItem(equippedInventory[EQUIP_INDEX_USABLE]).getRollFrame(location.getDirection(), rollingStateTime, equippedInventory[EQUIP_INDEX_USABLE].style), location.getX(), location.getY());
						batch.setColor(1, 1, 1, 1);
					}
				 } else {
					batch.draw(this.getEntityType().getAnimationFrame("sprinting", location.getDirection(), movingStateTime), location.getX(), location.getY());
					if (drawUseableOnBottom && equippedInventory[EQUIP_INDEX_USABLE] != null) {
						batch.setColor(new Color(equippedInventory[EQUIP_INDEX_USABLE].color));
						batch.draw(ItemType.getItemTypeByItem(equippedInventory[EQUIP_INDEX_USABLE]).getRollFrame(location.getDirection(), rollingStateTime, equippedInventory[EQUIP_INDEX_USABLE].style), location.getX(), location.getY());
						batch.setColor(1, 1, 1, 1);
					}
					for (int i = 0; i < EQUIP_SIZE - 1; i++) {//Loop through each part of equipable
						if (equippedInventory[i] == null)
							continue;
						batch.setColor(new Color(equippedInventory[i].color));
						if (i == EQUIP_INDEX_HAIR) {
							//Only draw hair if no hat/helmet is equipped
							if (equippedInventory[EQUIP_INDEX_HAT] == null)
								batch.draw(ItemType.getItemTypeByItem(equippedInventory[i]).getWalkFrame(location.getDirection(), movingStateTime, 0), location.getX(), location.getY());
						} else if (i == EQUIP_INDEX_FACE) {
							batch.setColor(1, 1, 1, 1);
							batch.draw(ItemType.getItemTypeByItem(equippedInventory[i]).getWalkFrame(location.getDirection(), movingStateTime, 0), location.getX(), location.getY());//Draw mouth
							batch.setColor(new Color(equippedInventory[i].color));
							batch.draw(ItemType.getItemTypeByItem(equippedInventory[i]).getWalkFrame(location.getDirection(), movingStateTime, 1), location.getX(), location.getY());//Draw eye's iris with color
							batch.setColor(new Color(equippedInventory[EQUIP_INDEX_HAIR].color));
							batch.draw(ItemType.getItemTypeByItem(equippedInventory[i]).getWalkFrame(location.getDirection(), movingStateTime, 2), location.getX(), location.getY());//Draw eyebrows with hair color
							batch.setColor(1, 1, 1, 1);
						} else
							batch.draw(ItemType.getItemTypeByItem(equippedInventory[i]).getWalkFrame(location.getDirection(), movingStateTime, equippedInventory[i].style), location.getX(), location.getY());
						batch.setColor(1, 1, 1, 1);
					}
					if (!drawUseableOnBottom && equippedInventory[EQUIP_INDEX_USABLE] != null) {
						batch.setColor(new Color(equippedInventory[EQUIP_INDEX_USABLE].color));
						batch.draw(ItemType.getItemTypeByItem(equippedInventory[EQUIP_INDEX_USABLE]).getRollFrame(location.getDirection(), rollingStateTime, equippedInventory[EQUIP_INDEX_USABLE].style), location.getX(), location.getY());
						batch.setColor(1, 1, 1, 1);
					}
				 }
			}
		} else {
			batch.draw(EntityType.PLAYER.getAnimationFrame("default", location.getDirection(), 0), location.getX(), location.getY());
			if (drawUseableOnBottom && equippedInventory[EQUIP_INDEX_USABLE] != null) {
				batch.setColor(new Color(equippedInventory[EQUIP_INDEX_USABLE].color));
				batch.draw(ItemType.getItemTypeByItem(equippedInventory[EQUIP_INDEX_USABLE]).getRollFrame(location.getDirection(), rollingStateTime, equippedInventory[EQUIP_INDEX_USABLE].style), location.getX(), location.getY());
				batch.setColor(1, 1, 1, 1);
			}
			for (int i = 0; i < EQUIP_SIZE - 1; i++) {//Loop through each part of equipable
				if (equippedInventory[i] == null)
					continue;
				batch.setColor(new Color(equippedInventory[i].color));
				if (i == EQUIP_INDEX_HAIR) {
					//If a hat is equipped, use the hair texture for when wearing hats
					if (equippedInventory[EQUIP_INDEX_HAT] == null)
						batch.draw(ItemType.getItemTypeByItem(equippedInventory[i]).getWalkFrame(location.getDirection(), 0, 0), location.getX(), location.getY());
				} else if (i == EQUIP_INDEX_FACE) {
					batch.setColor(1, 1, 1, 1);
					batch.draw(ItemType.getItemTypeByItem(equippedInventory[i]).getWalkFrame(location.getDirection(), 0, 0), location.getX(), location.getY());//Draw mouth
					batch.setColor(new Color(equippedInventory[i].color));
					batch.draw(ItemType.getItemTypeByItem(equippedInventory[i]).getWalkFrame(location.getDirection(), 0, 1), location.getX(), location.getY());//Draw eye's iris with color
					batch.setColor(new Color(equippedInventory[EQUIP_INDEX_HAIR].color));
					batch.draw(ItemType.getItemTypeByItem(equippedInventory[i]).getWalkFrame(location.getDirection(), 0, 2), location.getX(), location.getY());//Draw eyebrows with hair color
					batch.setColor(1, 1, 1, 1);
				} else
					batch.draw(ItemType.getItemTypeByItem(equippedInventory[i]).getWalkFrame(location.getDirection(), 0, equippedInventory[i].style), location.getX(), location.getY());
				batch.setColor(1, 1, 1, 1);
			}
			if (!drawUseableOnBottom && equippedInventory[EQUIP_INDEX_USABLE] != null) {
				batch.setColor(new Color(equippedInventory[EQUIP_INDEX_USABLE].color));
				batch.draw(ItemType.getItemTypeByItem(equippedInventory[EQUIP_INDEX_USABLE]).getRollFrame(location.getDirection(), rollingStateTime, equippedInventory[EQUIP_INDEX_USABLE].style), location.getX(), location.getY());
				batch.setColor(1, 1, 1, 1);
			}
		}
		batch.setColor(1, 1, 1, 1);
		super.render(batch);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void applyChangesSnapshot (EntitySnapshot snapshot) {
		if (!isCurrentPlayer()) {
			if (snapshot.getBoolean("is-rolling", false) && !isRolling()) {
				rollTimer = ROLLING_DURATION;
			}
			isSprinting = snapshot.getBoolean("is-sprinting", isSprinting);
			rollingDirection = Direction.values()[snapshot.getInt("rolling-direction", rollingDirection.ordinal())];
		}
		
		//Get clothes
		if (snapshot.doesPropertyExist("equipped-inventory")) {
			Json json = new Json();
			try {
				this.equippedInventory = json.fromJson(ClassReflection.forName("net.hollowbit.archipelo.items.Item"), snapshot.getString("equipped-inventory", ""));
			} catch (ReflectionException e) {
			}
		}
		
		super.applyChangesSnapshot(snapshot);
	}
	
	@Override
	public void applyInterpSnapshot(double timeStamp, EntitySnapshot snapshot) {
		if (isCurrentPlayer()) {
			//Correct player position using interp snapshot and time stamp from server
			movementLog.removeFromBeforeTimeStamp(timeStamp);
			goal.x = snapshot.getFloat("x", goal.x);
			goal.y = snapshot.getFloat("y", goal.y);
			
			double lastTime = timeStamp;
			
			//Redo player prediction movements
			for (MovementLogEntry logEntry : movementLog.getCurrentLogs()) {
				float deltatime = (float) ((logEntry.timeStamp - lastTime) / 1000);
				
				//Move depending on direction
				switch(logEntry.direction) {
				case UP:
					goal.add(0, (float) (deltatime * logEntry.speed));
					break;
				case UP_LEFT:
					goal.add((float) (-deltatime * logEntry.speed), (float) (deltatime * logEntry.speed));
					break;
				case UP_RIGHT:
					goal.add((float) (deltatime * logEntry.speed), (float) (deltatime * logEntry.speed));
					break;
				case DOWN:
					goal.add(0, (float) (-deltatime * logEntry.speed));
					break;
				case DOWN_LEFT:
					goal.add(-(float) (-deltatime * logEntry.speed), (float) (-deltatime * logEntry.speed));
					break;
				case DOWN_RIGHT:
					goal.add((float) (deltatime * logEntry.speed), (float) (-deltatime * logEntry.speed));
					break;
				case LEFT:
					goal.add((float) (-deltatime * logEntry.speed), 0);
					break;
				case RIGHT:
					goal.add((float) (deltatime * logEntry.speed), 0);
					break;
				}
				
				lastTime = logEntry.timeStamp;
			}
		} else
			super.applyInterpSnapshot(timeStamp, snapshot);
	}
	
	public boolean isMoving (boolean[] controls) {
		return controls[Controls.UP] || controls[Controls.LEFT] || controls[Controls.DOWN] || controls[Controls.RIGHT];
	}
	
	public boolean isRolling () {
		return rollTimer > 0;
	}
	
	public void controlUp (int control) {
		switch (control) {
		case Controls.ROLL:
			isSprinting = false;
			break;
		case Controls.UP:
		case Controls.LEFT:
		case Controls.DOWN:
		case Controls.RIGHT:
			movingStateTime = MOVEMENT_STATETIME_START;
			break;
		}
	}
	
	public void controlDown (int control) {
		switch (control) {
		case Controls.ROLL:
			if (rollDoubleClickTimer <= 0) {
				rollDoubleClickTimer = ROLL_DOUBLE_CLICK_DURATION;
			} else {
				rollDoubleClickTimer = 0;
				if (!isRolling())
					rollTimer = ROLLING_DURATION;
			}
			isSprinting = true;
			break;
		}
	}
	
	public void setIsCurrentPlayer (boolean isCurrentPlayer) {
		this.isCurrentPlayer = isCurrentPlayer;
	}
	
	@Override
	public boolean isPlayer () {
		return true;
	}
	
	public boolean isCurrentPlayer () {
		return isCurrentPlayer;
	}
	
	public float getSpeed () {
		return SPEED * (isRolling() ? ROLLING_SPEED_SCALE : (isSprinting ? SPRINTING_SPEED_SCALE : 1));
	}
	
	private boolean doesCurrentPositionCollideWithMap () {
		for (CollisionRect rect : getCollisionRects(location.pos)) {//Checks to make sure no collision rect is intersecting with map
			if (location.getMap().collidesWithMap(rect)) {
				return true;
			}
		}
		return false;
	}

}