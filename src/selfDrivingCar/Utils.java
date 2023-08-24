package selfDrivingCar;

import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JOptionPane;

import com.google.gson.Gson;

public class Utils {
	public static int[] arrayListDoubleToArrayintegerConverter(ArrayList<Double> ListToConvert,
			ArrayList<Double> cachedListToConvert) {
		Double value = 0.0;
		Double valueCached = 0.0;
		int[] uuu = new int[ListToConvert != null ? ListToConvert.size()
				: cachedListToConvert != null ? cachedListToConvert.size() : 4];
		for (int i = 0; i < uuu.length; i++) {
			if (ListToConvert != null) {
				value = ListToConvert.get(i);
			}
			if (cachedListToConvert != null) {
				valueCached = cachedListToConvert.get(i);
			}

			if (value != null) {
				uuu[i] = (int) Math.floor(value);
			} else {
				uuu[i] = (int) Math.floor(valueCached);
				; // або інше значення за замовчуванням
			}
		}

		// return ListToConvert.stream().mapToInt(value
		// ->(int)Math.floor(value)).toArray();
		return uuu;
	}

	public static void arrayListDysplayer(String arrayListLabel, ArrayList<Integer> carPolygonX) {
		System.out.print("start " + arrayListLabel + ": ");
		for (int i = 0; i < carPolygonX.size(); ++i) {
			System.out.print(carPolygonX.get(i).intValue() + " ");
		}
		System.out.println("finish");
	}

	public static double lerp(double a, double b, double t) {
		return a + (b - a) * t;
	}

	public static void saveGameToFile(gameSelfDrivingCar gameclass) {

		Gson gson = new Gson();
		SaveGame savedGameObject = new SaveGame(gameclass);
		String output = gson.toJson(savedGameObject);

		try (FileWriter file = new FileWriter(gameclass.saveFilePath)) {
			file.write(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// System.out.print(output);
	}

	public static Intersections getIntersection(double Ax, double Ay, double Bx, double By, double Cx, double Cy,
			double Dx, double Dy, gameSelfDrivingCar gameclass) {
		double tTop = (Dx - Cx) * (Ay - Cy) - (Dy - Cy) * (Ax - Cx);
		double uTop = (Cy - Ay) * (Ax - Bx) - (Cx - Ax) * (Ay - By);
		double bottom = (Dy - Cy) * (Bx - Ax) - (Dx - Cx) * (By - Ay);

		if (bottom != 0) {
			double t = tTop / bottom;
			double u = uTop / bottom;
			if (t >= 0 && t <= 1 && u >= 0 && u <= 1) {
				Intersections p5 = new Intersections(lerp(Ax, Bx, t), lerp(Ay, By, t), t);
				// System.out.println("x: " + p5.x + " y: " + p5.y + " offset: " + p5.offset);
				// System.out.println("A.x,y " + A.x+" "+A.y +" B.x,y " + B.x+" "+B.y +" C.x,y "
				// + C.x+" "+C.y +" D.x,y " + D.x+" "+D.y );
				// gameclass.intersectionsCounter++;
				// System.out.println("intersection " + gameclass.intersectionsCounter);
				return p5;
			}
		}
		// System.out.println("No intersection");
		return null;
	}

	public static boolean polysIntersect(Car car,
			ArrayList<ArrayList<Double>> roadBorders, int paintLineX, Car traffBot, gameSelfDrivingCar gameclass) {
		for (int i = 0; i < car.xDotsPolygonCoords.length; ++i) {
			if (roadBorders != null) {
				for (int j = car.roadListYIndexAreaMin; j < car.roadListYIndexAreaMax; ++j) {
					Intersections touch = getIntersection(
							car.xDotsPolygonCoords[i], car.yDotsPolygonCoords[i],
							car.xDotsPolygonCoords[i == car.yDotsPolygonCoords.length - 1 ? 0 : i + 1],
							car.yDotsPolygonCoords[i == car.yDotsPolygonCoords.length - 1 ? 0 : i + 1],
							roadBorders.get(j).get(paintLineX),
							roadBorders.get(j).get(paintLineX + 1),
							roadBorders.get(j + 1).get(paintLineX),
							roadBorders.get(j + 1).get(paintLineX + 1),
							gameclass);

					if (touch != null) {
						return true;
					}
				}
			} else {
				if (car.y + car.sensor.rayLength + car.height / 2 > (traffBot.y - traffBot.height / 2)
						&& car.y - car.sensor.rayLength - car.height / 2 < (traffBot.y + traffBot.height / 2)) {

					for (int j = 0; j < traffBot.xDotsPolygonCoords.length; ++j) {

						Intersections touch = Utils.getIntersection(

								car.xDotsPolygonCoords[i], car.yDotsPolygonCoords[i],
								car.xDotsPolygonCoords[i == car.yDotsPolygonCoords.length - 1 ? 0 : i + 1],
								car.yDotsPolygonCoords[i == car.yDotsPolygonCoords.length - 1 ? 0 : i + 1],

								traffBot.xDotsPolygonCoords[j],
								traffBot.yDotsPolygonCoords[j],
								traffBot.xDotsPolygonCoords[j == traffBot.yDotsPolygonCoords.length - 1 ? 0 : j + 1],
								traffBot.yDotsPolygonCoords[j == traffBot.yDotsPolygonCoords.length - 1 ? 0 : j + 1],
								gameclass);

						if (touch != null) {
							// System.out.println(touch.getX() + " " + touch.getY() + " " +
							// touch.getOffset());
							// System.out.println(" intersected with traffic");
							return true;
						}
					}
				}
			}
		}

		return false;

	}

	public static void linesWriter(ArrayList<ArrayList<Double>> arrayListTobeWritten, String path) {

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(new File(path)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (ArrayList<Double> step : arrayListTobeWritten) {
			for (Double stepArg : step) {
				try {
					writer.write(String.valueOf(stepArg));
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					writer.write(" ");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				writer.write("\r\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Road coords saved");

	}

	public static SaveGame getSavedGameFromFile(gameSelfDrivingCar gameclass) {
		BufferedReader br = null;
		try {
			File gameOnDisk = new File(gameclass.saveFilePath);
			br = new BufferedReader(new FileReader(gameOnDisk));
			Gson gson = new Gson();
			SaveGame savedGame = gson.fromJson(br, SaveGame.class);
			JOptionPane.showMessageDialog(gameclass, "File load success!", gameclass.SCREEN_TITLE,
					JOptionPane.INFORMATION_MESSAGE);
			return savedGame;
		} catch (FileNotFoundException ex) {
			// ex.printStackTrace();
			System.out.print("File not found");
			FileNameExtensionFilter filter = new FileNameExtensionFilter("json files", "json");
			JFileChooser open = new JFileChooser(".");
			open.setDialogTitle("choose correct file with Game saved parameters , .json");
			open.setFileSelectionMode(JFileChooser.FILES_ONLY);
			open.addChoosableFileFilter(filter);
			open.setAcceptAllFileFilterUsed(true);
			int result = open.showOpenDialog(gameclass);
			if (result == JFileChooser.APPROVE_OPTION) {

				File brain_file = new File(open.getSelectedFile().getAbsolutePath());
				try {
					br = new BufferedReader(new FileReader(brain_file));
				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(gameclass, "File not loaded", gameclass.SCREEN_TITLE,
							JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
				Gson gson = new Gson();
				SaveGame savedGame = gson.fromJson(br, SaveGame.class);
				JOptionPane.showMessageDialog(gameclass, "File load success!", gameclass.SCREEN_TITLE,
						JOptionPane.INFORMATION_MESSAGE);
				return savedGame;
			}
			JOptionPane.showMessageDialog(gameclass, "File not loaded", gameclass.SCREEN_TITLE,
					JOptionPane.ERROR_MESSAGE);
			return null;
		}

	}

	// private static float getSigmoid(double number) {
	// return (float) (1 / (1 + Math.exp(-number)));
	// }

	public static double hypeTthan(double number) {
		return (Math.exp(number) - Math.exp(-number)) / (Math.exp(number) + Math.exp(-number));
	}

	public static Color getRGBA(double value) {
		float alpha = (float) Math.abs(hypeTthan(value));
		float R = (float) ((value < 0) ? 0 : 1);
		float G = (float) R;
		float B = (float) ((value > 0) ? 0 : 1);
		// System.out.println("R " + R+" G "+ G+ " B "+ B + " alpha " + alpha);
		return new Color(R, G, B, alpha);
	}

}
