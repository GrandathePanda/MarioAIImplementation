//Better than BFS, still not great.

package ch.idsia.agents.controllers.examples;

import java.awt.Graphics;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;
import java.util.ListIterator;


import ch.idsia.agents.AgentOptions;
import ch.idsia.agents.IAgent;
import ch.idsia.agents.controllers.MarioHijackAIBase;
import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.engine.LevelScene;
import ch.idsia.benchmark.mario.engine.VisualizationComponent;
import ch.idsia.benchmark.mario.engine.generalization.Enemy;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.engine.input.MarioKey;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.IEnvironment;
import ch.idsia.benchmark.mario.options.FastOpts;
import togepi.GraphGenerator.Node;

/**
 * Your custom agent! Feel free to fool around!
 * 
 * @author Jakub 'Jimmy' Gemrot, gemrot@gamedev.cuni.cz
 */
public class HillClimberAgentTrue extends MarioHijackAIBase implements IAgent {

	
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
				|| e.danger(3, 0) || e.danger(2, -1);
	}
	
	private boolean brickAhead() {
		return
				   t.brick(1, 0) || t.brick(1, -1) 
				|| t.brick(2, 0) || t.brick(2, -1)
				|| t.brick(3, 0) || t.brick(3, -1);
	}
	
	//Node class, Shouldn't have to touch this. 
	private class Node {
		public boolean enemyHere = false;
		public boolean blockHere = false;
		public boolean goal = false;
		public boolean seen = false; //prevents cycles in the searching algorithms
		public boolean frontier = false; // also prevents cycles if for some reason the first fails.
		public int xPos = 0;
		public int yPos = 0;
		public Vector<Node> children = new Vector<Node>();
		public void reset() { // update the node to the current values
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
	
	//Pair class to use in the Graphs node indexing. You shouldn't have to touch this.
	private class Pair {
		public int x = 0;
		public int y = 0;
		
		@Override
		public int hashCode() { // Again don't have to touch this but cool thing worth noting.
			// A pair of integers can form a bijection(one to one and onto, thus unique and useful for hashing) to a single integer ZxZ->Z
			// This is a modified cantor pairing function, that maps positive and negative integers into
			// a computationally less expensive set, contained in 32 bits for unsigned and 64 bit for signed.
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
		int gridSize = 4; //Controls how big the simulated graph is. Feel free to tweak.
		HashMap<Pair,Node> Graph = new HashMap<Pair,Node>();
		//Generate all Nodes in the selected size range.
		for( int i = 0; i <= gridSize; i++ ) {
			for ( int j = 0; j>= -gridSize; j--) {
				Node currentNode = new Node(i,j);
				Graph.put(new Pair(i,j),currentNode);
			}
		}
		/*Create a list from the graph of all nodes in the graph in no particular order.
		 * Iterate through the list. At any given node all we have to do is test for the boundaries.
		 * If a node is a boundary point, don't generate children outside of the grid. If the node
		 * is not a boundary generate all children nodes in the up down forward and back direction.
		 * This forms the edges of the graph, every node has at most four children. 
		 * This finishes creating our undirected graph.
		 */
		Collection<Node> listNodes = Graph.values();
		Iterator<Node> listIter = listNodes.iterator();
		while(listIter.hasNext()) {
			Node iterable = listIter.next();
			if((iterable.yPos-1<-gridSize) == false) {
				Node childUp = Graph.get(new Pair(iterable.xPos,iterable.yPos-1)); //Example of using a pair to check the HashMap
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
	public boolean shooting = false;
	public void doActions(Node cNode) {
//		if(brickAhead()){
		if(brickAhead()){
			System.out.println("trying to jump");
			action.set(MarioKey.JUMP, mario.mayJump);	
			if (!mario.onGround && brickAhead()) {
				action.press(MarioKey.JUMP);
			}
		}
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
		public HashMap<Pair,Node> Graph = null;
		public Collection<Node> List = null;
		public boolean isGraphGenerated = false;
		public int counter = 0;
	public MarioInput actionSelectionAI() {
		if( isGraphGenerated == false ) { //If the graph hasn't been generated yet, generate it.
			Graph = generateGraph();
			List = Graph.values();
			isGraphGenerated = true;
		}
		else {
			Iterator<Node> resetNodes = List.iterator(); //If it has been generated just update all the nodes. 
			while(resetNodes.hasNext()) {
				Node resetNode = resetNodes.next();
				resetNode.reset();
			}
		}
		Node StartNode = Graph.get(new Pair(0,0));	
		if(StartNode.goal == true) {
//			doActions(StartNode);
			System.gc();
			return action;
		} else {
		//--------- To here for what ever you want to do
		action.release(MarioKey.SPEED);
		LinkedList<Node> frontier = new LinkedList<Node>();
		Vector<Node> frontier2 = new Vector<Node>();
		frontier.add(StartNode);		
		frontier2.add(StartNode);
		Node bestNode = StartNode;
		while(!frontier.isEmpty()){ //Hill Climbing
			Node curr = frontier.removeFirst();
//			doActions(curr);
			curr.seen = true;
			int currMin = 100;
			Iterator<Node> iter = curr.children.iterator();
			while(iter.hasNext()) {
				int currHVal = 0;
				Node tcurr = iter.next();
				frontier2.add(curr);
				if(curr.frontier == false && curr.seen == false) {
						curr.frontier = true;
					
				}
				if(tcurr.enemyHere){
				    ++currHVal;
				    ++currHVal;
//					System.out.println("here!");
				}
				if(tcurr.blockHere){
//					System.out.println("Aye here");
					++currHVal;
				}
//				System.out.println(currHVal);
				if(currHVal < currMin){
					bestNode = tcurr;
					currMin = currHVal;
				}
			}
			
			
		}
//		System.out.println("bestNode: ");
//		doActions(StartNode);
		doActions(bestNode);
		

		//Your code Here 
		
		}
		System.gc(); //Suggest garbage clean-up after work here.
		return action;
	}
	
	public static void main(String[] args) {
		String options = FastOpts.FAST_VISx2_02_JUMPING + FastOpts.L_ENEMY(Enemy.GOOMBA);
//		String options = FastOpts.FAST_VISx2_02_JUMPING;
		MarioSimulator simulator = new MarioSimulator(options);
		
		IAgent agent = new HillClimberAgentTrue();
		
		simulator.run(agent);
		
		System.exit(0);
	}
}