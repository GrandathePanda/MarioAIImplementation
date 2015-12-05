//Made it better.

package ch.idsia.agents.controllers.examples;

import EnemyActorPhysics.MyMario;
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
import ch.idsia.benchmark.mario.environments.MarioEnvironment;
import ch.idsia.benchmark.mario.options.FastOpts;
import togepi.GraphGenerator;
import togepi.GraphGenerator.Action;
import togepi.GraphGenerator.Node;
import togepi.Id3Node;
import togepi.Pair;
import togepi.genPair;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Your custom agent! Feel free to fool around!
 * 
 * @author Jakub 'Jimmy' Gemrot, gemrot@gamedev.cuni.cz
 */
public class AStarNewAgentIPerceptronCalc extends MarioHijackAIBase implements IAgent {
	public static double misClassifyEnemy = 0;
	public static double enemies = 0;
	public static double blocks = 0;
	public static double doubleblock = 0;
	public static double actions = 0;
	public static double jump = 0;
	public static double right = 0;
	public static double longjumpR = 0;
	public static double totalUsedStates = 0;
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

	public MarioInput doActions(Vector<Action> ac) {
		MarioInput doThese = new MarioInput();
		for(Action x : ac) {
			++actions;
			switch (x) {
				case Jump:
					++jump;
					doThese.set(MarioKey.JUMP, MarioEnvironment.getInstance().getMario().mayJump);
					break;
				case RightLongJump:
					++longjumpR;
					if(!MarioEnvironment.getInstance().getMario().onGround)
						doThese.press(MarioKey.JUMP);
					doThese.release(MarioKey.LEFT);
					doThese.press(MarioKey.RIGHT);
				case LeftLongJump:
					doThese.press(MarioKey.JUMP);
					doThese.release(MarioKey.RIGHT);
					doThese.press(MarioKey.LEFT);
				case Right:
					++right;
					doThese.release(MarioKey.LEFT);
					doThese.press(MarioKey.RIGHT);
					break;
				case RightSpeed:
					++right;
					doThese.release(MarioKey.LEFT);
					doThese.press(MarioKey.RIGHT);
					doThese.press(MarioKey.SPEED);
					break;
				case Left:
					doThese.release(MarioKey.RIGHT);
					doThese.press(MarioKey.LEFT);
					break;
				case LeftSpeed:
					doThese.release(MarioKey.RIGHT);
					doThese.press(MarioKey.LEFT);
					doThese.press(MarioKey.SPEED);
					break;
			}
		}
		return doThese;
	}


	boolean graph = false;
	GraphGenerator Graph = null;
	private final Action[] allPossibleActions = {Action.Jump,Action.LeftLongJump,Action.RightLongJump,Action.Left,Action.Right,Action.LeftSpeed,Action.RightSpeed};
	private final Action[] airPossibleActions = {Action.LeftLongJump,Action.RightLongJump,Action.Left,Action.Right};
	private final Action[] groundPossibleActions = {Action.Jump,Action.Left,Action.Right,Action.LeftSpeed,Action.RightSpeed};
	private final Action[] airHangPossibleActions = {Action.Left,Action.Right};
	boolean g1 = false; //deal with air drop in the beggining

	int marioSize = 1;

	public MarioInput actionSelectionAI() {
		//System.out.println(t.brick(1,0));
		if(((mario.mode.getCode()-2) * -1)+1 > marioSize) {
			++misClassifyEnemy;
			++enemies;
			marioSize = ((mario.mode.getCode()-2) * -1)+1;
			System.out.print(marioSize);
		}
		Vector<HashMap<Pair,Node>> solutionStates = new Vector<HashMap<Pair, Node>>();
		LinkedList<HashMap<Pair,Node>> frontierStates = new LinkedList<HashMap<Pair,Node>>();
		Vector<HashMap<Pair,Node>> seenStates = new Vector<HashMap<Pair,Node>>();
		Vector<Action> bestInTime = new Vector<>();
		if(!graph) { //If the graph hasn't been generated yet, generate it.
			Graph = new GraphGenerator(9,9,mario);
			Graph.generateGraph(e,t);
			Graph.isGraphGenerated = true;
			graph = true;
		}
		else {
			Graph.resetNodes(e,t,mario);

		}
		MyMario simM = Graph.State.get(new Pair(0,0)).alterMario;
		Action[] modPosAction;
		if(simM.onGround || mario.onGround) {
			modPosAction = groundPossibleActions;
		}
		else {
			if(!simM.onGround || !mario.onGround)
				modPosAction = airPossibleActions;
			else modPosAction = airHangPossibleActions;
		}
		int runs = 0;
		boolean firstStateSeenCurrent = false;
		frontierStates.push(Graph.State);
		solutionStates.add(Graph.State);
		Pair oldMarioPos = new Pair(0,0);
		while(!frontierStates.isEmpty() && runs < 7) {
			HashMap<Pair,Node> currentState = frontierStates.removeFirst();
			solutionStates.add(currentState);
			seenStates.add(currentState);
			Vector<genPair<Pair,genPair<Action,HashMap<Pair,Node>>>> childStates = null;
			if (firstStateSeenCurrent) {
				//System.out.println("HERE TickModel");
				childStates = Graph.tickModel(currentState, modPosAction);
			} else {
				//System.out.println("HERE Tick");
				childStates = Graph.tick(currentState, modPosAction);
				firstStateSeenCurrent = true;
			}
			genPair<Action,HashMap<Pair,Node>> tentativeBest = null;
			Pair tentativeLoc = null;
			double tentativeCost = 0;
			for(genPair<Pair,genPair<Action,HashMap<Pair,Node>>> x: childStates) {
				double cost = 0;
				Pair marioLoc = x.x;
				genPair<Action,HashMap<Pair,Node>> currChild = x.y;
				double heuristic = Math.sqrt(Math.pow(9-marioLoc.x,2)+Math.pow(-2-marioLoc.y,2));
				boolean samePos = (marioLoc.x == oldMarioPos.x && marioLoc.y == oldMarioPos.y);
				if(currChild.x == Action.Left || currChild.x == Action.LeftSpeed || currChild.x == Action.LeftLongJump) cost+= 5;
				double notMovingCost = ( samePos ) ? 1 : 0;
				double enemyCost = (Graph.collision(currChild.y.get(marioLoc).alterMario,currChild.y)) ? 1  : 0;
				if(enemyCost > 0) ++ enemies;
				if(currChild.x == Action.Jump && (Graph.collision(currChild.y.get(marioLoc).alterMario,currChild.y)) == false) {
					++blocks;
				}
				double scaleCost = enemyCost*100;
				double pathCost =  0;
				if(tentativeLoc == null) {
					pathCost= Math.sqrt(Math.pow(0-marioLoc.x,2)+Math.pow(0-marioLoc.y,2));
				}
				else pathCost = Math.sqrt(Math.pow(tentativeLoc.x-marioLoc.x,2)+Math.pow(tentativeLoc.y-marioLoc.y,2));
				cost += (pathCost+scaleCost+heuristic+notMovingCost);
				if(tentativeBest == null || cost <= tentativeCost)
				{
					tentativeBest = currChild;
					tentativeLoc = marioLoc;
					tentativeCost = cost;
				}

			}
			if(childStates.size() == 0) {
				System.out.println(runs);
				return doActions(bestInTime);
			}
			frontierStates.addFirst(tentativeBest.y);
			++totalUsedStates;
			if(tentativeBest.x == Action.RightLongJump) ++doubleblock;
			oldMarioPos = tentativeLoc;
			//System.out.println(tentativeLoc.x + "  " + tentativeLoc.y +" --- " + tentativeBest.x.toString() + " -- " +tentativeCost);
			simM = tentativeBest.y.get(tentativeLoc).alterMario;
			bestInTime.add(tentativeBest.x);
			++runs;
		}

		System.gc();
		return doActions(bestInTime);
	}


	public static Vector<Id3Node> produceTree(Vector<genPair<Id3Node.Attribute,Double>> gains, Vector<Id3Node> empty, Id3Node update) {
		Vector<Id3Node> Tree = empty;
		if(gains.isEmpty()) {
			update.atrib = Id3Node.Attribute.None;
			Tree.add(update);
			update.leaf = true;
			return Tree;
		}
		if(Tree.isEmpty()) {
			Id3Node root = new Id3Node();
			root.atrib = gains.remove(gains.size()-1).x;
			root.root = true;
			root.no = new Id3Node();
			root.yes = new Id3Node();
			root.yes.leaf = true;
			switch (root.atrib) {
				case Enemy:
					root.yes.doThis = Action.Jump;
					break;
				case Block:
					root.yes.doThis = Action.Jump;
					break;
				case DoubleBlock:
					root.yes.doThis = Action.RightLongJump;

			}
			Tree.add(root);
			return produceTree(gains,Tree,root.no);
		}
		else {
			update.atrib = gains.remove(gains.size()-1).x;
			update.no = new Id3Node();
			update.yes = new Id3Node();
			update.yes.leaf = true;
			switch (update.atrib) {
				case Enemy:
					update.yes.doThis = Action.Jump;
					break;
				case Block:
					update.yes.doThis = Action.Jump;
					break;
				case DoubleBlock:
					update.yes.doThis = Action.RightLongJump;

			}
			Tree.add(update);
			return produceTree(gains,Tree,update.no);

		}
	}
	public static void main(String[] args) {
		for(int i = 0; i < 1; ++i) {
			String options = FastOpts.FAST_VISx2_02_JUMPING+FastOpts.L_DIFFICULTY(0)+FastOpts.L_ENEMY(Enemy.GOOMBA)+FastOpts.L_RANDOMIZE+FastOpts.L_CANNONS_ON;
			MarioSimulator simulator = new MarioSimulator(options);
			IAgent agent = new AStarNewAgentIPerceptronCalc();
			simulator.run(agent);

		}
		double probMisClasify  = misClassifyEnemy/enemies;
		double probJump = jump/actions;
		double probLongR = longjumpR/actions;
		double probRightS = right/actions;
		double probEnemy = enemies/totalUsedStates;
		double probBlock = blocks/totalUsedStates;
		double probNotBlock = (totalUsedStates-blocks)/totalUsedStates;
		double probNotDbl = (totalUsedStates-doubleblock)/totalUsedStates;
		double probNotEnemy = (totalUsedStates-enemies)/totalUsedStates;
		double probDblBlk = doubleblock/totalUsedStates;
		System.out.println("Jump: " + jump + " longJ: "+ longjumpR + " RightS: " + right + "Actions: " + actions);
		System.out.println("Block: " + blocks + "dblBlock: " + doubleblock + "totalUsedStates: " + totalUsedStates);
		System.out.println("Enemies: " + enemies + "MissedEn: " + misClassifyEnemy);
		double entropy = -probLongR*Math.log(probLongR);;
		entropy += -probJump*Math.log(probJump);
		entropy += -probRightS*Math.log(probRightS);
		double GainBlk = entropy-probBlock*Math.log(probBlock)-probNotBlock*Math.log(probNotBlock);
		double GainEnemy = entropy-probEnemy*Math.log(probEnemy)-probNotEnemy*Math.log(probNotEnemy);
		double GainDblBlk = entropy-probDblBlk*Math.log(probDblBlk)-probNotDbl*Math.log(probNotDbl);
		Vector<genPair<Id3Node.Attribute,Double>> gains = new Vector<>();
		Vector<Double> gainsN = new Vector<>();
		gainsN.add(GainBlk);
		gainsN.add(GainEnemy);
		gainsN.add(GainDblBlk);

		Collections.sort(gainsN);
		for( Double x : gainsN) {
			System.out.println(x.doubleValue());
			if(x.doubleValue() == GainBlk) {
				gains.add(new genPair<Id3Node.Attribute, Double>(Id3Node.Attribute.Block,GainBlk));
			}
			else {
				if(x.doubleValue() == GainEnemy) {
					gains.add(new genPair<Id3Node.Attribute, Double>(Id3Node.Attribute.Enemy,GainEnemy));
				}
				else {
					gains.add(new genPair<Id3Node.Attribute, Double>(Id3Node.Attribute.DoubleBlock,GainDblBlk));
				}
			}
		}




		genPair<Id3Node.Attribute,Double> old = null;





		Vector<Id3Node> id3Tree =  produceTree(gains,new Vector<Id3Node>(),null);
		for(Id3Node x : id3Tree) {
			System.out.println(x.atrib);
		}
		String options = FastOpts.FAST_VISx2_02_JUMPING+FastOpts.L_DIFFICULTY(0)+FastOpts.L_ENEMY(Enemy.GOOMBA)+FastOpts.L_RANDOMIZE+FastOpts.L_CANNONS_ON;
		MarioSimulator simulator = new MarioSimulator(options);
		IAgent agent = new ID3Agent(id3Tree);
		simulator.run(agent);

	}
}