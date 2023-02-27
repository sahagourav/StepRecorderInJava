package com.org.sr;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

public class ScreenRecorderUI extends JFrame implements ActionListener, NativeMouseInputListener {

	private static final long serialVersionUID = 1L;

	private JPanel jFrame;
	private JLabel nameLabel;
	private JTextField name;
	private JLabel path;
	private JButton folderPath;
	private JButton start;
	private JButton stop;
	private JButton save;

	private Image playImage = null, pauseImage = null, stopImage = null;

	private Boolean isRecording = Boolean.FALSE;
	private Boolean isInit = Boolean.TRUE;
	private Long index = 0L;

	private Boolean isMouseListenerActive = Boolean.FALSE;

	private Integer previousExtendedState;
	
	public static void main(String[] args) {
		LogManager.getLogManager().reset();

		// Get the logger for "org.jnativehook" and set the level to off.
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ScreenRecorderUI frame = new ScreenRecorderUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ScreenRecorderUI() {
		setAutoRequestFocus(false);
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		try {
			playImage = new ImageIcon(classloader.getResource("Play.png")).getImage().getScaledInstance(24,
					24, java.awt.Image.SCALE_SMOOTH);
			pauseImage = new ImageIcon(classloader.getResource("Pause.png")).getImage().getScaledInstance(24,
					24, java.awt.Image.SCALE_SMOOTH);
			stopImage = new ImageIcon(classloader.getResource("Stop.png")).getImage().getScaledInstance(24,
					24, java.awt.Image.SCALE_SMOOTH);
		}catch(Exception e) {
			System.out.println(classloader.getResource("Play.png"));
			e.printStackTrace();
		}
		setTitle("Step Recorder");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 660, 170);
		jFrame = new JPanel();
		jFrame.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		this.previousExtendedState = this.getExtendedState();

		setContentPane(jFrame);
		jFrame.setLayout(null);

		nameLabel = new JLabel("Name : ");
		nameLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		nameLabel.setBounds(12, 10, 62, 30);
		jFrame.add(nameLabel);

		name = new JTextField();
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
		name.setBounds(72, 10, 350, 30);
		nameLabel.setLabelFor(name);
		jFrame.add(name);
		name.setColumns(10);

		path = new JLabel("");
		path.setFont(new Font("Tahoma", Font.PLAIN, 12));
		path.setBounds(120, 50, 501, 30);
		jFrame.add(path);

		start = new JButton("Start Record");
		start.setEnabled(false);
		// start.addMouseListener(this);
		start.addActionListener(this);
		start.setFont(new Font("Tahoma", Font.PLAIN, 12));
		start.setBounds(12, 91, 170, 30);
		start.setIcon(new ImageIcon(playImage));
		jFrame.add(start);

		stop = new JButton("Stop Record");
		stop.addActionListener(this);
		stop.setEnabled(false);
		stop.setFont(new Font("Tahoma", Font.PLAIN, 12));
		stop.setBounds(194, 91, 170, 30);
		stop.setIcon(new ImageIcon(stopImage));
		jFrame.add(stop);

		save = new JButton("Save");
		save.addActionListener(this);
		save.setEnabled(false);
		save.setFont(new Font("Tahoma", Font.PLAIN, 12));
		save.setBounds(376, 91, 120, 30);
		jFrame.add(save);

		folderPath = new JButton("Choose Location");
		folderPath.setFont(new Font("Tahoma", Font.PLAIN, 12));
		folderPath.addActionListener(this);
		folderPath.setBounds(450, 10, 178, 30);
		jFrame.add(folderPath);

		JButton reset = new JButton("Reset");
		reset.addActionListener(this);
		reset.setFont(new Font("Tahoma", Font.PLAIN, 12));
		reset.setBounds(508, 91, 120, 30);
		jFrame.add(reset);

		JLabel pathLabel = new JLabel("Folder Selected: ");
		pathLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		pathLabel.setBounds(11, 50, 99, 30);
		jFrame.add(pathLabel);
	}

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
			try {
				String ObjButtons[] = { "Yes", "No" };
				int saveConfig = JOptionPane.showOptionDialog(this, "Do you also want to keep the screenshots?",
						"Save Confirmation", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
						ObjButtons, ObjButtons[0]);
				if(saveConfig == JOptionPane.YES_OPTION) {
					DocumentSaver.saveToDocumentFile(path.getText().trim() + "\\", name.getText());
					JOptionPane.showMessageDialog(this, "Document saved successfully and screenshots saved in folder:\n"+path.getText()+"\\"+name.getText(), "Save Successful", JOptionPane.INFORMATION_MESSAGE);
				}else {
					DocumentSaver.saveToDocumentFile(path.getText().trim() + "\\", name.getText());
					FileUtils.forceDeleteOnExit(new File(path.getText().trim()+"\\"+name.getText()+"\\"));
					JOptionPane.showMessageDialog(this, "Document saved successfully\nand screenshots deleted!", "Save Successful", JOptionPane.INFORMATION_MESSAGE);
				}
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
			index = 0L;
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
					if (isRecording)
						folderPath.setEnabled(false);
					else
						folderPath.setEnabled(true);
				} else {
					start.setEnabled(false);
					stop.setEnabled(false);
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

	@Override
	public void nativeMouseClicked(NativeMouseEvent nativeEvent) {
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
			Double jFrameWidth = this.getBounds().getMaxX();
			Double jFrameHeight = this.getBounds().getMaxY();
			Double jFrameLastCoordinateX = jFrameInitCoordinateX + jFrameWidth;
			Double jFrameLastCoordinateY = jFrameInitCoordinateY + jFrameHeight;
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
}
