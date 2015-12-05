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

public class EvolutionStrategy extends MarioHijackAIBase implements IAgent {
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
		int rand = (int)Math.random();
		double actRand = Math.random() * 10;
		int index = (int)actRand%vec.size();
		return vec.get(index);
	}
	

	public genPair<Pair, genPair<Action, HashMap<Pair,Node>>> pickBetter(genPair<Pair, genPair<Action, HashMap<Pair,Node>>> p1,genPair<Pair, genPair<Action, HashMap<Pair,Node>>> p2 ){
		int heuristic1 = 0;
		int heuristic2 = 0;

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
/**
* What needs to be done:
* generate a vector containing random actions
* These actions are to be executed sequentially
* The resultant(final node/pair) of the final action of each of these vectors would be used to 
* Determine the final state
* The final state's heuristic is then determined. 
* Determine cost
* Compare vectors of actions, the one with the greatest heuristic - cost will be used
* 
* 
*/
	
	public Vector<Action> pickBestVectorOfActions(Vector<Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>>> tC){
		Vector<Action> ret = new Vector<Action>();
		Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> p1 = null;
		Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> p2 = null;
		int currBestHeuristic = 0;
		int currLeastEnemyCost = Integer.MAX_VALUE;
		Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> currBestVec = null;
		for(Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> eachVec: tC){
			Pair marioStartLoc = null;
			int currHeuristic = 0;
			int currCost = 0;
			for(genPair<Pair,genPair<Action,HashMap<Pair,Node>>> each: eachVec){
				if(marioStartLoc == null){
					marioStartLoc = each.x;
					continue;
				}
				Pair marioCurrLoc = each.x;
				genPair<Action,HashMap<Pair,Node>> currChild = each.y;
				
				if(currChild.y.get(marioCurrLoc).enemyHere){
					++currCost;
				}

				if(currChild.y.get(marioCurrLoc).xPos > 0){
					++currHeuristic;
				}
			}
			if(currHeuristic >= currBestHeuristic && currCost < currLeastEnemyCost){
				currBestHeuristic = currHeuristic;
				currLeastEnemyCost = currCost;
				currBestVec = eachVec;
			}
		}
			p1 = currBestVec;
			currBestHeuristic = 0;
			currLeastEnemyCost = Integer.MAX_VALUE;
			currBestVec = null;
		tC.remove(currBestVec);
		for(Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> eachVec: tC){
			Pair marioStartLoc = null;
			int currHeuristic = 0;
			int currCost = 0;
			for(genPair<Pair,genPair<Action,HashMap<Pair,Node>>> each: eachVec){
				if(marioStartLoc == null){
					marioStartLoc = each.x;
					continue;
				}
				Pair marioCurrLoc = each.x;
				genPair<Action,HashMap<Pair,Node>> currChild = each.y;
				
				if(currChild.y.get(marioCurrLoc).enemyHere){
					++currCost;
				}

				if(currChild.y.get(marioCurrLoc).xPos > 0){
					++currHeuristic;
				}
			}
			if(currHeuristic >= currBestHeuristic && currCost < currLeastEnemyCost){
				currBestHeuristic = currHeuristic;
				currLeastEnemyCost = currCost;
				currBestVec = eachVec;
			}
		}
			p2 = currBestVec;
			currBestHeuristic = 0;
			currLeastEnemyCost = Integer.MAX_VALUE;
		
		//P1 and P2 populated. Begin Cross breeding

		if(p1.size() == p2.size()){
			for(int x =0 ; x < p1.size() ; ++x){
				genPair<Pair,genPair<Action,HashMap<Pair,Node>>> gP1 = p1.get(x);
				genPair<Pair,genPair<Action,HashMap<Pair,Node>>> gP2 = p2.get(x);
				Pair p1Pair = gP1.x;
				Pair p2Pair = gP2.x;
				Node Node1 = gP1.y.y.get(p1Pair);
				Node Node2 = gP2.y.y.get(p2Pair);
				//Calculate mutation chance. 50/50 chance to mutate.
				double chanceToMutate = Math.random()*2;
				chanceToMutate = Math.floor(chanceToMutate);
				int chance = (int)chanceToMutate;
				if(chance == 1){

					if(mario.mayShoot){
						ret.add(Action.RightSpeed);
					}
					if(mario.mayJump){
						ret.add(Action.RightLongJump);
					} else {
						ret.add(Action.Right);
					}
				}
				else if(!Node1.enemyHere && Node1.xPos >= 0){
					ret.add(gP1.y.x);
				} else if(!Node2.enemyHere && Node2.xPos > 0){
					ret.add(gP2.y.x);
				} else {
					ret.add(Action.Right);
				}
			}
		}
		return ret;
	}
	public Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> randomizeOptions(Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> vec){
		return vec;
	}
	boolean graph = false;
	GraphGenerator Graph = null;
	public MarioInput actionSelectionAI(){
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
			Graph.resetNodes(e,t, mario);
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
		frontierStates.push(Graph.State);
		solutionStates.add(Graph.State);
		Vector<Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>>> toChoose = new Vector<Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>>>();
		for(int x = 0; x < 8; ++x){
			boolean firstStateSeenCurrent = false;
			int currHeuristic = 0;
			Vector<Action> toDo = new Vector<Action>();
			Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> possiblePath = new Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>>();
			while(!frontierStates.isEmpty() && runs < 5){
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
				if(childStates.isEmpty()){
					break;
				}
				for(genPair<Pair,genPair<Action,HashMap<Pair,Node>>> ab: childStates) {
					double cost = 0;
					Pair marioLoc = ab.x;
					System.out.println(marioLoc.x+"---"+marioLoc.y);
				}
				

				genPair<Pair,genPair<Action,HashMap<Pair,Node>>> randAct = pickRandom(childStates);
				frontierStates.addFirst(randAct.y.y);
				possiblePath.add(randAct);

				++runs;
			}
			toChoose.add(possiblePath);
		}


		Vector<Action> tC = null;
		tC = pickBestVectorOfActions(toChoose);
		return doActions(tC);
	}

	public static void main(String[] args){
		String options = FastOpts.FAST_VISx2_02_JUMPING+FastOpts.L_DIFFICULTY(0)+FastOpts.L_ENEMY(Enemy.GOOMBA);
		MarioSimulator simulator = new MarioSimulator(options);
		IAgent agent = new EvolutionStrategy();
		simulator.run(agent);
		
		System.exit(0);
	}
}
