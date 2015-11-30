/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  Neither the name of the Mario AI nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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



public class MySprite {

	protected static float GROUND_INERTIA = 0.89f;
	protected static float AIR_INERTIA = 0.89f;

	public float xOld, yOld, xa, ya;
	public int x, y;

	/**
	 * Sprites are 16x16 long, mapX = x / 16, mapY = y / 16...
	 * See {@link MySprite#tick()}.
	 */



	protected static float creaturesGravity = 1.0f;
	protected static float windCoeff = 0;
	protected static float iceCoeff = 0;


	public EntityType type = null;
	public float iceScale(final float ice) {
		return ice;
	}

	public float windScale(final float wind, int facing) {
		return facing == 1 ? wind : -wind;
	}

	public void move() {
		float xPrime = 0.0f;
		float yPrime = 0.0f;
		xPrime+=this.x+xa;
		yPrime+=this.y+ya;
		x = (int) xPrime;
		y = (int) yPrime;

	}


	public node nodeMove(){
		node n = new node();
		x += xa;
		y+=ya;
		n.x = x;
		n.y = y;
		return n;
	}


	public final Pair tick() { //THIS!!!!!
		xOld = x;
		yOld = y;
		move();
		return new Pair(x,y);
	}

	public final void tickNoMove() {
		xOld = x;
		yOld = y;
	}




	
	
}