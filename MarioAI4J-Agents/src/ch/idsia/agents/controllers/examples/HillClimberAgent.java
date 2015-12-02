package ch.idsia.agents.controllers.examples;

import java.awt.Graphics;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

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
import togepi.GraphGenerator.*;
import togepi.GraphGenerator.Action;
import togepi.GraphGenerator.Node;
import togepi.genPair;
import togepi.Pair;

import static togepi.GraphGenerator.mapCopy;

public class HillClimberAgent extends MarioHijackAIBase implements IAgent{
	
	@Override
	public void reset(AgentOptions options){
		super.reset(options);
	}
	@Override
	public void debugDraw(VisualizationComponent vis, LevelScene level,	IEnvironment env, Graphics g) {
		super.debugDraw(vis, level, env, g);
		// provide custom visualization using 'g'
	}
	
	public Action pickAction(Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> states, Node marioPos){
		Action currPicked = Action.Right;
		int heuristicYolo = 0;
		Pair curr = null;
		System.out.println(states.size());
		int counter = 0;
		for(genPair<Pair,genPair<Action,HashMap<Pair,Node>>> each: states){
			System.out.println(counter++);
			int currHeuristic = 0;
			Pair marioPosChild = each.x;
			HashMap<Pair, Node> child = each.y.y;
			if(child.get(marioPosChild).enemyHere){
				--currHeuristic;
			}
			if(child.get(marioPosChild).blockHere){
				--currHeuristic;
			}
			System.out.println("child yPos: " + child.get(marioPosChild).yPos);
			System.out.println("marioPos: " + marioPos.yPos);
			if(child.get(marioPosChild).yPos > marioPos.yPos){
				System.out.println("here" + each.y.x);
				currHeuristic++;
			}
			if(currHeuristic > heuristicYolo){
				System.out.println("Greater");
				heuristicYolo = currHeuristic;
				currPicked = each.y.x;
			}
			System.out.println("current heuristic: " + currHeuristic);
		}
		return currPicked;
	}
	
	public void performAction(Action a){
		switch(a){
			case Jump:
				action.set(MarioKey.JUMP, mario.mayJump);
				System.out.println("Here j");
				break;
			case RightShortJump:
				System.out.println("HERE rsj");
				action.set(MarioKey.JUMP, mario.mayJump);
				action.press(MarioKey.RIGHT);
				break;
			case RightLongJump:
				System.out.println("Here lsj");
				action.set(MarioKey.JUMP, Graph.simMario.mayJump);
				break;
			case LeftShortJump:
				System.out.println("HERE lsj");
				action.set(MarioKey.JUMP, Graph.simMario.mayJump);
				break;
			case LeftLongJump:
				System.out.println("HERE llj");
				action.set(MarioKey.JUMP, Graph.simMario.mayJump);
				
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
//				action.press(MarioKey.LEFT);
				
				break;
			case LeftSpeed:
				System.out.println("HERE ls");
				action.press(MarioKey.LEFT);
				action.press(MarioKey.SPEED);
				break;
		}
		
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
		} else {
			Graph.resetNodes(e, t);
		}
		Vector<HashMap<Pair,Node>> solutions = new Vector<HashMap<Pair, GraphGenerator.Node>>();
		System.out.println(solutions.size());
		solutions.add(mapCopy(Graph.State));
		HashMap<Pair, Node> s = solutions.get(0);
		Iterator it = s.entrySet().iterator();
		Map.Entry<Pair, Node> pair = (Map.Entry)it.next();
		Node curr = pair.getValue();
		Pair currP = pair.getKey();
//		System.out.println("xPos: " + curr.xPos + " yPos: " + curr.yPos );
//		System.out.println("X: "  + currP.x + " Y: " + currP.y);
		Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> states = null;
		states = Graph.tick(s, possibleActions);
		System.out.println("States size: " + states.size());
//		System.out.println("state size:);
		Action a = pickAction(states, curr);
		performAction(a);
		//		System.out.println("States at 0 size: " + states.get(0).x.x);
		//		System.out.println(solutions.size());
		return action;
	}
	
	public static void main(String[] args){
		String options = FastOpts.FAST_VISx2_02_JUMPING+FastOpts.L_DIFFICULTY(0)+FastOpts.L_ENEMY(Enemy.GOOMBA);
		
		MarioSimulator simulator = new MarioSimulator(options);
		
		IAgent agent = new HillClimberAgent();
		
		simulator.run(agent);
		
		System.exit(0);
	}
}