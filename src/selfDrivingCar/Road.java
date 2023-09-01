package selfDrivingCar;

import java.awt.*;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.Random;

public class Road {
	public int x;
	public int roadWidth;
	public int roadHeight;
	public int lanesCount;
	public int infinity;
	public double curveAmplitude;
	public ArrayList<ArrayList<Double>> roadMiddleLaneCoordsList;
	public int[][] roadMiddleLaneDrawCoordsArray;
	public int top;
	public int dashsegment = 60;
	public Stroke dashed = new BasicStroke(10, BasicStroke.JOIN_ROUND,
			BasicStroke.JOIN_BEVEL,
			0, new float[] { dashsegment, dashsegment / 2 }, 0);
	public Stroke plainStroke = new BasicStroke(10);
	public int bottom;
	public double curveFrequency;
	public int curveLength;
	public BufferedImage roadImage;
	public int firstTurnRight;
	public double[] roadLinesX;
	public double laneWidth;
	public double waves;

	public Road(int canvasWidth, int carWidth) {
		Random random = new Random();
		this.roadWidth = (int) Math.floor(canvasWidth * 0.9);
		this.x = (int) Math.floor(canvasWidth / 2);
		this.lanesCount = (int) Math.floor(this.roadWidth / (carWidth * 2.24));
		this.laneWidth = this.roadWidth / this.lanesCount;
		this.roadMiddleLaneCoordsList = new ArrayList<ArrayList<Double>>();
		this.curveFrequency = (double) (4.5 * random.nextDouble()) / this.roadWidth;
		this.waves = Math.floor(random.nextDouble() * 21) + 30;
		this.curveAmplitude = random.nextDouble() * laneWidth;
		this.curveLength = (int) Math.floor((waves * 2 * Math.PI) / curveFrequency);
		this.firstTurnRight = (random.nextDouble() > 0.5) ? 1 : -1;
		this.infinity = curveLength + 100;
		this.bottom = 800;
		this.top = -infinity;
		this.roadLinesX = new double[this.lanesCount + 1];

		getRoadPaintedLines(canvasWidth);
		roadImage = new BufferedImage(canvasWidth, (this.bottom + (int) Math.abs(this.top)),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = roadImage.createGraphics();
		int r = 60;
		Stroke dashed = new BasicStroke(10, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_BEVEL,
				0, new float[] { r, r / 2 }, 0);
		Stroke plainStroke = new BasicStroke(10);

		g2d.setColor(Color.WHITE);
		for (int i = 0; i < roadMiddleLaneDrawCoordsArray.length; ++i) {
			if (i > 0 && i < roadMiddleLaneDrawCoordsArray.length - 1) {
				g2d.setStroke(dashed);
				g2d.drawPolyline(roadMiddleLaneDrawCoordsArray[(i - 1) * 2],
						roadMiddleLaneDrawCoordsArray[(i - 1) * 2 + 1],
						roadMiddleLaneDrawCoordsArray[0].length);
			} else {
				g2d.setStroke(plainStroke);
				g2d.drawLine((int) roadLinesX[i], 0, (int) roadLinesX[i],
						infinity + this.bottom);
			}
		}

	}

	private void getRoadPaintedLines(int canvasWidth) {

		int firstLineX = x - roadWidth / 2;
		int lastLineX = x + roadWidth / 2;
		for (int i = 0; i < (lanesCount + 1); ++i) {
			if (i == 0) {
				roadLinesX[i] = firstLineX;
			} else if (i == lanesCount) {
				roadLinesX[i] = lastLineX;
			} else {
				roadLinesX[i] = firstLineX + i * laneWidth;
			}
		}

		for (int st = 0; st < 2; ++st) {
			ArrayList<Double> step = new ArrayList<Double>();
			for (int k = 1; k < roadLinesX.length - 1; ++k) {
				step.add(Double.valueOf(roadLinesX[k]));
				if (st == 0) {
					step.add(Double.valueOf(bottom));
				} else {
					step.add(1.0);
				}
			}
			roadMiddleLaneCoordsList.add(step);
		}

		int stepRoadArrayLength = (lanesCount - 1) * 2;
		for (int y = 0; y > -curveLength; y--) {
			ArrayList<Double> roadStep = new ArrayList<Double>();
			for (int j = 0; j < stepRoadArrayLength; ++j) {
				if (j % 2 == 0) {
					Double x = firstTurnRight * Math.sin(y * curveFrequency) * curveAmplitude;
					roadStep.add(x + roadLinesX[(j / 2) + 1]);
				} else {
					roadStep.add(Double.valueOf(y));
				}
			}

			roadMiddleLaneCoordsList.add(roadStep);
		}

		for (int st = 0; st < 2; ++st) {
			ArrayList<Double> step = new ArrayList<Double>();
			for (int k = 1; k < roadLinesX.length - 1; ++k) {
				step.add(Double.valueOf(roadLinesX[k]));
				if (st == 0) {
					step.add(Double.valueOf(-curveLength - 1));
				} else {
					step.add(Double.valueOf(top));
				}
			}
			roadMiddleLaneCoordsList.add(step);
		}
		// Utils.linesWriter(roadMiddleLaneCoordsList, "img/inputs3.txt");

		roadMiddleLaneDrawCoordsArray = new int[roadMiddleLaneCoordsList.get(0)
				.size()][roadMiddleLaneCoordsList.size()];
		for (int step = 0; step < roadMiddleLaneCoordsList.size(); ++step) {
			for (int stepValue = 0; stepValue < roadMiddleLaneCoordsList.get(0).size(); ++stepValue) {
				if (stepValue % 2 == 0) {
					roadMiddleLaneDrawCoordsArray[stepValue][step] = (int) Math
							.round(roadMiddleLaneCoordsList.get(step).get(stepValue));
				} else {
					roadMiddleLaneDrawCoordsArray[stepValue][step] = (int) (Math
							.round(roadMiddleLaneCoordsList.get(step).get(stepValue)) + infinity);
				}
				;
			}
		}

	}

	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		// g2d.setColor(Color.WHITE);

		// g2d.setStroke(plainStroke);
		// g2d.drawLine((int) roadLinesX[0], top, (int) roadLinesX[0],
		// bottom);
		// g2d.setStroke(dashed);
		// g2d.translate(0, top);
		// g2d.drawPolyline(roadMiddleLaneDrawCoordsArray[0],
		// roadMiddleLaneDrawCoordsArray[1], roadMiddleLaneCoordsList.size());
		// g2d.drawPolyline(roadMiddleLaneDrawCoordsArray[2],
		// roadMiddleLaneDrawCoordsArray[3],
		// roadMiddleLaneCoordsList.size());
		// g2d.translate(0, -top);
		// g2d.setStroke(plainStroke);
		// g2d.drawLine((int) roadLinesX[3], top, (int) roadLinesX[3],
		// bottom);
		// g2d.setStroke(plainStroke);

		// for (int i = 0; i <roadMiddleLaneDrawCoordsArray.length ; ++i) {
		// if (i > 0 && i < roadMiddleLaneDrawCoordsArray.length - 1) {
		// g2d.setStroke(dashed);
		// g2d.drawPolyline(roadMiddleLaneDrawCoordsArray[(i - 1) * 2],
		// roadMiddleLaneDrawCoordsArray[(i - 1) * 2 + 1],
		// roadMiddleLaneDrawCoordsArray[0].length);
		// } else {
		// g2d.setStroke(plainStroke);
		// g2d.drawLine((int) roadLinesX[i], top, (int) roadLinesX[i],
		// bottom);
		// }
		// }
		g2d.drawImage(roadImage, 0, top, null);
		// g2d.dispose();
	}
}
