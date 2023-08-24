package selfDrivingCar;

public class Intersections implements Comparable<Intersections> {

	private double x;
	private double y;
	private double offset;

	public Intersections(double d, double e, double t) {
		this.x = d;
		this.y = e;
		this.offset = t;
	}

	public double getOffset() {
		return this.offset;
	}

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	@Override
	public int compareTo(Intersections intersectionPoint) {

		if (intersectionPoint == null || this == null) {
			return 1; // Якщо intersectionPoint - null, то this більший
		}

		if (this.getOffset() > intersectionPoint.getOffset()) {
			return 1;
		} else if (this.getOffset() == intersectionPoint.getOffset()) {
			return 0;
		} else {
			return -1;
		}
	}

}
