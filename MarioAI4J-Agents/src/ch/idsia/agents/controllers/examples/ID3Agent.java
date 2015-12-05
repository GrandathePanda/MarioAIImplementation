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
import ch.idsia.benchmark.mario.environments.MarioEnvironment;
import ch.idsia.benchmark.mario.options.FastOpts;
import togepi.GraphGenerator;
import togepi.GraphGenerator.Action;
import togepi.GraphGenerator.Node;
import togepi.Id3Node;
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
public class ID3Agent extends MarioHijackAIBase implements IAgent {
	public ID3Agent(Vector<Id3Node> Tree) {
		ThisTree = Tree;
	}
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

	public MarioInput doActions(Vector<Action> ac) {
		MarioInput doThese = new MarioInput();
		for(Action x : ac) {
			switch (x) {
				case Jump:
					if(mario.mayJump && mario.onGround) {
						doThese.press(MarioKey.JUMP);
					}
						//doThese.press(MarioKey.JUMP);
					break;
				case RightLongJump:
					if(!mario.onGround) {
						doThese.press(MarioKey.JUMP);
					}
					doThese.release(MarioKey.LEFT);
					doThese.press(MarioKey.RIGHT);
				case LeftLongJump:
					doThese.press(MarioKey.JUMP);
					doThese.release(MarioKey.RIGHT);
					doThese.press(MarioKey.LEFT);
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



	boolean graph = false;
	GraphGenerator Graph = null;
	Vector<Id3Node> ThisTree = null;
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
			if(!simM.onGround)
				modPosAction = airPossibleActions;
			else modPosAction = airHangPossibleActions;
		}
		int runs = 0;
		boolean firstStateSeenCurrent = false;
		frontierStates.push(mapCopy(Graph.State));
		solutionStates.add(mapCopy(Graph.State));
		Vector<Action> actionsDo = new Vector<>();

			HashMap<Pair,Node> currentState = frontierStates.removeFirst();
			for(Map.Entry<Pair,Node> x : currentState.entrySet()) {
				if(x.getValue().mario) simM = x.getValue().alterMario;
			}
			solutionStates.add(currentState);
			seenStates.add(currentState);
			Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> childStates = null;
			System.out.println("HERE Tick");
			childStates = Graph.tick(currentState, modPosAction);
			firstStateSeenCurrent = true;

			if(mario.onGround && !g1) g1 = true;
			if(!g1) return action;
			for(genPair<Pair,genPair<Action,HashMap<Pair,Node>>> x : childStates) {
				Pair marioPosChild = x.x;
				HashMap<Pair,Node> child = x.y.y;
				Node Center = child.get(new Pair(0,0));
				Node CenterRight = child.get(new Pair(1,0));
				Id3Node Root = null;
				Action thisOne = null;
				for(Id3Node r : ThisTree) {
					if(r.root) Root = r;
				}
				Id3Node curr = Root;

				int count = 0;
				while(actionsDo.isEmpty()) {
					switch (curr.atrib) {
						case Enemy:
							if(CenterRight.enemyHere || Center.enemyHere) {
								actionsDo.add(Action.Jump);
							}
							else {
								curr = curr.no;
							}
							break;
						case Block:
							if (CenterRight.blockHere && !CenterRight.doubleBlock) {
								if(mario.onGround) {
									actionsDo.add(Action.Jump);
								}
								else {
									actionsDo.add(Action.RightLongJump);
								}
								break;
							}
							else {
								curr = curr.no;
							}
							break;
						case DoubleBlock:
							if (CenterRight.doubleBlock || Center.doubleBlock) {

								//actionsDo.add(Action.Jump);
								actionsDo.add(Action.RightLongJump);
							}
							else {
								curr = curr.no;
							}
							break;

						case None:
							actionsDo.add(Action.RightSpeed);
							break;
						default:
							actionsDo.add(Action.Right);
					}


				}


				//actionsDo.add(Action.RightSpeed);

				if( thisOne != null) actionsDo.add(thisOne);
				break;
			}
			++runs;

		
		System.out.println("REturning");
		System.gc();
		return doActions(actionsDo);
	}


}