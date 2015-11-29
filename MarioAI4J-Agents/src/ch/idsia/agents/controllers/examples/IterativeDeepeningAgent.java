//Better than BFS, still not great.
//Made It Better.

package ch.idsia.agents.controllers.examples;

import java.awt.Graphics;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
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

/**
 * Your custom agent! Feel free to fool around!
 * 
 * @author Jakub 'Jimmy' Gemrot, gemrot@gamedev.cuni.cz
 */
/*public class IterativeDeepeningAgent extends MarioHijackAIBase implements IAgent {

	public boolean shooting = false;
	@Override
	public void reset(AgentOptions options) {
		super.reset(options);
	}
	
	@Override
	public void debugDraw(VisualizationComponent vis, LevelScene level,	IEnvironment env, Graphics g) {
		super.debugDraw(vis, level, env, g);
		// provide custom visualization using 'g'
	}

	private boolean brickAhead() {
		return
				   t.brick(1, 0) || t.brick(1, -1) 
				|| t.brick(2, 0) || t.brick(2, -1)
				|| t.brick(3, 0) || t.brick(3, -1);
	}

	public void doActions(Node cNode) {
		if (mario.mayShoot) {
			if (shooting) {
				shooting = false;
				action.release(MarioKey.SPEED);
			} else 
			if (action.isPressed(MarioKey.SPEED)) {				
				action.release(MarioKey.SPEED);
			} else {
				shooting = true;
				action.press(MarioKey.SPEED);
			}
		} else {
			if (shooting) {
				shooting = false;
				action.release(MarioKey.SPEED);
			}
		}
		if(cNode.enemyHere || (cNode.xPos > 1 && cNode.yPos <= 0 && cNode.blockHere)) {
			action.set(MarioKey.JUMP, mario.mayJump);	
			if (!mario.onGround && brickAhead()) {
				action.press(MarioKey.JUMP);
			}
		}
		action.press(MarioKey.RIGHT);
	}
		//Make the graph a class level variable so it keeps its state. Only have to generate once.
		//Keep from here---------
		GraphGenerator Graph = new GraphGenerator(4,4);
		public int limit = 4 ; // must be less then or equal to gridSize or null exceptions will occur.
		
	public MarioInput actionSelectionAI() {
		if( Graph.isGraphGenerated == false ) { //If the graph hasn't been generated yet, generate it.
			Graph.generateGraph(e,t,GraphGenerator.AgentType.HILLCLIMB);

			Graph.isGraphGenerated = true;
		}
		else {
			Graph.resetNodes(e,t);
		}
		Node StartNode = Graph.State.get(Graph.new Pair(0,0));
		//--------- To here for what ever you want to do
		for(int i = 0; i < limit; ) {
			if(StartNode.goal == true) {
				doActions(StartNode);
				System.gc();
				return action;
			}
			else if(StartNode.xPos >= i || StartNode.yPos >= i) {
				i++;
				System.out.println(i);
				Graph.resetNodes(e,t);
				continue;
			}
			else {
				LinkedList<Node> frontier = new LinkedList<Node>();
				frontier.add(StartNode);
				while(frontier.isEmpty() == false) { //ID Implementation
					Node currentNode = frontier.removeLast();
					currentNode.seen = true;
					doActions(currentNode);
					
		
					action.press(MarioKey.RIGHT);
					Iterator<Node> iter = currentNode.children.iterator();
					boolean limitBool = false;
					while(iter.hasNext()) {
						Node currentChild = iter.next();
						if(currentChild.frontier == false && currentChild.seen == false) {
							if(currentChild.goal == true) {
								doActions(currentChild);
								return action;
							
							}
							else {
								if(currentChild.xPos >= i || currentChild.yPos >= i) {
									

									doActions(currentChild);
									
									break;
									
								}
								
								currentChild.frontier = true;
								frontier.addLast(currentChild);
							}
						}
					}
					if(limitBool == true )	
						break;
				}
			}
			i++;
			System.out.println(i);
			Graph.resetNodes(e,t);
			continue;
		}

		action.press(MarioKey.RIGHT);
		return action;
	}
	
	public static void main(String[] args) {
		String options = FastOpts.FAST_VISx2_02_JUMPING + FastOpts.L_ENEMY(Enemy.GOOMBA)/*+ FastOpts.L_RANDOMIZE*/;
/*
		MarioSimulator simulator = new MarioSimulator(options);
		
		IAgent agent = new IterativeDeepeningAgent();
		
		simulator.run(agent);
		
		System.exit(0);
	}
}

*/