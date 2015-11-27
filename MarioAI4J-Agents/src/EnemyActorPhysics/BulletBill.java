package EnemyActorPhysics;
//package ch.idsia.benchmark.mario.engine.sprites;

import ch.idsia.benchmark.mario.engine.Art;
import ch.idsia.benchmark.mario.engine.LevelScene;
import ch.idsia.benchmark.mario.engine.sprites.Fireball;
import ch.idsia.benchmark.mario.engine.sprites.Shell;
import ch.idsia.benchmark.mario.engine.sprites.Sparkle;


public class BulletBill extends Sprite
{
private int width = 4;
int height = 24;

private LevelScene world;
public int facing;

public boolean avoidCliffs = false;
public int anim;

public boolean dead = false;
private int deadTime = 0;

public BulletBill(LevelScene world, float x, float y, int dir)
{
    kind = KIND_BULLET_BILL;
    sheet = Art.enemies;

    this.x = x;
    this.y = y;
    this.world = world;
    xPicO = 8;
    yPicO = 31;

    height = 12;
    facing = 0;
    wPic = 16;
    yPic = 5;

    xPic = 0;
    ya = -5;
    this.facing = dir;
}

public boolean checkCollision()
{
    if (dead) return true;

    float xMarioD = world.mario.x - x;
    float yMarioD = world.mario.y - y;
    float w = 16;
    if (xMarioD > -16 && xMarioD < 16)
    {
        if (yMarioD > -height && yMarioD < world.mario.height)
        {
            if (world.mario.ya > 0 && yMarioD <= 0 && (!world.mario.onGround || !world.mario.wasOnGround))
            {
//                world.mario.stomp(this);
                dead = true;
                ++LevelScene.killedCreaturesTotal;
                ++LevelScene.killedCreaturesByStomp;

                xa = 0;
                ya = 1;
                deadTime = 100;
                return false;
            } else
            {
//                world.mario.getHurt(this.kind);
                return true;
            }
        }
    }
    return false;
}



public node nodeMove(){
	if(deadTime > 0){
		--deadTime;
	}
	x+= xa;
	y+= ya;
	ya*=0.95;
	ya+=1;
	node n = new node();
	n.x = x;
	n.y = y;
	return n;
}
public void move()
{
    if (deadTime > 0)
    {
        deadTime--;

        if (deadTime == 0)
        {
            deadTime = 1;
            for (int i = 0; i < 8; i++)
            {
                world.addSprite(new Sparkle((int) (x + Math.random() * 16 - 8) + 4, (int) (y - Math.random() * 8) + 4, (float) (Math.random() * 2 - 1), (float) Math.random() * -1, 0, 1, 5));
            }
//            spriteContext.removeSprite(this);
        }

        x += xa;
        y += ya;
        ya *= 0.95;
        ya += 1;

        return;
    }

    float sideWaysSpeed = 4f;

    xa = facing * sideWaysSpeed;
    xFlipPic = facing == -1;
    move(xa, 0);
}

private boolean move(float xa, float ya)
{
    x += xa;
    return true;
}

public boolean fireballCollideCheck(Fireball fireball)
{
    if (deadTime != 0) return false;

    float xD = fireball.x - x;
    float yD = fireball.y - y;

    if (xD > -16 && xD < 16)
    {
        if (yD > -height && yD < fireball.height)
        {
            return true;
        }
    }
    return false;
}

public boolean shellCollideCheck(Shell shell)
{
    if (deadTime != 0) return false;

    float xD = shell.x - x;
    float yD = shell.y - y;

    if (xD > -16 && xD < 16)
    {
        if (yD > -height && yD < shell.height)
        {
            dead = true;

            xa = 0;
            ya = 1;
            deadTime = 100;

            return true;
        }
    }
    return false;
}
}
