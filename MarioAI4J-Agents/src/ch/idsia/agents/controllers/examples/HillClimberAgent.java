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
import togepi.GraphGenerator;
import togepi.GraphGenerator.Node;

/**
 * Your custom agent! Feel free to fool around!
 * 
 * @author Jakub 'Jimmy' Gemrot, gemrot@gamedev.cuni.cz
 */
public class HillClimberAgent extends MarioHijackAIBase implements IAgent {

	
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
	GraphGenerator Graph = new GraphGenerator(4,4);
	public MarioInput actionSelectionAI() {
		if( Graph.isGraphGenerated == false ) { //If the graph hasn't been generated yet, generate it.
			Graph.generateGraph(e,t);
			Graph.isGraphGenerated = true;
		}
		else {
			Graph.resetNodes(e, t);
		}
		Node StartNode = Graph.State.get(Graph.new Pair(0,0));	
		if(StartNode.goal == true) {
			action.press(MarioKey.RIGHT);
			System.gc();
			return action;
		}
		//--------- To here for what ever you want to do
		LinkedList<Node> frontier = new LinkedList<Node>();
		frontier.add(StartNode);
//		while(StartNode.goal != true){
			Vector<Node> children = StartNode.children;
			frontier.addAll(children);
			
			Iterator<Node> frontIter = children.iterator();
			
			action.release(MarioKey.SPEED);
			action.release(MarioKey.RIGHT);
			int counter3 = 0;

			while(frontIter.hasNext()){
				Node currChild = frontIter.next();
				frontier.addAll(currChild.children);
				Iterator<Node> iter = children.iterator();
				while(iter.hasNext()){
					Node curr = iter.next();
					action.set(MarioKey.JUMP, (enemyAhead() || brickAhead()) && mario.mayJump);
					action.set(MarioKey.SPEED, (enemyAhead() || brickAhead()) && mario.mayShoot );				
					if(curr.enemyHere || curr.blockHere){
//						action.set(MarioKey.JUMP,  (curr.enemyHere || curr.blockHere && mario.mayJump));
						action.set(MarioKey.JUMP, mario.mayJump || mario.speed.y < 0);
						if(!mario.onGround){
							action.press(MarioKey.JUMP);
						}
					}
					if(currChild.enemyHere && mario.mayShoot){
						action.press(MarioKey.SPEED);
						return action;
					}else{
						action.press(MarioKey.RIGHT);
					}
				}
			}
			System.out.println(frontier.size());

			
//			System.out.println(frontier.size());
//			int count2 = children.size();
//			for(int x = 0; x < children.size(); ++x){
//				boolean isShooting = false;
//				
//				Node currentChild = children.elementAt(x);
//				currentChild.seen = true;
//
//				if(currentChild.enemyHere == false){
//					--count2;
//				}
//
//				if(currentChild.enemyHere || currentChild.blockHere){
//					if(currentChild.enemyHere && mario.mayShoot) {
//						isShooting = true;
//						action.press(MarioKey.SPEED);
//					}
//					if(true && isShooting){
//						action.press(MarioKey.RIGHT);
//					}
//					action.set(MarioKey.JUMP, (currentChild.enemyHere || currentChild.blockHere) && mario.mayJump);	
//					if(!mario.onGround){
//						action.press(MarioKey.JUMP);
//					}
////					System.out.println(mario.mayJump);
//				}
//				isShooting = false;
//			}

			
//		}
		

		//Your code Here 
		

		System.gc(); //Suggest garbage clean-up after work here.
		return action;
	}
	
	public static void main(String[] args) {
		String options = FastOpts.FAST_VISx2_02_JUMPING + FastOpts.L_ENEMY(Enemy.GOOMBA);
		MarioSimulator simulator = new MarioSimulator(options);
		
		IAgent agent = new HillClimberAgent();
		
		simulator.run(agent);
		
		System.exit(0);
	}
}