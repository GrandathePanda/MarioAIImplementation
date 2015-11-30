package EnemyActorPhysics;
//package ch.idsia.benchmark.mario.engine.sprites;

import ch.idsia.benchmark.mario.engine.Art;
import ch.idsia.benchmark.mario.engine.LevelScene;
import ch.idsia.benchmark.mario.engine.sprites.Fireball;
import ch.idsia.benchmark.mario.engine.sprites.Shell;
import ch.idsia.benchmark.mario.engine.sprites.Sparkle;


public class BulletBill extends MySprite
{
private int width = 4;
int height = 24;

private LevelScene world;
public int facing;

public boolean avoidCliffs = false;
public int anim;

public boolean dead = false;
private int deadTime = 0;

public BulletBill(int x, int y, int dir)
{


    this.x = x;
    this.y = y;
    height = 12;
    facing = 0;
    ya = -5;
    this.facing = dir;
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


    float sideWaysSpeed = 4f;

    xa = facing * sideWaysSpeed;
}




}
