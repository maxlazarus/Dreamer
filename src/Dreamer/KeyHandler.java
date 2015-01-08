package Dreamer;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;

abstract public class KeyHandler {
	static boolean initialized =  false;
	//these next two fields cannot be changed without drastic consequences
	static final char[] alphabetPlus = (
			"1234567890-=" + 
			"qwertyuiop[]" + 
			"asdfghjkl;'" + 
			"zxcvbnm,./" + 
			" "
			).toCharArray();
	static final int[] codes = {
			2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, // 1234567890-=
			16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, // qwertyuiop[]
			30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, // asdfghjkl;'
			44, 45, 46, 47, 48, 49, 50, 51, 52, 53,// zxcvbnm,./
			Keyboard.KEY_SPACE
			};
	static Map<String, Integer>keyMap = new HashMap<String, Integer>();
	static Map<Integer, Action>actionMap = new HashMap<Integer, Action>();
	static String keyBuffer = "";
	
	public KeyHandler() {
		if(!initialized) {
			for(int i = 0; i < alphabetPlus.length; i++) {
				keyMap.put(alphabetPlus[i] + "", codes[i]);
			}
			initialized = true;
		}
	}
	
	public static boolean test(Integer keyCode) {
		return Keyboard.isKeyDown(keyCode);
	}
	public static boolean test(String s) {
		return Keyboard.isKeyDown(keyMap.get(s));
	}
	public static void clearKeys() {
		actionMap.clear();
	}
	public static boolean addKey(Integer i, Action a) {
		actionMap.put(i, a);
		return true;
	}
	public static boolean addKey(char c, Action a) {
		return addKey(keyMap.get(c + ""), a);
	}
	
	public static void getKeys() {
		while (Keyboard.next()) {
			Integer keyNum = Keyboard.getEventKey();
			if(Keyboard.getEventKeyState()) {
				try {
					actionMap.get(keyNum).perform();
				} catch(NullPointerException e) {
					//not there, no bigs
				}
			} else {
				try {
					actionMap.get(keyNum).stop();
				} catch(NullPointerException e) {
					//not there, no bigs
				}
			}
		}
	}
	public static void openGameKeys() {
		clearKeys();
		new ZoomKeys().add();
		new FunctionKeys().add();
		new WASDKeys(Player.getFirst()).add();
	}
}
class FunctionKeys extends  KeyHandler {
	void add() {
		addKey(
				'p',
				new Action() {
					void perform() {
						Element.printActive();
					}
				}
		);
		addKey(
				't',
				new Action() {
					void perform() {
						Element.debug = !Element.debug;
					}
				}
		);
		addKey(
				'c',
				new Action() {
					void perform() {
						if(Library.defaultFontColor == Color.black) {
							Library.defaultFontColor = Color.cyan;
							Library.messageFontColor = Color.red;
						} else {
							Library.defaultFontColor = Color.black;
							Library.messageFontColor = Color.blue;
						}
					}
				}
		);
		addKey(
				'1',
				new Action() {
					void perform() {
						new TestLevel();
					}
				}
		);
		addKey(
				'2',
				new Action() {
					void perform() {
						new SimpleLevel();
					}
				}
		);
		addKey(
				'3',
				new Action() {
					void perform() {
						new BirdLevel();
					}
				}
		);
		addKey(
				'4',
				new Action() {
					void perform() {
						new ForestLevel();
					}
				}
		);
		addKey(
				'm',
				new Action() {
					void perform() {
						new MainMenu();
					}
				}
		);
		addKey(
				'i',
				new Action() {
					void perform() {
						for(Player p: Player.list) {
							p.reset();
							new ForestLevel();
						}
					}
				}
		);
	}
}
class EditorKeys extends KeyHandler {
	Editor editor;
	
	EditorKeys(Editor e){
		editor = e;
	};
	
	void add() {
		for(int i = 0; i < alphabetPlus.length; i++) {
			char key = alphabetPlus[i];
			addKey(
				codes[i], 
				new Action() {
					void perform() {
						KeyHandler.keyBuffer = KeyHandler.keyBuffer + key;
						editor.console.name = KeyHandler.keyBuffer;
					}
				}
			);
		}
		addKey(
			Keyboard.KEY_RETURN, 
			new Action() {
				void perform() {
					KeyHandler.keyBuffer = "";
					editor.console.name = KeyHandler.keyBuffer;
				}
			}
		);
		addKey(
			Keyboard.KEY_TAB,
			new Action() {
				void perform() {
					KeyHandler.openGameKeys();
				}
			}
		);
	}
}
class ZoomKeys extends KeyHandler {
	void add() {
		addKey(
				'z', 
				new Action() {
					void perform() {
						Camera.zoom = !Camera.zoom;
					}
				}
			);
		addKey(
				Keyboard.KEY_COMMA, 
				new CameraAction("zoom_in")
			);
		addKey(
				Keyboard.KEY_PERIOD, 
				new CameraAction("zoom_out")
			);
		addKey(
				Keyboard.KEY_UP, 
				new CameraAction("up")
			);
		addKey(
				Keyboard.KEY_DOWN, 
				new CameraAction("down")
			);
		addKey(
				Keyboard.KEY_LEFT, 
				new CameraAction("left")
			);
		addKey(
				Keyboard.KEY_RIGHT, 
				new CameraAction("right")
			);
	}
}
class WASDKeys extends KeyHandler {
	Actor subject;
	
	public WASDKeys(Actor a) {
		super();
		subject = a;
	}
	
	void add() {
		
		{
			addKey(
					' ',
					new KeyedActorAction(subject, "tryjump")
			);
			addKey(
					'd',
					new KeyedActorAction(subject, "tryright")
			);
			addKey(
					'a',
					new KeyedActorAction(subject, "tryleft")
			);
			addKey(
					'w',
					new KeyedActorAction(subject, "up")
			);
			addKey(
					's',
					new KeyedActorAction(subject, "down")
			);
			addKey(
					'k',
					new KeyedActorAction(subject, "tryattack")
			);
			addKey(
					Keyboard.KEY_LSHIFT,
					new KeyedActorAction(subject, "trysprint")
			);
			addKey(
					'e',
					new KeyedActorAction(subject, "acting")
			);
			addKey(
					'l',
					new KeyedActorAction(subject, "blocking")
			);
		}
	}
}

