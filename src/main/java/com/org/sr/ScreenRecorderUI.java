package com.org.sr;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

public class ScreenRecorderUI extends JFrame implements WindowListener, ActionListener, NativeMouseInputListener {

	private static final long serialVersionUID = 1L;

	private JPanel jFrame;
	private JLabel nameLabel;
	private JTextField name;
	private JLabel path;
	private JButton folderPath;
	private JButton start;
	private JButton stop;
	private JButton save;

	private Image windowIcon = null, playImage = null, pauseImage = null, stopImage = null, folderImage = null;

	private Boolean isRecording = Boolean.FALSE;
	private Boolean isInit = Boolean.TRUE;
	private Long index = 1L;

	private static int saveConfig = 0;
	private Boolean isMouseListenerActive = Boolean.FALSE;
	private static String folderToBeDeleted = "";
	private HashMap<String, Integer> taskBarBounds;

	private Boolean taskbarIconClicked = Boolean.FALSE;

	public static void main(String[] args) {
		LogManager.getLogManager().reset();

		// Get the logger for "org.jnativehook" and set the level to off.
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.OFF);
		/*
		 * Runtime.getRuntime().addShutdownHook(new Thread(() -> {
		 * System.out.println(ScreenRecorderUI.saveConfig);
		 * if(ScreenRecorderUI.saveConfig == 1) { try { FileUtils.deleteDirectory(new
		 * File(ScreenRecorderUI.folderToBeDeleted)); } catch (IOException e) {
		 * e.printStackTrace(); } } }));
		 */
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ScreenRecorderUI frame = new ScreenRecorderUI();
					frame.setVisible(true);
					frame.taskBarBounds = ScreenCapture.getTaskbarBounds();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ScreenRecorderUI() {
		setAlwaysOnTop(true);
		setAutoRequestFocus(false);
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		try {
			windowIcon = new ImageIcon(classloader.getResource("Record.png")).getImage().getScaledInstance(24, 24,
					java.awt.Image.SCALE_SMOOTH);
			playImage = new ImageIcon(classloader.getResource("Play.png")).getImage().getScaledInstance(24, 24,
					java.awt.Image.SCALE_SMOOTH);
			pauseImage = new ImageIcon(classloader.getResource("Pause.png")).getImage().getScaledInstance(24, 24,
					java.awt.Image.SCALE_SMOOTH);
			stopImage = new ImageIcon(classloader.getResource("Stop.png")).getImage().getScaledInstance(24, 24,
					java.awt.Image.SCALE_SMOOTH);
			folderImage = new ImageIcon(classloader.getResource("Folder.png")).getImage().getScaledInstance(24, 24,
					java.awt.Image.SCALE_SMOOTH);
		} catch (Exception e) {
			System.out.println(classloader.getResource("Play.png"));
			e.printStackTrace();
		}
		setIconImage(windowIcon);
		setTitle("Step Recorder");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 655, 170);
		jFrame = new JPanel();
		jFrame.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.addWindowListener(this);

		setContentPane(jFrame);
		jFrame.setLayout(null);

		nameLabel = new JLabel("Document Name : ");
		nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		nameLabel.setBounds(12, 10, 107, 30);
		jFrame.add(nameLabel);

		name = new JTextField();
		name.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		name.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (!StringUtils.isBlank(path.getText()) && !StringUtils.isBlank(name.getText())) {
					start.setEnabled(true);
					stop.setEnabled(true);
					if (isRecording)
						folderPath.setEnabled(false);
					else
						folderPath.setEnabled(true);
				} else {
					start.setEnabled(false);
					stop.setEnabled(false);
					folderPath.setEnabled(true);
				}
			}
		});
		name.setBounds(127, 10, 266, 30);
		nameLabel.setLabelFor(name);
		jFrame.add(name);
		name.setColumns(10);

		path = new JLabel("");
		path.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		path.setBounds(127, 50, 501, 30);
		jFrame.add(path);

		start = new JButton("Start Record");
		start.setEnabled(false);
		// start.addMouseListener(this);
		start.addActionListener(this);
		start.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		start.setBounds(12, 90, 170, 30);
		start.setIcon(new ImageIcon(playImage));
		jFrame.add(start);

		stop = new JButton("Stop Record");
		stop.addActionListener(this);
		stop.setEnabled(false);
		stop.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		stop.setBounds(194, 90, 170, 30);
		stop.setIcon(new ImageIcon(stopImage));
		jFrame.add(stop);

		save = new JButton("Save");
		save.addActionListener(this);
		save.setEnabled(false);
		save.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		save.setBounds(376, 90, 120, 30);
		jFrame.add(save);

		folderPath = new JButton("Choose Location");
		folderPath.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		folderPath.addActionListener(this);
		folderPath.setBounds(450, 10, 178, 30);
		folderPath.setIcon(new ImageIcon(folderImage));
		jFrame.add(folderPath);

		JButton reset = new JButton("Reset");
		reset.addActionListener(this);
		reset.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		reset.setBounds(508, 90, 120, 30);
		jFrame.add(reset);

		JLabel pathLabel = new JLabel("Folder Selected: ");
		pathLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		pathLabel.setBounds(12, 50, 99, 30);
		jFrame.add(pathLabel);
		
		JLabel docType = new JLabel(".docx");
		docType.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		docType.setBounds(403, 10, 37, 30);
		jFrame.add(docType);
	}

	// ActionListener Overridden Function

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("Start Record".equalsIgnoreCase(e.getActionCommand())
				|| "Pause Recording".equalsIgnoreCase(e.getActionCommand())
				|| "Resume Recording".equalsIgnoreCase(e.getActionCommand())) {
			isRecording = !isRecording;
			if (isInit) {
				try {
					GlobalScreen.registerNativeHook();
					if (!isMouseListenerActive) {
						isMouseListenerActive = Boolean.TRUE;
						GlobalScreen.addNativeMouseListener(this);
					}
				} catch (NativeHookException e1) {
					this.handleException(e1);
				}
				start.setText("Pause Recording");
				start.setIcon(new ImageIcon(pauseImage));
				isInit = Boolean.FALSE;
			} else {
				if (!isRecording) {
					isMouseListenerActive = Boolean.FALSE;
					start.setText("Resume Recording");
					start.setIcon(new ImageIcon(playImage));
					GlobalScreen.removeNativeMouseListener(this);
					// jFrame.getRootPane().removeMouseListener(this);
					// defaultToolkit.removeAWTEventListener(listener);
				} else {
					if (!isMouseListenerActive) {
						isMouseListenerActive = Boolean.TRUE;
						GlobalScreen.addNativeMouseListener(this);
					}
					start.setText("Pause Recording");
					start.setIcon(new ImageIcon(pauseImage));
					// jFrame.getRootPane().addMouseListener(this);
					// defaultToolkit.addAWTEventListener(listener, AWTEvent.MOUSE_EVENT_MASK |
					// AWTEvent.FOCUS_EVENT_MASK);
				}
			}
		} else if ("Stop Record".equalsIgnoreCase(e.getActionCommand())) {
			isRecording = Boolean.FALSE;
			isMouseListenerActive = Boolean.FALSE;
			isInit = Boolean.TRUE;
			save.setEnabled(true);
			start.setText("Start Record");
			start.setIcon(new ImageIcon(playImage));
			try {
				isMouseListenerActive = Boolean.FALSE;
				GlobalScreen.removeNativeMouseListener(this);
				GlobalScreen.unregisterNativeHook();
			} catch (NativeHookException e1) {
				this.handleException(e1);
			}
		} else if ("Save".equalsIgnoreCase(e.getActionCommand())) {
			ScreenRecorderUI.folderToBeDeleted = path.getText().trim() + "\\" + name.getText() + "\\";
			File folder = new File(ScreenRecorderUI.folderToBeDeleted);
			if (!(folder.exists() && folder.isDirectory() && folder.listFiles().length > 0)) {
				JOptionPane.showMessageDialog(this,
						"No files found for saving in folder: " + path.getText() + "\\" + name.getText()
								+ "\nPlease record a project or go to a directory where screenshots are present",
						"No files found", JOptionPane.WARNING_MESSAGE);
				return;
			}
			try {
				String ObjButtons[] = { "Yes", "No" };
				saveConfig = JOptionPane.showOptionDialog(this,
						"Keep the screenshots?",
						"Save Confirmation", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, ObjButtons,
						ObjButtons[0]);
				DocumentSaver.saveToDocumentFile(ScreenRecorderUI.folderToBeDeleted, name.getText());
				if (saveConfig == JOptionPane.YES_OPTION) {
					JOptionPane
							.showMessageDialog(this,
									"Document saved successfully and screenshots saved in folder:\n" + path.getText()
											+ "\\" + name.getText(),
									"Save Successful", JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(this,
								"Document saved successfully" + "\nand all screenshots deleted!",
								"Save Successful, Automatic Deletion Failed", JOptionPane.INFORMATION_MESSAGE);
				}
				Desktop.getDesktop().open(new File(ScreenRecorderUI.folderToBeDeleted));
			} catch (IOException e1) {
				this.handleException(e1);
			} catch (InvalidFormatException e1) {
				this.handleException(e1);
			} finally {
				System.exit(0);
			}

		} else if ("Reset".equalsIgnoreCase(e.getActionCommand())) {
			isRecording = Boolean.FALSE;
			isMouseListenerActive = Boolean.FALSE;
			isInit = Boolean.TRUE;
			start.setText("Start Record");
			start.setIcon(new ImageIcon(playImage));
			name.setText("");
			index = 1L;
			start.setEnabled(false);
			stop.setEnabled(false);
			save.setEnabled(false);
			path.setText("");
			folderPath.setEnabled(true);
			try {
				GlobalScreen.removeNativeMouseListener(this);
				GlobalScreen.unregisterNativeHook();
			} catch (NativeHookException e1) {
				this.handleException(e1);
			}
		} else if ("Choose Location".equalsIgnoreCase(e.getActionCommand())) {
			JFileChooser fileChooser = !!StringUtils.isBlank(path.getText()) ? new JFileChooser()
					: new JFileChooser(path.getText().trim());
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int option = fileChooser.showOpenDialog(jFrame);
			if (option == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				path.setText(file.getPath());
				if (!StringUtils.isBlank(path.getText()) && !StringUtils.isBlank(name.getText())) {
					start.setEnabled(true);
					stop.setEnabled(true);
					save.setEnabled(true);
					if (isRecording)
						folderPath.setEnabled(false);
					else
						folderPath.setEnabled(true);
				} else {
					start.setEnabled(false);
					stop.setEnabled(false);
					save.setEnabled(false);
					folderPath.setEnabled(true);
				}
			} else {
				if (!StringUtils.isBlank(path.getText()) && !StringUtils.isBlank(name.getText())) {
					start.setEnabled(true);
					stop.setEnabled(true);
					if (isRecording)
						folderPath.setEnabled(false);
					else
						folderPath.setEnabled(true);
				} else {
					start.setEnabled(false);
					stop.setEnabled(false);
					folderPath.setEnabled(true);
				}
			}
		}

		if (isRecording) {
			save.setEnabled(Boolean.FALSE);
		}
	}

	// NativeMouseInputListener Overridden Functions

	@Override
	public void nativeMouseClicked(NativeMouseEvent nativeEvent) {
		this.taskbarIconClicked = Boolean.FALSE;
		if (nativeEvent.getButton() == NativeMouseEvent.BUTTON3
				|| nativeEvent.getButton() == NativeMouseEvent.BUTTON2) {
			return;
		} else {
			/*
			 * System.err.println("\n**********BEFORE*************");
			 * System.err.println("newState : " + this.getExtendedState());
			 * System.err.println("oldState : " + this.previousExtendedState);
			 * System.err.println("************************************");
			 * if(this.getExtendedState() != this.previousExtendedState) {
			 * this.previousExtendedState = this.getExtendedState(); return; }
			 * System.err.println("**********AFTER******************");
			 * System.err.println("newState : " + this.getExtendedState());
			 * System.err.println("oldState : " + this.previousExtendedState);
			 * System.err.println("************************************");
			 */
			Double jFrameInitCoordinateX = this.getLocationOnScreen().getX();
			Double jFrameInitCoordinateY = this.getLocationOnScreen().getY();

			Double jFrameLastCoordinateX = this.getBounds().getMaxX();
			Double jFrameLastCoordinateY = this.getBounds().getMaxY();
			if ((nativeEvent.getX() > jFrameInitCoordinateX && nativeEvent.getX() < jFrameLastCoordinateX)
					&& (nativeEvent.getY() > jFrameInitCoordinateY && nativeEvent.getY() < jFrameLastCoordinateY)) {
				/*
				 * System.out.println("************************************");
				 * System.out.println("jFrameInitCoordinate : " + jFrameInitCoordinateX);
				 * System.out.println("jFrameLastCoordinateX : " + jFrameLastCoordinateX);
				 * System.out.println("jFrameInitCoordinateY : " + jFrameInitCoordinateY);
				 * System.out.println("jFrameLastCoordinateY : " + jFrameLastCoordinateY);
				 * System.out.println("************************************");
				 */
				return;
			}
		}
		if (isRecording) {

			// A possible notification that the taskbar of the iconified recorder was
			// clicked, it will work in combination to DeIconified
			if (nativeEvent.getX() >= taskBarBounds.get("MinX") && nativeEvent.getX() <= taskBarBounds.get("MaxX")
					&& nativeEvent.getY() >= taskBarBounds.get("MinY")
					&& nativeEvent.getX() <= taskBarBounds.get("MaxY")) {
				this.taskbarIconClicked = Boolean.TRUE;
			}
			try {
				ScreenCapture.captureImage(path.getText().trim() + "\\" + name.getText(), "\\" + name.getText(),
						index++);
			} catch (Exception e) {
				this.handleException(e);
			}
		}
	}

	@Override
	public void nativeMousePressed(NativeMouseEvent nativeEvent) {
	}

	@Override
	public void nativeMouseReleased(NativeMouseEvent nativeEvent) {
	}

	@Override
	public void nativeMouseMoved(NativeMouseEvent nativeEvent) {
	}

	@Override
	public void nativeMouseDragged(NativeMouseEvent nativeEvent) {
	}

	private void handleException(Exception e) {
		JOptionPane.showMessageDialog(this, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
	}

	// WindowListener Overridden Functions

	@Override
	public void windowActivated(WindowEvent arg0) {
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		if (isRecording) {
			try {
				ScreenCapture.deleteImage(path.getText().trim() + "\\" + name.getText(), "\\" + name.getText(),
						--index);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		if (isRecording && this.taskbarIconClicked) {
			try {
				ScreenCapture.deleteImage(path.getText().trim() + "\\" + name.getText(), "\\" + name.getText(),
						--index);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
	}
}
