package Dreamer;

import org.newdawn.slick.Color;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Vector2f;

abstract class Weapon extends Shape3d implements Updateable {
	
	private static final long serialVersionUID = 4519324822338846058L;	
	Face f;
	Actor actor;
	String name = "";
	int damage, carryHeight, width, height, cuttingEdge, weight;
	int direction, LEFT = 1, RIGHT = -1;
	WeaponCollision weaponCollision;
	Line weaponLine; 
	protected Vector2f blockPosition;
	protected Vector2f weaponPoint = new Vector2f();
	protected float weaponAngle = 0, blockAngle = 20;
	float texWidth, texHeight;
	int[] attackAngles;
	int[] attackOffsetX;
	int[] attackOffsetY;

	Weapon(Actor a) {
		weaponCollision = new WeaponCollision(this); 
		attach(a);
		addVertex(0, 0, 0);
		addVertex(0, 100, 0);
		addVertex(100, 100, 0);
		addVertex(100, 0, 0);
	}
	@Override
	boolean isVisible() {
		return true;
	}
	@Override
	void draw() {
		super.draw();
		if(Element.debug)
			if(weaponLine != null)
				Drawer.drawShape(weaponLine, Color.black);
	}	
	void makeFace() {
		f = new Face(Library.getTexture(name), new Color(1, 1, 1, 1.0f), 0, 1, 2, 3);
		width = Library.getImage(name).getWidth(); 
		height = Library.getImage(name).getHeight();
		texWidth = textureStretch(width);
		texHeight = textureStretch(height);
		f.setTexturePoints(0, 0,  texWidth, texHeight);			
		addFace(f);
	}
	
	void attack() {		
		Actor temp;
		updateCollision();
		Shape s = weaponCollision.getCollisionShape();
		for(Element e: Collidable.getActiveWithin(s)) {
			//very important to not compare this to itself, infinite loop
			if(Actor.class.isAssignableFrom(e.getClass()) && e != actor) {
				temp = (Actor)e;
				float dir = (temp.checkStatus("left"))? 1: -1;
				
				if (temp.getCollisionShape().intersects(s) && 
					temp.checkStatus("blocking")) {
						temp.dynamics.adjustVel(2 * dir, 0);
						actor.dynamics.adjustVel(-7 * dir, 0);
				} else if(temp.getCollisionShape().intersects(s) && !temp.checkStatus("damaged")) {
					temp.takeDamage(this.damage, s.getCenterX());
					temp.addStatus("damaged");
					temp.dynamics.adjustVel(8 * dir, 0);
				}	
			}
		}
		weaponCollision.remove();
	}
	void updateCollision() {
		weaponCollision.remove();
		weaponCollision.setCollisionShape(new Line(
				weaponPoint.x, 
				weaponPoint.y, 
				weaponPoint.x + cuttingEdge * (float)Math.sin((Math.PI * weaponAngle) / 180), 
				weaponPoint.y + cuttingEdge * (float)Math.cos((Math.PI * weaponAngle) / 180)
				)
		);
		weaponCollision.add();
	}
	void attach(Actor a) {
		if (a.weapon  != this) {
			actor = a;
			
			if(a.weapon != null)
				a.weapon.detach();	
			a.weapon = this;

			updateCollision();
		}
	}
	void detach() {
		actor.weapon.updateCollision();
		actor.weapon = null;
		actor = null;
	}
	public void update() {
		if(actor != null) {
			if(actor.checkStatus("dead")) {
				updateCollision();
				detach();
				return;
			}
			
			// Set weapon on front or back of Actor depending on ladder
			setZ((actor.checkStatus("climbing") || actor.checkStatus("blocking")?2f:-10f));

			manhattanRadius.set(100, 100, 0);
			
			if (actor.checkStatus("attacking"))
				attack();
			// Offset this from actor's center
			int xOffset, yOffset, xBlockOffset = 0, yBlockOffset = 0;	
			int rotation = actor.body.weaponStage;
			
			// Set weapon in direction of actor
			int direction;
			if (actor.checkStatus("blocking")) {
				direction = actor.body.direction;
				xBlockOffset = (int)blockPosition.x;
				yBlockOffset = (int)blockPosition.y;
				weaponAngle = -direction * blockAngle;
			} else {
				direction = (actor.checkStatus("left"))?LEFT:RIGHT;
				if (direction == LEFT)
					f.setTexturePoints(texWidth, 0, 0, texHeight);
				else
					f.setTexturePoints(0, 0, texWidth, texHeight);
				weaponAngle = -direction * attackAngles[rotation];
			}
			xOffset = attackOffsetX[rotation] + xBlockOffset;
			yOffset = carryHeight + yBlockOffset;	
			yOffset += attackOffsetY[rotation];
			weaponPoint.set(actor.getX() - direction * xOffset, actor.getY() + yOffset);
			
			float cos = (float)Math.cos((Math.PI * weaponAngle) / 180);
			float sin = (float)Math.sin((Math.PI * weaponAngle) / 180);

			vertices.get(0).set(
					weaponPoint.x - cos * width / 2, 
					weaponPoint.y + sin * width / 2,
					-0.1f
					); 
			vertices.get(1).set(
					weaponPoint.x + sin * height - cos * width / 2,
					weaponPoint.y + cos * height + sin * width / 2,
					-0.1f
					); 
			vertices.get(2).set(
					weaponPoint.x + sin * height + cos * width / 2,
					weaponPoint.y + cos * height - sin * width / 2,
					-0.1f
					); 
			vertices.get(3).set(
					weaponPoint.x + cos * width / 2, 
					weaponPoint.y - sin * width / 2,
					-0.1f
					); 
		}
	}
	int getWeight() {
		return weight;
	}
}
class Katana extends Weapon {
	
	private static final long serialVersionUID = 8042539459172540290L;

	Katana(Actor a) {
		super(a);
		name = "katana";
		damage = 25;
		weight = 20;
		carryHeight = 50;
		cuttingEdge = 100;
		
		blockPosition = new Vector2f(-2, -28);
		attackAngles = new int[]{250, 275, 310, 325, 60, 95};
		attackOffsetX = new int[]{48, 15, 22, 24, 22, 15};
		attackOffsetY = new int[]{-31, -38, -38, -39, -47, -57};
		makeFace();
	}
}
class Knife extends Weapon {
	Knife(Actor a) {
		super(a);
		name = "knife";
		damage = 10;
		weight = 10;
		carryHeight = 50;
		cuttingEdge = 100;
		
		blockPosition = new Vector2f(-2, -28);
		attackAngles = new int[]{250, 275, 310, 325, 60, 95};
		attackOffsetX = new int[]{40, 15, 22, 24, 22, 15};
		attackOffsetY = new int[]{-50, -38, -38, -39, -47, -57};
		makeFace();
	}
}
class Battleaxe extends Weapon {	
	
	private static final long serialVersionUID = -419016855374838000L;

	Battleaxe(Actor a) {
		super(a);
		name = "battleaxe";
		damage = 15;
		carryHeight = 3;
		makeFace();
	}
}
class Naginata extends Weapon {
	
	private static final long serialVersionUID = 4375430368264040154L;
	
	Vector2f block = new Vector2f(-2, -28);
	Naginata(Actor a) {
		super(a);
		name = "naginata";
		damage = 30;
		weight = 30;
		carryHeight = 55;
		cuttingEdge = 160;
		
		blockPosition = new Vector2f(15, -15);
		attackAngles = new int[]{-25, 0, 10, 30, 60, 90};
		attackOffsetX = new int[]{20, 20, 15, -10, -30, -20};
		attackOffsetY = new int[] {-90, -85, -85, -97, -85, -65};
		makeFace();
	}
}
class WeaponCollision extends Collidable {
	
	
	private static final long serialVersionUID = 4862178711404157907L;
	
	Weapon weapon;
	WeaponCollision(Weapon w) {
		weapon = w;
	}
	@Override
	boolean collide(Actor a) {
		if(weapon.actor == null)
			if(a.getCollisionShape().intersects(getCollisionShape()) && a.checkStatus("acting")) {
				weapon.attach(a);
				a.removeStatus("acting");
				return true;
			}
		return false;
	}
}