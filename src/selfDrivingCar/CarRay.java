package selfDrivingCar;

public class CarRay {
	Point startPoint;
	Point endPoint;
	Intersections intersectionPoint;

	public CarRay(Point startPoint, Point endPoint, Intersections intersectionPoint) {
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		this.intersectionPoint = intersectionPoint;
	}
}
