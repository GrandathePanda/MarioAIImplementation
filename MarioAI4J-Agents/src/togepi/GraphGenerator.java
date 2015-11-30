package togepi;

import EnemyActorPhysics.*;
import ch.idsia.agents.controllers.modules.Entities;
import ch.idsia.agents.controllers.modules.Tiles;
import ch.idsia.benchmark.mario.engine.generalization.Entity;
import ch.idsia.benchmark.mario.engine.generalization.MarioEntity;

import java.util.*;


/**
 * Core Class that contains the Graph and must be instantiated before actionSelectionAI() in your agent class.
 * The constructor takes two integers which correspond to the X and Y size of the grid you want to construct. Note, can be no larger than the perceptive grid, but can be smaller and non square e.g. 5x9.
 * Call generateGraph(Entities a, Tiles b, AgentType T) in actionSelectionAI() to generate the grid ON FIRST RUN
 */
public class GraphGenerator {
	public marioDoll simMario = null;
	/*! X Size of Grid After Instantiation */
	public int gridSizeX = 0;
	/*! Y Size of Grid After Instantiation */
	public int gridSizeY = 0;
	/*! Hashmap of <Pair,Node> Type containing current Perceptive State */
	public HashMap<Pair, Node> State = null;
	/*! Collection of <Node> Type containing current viewable field. Created from the HashMap */
	public Collection<Node> List = null;
	/*! Boolean set to true after generateGraph() has been called the first time.*/
	public boolean isGraphGenerated = false;
	private MarioEntity mario = null;
	private Entities e = null; /*! Contains Entities from current Perceptive Grid Sampling*/
	private Tiles t = null; /*! Contains Tiles from current Perceptive Grid Sampling*/
	public GraphGenerator(int x, int y, MarioEntity marioClone) {
		gridSizeX = x;
		gridSizeY = y;
		mario = marioClone;
		simMario = new marioDoll(mario.speed.x, mario.speed.y, ((mario.mode.getCode() - 2) * -1), 0, 0, mario.mayJump, mario.onGround);
	}

	public static <key> HashMap<key, Node> mapCopy(final HashMap<key, Node> hashMap) {
		HashMap<key, Node> copyMap = new HashMap<>();
		for (Map.Entry<key, Node> e : hashMap.entrySet()) {
			copyMap.put(e.getKey(), e.getValue().clone());
		}

		return copyMap;
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
		HashMap<Pair, Node> Graph = new HashMap<>();
		for (int i = -gridSizeX; i <= gridSizeX; i++) {
			for (int j = gridSizeY; j >= -gridSizeY; j--) {
				Node currentNode = new Node(i, j, 2 * gridSizeX, 2 * gridSizeY);
				if (i == 0 && j == 0) currentNode.mario = true;
				Graph.put(new Pair(i, j), currentNode);
			}
		}
		Collection<Node> listNodes = Graph.values();
		for (Node iterable : listNodes) {
			int y = iterable.yPos;
			int x = iterable.xPos;

			if (!((y - 1) < -gridSizeY)) {
				Node childUp = Graph.get(new Pair(iterable.xPos, iterable.yPos - 1)); //Example of using a pair to check the HashMap
				iterable.children.add(childUp);
			}
			if (!((y + 1) > gridSizeY)) {
				Node childDown = Graph.get(new Pair(iterable.xPos, iterable.yPos + 1));
				iterable.children.add(childDown);
			}
			if (!((x + 1) > gridSizeX)) {
				Node childForward = Graph.get(new Pair(iterable.xPos + 1, iterable.yPos));
				iterable.children.add(childForward);
				if (!((y + 1) > gridSizeY)) {
					Node childUpForward = Graph.get(new Pair(iterable.xPos + 1, iterable.yPos + 1));
					iterable.children.add(childUpForward);
				}
				if (!((y - 1) < -gridSizeY)) {
					Node childDownForward = Graph.get(new Pair(iterable.xPos + 1, iterable.yPos - 1));
					iterable.children.add(childDownForward);
				}
			}
			if (!((x - 1) < -gridSizeX)) {
				Node childBackward = Graph.get(new Pair(iterable.xPos - 1, iterable.yPos));
				iterable.children.add(childBackward);
				if (!((y + 1) > gridSizeY)) {
					Node childUpForward = Graph.get(new Pair(iterable.xPos - 1, iterable.yPos + 1));
					iterable.children.add(childUpForward);
				}
				if (!((y - 1) < -gridSizeY)) {
					Node childDownForward = Graph.get(new Pair(iterable.xPos - 1, iterable.yPos - 1));
					iterable.children.add(childDownForward);
				}
			}
		}

		isGraphGenerated = true;
		State = Graph;
		List = State.values();
	}

	public HashMap<Pair, Node> generateEmptyGraph() {

		HashMap<Pair, Node> Graph = new HashMap<>();
		for (int i = -gridSizeX; i <= gridSizeX; i++) {
			for (int j = gridSizeY; j >= -gridSizeY; j--) {
				Node currentNode = new Node(i, j);
				Graph.put(new Pair(i, j), currentNode);
			}
		}
		Collection<Node> listNodes = Graph.values();
		for (Node iterable : listNodes) {
			int y = iterable.yPos;
			int x = iterable.xPos;

			if (!((y - 1) < -gridSizeY)) {
				Node childUp = Graph.get(new Pair(iterable.xPos, iterable.yPos - 1)); //Example of using a pair to check the HashMap
				iterable.children.add(childUp);
			}
			if (!((y + 1) > gridSizeY)) {
				Node childDown = Graph.get(new Pair(iterable.xPos, iterable.yPos + 1));
				iterable.children.add(childDown);
			}
			if (!((x + 1) > gridSizeX)) {
				Node childForward = Graph.get(new Pair(iterable.xPos + 1, iterable.yPos));
				iterable.children.add(childForward);
				if (!((y + 1) > gridSizeY)) {
					Node childUpForward = Graph.get(new Pair(iterable.xPos + 1, iterable.yPos + 1));
					iterable.children.add(childUpForward);
				}
				if (!((y - 1) < -gridSizeY)) {
					Node childDownForward = Graph.get(new Pair(iterable.xPos + 1, iterable.yPos - 1));
					iterable.children.add(childDownForward);
				}
			}
			if (!((x - 1) < -gridSizeX)) {
				Node childBackward = Graph.get(new Pair(iterable.xPos - 1, iterable.yPos));
				iterable.children.add(childBackward);
				if (!((y + 1) > gridSizeY)) {
					Node childUpForward = Graph.get(new Pair(iterable.xPos - 1, iterable.yPos + 1));
					iterable.children.add(childUpForward);
				}
				if (!((y - 1) < -gridSizeY)) {
					Node childDownForward = Graph.get(new Pair(iterable.xPos - 1, iterable.yPos - 1));
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
		for (Node resetNode : List) {
			resetNode.reset();
		}
	}

	protected Pair approxMove(float x, float y, boolean left, boolean right, boolean speed) {
		simMario.xa = speed ? 1.2f : 0.6f;
		float xD = simMario.xa * 0.4f;
		x += left ? -xD : xD;
		return new Pair((int) x, (int) y);
	}

	public Pair approxMarioJump(float x, float y, float sX, float sY, boolean left, boolean right, boolean longJ) {
		double xD = 0;
		double yD = 0;
		float xJump = 0;
		float yJump = 0;
		int count = 0;
		int limit = 14;
		if (!longJ) limit = 4;
		while (count < limit) {
			if (simMario.jumpTime < 0) {
				simMario.xa = xJump;
				simMario.ya = -simMario.jumpTime * yJump;
				simMario.jumpTime++;
			} else if (simMario.onGround && simMario.mayJump) {
				xJump = 0;
				yJump = -1.9f;
				simMario.jumpTime = 7;
				simMario.ya = simMario.jumpTime * yJump;
				mario.onGround = false;
			} else if (simMario.jumpTime > 0) {
				simMario.xa += xJump;
				simMario.ya = simMario.jumpTime * yJump;
				simMario.jumpTime--;
			} else {
				simMario.jumpTime = 0;
				simMario.onGround = true;
				simMario.mayJump = true;
			}
			yD += simMario.ya * 0.04 + 0.5 * (simMario.jumpTime) * 0.0016;
			xD += simMario.xa * 0.4;
			++count;
		}
		simMario.xa = 1.2f;
		simMario.jumpTime = 7;
		x += left ? -xD : xD;
		y += yD;

		return new Pair((int) Math.ceil(x), (int) Math.ceil(y));
	}

	public Vector<genPair<Pair, genPair<Action, HashMap<Pair, Node>>>> tick(HashMap<Pair, Node> cState, Action[] possibleActions) {
		//tick =  40ms .040 seconds
		Vector<genPair<Pair, Entity>> existingEntities = new Vector<>();
		HashMap<Pair, Node> blankState = generateEmptyGraph();
		Vector<genPair<Pair, genPair<Action, HashMap<Pair, Node>>>> possibleStates = new Vector<>();
		Node marioNode = null;

		for (Map.Entry<Pair, Node> et : cState.entrySet()) {
			Node cNode = et.getValue();
			Pair pos = new Pair(cNode.xPos, cNode.yPos);
			List<Entity> cNodeEntities = e.entities(cNode.xPos, cNode.yPos);
			for (Entity x : cNodeEntities) {
				existingEntities.add(new genPair<>(pos, x));
			}

			//If the current node in the original state has blocks or a double block at this position so should the new one.
			blankState.get(new Pair(cNode.xPos, cNode.yPos)).blockHere = cNode.blockHere;
			blankState.get(new Pair(cNode.xPos, cNode.yPos)).doubleBlock = cNode.doubleBlock;

			if (cNode.mario) marioNode = cNode;

		}
		//Update the enemy positions
		for (genPair<Pair, Entity> e : existingEntities) {
			Node moveEntityTo = null;
			switch (e.y.type) {
				case FIREBALL:
					Fireball a = new Fireball(e.x.x, e.x.y, 1);
					a.ya = e.y.speed.y;
					a.xa = e.y.speed.x;
					a.tick();
					moveEntityTo = blankState.get(new Pair(a.x, a.y));
					moveEntityTo.modelEntitiesHere.add(a);
					break;
				case BULLET_BILL:
					BulletBill f = new BulletBill(e.x.x, e.x.y, -1);
					f.ya = e.y.speed.y;
					f.xa = e.y.speed.x;
					f.tick();
					moveEntityTo = blankState.get(new Pair(f.x, f.y));
					moveEntityTo.modelEntitiesHere.add(f);
					moveEntityTo.enemyHere = true;
					break;
				case ENEMY_FLOWER:
					FlowerEnemy g = new FlowerEnemy(e.x.x, e.x.y);
					g.ya = e.y.speed.y;
					g.xa = e.y.speed.x;
					g.tick();
					moveEntityTo = blankState.get(new Pair(g.x, g.y));
					moveEntityTo.modelEntitiesHere.add(g);
					moveEntityTo.enemyHere = true;
					break;
				default:
					MyEnemy h = new MyEnemy(e.x.x, e.x.y, -1, false, e.y.type);
					h.tick();
					moveEntityTo = blankState.get(new Pair(h.x, h.y));
					moveEntityTo.modelEntitiesHere.add(h);
					moveEntityTo.enemyHere = true;
					break;
			}
		}
		//Make copies of the new state for the number of possible actions mario can take and move mario based on a possible action
		Node mTemp;
		for (Action possibleAction : possibleActions) {
			Pair newMarioPos = null;
			HashMap<Pair, Node> currPossibleState = mapCopy(blankState);
			switch (possibleAction) {
				case Jump:
					newMarioPos = approxMarioJump(marioNode.xPos, marioNode.yPos, simMario.xa, simMario.ya, false, false, false);
					mTemp = new Node(newMarioPos.x,newMarioPos.y, marioNode.sizeX, marioNode.sizeY);
					if(mTemp.doubleBlock){
						newMarioPos.y += 2;
					} else if(mTemp.blockHere){
						newMarioPos.y += 1;
					}
					break;
				case RightShortJump:
					newMarioPos = approxMarioJump(marioNode.xPos, marioNode.yPos, simMario.xa, simMario.ya, false, true, false);
					mTemp = new Node(newMarioPos.x,newMarioPos.y, marioNode.sizeX, marioNode.sizeY);
					if(mTemp.doubleBlock){
						newMarioPos.y += 2;
					} else if(mTemp.blockHere){
						newMarioPos.y += 1;
					}
					break;
				case RightLongJump:
					newMarioPos = approxMarioJump(marioNode.xPos, marioNode.yPos, simMario.xa, simMario.ya, false, true, true);
					mTemp = new Node(newMarioPos.x,newMarioPos.y, marioNode.sizeX, marioNode.sizeY);
					if(mTemp.doubleBlock){
						newMarioPos.y += 2;
					} else if(mTemp.blockHere){
						newMarioPos.y += 1;
					}
					break;
				case LeftShortJump:
					newMarioPos = approxMarioJump(marioNode.xPos, marioNode.yPos, simMario.xa, simMario.ya, true, false, false);
					mTemp = new Node(newMarioPos.x,newMarioPos.y, marioNode.sizeX, marioNode.sizeY);
					if(mTemp.doubleBlock){
						newMarioPos.y += 2;
					} else if(mTemp.blockHere){
						newMarioPos.y += 1;
					}
					break;
				case LeftLongJump:
					newMarioPos = approxMarioJump(marioNode.xPos, marioNode.yPos, simMario.xa, simMario.ya, true, false, true);
					mTemp = new Node(newMarioPos.x,newMarioPos.y, marioNode.sizeX, marioNode.sizeY);
					if(mTemp.doubleBlock){
						newMarioPos.y += 2;
					} else if(mTemp.blockHere){
						newMarioPos.y += 1;
					}
					break;
				case Left:
					newMarioPos = approxMove(marioNode.xPos, marioNode.yPos, true, false, false);
					break;
				case Right:
					newMarioPos = approxMove(marioNode.xPos, marioNode.yPos, true, false, false);
					break;
				case RightSpeed:
					newMarioPos = approxMove(marioNode.xPos, marioNode.yPos, true, false, true);
					break;
				case LeftSpeed:
					newMarioPos = approxMove(marioNode.xPos, marioNode.yPos, true, false, true);
					break;
			}
			if (newMarioPos.y < -9 || newMarioPos.y > 9) continue;
			Node updatingMarioNode = currPossibleState.get(newMarioPos);
			updatingMarioNode.mario = true;
			possibleStates.add(new genPair<Pair, genPair<Action, HashMap<Pair, Node>>>(newMarioPos,
					new genPair<Action, HashMap<Pair, Node>>(possibleAction, currPossibleState)));
		}

		return possibleStates;


	}

	public Vector<genPair<Pair, genPair<Action, HashMap<Pair, Node>>>> tickModel(HashMap<Pair, Node> cState, Action[] possibleActions) {
		//tick =  40ms .040 seconds
		Vector<genPair<Pair, MySprite>> existingEntities = new Vector<>();
		HashMap<Pair, Node> blankState = generateEmptyGraph();
		Vector<genPair<Pair, genPair<Action, HashMap<Pair, Node>>>> possibleStates = new Vector<>();
		Node marioNode = null;

		for (Map.Entry<Pair, Node> et : cState.entrySet()) {
			Node cNode = et.getValue();
			Pair pos = new Pair(cNode.xPos, cNode.yPos);
			List<MySprite> cNodeEntities = cNode.modelEntitiesHere;
			for (MySprite x : cNodeEntities) {
				existingEntities.add(new genPair<>(pos, x));
			}

			//If the current node in the original state has blocks or a double block at this position so should the new one.
			blankState.get(new Pair(cNode.xPos, cNode.yPos)).blockHere = cNode.blockHere;
			blankState.get(new Pair(cNode.xPos, cNode.yPos)).doubleBlock = cNode.doubleBlock;

			if (cNode.mario) marioNode = cNode;
		}
		//Update the enemy positions
		for (genPair<Pair, MySprite> e : existingEntities) {
			Node moveEntityTo = null;
			switch (e.y.type) {
				case FIREBALL:
					Fireball a = new Fireball(e.x.x, e.x.y, 1);
					a.ya = e.y.ya;
					a.xa = e.y.xa;
					a.tick();
					moveEntityTo = blankState.get(new Pair(a.x, a.y));
					moveEntityTo.modelEntitiesHere.add(a);
					break;
				case BULLET_BILL:
					BulletBill f = (BulletBill) e.y;
					f.ya = e.y.ya;
					f.xa = e.y.xa;
					f.tick();
					moveEntityTo = blankState.get(new Pair(f.x, f.y));
					moveEntityTo.modelEntitiesHere.add(f);
					moveEntityTo.enemyHere = true;
					break;
				case ENEMY_FLOWER:
					FlowerEnemy g = (FlowerEnemy) e.y;
					g.ya = e.y.ya;
					g.xa = e.y.xa;
					g.tick();
					moveEntityTo = blankState.get(new Pair(g.x, g.y));
					moveEntityTo.modelEntitiesHere.add(g);
					moveEntityTo.enemyHere = true;
					break;
				default:
					MyEnemy h = (MyEnemy) e.y;
					h.tick();
					moveEntityTo = blankState.get(new Pair(h.x, h.y));
					moveEntityTo.modelEntitiesHere.add(h);
					moveEntityTo.enemyHere = true;
					break;
			}
		}
		Node mTemp;
		//Make copies of the new state for the number of possible actions mario can take and move mario based on a possible action
		for (Action possibleAction : possibleActions) {
			Pair newMarioPos = null;
			HashMap<Pair, Node> currPossibleState = mapCopy(blankState);
			switch (possibleAction) {
				case Jump:
					newMarioPos = approxMarioJump(marioNode.xPos, marioNode.yPos, simMario.xa, simMario.ya, false, false, false);
					mTemp = new Node(newMarioPos.x,newMarioPos.y, marioNode.sizeX, marioNode.sizeY);
					if(mTemp.doubleBlock){
						newMarioPos.y += 2;
					} else if(mTemp.blockHere){
						newMarioPos.y += 1;
					}
					break;
				case RightShortJump:
					newMarioPos = approxMarioJump(marioNode.xPos, marioNode.yPos, simMario.xa, simMario.ya, false, true, false);
					mTemp = new Node(newMarioPos.x,newMarioPos.y, marioNode.sizeX, marioNode.sizeY);
					if(mTemp.doubleBlock){
						newMarioPos.y += 2;
					} else if(mTemp.blockHere){
						newMarioPos.y += 1;
					}
					break;
				case RightLongJump:
					newMarioPos = approxMarioJump(marioNode.xPos, marioNode.yPos, simMario.xa, simMario.ya, false, true, true);
					mTemp = new Node(newMarioPos.x,newMarioPos.y, marioNode.sizeX, marioNode.sizeY);
					if(mTemp.doubleBlock){
						newMarioPos.y += 2;
					} else if(mTemp.blockHere){
						newMarioPos.y += 1;
					}
					break;
				case LeftShortJump:
					newMarioPos = approxMarioJump(marioNode.xPos, marioNode.yPos, simMario.xa, simMario.ya, true, false, false);
					mTemp = new Node(newMarioPos.x,newMarioPos.y, marioNode.sizeX, marioNode.sizeY);
					if(mTemp.doubleBlock){
						newMarioPos.y += 2;
					} else if(mTemp.blockHere){
						newMarioPos.y += 1;
					}
					break;
				case LeftLongJump:
					newMarioPos = approxMarioJump(marioNode.xPos, marioNode.yPos, simMario.xa, simMario.ya, true, false, true);
					mTemp = new Node(newMarioPos.x,newMarioPos.y, marioNode.sizeX, marioNode.sizeY);
					if(mTemp.doubleBlock){
						newMarioPos.y += 2;
					} else if(mTemp.blockHere){
						newMarioPos.y += 1;
					}
					break;
				case Left:
					newMarioPos = approxMove(marioNode.xPos, marioNode.yPos, true, false, false);
					break;
				case Right:
					newMarioPos = approxMove(marioNode.xPos, marioNode.yPos, true, false, false);
					break;
				case RightSpeed:
					newMarioPos = approxMove(marioNode.xPos, marioNode.yPos, true, false, true);
					break;
				case LeftSpeed:
					newMarioPos = approxMove(marioNode.xPos, marioNode.yPos, true, false, true);
					break;
			}
			if (newMarioPos.y < -9 || newMarioPos.y > 9) continue;
			Node updatingMarioNode = currPossibleState.get(newMarioPos);
			updatingMarioNode.mario = true;
			possibleStates.add(new genPair<Pair, genPair<Action, HashMap<Pair, Node>>>(newMarioPos,
					new genPair<Action, HashMap<Pair, Node>>(possibleAction, currPossibleState)));
		}

		return possibleStates;


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
		LeftSpeed
	}

	public class marioDoll {
		public float xa = 0;
		public float ya = 0;
		public int x = 0;
		public int y = 0;
		public int strikes = 0;
		public int jumpTime = 0;
		public boolean onGround = false;
		public boolean mayJump = false;

		public marioDoll(float xSpeed, float ySpeed, int marioSize, int xPos, int yPos, boolean marioOnGround, boolean marioMayJump) {
			xa = xSpeed;
			ya = ySpeed;
			strikes = marioSize;
			x = xPos;
			y = yPos;
			onGround = marioOnGround;
			mayJump = marioMayJump;
		}

		public marioDoll() {

		}
	}

	@SuppressWarnings("CloneDoesntCallSuperClone")
	public class Node {
		/**
		 * Node class containing all the information about this particular cell in the grid.
		 * Constructor Node(int x, int y, AgentType T, int sx, int sy)
		 */

		public Vector<MySprite> modelEntitiesHere = new Vector<>();
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
		/*! Vector Type Node containing all the children of this node in the graph. Set by generateGraph().*/
		public Vector<Node> children = new Vector<>();

		/**
		 * Node Constructor.
		 */
		public Node(int x, int y, int sX, int sY) {
			sizeX = sX;
			sizeY = sY;
			xPos = x;
			yPos = y;
			blockHere = t.brick(x, y);
			doubleBlock = (t.brick(x + 1, y) && t.brick(x, y - 1));

		}

		public Node(int x, int y) {
			xPos = x;
			yPos = y;
		}

		/**
		 * Function call updating the node to current state of perceptive grid.
		 * Can be called singly by Node.reset() or also called by GraphGenerator.resetNodes(Entities a, Tiles t)
		 */
		public void reset() {
			blockHere = t.brick(xPos, yPos);
			doubleBlock = (t.brick(xPos, yPos) && t.brick(xPos + 2, yPos - 1));
			enemyHere = e.danger(xPos, yPos);
		}

		public Node clone() {
			Node copy = new Node(this.xPos, this.yPos, this.sizeX, this.sizeY);
			copy.blockHere = this.blockHere;
			copy.doubleBlock = this.doubleBlock;
			copy.enemyHere = this.enemyHere;
			copy.mario = this.mario;
			return copy;
		}
	}


}

/*
* TODO: Modify all the state updating code to take lists of entites found on the node, create a list of the same entities using our implementation of them and then redistribute them accordingly
 	Required Updates: Node Constructor and Reset Functions, tick()
 	Probably Need: A list containing the old entities a queue for redistribution, and intermediate list of partially contained entities, a final list of entities;

 */

