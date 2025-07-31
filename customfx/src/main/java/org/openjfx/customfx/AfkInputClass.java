package org.openjfx.customfx;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;

/**
 * This class handles the input emulation used in the AFK system.
 * 
 * @author keyboard
 * @version 1.1.2
 */
public class AfkInputClass {
	//Reference our logic class for accessing our base GUI components and the enable/disable method.
	private AfkLogicClass logicClass;
	
	/**
	 * This thread handles the robot that emulates keyboard/mouse movements.
	 */
	private Thread afkThread;
	
	/**
	 * This timer handles the auto-shutoff for the primary afkThread
	 * @see {@linkplain #afkThread}
	 */
	private Timer timer;
	
	//If the afk thread is currently running.
	private boolean running = false;
	
	//The time that it should run for until it stops. By default -1 is infinite.
	private int autoRunTime = -1;
	
	//Booleans to determine if we should use the keyboard and mouse during emulation.
	boolean useKeyboard = true, useMouse = false;
	
	
	public AfkInputClass(AfkLogicClass logicClass) {
		this.logicClass = logicClass;
	}
	
	
	/**
	 * Takes two booleans and determines if we should use both the keyboard and mouse, just the keyboard or mouse, or neither.
	 * Disabling both will mean that you cannot go AFK.
	 * @param useKeyboard if we should use the keyboard during emulation
	 * @param useMouse if we should use the mouse during emulation
	 */
	public void setActions(boolean useKeyboard, boolean useMouse) {
		this.useKeyboard = useKeyboard;
		this.useMouse = useMouse;
	}
	
	
	/**
	 * Optionally set the runtime (in milliseconds) that we should AFK for.
	 * @param totalMillis the total millis afk for.
	 */
	public void setRunTime(int totalMillis) {
		this.autoRunTime = totalMillis;
	}
	
	
	
	/**
	 * Start the AFK thread. This will disable certain GUI elements, and will start emulating keyboard/mouse inputs after a given time.
	 * @param timeBetweenActions the time (in milliseconds) between each input.
	 * @param timeBeforeStart the time (in milliseconds) before the thread initiates the first emulated input.
	 */
	public void startAFK(int timeBetweenActions, int timeBeforeStart) {
		//Return if there is already a thread that exists. Something is bugged if this ever gets triggered.
		if(afkThread != null) {
			return;
		}
		
		running = true;
		
		//If we want to end the afk thread after a given amount of time.
		if(autoRunTime > 0) {
			timer = new Timer();
			
			// Schedule a TimerTask to run after the specified delay
			timer.schedule(new TimerTask() {
	            @Override
	            public void run() {
	            	//Stop running, and interrupt the thread. More than likely we are waiting for the next action.
	                running = false;
	                afkThread.interrupt();
	            }
	            //Run for x millis:
	        }, autoRunTime);
		}
		
		
		//Spawn a new thread to act as our emulation thread.
		afkThread = new Thread(()->{
			try {
				//Create a robot that will be our emulator.
				Robot robot = new Robot();
				boolean movedRight = false;
				
				//Make the thread sleep for a bit before starting.
				try {
					Thread.sleep(timeBeforeStart);
				} catch(InterruptedException e) {
					stopAFK();
					return;
				}
				
				
				//Our actual AFK loop.
				do {
					if(useKeyboard) {
						//Press space, followed by A or D.
						robot.keyPress(KeyEvent.VK_SPACE);
						robot.keyRelease(KeyEvent.VK_SPACE);
						robot.delay(1);
						
						robot.keyPress(movedRight ? KeyEvent.VK_A : KeyEvent.VK_D);
						robot.delay(25);
						robot.keyRelease(movedRight ? KeyEvent.VK_A : KeyEvent.VK_D);
						
						movedRight = !movedRight;	
					}
					
					
					if(useMouse) {
						//Hold the right mouse button, then drag to the left a fair amount.
						robot.mouseMove(500, 500);
						robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
						robot.mouseMove(400, 500);
						robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
					}
					
					
					//Now our thread sleeps until the next movement event needs to occur.
					try {
						Thread.sleep(timeBetweenActions);
					} catch(InterruptedException e) {	
						stopAFK();
						break;
					}
				} while(running);
				
				
				//If running is set to false, we know the thread was terminated softly (probably by our timer).
				running = false;
				stopAFK();
				
			} catch(Exception e) {
				e.printStackTrace();
				stopAFK();
			}
		});
		
		//Now actually spawn the thread.
		afkThread.setDaemon(true);
		afkThread.start();
	}
	
	
	
	/**
	 * Ends the AFK thread.
	 * Will stop the timer, inputs, and reset the GUI.
	 */
	public void stopAFK() {
		//Kill the timer if needed.
		if(timer != null) {
			timer.cancel();
			timer = null;
		}
		
		
		if(afkThread != null && afkThread.isAlive() && !afkThread.isInterrupted() && running) {
			try {
				//Attempt to reset the thread.
				afkThread.interrupt();
				afkThread.join();
			} catch(InterruptedException e) {
				System.out.println("Forced termination? More than likely from the GUI button.");
			}
		}
		
		
		//Reset our thread so we can recreate a new one if needed.
		running = false;
		afkThread = null;
		
		
		//Finally reset the GUI.
		Platform.runLater(()->{
			logicClass.stopAFK();
		});
	}
	

}
