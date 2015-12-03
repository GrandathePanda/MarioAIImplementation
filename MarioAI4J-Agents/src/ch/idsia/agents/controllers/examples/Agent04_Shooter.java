package ch.idsia.agents.controllers.examples;

import ch.idsia.agents.AgentOptions;
import ch.idsia.agents.IAgent;
import ch.idsia.agents.controllers.MarioHijackAIBase;
import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.engine.LevelScene;
import ch.idsia.benchmark.mario.engine.generalization.Enemy;
import ch.idsia.benchmark.mario.engine.generalization.Entity;
import ch.idsia.benchmark.mario.engine.generalization.EntityType;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.engine.input.MarioKey;
import ch.idsia.benchmark.mario.options.FastOpts;
import togepi.GraphGenerator;

import java.util.Iterator;
import java.util.List;

/**
 * Agent that sprints forward, jumps and shoots.
 * 
 * @author Jakub 'Jimmy' Gemrot, gemrot@gamedev.cuni.cz
 */
public class Agent04_Shooter extends MarioHijackAIBase implements IAgent {

	private boolean shooting = false;
	LevelScene y = new LevelScene();
	LevelScene x = new LevelScene();
	
	@Override
	public void reset(AgentOptions options) {
		super.reset(options);
	}
	protected void approxMove(float x, float y, float sX, float sY, float dx, float dy){
		double xF = sX*0.04+0.5*dx*0.00004;
		double yF = sY*0.04+0.5*dy*0.00004;
		System.out.println((x+xF)+"---"+(y+yF));

	}
	private boolean enemyAhead() {
		return
				   e.danger(1, 0) || e.danger(1, -1) 
				|| e.danger(2, 0) || e.danger(2, -1)
				|| e.danger(3, 0) || e.danger(2, -1);
	}
	
	private boolean brickAhead() {
		return
				   t.brick(1, 0) || t.brick(1, -1) 
				|| t.brick(2, 0) || t.brick(2, -1)
				|| t.brick(3, 0) || t.brick(3, -1);
	}
	public void approxMarioJump(float x, float y, float sX, float sY,boolean left,boolean right,boolean longJ) {
		int jumpTime = 0;
		double xD = 0;
		double yD = 0;
		float xJump = 0;
		float yJump = 0;
		int count = 0;
		float xa = 0.6f;
		float ya = 0;
		boolean onGround = true;

		int limit = 14;
		if(!longJ) limit = 4;
		while (count < limit) {
			if (jumpTime < 0) {
				xa = xJump;
				ya = -jumpTime * yJump;
				jumpTime++;
			} else if (onGround) {
				xJump = 0;
				yJump = -1.9f;
				jumpTime = 7;
				ya = jumpTime * yJump;
				onGround = false;
			} else if (jumpTime > 0) {
				xa += xJump;
				ya = jumpTime * yJump;
				jumpTime--;
			} else {
				jumpTime = 0;
				onGround = true;
			}
			yD += ya * 0.04 + 0.5 * (jumpTime) * 0.0016;
			xD += xa * 0.4;
			++count;
		}
		System.out.println(x+=xD);
		System.out.println(y+=yD);
	}

	public MarioInput actionSelectionAI() {
		e.getClass();
		approxMarioJump(0,0,0.6f,0,false,true,true);

		boolean run = false;
		// ALWAYS RUN RIGHT
		if(run == false) {
			action.press(MarioKey.RIGHT);
			run = true;
		}
		
		// ENEMY || BRICK AHEAD => JUMP
		// WARNING: do not press JUMP if UNABLE TO JUMP!
		action.set(MarioKey.JUMP, (enemyAhead() || brickAhead()) && mario.mayJump);
		
		// If in the air => keep JUMPing
		if (!mario.onGround) {
			action.press(MarioKey.JUMP);
		}
		
		if (mario.mayShoot) {
			if (shooting) {
				shooting = false;
				action.release(MarioKey.SPEED);
			} else 
			if (action.isPressed(MarioKey.SPEED)) {				
				action.release(MarioKey.SPEED);
			} else {
				shooting = true;
				action.press(MarioKey.SPEED);
			}
		} else {
			if (shooting) {
				shooting = false;
				action.release(MarioKey.SPEED);
			}
		}
		
		return action;
	}
	
	public static void main(String[] args) {
		// IMPLEMENTS END-LESS RUNS
		while (true) {
			String options = FastOpts.FAST_VISx2_02_JUMPING + FastOpts.L_ENEMY(Enemy.GOOMBA, Enemy.RED_KOOPA) + FastOpts.L_RANDOMIZE;
			
			MarioSimulator simulator = new MarioSimulator(options);
			
			IAgent agent = new Agent04_Shooter();
			
			simulator.run(agent);
		}
		
		//System.exit(0);
	}
}