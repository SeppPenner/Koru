package io.anuke.koru.systems;

import com.badlogic.gdx.utils.Array;

import io.anuke.koru.modules.Network;
import io.anuke.koru.network.Net;
import io.anuke.koru.network.Net.Mode;
import io.anuke.koru.network.packets.WorldUpdatePacket;
import io.anuke.koru.traits.ConnectionTrait;
import io.anuke.koru.traits.SyncTrait;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.ecs.Spark;
import io.anuke.ucore.ecs.TraitProcessor;

public class SyncSystem extends TraitProcessor{
	static public float syncrange = 150;
	
	public SyncSystem(){
		super(ConnectionTrait.class);
	}
	
	public void update(Array<Spark> sparks){
		if(Timers.get("synctimer", Network.packetFrequency))
			super.update(sparks);
	}
	
	public void update(Spark spark){
		
		WorldUpdatePacket packet = new WorldUpdatePacket();

		spark.getBasis().getProcessor(EntityMapper.class).getNearbySyncables(spark.pos().x, spark.pos().y, syncrange, (entity)->{
			if(entity == spark) return;
			packet.updates.put(entity.getID(), entity.get(SyncTrait.class).type.write(entity));
		});
		
		if(packet.updates.size != 0) {
			Net.sendTo(spark.get(ConnectionTrait.class).connectionID, packet, Mode.UDP);
		}
	}
	
}
