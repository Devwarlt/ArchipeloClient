package net.hollowbit.archipelo.entity;

import com.badlogic.gdx.math.Vector2;

import net.hollowbit.archipelo.world.Map;

public abstract class LivingEntity extends Entity {
	
	public static final int INTERPOLATION_SPEED = 250;
	public static final float MOVEMENT_STATETIME_START = 0.9f / 8;
	public static final double DIAGONAL_FACTOR = Math.sqrt(2);

	protected boolean isMoving;
	protected Vector2 goal;
	protected float movingStateTime;
	
	@Override
	public void create(EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		super.create(fullSnapshot, map, entityType);
		goal = new Vector2(fullSnapshot.getFloat("x", 0), fullSnapshot.getFloat("y", 0));
	}
	
	public void update (float deltatime) {
		//location.pos.lerp(goal, deltatime * INTERPOLATION_SPEED);
		double xDif = goal.x - location.getX();
		double yDif = goal.y - location.getY();
		double angle = Math.atan2(yDif, xDif) * 180 / Math.PI;
		
		double dX = Math.cos(angle * Math.PI / 180) * INTERPOLATION_SPEED;
		double dY = Math.sin(angle * Math.PI / 180) * INTERPOLATION_SPEED;
		
		location.setX((float) (((location.getX() - goal.x) * (location.getX() + dX - goal.x) > 0 && location.getX() != goal.x) ? location.getX() + dX : goal.x));
		location.setY((float) (((location.getY() - goal.y) * (location.getY() + dY - goal.y) > 0 && location.getY() != goal.y) ? location.getY() + dY : goal.y));
		
		if (isMoving)
			movingStateTime += deltatime;
	}
	
	@Override
	public void applyInterpSnapshot(double timeStamp, EntitySnapshot snapshot) {
		this.goal.set(snapshot.getFloat("x", location.getX()), snapshot.getFloat("y", location.getY()));
	}
	
	@Override
	public void applyChangesSnapshot(EntitySnapshot snapshot) {
		boolean wasMoving = isMoving;
		isMoving = snapshot.getBoolean("is-moving", isMoving);
		if (wasMoving && !isMoving)
			movingStateTime = MOVEMENT_STATETIME_START;
		
		super.applyChangesSnapshot(snapshot);
	}
	
	@Override
	public boolean isPlayer() {
		return false;
	}
	
	@Override
	public boolean isAlive() {
		return true;
	}
	
}