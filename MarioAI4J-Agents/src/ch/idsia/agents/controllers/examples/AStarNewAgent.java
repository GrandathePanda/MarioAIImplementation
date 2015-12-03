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
import java.util.Vector;

import static togepi.GraphGenerator.mapCopy;

/**
 * Your custom agent! Feel free to fool around!
 * 
 * @author Jakub 'Jimmy' Gemrot, gemrot@gamedev.cuni.cz
 */
public class AStarNewAgent extends MarioHijackAIBase implements IAgent {

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

	private MarioInput doActions(Vector<Action> ac) {
		MarioInput doThese = new MarioInput();
		for(Action x : ac) {
			switch (x) {
				case Jump:
					doThese.set(MarioKey.JUMP, mario.mayJump);
					break;
				case RightLongJump:
					doThese.press(MarioKey.JUMP);
					doThese.release(MarioKey.LEFT);
					doThese.press(MarioKey.RIGHT);
				case LeftLongJump:
					doThese.press(MarioKey.JUMP);
					doThese.release(MarioKey.RIGHT);
					action.press(MarioKey.LEFT);
				case Right:
					doThese.release(MarioKey.LEFT);
					doThese.press(MarioKey.RIGHT);
					break;
				case RightSpeed:
					doThese.release(MarioKey.LEFT);
					doThese.press(MarioKey.RIGHT);
					doThese.press(MarioKey.SPEED);
					break;
				case Left:
					doThese.release(MarioKey.RIGHT);
					doThese.press(MarioKey.LEFT);
					break;
				case LeftSpeed:
					doThese.release(MarioKey.RIGHT);
					doThese.press(MarioKey.LEFT);
					doThese.press(MarioKey.SPEED);
					break;
			}
		}
		return doThese;
	}
	public MarioInput actionSelectionAI() {

		Vector<HashMap<Pair,Node>> solutionStates = new Vector<HashMap<Pair, Node>>();
		LinkedList<HashMap<Pair,Node>> frontierStates = new LinkedList<HashMap<Pair,Node>>();
		Vector<HashMap<Pair,Node>> seenStates = new Vector<HashMap<Pair,Node>>();
		Vector<Action> bestInTime = new Vector<>();
		if(!graph) { //If the graph hasn't been generated yet, generate it.
			Graph = new GraphGenerator(9,9,mario);
			Graph.generateGraph(e,t);
			Graph.isGraphGenerated = true;
			graph = true;
		}
		else {
			Graph.resetNodes(e,t);

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
		boolean firstStateSeenCurrent = false;
		frontierStates.push(Graph.State);
		solutionStates.add(Graph.State);
		while(!frontierStates.isEmpty() && runs < 5) {
			HashMap<Pair,Node> currentState = frontierStates.removeFirst();
			solutionStates.add(currentState);
			seenStates.add(currentState);
			Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> childStates = null;
			if (firstStateSeenCurrent) {
				System.out.println("HERE TickModel");
				childStates = Graph.tickModel(currentState, modPosAction);
			} else {
				System.out.println("HERE Tick");
				childStates = Graph.tick(currentState, modPosAction);
				firstStateSeenCurrent = true;
			}
			genPair<Action,HashMap<Pair,Node>> tentativeBest = null;
			Pair tentativeLoc = null;
			double tentativeCost = 0;
			for(genPair<Pair,genPair<Action,HashMap<Pair,Node>>> x: childStates) {
				int cost = 0;
				Pair marioLoc = x.x;
				genPair<Action,HashMap<Pair,Node>> currChild = x.y;
				double heuristic = Math.sqrt(Math.pow(9-marioLoc.x,2)+Math.pow(-4-marioLoc.y,2));

				double enemyCost = (currChild.y.get(marioLoc).enemyHere) ? 1 : 0;
				double scaleCost = enemyCost*10;
				double pathCost =  0;
				if(tentativeLoc == null) {
					pathCost= Math.sqrt(Math.pow(0-marioLoc.x,2)+Math.pow(0-marioLoc.y,2));
				}
				else pathCost = Math.sqrt(Math.pow(tentativeLoc.x-marioLoc.x,2)+Math.pow(tentativeLoc.y-marioLoc.y,2));
				cost = (int)(pathCost+scaleCost+heuristic);
				if(tentativeBest == null || cost <= tentativeCost)
				{
					tentativeBest = currChild;
					tentativeLoc = marioLoc;
					tentativeCost = cost;
				}

			}
			frontierStates.addFirst(tentativeBest.y);
			System.out.println(tentativeLoc.x + "  " + tentativeLoc.y);
			simM = tentativeBest.y.get(tentativeLoc).alterMario;
			bestInTime.add(tentativeBest.x);
			++runs;
		}
		
		System.out.println("REturning");
		System.gc();
		return doActions(bestInTime);
	}
	
	public static void main(String[] args) {
		String options = FastOpts.FAST_VISx2_02_JUMPING+FastOpts.L_DIFFICULTY(1)+FastOpts.L_ENEMY(Enemy.GOOMBA)+FastOpts.L_RANDOMIZE+FastOpts.L_CANNONS_ON;
		
		MarioSimulator simulator = new MarioSimulator(options);
		
		IAgent agent = new AStarNewAgent();
		
		simulator.run(agent);
		
		System.exit(0);
	}
}