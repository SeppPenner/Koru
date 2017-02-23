package io.anuke.koru.entities;

import io.anuke.koru.components.*;
import io.anuke.koru.graphics.Draw;
import io.anuke.koru.renderers.EntityRenderer;

public enum ProjectileType{
	bolt, 
	slash{
		public float lifetime(){
			return 8f;
		}
		
		public float speed(){
			return 2.4f;
		}
		
		public float hitsize(){
			return 18f;
		}
		
		public boolean pierce(){
			return true;
		}
		
		public void draw(KoruEntity entity, EntityRenderer render, ProjectileComponent pro){
			render.draw(l->{
				l.layer = entity.getY()-4;
				Draw.color(1f-entity.get(FadeComponent.class).life/lifetime());
				Draw.grect(name(), entity.getX(), entity.getY(), rotation(entity)-45);
				Draw.color();
			});
			
			//render.drawShadow(name(), -1, entity);
		}
	};
	
	public boolean pierce(){
		return false;
	}
	
	public float hitsize(){
		return 3f;
	}
	
	public float speed(){
		return 3f;
	}
	
	public float lifetime(){
		return 30f;
	}
	
	public void draw(KoruEntity entity, EntityRenderer render, ProjectileComponent pro){
		render.draw(l->{
			l.layer = entity.getY()-4;
			Draw.grect(name(), entity.getX(), entity.getY(), rotation(entity)-45);
		});
		
		render.drawShadow(name(), -1, entity);
	}
	
	public float rotation(KoruEntity entity){
		return entity.get(ProjectileComponent.class).getRotation();
	}
	
	public int damage(){
		return 1;
	}
	
	public static KoruEntity createProjectile(long source, ProjectileType type, float rotation){
		return ProjectileType.createProjectile(source, type, rotation, -1);
	}
	
	public static KoruEntity createProjectile(long source, ProjectileType type, float rotation, int damage){
		KoruEntity entity = new KoruEntity(EntityType.projectile);
		entity.mapComponent(ProjectileComponent.class).type = type;
		entity.mapComponent(ProjectileComponent.class).setRotation(entity, rotation);
		entity.mapComponent(VelocityComponent.class).velocity.set(type.speed(), 0).rotate(rotation);
		entity.mapComponent(FadeComponent.class).lifetime = type.lifetime();
		entity.mapComponent(HitboxComponent.class).entityRect().setSize(type.hitsize());
		entity.mapComponent(DamageComponent.class).source = source;
		entity.mapComponent(DamageComponent.class).damage = damage == -1 ? type.damage() : damage;
		return entity;
	}
}
