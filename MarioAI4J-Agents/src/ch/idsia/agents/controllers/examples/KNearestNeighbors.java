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
import java.util.Collections;
import java.util.Comparator;
 

import static togepi.GraphGenerator.mapCopy;

public class KNearestNeighbors extends MarioHijackAIBase implements IAgent {
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
	

	
	public Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> eliminateUseless( Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> vec){
		for(genPair<Pair,genPair<Action, HashMap<Pair,Node>>> curr: vec){
			HashMap<Pair,Node> map = curr.y.y;
			Iterator it = map.entrySet().iterator();
			while(it.hasNext()){
				Node p = (Node)it.next();
				
//				if(p.blockHere){
//					it.remove();
//				}
			}
		}
		System.out.println("got out");
		return vec;
	}
	/**
	 * 
	 * @param vec -> This is going to be a vector with all of the action options
	 * @param currentPos -> This is going to be mario's current position.
	 * @return
	 */
	public genPair<Node, Action> pickBest(Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> vec, Node currentPos){
		//Takes the node looks at xPos and yPos, picks the option that will bring it 
		//furthest to the right
		System.out.println("vec size: " + vec.size());
//		vec = eliminateUseless(vec);
		double currGreatestHeuristic = 0.0; //Current greatest heuristic, or furthest distance
		Node greatestNode = null; //Current chosen Node.
		Action toDo = null; //current chosen Action
//		System.out.println("vec size: " +vec.size());
		for(genPair<Pair,genPair<Action, HashMap<Pair,Node>>> curr: vec){ //Loop through all possible location results, finds the one with gratest distance
//			Pair marioPosChild = x.x;
			int counter = 0;

			HashMap<Pair,Node> child = curr.y.y;
//			System.out.println(child.size()); //***NOTE*** I NEED THIS TO JUST LOOP THROUGH 81 ELEMENTS SURROUNDING MARIO. NOT THE FULL MAP!
			Iterator it = child.entrySet().iterator();
			while(it.hasNext()){ //Loop through and picks the action/node with heighest heuristic
				if(counter == 8){
					System.out.println("breaking");
					break;
				}
				Map.Entry pair = (Map.Entry)it.next();
				Node p = (Node)pair.getValue();
				double distance = distance(p.xPos, p.yPos, currentPos.xPos, currentPos.yPos); //cannot calculate distance this way
				if(curr.y.x == Action.Jump){
					System.out.println("TRYING");
				}
//				System.out.println("p.xPos: " + p.xPos + " p.yPos: " + p.yPos + " currentPos.xPos: " + currentPos.xPos + " currentPos.yPos: " + currentPos.yPos);
//				System.out.println("Distance: " + distance);

				if(/*p.xPos > currentPos.xPos && */distance > currGreatestHeuristic){
					greatestNode = p;
					currGreatestHeuristic = distance;
					toDo = curr.y.x;
//					System.out.println("Node: " + greatestNode.xPos + ", " + greatestNode.yPos + " heuristic: " + currGreatestHeuristic + " action: " + toDo );
				}
				++counter;
				it.remove();
			}

		}
		
		System.out.println("**************************************** HERE");
//		System.out.println("I have changed my mind: " + counter + " number of times.");
		System.out.println("I will do: " + toDo);
		genPair<Node, Action> ret = new genPair<Node,Action>(greatestNode, toDo);
		return ret;
	}
	
	public Vector<Action> pickMove(Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> childStates){
		System.out.println("Child states size: " + childStates.size());
		Vector<Action> ret = null;
		double greatestDistance = 0;
		Pair greatPos;
		Action currActions = Action.Right;
		int count = 1;
		for(genPair<Pair,genPair<Action,HashMap<Pair,Node>>> each: childStates){
			if(count == childStates.size()){
				break;
			}
			Pair marioPos = each.x;
			genPair<Action, HashMap<Pair,Node>> currChild = each.y;
//			double distance = distance(9,-4,each.x.x,each.x.y);
			double distance  = 1.0;
			if(greatestDistance < distance){
				greatestDistance = distance;
				greatPos = each.x;
				currActions = each.y.x;
			}
			++count;
		}
		ret.add(currActions);
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
			Graph.resetNodes(e,t,mario);

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
			double greatestDistance = 0;
			Pair greatPos;
			Action currActions = Action.Left;
			int count = 1;
			System.out.println(childStates.size());
			for(genPair<Pair,genPair<Action,HashMap<Pair,Node>>> each: childStates){
				if(count == childStates.size()){
					break;
				}
				Pair marioPos = each.x;
				genPair<Action, HashMap<Pair,Node>> currChild = each.y;
//				double distance = distance(9,-4,each.x.x,each.x.y);
				double distance  = 1.0;
				if(greatestDistance < distance){
					greatestDistance = distance;
					greatPos = each.x;
					currActions = each.y.x;
				}
				++count;
//				System.out.println(currActions);
				toDo.add(currActions);

			}
			++runs;
		}
		
		return doActions(toDo);
	}
	
	public static void main(String[] args){
		String options = FastOpts.FAST_VISx2_02_JUMPING+FastOpts.L_DIFFICULTY(0)+FastOpts.L_ENEMY(Enemy.GOOMBA);

		MarioSimulator simulator = new MarioSimulator(options);
		IAgent agent = new KNearestNeighbors();
		simulator.run(agent);
		
		System.exit(0);
	}
}
