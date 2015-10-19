//Sucks at this game.

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

/**
 * Your custom agent! Feel free to fool around!
 * 
 * @author Jakub 'Jimmy' Gemrot, gemrot@gamedev.cuni.cz
 */
public class BreadthFirstAgent extends MarioHijackAIBase implements IAgent {

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

	private boolean enemyAhead() {
		return
				   e.danger(1, 0) || e.danger(1, -1) 
				|| e.danger(2, 0) || e.danger(2, -1)
				|| e.danger(3, 0) || e.danger(3, -1);
	}
	
	private boolean brickAhead() {
		return
				   t.brick(1, 0) || t.brick(1, -1) 
				|| t.brick(2, 0) || t.brick(2, -1)
				|| t.brick(3, 0) || t.brick(3, -1);
	}
	

	private class Node {
		public boolean enemyHere = false;
		public boolean blockHere = false;
		public boolean goal = false;
		public boolean seen = false;
		public boolean frontier = false;
		public int xPos = 0;
		public int yPos = 0;
		public Vector<Node> children = new Vector<Node>();
		public void reset() {
			seen = false;
			frontier = false;
			blockHere = t.brick(xPos,yPos);
			enemyHere = e.danger(xPos,yPos);
		}
		public Node(int x, int y) {
			xPos = x;
			yPos = y;
			blockHere = t.brick(x,y);
			enemyHere = e.danger(x,y);
			
		}
	}
	private class Pair {
		public int x = 0;
		public int y = 0;
		
		@Override
		public int hashCode() {
			int A = x >= 0 ? 2 * x : -2 * x - 1;
			int B = y >= 0 ? 2 * y : -2 * y - 1;
			int code = A >= B ? A * A + A + B : A + B * B;
			return code;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Pair other = (Pair)obj;
			if(this.x == other.x && this.y == other.y)
				return true;
			return false;
		}
		public Pair(int a, int b) {
			x = a;
			y = b;
		}
		
		
	}
	public HashMap<Pair,Node> generateGraph() {
		int gridSize = 4; //Controls how big the simulated graph is. 
		HashMap<Pair,Node> Graph = new HashMap<Pair,Node>();
		for( int i = 0; i <= gridSize; i++ ) {
			for ( int j = 0; j>= -gridSize; j--) {
				Node currentNode = new Node(i,j);
				Graph.put(new Pair(i,j),currentNode);
			}
		}
		Collection<Node> listNodes = Graph.values();
		Iterator<Node> listIter = listNodes.iterator();
		while(listIter.hasNext()) {
			Node iterable = listIter.next();
			if((iterable.yPos-1<-gridSize) == false) {
				Node childUp = Graph.get(new Pair(iterable.xPos,iterable.yPos-1));
				iterable.children.add(childUp);
			}
			if((iterable.yPos+1>0) == false) {
				Node childDown = Graph.get(new Pair(iterable.xPos,iterable.yPos+1));
				iterable.children.add(childDown);
			}
			if(((iterable.xPos+1)>gridSize) == false) {
				Node childForward = Graph.get(new Pair(iterable.xPos+1,iterable.yPos));
				iterable.children.add(childForward);
			}
			else { iterable.goal = true;}
			if((iterable.xPos-1<0) == false) {
				Node childBackward = Graph.get(new Pair(iterable.xPos-1,iterable.yPos));
				iterable.children.add(childBackward);
			}	
		}

		
		return Graph;
	}
		public HashMap<Pair,Node> Graph = null;
		public Collection<Node> List = null;
		public boolean isGraphGenerated = false;
	public MarioInput actionSelectionAI() {
		if( isGraphGenerated == false ) {
			Graph = generateGraph();
			List = Graph.values();
			isGraphGenerated = true;
		}
		else {
			Iterator<Node> resetNodes = List.iterator();
			while(resetNodes.hasNext()) {
				Node resetNode = resetNodes.next();
				resetNode.reset();
			}
		}
		Node StartNode = Graph.get(new Pair(0,0));
		if(StartNode.goal == true) {
			action.press(MarioKey.RIGHT);
			System.gc();
			return action;
		}
		LinkedList<Node> frontier = new LinkedList<Node>();
		frontier.add(StartNode);
		while(frontier.isEmpty() == false) {
			Node currentNode = frontier.removeFirst();
			currentNode.seen = true;
			action.set(MarioKey.JUMP, (currentNode.enemyHere || currentNode.blockHere) && mario.mayJump);	
			if (!mario.onGround) {
				action.press(MarioKey.JUMP);
			}
			/*if(currentNode.enemyHere && mario.mayShoot) {
				action.press(MarioKey.SPEED);
				action.release(MarioKey.SPEED);
			}*/
			action.press(MarioKey.RIGHT);
			Iterator<Node> iter = currentNode.children.iterator();
			while(iter.hasNext()) {
				Node currentChild = iter.next();
				if(currentChild.frontier == false && currentChild.seen == false) {
					if(currentChild.goal == true) {
						action.set(MarioKey.JUMP, (currentChild.enemyHere || currentChild.blockHere) && mario.mayJump);	
						if (!mario.onGround) {
							action.press(MarioKey.JUMP);
						}
						/*if(currentNode.enemyHere && mario.mayShoot) {
							action.press(MarioKey.SPEED);
						}*/

						action.press(MarioKey.RIGHT);
					
					}
					else {

						currentChild.frontier = true;
						frontier.addLast(currentChild);
					}
				}
			}
		}
		

		System.gc();
		return action;
	}
	
	public static void main(String[] args) {
		String options = FastOpts.FAST_VISx2_02_JUMPING + FastOpts.L_ENEMY(Enemy.GOOMBA);
		
		MarioSimulator simulator = new MarioSimulator(options);
		
		IAgent agent = new BreadthFirstAgent();
		
		simulator.run(agent);
		
		System.exit(0);
	}
}