package Dreamer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

public class Camera {
	
	public enum Function {
		ORIGINAL, NEW
	}
	public static Function mode = Function.ORIGINAL;
	private static float angle = 0f;
	private static Rectangle prescaledScene  = new Rectangle(
			-Constants.screenWidth / 2, 
			-Constants.screenHeight / 2, 
			Constants.screenWidth, 
			Constants.screenHeight);
	private static Rectangle scene = prescaledScene;
	private static float centerX, centerY, centerZ = 2000;
	private static float scale = 1;
	static float focalLength = 2000;
	private static Element target;
	static boolean zoom = false;
	private static String nextMovement = "stop";
	private static int velocity = 0;
	
	static int zoomLength;
	
	//Matrix4f projectionMatrix = new Matrix4f();
	static float tempDistance;
	static Vector4f rotated = new Vector4f();
	static Vector3f tempV3f = new Vector3f();
	static Vector3f translated = new Vector3f();
	
	static void draw(Graphics g)
	{	
		try {
			//print out the camera info and focus box
			g.setColor(Library.defaultFontColor);
			scene.setCenterX(Constants.screenWidth / 2);
			scene.setCenterY(Constants.screenHeight / 2);
			g.draw(scene);
			scene.setCenterX(0);
			scene.setCenterY(0);
			g.setColor(Library.defaultFontColor);
			g.drawString("CAMTARGET "+target.toString(), 20, 180);
			g.drawString("CAMCENTER@("+(int)getCenterX()+", "+(int)getCenterY()+", "+scale+")", 20, 200);
			g.drawString("CAMSCENE ("+(int)getMinX()+", "+(int)getMinY()+", "+(int)getMaxX()+", "+(int)getMaxY()+")", 20, 220);
			g.setColor(Library.defaultFontColor);
		} catch(NullPointerException e) {
			//camera unfocused
		}
	}
	static void update() {
		
		switch (nextMovement) {
		
	        case	"zoom_in":
	    		nudge(0, 0, velocity);
	    		zoomLength--;
	    		velocity++;
				break;
				
	        case	"zoom_out":
	    		nudge(0, 0, -velocity);
	    		zoomLength++;
	    		velocity++;
				break;
				
	        case	"up":
				nudge(0, velocity, 0);
				velocity++;
				break;
				
	        case	"down":
				nudge(0, -velocity, 0);
				velocity++;
				break;
				
	        case	"left":
				nudge(-velocity, 0 ,0);
				velocity++;
				break;
				
	        case	"right":
				nudge(velocity, 0, 0);
				velocity++;
				break;
				
	        case "stop":
	        	velocity = 0;
	        	break;
		}
		
		if(target != null) {
			focus(target);
			if(target instanceof Updateable)
				((Updateable) target).update();
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_RBRACKET))
			angle += 0.000002f;
		if(Keyboard.isKeyDown(Keyboard.KEY_LBRACKET))
			angle -= 0.000002f;
		angle = angle % (float)(2 * Math.PI);
	}
	/**focuses the camera on a specific element
	 * 
	 * that element is casted to a ClassFocus with initial maxDistance 0
	 * if the cast is appropriate the maxDistance will be the maximum
	 * distance between the farthest apart objects of those classes and
	 * will be used to scale the camera
	 * 
	 * @param e element to focus on
	 * @return this camera(why? no good reason)
	 */
	static void focus(Element e) {	
		if(!zoom) {
			try {
				ClassFocus cf = (ClassFocus)e;
				if ((cf.maxDistance + Constants.VIEWMARGIN) > Constants.screenHeight)
					scale = Constants.screenHeight / (cf.maxDistance + Constants.VIEWMARGIN);
				else 
					scale = 1;
			} catch(ClassCastException cce) {
				//not a valid classFocus, normal scaling in effect
				scale = 1;
			}
			try {
				switch(mode) {
				case NEW:
					target = e;
					angle = -(float)Math.atan2(centerX - e.getX(), centerZ - e.getZ());
					centerY = e.getY();
					break; 
				default: 
					target = e;
					centerX = e.getX();
					centerY = e.getY();
					break;
				}
			} catch(NullPointerException n) {
				System.err.println("Camera not focused before updating!");
			}
		}
	}
	//nudge to adjust camera position
	static void nudge(float x, float y, float z) {
		centerX += x;
		centerY += y;
		centerZ += z;
	}
	//getters
	static float getMinX() {return (scene.getMinX() / scale) + centerX;}
	static float getMaxX() {return (scene.getMaxX() / scale) + centerX;}
	//TODO document what is going on here
	//since the screen is drawn flipped in the y-dir all kinds of things do not make logical sense
	//maybe the draw methods could be made more transparent?
	static float getMinY() {return (scene.getMinY() / scale) + centerY;}
	static float getMaxY() {return (scene.getMaxY() / scale) + centerY;}
	static float getWidth() {return scene.getWidth() / scale;}
	static float getHeight() {return scene.getHeight() / scale;}

	static float getScale() {return scale;}
	
	static float getCenterX() {return centerX;}
	static float getCenterY() {return centerY;}
	static float getCenterZ() {return centerZ;}

	static boolean isPointVisible(float x, float y, float z) {
		translate(x, y, z, tempV3f);
		if(tempV3f.x > 0)
			if(tempV3f.x < Constants.screenWidth)
				if(tempV3f.y > 0)
					if(tempV3f.y < Constants.screenHeight)
						return true;
		return false;
	}
	static Vector3f translateMouse(int x, int y) {
		return new Vector3f(
				((float)x / Constants.screenWidth * getWidth()) + getMinX(),
				((float)y / Constants.screenHeight * getHeight()) + getMinY(),
				0
				);
	}
	//this is the master translate method, this vector should be used ASAP after returning
	static Vector3f translate(float x, float y, float z, Vector3f result) {
		tempV3f.set(x - getCenterX(), y - getCenterY(), z - getCenterZ());
		tempDistance = tempV3f.x + tempV3f.y + tempV3f.z;
		switch(mode) {
			case NEW:
				rotated = Vector.rotate(0, 1, 0, tempV3f, angle);
				z = Math.abs(focalLength / rotated.z);
				x = rotated.x * z * tempDistance / focalLength;
				y = rotated.y * z * tempDistance / focalLength;
				break;
			default: 
				z = Math.abs(focalLength / tempV3f.z);
				x = tempV3f.x * z;
				y = tempV3f.y * z;
				break;
		}
		result.set(			
				x + Constants.screenWidth / 2,
				-y + Constants.screenHeight / 2,
				Math.min(1, Math.max(1 - z / 10000, 0))
				);
		return result;
	}
	static Vector3f translate(float x, float y, float z) {
			return translate(x, y, z, translated);
	}
	static Vector3f translate(Vector3f vector3f, Vector3f result) {
		return translate(vector3f.x, vector3f.y, vector3f.z, result);
	}
	public static void print() {
		String s = "camera: ";
		s = s.concat("minX: "+getMinX());
		s = s.concat(" minY: "+getMinY());
		s = s.concat(" maxX: "+getMaxX());
		s = s.concat(" maxY: "+getMaxY());
		System.out.println(s);
	}
	public static void command(String s) {
		nextMovement = s;
	}
}