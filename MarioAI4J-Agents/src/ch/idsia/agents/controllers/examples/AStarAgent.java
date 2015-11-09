//Better than BFS, still not great.
//Made It Better.

package ch.idsia.agents.controllers.examples;

import java.awt.Graphics;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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

	private boolean enemyAhead(Node n) {
		return
				   e.danger(n.xPos, 0) || e.danger(n.xPos, -1) || e.danger(n.xPos, 1)
				|| e.danger(n.xPos+1, 0) || e.danger(n.xPos+1, -1) || e.danger(n.xPos+1, 1)	
				|| e.danger(n.xPos+2, 0) || e.danger(n.xPos+2, -1) || e.danger(n.xPos+2, 1);
	}
	private boolean brickUp(Node n) {
		return
				 t.brick(n.xPos+1, -2) || t.brick(n.xPos+2, -2);
	}
	private int actionCount = 0;
	public void doActions(Vector<Node> SolutionSet) throws IOException {
		actionCount++;
		Writer logWrite = null;
		logWrite = new BufferedWriter(new OutputStreamWriter(
	              new FileOutputStream("/home/granda/Dropbox/School/Junior Year/First Semester/AI/Semester_Project/MarioAI4J-Agents/src/ch/idsia/agents/controllers/examples/actionLog"), "utf-8"));
		Iterator<Node> solnIter = SolutionSet.iterator();
		boolean speed = false;
		while(solnIter.hasNext()) {
			Node cNode = solnIter.next();
			Node nNode = cNode.next;
			//System.out.println(cNode.xPos +" "+cNode.yPos+" "+cNode.cost);
			if(nNode == null) {
				action.press(MarioKey.RIGHT);
				action.press(MarioKey.SPEED);
				logWrite.write("Action Sequence: "+actionCount+" Action: pNode Null Right Speed");

				speed = true;
				if(cNode.enemyHere) {
					action.release(MarioKey.SPEED);
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
					
				}
				continue;
				
			}
			if(cNode.enemyHere) {
				action.release(MarioKey.SPEED);
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
				
			}
			if(nNode.yPos < cNode.yPos || enemyAhead(cNode)) {
					logWrite.write("Action Sequence: "+actionCount+" Action: pNodeY > cNodeY Jump");
					action.set(MarioKey.JUMP, mario.mayJump);
					boolean highJump = false;
					if(cNode.prev != null) highJump = cNode.prev.yPos >= cNode.yPos;
					if (!mario.onGround && (highJump && ( brickUp(cNode) || enemyAhead(nNode)))) {
						logWrite.write("Action Sequence: "+actionCount+" Action: pNodeY > cNodeY Mid Air No Enemy Ahead pNode Continue Jump");
						action.press(MarioKey.JUMP);
						action.release(MarioKey.SPEED);
						return;
						
					}
					if(nNode.xPos > cNode.xPos) {
						logWrite.write("Action Sequence: "+actionCount+" Action: pNodeY > cNodeY pNodeX < cNodeX Right, Speed");
						action.press(MarioKey.RIGHT);
						//action.press(MarioKey.SPEED);
						speed = true;
					}
					else {
						if(nNode.xPos > cNode.xPos) {
							logWrite.write("Action Sequence: "+actionCount+" Action: pNodeY > cNodeY pNodeX > cNodeX Release-Right, Left, Release-Left");
							action.release(MarioKey.RIGHT);
							action.press(MarioKey.LEFT);
							action.release(MarioKey.LEFT);
							//continue;
							//action.release(MarioKey.LEFT);
						}
						else action.press(MarioKey.RIGHT);
						speed = false;
					}
				
			}
			else {
				
				if(nNode.xPos > cNode.xPos && mario.onGround == true) {	
					logWrite.write("Action Sequence: "+actionCount+" Action: pNodeY <= cNodeY pNodeX < cNodeX Right Speed");
						action.press(MarioKey.RIGHT);
						//action.press(MarioKey.SPEED);
						speed = true;
				}
				else {
					if(nNode.xPos < cNode.xPos) {
						logWrite.write("Action Sequence: "+actionCount+" Action: pNodeY <= cNodeY pNodeX > cNodeX Release-Right Left Release-Left");
						action.release(MarioKey.RIGHT);
						//action.release(MarioKey.SPEED);
						action.press(MarioKey.LEFT);
						action.release(MarioKey.LEFT);
						speed = false;
						//continue;
					}
					else action.press(MarioKey.RIGHT);

				}
			}
			
			
			


		}
		logWrite.close();
	}
		//Make the graph a class level variable so it keeps its state. Only have to generate once.
		//Keep from here---------
		GraphGenerator Graph = new GraphGenerator(9,9);
		
	public MarioInput actionSelectionAI() {
		if( Graph.isGraphGenerated == false ) { //If the graph hasn't been generated yet, generate it.
			Graph.generateGraph(e,t,GraphGenerator.AgentType.ASTAR);
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
				try {
					doActions(Solution);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.gc();
				return action;
			}
			else {
				PriorityQueue<Node> frontier = new PriorityQueue<Node>(Graph.gridSizeX*(Graph.gridSizeY+2), new Comparator<Node>() {

					@Override
					public int compare(Node node1, Node node2) {
						return (node1.cost == node2.cost) ? ((node1.yPos < node2.yPos || node1.xPos > node2.xPos) ? 1 : -1)
								: (node1.cost < node2.cost ? 1 : -1);
						
					}
					
				});
				StartNode.frontier = true;
				frontier.add(StartNode);
				while(frontier.isEmpty() == false) { //ID Implementation
					Node currentNode = frontier.remove();
					if(prevSolutionState == null) prevSolutionState = currentNode;
					else { prevSolutionState.next = currentNode; currentNode.prev = prevSolutionState; prevSolutionState = currentNode;}
					Solution.add(currentNode);
					if(currentNode.goal == true) {
						System.out.println("GOAL!");
						try {
							doActions(Solution);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.gc();
						return action;
					}
					Iterator<Node> iter = currentNode.children.iterator();
					Node tentativeFrontier = null;
					int tentativeCost = 0;
					while(iter.hasNext()) {
						Node currentChild = iter.next();
						if(currentChild.frontier == false) {
							if(tentativeFrontier == null) {
								tentativeFrontier = currentChild;
								tentativeCost = currentChild.cost;
							}
							else {
								if(tentativeCost > currentChild.cost) {
									tentativeFrontier = currentChild;
									tentativeCost = currentChild.cost;
								}
							}
						}
					}
					//if(tentativeFrontier == null) continue;
					tentativeFrontier.frontier = true;
					frontier.add(tentativeFrontier);
				}
			}

		return action;
	}
	
	public static void main(String[] args) {
		String options = FastOpts.FAST_VISx2_02_JUMPING+FastOpts.L_DIFFICULTY(0)+FastOpts.L_ENEMY(Enemy.GOOMBA,Enemy.RED_KOOPA, Enemy.GOOMBA_WINGED);
		
		MarioSimulator simulator = new MarioSimulator(options);
		
		IAgent agent = new AStarAgent();
		
		simulator.run(agent);
		
		System.exit(0);
	}
}