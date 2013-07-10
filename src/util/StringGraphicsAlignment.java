package util;

public enum StringGraphicsAlignment {
	Left(0), Right(1),
	Top(0), Bottom(1),
	Center(2);
	
	private int type;
	StringGraphicsAlignment(int type) {
		this.type = type;
	}
}
