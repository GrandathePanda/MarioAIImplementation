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


	public MarioInput actionSelectionAI() {
		//System.out.println(t.brick(1,0));

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
			Graph.resetNodes(e,t,mario);

		}
		MyMario simM = Graph.State.get(new Pair(0,0)).alterMario;
		Action[] modPosAction;
		if(simM.onGround || mario.onGround) {
			modPosAction = groundPossibleActions;
		}
		else {
			if(!simM.onGround || !mario.onGround)
				modPosAction = airPossibleActions;
			else modPosAction = airHangPossibleActions;
		}
		int runs = 0;
		boolean firstStateSeenCurrent = false;
		frontierStates.push(Graph.State);
		solutionStates.add(Graph.State);
		Pair oldMarioPos = new Pair(0,0);
		while(!frontierStates.isEmpty() && runs < 3) {
			HashMap<Pair,Node> currentState = frontierStates.removeFirst();
			solutionStates.add(currentState);
			seenStates.add(currentState);
			Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> childStates = null;
			if (firstStateSeenCurrent) {
				//System.out.println("HERE TickModel");
				childStates = Graph.tickModel(currentState, modPosAction);
			} else {
				//System.out.println("HERE Tick");
				childStates = Graph.tick(currentState, modPosAction);
				firstStateSeenCurrent = true;
			}
			genPair<Action,HashMap<Pair,Node>> tentativeBest = null;
			Pair tentativeLoc = null;
			double tentativeCost = 0;
			for(genPair<Pair,genPair<Action,HashMap<Pair,Node>>> x: childStates) {
				double cost = 0;
				Pair marioLoc = x.x;
				System.out.println(marioLoc.x+"---"+marioLoc.y);
				genPair<Action,HashMap<Pair,Node>> currChild = x.y;
				double heuristic = Math.sqrt(Math.pow(9-marioLoc.x,2)+Math.pow(-2-marioLoc.y,2));
				boolean samePos = (marioLoc.x == oldMarioPos.x && marioLoc.y == oldMarioPos.y);
				if(currChild.x == Action.Left || currChild.x == Action.LeftSpeed || currChild.x == Action.LeftLongJump) cost+= 5;
				double notMovingCost = ( samePos ) ? 1 : 0;
				double enemyCost = (Graph.collision(currChild.y.get(marioLoc).alterMario,currChild.y)) ? 1  : 0;
				double scaleCost = enemyCost*100;
				double pathCost =  0;
				if(tentativeLoc == null) {
					pathCost= Math.sqrt(Math.pow(0-marioLoc.x,2)+Math.pow(0-marioLoc.y,2));
				}
				else pathCost = Math.sqrt(Math.pow(tentativeLoc.x-marioLoc.x,2)+Math.pow(tentativeLoc.y-marioLoc.y,2));
				cost += (pathCost+scaleCost+heuristic+notMovingCost);
				if(tentativeBest == null || cost <= tentativeCost)
				{
					tentativeBest = currChild;
					tentativeLoc = marioLoc;
					tentativeCost = cost;
				}

			}
			if(childStates.size() == 0) {
				System.out.println(runs);
				return Graph.doActions(bestInTime);
			}
			frontierStates.addFirst(tentativeBest.y);
			oldMarioPos = tentativeLoc;
			//System.out.println(tentativeLoc.x + "  " + tentativeLoc.y +" --- " + tentativeBest.x.toString() + " -- " +tentativeCost);
			simM = tentativeBest.y.get(tentativeLoc).alterMario;
			bestInTime.add(tentativeBest.x);
			++runs;
		}

		System.gc();
		return Graph.doActions(bestInTime);
	}
	
	public static void main(String[] args) {
		String options = FastOpts.FAST_VISx2_02_JUMPING+FastOpts.L_DIFFICULTY(15)+FastOpts.L_ENEMY(Enemy.GOOMBA)+FastOpts.L_CANNONS_ON;

		MarioSimulator simulator = new MarioSimulator(options);
		
		IAgent agent = new AStarNewAgent();
		
		simulator.run(agent);
		
		System.exit(0);
	}
}