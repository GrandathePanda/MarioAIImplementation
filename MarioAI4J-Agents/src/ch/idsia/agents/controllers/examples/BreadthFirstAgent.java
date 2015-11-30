//Made it better.

package ch.idsia.agents.controllers.examples;

import java.awt.Graphics;
import java.util.*;

import ch.idsia.agents.AgentOptions;
import ch.idsia.agents.IAgent;
import ch.idsia.agents.controllers.MarioHijackAIBase;
import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.engine.LevelScene;
import ch.idsia.benchmark.mario.engine.VisualizationComponent;
import ch.idsia.benchmark.mario.engine.generalization.Enemy;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.engine.input.MarioKey;
import ch.idsia.benchmark.mario.environments.IEnvironment;
import ch.idsia.benchmark.mario.options.FastOpts;
import togepi.GraphGenerator;
import togepi.genPair;
import togepi.GraphGenerator.*;
import togepi.Pair;

import static togepi.GraphGenerator.mapCopy;

/**
 * Your custom agent! Feel free to fool around!
 * 
 * @author Jakub 'Jimmy' Gemrot, gemrot@gamedev.cuni.cz
 */
public class BreadthFirstAgent extends MarioHijackAIBase implements IAgent {

	private boolean shooting = false;
	
	@Override
	public void reset(AgentOptions options) {
		super.reset(options);
	}
	
	@Override
	public void debugDraw(VisualizationComponent vis, LevelScene level,	IEnvironment env, Graphics g) {
		super.debugDraw(vis, level, env, g);
		// provide custom visualization using 'g'
	}




	boolean graph = false;
	GraphGenerator Graph = null;
	Action[] possibleActions = {Action.Jump,Action.LeftLongJump,Action.LeftShortJump,Action.RightLongJump,Action.RightShortJump,Action.Left,Action.Right,Action.LeftSpeed,Action.RightSpeed};
	public MarioInput actionSelectionAI() {
		Vector<HashMap<Pair,Node>> solutionStates = new Vector<HashMap<Pair, GraphGenerator.Node>>();
		LinkedList<HashMap<Pair,Node>> frontierStates = new LinkedList<HashMap<Pair,Node>>();
		Vector<HashMap<Pair,Node>> seenStates = new Vector<HashMap<Pair,Node>>();
		if(!graph) { //If the graph hasn't been generated yet, generate it.
			Graph = new GraphGenerator(9,9,mario);
			Graph.generateGraph(e,t);
			Graph.isGraphGenerated = true;
			graph = true;
		}
		else {
			Graph.resetNodes(e,t);
		}
		int runs = 0;
		boolean firstStateSeenCurrent = false;
		frontierStates.push(mapCopy(Graph.State));
		solutionStates.add(mapCopy(Graph.State));
		while(!frontierStates.isEmpty() && runs < 9) {
			HashMap<Pair,Node> currentState = frontierStates.removeFirst();
			solutionStates.add(currentState);
			seenStates.add(currentState);
			Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> childStates = null;
			if (firstStateSeenCurrent) {
				System.out.println("HERE TickModel");
				childStates = Graph.tickModel(currentState, possibleActions);
			} else {
				System.out.println("HERE Tick");
				childStates = Graph.tick(currentState, possibleActions);
				firstStateSeenCurrent = true;
			}

			for(genPair<Pair,genPair<Action,HashMap<Pair,Node>>> x : childStates) {
				Pair marioPosChild = x.x;
				HashMap<Pair,Node> child = x.y.y;
				Action doThis = x.y.x;
				if(runs+1 < 9) frontierStates.add(child);
				//action.press(MarioKey.RIGHT);
				System.out.println(doThis.toString()+" ");
				switch(doThis) {
					case Jump:
						action.set(MarioKey.JUMP,mario.mayJump);
						System.out.println("HERE j");
						break;
					case RightShortJump:
						System.out.println("HERE rsj");
						action.set(MarioKey.JUMP,mario.mayJump);
						action.press(MarioKey.RIGHT);

						break;
					case RightLongJump:
						System.out.println("HERE rlj");
						action.set(MarioKey.JUMP,Graph.simMario.mayJump);
						action.press(MarioKey.RIGHT);
						action.press(MarioKey.JUMP);

						break;
					case LeftShortJump:
						System.out.println("HERE lsj");
						action.set(MarioKey.JUMP,Graph.simMario.mayJump);
						//action.press(MarioKey.LEFT);
						//action.release(MarioKey.JUMP);
						break;
					case LeftLongJump:
						System.out.println("HERE llj");
						action.set(MarioKey.JUMP,Graph.simMario.mayJump);
						//action.press(MarioKey.LEFT);
						break;
					case Right:
						System.out.println("HERE r");
						action.press(MarioKey.RIGHT);
						break;
					case RightSpeed:
						System.out.println("HERE rs");
						action.press(MarioKey.RIGHT);
						action.press(MarioKey.SPEED);
						break;
					case Left:
						System.out.println("HERE l");
						//action.press(MarioKey.LEFT);
						break;
					case LeftSpeed:
						System.out.println("HERE ls");
						//action.press(MarioKey.LEFT);
						action.press(MarioKey.SPEED);
						break;
				}


			}
			++runs;
		}
		
		System.out.println("REturning");
		System.gc();
		return action;
	}
	
	public static void main(String[] args) {
		String options = FastOpts.FAST_VISx2_02_JUMPING+FastOpts.L_DIFFICULTY(0)+FastOpts.L_ENEMY(Enemy.GOOMBA);
		
		MarioSimulator simulator = new MarioSimulator(options);
		
		IAgent agent = new BreadthFirstAgent();
		
		simulator.run(agent);
		
		System.exit(0);
	}
}