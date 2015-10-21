//Better than BFS, still not great.
//Made It Better.

package ch.idsia.agents.controllers.examples;

import java.awt.Graphics;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Vector;
import java.util.Comparator;
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
public class AStarAgent extends MarioHijackAIBase implements IAgent {

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



	public void doActions(Vector<Node> SolutionSet) {
		Iterator<Node> solnIter = SolutionSet.iterator();
		while(solnIter.hasNext()) {
			Node cNode = solnIter.next();
			
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
			if(cNode.next != null) {
				action.set(MarioKey.JUMP, (cNode.next.enemyHere || cNode.enemyHere) && mario.mayJump);
				//System.out.println("(CNode X: "+cNode.xPos+", CNode Y: "+cNode.yPos+") Cost: "+cNode.cost+" CNode Block: "+ cNode.blockHere + " " +(cNode.next.yPos < cNode.yPos));
				action.set(MarioKey.JUMP,((cNode.next.yPos <= cNode.yPos) || cNode.next.blockHere  || cNode.blockHere) && mario.mayJump);
				if(cNode.prev != null) {
					action.set(MarioKey.JUMP,(cNode.prev.yPos > cNode.yPos) && mario.mayJump);
				}
				
		
			}
			else {
				action.set(MarioKey.JUMP, ((cNode.enemyHere || cNode.blockHere || (cNode.prev.yPos > cNode.yPos)) && mario.mayJump));
				if (!mario.onGround && (!cNode.prev.enemyHere || !cNode.enemyHere)) {
					action.press(MarioKey.JUMP);
				}
				else action.press(MarioKey.RIGHT);
				
			}
			


		}
	}
		//Make the graph a class level variable so it keeps its state. Only have to generate once.
		//Keep from here---------
		GraphGenerator Graph = new GraphGenerator(4,2,GraphGenerator.AgentType.ASTAR);
		
	public MarioInput actionSelectionAI() {
		if( Graph.isGraphGenerated == false ) { //If the graph hasn't been generated yet, generate it.
			Graph.generateGraph(e,t);
			Graph.isGraphGenerated = true;
		}
		else {
			Graph.resetNodes(e,t);
		}
		Vector<Node> Solution = new Vector<Node>();
		Node prevSolutionState = null;
		Node StartNode = Graph.State.get(Graph.new Pair(0,0));
		//--------- To here for what ever you want to do
			if(StartNode.goal == true) {
				Solution.add(StartNode);
				doActions(Solution);
				System.gc();
				return action;
			}
			else {
				PriorityQueue<Node> frontier = new PriorityQueue<Node>(Graph.gridSizeX*Graph.gridSizeY, new Comparator<Node>() {

					@Override
					public int compare(Node node1, Node node2) {
						return (node1.cost == node2.cost) ? ((node1.yPos < node2.yPos || node1.xPos < node2.xPos) ? 1 : -1)
								: (node1.cost > node2.cost ? 1 : -1);
						
					}
					
				});
				frontier.add(StartNode);
				while(frontier.isEmpty() == false) { //ID Implementation
					Node currentNode = frontier.remove();
					if(prevSolutionState == null) prevSolutionState = currentNode;
					else { prevSolutionState.next = currentNode; currentNode.prev = prevSolutionState; prevSolutionState = currentNode;}
					Solution.add(currentNode);
					currentNode.seen = true;
					if(currentNode.goal == true) {
						doActions(Solution);
						return action;
					}
					Iterator<Node> iter = currentNode.children.iterator();
					while(iter.hasNext()) {
						Node currentChild = iter.next();
						if(currentChild.frontier == false && currentChild.seen == false) {
								currentChild.frontier = true;
								frontier.add(currentChild);
							
						}
					}
				}
			}

		action.press(MarioKey.RIGHT);
		return action;
	}
	
	public static void main(String[] args) {
		String options = FastOpts.FAST_VISx2_02_JUMPING + FastOpts.L_ENEMY(Enemy.GOOMBA)/*+ FastOpts.L_RANDOMIZE*/;
		
		MarioSimulator simulator = new MarioSimulator(options);
		
		IAgent agent = new AStarAgent();
		
		simulator.run(agent);
		
		System.exit(0);
	}
}