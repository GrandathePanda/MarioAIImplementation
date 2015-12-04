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

import ch.idsia.benchmark.mario.engine.Art;
import ch.idsia.benchmark.mario.engine.LevelScene;
import ch.idsia.benchmark.mario.engine.SimulatorOptions;
import ch.idsia.benchmark.mario.engine.generalization.EntityType;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.engine.input.MarioKey;
import ch.idsia.benchmark.mario.engine.level.Level;
import ch.idsia.benchmark.mario.engine.sprites.BulletBill;
import ch.idsia.benchmark.mario.engine.sprites.*;
import ch.idsia.benchmark.mario.engine.sprites.Fireball;
import ch.idsia.benchmark.mario.environments.MarioEnvironment;
import ch.idsia.benchmark.mario.options.SimulationOptions;
import ch.idsia.benchmark.mario.options.SystemOptions;
import togepi.Pair;

public class MyMario extends MySprite {

	/**
	 * @author Jakub 'Jimmy' Gemrot, gemrot@gamedev.cuni.cz
	 */
	public static enum MarioMode {
		SMALL(0),
		LARGE(1),
		FIRE_LARGE(2);

		private int code;

		private MarioMode(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}

	}

	public static final int STATUS_RUNNING = 2;
	public static final int STATUS_WIN = 1;
	public static final int STATUS_DEAD = 0;

	private static float marioGravity;

	public static boolean large = false;
	public static boolean fire = false;
	public static int coins = 0;
	public static int hiddenBlocksFound = 0;
	public static int collisionsWithCreatures = 0;
	public static int mushroomsDevoured = 0;
	public static int greenMushroomsDevoured = 0;
	public static int flowersDevoured = 0;

	private static boolean isTrace;

	private static boolean isMarioInvulnerable;

	private int status = STATUS_RUNNING;
	// for racoon when carrying the shell
	private int prevWPic;
	private int prevxPicO;
	private int prevyPicO;
	private int prevHPic;

	private boolean isRacoon;
	private float yaa = 1;

	private static float windCoeff = 0f;
	private static float iceCoeff = 0f;
	private static float jumpPower;
	private boolean inLadderZone;
	private boolean onLadder;
	private boolean onTopOfLadder = false;

	public static void resetStatic() {
		large = SimulationOptions.getMarioStartMode() > 0;
		fire = SimulationOptions.getMarioStartMode() == 2;
		coins = 0;
		hiddenBlocksFound = 0;
		mushroomsDevoured = 0;
		flowersDevoured = 0;
		collisionsWithCreatures = 0;

		isMarioInvulnerable = SimulationOptions.isMarioInvulnerable();
		marioGravity = SimulationOptions.getGravityMario();
		jumpPower = SimulationOptions.getMarioJumpPower();

		isTrace = SystemOptions.isTraceFile();

		iceCoeff = SimulationOptions.getWindMario();
		windCoeff = SimulationOptions.getWindCreatures();
	}

	public MarioMode getMode() {
		return (large && fire ? MarioMode.FIRE_LARGE : large ? MarioMode.LARGE : MarioMode.SMALL);
	}

	// private static float GROUND_INERTIA = 0.89f;
	// private static float AIR_INERTIA = 0.89f;

	public MarioInput myKeys = new MarioInput();

	private float runTime;
	public boolean wasOnGround = false;
	public boolean onGround = false;
	public boolean mayJump = false;
	private boolean ducking = false;
	private boolean sliding = false;
	public int jumpTime = 0;
	public float xJumpSpeed;
	public float yJumpSpeed;

	private boolean ableToShoot = false;
	int width = 4;
	public int height = 24;

	private static LevelScene levelScene;
	public int facing;

	public int xDeathPos, yDeathPos;

	public int deathTime = 0;
	public int winTime = 0;
	private int invulnerableTime = 0;

	public Sprite carried = null;

	// private static Mario instance;

	public MyMario(int xp, int yp,boolean OG, boolean MJ) {
		type = EntityType.MARIO;
		// Mario.instance = this;
		x = xp;
		y = yp;
		onGround = OG;
		mayJump = MJ;

		facing = 1;
		setMode(MyMario.large, MyMario.fire);
		yaa = marioGravity * 3;
		jT = jumpPower / (marioGravity);
	}

	private float jT;
	private boolean lastLarge;
	private boolean lastFire;
	private boolean newLarge;
	private boolean newFire;

	public MyMario clone() {
		MyMario newMario = new MyMario(this.x,this.y,this.onGround,this.mayJump);
		newMario.ableToShoot = this.ableToShoot;
		newMario.deathTime = this.deathTime;
		newMario.xa = this.xa;
		newMario.ya = this.ya;
		newMario.jT = this.jT;
		newMario.jumpTime = this.jumpTime;
		newMario.mayJump = this.mayJump;
		newMario.onGround = this.onGround;
		newMario.ableToShoot = this.ableToShoot;
		newMario.facing = this.facing;
		newMario.myKeys = new MarioInput();
		newMario.lastFire = this.lastFire;
		newMario.lastLarge = this.lastLarge;
		newMario.xOld = this.xOld;
		newMario.yOld = this.yOld;
		newMario.runTime = this.runTime;
		newMario.yaa = this.yaa;
		newMario.yJumpSpeed = this.yJumpSpeed;
		newMario.xJumpSpeed = this.xJumpSpeed;
		return newMario;
	}

	void setMode(boolean large, boolean fire) {

		// System.out.println("large = " + large);
		if (fire)
			large = true;
		if (!large)
			fire = false;

		lastLarge = MyMario.large;
		lastFire = MyMario.fire;

		MyMario.large = large;
		MyMario.fire = fire;

		newLarge = MyMario.large;
		newFire = MyMario.fire;


	}





	public void move() {
		if (SimulatorOptions.isFly) {
			xa = ya = 0;
			ya = myKeys.isPressed(MarioKey.DOWN) ? 10 : ya;
			ya = myKeys.isPressed(MarioKey.UP) ? -10 : ya;
			xa = myKeys.isPressed(MarioKey.RIGHT) ? 10 : xa;
			xa = myKeys.isPressed(MarioKey.LEFT) ? -10 : xa;
		}


		wasOnGround = onGround;
		//System.out.println(myKeys.isPressed(MarioKey.RIGHT));
		float sideWaysSpeed = myKeys.isPressed(MarioKey.SPEED) ? 1.2f : 0.6f;

		// float sideWaysSpeed = onGround ? 2.5f : 1.2f;

		if (onGround) {
			ducking = myKeys.isPressed(MarioKey.DOWN) && large;
		}

		if (xa > 2) {
			facing = 1;
		}
		if (xa < -2) {
			facing = -1;
		}

		// float Wind = 0.2f;
		// float windAngle = 180;
		// xa += Wind * Math.cos(windAngle * Math.PI / 180);

		if (myKeys.isPressed(MarioKey.JUMP) || (jumpTime < 0 && !onGround && !sliding)) {
			if (!(mayJump) && onGround) {
				myKeys.release(MarioKey.JUMP);
			}
			if (jumpTime < 0) {
				xa = xJumpSpeed;
				ya = -jumpTime * yJumpSpeed;
				jumpTime++;
				myKeys.reset();
			} else if (onGround && mayJump) {
				xJumpSpeed = 0;
				yJumpSpeed = -1.9f;
				jumpTime = (int) jT;
				ya = jumpTime * yJumpSpeed;
				onGround = false;
				sliding = false;
			} else if (sliding && mayJump) {
				xJumpSpeed = -facing * 6.0f;
				yJumpSpeed = -2.0f;
				jumpTime = -6;
				xa = xJumpSpeed;
				ya = -jumpTime * yJumpSpeed;
				onGround = false;
				sliding = false;
				facing = -facing;
			} else if (jumpTime > 0) {
				xa += xJumpSpeed;
				ya = jumpTime * yJumpSpeed;
				jumpTime--;
			}
		} else {
			jumpTime = 0;
		}

		if (myKeys.isPressed(MarioKey.LEFT) && !ducking) {
			if (facing == 1)
				sliding = false;
			xa -= sideWaysSpeed;
			if (jumpTime >= 0)
				facing = -1;
		}

		if (myKeys.isPressed(MarioKey.RIGHT) && !ducking) {
			if (facing == -1)
				sliding = false;
			xa += sideWaysSpeed;
			if (jumpTime >= 0)
				facing = 1;
		}

		if ((!myKeys.isPressed(MarioKey.LEFT) && !myKeys.isPressed(MarioKey.RIGHT)) || ducking || ya < 0 || onGround) {
			sliding = false;
		}

		if (myKeys.isPressed(MarioKey.SPEED) && ableToShoot && MyMario.fire
				&& levelScene.fireballsOnScreen < 2) {
			levelScene.addSprite(new Fireball(levelScene, x + facing * 6,
					y - 20, facing));
		}
		// Cheats:
		if (SimulatorOptions.isPowerRestoration && myKeys.isPressed(MarioKey.SPEED)
				&& (!MyMario.large || !MyMario.fire))
			setMode(true, true);

		ableToShoot = !myKeys.isPressed(MarioKey.SPEED);

		mayJump = (onGround || sliding) && !myKeys.isPressed(MarioKey.JUMP);



		runTime += (Math.abs(xa)) + 5;
		if (Math.abs(xa) < 0.5f) {
			runTime = 0;
			xa = 0;
		}



		if (sliding) {
			for (int i = 0; i < 1; i++) {
				levelScene.addSprite(new Sparkle(
						(int) (x + Math.random() * 4 - 2) + facing * 8,
						(int) (y + Math.random() * 4) - 24, (float) (Math
						.random() * 2 - 1), (float) Math.random() * 1,
						0, 1, 5));
			}
			ya *= 0.5f;
		}

		onGround = false;



		if (x < 0) {
			x = 0;
			xa = 0;
		}

		/* if (x > levelScene.level.xExit * LevelScene.cellSize *//*- 8*//*
																		 * && x
																		 * <
																		 * levelScene
																		 * .
																		 * level
																		 * .
																		 * xExit
																		 * *
																		 * LevelScene
																		 * .
																		 * cellSize
																		 * + 2 *
																		 * LevelScene
																		 * .
																		 * cellSize
																		 * && y
																		 * <
																		 * levelScene
																		 * .
																		 * level
																		 * .
																		 * yExit
																		 * *
																		 * LevelScene
																		 * .
																		 * cellSize
																		 * )
																		 */


		ya *= 0.85f;
		if (onGround) {
			xa *= (GROUND_INERTIA + windScale(windCoeff, facing) + iceScale(iceCoeff));
		} else {
			xa *= (AIR_INERTIA + windScale(windCoeff, facing) + iceScale(iceCoeff));
		}

		// if /

		if (!onGround) {
			// ya += 3;
			ya += yaa;
		}

	}


}













