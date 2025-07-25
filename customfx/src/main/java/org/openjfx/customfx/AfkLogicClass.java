package org.openjfx.customfx;


import java.lang.reflect.Method;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;

public class AfkLogicClass {
	private Button afkStartButton;
	private boolean canGoAfk = true;
	private boolean afk = false;
	
	
	//Basic:
	private TitledPane optionSection;
	
	//At least one of these need to be enabled in order for the anti-afk to function.
	private CheckBox useKeyboard;
	private CheckBox useMouse;
	
	private boolean keyboardActive;
	private boolean mouseActive;
	
	
	private RadioButton timedAFKKnob;
	private RadioButton infiniteAFKKnob;
	
	
	private TextField afkTimeTextBox;
	private int afkTimer = -1;
	private ChoiceBox<String> timespan;
	
	private boolean timer = false;
	
	
	//Advanced:
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
		this.infiniteAFKKnob = infiniteAFKKnob;
		this.afkTimeTextBox = afkTimeTextBox;
		afkTimeTextBox.setOnAction(this::updateTime);
		
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
	
	
	
	private void afkTypeLogic(ActionEvent event) {
		keyboardActive = useKeyboard.isSelected();
		mouseActive = useMouse.isSelected();
		
		if(!(keyboardActive || mouseActive)) {
			canGoAfk = false;
			System.out.println("AFK IS NO");
			afkStartButton.setDisable(!canGoAfk);
			return;
		}
		
		canGoAfk = true;
		System.out.println("AFK IS YES");
		afkStartButton.setDisable(!canGoAfk);
	}
	
	
	private void timeLogic(ActionEvent event){
		timer = timedAFKKnob.isSelected();
		afkTimeTextBox.setDisable(!timer);
		timespan.setDisable(!timer);
		
	}
	
	
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
	
	
	private void updateTime(ActionEvent event) {
		
	}
	
	
	public void afkButtonLogic(ActionEvent event) {
		//Redundant but better safe than sorry.
		if(!canGoAfk) {
			return;
		}
		
		System.out.println("Clicked");
		
		if(timer) {
			afkEmulator.setRunTime(calculateRuntime(getIntegerValue(afkTimeTextBox.getText()), timespan.getValue()));
		} else {
			afkEmulator.setRunTime(-1);
		}
		
		afkEmulator.setActions(keyboardActive, mouseActive);
		
		timeBetweenActions = getIntegerValue(betweenActionsTextBox.getText());
		timeBetweenActions = calculateRuntime(timeBetweenActions, betweenActionsTimespan.getValue());
		
		timeBeforeStart = getIntegerValue(beforeStartTextBox.getText());
		timeBeforeStart = calculateRuntime(timeBeforeStart, beforeStartTimespan.getValue());
		
		if(!afk) {
			this.startAFK();
			afkEmulator.startAFK(timeBetweenActions, timeBeforeStart);
		} else {
			this.stopAFK();
			afkEmulator.stopAFK();
		}
//		
		
		
		
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
