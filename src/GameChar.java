import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JTextArea;

public class GameChar implements Serializable {
	private static final long serialVersionUID = 2L;
	Map gameMap;
	JTextArea transcript;
	int rowCoord = 0; // The first coordinate in the ordered pair. Associated with North (negative) and South (positive).
	int columnCoord = 0; // The second coordinate. Associated with East (positive) and West (negative).
	final int MINROW = 0;
	int MAXROW;
	final int MINCOL = 0;
	int MAXCOL;
	int view = 1;
	
	ArrayList<String> inventory = new ArrayList<String>();
	
	public GameChar(Map map, JTextArea transcript) {
		gameMap = map;
		this.transcript = transcript;
		MAXROW = gameMap.mapSize[0]-1;
		MAXCOL = gameMap.mapSize[1]-1;
	}
	
	public void go(String[] userInput) {
		if (userInput.length == 0) {
			transcript.insert("You must indicate a direction.\n", transcript.getText().length());
		}
		else {
			char direction = Character.toLowerCase(userInput[0].charAt(0));

			if (direction == 'n') {
				if (rowCoord > MINROW) {
					transcript.insert("Moving north . . .\n", transcript.getText().length());
					rowCoord--;
				}
				else {
					transcript.insert("You can't go that far north.\n", transcript.getText().length());
				}
			}
			else if (direction == 'e') {
				if (columnCoord < MAXCOL) {
					transcript.insert("Moving east . . .\n", transcript.getText().length());
					columnCoord++;
				}
				else {
					transcript.insert("You can't go that far east.\n", transcript.getText().length());
				}
			}
			else if (direction == 's') {
				if (rowCoord < MAXROW) {
					transcript.insert("Moving south . . .\n", transcript.getText().length());
					rowCoord++;
				}
				else {
					transcript.insert("You can't go that far south.\n", transcript.getText().length());
				}
			}
			else if (direction == 'w') {
				if (columnCoord > MINCOL) {
					transcript.insert("Moving west . . .\n", transcript.getText().length());
					columnCoord--;
				}
				else {
					transcript.insert("You can't go that far west.\n", transcript.getText().length());
				}
			}
			else {
				transcript.insert("You can't go that way.\n", transcript.getText().length());
			}
			printLocation();
		}
	}
	
	public void take(String desiredItem) {
		int[] pair = {rowCoord, columnCoord};
		int[] mapKey = null;
		String foundItem = null;
		
		for (int[] key : gameMap.items.keySet()) { // Find the items at the current location.
			if (Arrays.equals(pair, key)) {
				mapKey = key; // Save the key. We'll need it to modify the contents at that key.
			}
		}
		if (mapKey != null) { // Items are available to pick up.
			ArrayList<String> loot = gameMap.items.get(mapKey);
			for (String item : loot) {
				if (item.equals(desiredItem)) { // The user-specified item was found at the current location.
					foundItem = item;
				}
			}
			if (foundItem != null) { // Can't modify loot within the foreach loop in case we remove a future iteration.
				inventory.add(foundItem);
				loot.remove(foundItem);
				transcript.insert("Picked up: " + foundItem + "\n", transcript.getText().length());
			}
		}
		if (mapKey == null || foundItem == null) {
			transcript.insert("There is no \"" + desiredItem + "\" here to pick up.\n", transcript.getText().length());
		}
	}
	
	public void drop(String toDrop) {
		int[] pair = {rowCoord, columnCoord};
		String foundItem = null;
			
		for (String item : inventory) { // Make sure you have the item to drop.
			if (item.equals(toDrop)) {
				foundItem = item;
			}
		}
		
		if (foundItem != null) {
			inventory.remove(foundItem);
			gameMap.addItem(pair, foundItem);
			transcript.insert("You dropped \"" + foundItem + "\".\n", transcript.getText().length());
		}
		else {
			transcript.insert("You aren't carrying \"" + toDrop + "\" to drop.\n", transcript.getText().length());
		}
	}
	
	public void inventory() {
		transcript.insert("You are carrying:\n", transcript.getText().length());
		for (String item : inventory) 
		{
			transcript.insert(item + "\n", transcript.getText().length());
		}
		if (inventory.isEmpty()) {
			transcript.insert("Nothing.\n", transcript.getText().length());
		}
	}
	
	public void printLocation() {		
		transcript.insert("You are at location " + rowCoord + "," + columnCoord + " in terrain: " + gameMap.terrainTypes.get(Character.toString(gameMap.terrain(rowCoord, columnCoord))) + "\n", transcript.getText().length());
		// gameMap.printMinimap(rowCoord, columnCoord, view); // Doesn't seem like we need to print the minimap in part 3 of the project.
	}
}
