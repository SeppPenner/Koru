package io.anuke.koru.traits;

import io.anuke.koru.network.syncing.SyncData.Synced;
import io.anuke.ucore.ecs.Trait;

@Synced
public class DirectionTrait extends Trait{
	public Direction direction = Direction.front;
	public transient float walktime;
	public transient boolean walking;
	
	public void setOrdinal(int i){
		direction = Direction.values()[i];
	}
	
	public static enum Direction{
		right, front, left, back
	}
}
