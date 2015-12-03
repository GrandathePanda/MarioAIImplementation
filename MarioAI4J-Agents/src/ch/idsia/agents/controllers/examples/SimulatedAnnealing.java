package ch.idsia.agents.controllers.examples;

import java.awt.Graphics;
import java.util.*;

import EnemyActorPhysics.MyMario;
import EnemyActorPhysics.MySprite;
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

public class SimulatedAnnealing extends MarioHijackAIBase implements IAgent {
	private final Action[] allPossibleActions = {Action.Jump,Action.LeftLongJump,Action.RightLongJump,Action.Left,Action.Right,Action.LeftSpeed,Action.RightSpeed};
	private final Action[] airPossibleActions = {Action.LeftLongJump,Action.RightLongJump,Action.Left,Action.Right};
	private final Action[] groundPossibleActions = {Action.Jump,Action.Left,Action.Right,Action.LeftSpeed,Action.RightSpeed};
	private final Action[] airHangPossibleActions = {Action.Left,Action.Right};
	boolean g1 = false; //deal with air drop in the beggining
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
	/**
	 * 
	 * @param x First element X coordinate
	 * @param y First element Y coordinate
	 * @param otherX Second element X coordinate
	 * @param otherY Second element Y coordinate
	 * @return Haversine distance between the two.
	 */

	private MarioInput doActions(Vector<Action> ac) {
		MarioInput doThese = new MarioInput();
		for(Action x : ac) {
//			System.out.println(x);
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
	
	public double distance(int x, int y, int otherX, int otherY){
		int curr = (otherX - x ) * (otherX - x) + (otherY - y)  * (otherY - y);
		return Math.sqrt(curr);
	}
	

	public genPair<Pair, genPair<Action, HashMap<Pair,Node>>> pickRandom(Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> vec){
		int index = (int)Math.random()/vec.size();
		return vec.get(index);
	}

	public genPair<Pair, genPair<Action, HashMap<Pair,Node>>> pickBetter(genPair<Pair, genPair<Action, HashMap<Pair,Node>>> p1,genPair<Pair, genPair<Action, HashMap<Pair,Node>>> p2 ){
		int heuristic1 = 0;
		int heuristic2 = 0;
		if(p1.x != p2.x){
			System.out.println("err");
		}
		Pair marioLoc = p1.x;
		genPair<Action,HashMap<Pair,Node>> p1Child = p1.y;
		
		if(p1Child.y.get(marioLoc).enemyHere){
			--heuristic1;
		}
		if(p1Child.y.get(marioLoc).blockHere){
			--heuristic1;
		}
		if(p1Child.y.get(marioLoc).xPos > 0){
			++heuristic1;
		}
		genPair<Action,HashMap<Pair,Node>> p2Child = p2.y;
		if(p2Child.y.get(marioLoc).enemyHere){
			--heuristic2;
		}
		if(p2Child.y.get(marioLoc).blockHere){
			--heuristic2;
		}
		if(p2Child.y.get(marioLoc).xPos > 0){
			++heuristic2;
		}		
		genPair<Pair, genPair<Action, HashMap<Pair,Node>>> ret = (heuristic1 > heuristic2) ?  p1 :  p2;
		return ret;
	}

	
	boolean graph = false;
	GraphGenerator Graph = null;
	Action[] possibleActions = {Action.Jump,Action.LeftLongJump,Action.RightLongJump,Action.Left,Action.Right,Action.LeftSpeed,Action.RightSpeed};
	public MarioInput actionSelectionAI(){
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
                if(!simM.onGround)
                        modPosAction = airPossibleActions;
                else modPosAction = airHangPossibleActions;
        }

		int runs = 0;
		boolean firstStateSeenCurrent = false;
		frontierStates.push(Graph.State);
		solutionStates.add(Graph.State);
		Vector<Action> toDo = new Vector<Action>();
		while(!frontierStates.isEmpty() && runs < 9){
			HashMap<Pair,Node> currentState = frontierStates.removeFirst();
			solutionStates.add(currentState);
			seenStates.add(currentState);
			Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> childStates = new Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>>();
			if (firstStateSeenCurrent) {
				System.out.println("HERE TickModel");
				childStates = Graph.tickModel(currentState, modPosAction);
			} else {
				System.out.println("HERE Tick");
				childStates = Graph.tick(currentState, modPosAction);
				firstStateSeenCurrent = true;
			}
//			System.out.println(childStates.size());
			genPair<Pair,genPair<Action,HashMap<Pair,Node>>> option1 = pickRandom(childStates);
			childStates.remove(option1);
			genPair<Pair,genPair<Action,HashMap<Pair,Node>>> option2 = pickRandom(childStates);
			genPair<Pair,genPair<Action,HashMap<Pair,Node>>> act = pickBetter(option1, option2);
			System.out.println(act.y.x);
			toDo.add(act.y.x);

			++runs;
		}
		
		return doActions(toDo);
	}
	/*
	public MarioInput actionSelectionAI2(){
		if(!graph){
			Graph = new GraphGenerator(9,9,mario);
			Graph.generateGraph(e, t);
			Graph.isGraphGenerated = true;
			graph = true;
		}
		else {
			Graph.resetNodes(e, t);
		}
		int runs = 0;
		Vector<HashMap<Pair,Node>> states = new Vector<HashMap<Pair, GraphGenerator.Node>>();
		states.add(mapCopy(Graph.State));
		HashMap<Pair,Node> curr = states.get(0);
		Pair p2 = new Pair(9,9);
		Node p = curr.get(p2);
		Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> possibleMoves = null;
		possibleMoves = Graph.tickModel(curr, possibleActions);

		possibleMoves.get(0).y.y.get(p2);
		
		Node marioNode = null;
		Vector<genPair<Pair, MySprite>> existingEntities = new Vector<>();
		for(Map.Entry<Pair, Node> et: curr.entrySet()){
			Node cNode = et.getValue();
			Pair pos = new Pair(cNode.xPos, cNode.yPos);
			List<MySprite> cNodeEntities = cNode.modelEntitiesHere;
			for(MySprite x: cNodeEntities){
				existingEntities.add(new genPair<>(pos,x));
			}
			if(cNode.mario) marioNode = cNode;
		}
		
		genPair<Node, Action> myAct = pickBest(possibleMoves, marioNode);
		Action toDo = myAct.y;
	
		System.gc();
		return action;
	}*/
	public static void main(String[] args){
		String options = FastOpts.FAST_VISx2_02_JUMPING+FastOpts.L_DIFFICULTY(0)+FastOpts.L_ENEMY(Enemy.GOOMBA);
		MarioSimulator simulator = new MarioSimulator(options);
		IAgent agent = new SimulatedAnnealing();
		simulator.run(agent);
		
		System.exit(0);
	}
}
