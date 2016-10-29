package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
    The Player.
*/
public class Player extends Creature {

    private static final float JUMP_SPEED = -.95f;
    private boolean playerFacingRight;
    private boolean onGround;
    public int shootFlag;

    public static final int STATE_NORMAL = 0;
    public static final int STATE_SHOOTING = 1;
    public static final int STATE_AUTO = 2;
    private int state;
    private long stateTime;
    public int canShoot;
    private int buttonPressed;
    
    /*public boolean canShoot(float elapsedTime){
    	boolean canShoot;
    	
    	return canShoot;
    }*/

    public Player(Animation left, Animation right,Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
        state = STATE_NORMAL;
    }


    public void collideHorizontal() {
        setVelocityX(0);
    }
    
    public void setState(int state, float elapsedTime, int buttonPressed) {
    	float currentTime = 0;
    	
        if (this.state != state) {
            this.state = state;
            stateTime = 0;
            if (state == STATE_NORMAL) {
            	this.canShoot = 0;
            	this.buttonPressed = 0;
            }else if(state == STATE_SHOOTING){
            	if(this.buttonPressed == buttonPressed){
            		state = STATE_AUTO;
            	}
            }else if(state == STATE_AUTO){
            	this.buttonPressed = buttonPressed;
            	currentTime += elapsedTime;
            	this.canShoot = 1;
            	if(currentTime > .5){
            		state = STATE_AUTO;
            		this.canShoot = 0;
            		currentTime = 0;
            	}
            }
        }
    }

    public void collideVertical() {
        // check if collided with ground
        if (getVelocityY() > 0) {
            onGround = true;
        }
        setVelocityY(0);
    }


    public void setY(float y) {
        // check if falling
        if (Math.round(y) > Math.round(getY())) {
            onGround = false;
        }
        super.setY(y);
    }


    public void wakeUp() {
        // do nothing
    }


    /**
        Makes the player jump if the player is on the ground or
        if forceJump is true.
    */
    public void jump(boolean forceJump) {
        if (onGround || forceJump) {
            onGround = false;
            setVelocityY(JUMP_SPEED);
        }
    }

    public void setFacingRight(boolean i){
    	this.playerFacingRight = i;
    }
    
    public boolean getPlayerFacingRight(){
    	if(playerFacingRight) return true;
    	else return false;
    }

    public float getMaxSpeed() {
        return 0.5f;
    }

}
