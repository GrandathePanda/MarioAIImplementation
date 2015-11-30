/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Mario AI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package EnemyActorPhysics;



import ch.idsia.benchmark.mario.engine.generalization.EntityType;
import togepi.Pair;




public class MyEnemy extends MySprite
{


public float runTime;
public boolean onGround = false;
//    private boolean mayJump = false;
//    private int jumpTime = 0;
//    private float xJumpSpeed;
//    private float yJumpSpeed;

int width = 4;
int height = 24;

public float yaa = 1;


public int facing;
public int deadTime = 0;
public boolean flyDeath = false;

public boolean avoidCliffs = true;

public boolean winged = true;
public int wingTime = 0;

public float yaw = 1;

public boolean noFireballDeath;

public MyEnemy(int x, int y, int dir, boolean winged,EntityType type)
{


    this.winged = winged;

    this.x = x;
    this.y = y;

    yaa = creaturesGravity * 2;
    yaw = creaturesGravity == 1 ? 1 : 0.3f * creaturesGravity;

    avoidCliffs = type == EntityType.RED_KOOPA_WINGED || type == EntityType.GREEN_KOOPA_WINGED;

    noFireballDeath = type == EntityType.SPIKY || type == EntityType.SPIKY_WINGED;
    facing = dir;
    if (facing == 0) facing = 1;
}



public void move()
{

    wingTime++;

        if (flyDeath)
        {
            x += xa;
            y += ya;
            ya *= 0.95;
            ya += 1;
        }

    float sideWaysSpeed = 1.75f;
    //        float sideWaysSpeed = onGround ? 2.5f : 1.2f;

    if (xa > 2)
        facing = 1;
    else if (xa < -2)
        facing = -1;

    xa = facing * sideWaysSpeed;
//    xa += facing == 1 ? -wind : wind;
//        mayJump = (onGround);



    runTime += (Math.abs(xa)) + 5;



    if (Math.random() > 0.7) facing = -facing;
    if(ya == 0)
        onGround = false;

    ya *= winged ? 0.95f : 0.85f;
    if (onGround)
    {
        xa *= (GROUND_INERTIA + windScale(windCoeff, facing) + iceScale(iceCoeff));
    } else
    {
        xa *= (AIR_INERTIA + windScale(windCoeff, facing) + iceScale(iceCoeff));
    }

    if (!onGround)
    {
        if (winged)
        {
            ya += 0.6f * yaw;
        } else
        {
            ya += yaa;
        }
    } else if (winged)
    {
        ya = -10;
    }

}



}