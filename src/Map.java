import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class Map implements Serializable {
	private static final long serialVersionUID = 3L;
	JTextArea transcript;
	JPanel game;
	
	String mapFileName;
	String itemFileName;
	char[][] map;
	char[][] miniMap; // Map with one-thick border of "blank" territory. 
	int[] mapSize = {0,0}; // [rows, columns]
	int[] tileSize = {0,0}; // [height, width]
	HashMap<String, String> imageFiles = new HashMap<String, String>(); // Maps terrain symbols to image paths.
	HashMap<String, String> terrainTypes = new HashMap<String, String>(); // Maps terrain symbols to terrain descriptions.
	HashMap<int[], ArrayList<String>> items = new HashMap<int[], ArrayList<String>>(); // Item coordinates [row, column] mapped to ArrayList of items at that location. 
	
	public Map(String mapFileName, JTextArea transcript, JPanel game) {
		this.transcript = transcript;
		this.game = game;
		this.mapFileName = mapFileName;
		readMap();
	}
	
	public void readMap() {
		File file = new File(this.mapFileName);
		try {
			Scanner scanner = new Scanner(file);
			
			String dimensionLine = scanner.nextLine();
			String[] dimensions = dimensionLine.split(" +");
			mapSize[0] = Integer.parseInt(dimensions[0]); // Number of rows.
			mapSize[1] = Integer.parseInt(dimensions[1]); // Number of columns.
			map = new char[mapSize[0]][mapSize[1]]; // Initialize map to the proper size.
			
			for (int rowIterator = 0; rowIterator < mapSize[0]; rowIterator++) { // Read in the map.
				String line = scanner.nextLine();
				char[] lineArray = line.toCharArray();
				int colIterator = 0;
				for (char c : lineArray) {
					map[rowIterator][colIterator] = c;
					colIterator++;
				}
			}

			// Read in the tile size.
			dimensionLine = scanner.nextLine();
			dimensions = dimensionLine.split(" +");
			tileSize[0] = Integer.parseInt(dimensions[0]); // Height of the icon.
			tileSize[1] = Integer.parseInt(dimensions[1]); // Width of the icon.
			
			// Read in the item filename
			itemFileName = scanner.nextLine();
			
			while (scanner.hasNext()) { // Read the remaining lines mapping letters to image files.
				String line = scanner.nextLine();
				String[] fileInfo = line.split(";");
				imageFiles.put(fileInfo[0],  fileInfo[2]); // Add the symbol and the associated file name.
				terrainTypes.put(fileInfo[0],  fileInfo[1]); // Add the symbol and terrain description.
			}

			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("The file was not found.");
			e.printStackTrace();
		}
		decorateMap();
		readItems();
	}
	
	public void decorateMap() {
		final int boundary = 2;
		miniMap = new char[mapSize[0] + boundary][mapSize[1] + boundary];
		
		// Initialize boundary
		for (int i = 0; i < mapSize[0] + boundary; i++) {
			miniMap[i][0] = 'X';
			miniMap[i][mapSize[1] + 1] = 'X';
		}
		for (int i = 0; i < mapSize[1] + boundary; i++) {
			miniMap[0][i] = 'X';
			miniMap[mapSize[0] + 1][i] = 'X';
		}
		
		// Copy map to remaining area.
		int rowIterator = 1;
		int colIterator = 1;
		for (char[] row : map) {
			for (char terrain : row) {
				miniMap[rowIterator][colIterator] = terrain;
				colIterator++;
			}
			colIterator = 1;
			rowIterator++;
		}
	}
	
	public void readItems() {
		File file = new File(this.itemFileName);
		try {
			Scanner scanner = new Scanner(file);
			
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] words = line.split(";");
				int[] pair = {Integer.parseInt(words[0]), Integer.parseInt(words[1])}; // First two words are the coordinates.
				String item = words[2];
				
				addItem(pair, item);
			}
			scanner.close();
		} catch (FileNotFoundException e) { // The items file could not be opened.
			e.printStackTrace();
		}
	}
	
	public void addItem(int[] pair, String item) {
		// Determine if items already exist at location "pair".
		boolean alreadyExists = false;
		for (int[] key : items.keySet()) { // Can't use items.containsKey(pair) since it compares object addresses. We need to compare integers within the int[].
			if (Arrays.equals(key, pair)) { // Compare the integers contained within the key with those contained in the pair.
				alreadyExists = true;
				ArrayList<String> value = items.get(key);
				value.add(item); // Add the new item to the list of items at that location.
				items.put(key, value);
			}
		}
		if (alreadyExists == false) { // We did not place the item in an existing key:value pair. Create a new one.
			ArrayList<String> value = new ArrayList<String>();
			value.add(item);
			items.put(pair, value); // Add the new ArrayList to items.
		}
	}
	
	public void printItems(int rowCoord, int columnCoord) { // Prints items that exist at the current location.
		int[] pair = {rowCoord, columnCoord};
		for (int[] key : items.keySet()) { // Can't use items.containsKey(pair) since it compares object addresses. We need to compare integers within the int[].
			if (Arrays.equals(key, pair) && !items.get(key).isEmpty()) { // Compare the integers contained within the key with those contained in the pair. Also, check that all of the items haven't been taken from that location.
				transcript.insert("You found the following items at " + rowCoord + ", " + columnCoord + ":\n", transcript.getText().length());
				for (String item : items.get(key)) {
					transcript.insert(item + "\n", transcript.getText().length());
				}
			}
		}
	}
	
	public char terrain(int rowCoord, int columnCoord) {
		return map[rowCoord][columnCoord];
	}
	
	public void printMinimap(int rowCoord, int columnCoord, int view) {
		// Need to also decorate the map with an increasingly large border as the view changes.
		for (int n = 0; n < 2*view+1; n++) {
			transcript.insert("" + miniMap[rowCoord+n][columnCoord] + miniMap[rowCoord+n][columnCoord+1] + miniMap[rowCoord+n][columnCoord+2] + "\n", transcript.getText().length());
		}
	}
}
