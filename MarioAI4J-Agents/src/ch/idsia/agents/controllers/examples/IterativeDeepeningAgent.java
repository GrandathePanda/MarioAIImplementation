//Made it better.

package ch.idsia.agents.controllers.examples;

import EnemyActorPhysics.MyMario;
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
import togepi.GraphGenerator.Action;
import togepi.GraphGenerator.Node;
import togepi.Pair;
import togepi.genPair;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import static togepi.GraphGenerator.mapCopy;

/**
 * Your custom agent! Feel free to fool around!
 * 
 * @author Jakub 'Jimmy' Gemrot, gemrot@gamedev.cuni.cz
 */
public class IterativeDeepeningAgent extends MarioHijackAIBase implements IAgent {

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
	private final Action[] allPossibleActions = {Action.Jump,Action.LeftLongJump,Action.RightLongJump,Action.Left,Action.Right,Action.LeftSpeed,Action.RightSpeed};
	private final Action[] airPossibleActions = {Action.LeftLongJump,Action.RightLongJump,Action.Left,Action.Right};
	private final Action[] groundPossibleActions = {Action.Jump,Action.Left,Action.Right,Action.LeftSpeed,Action.RightSpeed};
	private final Action[] airHangPossibleActions = {Action.Left,Action.Right};
	boolean g1 = false; //deal with air drop in the beggining
	public MarioInput actionSelectionAI() {

		Vector<HashMap<Pair,Node>> solutionStates = new Vector<HashMap<Pair, Node>>();
		LinkedList<HashMap<Pair,Node>> frontierStates = new LinkedList<HashMap<Pair,Node>>();
		Vector<HashMap<Pair,Node>> seenStates = new Vector<HashMap<Pair,Node>>();
		if(!graph) { //If the graph hasn't been generated yet, generate it.
			Graph = new GraphGenerator(9,9,mario);
			Graph.generateGraph(e,t);
			Graph.isGraphGenerated = true;
			graph = true;
		}
		else {
			Graph.resetNodes(e,t,mario);
		}
		MyMario simM = Graph.State.get(new Pair(0,0)).alterMario;
		Action[] modPosAction;
		if(simM.onGround) {
			modPosAction = groundPossibleActions;
		}
		else {
			if(simM.jumpTime > 3)
				modPosAction = airPossibleActions;
			else modPosAction = airHangPossibleActions;
		}
		int runs = 0;
		int limit = 1;
		boolean weMadeIt = false;
		boolean firstStateSeenCurrent = false;
		frontierStates.push(mapCopy(Graph.State));
		solutionStates.add(mapCopy(Graph.State));
		while(!frontierStates.isEmpty() && runs < limit) {
			HashMap<Pair,Node> currentState = frontierStates.removeFirst();
			for(Map.Entry<Pair,Node> x : currentState.entrySet()) {
				if(x.getValue().mario) simM = x.getValue().alterMario;
			}
			solutionStates.add(currentState);
			seenStates.add(currentState);
			Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> childStates = null;
			if(mario.onGround && !g1) g1 = true;
			if(!g1) return action;
			if (firstStateSeenCurrent) {
				childStates = Graph.tickModel(currentState, modPosAction);
				System.out.println("TICKING MODEL");
			} else {
				childStates = Graph.tick(currentState, modPosAction);
				System.out.println("TICKING ORIGINAL");
				firstStateSeenCurrent = true;
			}
			for(genPair<Pair,genPair<Action,HashMap<Pair,Node>>> x : childStates) {
				Pair marioPosChild = x.x;
				HashMap<Pair,Node> child = x.y.y;
				Action doThis = x.y.x;
				if(marioPosChild.x > 2 || runs >= 9) weMadeIt = true;
				if(runs < limit) frontierStates.addFirst(child);
				switch(doThis) {
					case Jump:
						action.set(MarioKey.JUMP,simM.mayJump && g1);
						break;
					case RightLongJump:
						if(!simM.onGround) action.press(MarioKey.JUMP);
						action.toggle(MarioKey.LEFT);
						action.press(MarioKey.RIGHT);
					case LeftLongJump:
						if(!simM.onGround) action.press(MarioKey.JUMP);
						action.toggle(MarioKey.RIGHT);
						action.press(MarioKey.LEFT);
					case Right:
						action.toggle(MarioKey.LEFT);
						action.press(MarioKey.RIGHT);
						break;
					case RightSpeed:
						action.toggle(MarioKey.LEFT);
						action.press(MarioKey.RIGHT);
						action.press(MarioKey.SPEED);
						break;
					case Left:
						action.toggle(MarioKey.RIGHT);
						action.press(MarioKey.LEFT);
						break;
					case LeftSpeed:
						action.toggle(MarioKey.RIGHT);
						action.press(MarioKey.LEFT);
						action.press(MarioKey.SPEED);
						break;
				}


			}
			if(!weMadeIt) ++limit;
			++runs;
		}
		
		System.gc();
		return action;
	}
	
	public static void main(String[] args) {
		String options = FastOpts.FAST_VISx2_02_JUMPING+FastOpts.L_DIFFICULTY(0)+FastOpts.L_ENEMY(Enemy.GOOMBA)+FastOpts.L_RANDOMIZE+FastOpts.L_CANNONS_ON;

		MarioSimulator simulator = new MarioSimulator(options);
		
		IAgent agent = new IterativeDeepeningAgent();
		
		simulator.run(agent);
		
		System.exit(0);
	}
}