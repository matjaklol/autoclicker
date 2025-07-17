package org.openjfx.customfx;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;

/**
 * This class handles the input emulation used in the AFK system.
 * 
 * @author keyboard
 */
public class AfkInputClass {
	private AfkLogicClass logicClass;
	private Thread afkThread;
	private Timer timer;
	
	private boolean running = false;
	private boolean stopAFK = false;
	int autoRunTime = -1;
	boolean useKeyboard = true, useMouse = false;
	
	public AfkInputClass(AfkLogicClass logicClass) {
		this.logicClass = logicClass;
	}
	
	public void setActions(boolean useKeyboard, boolean useMouse) {
		this.useKeyboard = useKeyboard;
		this.useMouse = useMouse;
	}
	
	
	public void setRunTime(int totalMillis) {
		this.autoRunTime = totalMillis;
	}
	
	
	public void startAFK(int timeBetweenActions, int timeBeforeStart) {
		if(afkThread != null) {
			return;
		}
		
		Platform.runLater(()->{
			logicClass.startAFK();
		});
		
		afkThread = new Thread(()->{
			try {
				Robot robot = new Robot();
				boolean movedRight = false;
				try {
					Thread.sleep(timeBeforeStart);
				} catch(InterruptedException e) {
					stopAFK();
					System.out.println("Thread interrupted during startup time.");
					return;
				}
				
				timer = new Timer();
				
				// Schedule a TimerTask to run after the specified delay
				timer.schedule(new TimerTask() {
		            @Override
		            public void run() {
		                running = false;
		                robot.waitForIdle();
		            	stopAFK();
		            }
		        }, autoRunTime);
		        
				do {
					if(useKeyboard) {
						robot.keyPress(KeyEvent.VK_SPACE);
						robot.keyRelease(KeyEvent.VK_SPACE);
						robot.delay(1);
						
						robot.keyPress(movedRight ? KeyEvent.VK_A : KeyEvent.VK_D);
						robot.delay(25);
						robot.keyRelease(movedRight ? KeyEvent.VK_A : KeyEvent.VK_D);
						
						movedRight = !movedRight;	
					}
					
					if(useMouse) {
						robot.mouseMove(500, 500);
						robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
						robot.mouseMove(400, 500);
						robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
					}
					
					try {
						Thread.sleep(timeBetweenActions);
					} catch(InterruptedException e) {
						stopAFK();
						break;
					}
				} while(running);
				
				
				
			} catch(Exception e) {
				e.printStackTrace();
				
			}
		});
	}
	
	
	public void stopAFK() {
		timer.cancel();
		timer = null;
		
		if(afkThread != null && afkThread.isAlive() && !afkThread.isInterrupted() && running) {
			afkThread.interrupt();
			
			try {
				afkThread.join();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		running = false;
		afkThread = null;
		
		Platform.runLater(()->{
			logicClass.stopAFK();
		});
	}
	

}
