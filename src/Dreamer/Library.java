package Dreamer;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.util.ResourceLoader;

class Library {
	private static HashMap<String, ImageTracker> images = new HashMap<String, ImageTracker>();
	private static HashMap<String, File> models = new HashMap<String, File>();
	static Texture tempTexture = null;	
    static Font font;// = new Font("Courier", Font.BOLD, 60);
    static TrueTypeFont messageFont;// = new TrueTypeFont(font, false);
    static Color messageFontColor = Color.black;
	static Color messageFontShadow = Color.gray;
	static TrueTypeFont defaultFont;
	static Color defaultFontColor = Color.black;
    static boolean messages = false;
	
	//main tests loading the current images 
	public static void test() throws IOException {
		System.out.println("Initializing Library");
		Dreamer.init();
		Library.messages = true;
		Library.load();
	}
	static void load() throws IOException {
		importFonts();
		importArt();
	}
	public static void addImage(String string, ImageTracker temp) {
		images.put(string, temp);
	}
	static Image getImage(String s, float f) {
		return images.get(s).scale(f);
	}
	static Image getImage(String s) {
		return images.get(s).original();
	}
	static Shape3d getModel(String s) {
		Shape3d m = new Shape3d();
		try {
			m = ObjLoader.loadModel(models.get(s));
		} catch (FileNotFoundException e) {
			//model not found
			e.printStackTrace();
		} catch (IOException e) {
			//model not found in a different way
			e.printStackTrace();
		}
		return m;
	}
	static Shape3d getModel(String s, int scale) {
		Shape3d m = new Shape3d();
		try {
			m = ObjLoader.loadModel(models.get(s), scale);
		} catch (FileNotFoundException e) {
			//model not found
			e.printStackTrace();
		} catch (IOException e) {
			//model not found in a different way
			e.printStackTrace();
		}
		return m;
	}
	static Texture getTexture(String s) {
		return images.get(s).image.getTexture();
	}
	static private void loadModel(String file) {
		String referrenceName = file.substring(0, file.length());
		referrenceName = referrenceName.replace(Constants.RESPATH, "");
		try {
			String modelName = referrenceName.replace("models/", "").replace(".obj", "");
            models.put(modelName, new File("res/" + referrenceName));
        } catch (Exception e) {
            System.out.println("Failure to load the model file: " + referrenceName);
            e.printStackTrace();
        }
	}
	static void loadImage(String file) {
		String referrenceName = file.substring(0, file.toString().lastIndexOf("."));
    	referrenceName = referrenceName.replace(Constants.RESPATH, "");
		
		ImageTracker tempImage = new ImageTracker(referrenceName);
    	images.put(referrenceName, tempImage);
	}
	static void importArt() throws IOException {
		Files.walk(Paths.get(Constants.RESPATH)).forEach(filePath -> {
			if (Files.isRegularFile(filePath)) {
				if (filePath.toString().contains(".obj"))
					loadModel(filePath.toString());
				else if (filePath.toString().contains(".png"))
					loadImage(filePath.toString()); 
			}
		});
	}
	static void importFonts() {
		// load font from a .ttf file
		InputStream inputStream;

		Font chunk;
 		try {
			inputStream = ResourceLoader.getResourceAsStream(Constants.RESPATH + Constants.FONTSPATH + "8-bit Madness.ttf");
			chunk = Font.createFont(Font.TRUETYPE_FONT, inputStream);
			chunk = chunk.deriveFont(60f); // set font size
			messageFont = new TrueTypeFont(chunk, true);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    font = new Font("Serif", Font.BOLD, 14);
	    defaultFont = new TrueTypeFont(font, true);
	}
}
/*ImageTracker: this keeps a scaled reference of an Image and updates this as necessary
 *TODO probably should switch all Images besides backgrounds and foregrounds to textures
 */
class ImageTracker {
	
	Image image;
	Image scaledImage;
	int scale = 100;
	
	ImageTracker() {}
	
	ImageTracker(String s) {
		try {
			image =  new Image(Constants.RESPATH+s+".png", true, Image.FILTER_NEAREST);
			scaledImage = image.copy();
			if(Library.messages) System.out.println("Image import of "+s+".png successful");
		} catch (RuntimeException e) {
			System.err.println("Image import of "+s+".png failed");
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Image import of "+s+".png failed");
			e.printStackTrace();
		}
	}
	Image scale(float f) {
		if((int)(f * 100) != scale)
		{
			scale = (int)(f * 100);
			scaledImage = image.getScaledCopy(f);
		}
		return scaledImage;
	}
	Image original() {return image;}
}
