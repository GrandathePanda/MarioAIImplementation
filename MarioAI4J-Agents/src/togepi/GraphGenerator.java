package togepi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import ch.idsia.agents.controllers.modules.Entities;
import ch.idsia.agents.controllers.modules.Tiles;
import ch.idsia.benchmark.mario.engine.generalization.Tile;
import ch.idsia.benchmark.mario.engine.input.MarioKey;

/**
 * Core Class that contains the Graph and must be instantiated before actionSelectionAI() in your agent class.
 * The constructor takes two integers which correspond to the X and Y size of the grid you want to construct. Note, can be no larger than the perceptive grid, but can be smaller and non square e.g. 5x9.
 * Call generateGraph(Entities a, Tiles b, AgentType T) in actionSelectionAI() to generate the grid ON FIRST RUN
 */
public class GraphGenerator {
	private Entities e = null; /*! Contains Entities from current Perceptive Grid Sampling*/
	private Tiles t = null; /*! Contains Tiles from current Perceptive Grid Sampling*/
	public enum AgentType {
		ASTAR, HILLCLIMB;
	}
	/*! X Size of Grid After Instantiation */
	public int gridSizeX = 0; 
	/*! Y Size of Grid After Instantiation */
	public int gridSizeY = 0; 
	/*! Hashmap of <Pair,Node> Type containing current Perceptive State */
	public HashMap<Pair,Node> State = null; 
	/*! Collection of <Node> Type containing current Perceptive State. Created from the HashMap */
	public Collection<Node> List = null; 
	/*! Boolean set to true after generateGraph() has been called the first time.*/
	public boolean isGraphGenerated = false; 
	public GraphGenerator(int x, int y) {
		gridSizeX = x;
		gridSizeY = y;
				
	}
	public class Node {
		/**
		 * Node class containing all the information about this particular cell in the grid.
		 * Constructor Node(int x, int y, AgentType T, int sx, int sy)
		 */
		public boolean AStar = false;
		public boolean HillClimb = false;
		public int sizeX = 9;
		public int sizeY = 9;
		public boolean Other = false; 
		/*! Boolean true if enemy is in this cell. */
		public boolean enemyHere = false; 
		/*! Boolean true if block is in this cell. */
		public boolean blockHere = false; 
		/*! Boolean true if block is in this cell and the one above it. */
		public boolean doubleBlock = false; 
		/*! Boolean true if enemy is in a cell up to 3 X positions ahead. */
		public boolean enemyAhead = false; 
		/*! Boolean true if enemy is in a cell up to 3 Y positions below. */
		public boolean enemyBelow = false; 
		/*! This cells path cost. determined in the constructor */
		private int pathCost = 0; 
		/*! This cells heuristic cost. determined in the constructor*/
		private int heuristicCost = 0; 
		/*! This cells total cost. path+heuristic*/
		public int cost = 0; 
		/*! Boolean true if this node is a goal as determined by the generateGraph() call. */
		public boolean goal = false; 
		/*! Boolean true if this node has been looked at before. Up to you the coder to set this and check it in your algorithm. */
		public boolean seen = false; 
		/*! Boolean true if this node has been in the frontier before (Used for search algorithms). Up to you the coder to set this and check it in your algorithm.*/
		public boolean frontier = false; 
		/*! Integer This nodes X Position set in generateGraph()*/
		public int xPos = 0; 
		/*! Integer This nodes Y Position set in generateGraph()*/
		public int yPos = 0; 
		/*! Node Optional for solution-chains(Pathing/Search Algorithms) the node after it in the chain. Set by you the coder.*/
		public Node next = null; 
		/*! Node Optional for solution-chains(Pathing/Search Algorithms) the node before it in the chain. Set by you the coder.*/
		public Node prev = null; 
		/*! Vector Type Node containing all the children of this node in the graph. Set by generateGraph().*/
		public Vector<Node> children = new Vector<Node>(); 
		/**
		 * Function call updating the node to current state of perceptive grid. 
		 * Can be called singly by Node.reset() or also called by GraphGenerator.resetNodes(Entities a, Tiles t)
		 */
		public void reset() { 

			seen = false; 
			frontier = false;
			blockHere = t.brick(xPos,yPos);
			doubleBlock =  (t.brick(xPos+1, yPos) || t.brick(xPos+2, yPos)) && (t.brick(xPos+1, yPos) || t.brick(xPos+2, yPos));
			enemyHere = e.danger(xPos,yPos);	
			if(AStar == true) {
				heuristicCost = (int)Math.floor(Math.sqrt(Math.pow(sizeX-xPos,2)+Math.pow(0-yPos,2)));
				pathCost = 1;
				if(enemyHere) pathCost+=10;
				if(blockHere) pathCost+=5;
				if(t.brick(xPos,yPos-1) || e.danger(xPos,yPos-1)) pathCost+=15;
				if(t.brick(xPos+1, yPos)) pathCost+=6;
				 
			}
			cost = pathCost+heuristicCost;
		}
		/**
		 * Node Constructor. 
		 */
		public Node(int x, int y, AgentType T, int sx, int sy) {

			switch(T) {
				case HILLCLIMB:
					HillClimb = true;
				case ASTAR:
					AStar = true;
			}
			xPos = x;
			yPos = y;
			blockHere = t.brick(x,y);
			doubleBlock =  (t.brick(x+1, y) || t.brick(x+2, y)) && (t.brick(x+1, y-1) || t.brick(x+2, y-1));
			enemyHere = e.danger(x,y);
			if(AStar == true) {
				heuristicCost = (int)Math.floor(Math.sqrt(Math.pow(sizeX-x,2)+Math.pow(0-y,2)));	
				pathCost = 1;
				if(enemyHere) pathCost+=10;
				if(blockHere) pathCost+=5;
				if(t.brick(x,y-1) || e.danger(x,y-1)) pathCost+=15;
				if(t.brick(x+1, y)) pathCost+=6;
				
			}
			cost = pathCost+heuristicCost;
			
		}
	}
	
	/*!Pair class to use in the Graphs node indexing. You shouldn't have to touch this.*/
	public class Pair {
		public int x = 0;
		public int y = 0;
		
		@Override
		/** Again don't have to touch this but cool thing worth noting.
		*A pair of integers can form a bijection(one to one and onto, thus unique and useful for hashing) to a single integer ZxZ->Z
		*This is a modified cantor pairing function, that maps positive and negative integers into
		*a computationally less expensive set, contained in 32 bits for unsigned and 64 bit for signed.
		*Based on Matthew Szudzik Elegant Pairing Wolfram Research 2006
		*/
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
	/**
	 * Function call generate the graph. Takes 3 Arguments Current Entities, Current Tiles, and Agent-Type(Currently Only valid for A-Star).
	 * First creates all nodes in the given range (X by Y)
	 * Then links the nodes to their children/parents.
	 * Finally sets if a node is a goal state. Which currently is any node on the right edge of the graph that has no blocks or enemies.
	 */
	public void generateGraph(Entities a, Tiles b, AgentType T) {

		e = a;
		t = b;
		HashMap<Pair,Node> Graph = new HashMap<Pair,Node>();
		for( int i = -gridSizeX; i <= gridSizeX; i++ ) {
			for ( int j = gridSizeY; j>= -gridSizeY; j--) {
				Node currentNode = new Node(i,j,T,2*gridSizeX,2*gridSizeY);
				Graph.put(new Pair(i,j),currentNode);
			}
		}
		Collection<Node> listNodes = Graph.values();
		Iterator<Node> listIter = listNodes.iterator();
		while(listIter.hasNext()) {
			Node iterable = listIter.next();
			int y = iterable.yPos;
			int x = iterable.xPos;

			if((y-1<-gridSizeY) == false) {
				Node childUp = Graph.get(new Pair(iterable.xPos,iterable.yPos-1)); //Example of using a pair to check the HashMap
				iterable.children.add(childUp);
			}
			if((y+1>gridSizeY) == false) {
				Node childDown = Graph.get(new Pair(iterable.xPos,iterable.yPos+1));
				iterable.children.add(childDown);
			}
			if(((x+1)>gridSizeX) == false) {
				Node childForward = Graph.get(new Pair(iterable.xPos+1,iterable.yPos));
				iterable.children.add(childForward);
				if((y+1>gridSizeY) == false) {
					Node childUpForward = Graph.get(new Pair(iterable.xPos+1,iterable.yPos+1));
					iterable.children.add(childUpForward);
				}
				if((y-1<-gridSizeY) == false) {
					Node childDownForward = Graph.get(new Pair(iterable.xPos+1,iterable.yPos-1));
					iterable.children.add(childDownForward);
				}
			}
			else if(iterable.blockHere == false && iterable.enemyHere == false) iterable.goal = true;
			if((x-1<-gridSizeX) == false) {
				Node childBackward = Graph.get(new Pair(iterable.xPos-1,iterable.yPos));
				iterable.children.add(childBackward);
				if((y+1>gridSizeY) == false) {
					Node childUpForward = Graph.get(new Pair(iterable.xPos-1,iterable.yPos+1));
					iterable.children.add(childUpForward);
				}
				if((y-1<-gridSizeY) == false) {
					Node childDownForward = Graph.get(new Pair(iterable.xPos-1,iterable.yPos-1));
					iterable.children.add(childDownForward);
				}
			}	
		}

		isGraphGenerated = true;
		State =  Graph;
		List = State.values();
	}
	/**
	 * Function call loops through List of nodes and updates them to current state. 
	 */
	public void resetNodes(Entities a, Tiles b) {

		e = a;
		t = b;
		Iterator<Node> resetNodes = List.iterator(); //If it has been generated just update all the nodes. 
		while(resetNodes.hasNext()) {
			Node resetNode = resetNodes.next();
			resetNode.reset();
		}
	}

}
