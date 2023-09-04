package selfDrivingCar;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

public class Sensor {
	public double rayLength;
	public ArrayList<CarRay> rays;
	public ArrayList<Intersections> readings;
	public ArrayList<Double> offsets;
	public static Stroke plainStroke = new BasicStroke(2);
	public int raysCount;
	public double raysSpreadAngle;

	public Sensor(int x, int y, int raysCount, double angle, double spreadAngle, int height) {
		this.raysCount = raysCount;
		this.rayLength = height * 3;
		this.raysSpreadAngle = spreadAngle;
		this.rays = new ArrayList<CarRay>();
		this.readings = new ArrayList<Intersections>();
		this.offsets = new ArrayList<Double>();
		castCarRays(x, y, angle, this.raysCount, this.raysSpreadAngle, this.rayLength, this.rays);
	}

	public static void castCarRays(int x, int y,double angle, int raysCount, double raysSpreadAngle, double rayLength, ArrayList<CarRay> rays ) {
		Point centerPoint = new Point(x, y);  
		for (int i = 0; i < raysCount; ++i) {
			double rayAngle = Utils.lerp(raysSpreadAngle / 2, -raysSpreadAngle / 2,
					(raysCount == 1) ? 0.5 : ((double) i / ((double) raysCount - 1))) - angle - Math.PI / 2;
			double rayEndX = x - Math.sin(rayAngle) * rayLength;
			double rayEndY = y - Math.cos(rayAngle) * rayLength;
			Point rayEndPoint = new Point(rayEndX, rayEndY);
			if (rays.size()==0) {
				rays.add(new CarRay(centerPoint, rayEndPoint, null));
			} else if (rays.size() == i) {
				rays.add(new CarRay(centerPoint, rayEndPoint, null));
			} else {
				rays.get(i).startPoint = centerPoint;
				rays.get(i).endPoint = rayEndPoint;
				rays.get(i).intersectionPoint = null;
			}
		}
	}

	public static void getReading(Car car, Road road, CopyOnWriteArrayList<Car> traffic) {
		for (int k = 0; k < car.sensor.rays.size(); ++k) {
			ArrayList<Intersections> touches = new ArrayList<Intersections>();
			for (int i = 0; i < road.roadMiddleLaneCoordsList.get(0).size() -
					1; i += 2) {
				for (int j = car.roadListYIndexAreaMin; j < car.roadListYIndexAreaMax; ++j) {
					Intersections touch = Utils.getIntersection(car.sensor.rays.get(k).startPoint.x,
							car.sensor.rays.get(k).startPoint.y,
							car.sensor.rays.get(k).endPoint.x, car.sensor.rays.get(k).endPoint.y,
							road.roadMiddleLaneCoordsList.get(j).get(i),
							road.roadMiddleLaneCoordsList.get(j).get(i + 1),
							road.roadMiddleLaneCoordsList.get(j + 1).get(i),
							road.roadMiddleLaneCoordsList.get(j + 1).get(i + 1));
					if (touch != null) {
						touches.add(touch);
					}
				}
			}
			for (int i = 0; i < traffic.size(); ++i) {

				for (int j = 0; j < traffic.get(i).xDotsPolygonCoords.length; ++j) {
					Intersections touch = Utils.getIntersection(car.sensor.rays.get(k).startPoint.x,
							car.sensor.rays.get(k).startPoint.y,
							car.sensor.rays.get(k).endPoint.x, car.sensor.rays.get(k).endPoint.y,
							traffic.get(i).xDotsPolygonCoords[j],
							traffic.get(i).yDotsPolygonCoords[j],
							traffic.get(
									i).xDotsPolygonCoords[j == traffic.get(i).xDotsPolygonCoords.length - 1
											? 0
											: j + 1],
							traffic.get(i).yDotsPolygonCoords[j == traffic
									.get(i).yDotsPolygonCoords.length - 1 ? 0 : j + 1]);
					if (touch != null) {
						touches.add(touch);
					}
				}
			}
			if (touches.size() == 0) {
				car.sensor.rays.get(k).intersectionPoint = null;
			} else {
				car.sensor.rays.get(k).intersectionPoint = Collections.min(touches);
			}
		}

	}

	public void paint(Graphics2D g) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setStroke(plainStroke);
		for (CarRay rayLine : rays) {
			if (rayLine.intersectionPoint != null) {
				// g2d.setColor(Color.GREEN);
				// g2d.drawLine((int) rayLine.startPoint.x, (int) rayLine.startPoint.y, (int)
				// rayLine.endPoint.x,
				// (int) rayLine.endPoint.y);
				g2d.setColor(Color.YELLOW);
				g2d.drawLine((int) rayLine.startPoint.x, (int) rayLine.startPoint.y,
						(int) rayLine.intersectionPoint.getX(),
						(int) rayLine.intersectionPoint.getY());
			} else {
				g2d.setColor(Color.YELLOW);
				g2d.drawLine((int) rayLine.startPoint.x, (int) rayLine.startPoint.y, (int) rayLine.endPoint.x,
						(int) rayLine.endPoint.y);
			}

		}
		// g2d.dispose();

	}
}
