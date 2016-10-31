package com.brackeen.javagamebook.tilegame.sprites;

import java.lang.reflect.Constructor;
import com.brackeen.javagamebook.graphics.*;

public class Bullet extends Sprite {
	
	public static final int STATE_FLYING = 1;
	public static final int STATE_HIT = 2;
	public static final int STATE_MISS = 3;
	private int state;
	private int stateTime;
	private static final float BULLET_SPEED = .025f;
	private boolean playerBullet = true;
	
	public Bullet(Animation anim){
		super(anim);
		state = STATE_FLYING;
	}
	
	/*
	public Object clone(){
		Constructor constructor = getClass().getConstructors()[0];
		try {
            return constructor.newInstance(new Object[] {
                (Animation)bullet.clone()
            });
        }
        catch (Exception ex) {
            // should never happen
            ex.printStackTrace();
            return null;
        }
		
	}*/
	
	public boolean isPlayerBullet(){
		return this.playerBullet;
	}
	public void setY(float y) {
        super.setY(y);
    }
	
	public void setX(float x){
		super.setX(x);
	}
	
	public int getState() {
        return state;
    }

	public boolean isFlying(){
		return (state == STATE_FLYING);
	}
	
	public float getBulletSpeed(){
		return BULLET_SPEED;
	}
    /**
        Sets the state of this Creature to STATE_NORMAL,
        STATE_DYING, or STATE_DEAD.
    */
    public void setState(int state) {
        if (this.state != state) {
            this.state = state;
            stateTime = 0;
        }
    }
    public void update(long elapsedTime) {
        // select the correct Animation
        Animation newAnim = anim;

        // update the Animation
        if (anim != newAnim) {
            anim = newAnim;
            anim.start();
        }
        else {
            anim.update(elapsedTime);
        }

        // update to "dead" state
        stateTime += elapsedTime;
        /*if (state == STATE_DYING && stateTime >= DIE_TIME) {
            setState(STATE_DEAD);
        }*/
    }
}
