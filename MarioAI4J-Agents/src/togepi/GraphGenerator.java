package togepi;

import java.util.*;

import ch.idsia.agents.controllers.modules.Entities;
import ch.idsia.agents.controllers.modules.Tiles;
import ch.idsia.benchmark.mario.engine.generalization.EntityType;
import ch.idsia.benchmark.mario.engine.generalization.MarioEntity;
import ch.idsia.benchmark.mario.engine.generalization.Tile;
import ch.idsia.benchmark.mario.engine.input.MarioKey;
import ch.idsia.tasks.MarioSystemOfValues;
import ch.idsia.benchmark.mario.engine.LevelScene;


/**
 * Core Class that contains the Graph and must be instantiated before actionSelectionAI() in your agent class.
 * The constructor takes two integers which correspond to the X and Y size of the grid you want to construct. Note, can be no larger than the perceptive grid, but can be smaller and non square e.g. 5x9.
 * Call generateGraph(Entities a, Tiles b, AgentType T) in actionSelectionAI() to generate the grid ON FIRST RUN
 */
public class GraphGenerator {
	public class entityDoll {
			public float xS = 0;
			public float yS = 0;
			public float dX = 0;
			public float dY = 0;
			public int x = 0;
			public int y = 0;
			public float xJS = 0;
			public float yJS = 0;
			public float xJA = 0;
			public float yJA = 0;
			public int strikes = 0;
			public int xF = 0;
			public int yF = 0;
			public int jumpTime = 0;
			public boolean onGround = false;
			public boolean mayJump = false;

		public entityDoll(float xSpeed, float ySpeed, float changeX, float changeY, int marioSize, int xPos, int yPos, boolean marioOnGround, boolean marioMayJump) {
			xS = xSpeed;
			yS = ySpeed;
			dX = changeX;
			dY = changeY;
			strikes = marioSize;
			x = xPos;
			y = yPos;
			onGround = marioOnGround;
			mayJump = marioMayJump;
		}
		public entityDoll() {

		}
	}
	public enum Action {
		Jump,
		RightShortJump,
		RightLongJump,
		LeftShortJump,
		LeftLongJump,
		Right,
		RightSpeed,
		Left,
		LeftSpeed;
	}
	private entityDoll simMario = null;
	MarioEntity mario = null;
	private Entities e = null; /*! Contains Entities from current Perceptive Grid Sampling*/
	private Tiles t = null; /*! Contains Tiles from current Perceptive Grid Sampling*/
	/*! X Size of Grid After Instantiation */
	public int gridSizeX = 0; 
	/*! Y Size of Grid After Instantiation */
	public int gridSizeY = 0; 
	/*! Hashmap of <Pair,Node> Type containing current Perceptive State */
	public HashMap<Pair,Node> State = null; 
	/*! Collection of <Node> Type containing current viewable field. Created from the HashMap */
	public Collection<Node> List = null; 
	/*! Boolean set to true after generateGraph() has been called the first time.*/
	public boolean isGraphGenerated = false; 
	public GraphGenerator(int x, int y, MarioEntity marioClone) {
		gridSizeX = x;
		gridSizeY = y;
		mario = marioClone;
		simMario = new entityDoll(mario.speed.x, mario.speed.y,mario.dX,mario.dY,((mario.mode.getCode()-2)*-1),9,9,mario.mayJump,mario.onGround);
	}
	public static <key> HashMap<key,Node> mapCopy(final HashMap<key,Node> hashMap) {
		HashMap<key,Node> copyMap = new HashMap<key,Node>();
		for(Map.Entry<key,Node> e : hashMap.entrySet()) {
			copyMap.put(e.getKey(),e.getValue().clone());
		}

		return copyMap;
	}
	public class Node {
		/**
		 * Node class containing all the information about this particular cell in the grid.
		 * Constructor Node(int x, int y, AgentType T, int sx, int sy)
		 */

		public int sizeX = 9;
		public int sizeY = 9;
		public boolean mario = false;
		public boolean Other = false; 
		/*! Boolean true if enemy is in this cell. */
		public boolean enemyHere = false; 
		/*! Boolean true if block is in this cell. */
		public boolean blockHere = false; 
		/*! Boolean true if block is in this cell and the one above it. */
		public boolean doubleBlock = false;
		/*! Integer This nodes X Position set in generateGraph()*/
		public int xPos = 0; 
		/*! Integer This nodes Y Position set in generateGraph()*/
		public int yPos = 0; 
		/*! Node Optional for solution-chains(Pathing/Search Algorithms) the node after it in the chain. Set by you the coder.*/
		public Node next = null; 
		/*! Node Optional for solution-chains(Pathing/Search Algorithms) the node before it in the chain. Set by you the coder.*/
		public Node prev = null;
		public EntityType EnemyType = null;
		/*! Vector Type Node containing all the children of this node in the graph. Set by generateGraph().*/
		public Vector<Node> children = new Vector<Node>(); 
		/**
		 * Function call updating the node to current state of perceptive grid. 
		 * Can be called singly by Node.reset() or also called by GraphGenerator.resetNodes(Entities a, Tiles t)
		 */
		public void reset() {
			blockHere = t.brick(xPos,yPos);
			doubleBlock =  (t.brick(xPos, yPos) && t.brick(xPos+2, yPos-1));
			enemyHere = e.danger(xPos,yPos);
		}
		/**
		 * Node Constructor. 
		 */
		public Node(int x, int y, int sX, int sY) {
			sizeX = sX;
			sizeY = sY;
			xPos = x;
			yPos = y;
			blockHere = t.brick(x,y);
			doubleBlock =  (t.brick(x+1, y) && t.brick(x, y-1));
			if(enemyHere = e.danger(x,y)){
				EnemyType = e.entityType(x,y);
			}

		}
		public Node(int x, int y) {
			xPos = x;
			yPos = y;
		}
		public Node clone() {
			Node copy = new Node(this.xPos,this.yPos,this.sizeX,this.sizeY);
			copy.blockHere = this.blockHere;
			copy.doubleBlock = this.doubleBlock;
			copy.enemyHere = this.enemyHere;
			copy.mario = this.mario;
			copy.EnemyType = this.EnemyType;
			return copy;
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
	/*!Generic Pair class to use with everything including integers, key difference that made the need to write this,
	 *couldn't generify the other pair class without generifying hashCode().
	 *
	 */

	public class genPair<val1,val2>{
		public val1 x = null;
		public val2 y = null;

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			genPair other = (genPair)obj;
			if(this.x == other.x && this.y == other.y)
				return true;
			return false;
		}
		public genPair(val1 a, val2 b) {
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
	public void generateGraph(Entities a, Tiles b) {

		e = a;
		t = b;
		HashMap<Pair,Node> Graph = new HashMap<Pair,Node>();
		for( int i = -gridSizeX; i <= gridSizeX; i++ ) {
			for ( int j = gridSizeY; j>= -gridSizeY; j--) {
				Node currentNode = new Node(i,j,2*gridSizeX,2*gridSizeY);
				Graph.put(new Pair(i,j),currentNode);
			}
		}
		Collection<Node> listNodes = Graph.values();
		Iterator<Node> listIter = listNodes.iterator();
		while(listIter.hasNext()) {
			Node iterable = listIter.next();
			int y = iterable.yPos;
			int x = iterable.xPos;

			if(!((y - 1)<-gridSizeY)) {
				Node childUp = Graph.get(new Pair(iterable.xPos,iterable.yPos-1)); //Example of using a pair to check the HashMap
				iterable.children.add(childUp);
			}
			if(!((y + 1)>gridSizeY)) {
				Node childDown = Graph.get(new Pair(iterable.xPos,iterable.yPos+1));
				iterable.children.add(childDown);
			}
			if(!((x + 1)>gridSizeX)) {
				Node childForward = Graph.get(new Pair(iterable.xPos+1,iterable.yPos));
				iterable.children.add(childForward);
				if(!((y + 1)>gridSizeY)) {
					Node childUpForward = Graph.get(new Pair(iterable.xPos+1,iterable.yPos+1));
					iterable.children.add(childUpForward);
				}
				if(!((y - 1)<-gridSizeY)) {
					Node childDownForward = Graph.get(new Pair(iterable.xPos+1,iterable.yPos-1));
					iterable.children.add(childDownForward);
				}
			}
			if(!((x - 1)<-gridSizeX)) {
				Node childBackward = Graph.get(new Pair(iterable.xPos-1,iterable.yPos));
				iterable.children.add(childBackward);
				if(!((y + 1)>gridSizeY)) {
					Node childUpForward = Graph.get(new Pair(iterable.xPos-1,iterable.yPos+1));
					iterable.children.add(childUpForward);
				}
				if(!((y - 1)<-gridSizeY)) {
					Node childDownForward = Graph.get(new Pair(iterable.xPos-1,iterable.yPos-1));
					iterable.children.add(childDownForward);
				}
			}	
		}

		isGraphGenerated = true;
		State =  Graph;
		List = State.values();
	}
	public HashMap<Pair,Node> generateEmptyGraph() {

		HashMap<Pair,Node> Graph = new HashMap<Pair,Node>();
		for( int i = -gridSizeX; i <= gridSizeX; i++ ) {
			for ( int j = gridSizeY; j>= -gridSizeY; j--) {
				Node currentNode = new Node(i,j);
				Graph.put(new Pair(i,j),currentNode);
			}
		}
		Collection<Node> listNodes = Graph.values();
		Iterator<Node> listIter = listNodes.iterator();
		while(listIter.hasNext()) {
			Node iterable = listIter.next();
			int y = iterable.yPos;
			int x = iterable.xPos;

			if(!((y - 1)<-gridSizeY)) {
				Node childUp = Graph.get(new Pair(iterable.xPos,iterable.yPos-1)); //Example of using a pair to check the HashMap
				iterable.children.add(childUp);
			}
			if(!((y + 1)>gridSizeY)) {
				Node childDown = Graph.get(new Pair(iterable.xPos,iterable.yPos+1));
				iterable.children.add(childDown);
			}
			if(!((x + 1)>gridSizeX)) {
				Node childForward = Graph.get(new Pair(iterable.xPos+1,iterable.yPos));
				iterable.children.add(childForward);
				if(!((y + 1)>gridSizeY)) {
					Node childUpForward = Graph.get(new Pair(iterable.xPos+1,iterable.yPos+1));
					iterable.children.add(childUpForward);
				}
				if(!((y - 1)<-gridSizeY)) {
					Node childDownForward = Graph.get(new Pair(iterable.xPos+1,iterable.yPos-1));
					iterable.children.add(childDownForward);
				}
			}
			if(!((x - 1)<-gridSizeX)) {
				Node childBackward = Graph.get(new Pair(iterable.xPos-1,iterable.yPos));
				iterable.children.add(childBackward);
				if(!((y + 1)>gridSizeY)) {
					Node childUpForward = Graph.get(new Pair(iterable.xPos-1,iterable.yPos+1));
					iterable.children.add(childUpForward);
				}
				if(!((y - 1)<-gridSizeY)) {
					Node childDownForward = Graph.get(new Pair(iterable.xPos-1,iterable.yPos-1));
					iterable.children.add(childDownForward);
				}
			}
		}
		return Graph;
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
	protected Pair approxBBMove(float x, float y) {
		Pair placehold = new Pair(1,2);
		return placehold;
	}

	protected Pair approxMove(float x, float y, float sX, float sY, float dx, float dy){
		double xD = sX*0.04+0.5*dx*0.00004;
		double yD = sY*0.04+0.5*dy*0.00004;
		return new Pair((int)(xD+x),(int)(yD+y));
	}

	protected Pair approxMarioJump(float x, float y, float sX, float sY, float dx, float dy) {
		double xD = 0;
		double yD = 0;
		simMario.xJS = sX;
		simMario.yJS = sY;
		if (simMario.jumpTime < 0) {
			simMario.xJA = simMario.xJS;
			simMario.yJA = -simMario.jumpTime * simMario.yJS;
			simMario.jumpTime++;
		} else if (simMario.onGround && simMario.mayJump) {
			simMario.xJS = 1;
			simMario.yJS = -1.9f;
			++simMario.jumpTime;
			simMario.yJA = simMario.jumpTime * simMario.yJS;
			mario.onGround = false;
		}
		 else if (simMario.jumpTime > 0) {
			simMario.xJA += simMario.xJS;
			simMario.yJA = simMario.jumpTime * simMario.yJS;
			simMario.jumpTime--;
		}
		else {
			simMario.jumpTime = 0;
			simMario.onGround = true;
			simMario.mayJump = true;
		}
		yD = simMario.yJS*0.04+0.5*(simMario.yJA)*0.00004;
		xD = simMario.xJS*0.04;
		x+=xD;
		y+=yD;
		return new Pair((int)Math.floor(x),(int)Math.floor(y));
	}

	protected boolean collision(Pair unit1, Pair unit2) {
		return unit1.equals(unit2);
	}



	protected Pair approxGMove(float x, float y) {
		Pair placehold = new Pair(1,2);
		return placehold;
	}

	protected int approxFMove(float y) {
		int x = 0 ;
		return x;
	}
	public Vector<HashMap<Pair,Node>> tick( HashMap<Pair,Node> cState, Action[] possibleActions) {
		//tick =  40ms .040 seconds
		HashMap<Pair,Node> blankState = generateEmptyGraph();
		Vector<HashMap<Pair,Node>> possibleStates = new Vector<HashMap<Pair,Node>>();
		Node marioNode = null;
		Vector<genPair<EntityType,Pair>> updatedEnemyLocale = new Vector<genPair<EntityType,Pair>>();
		for(Map.Entry<Pair,Node> e : cState.entrySet()) {
			Node cNode = e.getValue();
			if(cNode.enemyHere) {
				EntityType eType = cNode.EnemyType;
				genPair<EntityType,Pair> newEnemyLoc = null;
				/*OH my god this switch statement is horrendous and should never be written in any production code -Ian T Butler on his own code.*/
				switch(eType) {
					//Change each move to their respective move method later
					case BULLET_BILL:
						newEnemyLoc = new genPair<EntityType,Pair>(eType,approxMove(cNode.xPos,cNode.yPos,0,0,0,0));
						break;
					case ENEMY_FLOWER:
						newEnemyLoc = new genPair<EntityType,Pair>(eType,approxMove(cNode.xPos,cNode.yPos,0,0,0,0));
						break;
					case GOOMBA:
						newEnemyLoc = new genPair<EntityType,Pair>(eType,approxMove(cNode.xPos,cNode.yPos,0,0,0,0));
						break;
					case GOOMBA_WINGED:
						newEnemyLoc = new genPair<EntityType,Pair>(eType,approxMove(cNode.xPos,cNode.yPos,0,0,0,0));
						break;
					case WAVE_GOOMBA:
						newEnemyLoc = new genPair<EntityType,Pair>(eType,approxMove(cNode.xPos,cNode.yPos,0,0,0,0));
						break;
					case RED_KOOPA:
						newEnemyLoc = new genPair<EntityType,Pair>(eType,approxMove(cNode.xPos,cNode.yPos,0,0,0,0));
						break;
					case RED_KOOPA_WINGED:
						newEnemyLoc = new genPair<EntityType,Pair>(eType,approxMove(cNode.xPos,cNode.yPos,0,0,0,0));
						break;
					case GREEN_KOOPA:
						newEnemyLoc = new genPair<EntityType,Pair>(eType,approxMove(cNode.xPos,cNode.yPos,0,0,0,0));
						break;
					case GREEN_KOOPA_WINGED:
						newEnemyLoc = new genPair<EntityType,Pair>(eType,approxMove(cNode.xPos,cNode.yPos,0,0,0,0));
						break;
					case SPIKY:
						newEnemyLoc = new genPair<EntityType,Pair>(eType,approxMove(cNode.xPos,cNode.yPos,0,0,0,0));
						break;
					case SPIKY_WINGED:
						newEnemyLoc = new genPair<EntityType,Pair>(eType,approxMove(cNode.xPos,cNode.yPos,0,0,0,0));
						break;
					case FIREBALL:
						newEnemyLoc = new genPair<EntityType,Pair>(eType,approxMove(cNode.xPos,cNode.yPos,0,0,0,0));
						break;
				}
				updatedEnemyLocale.add(newEnemyLoc);
			}
			//If the current node in the original state has blocks or a double block at this position so should the new one.
			blankState.get(new Pair(cNode.xPos,cNode.yPos)).blockHere = cNode.blockHere;
			blankState.get(new Pair(cNode.xPos,cNode.yPos)).doubleBlock = cNode.doubleBlock;
			if(cNode.mario) marioNode = cNode;

		}
		//Update the enemy positions
		for(genPair x : updatedEnemyLocale ) {
			Node beingUpdated = blankState.get(x.y);
			beingUpdated.enemyHere = true;
			beingUpdated.EnemyType = (EntityType)x.x;
		}
		//Make copies of the new state for the number of possible actions mario can take and move mario based on a possible action
		for(int i = 0; i < possibleActions.length; ++i) {
			Pair newMarioPos = null;
			HashMap<Pair,Node> currPossibleState = mapCopy(blankState);
			switch(possibleActions[i]) {
				case Jump:
					newMarioPos = approxMarioJump(marioNode.xPos,marioNode.yPos,simMario.xS,simMario.yS,simMario.dX,simMario.dY);
					break;
				case RightShortJump:
					newMarioPos = approxMarioJump(marioNode.xPos,marioNode.yPos,simMario.xS,simMario.yS,simMario.dX,simMario.dY);
					break;
				case RightLongJump:
					newMarioPos = approxMarioJump(marioNode.xPos,marioNode.yPos,simMario.xS,simMario.yS,simMario.dX,simMario.dY);
					break;
				case LeftShortJump:
					newMarioPos = approxMarioJump(marioNode.xPos,marioNode.yPos,simMario.xS,simMario.yS,simMario.dX,simMario.dY);
					break;
				case LeftLongJump:
					newMarioPos = approxMarioJump(marioNode.xPos,marioNode.yPos,simMario.xS,simMario.yS,simMario.dX,simMario.dY);
					break;
				case Right:
					newMarioPos = approxMove(marioNode.xPos,marioNode.yPos,simMario.xS,simMario.yS,simMario.dX,simMario.dY);
					break;
				case RightSpeed:
					newMarioPos = approxMove(marioNode.xPos,marioNode.yPos,simMario.xS,simMario.yS,simMario.dX,simMario.dY);
					break;
				case Left:
					newMarioPos = approxMove(marioNode.xPos,marioNode.yPos,simMario.xS,simMario.yS,simMario.dX,simMario.dY);
					break;
				case LeftSpeed:
					newMarioPos = approxMove(marioNode.xPos,marioNode.yPos,simMario.xS,simMario.yS,simMario.dX,simMario.dY);
					break;
			}
			Node updatingMarioNode = currPossibleState.get(newMarioPos);
			updatingMarioNode.mario = true;
			possibleStates.add(currPossibleState);
		}

		return possibleStates;


	}



}

/*
* TODO: Modify all the state updating code to take lists of entites found on the node, create a list of the same entities using our implementation of them and then redistribute them accordingly
 	Required Updates: Node Constructor and Reset Functions, tick()
 	Probably Need: A list containing the old entities a queue for redistribution, and intermediate list of partially contained entities, a final list of entities;

 */

