package Dreamer;

import interfaces.Manageable;
import interfaces.Updateable;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import enums.Status;
import static enums.Status.*;

public class Body extends Positionable implements Updateable { 	
	
	private static final long serialVersionUID = -752809389367312382L;
	Animation2 legs, body, head;
	Actor actor;
	int direction;
	Random r = new Random();
	int blinkCounter;
	int xOffset, yOffset;
	int LEGSPEED = 150, BODYSPEED = 40, HEADSPEED = 150; 
	int dmgCounter;
	public int weaponStage = 0;
	int attackHoldLength = 20;
	int attackCounter = 0;
	
	//for adjusting the body part animations' positions vertically
	int bodyAdjust = -5;
	int headAdjust = 30;
	// the point where items attach
	Vector2f beltPoint  = new Vector2f(-10, 7);
	
	Body(String s, Actor a) { this(s + "legs", s + "body", s + "head", a); }
	
	Body(String legs, String body, String head, Actor a) {
		
		actor = a;
		this.legs = new Animation2(legs, 6, 4, LEGSPEED);
		this.body = new Animation2(body, 6, 5, BODYSPEED);
		this.head = new Animation2(head, 6, 4, HEADSPEED);
		turnBody(LEFT);
		setParts();
	}
	
	// move Animations to match Actor position
	void setParts() {
		
		legs.setPosition(actor.getX(), actor.getY(), actor.getZ() - 0.05f);
		body.setPosition(actor.getX(), actor.getY() + bodyAdjust, actor.getZ());
		head.setPosition(actor.getX(), actor.getY() + headAdjust, actor.getZ() + 0.5f);
	}
	
	void turnBody(Status dir) {
	
		legs.setDirection(dir);
		body.setDirection(dir);
		head.setDirection(dir);
	}
	
	void reactToStatus() {
		// commonly used variables	
		boolean attacking = actor.checkStatus(ATTACKING);
		boolean jumping = actor.checkStatus(JUMPING);
		boolean climbing = actor.checkStatus(CLIMBING);
		boolean blocking = actor.checkStatus(BLOCKING);
		
		if (!blocking) {
			if (actor.checkStatus(Status.RIGHT))
				turnBody(RIGHT);	
			else 
				turnBody(LEFT);
		} 
		
		if (actor.checkStatus(DAMAGED)) {
			
			carryWeapon();
			head.selectRow(2);
			legs.stop();
			body.stop();
			head.stop();
			
			++dmgCounter;
			if (dmgCounter > Constants.DAMAGESTUN) {
				actor.removeStatus(DAMAGED);
				dmgCounter = 0;
			}
		} else {	
			
			if (Math.abs(actor.dynamics.getXVel()) > 1 && !climbing) {
			
				legs.selectRow(jumping ? 2 : 1);
				legs.setSpeed(jumping ? LEGSPEED : LEGSPEED * 6 / Math.abs(actor.dynamics.getXVel() + 1));
				legs.start();
			
			} else if ((actor.checkStatus(UP) || actor.checkStatus(DOWN)) && climbing) {
			
				legs.selectRow(3);
				legs.setSpeed(Math.abs(actor.dynamics.getYVel() + 1) * (Constants.VEL / 200));
				
				legs.start();
			} else if (climbing) {
			
				legs.selectRow(3);
				legs.setSpeed(Math.abs(actor.dynamics.getYVel() + 1) * (Constants.VEL / 200));
				legs.stop();
				
			} else {
				
				legs.selectRow(0);
				legs.reset();
			}				
			
			//body
			if (blocking) {
				
				carryWeapon(3);
				body.stop();
				body.reset();
				body.selectRow(4);
			
			} else if (attacking) {
			
				carryWeapon(body.currentIndex);
				
				if (body.currentIndex == body.framesWide() - 1) {
				
					++attackCounter;
					
					if (attackCounter == attackHoldLength) {
						actor.removeStatus(ATTACKING);
						attackCounter = 0;
					}
				} else {
					
					body.setLooping(false);
					body.selectRow(2);
					body.setSpeed(BODYSPEED);
					body.start();
				}
			} else if (jumping || Math.abs(actor.dynamics.getXVel()) > 1) {
				
				carryWeapon();
				body.setLooping(true);
				body.selectRow(1);
				
				if (jumping)
					body.setSpeed(Math.abs(1200 / Math.abs(actor.dynamics.getYVel() + 1)));
				else
					body.setSpeed(Math.abs(800 / Math.abs(actor.dynamics.getXVel() + 1)));
				body.start();
			
			} else if (climbing) {
			
				carryWeapon();
				body.setLooping(true);
				body.selectRow(3);
				body.setSpeed(Math.abs(actor.dynamics.getYVel() + 1) / (Constants.VEL / 200));
				body.stop();
			
			} else {
			
				carryWeapon();
				body.setLooping(true);
				body.selectRow(0);
				body.reset();
			}
			
			//head
			if (attacking || blocking) {
				
				head.selectRow(1);
				head.start();
			
			} else if ((actor.checkStatus(UP) || actor.checkStatus(DOWN)) && climbing) {
			
				head.selectRow(3);
				head.setSpeed(Math.abs(actor.dynamics.getYVel() + 1) / (Constants.VEL / 200));
				head.start();
		
			} else {
			
				if (blinkCounter > 0){
	
					blinkCounter--;
				
				} else if(r.nextInt(200) < 3) {
				
					head.selectRow(1);
					blinkCounter = r.nextInt(10) + 5;
				
				} else 
					head.selectRow(0);
			}
		}
	}
	// carry the Weapon as normal
	void carryWeapon() { carryWeapon(0); }
	// rotate weapon through attack sequence
	void carryWeapon(int i) { weaponStage = i; }
	
	void resetLegs() { legs.reset(); }
	
	void resetBody() { body.reset(); }
	
	void resetHead() { head.reset(); }
	
	Vector3f getHeadPosition() {
		return new Vector3f(head.getX(), head.getY(), head.getZ());
	}
	
	public void update() {
	
		reactToStatus();
		setParts();
	}
	
	public java.util.Collection<Manageable> getChildren() {
		
		java.util.Collection<Manageable> children = new java.util.HashSet<>();
		children.add(legs);
		children.add(body);
		children.add(head);
		return children;
	}
}

