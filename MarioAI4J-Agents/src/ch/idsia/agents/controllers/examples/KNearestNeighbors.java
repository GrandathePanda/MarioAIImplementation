package ch.idsia.agents.controllers.examples;

import java.awt.Graphics;
import java.util.*;

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

public class KNearestNeighbors extends MarioHijackAIBase implements IAgent {
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

	public class vectorNode {
		int x;
		int y;
		Action act;
	}
	
	public double distance(int x, int y, int otherX, int otherY){
		int curr = (otherX - x ) * (otherX - x) + (otherY - y)  * (otherY - y);
		return Math.sqrt(curr);
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
		double currGreatestHeuristic = 0.0;
		Node greatestNode = null;
		Action toDo = null;
		for(genPair<Pair,genPair<Action, HashMap<Pair,Node>>> curr: vec){
//			Pair marioPosChild = x.x;
			HashMap<Pair,Node> child = curr.y.y;
			Iterator it = child.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry pair = (Map.Entry)it.next();
				Node p = (Node)pair.getValue();
				double distance = distance(currentPos.xPos, currentPos.yPos, p.xPos, p.yPos);
				if(distance > currGreatestHeuristic && p.xPos > currentPos.xPos){
					greatestNode = p;
					currGreatestHeuristic = distance;
					toDo = curr.y.x;
				}
				it.remove();
			}

		}
		
//		for(int x = 0; x < vec.size(); ++x){
//			genPair<Pair, genPair<Action, HashMap<Pair,Node>>> curr = vec.get(x);
//			double distance = distance(currentPos.xPos, currentPos.yPos, curr.xPos, curr.yPos);
//			if(currGreatestHeuristic < distance && curr.xPos > currentPos.xPos){
//				greatestNode = curr;
//				currGreatestHeuristic = distance;
//			}
//		}
		genPair<Node, Action> ret = new genPair<Node,Action>(greatestNode, toDo);
		return ret;
	}
	

	
	boolean graph = false;
	GraphGenerator Graph = null;
	Action[] possibleActions = {Action.Jump,Action.LeftLongJump,Action.LeftShortJump,Action.RightLongJump,Action.RightShortJump,Action.Left,Action.Right,Action.LeftSpeed,Action.RightSpeed};
	public MarioInput actionSelectionAI(){

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
//		System.out.println(curr.size());

		Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> possibleMoves = null;
		possibleMoves = Graph.tickModel(states.get(0), possibleActions);
//		System.out.println("poss Moves size: " + possibleMoves.size());

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
		switch(toDo) {
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
		System.gc();
		return action;
	}
	public static void main(String[] args){
		String options = FastOpts.FAST_VISx2_02_JUMPING+FastOpts.L_DIFFICULTY(0)+FastOpts.L_ENEMY(Enemy.GOOMBA);
		MarioSimulator simulator = new MarioSimulator(options);
		IAgent agent = new KNearestNeighbors();
		simulator.run(agent);
		
		System.exit(0);
	}
}
