package togepi;

import EnemyActorPhysics.*;
import ch.idsia.agents.controllers.modules.Entities;
import ch.idsia.agents.controllers.modules.Tiles;
import ch.idsia.benchmark.mario.engine.generalization.Entity;
import ch.idsia.benchmark.mario.engine.generalization.EntityType;
import ch.idsia.benchmark.mario.engine.generalization.MarioEntity;
import ch.idsia.benchmark.mario.engine.input.MarioKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


/**
 * Core Class that contains the Graph and must be instantiated before actionSelectionAI() in your agent class.
 * The constructor takes two integers which correspond to the X and Y size of the grid you want to construct. Note, can be no larger than the perceptive grid, but can be smaller and non square e.g. 5x9.
 * Call generateGraph(Entities a, Tiles b, AgentType T) in actionSelectionAI() to generate the grid ON FIRST RUN
 */
public class GraphGenerator {
	/*@Nullable
	public marioDoll simMario = null;*/
	/*! X Size of Grid After Instantiation */
	public int gridSizeX = 0;
	public MarioEntity actualMario = null;
	/*! Y Size of Grid After Instantiation */
	public int gridSizeY = 0;
	/*! Hashmap of <Pair,Node> Type containing current Perceptive State */
	@Nullable
	public HashMap<Pair, Node> State = null;
	/*! Collection of <Node> Type containing current viewable field. Created from the HashMap */
	@Nullable
	public Collection<Node> List = null;
	/*! Boolean set to true after generateGraph() has been called the first time.*/
	public boolean isGraphGenerated = false;
	@Nullable
	private Entities e = null; /*! Contains Entities from current Perceptive Grid Sampling*/
	@Nullable
	private Tiles t = null; /*! Contains Tiles from current Perceptive Grid Sampling*/
	public GraphGenerator(int x, int y, MarioEntity marioClone) {
		gridSizeX = x;
		gridSizeY = y;
		actualMario = marioClone;
		//simMario = new marioDoll(mario.speed.x, mario.speed.y, ((mario.mode.getCode() - 2) * -1)+1, 0, 0, mario.mayJump, mario.onGround);
	}
	/*public void generateSimMario(@NotNull MarioEntity mario) {
		simMario = new marioDoll(mario.speed.x, mario.speed.y, ((mario.mode.getCode() - 2) * -1)+1, 0, 0, mario.mayJump, mario.onGround);
		if(mario.onGround) simMario.jumpTime = 4;
		else simMario.jumpTime = 3;
	}*/

	@NotNull
	public static <key> HashMap<key, Node> mapCopy(@NotNull final HashMap<key, Node> hashMap) {
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
				if (i == 0 && j == 0) {
					currentNode.mario = true;
					currentNode.alterMario = new MyMario(0,0,actualMario.onGround,actualMario.mayJump);
				}
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

	@NotNull
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
	public void resetNodes(Entities a, Tiles b, MarioEntity aM) {
		actualMario = aM;
		e = a;
		t = b;
		for (Node resetNode : List) {
			resetNode.reset();
			if(resetNode.xPos == 0 && resetNode.yPos == 0) {
				resetNode.alterMario = new MyMario(0,0, aM.onGround, aM.mayJump);
				resetNode.mario = true;
			}
		}
	}



	@NotNull
	public Vector<genPair<Pair, genPair<Action, HashMap<Pair, Node>>>> tick(@NotNull HashMap<Pair, Node> cState, @NotNull Action[] possibleActions) {
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

			if (cNode.mario) {
				marioNode = cNode;
			}

		}
		//Update the enemy positions
		for (genPair<Pair, Entity> e : existingEntities) {
			Node moveEntityTo = null;
			switch (e.y.type) {
				case FIREBALL:
					Fireball a = new Fireball(e.x.x, e.x.y, 1);
					a.ya = e.y.speed.y;
					a.xa = e.y.speed.x;
					Pair newLoc = a.tick();
					if(!(a.x < -9 || a.x > 9)) {
						moveEntityTo = blankState.get(newLoc);
						if(moveEntityTo ==  null) continue;
						moveEntityTo.modelEntitiesHere.add(a);
					}
					break;
				case BULLET_BILL:
					BulletBill f = new BulletBill(e.x.x, e.x.y, -1);
					f.ya = e.y.speed.y;
					f.xa = e.y.speed.x;
					Pair newLoc2 = f.tick();
					if(!(f.x < -9 || f.x > 9)) {
						moveEntityTo = blankState.get(newLoc2);
						moveEntityTo.modelEntitiesHere.add(f);
						moveEntityTo.enemyHere = true;
					}
					break;
				case ENEMY_FLOWER:
					FlowerEnemy g = new FlowerEnemy(e.x.x, e.x.y);
					g.ya = e.y.speed.y;
					g.xa = e.y.speed.x;
					Pair newLoc3 = g.tick();
					if(!(g.x < -9 || g.x > 9)) {
						moveEntityTo = blankState.get(newLoc3);
						moveEntityTo.modelEntitiesHere.add(g);
						moveEntityTo.enemyHere = true;
					}

					break;
				default:
					MyEnemy h = new MyEnemy(e.x.x, e.x.y, -1, false, e.y.type);
					Pair newLoc4 = h.tick();
					if ((h.x < -9 || h.x > 9) || (h.y <-9 || h.y>9)) {
					} else {
						System.out.println(h.x+"--"+h.y);
						moveEntityTo = blankState.get(newLoc4);
						if(moveEntityTo.blockHere) {
							newLoc4 = new Pair((int)h.xOld,(int)h.yOld);
							moveEntityTo = blankState.get(newLoc4);
							if(moveEntityTo.blockHere) break;
						}
						moveEntityTo.modelEntitiesHere.add(h);
						moveEntityTo.enemyHere = true;
					}
					break;
			}
		}
		//Make copies of the new state for the number of possible actions mario can take and move mario based on a possible action
		for (Action possibleAction : possibleActions) {
			Pair newMarioPos = null;
			Pair oldMarioPos = new Pair(marioNode.xPos,marioNode.yPos);
			MyMario oldMario = marioNode.alterMario;
			MyMario altRealityMario = marioNode.alterMario.clone();
			//System.out.println(altRealityMario.onGround +""+ altRealityMario.mayJump);
			HashMap<Pair, Node> currPossibleState = mapCopy(blankState);
			switch (possibleAction) {
				case Jump:
					altRealityMario.myKeys.set(MarioKey.JUMP,altRealityMario.mayJump);
					altRealityMario.tick();
					newMarioPos = new Pair(altRealityMario.x,--altRealityMario.y);
					break;
				case RightLongJump:
					altRealityMario.myKeys.press(MarioKey.JUMP);
					altRealityMario.myKeys.press(MarioKey.RIGHT);
					altRealityMario.facing = 1;
					altRealityMario.tick();
					newMarioPos = new Pair((int)altRealityMario.x,(int)altRealityMario.y);
					break;
				case LeftLongJump:
					altRealityMario.myKeys.press(MarioKey.JUMP);
					altRealityMario.myKeys.press(MarioKey.LEFT);
					altRealityMario.facing = -1;
					altRealityMario.tick();
					newMarioPos = new Pair((int)altRealityMario.x,(int)altRealityMario.y);
					break;
				case Left:
					altRealityMario.myKeys.press(MarioKey.LEFT);
					altRealityMario.tick();
					newMarioPos = new Pair((int)altRealityMario.x,(int)altRealityMario.y);
					break;
				case Right:
					altRealityMario.myKeys.press(MarioKey.RIGHT);
					altRealityMario.tick();
					newMarioPos = new Pair((int)altRealityMario.x,(int)altRealityMario.y);
					break;
				case RightSpeed:
					altRealityMario.myKeys.press(MarioKey.RIGHT);
					altRealityMario.myKeys.press(MarioKey.SPEED);
					altRealityMario.tick();
					newMarioPos = new Pair((int)altRealityMario.x,(int)altRealityMario.y);
					break;
				case LeftSpeed:
					altRealityMario.myKeys.press(MarioKey.LEFT);
					altRealityMario.myKeys.press(MarioKey.SPEED);
					altRealityMario.tick();
					newMarioPos = new Pair((int)altRealityMario.x,(int)altRealityMario.y);
					break;
			}
			if (newMarioPos.y < -9 || newMarioPos.y > 9) continue;
			Node updatingMarioNode = currPossibleState.get(newMarioPos);
			Node changeOldPos = currPossibleState.get(oldMarioPos);
			if(updatingMarioNode.blockHere && possibleAction != Action.LeftLongJump && possibleAction != Action.RightLongJump) {
				newMarioPos = oldMarioPos;
				updatingMarioNode = currPossibleState.get(newMarioPos);
				updatingMarioNode.mario = true;
				updatingMarioNode.alterMario = oldMario;
			}
			else {
				changeOldPos.mario = false;
				updatingMarioNode.mario = true;
				updatingMarioNode.alterMario = altRealityMario;
			}
			possibleStates.add(new genPair<Pair, genPair<Action, HashMap<Pair, Node>>>(newMarioPos,
					new genPair<Action, HashMap<Pair, Node>>(possibleAction, currPossibleState)));
		}

		return possibleStates;


	}

	@NotNull
	public Vector<genPair<Pair, genPair<Action, HashMap<Pair, Node>>>> tickModel(@NotNull HashMap<Pair, Node> cState, @NotNull Action[] possibleActions) {
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

			if (cNode.mario) {
				marioNode = cNode;
			}

		}
		//Update the enemy positions
		for (genPair<Pair, MySprite> e : existingEntities) {
			Node moveEntityTo = null;
			Vector<Fireball> ballsOfFire = new Vector<>();
			switch (e.y.type) {
				case FIREBALL:
					Fireball a = new Fireball(e.x.x, e.x.y, 1);
					a.ya = e.y.ya;
					a.xa = e.y.xa;
					Pair newLoc = a.tick();
					moveEntityTo = blankState.get(newLoc);
					moveEntityTo.modelEntitiesHere.add(a);
					break;
				case BULLET_BILL:
					BulletBill f = (BulletBill) e.y;
					f.ya = e.y.ya;
					f.xa = e.y.xa;
					Pair newLoc2 = f.tick();
					moveEntityTo = blankState.get(newLoc2);
					moveEntityTo.modelEntitiesHere.add(f);
					moveEntityTo.enemyHere = true;
					break;
				case ENEMY_FLOWER:
					FlowerEnemy g = (FlowerEnemy) e.y;
					g.ya = e.y.ya;
					g.xa = e.y.xa;
					Pair newLoc3 = g.tick();
					moveEntityTo = blankState.get(newLoc3);
					moveEntityTo.modelEntitiesHere.add(g);
					moveEntityTo.enemyHere = true;
					break;
				default:
					MyEnemy h = (MyEnemy) e.y;
					Pair newLoc4 = h.tick();
					moveEntityTo = blankState.get(newLoc4);
					if(moveEntityTo.blockHere) {
						newLoc4 = new Pair((int)h.xOld,(int)h.yOld);
						moveEntityTo = blankState.get(newLoc4);
						if(moveEntityTo.blockHere) break;
					}
					moveEntityTo.modelEntitiesHere.add(h);
					moveEntityTo.enemyHere = true;
					break;
			}
			for( Fireball x : ballsOfFire) {
				collision(x,blankState);
			}
		}
		//Make copies of the new state for the number of possible actions mario can take and move mario based on a possible action
		for (Action possibleAction : possibleActions) {
			Pair oldMarioPos = new Pair(marioNode.xPos,marioNode.yPos);
			Pair newMarioPos = null;
			MyMario oldMario = marioNode.alterMario;
			MyMario altRealityMario = marioNode.alterMario.clone();
			//System.out.println(altRealityMario.mayJump + "" + altRealityMario.onGround);
			HashMap<Pair, Node> currPossibleState = mapCopy(blankState);
			switch (possibleAction) {
				case Jump:
					altRealityMario.myKeys.set(MarioKey.JUMP,altRealityMario.mayJump);
					altRealityMario.tick();
					newMarioPos = new Pair((int)altRealityMario.x,--altRealityMario.y);
					break;
				case RightLongJump:
					altRealityMario.myKeys.press(MarioKey.JUMP);
					altRealityMario.myKeys.press(MarioKey.RIGHT);
					altRealityMario.tick();
					newMarioPos = new Pair((int)altRealityMario.x,(int)altRealityMario.y);
					break;
				case LeftLongJump:
					altRealityMario.myKeys.press(MarioKey.JUMP);
					altRealityMario.myKeys.press(MarioKey.LEFT);
					altRealityMario.tick();
					newMarioPos = new Pair((int)altRealityMario.x,(int)altRealityMario.y);
					break;
				case Left:
					altRealityMario.myKeys.press(MarioKey.LEFT);
					altRealityMario.tick();
					newMarioPos = new Pair((int)altRealityMario.x,(int)altRealityMario.y);
					break;
				case Right:
					altRealityMario.myKeys.press(MarioKey.RIGHT);
					altRealityMario.tick();
					newMarioPos = new Pair((int)altRealityMario.x,(int)altRealityMario.y);
					break;
				case RightSpeed:
					altRealityMario.myKeys.press(MarioKey.RIGHT);
					altRealityMario.myKeys.press(MarioKey.SPEED);
					altRealityMario.tick();
					newMarioPos = new Pair((int)altRealityMario.x,(int)altRealityMario.y);
					break;
				case LeftSpeed:
					altRealityMario.myKeys.press(MarioKey.LEFT);
					altRealityMario.myKeys.press(MarioKey.SPEED);
					altRealityMario.tick();
					newMarioPos = new Pair((int)altRealityMario.x,(int)altRealityMario.y);
					break;
			}
			if (newMarioPos.y < -9 || newMarioPos.y > 9 || newMarioPos.x > 9 || newMarioPos.x < -9) continue;
			Node updatingMarioNode = currPossibleState.get(newMarioPos);
			Node updateOldPos = currPossibleState.get(oldMarioPos);
			if(updatingMarioNode.blockHere && possibleAction != Action.LeftLongJump && possibleAction != Action.RightLongJump) {
				newMarioPos = oldMarioPos;
				updatingMarioNode = currPossibleState.get(newMarioPos);
				updatingMarioNode.mario = true;
				updatingMarioNode.alterMario = oldMario;
			}
			else {
				updateOldPos.mario = false;
				updateOldPos.alterMario = null;
				updatingMarioNode.mario = true;
				updatingMarioNode.alterMario = altRealityMario;
			}
			possibleStates.add(new genPair<Pair, genPair<Action, HashMap<Pair, Node>>>(newMarioPos,
					new genPair<Action, HashMap<Pair, Node>>(possibleAction, currPossibleState)));
		}

		return possibleStates;


	}

	public boolean collision(MySprite sprite, HashMap<Pair,Node> state) {
		Pair mario = new Pair((int)sprite.x,(int)sprite.y);
		Pair left = new Pair(mario.x-1,mario.y);
		Pair right = new Pair(mario.x+1,mario.y);
		Pair leftUp = new Pair(mario.x-1,mario.y-1);
		Pair rightUp = new Pair(mario.x+1,mario.y-1);
		Pair leftDown = new Pair(mario.x-1,mario.y+1);
		Pair rightDown = new Pair(mario.x+1,mario.y+1);
		Pair down = new Pair(mario.x,mario.y-1);
		Pair up = new Pair(mario.x,mario.y);
		Node leftN = state.get(left);
		Node rightN = state.get(right);
		Node leftUpN = state.get(leftUp);
		Node rightUpN = state.get(rightUp);
		Node leftDownN = state.get(leftDown);
		Node rightDownN = state.get(rightDown);
		Node upN = state.get(up);
		Node here = state.get(mario);
		boolean hit = false;
		MyMario checkMario = here.alterMario;
		Vector<Node> checkThese = new Vector<>();
		if(leftN != null) checkThese.add(leftN);
		if(rightN != null) checkThese.add(rightN);
		if(upN != null) checkThese.add(upN);
		if(leftUpN != null) checkThese.add(leftUpN);
		if(rightUpN != null) checkThese.add(rightUpN);
		if(leftDownN != null) checkThese.add(leftDownN);
		if(rightDownN != null) checkThese.add(rightDownN);
		for(Node x : checkThese) {
			if(x.enemyHere) hit = true;
			if(sprite.type == EntityType.FIREBALL  && hit== true) {
				x.modelEntitiesHere = new Vector<>();
				here.modelEntitiesHere.remove(sprite);

			}
		}
		return hit;
	}

	public enum Action {
		Jump,
		RightLongJump,
		LeftLongJump,
		Right,
		RightSpeed,
		Left,
		LeftSpeed
	}


	@SuppressWarnings("CloneDoesntCallSuperClone")
	public class Node {
		/**
		 * Node class containing all the information about this particular cell in the grid.
		 * Constructor Node(int x, int y, AgentType T, int sx, int sy)
		 */

		@NotNull
		public Vector<MySprite> modelEntitiesHere = new Vector<>();
		public int sizeX = 9;
		public int sizeY = 9;
		public MyMario alterMario = null;
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
		@Nullable
		public Node next = null;
		/*! Node Optional for solution-chains(Pathing/Search Algorithms) the node before it in the chain. Set by you the coder.*/
		@Nullable
		public Node prev = null;
		/*! Vector Type Node containing all the children of this node in the graph. Set by generateGraph().*/
		@NotNull
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
			alterMario = null;
			mario = false;
			modelEntitiesHere = new Vector<>();
			doubleBlock = (t.brick(xPos, yPos) && t.brick(xPos + 2, yPos - 1));
			enemyHere = e.danger(xPos, yPos);
		}

		@NotNull
		public Node clone() {
			Node copy = new Node(this.xPos, this.yPos, this.sizeX, this.sizeY);
			copy.blockHere = this.blockHere;
			copy.doubleBlock = this.doubleBlock;
			copy.enemyHere = this.enemyHere;
			copy.mario = this.mario;
			copy.alterMario = this.alterMario;
			return copy;
		}
	}


}

/*
* TODO: Modify all the state updating code to take lists of entites found on the node, create a list of the same entities using our implementation of them and then redistribute them accordingly
 	Required Updates: Node Constructor and Reset Functions, tick()
 	Probably Need: A list containing the old entities a queue for redistribution, and intermediate list of partially contained entities, a final list of entities;

 */

