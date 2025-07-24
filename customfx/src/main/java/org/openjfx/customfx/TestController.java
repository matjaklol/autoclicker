package org.openjfx.customfx;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

public class TestController implements Initializable {
	
	//The default autoclicker start/stop keybind. (F6).
	private int autoClickerKeybind = NativeKeyEvent.VC_F6;
	
	//The default emergency cancel keybind. (ESCAPE).
	public static int autoClickerFailsafe = NativeKeyEvent.VC_ESCAPE;
	
	
	private KeybindHandler handlerRef;
	public void setKeybindHandler(KeybindHandler handler) {
		handlerRef = handler;
	}
	
	public void cleanRefs() {
		this.handlerRef = null;
	}
	
	
	
	
	
	
	
	
	/**
	 * Returns an integer value for a given string.
	 * @param value the string to check for.
	 * @return 0 (if invalid), or positive value.
	 */
	private int validString(String value) {
		if (value.matches("[-+]?\\d+")) {
	        return Math.abs(Integer.parseInt(value));
	    }
	    return 0;
	}
	
	
	
	
	
	/* ===AUTOCLICKER OFFSET/TIMER LOGIC===
	 * 
	 * 
	 * 
	 */
	
	//This pane is the one that contains each text field for the base delay. Disabled/enabled on autoclicker start/stop.
	@FXML
	public TitledPane clickIntervalTitlePane;
	
	
	//Input fields (hour delay, minute delay, etc.)
	@FXML 
	private TextField hourTextField;
	@FXML 
	private TextField minuteTextField;
	@FXML 
	private TextField secondTextField;
	@FXML 
	private TextField millisTextField;
	
	public static boolean running = false;
	
	/**
	 * Total delay (in milliseconds) between each click for the autoclicker.
	 */
	public long totalDelay = 1L;
	
	@FXML
	public Button startAutoClickerButton;
	
	@FXML
	public Button stopAutoClickerButton;
	
	/**
	 * This method will START the autoclicker AND disable some GUI elements. (Only called via the GUI buttons).
	 */
	@FXML 
	public void startAutoClicker() {
		remoteStartAutoclicker();
		//Let the keybind handler / autoclicker start autoclicking.
		handlerRef.startAutoClicker();
	}
	
	
	/**
	 * This method will STOP the autoclicker AND enable the GUI buttons that were disabled. (Only called via the GUI buttons).
	 */
	@FXML
	public void stopAutoClicker() {
		remoteStopAutoclicker();
		
		//Force disable the autoclicking if using the GUI buttons for this.
		handlerRef.stopAutoClicker();
	}
	
	
	/**
	 * This method updates the GUI buttons to enable/disable certain sections during the autoclicker running.
	 */
	public void remoteStartAutoclicker() {
		//Disable start button, enable the stop button.
		startAutoClickerButton.setDisable(true);
		stopAutoClickerButton.setDisable(false);
		
		//Disable the hotkey button.
		hotkeyButton.setDisable(true);
		
		//Disable the interval section.
		clickIntervalTitlePane.setDisable(true);
		
		//Disable the left/right/middle click settings pane.
		buttonSettings.setDisable(true);
		
		//Disable the repeat count pane.
		repeatSettingsPane.setDisable(true);
	}
	
	
	/**
	 * This method updates the GUI buttons to enable/disable certain sections during the autoclicker running.
	 */
	public void remoteStopAutoclicker() {
		//Enable the start autoclicking button, disable the stop button.
		startAutoClickerButton.setDisable(false);
		stopAutoClickerButton.setDisable(true);
		
		//Allow a new hotkey to be assigned.
		hotkeyButton.setDisable(false);
		
		//Reenable the offset/interval section.
		clickIntervalTitlePane.setDisable(false);
		
		//Reenable the left/right/middle click settings pane.
		buttonSettings.setDisable(false);
		
		//Reenable the repeat count pane.
		repeatSettingsPane.setDisable(false);
	}
	
	
	
	
	/**
	 * Calculate the delay in milliseconds between each click (from the user's inputs in the text boxes).
	 * @return a value that is at least 1 milliseconds between each click (to prevent errors). 
	 */
	public long calculateTotalDelay() {
		long delay = 0;
		delay += validString(hourTextField.getText()) * 3.6e+6; //1 Hour = 3.6*10^6 milliseconds
		delay += validString(minuteTextField.getText()) * 60_000; //1 Minute = 60_000 milliseconds
		delay += validString(secondTextField.getText()) * 1000; //1 Second = 1000 milliseconds
		delay += validString(millisTextField.getText()); //add our base millisecond amount.
		
		//Check to make sure we have a valid minimum amount of time between clicks.
		if(delay <= 0) {
			delay = 1;
		}
		return delay;
	}
	
	
	
	
	
	
	/* ===RANDOM OFFSET LOGIC===
	 * 
	 * 
	 * 
	 * 
	 */
	@FXML 
	public CheckBox randomOffsetCheckbox;
	
	
	private boolean randomOffset = false;
	private int randomOffsetAmount = 40;
	
	@FXML
	private TextField offsetTextField;
	
	@FXML
	private void enableRandomOffset() {
		randomOffset = !randomOffset;
		offsetTextField.setDisable(!randomOffset);
		updateRandomOffset();
	}
	
	@FXML
	private void updateRandomOffset() {
		int newOffset = validString(offsetTextField.getText());
		randomOffsetAmount = newOffset;
	}
	
	public boolean addingRandomOffset() {
		return randomOffset;
	}
	
	public int getRandomOffsetAmount() {
		return randomOffsetAmount;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/* === MOUSE CLICK TYPE / CLICK COUNT ===
	 * 
	 * 
	 * 
	 */
	
	@FXML 
	private TitledPane buttonSettings;
	
	@FXML 
	private ChoiceBox<String> clickTypeDropdown;
	
	@FXML
	private ChoiceBox<String> mouseButtonDropdown;
	
	private String[] mouseButtonTypes = {"Left", "Right", "Middle"};
	private String[] mouseClickTypes = {"Single", "Double"};
	
	

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		afkLogic = new AfkLogicClass(afkStartButton, useKeyboard, useMouse, timedAFKKnob, infiniteAFKKnob, afkTimeTextBox, timespan, betweenActionsTextBox, betweenActionsTimespan, beforeStartTextBox, beforeStartTimespan);
		afkLogic.setPanes(afkOptionPane, advancedOptionPane);
		
		
		clickTypeDropdown.getItems().addAll(mouseClickTypes);
		clickTypeDropdown.setValue(mouseClickTypes[0]);
		clickTypeDropdown.setOnAction(this::getClickType);
		
		
		mouseButtonDropdown.getItems().addAll(mouseButtonTypes);
		mouseButtonDropdown.setValue(mouseButtonTypes[0]);
		mouseButtonDropdown.setOnAction(this::getMouseType);
		
		startAutoClickerButton.setText("Start ("+NativeKeyEvent.getKeyText(autoClickerKeybind)+")");
		stopAutoClickerButton.setText("Stop ("+NativeKeyEvent.getKeyText(autoClickerKeybind)+")");
	}
	
	public int getMouseType(ActionEvent event) {
		String mouseType = mouseButtonDropdown.getValue();
		
		
		//Button1 -> left click
		//Button3 -> right click
		//Button2 -> middle click
		switch(mouseType) {
			case "Left": return InputEvent.BUTTON1_DOWN_MASK;
			case "Right": return InputEvent.BUTTON3_DOWN_MASK;
			case "Middle": return InputEvent.BUTTON2_DOWN_MASK;
			default: return InputEvent.BUTTON1_DOWN_MASK;
		}
	}
	
	
	public boolean getClickType(ActionEvent event) {
		String clickType = clickTypeDropdown.getValue();
		
		return clickType.equals("Double");
	}
	
	

	
	
	
	
	
	
	
	
	/*	===REPEAT X AMOUNTS===
	 * 
	 * 
	 */
	@FXML
	private TitledPane repeatSettingsPane;
	
	@FXML
	private RadioButton manualRepeatKnob;
	
	@FXML
	private RadioButton automaticRepeatKnob;
	
	@FXML 
	private TextField repeatAmountTextField;
	
	private boolean repeating = false;
	private int times = 1;
	
	@FXML
	private void repeatLogic(ActionEvent event) {
		repeatAmountTextField.setDisable(event.getSource() == automaticRepeatKnob);
		calculateRepeatLogic();
	}
	
	@FXML
	private void calculateRepeatLogic() {
		System.out.println("Ran");
		
		if(automaticRepeatKnob.isSelected()) {
			repeating = false;
			return;
		}
		
		repeating = true;
		times = validString(repeatAmountTextField.getText());
	}
	
	
	/**
	 * Calculates how many times to repeat before automatically stopping the autoclicker.
	 * 
	 * @return -1 if we are repeating until manually stopped. Otherwise some integer value number of times (can  still be manually stopped).
	 */
	public int getRepeatCount() {
		if(!repeating) {
			return -1;
		}
		
		return times;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/* ===UPDATE THE HOTKEY===
	 * 
	 * 
	 * 
	 */
	public boolean hotkeyRunning = false;
	
	
	@FXML
	public Button hotkeyButton;
	
	@FXML
	public void hotkeyButtonPressed() {
		hotkeyButton.setDisable(true);
		clickIntervalTitlePane.setDisable(true);
		hotkeyRunning = true;
		hotkeyButton.setText("Press any key:");
		startAutoClickerButton.setDisable(true);
	}
	
	public void disableHotkeyRunning() {
		hotkeyRunning = false;
		hotkeyButton.setDisable(false);
		clickIntervalTitlePane.setDisable(false);
		hotkeyButton.setText("Adjust keybind");
		startAutoClickerButton.setText("Start ("+NativeKeyEvent.getKeyText(autoClickerKeybind)+")");
		stopAutoClickerButton.setText("Stop ("+NativeKeyEvent.getKeyText(autoClickerKeybind)+")");
		startAutoClickerButton.setDisable(false);
		
	}

	public void setAutoClickerKeybind(int keyCode) {
		autoClickerKeybind = keyCode;
		
	}

	public int getAutoClickerKeybind() {
		// TODO Auto-generated method stub
		return autoClickerKeybind;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/* ===HANDLE AFK===
	 * 
	 * 
	 * 
	 */
	@FXML
	private CheckBox useKeyboard;
	
	@FXML
	private CheckBox useMouse;
	
	@FXML
	private RadioButton timedAFKKnob;
	@FXML
	private RadioButton infiniteAFKKnob;
	
	@FXML
	private TextField afkTimeTextBox;
	@FXML
	private ChoiceBox<String> timespan;
	
	@FXML
	private TextField betweenActionsTextBox;
	@FXML
	private ChoiceBox<String> betweenActionsTimespan;
	@FXML
	private TextField beforeStartTextBox;
	@FXML
	private ChoiceBox<String> beforeStartTimespan;
	
	@FXML
	private Button afkStartButton;
	
	@FXML
	private TitledPane advancedOptionPane;
	
	@FXML private TitledPane afkOptionPane;
	
	private AfkLogicClass afkLogic;
}
