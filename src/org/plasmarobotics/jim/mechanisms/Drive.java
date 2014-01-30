/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plasmarobotics.jim.mechanisms;

import org.plasmarobotics.jim.Constants;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import org.plasmarobotics.jim.Logger;
import org.plasmarobotics.jim.controls.ControlPack;
import org.plasmarobotics.jim.sensors.PlasmaEncoder;
import org.plasmarobotics.jim.sensors.PlasmaGyro;
import org.plasmarobotics.jim.sensors.SensorPack;


/**
 *This class controls the drive system 
 * @author Jim
 */

public class Drive implements Mechanism{
    
    private Joystick leftJoystick,
            rightJoystick;
   
    private RobotDrive chassis;
    
    private PlasmaEncoder LeftEncoder,
            RightEncoder;
    
    private PlasmaGyro gyro;
    
    private boolean resetNeeded;
    
    /**
     * Used to create a FroboDrive object to control all driving operations
     * 
     * @param leftJoystick Left joystick for tank drive
     * @param rightJoystick Right joystick for tank drive
     */
    public Drive(ControlPack controls, SensorPack sensors){
        //Binds the joysticks...
        this.rightJoystick = controls.getRightJoystick();
        this.leftJoystick = controls.getLeftJoystick();
        
        //Creates a RobotDrive...
        chassis = new RobotDrive(Constants.FRONT_LEFT_DRIVE_CHANNEL, 
                Constants.BACK_LEFT_DRIVE_CHANNEL, 
                Constants.FRONT_RIGHT_DRIVE_CHANNEL, 
                Constants.BACK_RIGHT_DRIVE_CHANNEL);
        
        //setting up encoders
        this.LeftEncoder = sensors.getLeftEncoder();
        this.RightEncoder = sensors.getRightEncoder();
                
        //gyro
        this.gyro = sensors.getGyro();
        
        // resetNeeded = true;
        // ^ gyro already reset during init
        System.out.println("Drive online");
    }
    
    
    
    /**
     * Meant to be run once before teleop. 
     * -sets all motor direction
     */
    public void setupTeleop(){
        chassis.setInvertedMotor(RobotDrive.MotorType.kFrontLeft,true);
        chassis.setInvertedMotor(RobotDrive.MotorType.kRearLeft,true);
        chassis.setInvertedMotor(RobotDrive.MotorType.kFrontRight,true);
        chassis.setInvertedMotor(RobotDrive.MotorType.kRearRight,true);
        System.out.println("Drive prepared for teleop");
    }
    
    /**
     * Meant to be run once before autonomous
     * -sets all motor direction
     * resets the encoders and gyro
     */
    public void setupAutonomous(){
        chassis.setInvertedMotor(RobotDrive.MotorType.kFrontLeft,false);
        chassis.setInvertedMotor(RobotDrive.MotorType.kRearLeft,false);
        chassis.setInvertedMotor(RobotDrive.MotorType.kFrontRight,false);
        chassis.setInvertedMotor(RobotDrive.MotorType.kRearRight,false);
        reset();
        System.out.println("Drive prepared for autonomous");
    }
  
    /*
     * called periodically during teleop
     */
    public void updateTeleop(){
        chassis.tankDrive(leftJoystick, rightJoystick);
    }

    /**
     * called periodically during autonomous
     */
    public void updateAutonomous() {
    
    }

    
    
    
    
    
    /**
     * The robot will drive in a straight line for a given distance
     * @param speed Speed of motors (-1 to 1 scale)
     * @param distance Distance (in inches) to drive
     * @return true when operation is completed
     */
    
                
    //TODO: add dampening
    public boolean drive(double speed, double distance){
        reset();
        
        if(((LeftEncoder.getDistance() + RightEncoder.getDistance())/2) < distance){
            chassis.drive(speed, (gyro.getAbsoluteAngle() * .03));//stay on track with .03 curve
            return false;
        } else{
            resetNeeded = true;
            chassis.drive(0, 0);//stop robot
            return true;
           
        }
        
            
    }
    
    /**
     * Turns the robot
     * @param degrees angle to rotate (degrees, not radians) positive is counter clockwise.
     * @return true when operation is complete
     */
    
    //TODO: Fix logic
    
    public boolean turn(double degrees){
        reset();
        
        double difference = Math.abs(degrees + gyro.getAbsoluteAngle());
        double motorOutput = (1/(1+(180/difference)));
        
        Logger.log("Turning difference: " + difference, this, 4);
        if(degrees - gyro.getAbsoluteAngle() < -5){
            chassis.drive(.3, 1);
            Logger.log("Turning right", this, 3);
        } else if(degrees - gyro.getAbsoluteAngle() > 5){
            chassis.drive(.3, -1);
            Logger.log("Turning left", this, 3);
        } else{
            chassis.drive(0, 0);
            Logger.log("Turn complete.", this, 4);
            resetNeeded = true;
            return true;
        }
        
        return false;

    }
    
    /**
     * resets the sensors on the robot
     */
    public void reset(){
        if(resetNeeded){
            LeftEncoder.reset();
            RightEncoder.reset();
            gyro.reset();
            resetNeeded = false;
        }
        
    }

    public void disable() {
       
    }
    
    
}