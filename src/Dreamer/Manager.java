package Dreamer;

import interfaces.*;

import java.util.HashSet;
import java.util.Collection;
import java.io.Serializable;

public class Manager {
	
	static HashSet<Serializable> masterList = new HashSet<>(2000);
	
	static HashSet<Drawable> activeDrawingSet = new HashSet<>(2000);
	
	static SaferTreeSet<Updateable> updateSet = new SaferTreeSet<>(new UpdateComparator());
	
	static PerformanceMonitor performance = new PerformanceMonitor("drawActive");
	static boolean debug = false, drawing = false, trackview = false;
	static int count = 0;
	static Collection<Manageable> emptyList = new java.util.ArrayList<>();
	
	public static void add(Object o) {
	
		updateSet.tryAdd(o);
		Collider.tryAdd(o);
		if(o instanceof Serializable)
			masterList.add((Serializable)o);
		else
			System.err.println("Manager.java 29: Warning! " + o.toString() + " not Serializable");	
	}
	
	public static void remove(Object o) {
	
		updateSet.tryRemove(o);
		Collider.tryRemove(o);
		masterList.remove((Serializable)o);
	}
	
	public static void updateAll() {		
		
		java.util.Collection<Updateable> updatingThings 
			= new java.util.LinkedHashSet<>();
		updatingThings.addAll(updateSet);
		updatingThings.stream().forEach( (x)-> x.update() );	
	}
	
	public static void activateVisible() {
		
		for (Object o: masterList)
			
			if(o instanceof Drawable)
			
				if (((Drawable)o).isVisible())
					activeDrawingSet.add((Drawable)o);
	}

	static void drawActive() {

		drawing = true;
		count = 0;
		
		performance.clear();
		performance.start();
		
		for (Drawable d : Background.background) {
			
			count++;
			Light.light(d);
			d.draw();
			performance.mark(count + "," + d.toString());
		}

		for (Drawable d : activeDrawingSet) {
			
			count++;
			Light.light(d);
			d.draw();
			performance.mark(count + "," + d.toString());
		}

		Face.drawFaces();
		performance.mark("Faces");

		for (Drawable d : Foreground.foreground) {
			
			count++;
			d.draw();
			performance.mark(count + "," + d.toString());
		}

		performance.sort();
		drawing = false;
	}

	static void clearAll() {

		masterList.clear();
		activeDrawingSet.clear();
		updateSet.clear();
		Collider.clear();
		Background.background.clear();
		Foreground.foreground.clear();
		Light.clearAll();
	}

	static void clearActive() {

		activeDrawingSet.clear();
	}
}

class UpdateComparator implements java.util.Comparator<Updateable> {
	
	@Override
	public int compare(Updateable arg0, Updateable arg1) {
	
		return (arg0.isPriority() && !arg1.isPriority())? 1 : -1;
	}
}