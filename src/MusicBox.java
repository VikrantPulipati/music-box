import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.net.*;

import java.io.*;
import javax.sound.sampled.*;
import javax.swing.*;


public class MusicBox extends JFrame implements Runnable, AdjustmentListener, ActionListener {
	
	JToggleButton button[][]=new JToggleButton[37][180];
	JScrollBar tempoBar;
	JMenuBar menuBar;
	JMenu file, instrumentMenu, matrixMenu;
	JMenuItem save, load;
	JMenuItem addCol, removeCol;
	JMenuItem[] instrumentItems;
	JButton stopPlay, clear, randomize;
	JFileChooser fileChooser;
	JLabel[] labels = new JLabel[button.length];
	JScrollPane buttonPane;
	JPanel buttonPanel, labelPanel, scrollBarPanel, tempoPanel, menuButtonPanel;
	JLabel tempoLabel;
	boolean notStopped = true;
	JFrame frame=new JFrame();
	String[] clipNames;
	Clip[] clip;
	int tempo;
	boolean playing = false;
	int row = 0, col = 0;
	Font font = new Font("Times New Roman", Font.PLAIN, 10);
	String[] instrumentNames = {"Bell", "Piano", "Oh-Ah", "Oboe", "Marimba", "Glockenspiel"};
	
	public MusicBox () {
		setSize(1000, 800);
		clipNames=new String[]{"C0", "B1", "ASharp1", "A1", "GSharp1", "G1", "FSharp1", "F1", "E1", "DSharp1", "D1", "CSharp1", "C1",
				"B2", "ASharp2", "A2", "GSharp2", "G2", "FSharp2", "F2", "E2", "DSharp2", "D2", "CSharp2", "C2",
				"B3", "ASharp3", "A3", "GSharp3", "G3", "FSharp3", "F3", "E3", "DSharp3", "D3", "CSharp3", "C3"};
		clip=new Clip[clipNames.length];
		String initInstrument = "\\" + instrumentNames[0] + "\\" + instrumentNames[0];
		try {
			for(int i = 0; i < clipNames.length; i++) {
				URL url = new File(instrumentNames[0] + "\\" + instrumentNames[0] + " - " + clipNames[i] + ".wav").toURI().toURL();;
				AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
				clip[i] = AudioSystem.getClip();
				clip[i].open(audioIn);
			}
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(button.length, button[0].length, 2, 5));
		for(int r = 0; r < button.length; r++) {
			String name = clipNames[r].replaceAll("Sharp", "#");
			for(int c = 0; c < button[0].length; c++) {
				button[r][c] = new JToggleButton();
				button[r][c].setFont(font);
				button[r][c].setText(name);
				button[r][c].setPreferredSize(new Dimension(30, 30));
				button[r][c].setMargin(new Insets(0, 0, 0, 0));
				
				buttonPanel.add(button[r][c]);
			}
		}
		
		clear = new JButton("Clear");
		clear.addActionListener(this);
		
		stopPlay = new JButton("Play");
		stopPlay.addActionListener(this);
		
		randomize = new JButton("Randomize");
		randomize.addActionListener(this);
		
		menuButtonPanel = new JPanel();
		menuButtonPanel.setLayout(new GridLayout(1, 3));
		menuButtonPanel.add(randomize);
		menuButtonPanel.add(stopPlay);
		menuButtonPanel.add(clear);
		
		menuBar = new JMenuBar();
		//menuBar.setLayout(new GridLayout(1, 3));
		file = new JMenu("File");
		file.setPreferredSize(new Dimension(120, 30));
		save = new JMenuItem("Save");
		load = new JMenuItem("Load");
		file.add(save);
		file.add(load);
		save.addActionListener(this);
		load.addActionListener(this);

		menuBar.add(file, BorderLayout.WEST);
		
		matrixMenu = new JMenu("Matrix");
		matrixMenu.setPreferredSize(new Dimension(120, 30));
		addCol = new JMenuItem("Add Column");
		addCol.addActionListener(this);
		removeCol = new JMenuItem("Remove Column");
		removeCol.addActionListener(this);
		matrixMenu.add(addCol);
		matrixMenu.add(removeCol);
		
		menuBar.add(matrixMenu);
		
		instrumentMenu = new JMenu("Instruments");
		instrumentMenu.setPreferredSize(new Dimension(120, 30));
		instrumentItems = new JMenuItem[instrumentNames.length];
		for (int i = 0; i < instrumentNames.length; i++) {
			instrumentItems[i] = new JMenuItem(instrumentNames[i]);
			instrumentItems[i].addActionListener(this);
			instrumentMenu.add(instrumentItems[i]);
		}
		
		menuBar.add(instrumentMenu);
		
		menuBar.add(menuButtonPanel);
		
		tempoBar = new JScrollBar(JScrollBar.HORIZONTAL, 200, 0, 50, 500);
		tempoBar.addAdjustmentListener(this);
		tempo = tempoBar.getValue();
		tempoLabel = new JLabel(String.format("%s%6s", "Tempo: ", tempo + "   "));
		
		tempoPanel = new JPanel(new BorderLayout());
		//tempoPanel.add(tempoLabel, BorderLayout.WEST);
		//tempoPanel.add(tempoBar, BorderLayout.CENTER);
		
		labelPanel = new JPanel(new GridLayout(1, 1));
		labelPanel.add(tempoLabel);
		
		scrollBarPanel = new JPanel(new GridLayout(1, 1));
		scrollBarPanel.add(tempoBar);
		
		tempoPanel.add(labelPanel, BorderLayout.WEST);
		tempoPanel.add(scrollBarPanel, BorderLayout.CENTER);
		
		buttonPane = new JScrollPane(buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.add(buttonPane, BorderLayout.CENTER);
		this.add(tempoPanel, BorderLayout.SOUTH);
		this.add(menuBar, BorderLayout.NORTH);
		
		String currDir = System.getProperty("user.dir");
		fileChooser = new JFileChooser(currDir);
		
		setTitle(" Music Box");
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Thread timing = new Thread(this);
		timing.start();
	}

	public void run() {
		do {
			try {
				if (!playing) {
					new Thread().sleep(0);
				} else {
					for (int r = 0; r < button.length; r++) {
						button[r][col].setBackground(Color.YELLOW);
						if (button[r][col].isSelected()) {
							clip[r].start();
							button[r][col].setForeground(Color.RED);
						}
					}
					
					new Thread().sleep(tempo);
					for (int r = 0; r < button.length; r++) {
						if (button[r][col].isSelected()) {
							clip[r].stop();
							clip[r].setFramePosition(0);	
							button[r][col].setForeground(Color.BLACK);
						}
						button[r][col].setBackground(null);
					}
					col++;
					if (col == button[0].length) {
						col = 0;
					}
					
					if (buttonPane.getHorizontalScrollBar().getValue() < col*30-30*28 || buttonPane.getHorizontalScrollBar().getValue() > 30*col) {
						buttonPane.getHorizontalScrollBar().setValue(col*30);
					}
					
				}
			} catch (InterruptedException e) {
				
			}
		} while (notStopped);

	}

	public static void main (String[] args) {
		MusicBox app=new MusicBox();

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//CLEAR BUTTON
		if (e.getSource() == clear) {
			for (int r = 0; r < button.length; r++) {
				for (int c = 0; c < button[0].length; c++) {
					button[r][c].setSelected(false);
				}
			}
			stopPlay.setText("Play");
			col = 0;
			playing = false;
		}
		
		//STOP AND PLAY BUTTON
		if (e.getSource() == stopPlay) {
			playing = !playing;
			if (!playing) {
				stopPlay.setText("Play");
			} else {
				stopPlay.setText("Stop");
			}
		}
		
		for (int i = 0; i < instrumentNames.length; i++) {
			if (e.getSource() == instrumentItems[i]) {
				String selectedInstrument = "\\" + instrumentNames[i] + "\\" + instrumentNames[i];
				try {
					for(int j = 0; j < clipNames.length; j++) {
						URL url = new File(instrumentNames[i] + "\\" + instrumentNames[i] + " - " + clipNames[j] + ".wav").toURI().toURL();;
						AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
						clip[j] = AudioSystem.getClip();
						clip[j].open(audioIn);
					}
				} catch (UnsupportedAudioFileException ex) {
					ex.printStackTrace();
				} catch (IOException ex) {
					ex.printStackTrace();
				} catch (LineUnavailableException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		if (e.getSource() == save) {
			saveSong();
		}
		
		if (e.getSource() == load) {
			int returnVal = fileChooser.showOpenDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					File loadFile = fileChooser.getSelectedFile();
					BufferedReader input = new BufferedReader(new FileReader(loadFile));
					String temp;
					temp = input.readLine();
					tempo = Integer.parseInt(temp.substring(0, 3));
					tempoBar.setValue(tempo);
					Character[][] song  = new Character[button.length][temp.length()];
					
					int r = 0;
					while ((temp = input.readLine()) != null) {
						for (int c = 0; c < temp.length(); c++) {
							song[r][c] = temp.charAt(c);
						}
						r++;
					}
					setNotes(song);
				} catch (IOException ex) {
					
				}
			}
		}
		
		if (e.getSource() == addCol) {
			buttonPane.remove(buttonPanel);
			
			JToggleButton[][] temp = button.clone();
			
			buttonPanel = new JPanel();
			button = new JToggleButton[37][button[0].length+1];
			buttonPanel.setLayout(new GridLayout(button.length, button[0].length));
			
			for (int r = 0; r < button.length; r++) {
				String name = clipNames[r].replaceAll("Sharp", "#");
				for (int c = 0; c < button[0].length; c++) {
					button[r][c] = new JToggleButton();
					button[r][c].setFont(font);
					button[r][c].setText(name);
					button[r][c].setPreferredSize(new Dimension(30,30));
					button[r][c].setMargin(new Insets(0, 0, 0, 0));
					buttonPanel.add(button[r][c]);
				}
			}
			
			for (int r = 0; r < temp.length; r++) {
				for (int c = 0; c < temp[0].length; c++) {
					if (temp[r][c].isSelected()) {
						button[r][c].setSelected(true);
					} else {
						button[r][c].setSelected(false);
					}
				}
			}
			
			
			this.remove(buttonPane);
			buttonPane = new JScrollPane(buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			this.add(buttonPane, BorderLayout.CENTER);
			this.revalidate();
		}
		
		if (e.getSource() == removeCol) {
			buttonPane.remove(buttonPanel);
			
			JToggleButton[][] temp = button.clone();
			
			buttonPanel = new JPanel();
			button = new JToggleButton[37][button[0].length-1];
			buttonPanel.setLayout(new GridLayout(button.length, button[0].length));
			
			for (int r = 0; r < button.length; r++) {
				String name = clipNames[r].replaceAll("Sharp", "#");
				for (int c = 0; c < button[0].length; c++) {
					button[r][c] = new JToggleButton();
					button[r][c].setFont(font);
					button[r][c].setText(name);
					button[r][c].setPreferredSize(new Dimension(30,30));
					button[r][c].setMargin(new Insets(0, 0, 0, 0));
					buttonPanel.add(button[r][c]);
				}
			}
			
			for (int r = 0; r < button.length; r++) {
				for (int c = 0; c < button[0].length; c++) {
					if (temp[r][c].isSelected()) {
						button[r][c].setSelected(true);
					} else {
						button[r][c].setSelected(false);
					}
				}
			}
			
			this.remove(buttonPane);
			buttonPane = new JScrollPane(buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			this.add(buttonPane, BorderLayout.CENTER);
			
			this.revalidate();
		}
		
		if (e.getSource() == randomize) {
			for (int r = 0; r < button.length; r++) {
				for (int c = 0; c < button[0].length; c++) {
					int n = (int)(Math.random()*6);
					if (n == 0) {
						button[r][c].setSelected(true);
					} else {
						button[r][c].setSelected(false);
					}
				}
			}
		}
	}
	
	public void setNotes (Character[][] notes) {
		buttonPane.remove(buttonPanel);
		
		buttonPanel = new JPanel();
		button = new JToggleButton[37][notes[0].length];
		buttonPanel.setLayout(new GridLayout(button.length, button[0].length));
		
		for (int r = 0; r < button.length; r++) {
			String name = clipNames[r].replaceAll("Sharp", "#");
			for (int c = 0; c < button[0].length; c++) {
				button[r][c] = new JToggleButton();
				button[r][c].setFont(font);
				button[r][c].setText(name);
				button[r][c].setPreferredSize(new Dimension(30,30));
				button[r][c].setMargin(new Insets(0, 0, 0, 0));
				buttonPanel.add(button[r][c]);
			}
		}
		
		this.remove(buttonPane);
		buttonPane = new JScrollPane(buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.add(buttonPane, BorderLayout.CENTER);
		
		for (int r = 0; r < button.length; r++) {
			for (int c = 0; c < button[0].length; c++) {
				try {
					if (notes[r][c] == 'x') {
						button[r][c].setSelected(true);
					} else {
						button[r][c].setSelected(false);
					}
				} catch (NullPointerException e) { }
				catch (ArrayIndexOutOfBoundsException e) { }
			}
		}
		
		this.revalidate();
	}
	
	public void saveSong() {
		FileNameExtensionFilter filter = new FileNameExtensionFilter("*.txt", "txt");
		fileChooser.setFileFilter(filter);
		if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
				String str = file.getAbsolutePath();
				if (str.indexOf(".txt") >= 0) {
					str = str.substring(0, str.length()-4);
				}
				String output = "";
				
				for (int r = 0; r <= button.length; r++) {
					if (r == 0) {
						output += tempo;
						for (int i = 0; i < button[0].length; i++) {
							output += " ";
						}
					} else {
						for (int c = 0; c < button[0].length; c++) {
							if (button[r-1][c].isSelected()) {
								output += "x";
							} else {
								output += "-";
							}
						}
					}
					output += "\n";
				}
				
				BufferedWriter outputStream = new BufferedWriter(new FileWriter(str + ".txt"));
				outputStream.write(output);
				outputStream.close();
			} catch (IOException e) {
				
			}
		}
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		if (e.getSource() == tempoBar) {
			tempo = tempoBar.getValue();
			tempoLabel.setText(String.format("%s%6s", "Tempo: ", tempo + "   "));
		}
	}
}