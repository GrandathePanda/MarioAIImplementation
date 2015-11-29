package EnemyActorPhysics;
import java.util.ArrayList;

public class controller {
	public double timer;
	public ArrayList<MySprite> allActors;
	
	public controller(){
		timer = 0.0;
		
	}
	public ArrayList<node> stepForward(){
		timer += 0.4;
		ArrayList<node> ret = new ArrayList<node>();
		for(int x = 0; x < allActors.size(); ++x){
			ret.add(allActors.get(x).nodeMove());
		}
		return ret;
	}
}
