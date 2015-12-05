package togepi;

import java.util.ArrayList;

/**
 * Created by itb20 on 12/4/2015.
 */
public class Id3Node {
	public enum Attribute {
		Enemy,
		Block,
		DoubleBlock
	}
	public boolean root = false;
	public boolean leaf = true;
	public Attribute atrib = null;
	public GraphGenerator.Action doThis = null;
	public Id3Node yes = null;
	public Id3Node no = null;
}
