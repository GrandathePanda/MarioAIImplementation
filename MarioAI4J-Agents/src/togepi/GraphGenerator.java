package togepi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import ch.idsia.agents.controllers.modules.Entities;
import ch.idsia.agents.controllers.modules.Tiles;
import ch.idsia.benchmark.mario.engine.input.MarioKey;


public class GraphGenerator {
	private Entities e = null;
	private Tiles t = null;
	public boolean AStar = false;
	public boolean HillClimb = false;
	public enum AgentType {
		OTHER, ASTAR, HILLCLIMB;
	}
	public boolean Other = false;
	public int gridSizeX = 0;
	public int gridSizeY = 0;
	public HashMap<Pair,Node> State = null;
	public Collection<Node> List = null;
	public boolean isGraphGenerated = false;
	public GraphGenerator(int x, int y) {
		gridSizeX = x;
		gridSizeY = y;
	}
	public class Node {
		public boolean enemyHere = false;
		public boolean blockHere = false;
		public boolean enemyAhead = false;
		public boolean enemyBelow = false;
		private int pathCost = 0;
		private int heuristicCost = 0;
		public int cost = 0;
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
	public class Pair {
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
	public void generateGraph(Entities a, Tiles b) {
		e = a;
		t = b;
		HashMap<Pair,Node> Graph = new HashMap<Pair,Node>();
		//Generate all Nodes in the selected size range.
		for( int i = 0; i <= gridSizeX; i++ ) {
			for ( int j = gridSizeY; j>= -gridSizeY; j--) {
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
			else { if(iterable.enemyHere == false && iterable.blockHere == false) {iterable.goal = true;}}
			if((x-1<0) == false) {
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
