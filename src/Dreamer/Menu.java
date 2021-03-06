package Dreamer;

import enums.FontType;
import enums.Justification;
import interfaces.Performable;

public class Menu {

	Menu(Justification j, float xPosition, float yPosition) {

		justification = j;
		xposition = xPosition;
		yposition = yPosition;
	}

	private Menu parent;
	java.util.List<MenuOption> optionList = new java.util.ArrayList<MenuOption>();
	Justification justification = Justification.LEFT;
	float spacing = 40, xposition = 0, yposition = 0;
	int currentOption = 0;
	
	void setFont(FontType f) {
		
		optionList.stream().forEach( (x)-> {
			x.shadowMessage.font = f;
		});
	}
	
	void open() {

		if (parent != null)
			parent.exit();
		Keys.saveKeys();
		Keys.openMenuKeys(this);
		optionList.get(currentOption).shadowMessage.highlight = true;
		for (MenuOption mo : optionList)
			mo.shadowMessage.add();
	}

	void command(String s) {
		
		int size = optionList.size();

		switch (s) {

		case "up":
			currentOption = (size + currentOption - 1) % size;
			optionList
				.stream()
				.forEach(
					(o) -> o.shadowMessage.position.y -= (currentOption == size - 1) ? 
						-spacing * (size - 1) : spacing);
			break;

		case "down":
			currentOption = (currentOption + 1) % size;
			optionList
				.stream()
				.forEach(
					(o) -> o.shadowMessage.position.y += (currentOption == 0) ? 
						-spacing * (size - 1) : spacing);
			break;

		case "select":
			Performable d = optionList.get(currentOption).performable;
			if (d != null)
				d.perform();
			break;

		case "exit":
			exit();
			break;
		}

		for (int i = 0; i < size; i++) {
			optionList.get(i).shadowMessage.highlight = (i == currentOption) ? true
					: false;
		}
	}

	void exit() {

		Keys.restoreKeys();
		for (MenuOption mo : optionList) {
			mo.shadowMessage.remove();
		}
		
		if(parent != null) parent.open();
	}

	private class MenuOption {

		Performable performable;
		Text shadowMessage;	

		MenuOption(String s, Performable d) {
			performable = d;
			shadowMessage = new Text(s, 0, 0);
		}

		MenuOption setPosition(float x, float y) {
			shadowMessage.setPosition(x, y, 0);
			return this;
		}

		MenuOption setJustification(Justification j) {
			shadowMessage.justification = j;
			return this;
		}
	}

	Menu addExitOption() {
		addOption("EXIT MENU", () -> {
			exit();
		});
		return this;
	}

	Menu addOption(String s, Performable d) {
		optionList.add(new MenuOption(s, d).setPosition(xposition,
				yposition -= spacing).setJustification(justification));
		return this;
	}

	Menu setParent(Menu m) {
		parent = m;
		return this;
	}
}