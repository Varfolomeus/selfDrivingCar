package selfDrivingCar;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import javax.swing.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class gameSelfDrivingCar extends JFrame {

	private static final long serialVersionUID = -8258954113971711416L;
	public final String SCREEN_TITLE = "Game: Self-driving car...";
	public final String CAME_OVER = "Game over, will restart in a few moments";
	public static final String saveFilePath = "img/savedBrain.json";
	private static JFrame gameFrame;
	private static JPanel paramsGamePanel;
	private static JPanel viewImageGamePanel;
	private static Container container;
	public static boolean userWantsToPlay;
	public static double botMaxSpeed;
	public static double dummyMaxSpeed;
	public static double humanBotMaxSpeed;
	public static int WINDOW_HEIGHT = 1000;
	public static int WINDOW_WIDTH = 700;
	final static int START_LOCATION_X = 100;
	final static int START_LOCATION_Y = 100;
	public static JComboBox<String> comboBox;
	public static JTextField inputField;
	public static JRadioButton yesOption;
	public static JRadioButton noOption;
	public static NNetwork savedBrain;
	public static int sleepTime = 40;
	public static int RAYS_COUNT = 13;
	public static double RAYS_SPREAD_ANGLE_MULTIPLIER = 1.75;
	public static double RAYS_SPREAD_ANGLE;
	public static int CarsNumber = 10;
	public static volatile int userKeyEventX = 0;
	public static volatile int userKeyEventY = 0;
	public static int[] layersNNetwork;
	public long startTime;
	final static int CAR_DECISIONS_COUNT = 4;
	final static String SAVE_FILE_EXT = ".brain";
	public static Double FLOP = 0.7;
	public static String NNLayersInput = "7,7";
	public static double mutationStep = 0.43;
	public static boolean chainedMutations = false;
	public static int CAR_CANVAS_HEIGHT;
	public static int CAR_CANVAS_WIDTH;
	public static int NN_CANVAS_HEIGHT;
	public static int NN_CANVAS_WIDTH;
	public static int carWidth;
	public static int carHeight;
	public static Boolean isGameOver = false;
	public static Color carStartColor = new Color(200, 100, 30);
	public static CopyOnWriteArrayList<Car> cars;
	public static CopyOnWriteArrayList<Car> traffic;
	public static volatile Car bestCar;
	public static volatile Car userCar;
	private static Image carImage;
	private static Image damagedCarImage;
	private static Image bestCarImage;
	private static Image userCarImage;
	private static volatile int YBestCar;
	public static SaveGame savedGame;
	public static CarCanvas carCanvas;
	public static boolean mutations = false;
	public static boolean reloadRoadIfRestart = false;
	public static boolean useSavedBrain = false;
	public static int trafficCascades;
	public static NetworkCanvas networkCanvas;
	public static Road road;
	public static boolean gameReloading;
	private static long currentTime;
	private static long savedTime;
	private static long currentFrameTime;
	private static long savedFrameTime;
	private static long frameTime;

	public static void main(String[] args) {
		new gameSelfDrivingCar().go();
	}

	void go() {
		gameFrame = new JFrame();
		gameFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		gameFrame.setTitle(SCREEN_TITLE);
		userWantsToPlay = false;
		container = gameFrame.getContentPane();
		paramsGamePanel = new JPanel();
		viewImageGamePanel = new JPanel();
		yesOption = new JRadioButton("Yes");
		noOption = new JRadioButton("No");
		botMaxSpeed = 2;
		dummyMaxSpeed = 3;
		humanBotMaxSpeed = 3;
		getSavedGame();
		if (savedGame == null) {
			comboBox = new JComboBox<String>(new String[] { "no saved data", "save game once", "press 's' " });
		} else {
			comboBox = new JComboBox<String>(getMenuItems());
		}
		comboBox.setBounds(10, 0, 150, 31);
		inputField = new JTextField();
		inputField.setBounds(comboBox.getWidth() + 10, 0, 50, 32);
		yesOption.setBounds(comboBox.getWidth() + 10, 0, 48, 15);
		noOption.setBounds(comboBox.getWidth() + 10, yesOption.getHeight() + 2, 48, 15);
		gameReloading = false;
		int[] layersNNetworkToSet;
		if (!NNLayersInput.equals("")) {
			int[] tempArray = Arrays.stream(NNLayersInput.split(",")).mapToInt(d -> Integer.parseInt(d)).toArray();
			layersNNetworkToSet = new int[tempArray.length + 2];
			layersNNetworkToSet[0] = RAYS_COUNT + 1;
			layersNNetworkToSet[layersNNetworkToSet.length - 1] = CAR_DECISIONS_COUNT;
			System.arraycopy(tempArray, 0, layersNNetworkToSet, 1, tempArray.length);
		} else {
			layersNNetworkToSet = new int[] { RAYS_COUNT + 1, CAR_DECISIONS_COUNT };
		}
		boolean ttttttt = Arrays.equals(layersNNetwork, layersNNetworkToSet);
		if (!ttttttt) {
			layersNNetwork = layersNNetworkToSet.clone();
			savedBrain = null;
			useSavedBrain = false;
			mutations = false;
			chainedMutations = false;
		}
		startTime = System.currentTimeMillis();
		CAR_CANVAS_HEIGHT = WINDOW_HEIGHT - 34;
		CAR_CANVAS_WIDTH = (int) Math.floor(WINDOW_WIDTH * 0.5);
		NN_CANVAS_HEIGHT = WINDOW_HEIGHT - 34;
		NN_CANVAS_WIDTH = (int) Math.floor(WINDOW_WIDTH * 0.45);
		carWidth = (int) Math.floor(CAR_CANVAS_WIDTH / 8);
		carHeight = (int) Math.floor((double) 50 / 30 * carWidth);
		gameFrame.setBounds(START_LOCATION_X, START_LOCATION_Y, WINDOW_WIDTH, WINDOW_HEIGHT + 52);
		carImage = imageProcessor.getImage(Color.BLUE).getScaledInstance((int) carWidth,
				(int) carHeight, Image.SCALE_DEFAULT);
		damagedCarImage = imageProcessor.getImage(Color.RED).getScaledInstance((int) carWidth,
				(int) carHeight, Image.SCALE_DEFAULT);
		bestCarImage = imageProcessor.getImage(Color.GREEN).getScaledInstance((int) carWidth,
				(int) carHeight, Image.SCALE_DEFAULT);
		userCarImage = imageProcessor.getImage(carStartColor).getScaledInstance((int) carWidth,
				(int) carHeight, Image.SCALE_DEFAULT);
		container.setLayout(null);
		cars = new CopyOnWriteArrayList<Car>();
		traffic = new CopyOnWriteArrayList<Car>();
		Random random = new Random();
		optionsRadioButtonsSetup(this);
		comboBoxSetup(this);
		inputFieldSetup(this);
		carCanvas = new CarCanvas();
		viewImageGamePanelSetup();
		road = new Road(CAR_CANVAS_WIDTH, carWidth);
		trafficCascades = random.nextInt(35) + 5;
		carCanvas.setBounds(0, 0, CAR_CANVAS_WIDTH, CAR_CANVAS_HEIGHT);
		networkCanvas = new NetworkCanvas();
		networkCanvas.setBounds(carCanvas.getWidth() + 8, 0, NN_CANVAS_WIDTH, NN_CANVAS_HEIGHT);
		paramsGamePanel.setLayout(null);
		viewImageGamePanel.setLayout(null);
		viewImageGamePanel.add(carCanvas);
		viewImageGamePanel.add(networkCanvas);
		int withForOptionPanel = (comboBox.getWidth() + 10 + inputField.getWidth() + 15) > (comboBox.getWidth() + 10
				+ yesOption.getWidth() + 15)
						? (comboBox.getWidth() + inputField.getWidth() + 15)
						: comboBox.getWidth() + 10 + noOption.getWidth() + 15;
		paramsGamePanel.setBounds(4, 4, withForOptionPanel, inputField.getHeight());
		viewImageGamePanel.setBounds(4, paramsGamePanel.getHeight() + 4,
				carCanvas.getWidth() + networkCanvas.getWidth(),
				networkCanvas.getHeight());
		paramsGamePanel.add(comboBox);
		paramsGamePanel.add(inputField);
		container.add(paramsGamePanel);
		container.add(viewImageGamePanel);
		gameFrame.setResizable(true);
		gameFrame.setVisible(true);
		savedTime = System.currentTimeMillis();
		savedFrameTime = savedTime;
		cars = generateCars(CarsNumber, mutations);

		traffic = generateTraffic(trafficCascades);
		container.requestFocus();

		// int numberOfCores = Runtime.getRuntime().availableProcessors();
		// ExecutorService executorService =
		// Executors.newFixedThreadPool(numberOfCores);
		ExecutorService executorService = Executors.newCachedThreadPool();

		while (!isGameOver) {
			ArrayList<Future<?>> trafficFutures = new ArrayList<>();
			ArrayList<Future<?>> carsFutures = new ArrayList<>();

			for (Car carObj : cars) {
				carsFutures.add(executorService.submit((Runnable) () -> {
					if (!carObj.damaged || carObj.humanDrives) {
						carObj.carMove(userKeyEventX, userKeyEventY, road, traffic, bestCar == null ? null : bestCar);
					}
				}));

			}
			currentTime = System.currentTimeMillis();

			if (userKeyEventX != 0 && userKeyEventY == 0 && currentTime - savedTime > 500L) {
				userKeyEventX = 0;
				userKeyEventY = 0;
				savedTime = currentTime;
			}
			for (Car trafficObj : traffic) {
				trafficFutures.add(executorService.submit((Runnable) () -> {
					trafficObj.carMove(0, 0, null, null, null);
				}));
			}

			for (Future<?> future : trafficFutures) {
				try {
					future.get();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			for (Future<?> future : carsFutures) {
				try {
					future.get();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			getCurrentBestCar();

			carCanvas.updateCanvas();
			networkCanvas.updateCanvas(bestCar.brain);
			filterDamaged();
			if (gameReloading) {
				restartGame();
				gameReloading = !gameReloading;
			}
			currentFrameTime = System.currentTimeMillis();
			frameTime = currentFrameTime - savedFrameTime;
			;
			try {
				Thread.sleep((frameTime > sleepTime ? (sleepTime > 20 ? sleepTime : 20) : 20));
			} catch (InterruptedException e1) {
			}
			savedFrameTime = currentFrameTime;

		}
		executorService.shutdown();
	}

	private void getCurrentBestCar() {
		YBestCar = road.bottom;
		cars.forEach(car -> {
			car.bestCar = false;
			if (car.y < YBestCar && !car.humanDrives && car.x > 0 && car.x < CAR_CANVAS_WIDTH) {
				bestCar = car;
				YBestCar = car.y;
			}
		});
		bestCar.bestCar = true;
		try {
			savedBrain = (NNetwork) bestCar.brain.clone();
		} catch (CloneNotSupportedException e1) {
			e1.printStackTrace();
		}
		;
	}

	private void optionsRadioButtonsSetup(gameSelfDrivingCar gameClass) {
		yesOption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == yesOption) {
					try {
						Field comboBoxActivatedField = gameClass.getClass()
								.getDeclaredField(comboBox.getItemAt(comboBox.getSelectedIndex()));
						if (!yesOption.isSelected()) {
							yesOption.setSelected(true);
						}
						noOption.setSelected(false);
						if (comboBoxActivatedField.get(gameClass) instanceof Boolean) {
							comboBoxActivatedField.set(gameClass, true);
						}
						yesOption.setFocusable(false);
						noOption.setFocusable(false);
						container.requestFocus();
					} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
							| IllegalAccessException ex) {
						// ex.printStackTrace();
					}
				}
			};
		});
		noOption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == noOption) {
					try {
						Field comboBoxActivatedField = gameClass.getClass()
								.getDeclaredField(comboBox.getItemAt(comboBox.getSelectedIndex()));
						if (!noOption.isSelected()) {
							noOption.setSelected(true);
						}
						yesOption.setSelected(false);

						if (comboBoxActivatedField.get(this) instanceof Boolean) {
							comboBoxActivatedField.set(this, false);
						}
						yesOption.setFocusable(false);
						noOption.setFocusable(false);
						container.requestFocus();

					} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
							| IllegalAccessException ex) {
						// ex.printStackTrace();
					}
				}
			};
		});

	}

	private void viewImageGamePanelSetup() {
		container.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int keyEvent = e.getKeyCode();
				checkKeyActions(keyEvent);
				// System.out.println(e.getKeyCode());
			}
		});
		carCanvas.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				checkMouseActions(e.getX(), e.getY());
			}

		});
		carCanvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				checkMouseActions(0, 0);
			}

		});
	}

	private void checkMouseActions(int xEvent, int yEvent) {
		// System.out.println("x: " + xEvent + " y: " + yEvent);
		userKeyEventX = xEvent;
		userKeyEventY = yEvent;
	}

	private void inputFieldSetup(gameSelfDrivingCar gameClass) {
		inputField.setFocusable(false);

		inputField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (e.getSource() == inputField) {
						String selectedField = comboBox.getItemAt(comboBox.getSelectedIndex());
						Field gameField = gameClass.getClass().getDeclaredField(selectedField);
						gameField.setAccessible(true);
						Object valueToSet = null;
						if (gameField.get(gameClass) instanceof Double) {
							valueToSet = Double.parseDouble(inputField.getText());
						} else if (gameField.get(gameClass) instanceof Integer) {
							valueToSet = Integer.parseInt(inputField.getText());
						} else if (gameField.get(gameClass) instanceof Boolean) {
							valueToSet = Boolean.parseBoolean(inputField.getText());
						} else {
							valueToSet = inputField.getText();
						}
						gameField.set(gameClass, valueToSet);
					}
					inputField.setFocusable(false);
					container.requestFocus();

				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
						| IllegalAccessException e1) {
				}
			}
		});
		inputField.addMouseListener(new MouseAdapter() {
			private long lastClickTime = 0;

			@Override
			public void mouseClicked(MouseEvent e) {
				long currentTime = System.currentTimeMillis();
				if (currentTime - lastClickTime < 500) { // Check if the time between clicks is less than 500
															// milliseconds
					inputField.setFocusable(true); // Make inputField focusable
					inputField.requestFocus(); // Set focus to inputField
				}
				lastClickTime = currentTime;
			}
		});
	}

	private void comboBoxSetup(gameSelfDrivingCar gameClass) {
		comboBox.setFocusable(false);
		comboBox.addActionListener(new ActionListener() {
			String comboBoxSelection;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == comboBox) {
					comboBoxSelection = comboBox.getItemAt(comboBox.getSelectedIndex());
					try {
						Field gameField = gameClass.getClass().getDeclaredField(comboBoxSelection);
						if (gameField.get(gameClass) instanceof Boolean) {
							paramsGamePanel.remove(inputField);
							paramsGamePanel.add(yesOption);
							paramsGamePanel.add(noOption);
							yesOption.setFocusable(true);
							noOption.setFocusable(true);
							yesOption.setSelected((boolean) gameField.get(this));
							noOption.setSelected(!((boolean) gameField.get(this)));

						} else {
							paramsGamePanel.remove(yesOption);
							paramsGamePanel.remove(noOption);
							paramsGamePanel.add(comboBox);
							paramsGamePanel.add(inputField);
							inputField.setFocusable(true);
							inputField.setText(gameField.get(this).toString());
						}
						paramsGamePanel.revalidate();
						paramsGamePanel.repaint();
					} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
							| IllegalAccessException e1) {
						// System.out.println("Проблема з передачею у рамку внесення результату");
						// e1.printStackTrace();
					}
				}
			}
		});
	}

	private String[] getMenuItems() {
		Field[] fields = savedGame.getClass().getDeclaredFields();
		String menuItemsToReturn[] = new String[fields.length];
		for (byte i = 0; i < menuItemsToReturn.length; ++i) {
			menuItemsToReturn[i] = fields[i].getName();
		}

		return menuItemsToReturn;
	}

	private void checkKeyActions(int keyEvent) {
		// System.out.println(keyEvent);
		userKeyEventX = keyEvent;
		userKeyEventY = 0;

		switch (userKeyEventX) {
			case 44:
				// < - 44 - chainedMutations enabled or not +
				chainedMutations = !chainedMutations;
				if (comboBox.getItemAt(comboBox.getSelectedIndex()).equalsIgnoreCase("chainedMutations")) {
					yesOption.setSelected(chainedMutations);
					noOption.setSelected(!chainedMutations);
				}
				break;
			case 76:
				// l - 76 - load best car brain +
				getSavedGame();
				break;
			case 77:
				// m - 77 - mutations enabled or not +
				mutations = !mutations;
				if (comboBox.getItemAt(comboBox.getSelectedIndex()).equalsIgnoreCase("mutations")) {
					yesOption.setSelected(mutations);
					noOption.setSelected(!mutations);
				}
				break;
			case 78:
				// n - 78 - new game +
				gameReloading = true;
				break;
			case 83:
				// s - 83 - save best car brain +
				saveGame();
				break;
			case 85:
				// u - 85 - use saved brain in new car generation --
				useSavedBrain = !useSavedBrain;
				if (comboBox.getItemAt(comboBox.getSelectedIndex()).equalsIgnoreCase("useSavedBrain")) {
					yesOption.setSelected(useSavedBrain);
					noOption.setSelected(!useSavedBrain);
				}
				break;
		}

	}

	private void filterDamaged() {
		cars.removeIf(carObj -> carObj.damaged && !carObj.bestCar && !carObj.humanDrives);
	}

	public CopyOnWriteArrayList<Car> generateCars(int CarsNumber, boolean mutations) {
		if (CarsNumber == 0)
			CarsNumber = 1;
		RAYS_SPREAD_ANGLE = Math.PI * RAYS_SPREAD_ANGLE_MULTIPLIER;
		CopyOnWriteArrayList<Car> carListToBeCreated = new CopyOnWriteArrayList<Car>();

		for (int i = 0; i < CarsNumber; ++i) {
			carListToBeCreated.add(
					new Car((int) Math.floor((double) CAR_CANVAS_WIDTH / 2), 100, carWidth, carHeight, RAYS_COUNT,
							RAYS_SPREAD_ANGLE, CAR_DECISIONS_COUNT, layersNNetwork, "AI", botMaxSpeed, CAR_CANVAS_WIDTH,
							FLOP, damagedCarImage, bestCarImage, carImage));

			if (savedBrain != null && useSavedBrain) {
				if (carListToBeCreated.get(i).useBrain) {
					if (i == 0) {
						try {
							carListToBeCreated.get(i).brain = (NNetwork) savedBrain.clone();
						} catch (CloneNotSupportedException e) {
							e.printStackTrace();
						}
					} else {
						if (mutations && chainedMutations) {
							carListToBeCreated.get(i).brain = carListToBeCreated.get(i).brain.mutate(
									carListToBeCreated.get(i - 1).brain,
									mutationStep);
						} else if (mutations && !chainedMutations) {
							carListToBeCreated.get(i).brain = carListToBeCreated.get(i).brain.mutate(savedBrain,
									mutationStep);
						}
					}
				}
			}

		}
		if (userWantsToPlay) {
			userCar = new Car((int) Math.floor((double) CAR_CANVAS_WIDTH / 2), 100, carWidth, carHeight, RAYS_COUNT,
					RAYS_SPREAD_ANGLE, CAR_DECISIONS_COUNT, layersNNetwork, "KEYS", humanBotMaxSpeed, CAR_CANVAS_WIDTH,
					FLOP, damagedCarImage, bestCarImage, userCarImage);
			carListToBeCreated.add(userCar);
		}
		return carListToBeCreated;
	}

	public CopyOnWriteArrayList<Car> generateTraffic(int cascades) {
		Random random = new Random();
		int carY = 0;
		int roadLineWidth = (int) road.laneWidth;
		int startBotX = (CAR_CANVAS_WIDTH - road.roadWidth + roadLineWidth) / 2;
		CopyOnWriteArrayList<Car> traffic1 = new CopyOnWriteArrayList<Car>();
		for (int i = 0; i <= cascades; i++) {
			if (i == 0)
				carY = -100;
			else
				carY -= Math.floor(random.nextDouble() * 200) + 100;
			int carsNumberInCascade = 1 + random.nextInt(3);
			int lineNumber = (int) Math.floor(random.nextDouble() * road.lanesCount);
			for (int j = 0; j < carsNumberInCascade; ++j) {
				int carX = startBotX + roadLineWidth * (lineNumber++ % road.lanesCount);
				if (carX > CAR_CANVAS_WIDTH) {
					System.out.print("YO - THERE IS A CAR OUTSIDE ROAD");
				}
				traffic1.add(
						(new Car(carX, carY, carWidth, carHeight, RAYS_COUNT, RAYS_SPREAD_ANGLE, CAR_DECISIONS_COUNT,
								layersNNetwork, "DUMMY",
								dummyMaxSpeed / 4 + random.nextDouble() * dummyMaxSpeed * 3 / 4,
								CAR_CANVAS_WIDTH, FLOP, damagedCarImage, bestCarImage, carImage)));
			}
		}
		return traffic1;
	}

	public void getSavedGame() {
		SaveGame savedGameToSet = Utils.getSavedGameFromFile(this);
		if (savedGameToSet != null) {
			savedGame = savedGameToSet;
			Field[] fields = savedGame.getClass().getDeclaredFields();
			for (Field field : fields) {
				Field gameField = null;
				try {
					gameField = this.getClass().getDeclaredField(field.getName());
					field.setAccessible(true);
					gameField.setAccessible(true);
					if (field.getName().equalsIgnoreCase("savedBrain")
							&& field.get(savedGame) instanceof NNetwork) {
						NNetwork clonedSavedBrain = (NNetwork) ((NNetwork) field.get(savedGame)).clone();
						gameField.set(this, clonedSavedBrain);
						layersNNetwork = clonedSavedBrain.neuronCounts;

					} else {
						gameField.set(this, field.get(savedGame));
					}
				} catch (IllegalArgumentException | IllegalAccessException | CloneNotSupportedException
						| NoSuchFieldException e) {
					e.printStackTrace();
				}
			}
		} else {
			// System.out.println("--Saved game not found");
			savedGame = savedGameToSet;
		}
	}

	public void saveGame() {
		Utils.saveGameToFile(this);
		JOptionPane.showMessageDialog(this, "File saved");
	}

	public class CarCanvas extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1566494876848210353L;
		private BufferedImage canvasImage;

		public CarCanvas() {
			canvasImage = new BufferedImage(CAR_CANVAS_WIDTH, CAR_CANVAS_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		}

		public void updateCanvas() {
			Graphics2D g2d = canvasImage.createGraphics();

			g2d.setColor(new Color(30, 140, 255));
			g2d.fillRect(0, 0, getWidth(), getHeight());

			if (cars.size() > 0) {
				if (userWantsToPlay && userCar != null) {
					g2d.translate(0, (int) (-userCar.y + CAR_CANVAS_HEIGHT * 0.7));
				} else if (bestCar != null) {
					g2d.translate(0, (int) (-bestCar.y + CAR_CANVAS_HEIGHT * 0.7));
				}
			}
			road.paint(g2d);
			for (Car carObj : traffic) {
				carObj.paint(g2d);
			}
			for (Car carObj : cars) {
				if (userWantsToPlay) {
					if (!carObj.humanDrives) {
						carObj.paint(g2d);
					}
				} else {
					if (!carObj.bestCar) {
						carObj.paint(g2d);
					}
				}

			}
			if (bestCar != null) {
				bestCar.paint(g2d);
			}
			if (userCar != null) {
				userCar.paint(g2d);
			}
			if (cars.size() > 0) {
				if (userWantsToPlay && userCar != null) {
					g2d.translate(0, (int) -(-userCar.y + CAR_CANVAS_HEIGHT * 0.7));
				} else if (bestCar != null) {
					g2d.translate(0, (int) -(-bestCar.y + CAR_CANVAS_HEIGHT * 0.7));
				}
			}

			int startTextY = 45;
			int textLineHeight = 25;
			g2d.setColor(new Color(100, 20, 200));
			g2d.setFont(new Font("Arial", Font.BOLD, 30));
			g2d.setColor(Color.RED);
			g2d.drawString("Боти: " + cars.size(), 15, startTextY);
			g2d.drawString("Best brain: " + useSavedBrain, 15, startTextY += textLineHeight);
			g2d.drawString("Мутації: " + mutations, 15, startTextY += textLineHeight);
			g2d.drawString("Ланцюг-мутації: " + chainedMutations, 15, startTextY += textLineHeight);
			g2d.drawString(
					"Н-мережа: " + RAYS_COUNT + "," + NNLayersInput + (NNLayersInput.equalsIgnoreCase("") ? "" : ",")
							+ CAR_DECISIONS_COUNT,
					15,
					startTextY += textLineHeight);
			g2d.dispose();
			repaint();
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			g.drawImage(canvasImage, 0, 0, null);
		}

	}

	public class NetworkCanvas extends JPanel {

		private static final long serialVersionUID = -6710282581556726869L;
		private BufferedImage canvasImage;
		public Stroke plainStroke;
		public String[] lastLevelArrows;
		public int r;

		public NetworkCanvas() {
			this.canvasImage = new BufferedImage(NN_CANVAS_WIDTH, NN_CANVAS_HEIGHT, BufferedImage.TYPE_INT_ARGB);
			this.plainStroke = new BasicStroke(1);
			this.lastLevelArrows = new String[] { "\u2191", "\u2190", "\u2192", "\u2193" };
			this.r = 10;
		}

		public void updateCanvas(NNetwork networkToDisplay) {
			Graphics2D g2d = canvasImage.createGraphics();
			g2d.setColor(new Color(153, 0, 0));
			g2d.fillRect(0, 0, getWidth(), getHeight());

			int margin = 50;
			int left = margin;
			int top = margin;
			int width = getWidth() - margin * 2;
			int height = getHeight() - margin * 2;

			int levelHeight = height / networkToDisplay.NNlevels.length;

			for (int i = networkToDisplay.NNlevels.length - 1; i >= 0; i--) {
				int levelTop = (int) Math.floor(top + Utils.lerp(
						height - levelHeight,
						0,
						networkToDisplay.NNlevels.length == 1
								? 0.5
								: (double) i / (networkToDisplay.NNlevels.length - 1)));

				drawLevel(g2d, networkToDisplay.NNlevels[i],
						left, levelTop, width, levelHeight,
						i == networkToDisplay.NNlevels.length - 1);
			}

			g2d.dispose();
			repaint();
		}

		public double getNodeX(double[] nodes, int index, int left, int right) {
			return Utils.lerp(
					left,
					right,
					nodes.length == 1
							? 0.5
							: (double) index / (nodes.length - 1));
		}

		public void drawLevel(Graphics2D g2d, NNLevel level, int left,
				int top, int width, int height, boolean isLastLevel) {

			int right = left + width;
			int bottom = top + height;
			Stroke dashedStroke = new BasicStroke(2, BasicStroke.JOIN_ROUND,
					BasicStroke.JOIN_BEVEL,
					0, new float[] { r, r / 2 }, (float) ((System.currentTimeMillis() - startTime) / 46));
			g2d.setStroke(dashedStroke);
			double[] inputs = level.levelInputs;
			double[] outputs = level.levelOutputs;
			double[][] weights = level.weights;
			double[] biases = level.biases;

			int nodeRadius = width / 5;

			for (int i = 0; i < inputs.length; i++) {
				for (int j = 0; j < outputs.length; j++) {
					g2d.setColor(Utils.getRGBA(weights[i][j]));
					// System.out.println(g2d.getColor());
					g2d.drawLine((int) getNodeX(inputs, i, left, right),
							bottom, (int) getNodeX(outputs, j, left, right),
							top);
				}
			}
			for (int i = 0; i < inputs.length; i++) {
				int x = (int) getNodeX(inputs, i, left, right);
				g2d.setColor(Color.BLACK);
				g2d.fillArc(x - (int) (nodeRadius / 2), bottom - (int) nodeRadius / 2, nodeRadius, nodeRadius, 0, 360);
				g2d.setColor(Utils.getRGBA(inputs[i]));
				g2d.fillArc(x - (int) Math.floor(nodeRadius / 3.2), bottom - (int) Math.floor(nodeRadius / 3.2),
						(int) Math.round(nodeRadius * 0.6),
						(int) Math.round(nodeRadius * 0.6),
						0, 360);
			}
			for (int i = 0; i < outputs.length; i++) {
				int x = (int) getNodeX(outputs, i, left, right);
				g2d.setColor(Color.BLACK);
				g2d.fillArc(x - (int) (nodeRadius / 2), top - (int) (nodeRadius / 2), nodeRadius, nodeRadius, 0, 360);
				g2d.setColor(Utils.getRGBA(outputs[i]));
				g2d.fillArc(x - (int) (nodeRadius / 3.2), top - (int) (nodeRadius / 3.2),
						(int) Math.round(nodeRadius * 0.6), (int) Math.round(nodeRadius * 0.6), 0,
						360);
				g2d.setColor(Utils.getRGBA(biases[i]));
				g2d.drawArc(x - (int) (nodeRadius / 2.5), top - (int) (nodeRadius / 2.5),
						(int) Math.round(nodeRadius * 0.8), (int) Math.round(nodeRadius * 0.8), 0, 360);
				if (isLastLevel) {
					String text = this.lastLevelArrows[i];
					g2d.setFont(new Font("Arial", Font.BOLD, (int) (nodeRadius * 0.5)));
					int textWidth = g2d.getFontMetrics().stringWidth(text) / 2;
					g2d.setPaint(Color.BLACK);
					g2d.drawString(text, x - textWidth, (int) Math.round(top + nodeRadius * 0.1));
					g2d.setStroke(this.plainStroke);
					;
					g2d.setColor(Color.WHITE);
					g2d.drawString(text, x - textWidth, (int) Math.round(top + nodeRadius * 0.1));
					g2d.setStroke(dashedStroke);

				}
			}
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			g.drawImage(canvasImage, 0, 0, null);

		}
	}

	public void restartGame() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		bestCar = null;
		int[] layersNNetworkToSet;
		if (!NNLayersInput.equals("")) {
			int[] tempArray = Arrays.stream(NNLayersInput.split(",")).mapToInt(d -> Integer.parseInt(d)).toArray();
			layersNNetworkToSet = new int[tempArray.length + 2];
			layersNNetworkToSet[0] = RAYS_COUNT + 1;
			layersNNetworkToSet[layersNNetworkToSet.length - 1] = CAR_DECISIONS_COUNT;
			System.arraycopy(tempArray, 0, layersNNetworkToSet, 1, tempArray.length);
		} else {
			layersNNetworkToSet = new int[] { RAYS_COUNT + 1, CAR_DECISIONS_COUNT };
		}
		if (!Arrays.equals(layersNNetwork, layersNNetworkToSet)) {
			layersNNetwork = layersNNetworkToSet;
			savedBrain = null;
			useSavedBrain = false;
			mutations = false;
			chainedMutations = false;
		}

		cars.clear();
		traffic.clear();
		if (reloadRoadIfRestart) {
			road = null;
			road = new Road(CAR_CANVAS_WIDTH, carWidth);
		}
		traffic = generateTraffic(trafficCascades);
		cars = generateCars(CarsNumber, mutations);
	}
}
