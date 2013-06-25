import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Adventure extends JFrame implements ActionListener, Serializable {
	private static final long serialVersionUID = 1L;
	public static int imgW = 60; // Default size. Modified by Map.java.
	public static int imgH = 60; // Default size. Modified by Map.java.
	
	static Adventure window;
	GameChar character;
	Map map;

	JPanel gamePanel = new JPanel(); // Contains the game view and menu controls.
	JPanel menu = new JPanel(); // Menu controls.
	JPanel game = new JPanel(); // Game view.
	
	JButton open = new JButton("Open");
	JButton save = new JButton("Save");
	JButton quit = new JButton("Quit");
	
	JPanel text = new JPanel(); // Contains the game transcript and command box.
	JTextArea transcript = new JTextArea(); // Game transcript.
	JTextField chatbox = new JTextField(); // Command text box.
	
	public static void main(String[] args) {
		String mapFileName = getArg(args);		
		window = new Adventure(mapFileName);
	}
	
	Adventure(String mapFileName) {
		map = new Map(mapFileName, transcript, game);
		character = new GameChar(map, transcript);
		
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager(); 
		manager.addKeyEventDispatcher(new MyDispatcher()); // Register dispatcher to see all keypresses, even if chatbox isn't focused.
		
		setTitle("Program 6 - Adventure Game Part 3");
		setSize(600, 600);
		setLayout(new BorderLayout());
		
		gamePanel.setLayout(new BorderLayout()); // The game view and menu controls are contained within.
		// Setting up the button menu.
		menu.setLayout(new GridLayout(3, 1, 0, 10)); // GridLayout(int rows, int cols, int hgap, int vgap) 
		open.addActionListener(this);
		save.addActionListener(this);
		quit.addActionListener(this);
		menu.add(open);
		menu.add(save);
		menu.add(quit);
		gamePanel.add(menu, BorderLayout.EAST); // Add the button panel.
		// Setting up the game view.
		game.setLayout(new GridLayout(5, 5, 0, 0));
		
		drawMap(character.rowCoord, character.columnCoord, 5); // Unlike the minimap, the view is hard-coded to display a 5*5 matrix.
																	// Could later print anything outside the view as a black cell.
		
		gamePanel.add(game, BorderLayout.WEST); // Add the game view.
		add(gamePanel, BorderLayout.NORTH);
		
		text.setLayout(new BorderLayout());
		
		// Configure and add the transcript area.
		transcript.setEditable(false);
		transcript.setLineWrap(true);
		transcript.setWrapStyleWord(true);
		text.add(new JScrollPane(transcript), BorderLayout.CENTER);
		
		// chatbox.addKeyListener(this);
		chatbox.addActionListener(this);
		add(new JScrollPane(chatbox), BorderLayout.SOUTH);
		add(text, BorderLayout.CENTER);
		
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == chatbox) {
			String line = chatbox.getText(); // The text entered by the user.
			input(line);
			chatbox.setText(""); // Clear the chat box.
		}
		if (e.getSource() == open) {
			try {
				open();
				transcript.setText(""); // Clear the game transcript.
				transcript.insert("Game opened.\n", transcript.getText().length());
				map.game = game; // Reattach game panel to newly reloaded map.
				map.transcript = transcript; // Reattach transcript to newly reloaded map.
				character.transcript = transcript; // Reattach transcript to newly reloaded character.
				drawMap(character.rowCoord, character.columnCoord, 5); // Redraw the game space.
			} catch (ClassNotFoundException e1) {
				transcript.insert("Error reading file.\n", transcript.getText().length());
			} catch (IOException e1) {
				transcript.insert("That is not a valid file name.\n", transcript.getText().length());
			}
		}
		if (e.getSource() == save) {
			try {
				save();
				transcript.insert("Game saved.\n", transcript.getText().length());
			} catch (IOException e1) {
				transcript.insert("That is not a valid file name.\n", transcript.getText().length());
				e1.printStackTrace();
			}

		}
		if (e.getSource() == quit) {
			System.exit(0);
		}
	}
	
	public void open() throws ClassNotFoundException, IOException {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		@SuppressWarnings("unused")
		int result = chooser.showOpenDialog(this);
		String filename = chooser.getSelectedFile().getPath();
		
		// Read from disk using FileInputStream
		FileInputStream f_in = new FileInputStream(filename);

		// Read object using ObjectInputStream
		ObjectInputStream obj_in = new ObjectInputStream (f_in);

		// Read in the three objects.
		ArrayList<Object> objects = new ArrayList<Object>();
		objects.add(obj_in.readObject());
		objects.add(obj_in.readObject());
		// objects.add(obj_in.readObject());

		for (Object obj : objects) {
			if (obj instanceof GameChar) {
				character = (GameChar) obj;
			}
			else if (obj instanceof Map) {
				map = (Map) obj;
			}
		}
		obj_in.close();
	}
	
	public void save() throws IOException {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		@SuppressWarnings("unused")
		int result = chooser.showSaveDialog(this);
		String filename = chooser.getSelectedFile().getPath();
		
		// Write to disk with FileOutputStream
		FileOutputStream f_out = new FileOutputStream(filename);

		// Write object with ObjectOutputStream
		ObjectOutputStream obj_out = new ObjectOutputStream(f_out);

		// Write object out to disk
		// obj_out.writeObject(window);
		obj_out.writeObject(map);
		obj_out.writeObject(character);
		
		obj_out.close();
	}
	
	private class MyDispatcher implements KeyEventDispatcher, Serializable {
		private static final long serialVersionUID = 4L;
		// Sees all keypresses, regardless of focus (as required by KeyListener).
		public boolean dispatchKeyEvent(KeyEvent e) {
			if (e.getID() == KeyEvent.KEY_PRESSED) {
			    int keyCode = e.getKeyCode();
			    String[] dir = {""};
			    switch(keyCode) { 
			        case KeyEvent.VK_UP:
			            dir[0] = "n";
			            break;
			        case KeyEvent.VK_DOWN:
			        	dir[0] = "s"; 
			            break;
			        case KeyEvent.VK_LEFT:
			        	dir[0] = "w";
			            break;
			        case KeyEvent.VK_RIGHT :
			        	dir[0] = "e";
			            break;
			     }
			    if (!dir[0].equals("")) { // The input was an arrow key.
			    	character.go(dir);
			    	drawMap(character.rowCoord, character.columnCoord, 5); // Five columns, hard-coded.
			    }
			}
			return false;
		}
	}

	public void input(String text) {
		String[] words = new String[0];
		words = text.split(" +");
		
		if (text.length() > 0 && words.length > 0) { // Checks for no input, or only spaces entered.
			String command = words[0];

			String[] parameters = Arrays.copyOfRange(words, 1, words.length);
			char firstchar = 'Z';
			if (command.length() > 0) {
				firstchar = Character.toLowerCase(command.charAt(0));
			}
			if (firstchar == 'g') { // Interpret as the 'go' command.
				character.go(parameters);
				drawMap(character.rowCoord, character.columnCoord, 5); // Five columns, hard-coded.
			}	
			else if (firstchar == 'i') { // Inventory command.
				character.inventory();
			}
			else if (firstchar == 't') { // Take command.
				character.take(paramsToString(parameters));
			}
			else if (firstchar == 'd') { // Drop command.
				character.drop(paramsToString(parameters));
			}
			else if (firstchar == 'q') {
				transcript.insert("Farewell.", transcript.getText().length());
				character.printLocation();
				System.exit(0);
			}
			else {
				transcript.insert("That is not a valid command.\n", transcript.getText().length());
			}
		}
		else {
			transcript.insert("You must enter a command.\n", transcript.getText().length());
		}
	}
	
	public String paramsToString(String[] parameters) {
		String item = "";
		for (String word : parameters) {
			item += word + " ";
		}
		return new String(Arrays.copyOfRange(item.toCharArray(), 0, item.toCharArray().length - 1)); // Remove the extra space from the end of item.
	}
	
	public static String getArg(String[] args){
		if (args.length == 1)
		{
			return args[0];
		}
		else
		{
			System.out.print("Launch the program with a single file name. ");
			System.exit(0);
			return null;
		}
	}
	
	public void drawMap(int rowCoord, int columnCoord, int view) { // Draw to the gridBag
		game.removeAll(); // Clear the display.
		for (int row = -2; row < view-2; row++) {
			for (int col = -2; col < view-2; col++) { 
				if (rowCoord + row < 0 || rowCoord + row >= map.mapSize[0] || columnCoord + col < 0 || columnCoord + col >= map.mapSize[1]) { // Out-of-bounds check:
					// game.add(new JLabel(new ImageIcon("MapPics/out.png")));
					ImageComponent component = this.new ImageComponent("MapPics/out.png");
					game.add(component);
				}
				else if (row == 0 && col == 0) { // Center, override terrain with character.
					//ImageComponent component = this.new ImageComponent("MapPics/person.png");
					//game.add(component);
					
					game.add(new JLabel(new ImageIcon("MapPics/person.png")));
				}
				else { // Valid terrain cell
					String terrain = Character.toString(map.map[rowCoord+row][columnCoord+col]);
					
					if (map.imageFiles.containsKey(terrain)) {
						// game.add(new JLabel(new ImageIcon(map.imageFiles.get(terrain))));
						ImageComponent component = this.new ImageComponent(map.imageFiles.get(terrain));
						game.add(component);
					}	
				}			
			}
		}
		repaint();
		game.revalidate();
		map.printItems(rowCoord, columnCoord);
	}
	
	class ImageComponent extends JPanel { // Inner class for drawing to the GridBag.
		private static final long serialVersionUID = 1L;
		transient Image image;
		
		public ImageComponent(String filename) {
			image = null;
			try {
				image = ImageIO.read(new File(filename));
			}
			catch (IOException e)
			{
				System.out.println("Not a valid image file");
			}
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);		
			if (image == null) return;
			g.drawImage(image, 0,  0, imgW, imgH, this);
		}
	}
}

