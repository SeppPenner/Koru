package net.pixelstatic.koru.sprites;

import net.pixelstatic.koru.modules.Renderer;
import net.pixelstatic.utils.graphics.Atlas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool.Poolable;

public class Layer implements Comparable<Layer>, Poolable{
	public static PooledLayerList list;
	public static Atlas atlas;
	boolean temp = false;
	public static final float shadowlayer = 257, reflectionlayer = -1;
	public static final Color shadowcolor = new Color(0, 0, 0, 0.14f);
	public Color color = Color.WHITE.cpy();
	public float layer, x, y, rotation, scalex = 1f, scaley = 1f, heightoffset, width, height, vshiftx, vshifty;
	public boolean scaled;
	public String region;
	public LayerType type = LayerType.SPRITE;
	public String text;
	public TextureRegion texture;
	public boolean alignbottom = false;
	public PooledEffect particle;
	public static Sprite sprite;
	public SortType sort = SortType.OBJECT;

	public enum LayerType{
		SPRITE, TEXT, TEXTURE, SHAPE, PARTICLE, VERTICESPRITE, REFLECTION
	}

	public enum SortType{
		FLOOR{
			boolean compare(Layer layer, Layer other){
				return layer.layer > other.layer;
			}
		},
		OBJECT{
			boolean compare(Layer layer, Layer other){
				return layer.layer < other.layer;
			}
		};

		abstract boolean compare(Layer layer, Layer other);
	}

	public void draw(Renderer renderer){
		renderer.batch().setColor(color);
		if(type == LayerType.SPRITE){
			float yalign = 0;
			if(alignbottom){
				TextureRegion tex = renderer.getRegion(region);
				yalign = tex.getRegionHeight() / 2f;
			}
			if(scaled){
				renderer.drawscl(region, x, y + yalign, scalex, scaley);
			}else if(rotation == 0){
				renderer.draw(region, x, y + yalign);
			}else{
				renderer.draw(region, x, y + yalign, rotation);
			}
		}else if(type == LayerType.REFLECTION){
			float yalign = 0;
			if(alignbottom){
				TextureRegion tex = renderer.getRegion(region);
				yalign = tex.getRegionHeight() / 2f;
			}
			
			TextureRegion tex = renderer.getRegion(region);
			if(sprite == null) sprite = new Sprite(tex);

			
			sprite.setRegion(tex);
			sprite.setColor(color);

			sprite.setPosition((int)(x - tex.getRegionWidth() / 2f), (int)(y + yalign + tex.getRegionHeight() / 2f));
			sprite.setSize(tex.getRegionWidth(), -tex.getRegionHeight());
			
			float alpha = color.a;
			color.a = 0.2f;
			float abits = color.toFloatBits();
			color.a = 0.9f;
			float tbits = color.toFloatBits();
			
			color.a = alpha;
			
			sprite.getVertices()[SpriteBatch.C1] = tbits;
			sprite.getVertices()[SpriteBatch.C4] = tbits;
			
			sprite.getVertices()[SpriteBatch.C2] = abits;
			sprite.getVertices()[SpriteBatch.C3] = abits;

			sprite.draw(renderer.batch);
			//renderer.drawscl(region, x, y + yalign, 1f, -1f);
		}else if(type == LayerType.VERTICESPRITE){
			float yalign = 0;

			TextureRegion tex = renderer.getRegion(region);
			if(alignbottom){
				yalign = tex.getRegionHeight() / 2;
			}
			if(sprite == null) sprite = new Sprite(tex);

			sprite.setRegion(tex);
			sprite.setColor(color);

			sprite.setPosition(x - tex.getRegionWidth() / 2, y + yalign + tex.getRegionHeight() / 2);
			sprite.setSize(tex.getRegionWidth(), tex.getRegionHeight());

			sprite.getVertices()[SpriteBatch.Y2] = sprite.getVertices()[SpriteBatch.Y2] + vshifty;
			sprite.getVertices()[SpriteBatch.Y3] = sprite.getVertices()[SpriteBatch.Y3] + vshifty;

			sprite.getVertices()[SpriteBatch.X2] += vshiftx;
			sprite.getVertices()[SpriteBatch.X3] += vshiftx;
			//	renderer.batch()
			sprite.draw(renderer.batch());
		}else if(type == LayerType.TEXT){
			renderer.font().setUseIntegerPositions(false);
			renderer.font().setColor(color);
			renderer.font().getData().setScale(scalex / 5f);
			GlyphLayout glyphs = renderer.getBounds(text);
			renderer.font().draw(renderer.batch(), text, x - glyphs.width / 2, y + glyphs.height / 2);
		}else if(type == LayerType.TEXTURE){
			renderer.batch().setColor(color);
			renderer.batch().draw(texture, x - texture.getRegionWidth() / 2, y - texture.getRegionHeight() / 2, texture.getRegionHeight() / 2, texture.getRegionWidth() / 2, texture.getRegionWidth(), texture.getRegionHeight(), 1f, 1f, rotation);
		}else if(type == LayerType.SHAPE){
			renderer.batch().draw(renderer.getRegion(region), x, y, width, height);
		}else if(type == LayerType.PARTICLE){
			particle.setPosition(x, y);
			particle.draw(renderer.batch(), Gdx.graphics.getDeltaTime());
		}

	}

	public Layer add(){
		list.add(this);
		return this;
	}

	public Layer addShadow(){
		return addShadow(/*"shadow" + (int)(atlas.findRegion(region).getRegionWidth() * 0.9f / 2f) * 2*/ null);
	}

	public Layer addShadow(String name){
		if(name != null){
			Layer shadow = obtainLayer();
			shadow.region = name;
			shadow.setPosition(x, y ).setTemp().setSort(SortType.FLOOR).setLayer(shadowlayer).add();
		}
		/*
		Layer shadow = obtainLayer();
		shadow.region = region;//name;
		shadow.type = LayerType.VERTICESPRITE;
		shadow.vshiftx = atlas.findRegion(region).getRegionWidth()/1.6f;
		shadow.vshifty = -atlas.findRegion(region).getRegionHeight()/10f;
		shadow.setPosition(x, y - atlas.regionHeight(region) / 2).setTemp().setLayer(shadowlayer).add();
		*/
		//newShadow();
		//newShadow().setLayer(layer-0.1f).setColor(shadowcolor);

		addBlobShadow();
		addReflection();
		return this;
	}

	Layer newShadow(){
		Layer shadow = obtainLayer();
		shadow.region = region;//name;
		shadow.type = LayerType.VERTICESPRITE;
		shadow.vshiftx = atlas.findRegion(region).getRegionWidth() / 1.6f;
		shadow.vshifty = -atlas.findRegion(region).getRegionHeight() / 10f;
		shadow.setPosition(x, y - atlas.regionHeight(region) / 2).setTemp().setLayer(shadowlayer).add();
		return shadow;
	}

	public Layer addBlobShadow(){
		addBlobShadow(0);
		return this;
	}

	public Layer addBlobShadow(float offset){
		Layer shadow = obtainLayer();
		String region = "shadow" + (int)(atlas.findRegion(this.region).getRegionWidth() * 0.9f / 2f) * 2;
		shadow.region = region;//name;
		shadow.setSort(SortType.FLOOR).setPosition(x, y + offset).setTemp().setLayer(shadowlayer).add();
		return this;
	}

	public void addReflection(){
		//if(this.region.equals("player")) Koru.log(reflectionlayer);
		Layer reflection = obtainLayer();
		reflection.region = region;
		reflection.setSort(SortType.FLOOR).setType(LayerType.REFLECTION).setPosition(x, y - atlas.regionHeight(region) / 2).setColor(color).setTemp().setScale(1f, -1f).setLayer(reflectionlayer - (0.01f) * (y % 1000) / 1000f).add();
	}

	public float height(){
		return atlas.regionHeight(region);
	}

	public Layer setHeightOffset(float offset){
		this.heightoffset = offset;
		return this;
	}

	public Layer yLayer(){
		return yLayer(this.y, true);
	}

	public Layer yLayer(boolean shadow){
		return yLayer(this.y, shadow);
	}

	public Layer yLayer(float y, boolean shadow){
		alignbottom = true;
		layer = heightoffset + y;
		setSort(SortType.OBJECT);
		if(type == LayerType.SPRITE && shadow) addShadow();
		return this;
	}

	public Layer update(float x, float y){
		setPosition(x, y);
		yLayer();
		add();
		return this;
	}

	public static Layer obtainLayer(){
		return list.getLayer();
	}

	public void free(){
		list.pool.free(this);
	}

	protected Layer(){

	}

	public Layer setTemp(){
		this.temp = true;
		return this;
	}

	public Layer alignBottom(){
		this.alignbottom = true;
		return this;
	}

	public Layer set(Layer layer){
		return this.set(layer.region, layer.x, layer.y).setColor(layer.color).setScale(layer.scalex, layer.scaley).setType(layer.type).setTexture(layer.texture).setRotation(layer.rotation);
	}

	public Layer setParticle(PooledEffect effect){
		this.setType(LayerType.PARTICLE).particle = effect;
		return this;
	}

	public Layer(String region, float x, float y){
		this.region = region;
		this.x = x;
		this.y = y;
	}

	public Layer setTexture(TextureRegion texture){
		this.texture = texture;
		return this;
	}

	public Layer setType(LayerType type){
		this.type = type;
		return this;
	}

	public Layer setSort(SortType type){
		this.sort = type;
		return this;
	}

	public Layer setTile(int id){
		return this.setSort(SortType.FLOOR).setLayer( id * 2);
	}

	public Layer setPosition(float x, float y){
		this.x = x;
		this.y = y;
		return this;
	}

	public Layer translate(float x, float y){
		return setPosition(this.x + x, this.y + y);
	}

	public Layer setText(String text){
		this.text = text;
		return this;
	}

	public Layer setScale(float scale){
		return setScale(scale, scale);
	}

	public Layer setScale(float scalex, float scaley){
		this.scalex = scalex;
		this.scaley = scaley;
		scaled = true;
		return this;
	}

	public Layer setLayer(float layer){
		this.layer = layer;
		return this;
	}

	public Layer setColor(Color c){
		color = c;
		return this;
	}

	public Layer setRotation(float rotation){
		this.rotation = rotation;
		return this;
	}

	public Layer rotate(float rotation){
		this.rotation += rotation;
		return this;
	}

	public Layer set(String region, float x, float y){
		this.region = region;
		this.x = x;
		this.y = y;
		return this;
	}

	public Layer setShape(float width, float height){
		this.width = width;
		this.height = height;
		this.type = LayerType.SHAPE;
		return this;
	}

	public void clear(){
		region = "";
		layer = 0;
		x = 0;
		y = 0;
		rotation = 0;
		color = Color.WHITE.cpy();
		type = LayerType.SPRITE;
		scalex = 1f;
		scaley = 1f;
		scaled = false;
		texture = null;
		temp = false;
		alignbottom = false;
		heightoffset = 0;
		width = 0;
		height = 0;
		sort = SortType.OBJECT;
		//if(particle != null) particle.free();
	}

	@Override
	public int compareTo(Layer s){
		if(sort != s.sort) return sort.ordinal() < s.sort.ordinal() ? -1 : 1;
		if(MathUtils.isEqual(layer, s.layer)) return 0;
		return sort.compare(this, s) ? 1 : -1;
	}

	@Override
	public void reset(){
		clear();
	}
}