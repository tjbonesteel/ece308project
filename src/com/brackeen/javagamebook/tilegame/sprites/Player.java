package com.brackeen.javagamebook.tilegame.sprites;
import java.util.Timer;

import com.brackeen.javagamebook.graphics.Animation;

/**
    The Player.
*/
public class Player extends Creature {

    private static final float JUMP_SPEED = -.95f;
    private boolean playerFacingRight = true;
    private boolean onGround;
    public int shootFlag;
    public int health;
    
    private int state;
    private long stateTime;
    private int buttonPressed;
    public boolean canShoot = true;
    public int xTile;
    
    /*public boolean canShoot(float elapsedTime){
    	boolean canShoot;
    	
    	return canShoot;
    }*/

    
    public void addHealth(int health){
	    	
    	this.health += health;
	    	
    	if (this.health > 40){
    		this.health = 40;
    	}
    }
    
    
    
    public Player(Animation left, Animation right,Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
        this.health = 0;
        this.xTile = 0;
        state = STATE_NORMAL;
    }


    public void collideHorizontal() {
        setVelocityX(0);
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
