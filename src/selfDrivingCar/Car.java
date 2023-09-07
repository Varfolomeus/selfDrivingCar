package selfDrivingCar;

import java.awt.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Car {
	public int x;
	private int x1;
	public int y;
	private int y1;
	public boolean bestCar;
	private static int freezedFramesMaxCount = 135;
	private int freezedFrames;
	protected static int width;
	public static int height;
	public int roadListYIndexAreaMax;
	public int roadListYIndexAreaMin;
	private static int toBestCarMaxDistance;
	private static double friction = 0.05;
	private double speed;
	private static double acceleration = 0.2;
	public double maxSpeed;
	public static int[] layersNNetwork;
	public double angle;
	private double angleSpeed;
	public static double angleMaxSpeed = 0.034;
	public static double angleAcceleration = angleMaxSpeed / 4;
	public boolean damaged;
	public boolean useBrain;
	public boolean humanDrives;
	private static int raysCount;
	private static double raysSpreadAngle;
	public int[] xDotsPolygonCoords;
	public int[] yDotsPolygonCoords;
	public Sensor sensor;
	public Image carImage;
	public static Image damagedCarImage;
	public static Image bestCarImage;
	public NNetwork brain;
	private Controls controls;
	public static int canvasWidth;

	public Car(int x, int y, int carWidth, int carHeight, int raysCountToSet, double raysSpreadAngleToSet,
			int carDecisionCount, int[] layersNNetworkToSet, String controlType, double maxSpeedToSet,
			int canvasWidthToSet, double flop, Image damagedCarImageToSet, Image bestCarImageToSet, Image carImage) {
		this.x = x;
		this.roadListYIndexAreaMax = 0;
		this.roadListYIndexAreaMin = 0;
		this.x1 = this.x;
		this.y = y;
		this.bestCar = false;
		this.y1 = this.y;
		this.angle = -Math.PI / 2;
		this.freezedFrames = 0;
		this.angleSpeed = 0;
		if (width != carWidth) {
			width = carWidth;
			height = carHeight;
			toBestCarMaxDistance = height * 10;
			raysCount = raysCountToSet;
			raysSpreadAngle = raysSpreadAngleToSet;
			canvasWidth = canvasWidthToSet;
			damagedCarImage = damagedCarImageToSet;
			bestCarImage = bestCarImageToSet;
		}
		if (!Arrays.equals(layersNNetworkToSet, layersNNetwork)) {
			layersNNetwork = layersNNetworkToSet;
		}
		this.xDotsPolygonCoords = new int[4];
		this.yDotsPolygonCoords = new int[4];
		this.maxSpeed = maxSpeedToSet;
		this.createPolygon();
		this.speed = 0;
		this.damaged = false;
		this.useBrain = controlType.equalsIgnoreCase("AI");
		this.humanDrives = controlType.equalsIgnoreCase("KEYS");
		if (!controlType.equalsIgnoreCase("DUMMY")) {
			this.sensor = new Sensor(this.x, this.y, raysCount, this.angle, raysSpreadAngle, height);
			this.brain = new NNetwork(layersNNetwork, flop);
			this.carImage = carImage;
		} else {
			this.carImage = imageProcessor.getImage(null).getScaledInstance((int) carWidth,
					(int) carHeight, Image.SCALE_DEFAULT);
		}
		this.controls = new Controls(controlType);

	}

	protected void createPolygon() {
		double rad = Math.hypot(width, height) / 2;
		double alpha = Math.atan2(height, width);

		xDotsPolygonCoords[0] = (int) Math.round(x - Math.sin(Math.PI - angle - alpha) * rad);
		yDotsPolygonCoords[0] = (int) Math.round(y - Math.cos(Math.PI - angle - alpha) * rad);
		xDotsPolygonCoords[1] = (int) Math.round(x - Math.sin(Math.PI - angle + alpha) * rad);
		yDotsPolygonCoords[1] = (int) Math.round(y - Math.cos(Math.PI - angle + alpha) * rad);
		xDotsPolygonCoords[2] = (int) Math.round(x - Math.sin(-angle - alpha) * rad);
		yDotsPolygonCoords[2] = (int) Math.round(y - Math.cos(-angle - alpha) * rad);
		xDotsPolygonCoords[3] = (int) Math.round(x - Math.sin(-angle + alpha) * rad);
		yDotsPolygonCoords[3] = (int) Math.round(y - Math.cos(-angle + alpha) * rad);
	}

	public void carMove(int keyeventX, int keyeventY, Road road, CopyOnWriteArrayList<Car> traffic, Car bestCar) {
		move(keyeventX, keyeventY);
		if (useBrain || humanDrives) {
			getroadArea(road.roadMiddleLaneCoordsList);
			damaged = accessDamage(road.roadMiddleLaneCoordsList, traffic)
					|| isTotallyFreezed(isFreezed()) || isOutsider(bestCar);
		}
		if (sensor != null) {
			Sensor.castCarRays(x, y, angle, raysCount, raysSpreadAngle, this.sensor.rayLength, this.sensor.rays);
			Sensor.getReading(this, road, traffic);
			for (int i = 0; i < brain.NNlevels[0].levelInputs.length - 1; ++i) {
				if (sensor.rays.get(i).intersectionPoint == null) {
					brain.NNlevels[0].levelInputs[i] = 0;
				} else {
					brain.NNlevels[0].levelInputs[i] = 1 - sensor.rays.get(i).intersectionPoint.getOffset();
				}
			}
			brain.NNlevels[0].levelInputs[brain.NNlevels[0].levelInputs.length - 1] = speed / maxSpeed;
			brain.feedForward();
			if (useBrain) {
				if (useBrain) {
					controls.forward = (brain.NNlevels[brain.NNlevels.length - 1].levelOutputs[0] == 0.0) ? false
							: true;
					controls.left = (brain.NNlevels[brain.NNlevels.length - 1].levelOutputs[1] == 0.0) ? false : true;
					controls.right = (brain.NNlevels[brain.NNlevels.length - 1].levelOutputs[2] == 0.0) ? false : true;
					controls.reverse = (brain.NNlevels[brain.NNlevels.length - 1].levelOutputs[3] == 0.0) ? false
							: true;
				}
			}
		}
	}

	private boolean isOutsider(Car bestCarToCompare) {
		if (bestCarToCompare != null) {
			return !bestCar && (y - bestCarToCompare.y > toBestCarMaxDistance || x < 0 || x > canvasWidth);
		} else {
			return false;
		}
	}

	private boolean isTotallyFreezed(boolean freezed) {
		if (freezed) {
			freezedFrames++;
		} else {
			freezedFrames = 0;
		}
		;
		if (freezedFrames >= freezedFramesMaxCount) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isFreezed() {

		if (x == x1 && y == y1) {
			return true;
		}
		x1 = x;
		y1 = y;
		return false;
	}

	private void getroadArea(ArrayList<ArrayList<Double>> roadMiddleLaneCoordsList) {
		double yInListMin = y + sensor.rayLength + height;
		double yInListMax = y - sensor.rayLength - height;
		roadListYIndexAreaMax = roadMiddleLaneCoordsList.size() - 1;
		roadListYIndexAreaMin = 0;
		int currentIndexmaxY = roadListYIndexAreaMax;
		int currentIndexminY = roadListYIndexAreaMin; // Indexes of List to use in intersections searches
		int currentIndexmiddleY = 0;
		double target = 0;

		for (int i = 0; i < 2; ++i) {
			int storedIndex = 0;
			if (i == 0) {
				target = yInListMin;
				currentIndexminY = roadListYIndexAreaMin;
			} else {
				target = yInListMax;
				storedIndex = 0;
				currentIndexmaxY = roadListYIndexAreaMax;
			}
			while (currentIndexmaxY != currentIndexminY) {
				currentIndexmiddleY = (currentIndexmaxY + currentIndexminY) / 2;
				double middleValueY = roadMiddleLaneCoordsList.get(currentIndexmiddleY).get(1);
				if (target < middleValueY) {
					currentIndexminY = currentIndexmiddleY;

				} else if (target > middleValueY) {
					currentIndexmaxY = currentIndexmiddleY;
				} else {
					currentIndexmaxY = currentIndexmiddleY;
					currentIndexminY = currentIndexmaxY;
				}
				if (storedIndex == currentIndexmiddleY) {
					currentIndexmaxY = currentIndexmiddleY;
					currentIndexminY = currentIndexmaxY;
				}
				storedIndex = currentIndexmiddleY;
			}
			if (i == 0) {
				roadListYIndexAreaMin = currentIndexmiddleY;
			} else {
				roadListYIndexAreaMax = currentIndexmiddleY;
			}
			if (roadListYIndexAreaMax == 0 && roadListYIndexAreaMin == 0) {
				roadListYIndexAreaMax += 1;
			}
			if (roadListYIndexAreaMax == roadMiddleLaneCoordsList.size() - 2
					&& roadListYIndexAreaMin == roadMiddleLaneCoordsList.size() - 2) {
				roadListYIndexAreaMax += 1;
			}
		}
	}

	private boolean accessDamage(ArrayList<ArrayList<Double>> roadBorders, CopyOnWriteArrayList<Car> traffic) {

		for (int paintLineX = 0; paintLineX < roadBorders.get(0).size(); paintLineX += 2) {
			if (Utils.polysIntersect(this, roadBorders, paintLineX,
					null)) {
				return true;
			}
		}
		if (traffic.size() > 0) {
			for (Car traffBot : traffic) {
				if (Utils.polysIntersect(this, null, 0,
						traffBot)) {
					return true;
				}
			}
		}
		return false;
	}

	private void move(int xToGet, int yToGet) {
		controls.addKeyboardListeners(x, y, angle, speed, maxSpeed, xToGet, yToGet);
		if (controls.forward) {
			speed += acceleration;
		} else if (controls.reverse) {
			speed -= acceleration;
		}

		if (speed > maxSpeed) {
			speed = maxSpeed;
		}
		if (speed < -maxSpeed / 2) {
			speed = -maxSpeed / 2;
		}
		if (speed > 0) {
			speed -= friction;
		}
		if (speed < 0) {
			speed += friction;
		}
		if (Math.abs(speed) < friction) {
			speed = 0;
		}
		if (angleSpeed > angleMaxSpeed) {
			angleSpeed = angleMaxSpeed;
		} else if (angleSpeed < -angleMaxSpeed) {
			angleSpeed = -angleMaxSpeed;
		}

		if (speed != 0) {
			int flip = (speed > 0) ? -1 : 1;
			if (controls.left) {
				angleSpeed -= angleAcceleration;
			} else if (controls.right) {
				angleSpeed += angleAcceleration;
			}
			angle -= angleSpeed * flip;
		}

		if (controls.left == false && controls.right == false) {
			angleSpeed = 0;
		}

		x += (int) Math.round(speed * Math.cos(angle));
		y += (int) Math.round(speed * Math.sin(angle));
		createPolygon();
	}

	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		// g2d.setColor(currentColor);
		if (sensor != null && (bestCar || humanDrives)) {
			sensor.paint(g2d);
		}
		// if (damaged) {
		// g2d.setColor(damagedColor);
		// }
		// g2d.fillPolygon(xDotsPolygonCoords, yDotsPolygonCoords,
		// xDotsPolygonCoords.length);
		g2d.translate(x, y);
		g2d.rotate(angle + Math.PI / 2);
		g2d.translate(-x, -y);
		if (bestCar) {
			g2d.drawImage(bestCarImage, x - (bestCarImage.getWidth(null) /
					2),
					y - (bestCarImage.getHeight(null) / 2), null);
		} else if (damaged) {
			g2d.drawImage(damagedCarImage, x - (damagedCarImage.getWidth(null) / 2),
					y - (damagedCarImage.getHeight(null) / 2), null);
		} else {
			g2d.drawImage(carImage, x - (carImage.getWidth(null) / 2),
					y - (carImage.getHeight(null) / 2), null);
		}

		g2d.translate(x, y);
		g2d.rotate(-angle - Math.PI / 2);
		g2d.translate(-x, -y);

	}
}