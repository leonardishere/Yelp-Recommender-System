
public class StringPair {
	public String s1, s2;
	
	public StringPair(String s1, String s2) {
		this.s1 = s1;
		this.s2 = s2;
	}
	
	@Override
	public int hashCode() {
		return s1.hashCode() + s2.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if(this.getClass() != other.getClass()) return false;
		StringPair otherPair = (StringPair) other;
		return (this.s1.equals(otherPair.s1) && this.s2.equals(otherPair.s2)) ||
			   (this.s1.equals(otherPair.s2) && this.s2.equals(otherPair.s1));
	}
}
