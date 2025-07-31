package org.openjfx.customfx;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;

/**
 * Handles the logic behind the Anti-AFK system.
 * 
 * @author keyboard
 * @version 1.1.2
 */
public class AfkLogicClass {
	//The start button and some basic booleans.
	private Button afkStartButton;
	private boolean canGoAfk = true;
	private boolean afk = false;
	
	
	//Basic Section:
	private TitledPane optionSection;
	
	//At least one of these need to be enabled in order for the anti-afk to function.
	private CheckBox useKeyboard;
	private CheckBox useMouse;
	
	private boolean keyboardActive;
	private boolean mouseActive;
	
	
	private RadioButton timedAFKKnob;
	private TextField afkTimeTextBox;
	private ChoiceBox<String> timespan;
	
	private boolean timer = false;
	
	
	//Advanced Section:
	private TitledPane advancedSection;
	private TextField betweenActionsTextBox;
	private ChoiceBox<String> betweenActionsTimespan;
	private int timeBetweenActions = 1;
	
	
	private TextField beforeStartTextBox;
	private ChoiceBox<String> beforeStartTimespan;
	private int timeBeforeStart = 15;
	
	private String[] timeTypes = {"Hours", "Minutes", "Seconds", "Milliseconds"};
	
	
	private AfkInputClass afkEmulator;
	
	
	public AfkLogicClass(Button afkStartButton, CheckBox useKeyboard, CheckBox useMouse, RadioButton timedAFKKnob, RadioButton infiniteAFKKnob, TextField afkTimeTextBox, ChoiceBox<String> timespan, TextField betweenActionsTextBox, ChoiceBox<String> betweenActionsTimespan, TextField beforeStartTextBox, ChoiceBox<String> beforeStartTimespan) {
		this.afkStartButton = afkStartButton;
		afkStartButton.setOnAction(this::afkButtonLogic);
		
		this.useKeyboard = useKeyboard;
		this.useMouse = useMouse;
		
		this.timedAFKKnob = timedAFKKnob;
		this.afkTimeTextBox = afkTimeTextBox;
		
		this.timespan = timespan;
		
		this.betweenActionsTextBox = betweenActionsTextBox;
		
		
		this.betweenActionsTimespan = betweenActionsTimespan;
		this.beforeStartTextBox = beforeStartTextBox;
		this.beforeStartTimespan = beforeStartTimespan;
		
		
		
		timespan.getItems().addAll(timeTypes);
		timespan.setValue(timeTypes[1]);
		
		betweenActionsTimespan.getItems().addAll(timeTypes);
		betweenActionsTimespan.setValue(timeTypes[1]);
		
		beforeStartTimespan.getItems().addAll(timeTypes);
		beforeStartTimespan.setValue(timeTypes[2]);
		
		useKeyboard.setOnAction(this::afkTypeLogic);
		useMouse.setOnAction(this::afkTypeLogic);
		
		timedAFKKnob.setOnAction(this::timeLogic);
		infiniteAFKKnob.setOnAction(this::timeLogic);
		
		afkTimeTextBox.setDisable(!timer);
		timespan.setDisable(!timer);
		
		afkEmulator = new AfkInputClass(this);
	}
	
	/**
	 * Sets the TitledPanes used to disable/enable the gui.
	 * @param optionSection the TitledPane that handles the basic options section
	 * @param advancedSection the TitledPane that handles the advanced options section.
	 */
	public void setPanes(TitledPane optionSection, TitledPane advancedSection) {
		this.optionSection = optionSection;
		this.advancedSection = advancedSection;
	}
	
	/**
	 * Returns an integer value for a given string.
	 * @param value the string to check for.
	 * @return 0 (if invalid), or positive value.
	 */
	private int getIntegerValue(String value) {
		if (value.matches("[-+]?\\d+")) {
	        return Math.abs(Integer.parseInt(value));
	    }
	    return 0;
	}
	
	
	
	/**
	 * This method handles the basic logic for which AFK modes to utilize (keyboard only, mouse only, or keyboard & mouse).
	 * @param event
	 */
	private void afkTypeLogic(ActionEvent event) {
		keyboardActive = useKeyboard.isSelected();
		mouseActive = useMouse.isSelected();
		
		//Don't allow the user to go AFK if they have no options selected.
		if(!(keyboardActive || mouseActive)) {
			canGoAfk = false;
			afkStartButton.setDisable(!canGoAfk);
			return;
		}
		
		canGoAfk = true;
		afkStartButton.setDisable(!canGoAfk);
	}
	
	/**
	 * This method handles the logic for whether or not we are using a timed afk (x hours/minutes).
	 * @param event
	 */
	private void timeLogic(ActionEvent event){
		timer = timedAFKKnob.isSelected();
		afkTimeTextBox.setDisable(!timer);
		timespan.setDisable(!timer);
		
	}
	
	
	/**
	 * This method takes an integer value and a string value for however long we should afk for. 
	 * @param value the time period (0->inf)
	 * @param timeType (hour, minute, second, millisecond)
	 * @return however many milliseconds to run for.
	 */
	private int calculateRuntime(int value, String timeType) {
		if(value <= -1) {
			return -1;
		}
		
		
		switch(timeType) {
			case "Hours": return (int) (value * 3.6e+6);
			case "Minutes": return (value * 60_000);
			case "Seconds": return (value * 1_000);
			default: return value;
		}
	}
	
	
	
	
	/**
	 * This method is called when pressing the 'START AFK' or 'STOP AFK' button.
	 * @param event
	 */
	public void afkButtonLogic(ActionEvent event) {
		//Redundant but better safe than sorry.
		if(!canGoAfk) {
			return;
		}
		
		
		//If we have a timer go ahead and tell the emulator how long to run the afk for.
		if(timer) {
			afkEmulator.setRunTime(calculateRuntime(getIntegerValue(afkTimeTextBox.getText()), timespan.getValue()));
		} else {
			afkEmulator.setRunTime(-1);
		}
		
		
		//Set our options for afking (mouse/keyboard modes).
		afkEmulator.setActions(keyboardActive, mouseActive);
		
		//Lastly our time between each action
		timeBetweenActions = getIntegerValue(betweenActionsTextBox.getText());
		timeBetweenActions = calculateRuntime(timeBetweenActions, betweenActionsTimespan.getValue());
		
		//and our time before we start.
		timeBeforeStart = getIntegerValue(beforeStartTextBox.getText());
		timeBeforeStart = calculateRuntime(timeBeforeStart, beforeStartTimespan.getValue());
		
		//Start/stop the afk emulator and enable/disable the GUI elements.
		if(!afk) {
			this.startAFK();
			afkEmulator.startAFK(timeBetweenActions, timeBeforeStart);
		} else {
			this.stopAFK();
			afkEmulator.stopAFK();
		}
		
		
	}
	
	
	/**
	 * Disables some GUI components during the running of the Anti-AFK system.
	 * 
	 */
	public void startAFK() {
		if(this.afk == true) return;
		afkStartButton.setText("Stop");
		afkStartButton.setCancelButton(true);
		optionSection.setDisable(true);
		advancedSection.setDisable(true);
		this.afk = true;
	}
	
	/**
	 * Re-enables some GUI components that were disabled from the starting of the Anti-AFK system.
	 */
	public void stopAFK() {
		if(this.afk == false) return;
		afkStartButton.setText("Start!");
		afkStartButton.setCancelButton(false);
		optionSection.setDisable(false);
		advancedSection.setDisable(false);
		this.afk = false;
	}
	
	
}
