package de.gnox.rovy.server;

import com.pi4j.io.gpio.RaspiPin;

import de.gnox.rovy.api.RovyTelemetryData;

public class Car {


	private static final int STATUSRECHECK_MILLIS = 1;

	private static final int SPEEDUP_START = 50;
	
	private static final int SPEEDUP_MILLIS = 2000;

	private Wheel rightWheel;

	private Wheel leftWheel;

	public Car() {
		 rightWheel = new Wheel(1, RaspiPin.GPIO_03, RaspiPin.GPIO_06);
		 leftWheel = new Wheel(23, RaspiPin.GPIO_29, RaspiPin.GPIO_28);

//		rightWheel = new HwPwmWheel(RaspiPin.GPIO_01, RaspiPin.GPIO_03, RaspiPin.GPIO_22);
//		leftWheel = new HwPwmWheel(RaspiPin.GPIO_23, RaspiPin.GPIO_29, RaspiPin.GPIO_28);

//		 leftWheel = new Wheel(40, 38);
	}

//	public void drive(float meters, Cam cam) throws RovyException {
//		if (meters < -10 || meters > 10)
//			throw new RovyException("meters not between +-10");
//
//		int millis = (int) (Math.abs(meters) * 4166.0f);
////		cam.clear();
//		cam.captureVideoAsync(millis);
//
//		int speed = 100;
//		if (meters > 0) {
//			rightWheel.start(true, speed);
//			leftWheel.start(true, speed);
//		} else {
//			rightWheel.start(false, speed);
//			leftWheel.start(false, speed);
//		}
//
//		RovyUtility.sleep(millis);
//		// RoverUtility.sleep((int)(1000f));
//		stopNow();
//		cam.waitForVideo();
//	}
	
	public void driveNew(int cm, Cam cam, I2cDisplay display) throws RovyException {
		if (cm < -1000 || cm > 1000)
			throw new RovyException("cm not between +-1000");

		float meters = (float)cm / 100.0f;
		
		float metersAbs = Math.abs(meters);
		
//		int millis = (int) (metersAbs * 4166.0f);
		
//		cam.captureVideoAsync(millis);
		cam.startCapturing();

		int speed = 0;
		
		boolean foreward = meters > 0;
		
		if (foreward)
			display.lookForeward();
		else 
			display.lookBackward();
		
		rightWheel.start(foreward, speed);
		leftWheel.start(foreward, speed);

		
		long startTime = System.currentTimeMillis();
		long now = startTime;
		while (now - startTime < WHEEL_TIMEOUT) {
			speed = speedup(startTime, now, 100);
			
			RovyUtility.sleep(STATUSRECHECK_MILLIS);
			rightWheel.processInput();
			leftWheel.processInput();
			
			synchronizeWheels(speed / 2, speed);
			if (rightWheel.getDistanceInMeter() >= metersAbs)  {
				rightWheel.stop();
//				rightWheel.brake();
			}
			if (leftWheel.getDistanceInMeter() >= metersAbs) {
				leftWheel.stop();
//				leftWheel.brake();
			}
			if (rightWheel.isStopped() && leftWheel.isStopped())
				break;
			now = System.currentTimeMillis();
		}
		stopNow();
//		System.out.println("video millis: " + millis);
//		System.out.println("needed millis: " + (now - startTime));
		
//		RovyUtility.sleep(millis);
//		// RoverUtility.sleep((int)(1000f));
//		stopNow();
		
//		cam.waitForVideo();
		cam.finishCapturing();
		display.lookNormal();
	}
	
//	public void driveStepped(float meters, Cam cam) throws RovyException {
//		if (meters < -10 || meters > 10)
//			throw new RovyException("meters not between +-10");
//
//		float metersAbs = Math.abs(meters);
//		
//		int targetDistance = (int)(Wheel.DISTANCE_OF_METER * metersAbs);
//		
////		int targetDistance = (int)(metersAbs);
//		
////		int millis = (int) (metersAbs * 4166.0f);
//////		cam.clear();
////		cam.captureVideoAsync(millis);
//		
//
//		boolean foreward = meters >= 0;
//		
//		rightWheel.stepNew(true);
//		leftWheel.stepNew(true);
//		
//		// distance to
////		rightWheel.stop();
////		leftWheel.stop();
////		rightWheel.setDDistance(0);
////		leftWheel.setDDistance(0);
//		
////		for (int i = 0; i < 100; i++) {
////			if (rightWheel.getDDistance() == 1 && leftWheel.getDDistance() == 1) 
////				break;
////		
////			if (leftWheel.getDDistance() > 1)
////				leftWheel.stepNew(false);
////			else if (leftWheel.getDDistance() < 1)
////				leftWheel.stepNew(true);
////			
////			if (rightWheel.getDDistance() > 1)
////				rightWheel.stepNew(false);
////			else if (rightWheel.getDDistance() < 1)
////				rightWheel.stepNew(true);
////		
////			RovyUtility.sleep(100);
////		}
//
////		long startTime = System.currentTimeMillis();
////		long now = startTime;
////		while (now - startTime < WHEEL_TIMEOUT) {
////			
////			if (rightWheel.getDistance() <= leftWheel.getDistance())
////				rightWheel.stepNew(foreward);
////
////			if (leftWheel.getDistance() <= rightWheel.getDistance())
////				leftWheel.stepNew(foreward);
////
////			if (rightWheel.getDistance() >= targetDistance && leftWheel.getDistance() >= targetDistance)
////				break;
////
////			
////			RovyUtility.sleep(100);
////			now = System.currentTimeMillis();
////		}
//		
//		
////		RovyUtility.sleep(millis);
////		// RoverUtility.sleep((int)(1000f));
////		stopNow();
////		cam.waitForVideo();
//	}

	private void synchronizeWheels(int minSpeed, int maxSpeed) {
		if (rightWheel.isStopped() && leftWheel.isStopped())
			return;		
		if (rightWheel.isStopped()) {
			leftWheel.setSpeed(maxSpeed);
			return;
		}
		if (leftWheel.isStopped()) {
			rightWheel.setSpeed(maxSpeed);
			return;
		}
		
		long leftDistance = leftWheel.getDistance();
//		int leftSpeed = leftWheel.getSpeed();
		long rightDistance = rightWheel.getDistance();
//		int rightSpeed = rightWheel.getSpeed();
		

		int leftSpeedNew = maxSpeed;
		int rightSpeedNew = maxSpeed;
		
		if (leftDistance > rightDistance) {
			
			rightSpeedNew = maxSpeed;
			leftSpeedNew = minSpeed;
			
		} else if (leftDistance < rightDistance) {
			
			rightSpeedNew = minSpeed;
			leftSpeedNew = maxSpeed;
			
		}
			
//		System.out.println("speed sync: L=" + leftSpeedNew + " R=" + rightSpeedNew);
		
		leftWheel.setSpeed(leftSpeedNew);
		rightWheel.setSpeed(rightSpeedNew);
		
	}
	
//	private void synchronizeWheels() {
//		if (rightWheel.isStopped() || leftWheel.isStopped())
//			return;
//		
//		long leftDistance = leftWheel.getDistance();
//		int leftSpeed = leftWheel.getSpeed();
//		long rightDistance = rightWheel.getDistance();
//		int rightSpeed = rightWheel.getSpeed();
//		
//
//		int leftSpeedNew = leftSpeed;
//		int rightSpeedNew = rightSpeed;
//		
//		if (leftDistance > rightDistance) {
//			
//			long distanceDiff = leftDistance - rightDistance;
//			int speedKorr = (int)(40 * distanceDiff / 2);
//			
//			rightSpeedNew += speedKorr;
//			if (rightSpeedNew > 100) {
//				leftSpeedNew -= (rightSpeedNew - 100);
//				rightSpeedNew = 100;
//			}
//			
//		} else if (leftDistance < rightDistance) {
//			
//			long distanceDiff = rightDistance - leftDistance;
//			int speedKorr = (int)(40 * distanceDiff / 2);
//			
//			leftSpeedNew += speedKorr;
//			if (leftSpeedNew > 100) {
//				rightSpeedNew -= (leftSpeedNew - 100);
//				leftSpeedNew = 100;
//			}
//			
//		}
//		
//		
//		if (leftSpeedNew < 70)
//			leftSpeedNew = 70;
//		if (rightSpeedNew < 70)
//			rightSpeedNew = 70;
//		
//		System.out.println("speed sync: L=" + leftSpeedNew + " R=" + rightSpeedNew);
//		
//		leftWheel.setSpeed(leftSpeedNew);
//		rightWheel.setSpeed(rightSpeedNew);
//		
//	}

//	public void turn(float degrees, Cam cam) throws RovyException {
//		if (degrees < -360 || degrees > 360)
//			throw new RovyException("degrees not between +-360");
//
//		int millis = (int) (Math.abs(degrees) * 5.5f);
////		cam.clear();
//		cam.captureVideoAsync(millis);
//
//		int speed = 50;
//		if (degrees > 0) {
//			rightWheel.start(false, speed);
//			leftWheel.start(true, speed);
//		} else {
//			rightWheel.start(true, speed);
//			leftWheel.start(false, speed);
//		}
//
//		RovyUtility.sleep(millis);
//		stopNow();
//		cam.waitForVideo();
//	}

	public void turnNew(float degrees, Cam cam, I2cDisplay display) throws RovyException {
		if (degrees < -360 || degrees > 360)
			throw new RovyException("degrees not between +-360");

//		int millis = (int) (Math.abs(degrees) * 6.5f);
//		cam.clear();
		cam.startCapturing();

		int speed = 0;
		boolean right = degrees > 0;
		
		if (right)
			display.lookRight();
		else
			display.lookLeft();
		
		rightWheel.start(!right, speed);
		leftWheel.start(right, speed);

		int targetDistance = (int) (Math.abs(degrees) * DISTANCE_OF_DEGREE);
//		targetDistance = Math.max(0, targetDistance - 3); // Wegen des Schwungs nochmal 3 abziehen 
		long startTime = System.currentTimeMillis();
		long now = startTime;
		while (now - startTime < WHEEL_TIMEOUT) {
			speed = speedup(startTime, now, 70);
			
			RovyUtility.sleep(STATUSRECHECK_MILLIS);
			rightWheel.processInput();
			leftWheel.processInput();
			
			synchronizeWheels(speed / 2, speed);
			if (rightWheel.getDistance() >= targetDistance) 
				rightWheel.stop();
			if (leftWheel.getDistance() >= targetDistance) 
				leftWheel.stop();
			if (rightWheel.isStopped() && leftWheel.isStopped())
				break;
			now = System.currentTimeMillis();
		}
		stopNow();
//		System.out.println("video millis: " + millis);
//		System.out.println("needed millis: " + (now - startTime));
		
//		cam.waitForVideo();
		cam.finishCapturing();
		display.lookNormal();
	}
	
	public void turnFwdBkw(float degrees, boolean fwd, I2cDisplay display) throws RovyException {
		if (degrees < -360 || degrees > 360)
			throw new RovyException("degrees not between +-360");

//		int millis = (int) (Math.abs(degrees) * 6.5f);
//		cam.clear();
//		cam.startCapturing();

		int speed = 0;

		Wheel wheel = rightWheel;
		if (fwd) {
			if (degrees > 0) {
				wheel = leftWheel;
				display.lookRight();
			} else {
				display.lookLeft();
			}
		} else {
			if (degrees < 0) {
				wheel = leftWheel;
				display.lookLeft();
			} else {
				display.lookRight();
			}
		}
		
		wheel.start(fwd, speed);


		int targetDistance = (int) (Math.abs(degrees) * DISTANCE_OF_DEGREE_ONEWHEEL);
		long startTime = System.currentTimeMillis();
		long now = startTime;
		while (now - startTime < WHEEL_TIMEOUT) {
			speed = speedup(startTime, now, 100);
			
			RovyUtility.sleep(STATUSRECHECK_MILLIS);
			wheel.processInput();
			
			wheel.setSpeed(speed);
			
			if (wheel.getDistance() >= targetDistance) {
				wheel.stop();
				break;
			}
			now = System.currentTimeMillis();
		}
		stopNow();
//		System.out.println("video millis: " + millis);
//		System.out.println("needed millis: " + (now - startTime));
		
//		cam.waitForVideo();
//		cam.finishCapturing();
		display.lookNormal();
	}
	
	public void slide(float degrees, I2cDisplay display) throws RovyException {
		if (degrees < -90 || degrees > 90)
			throw new RovyException("degrees not between +-90");
		turnFwdBkw(-degrees, false,  display);
		turnFwdBkw(degrees, false,  display);
		turnFwdBkw(degrees, true,  display);
		turnFwdBkw(-degrees, true,  display);
	}

	public int speedup(long starttime, long now, int maxspeed) {
		return speedup(now - starttime, maxspeed);
	}
	
	public int speedup(long driveTime, int maxspeed) {
		int speedoffset = maxspeed - SPEEDUP_START;
		int speed;
		speed = SPEEDUP_START + (int)((float)speedoffset * (float)driveTime / (float)SPEEDUP_MILLIS);
		if (speed > maxspeed)
			speed = maxspeed;
		return speed;
	}

	public void dance(Cam cam) throws RovyException {

		int millis1 = 200;
		int millis2 = 400;
		int millis3 = 200;
		int millis = millis1 + millis2 + millis3;

//		cam.clear();
		cam.startCapturing();

		int speed = 100;
		rightWheel.start(false, speed);
		leftWheel.start(true, speed);

		RovyUtility.sleep(millis1);
		stopNow();

		rightWheel.start(true, speed);
		leftWheel.start(false, speed);

		RovyUtility.sleep(millis2);
		stopNow();

		rightWheel.start(false, speed);
		leftWheel.start(true, speed);

		RovyUtility.sleep(millis3);
		stopNow();

		cam.finishCapturing();
	}

	public void stopNow() {
		rightWheel.stop();
		leftWheel.stop();
	}
	
	public void fillTelemetryData(String prefix, RovyTelemetryData telemetryData) {
		rightWheel.fillTelemetryData(prefix + "rightWheel: ", telemetryData);
		leftWheel.fillTelemetryData(prefix + "leftWheel: ", telemetryData);
	}
	

	private float DISTANCE_OF_DEGREE = 72.0f / 360.0f;
	
	private float DISTANCE_OF_DEGREE_ONEWHEEL = 146.0f / 360.0f;
	
	private int WHEEL_TIMEOUT = 20000;

}
