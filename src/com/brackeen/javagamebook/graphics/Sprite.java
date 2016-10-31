package com.brackeen.javagamebook.graphics;

import java.awt.Image;

public class Sprite {

    protected Animation anim;
    // position (pixels)
    private float x;
    private float y;
    // velocity (pixels per millisecond)
    private float dx;
    private float dy;

    public static final int STATE_NORMAL = 0;
    public static final int STATE_SHOOTING = 1;
    public static final int STATE_AUTO = 2;
    public int buttonPressed;
    public boolean canShoot;
    private int state;
    private float stateTime;

    public void wasteTime(){
    	for (int i = 0; i < 1000000;i++){
    		for(int j = 0; j < 1000; j++){
    			
    		}
    	}
    }
    
    //states
    public void setState(int state, float elapsedTime, int buttonPressed) {
    	float currentTime = 0;
        if (this.state != state) {
            this.state = state;
            stateTime = 0;
            if (state == STATE_NORMAL) {
            	this.canShoot = false;
            	this.buttonPressed = 0;
            	
            }else if(state == STATE_SHOOTING){
            	this.canShoot = true;
            	if(this.buttonPressed == buttonPressed){
            		state = STATE_AUTO;
            	}
            }else if(state == STATE_AUTO){
            	wasteTime();
            	this.buttonPressed = buttonPressed;
            	this.canShoot = true;
            	
            	
            }
        }
    }
    /**
        Creates a new Sprite object with the specified Animation.
    */
    public Sprite(Animation anim) {
        this.anim = anim;
    }

    /**
        Updates this Sprite's Animation and its position based
        on the velocity.
    */
    public void update(long elapsedTime) {
        x += dx * elapsedTime;
        y += dy * elapsedTime;
        anim.update(elapsedTime);
    }

    /**
        Gets this Sprite's current x position.
    */
    public float getX() {
        return x;
    }

    /**
        Gets this Sprite's current y position.
    */
    public float getY() {
        return y;
    }

    /**
        Sets this Sprite's current x position.
    */
    public void setX(float x) {
        this.x = x;
    }

    /**
        Sets this Sprite's current y position.
    */
    public void setY(float y) {
        this.y = y;
    }

    /**
        Gets this Sprite's width, based on the size of the
        current image.
    */
    public int getWidth() {
        return anim.getImage().getWidth(null);
    }

    /**
        Gets this Sprite's height, based on the size of the
        current image.
    */
    public int getHeight() {
        return anim.getImage().getHeight(null);
    }

    /**
        Gets the horizontal velocity of this Sprite in pixels
        per millisecond.
    */
    public float getVelocityX() {
        return dx;
    }

    /**
        Gets the vertical velocity of this Sprite in pixels
        per millisecond.
    */
    public float getVelocityY() {
        return dy;
    }

    /**
        Sets the horizontal velocity of this Sprite in pixels
        per millisecond.
    */
    public void setVelocityX(float dx) {
        this.dx = dx;
    }

    /**
        Sets the vertical velocity of this Sprite in pixels
        per millisecond.
    */
    public void setVelocityY(float dy) {
        this.dy = dy;
    }

    /**
        Gets this Sprite's current image.
    */
    public Image getImage() {
        return anim.getImage();
    }

    /**
        Clones this Sprite. Does not clone position or velocity
        info.
    */
    public Object clone() {
        return new Sprite(anim);
    }
}
