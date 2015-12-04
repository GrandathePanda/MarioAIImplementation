package ch.idsia.agents.controllers.examples;

import java.awt.Graphics;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

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

	boolean graph = false;
	GraphGenerator Graph = null;
	private final Action[] allPossibleActions = {Action.Jump,Action.LeftLongJump,Action.RightLongJump,Action.Left,Action.Right,Action.LeftSpeed,Action.RightSpeed};
	private final Action[] airPossibleActions = {Action.LeftLongJump,Action.RightLongJump,Action.Left,Action.Right};
	private final Action[] groundPossibleActions = {Action.Jump,Action.Left,Action.Right,Action.LeftSpeed,Action.RightSpeed};
	private final Action[] airHangPossibleActions = {Action.Left,Action.Right};
	boolean g1 = false; //deal with air drop in the beggining
	public MarioInput actionSelectionAI(){
		
		if(!graph){
			Graph = new GraphGenerator(9,9,mario);
			Graph.generateGraph(e, t);
			Graph.isGraphGenerated = true;
			graph = true;
		} else {
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
		Vector<HashMap<Pair,Node>> solutions = new Vector<HashMap<Pair, GraphGenerator.Node>>();
		solutions.add(mapCopy(Graph.State));
		HashMap<Pair, Node> s = solutions.get(0);
		Iterator it = s.entrySet().iterator();
		Map.Entry<Pair, Node> pair = (Map.Entry)it.next();
		Node curr = pair.getValue();
		Pair currP = pair.getKey();
		Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> states = null;
		states = Graph.tick(s, modPosAction);
		Action a = pickAction(states, curr);
		Vector<Action> vec = new Vector<Action>();
		vec.add(a);
		return Graph.doActions(vec);
	}
	
	public static void main(String[] args){
		String options = FastOpts.FAST_VISx2_02_JUMPING+FastOpts.L_DIFFICULTY(0)+FastOpts.L_ENEMY(Enemy.GOOMBA);
		
		MarioSimulator simulator = new MarioSimulator(options);
		
		IAgent agent = new HillClimberAgent();
		
		simulator.run(agent);
		
		System.exit(0);
	}
}