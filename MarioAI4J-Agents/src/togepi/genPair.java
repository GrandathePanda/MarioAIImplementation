package togepi;

/**
 * Created by itb20 on 11/29/2015.
 */
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