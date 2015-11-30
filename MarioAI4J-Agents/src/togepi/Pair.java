package togepi;

/**
 * Created by Ian on 11/29/2015.
 */
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