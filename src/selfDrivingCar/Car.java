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
	public Color normalColor;
	public Color damagedColor;
	public Color currentColor;
	private int freezedFramesMaxCount = 135;
	private int freezedFrames;
	protected int width;
	protected int height;
	public int roadListYIndexAreaMax;
	public int roadListYIndexAreaMin;
	private int toBestCarMaxDistance;
	private double friction = 0.05;
	private double speed;
	private double acceleration = 0.2;
	private double maxSpeed;
	public int[] layersNNetwork;
	public double angle;
	private double angleSpeed;
	private double angleMaxSpeed = 0.09;
	private double angleAcceleration = angleMaxSpeed / 2;
	private double[] offsets;
	public boolean damaged;
	public boolean useBrain;
	public boolean humanDrives;
	private int raysCount;
	private double raysSpreadAngle;
	public int[] xDotsPolygonCoords;
	public int[] yDotsPolygonCoords;
	public int[] xDotsCachedPolygonCoords;
	public int[] yDotsCachedPolygonCoords;
	public Sensor sensor;
	public Image carImage;
	public Image bestCarImage;
	public NNetwork brain;
	private Controls controls;

	public Car(int x, int y, int carWidth, int carHeight, int raysCount, double raysSpreadAngle,
			int carDecisionCount, String NNLayersInput, String controlType, double maxSpeed, Color color,
			gameSelfDrivingCar gameclass) {
		this.x = x;
		this.roadListYIndexAreaMax = 0;
		this.roadListYIndexAreaMin = 0;
		this.x1 = this.x;
		this.y = y;
		this.bestCar = false;
		this.normalColor = color;
		this.currentColor = this.normalColor;
		this.damagedColor = Color.red;
		this.raysCount = raysCount;
		this.raysSpreadAngle = raysSpreadAngle;
		this.y1 = this.y;
		this.angle = -Math.PI / 2;
		this.freezedFrames = 0;
		this.angleSpeed = 0;
		this.width = carWidth;
		this.height = carHeight;
		this.xDotsPolygonCoords = new int[4];
		this.yDotsPolygonCoords = new int[4];
		this.xDotsCachedPolygonCoords = new int[4];
		this.yDotsCachedPolygonCoords = new int[4];
		this.createPolygon();
		this.speed = 0;
		this.maxSpeed = maxSpeed;
		this.toBestCarMaxDistance = this.height * 10;
		this.damaged = false;
		this.useBrain = controlType.equalsIgnoreCase("AI");
		this.humanDrives = controlType.equalsIgnoreCase("KEYS");

		ArrayList<Integer> NNLayers = new ArrayList<Integer>();
		if (!NNLayersInput.equals("")) {
			NNLayers.add(raysCount + 1);
			String[] sttringToArray = NNLayersInput.split(",");
			for (String number : sttringToArray) {
				NNLayers.add(Integer.parseInt(number));
			}
			;
			NNLayers.add(carDecisionCount);
		} else {
			NNLayers.add(raysCount + 1);
			NNLayers.add(carDecisionCount);
		}
		layersNNetwork = new int[NNLayers.size()];
		layersNNetwork = NNLayers.stream().mapToInt(d -> d).toArray();
		if (!controlType.equalsIgnoreCase("DUMMY")) {
			this.sensor = new Sensor(this.x, this.y, this.raysCount, this.angle, this.raysSpreadAngle, this.height);
			this.brain = new NNetwork(layersNNetwork, gameclass.FLOP);
		}
		this.controls = new Controls(controlType);
		this.carImage = imageProcessor.getImage(this.currentColor, gameclass.random).getScaledInstance((int) carWidth,
				(int) carHeight, Image.SCALE_DEFAULT);
		this.bestCarImage = imageProcessor.getImage(Color.GREEN, gameclass.random).getScaledInstance((int) carWidth,
				(int) carHeight, Image.SCALE_DEFAULT);
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

	public void carMove(int keyevent, Road road, CopyOnWriteArrayList<Car> traffic, Car bestCar) {
		currentColor = (!damaged) ? normalColor : damagedColor;
		move(keyevent);
		if (useBrain || humanDrives) {
			getroadArea(road.roadMiddleLaneCoordsList);
			damaged = accessDamage(road.roadMiddleLaneCoordsList, traffic)
					|| isTotallyFreezed(isFreezed()) || isOutsider(bestCar);
		}
		if (sensor != null) {
			sensor.castCarRays(x, y, angle);
			sensor.getReading(this, road, traffic);
			offsets = new double[layersNNetwork[0]];
			for (int i = 0; i < offsets.length - 1; ++i) {
				if (sensor.rays.get(i).intersectionPoint == null) {
					offsets[i] = 0;
				} else {
					offsets[i] = 1 - sensor.rays.get(i).intersectionPoint.getOffset();
				}
			}
			offsets[offsets.length - 1] = speed / maxSpeed;
			int[] outputs = new int[brain.NNlevels[brain.NNlevels.length - 1].levelOutputs.length];
			outputs = brain.feedForward(offsets, brain);
			if (useBrain) {
				if (useBrain) {
					controls.forward = (outputs[0] == 0) ? false : true;
					controls.left = (outputs[1] == 0) ? false : true;
					controls.right = (outputs[2] == 0) ? false : true;
					controls.reverse = (outputs[3] == 0) ? false : true;
				}
			}
		}
	}

	private boolean isOutsider(Car bestCar) {
		if (bestCar != null) {
			return !this.bestCar && y - bestCar.y > toBestCarMaxDistance;
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

	private void move(int keyevent) {
		controls.addKeyboardListeners(keyevent);
		if (controls.forward) {
			speed += acceleration;
		} else if (controls.reverse) {
			speed -= acceleration;
		}

		if (speed != 0) {
			int flip = (speed > 0) ? -1 : 1;
			angle -= angleSpeed * flip;
			if (controls.left) {
				angleSpeed -= angleAcceleration;
			} else if (controls.right) {
				angleSpeed += angleAcceleration;
			}
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
		;
		if (angleSpeed > angleMaxSpeed) {
			angleSpeed = angleMaxSpeed;
		} else if (angleSpeed < -angleMaxSpeed) {
			angleSpeed = -angleMaxSpeed;
		}
		;
		if (angleSpeed > 0) {
			angleSpeed -= angleAcceleration / 3;
		} else if (angleSpeed < 0) {
			angleSpeed += angleAcceleration / 3;
		}

		if (Math.abs(angleSpeed) < angleAcceleration / 2) {
			angleSpeed = 0;
		}
		;

		x += (int) Math.round(speed * Math.cos(angle));
		y += (int) Math.round(speed * Math.sin(angle));

		;
		createPolygon();
		updateCachedPolygons();
	}

	private void updateCachedPolygons() {
		xDotsCachedPolygonCoords = xDotsPolygonCoords.clone();
		yDotsCachedPolygonCoords = yDotsPolygonCoords.clone();

	}

	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setColor(currentColor);
		if (sensor != null && (bestCar || humanDrives)) {
			sensor.paint(g2d);
		}
		if (damaged) {
			g2d.setColor(damagedColor);
		}
		g2d.fillPolygon(xDotsPolygonCoords, yDotsPolygonCoords, xDotsPolygonCoords.length);
		g2d.translate(x, y);
		g2d.rotate(angle + Math.PI / 2);
		g2d.translate(-x, -y);
		if (bestCar) {
			g2d.drawImage(bestCarImage, x - (bestCarImage.getWidth(null) /
					2),
					y - (bestCarImage.getHeight(null) / 2), null);
		} else {
			g2d.drawImage(carImage, x - (carImage.getWidth(null) / 2),
					y - (carImage.getHeight(null) / 2), null);
		}

		g2d.translate(x, y);
		g2d.rotate(-angle - Math.PI / 2);
		g2d.translate(-x, -y);

	}
}