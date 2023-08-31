package selfDrivingCar;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import javax.swing.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class gameSelfDrivingCar extends JFrame {

	private static final long serialVersionUID = -8258954113971711416L;
	public final String SCREEN_TITLE = "Game: Self-driving car...";
	public final String CAME_OVER = "Game over, will restart in a fiew moments";
	public final String saveFilePath = "img/savedBrain.json";
	JFrame gameFrame;
	JPanel paramsGamePannel;
	JPanel viewImageGamePannel;
	Container container;
	public boolean userWantsToPlay;
	public double botMaxSpeed;
	public double dummyMaxSpeed;
	public double humanBotMaxSpeed;
	public int WINDOW_HEIGHT = 1000;
	public int WINDOW_WIDTH = 700;
	final int START_LOCATIONX = 100;
	final int START_LOCATIONY = 100;
	public JComboBox<String> comboBox;
	public JTextField inputField;
	public JRadioButton yesOption;
	public JRadioButton noOption;
	public NNetwork savedBrain;
	public int sleeptime = 40;
	public int RAYS_COUNT = 13;
	public double RAYS_SPREAD_ANGLE_timser = 1.75;
	public double RAYS_SPREAD_ANGLE;
	public int CarsNumber = 10;
	public volatile int userKeyEvent = 0;
	public long startTime;
	final int CAR_DECISIONS_COUNT = 4;
	final String SAVE_FILE_EXT = ".brain";
	public Double FLOP = 0.7;
	public String NNLayersInput = "7,7";
	public double mutationStep = 0.43;
	boolean chainedmutations = false;
	Random random;
	public int CAR_CANVAS_HEIGHT;
	public int CAR_CANVAS_WIDTH;
	public int NN_CANVAS_HEIGHT;
	public int NN_CANVAS_WIDTH;
	public int carWidth;
	public int carHeight;
	public Boolean isGameOver = false;
	public Color carStartColor = new Color(200, 100, 30);
	public CopyOnWriteArrayList<Car> cars;
	public CopyOnWriteArrayList<Car> traffic;
	public Car bestCar;
	public Car userCar;
	public int YbestCar;
	public SaveGame savedGame;
	public CarCanvas carCanvas;
	public boolean mutations = false;
	public boolean reloadRoadIfRestart = false;
	public boolean useSavedBrain = false;
	public int trafficCascades;
	public NetworkCanvas networkCanvas;
	public Road road;
	public boolean gamereloading;
	private long currentTime;
	private long savedTime;
	private long currentFrameTime;
	private long savedFrameTime;
	private long frameTime;

	public static void main(String[] args) {
		new gameSelfDrivingCar().go();
	}

	void go() {
		gameFrame = new JFrame();
		gameFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		gameFrame.setTitle(SCREEN_TITLE);
		userWantsToPlay = false;
		container = gameFrame.getContentPane();
		paramsGamePannel = new JPanel();
		viewImageGamePannel = new JPanel();
		yesOption = new JRadioButton("Yes");
		noOption = new JRadioButton("No");
		botMaxSpeed = 2;
		dummyMaxSpeed = 3;
		humanBotMaxSpeed = 3;
		getSavedGame();
		if (savedGame == null) {
			comboBox = new JComboBox<String>(new String[] { "no saved data", "save game onse", "press 's' " });
		} else {
			comboBox = new JComboBox<String>(getMenuItems());
		}
		comboBox.setBounds(10, 0, 150, 31);
		inputField = new JTextField();
		inputField.setBounds(comboBox.getWidth() + 10, 0, 50, 32);
		yesOption.setBounds(comboBox.getWidth() + 10, 0, 48, 15);
		noOption.setBounds(comboBox.getWidth() + 10, yesOption.getHeight() + 2, 48, 15);
		gamereloading = false;
		startTime = System.currentTimeMillis();
		CAR_CANVAS_HEIGHT = WINDOW_HEIGHT - 34;
		CAR_CANVAS_WIDTH = (int) Math.floor(WINDOW_WIDTH * 0.5);
		NN_CANVAS_HEIGHT = WINDOW_HEIGHT - 34;
		NN_CANVAS_WIDTH = (int) Math.floor(WINDOW_WIDTH * 0.45);
		carWidth = (int) Math.floor(CAR_CANVAS_WIDTH / 8);
		carHeight = (int) Math.floor((double) 50 / 30 * carWidth);
		gameFrame.setBounds(START_LOCATIONX, START_LOCATIONY, WINDOW_WIDTH, WINDOW_HEIGHT + 52);
		container.setLayout(null);
		cars = new CopyOnWriteArrayList<Car>();
		traffic = new CopyOnWriteArrayList<Car>();
		random = new Random();
		optionsRadioButtonsSetup(this);
		comboboxSetup(this);
		inputFieldSetup(this);
		viewImageGamePannelSetup(this);
		road = new Road(CAR_CANVAS_WIDTH, carWidth);
		trafficCascades = random.nextInt(35) + 5;
		carCanvas = new CarCanvas();
		carCanvas.setBounds(0, 0, CAR_CANVAS_WIDTH, CAR_CANVAS_HEIGHT);
		networkCanvas = new NetworkCanvas();
		networkCanvas.setBounds(carCanvas.getWidth() + 8, 0, NN_CANVAS_WIDTH, NN_CANVAS_HEIGHT);
		paramsGamePannel.setLayout(null);
		viewImageGamePannel.setLayout(null);
		viewImageGamePannel.add(carCanvas);
		viewImageGamePannel.add(networkCanvas);
		int withForOptionPannel = (comboBox.getWidth() + 10 + inputField.getWidth() + 15) > (comboBox.getWidth() + 10
				+ yesOption.getWidth() + 15)
						? (comboBox.getWidth() + inputField.getWidth() + 15)
						: comboBox.getWidth() + 10 + noOption.getWidth() + 15;
		paramsGamePannel.setBounds(4, 4, withForOptionPannel, inputField.getHeight());
		viewImageGamePannel.setBounds(4, paramsGamePannel.getHeight() + 4,
				carCanvas.getWidth() + networkCanvas.getWidth(),
				networkCanvas.getHeight());
		paramsGamePannel.add(comboBox);
		paramsGamePannel.add(inputField);
		container.add(paramsGamePannel);
		container.add(viewImageGamePannel);
		gameFrame.setResizable(true);
		gameFrame.setVisible(true);
		savedTime = System.currentTimeMillis();
		savedFrameTime = savedTime;

		if (savedGame != null) {
			Car testCar = new Car((int) Math.floor((double) CAR_CANVAS_WIDTH / 2), 100, carWidth, carHeight, RAYS_COUNT,
					Math.PI * RAYS_SPREAD_ANGLE_timser, CAR_DECISIONS_COUNT, NNLayersInput, "AI", botMaxSpeed,
					Color.BLUE, this);
			if (Arrays.equals(testCar.brain.neuronCounts, savedBrain.neuronCounts)) {
				useSavedBrain = savedGame.useSavedBrain;
			} else {
				useSavedBrain = false;
				savedBrain = null;
			}
			;
			testCar = null;
		} else {
			useSavedBrain = false;
			savedBrain = null;
		}
		;
		cars = generateCars(CarsNumber, mutations);

		traffic = generateTraffic(trafficCascades);
		container.requestFocus();

		int numberOfCores = Runtime.getRuntime().availableProcessors();
		ExecutorService executorService = Executors.newFixedThreadPool(numberOfCores);

		while (!isGameOver) {
			ArrayList<Future<?>> traffFutures = new ArrayList<>();
			ArrayList<Future<?>> carsFutures = new ArrayList<>();

			for (Car carobj : cars) {
				carsFutures.add(executorService.submit((Runnable) () -> {
					if (!carobj.damaged || carobj.humanDrives) {
						carobj.carMove(userKeyEvent, road, traffic, bestCar == null ? null : bestCar);
					}
				}));

			}
			currentTime = System.currentTimeMillis();

			if (userKeyEvent != 0 && currentTime - savedTime > 500L) {
				userKeyEvent = 0;
				savedTime = currentTime;
			}
			for (Car traphObj : traffic) {
				traffFutures.add(executorService.submit((Runnable) () -> {
					traphObj.carMove(0, null, null, null);
				}));
			}

			for (Future<?> future : traffFutures) {
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
			filterDamaged(cars);
			if (gamereloading) {
				restartGame(this);
				gamereloading = !gamereloading;
			}
			currentFrameTime = System.currentTimeMillis();
			frameTime = currentFrameTime - savedFrameTime;
			;
			try {
				Thread.sleep((frameTime > sleeptime ? (sleeptime > 20 ? sleeptime : 20) : 20));
			} catch (InterruptedException e1) {
			}
			savedFrameTime = currentFrameTime;

		}
		executorService.shutdown();
	}

	private void getCurrentBestCar() {
		YbestCar = road.bottom;
		cars.forEach(car -> {
			car.bestCar = false;
			if (car.y < YbestCar && !car.humanDrives) {
				bestCar = car;
				YbestCar = car.y;
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
		gameClass.yesOption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == gameClass.yesOption) {
					try {
						Field comboboxActivatedField = gameClass.getClass()
								.getDeclaredField(gameClass.comboBox.getItemAt(gameClass.comboBox.getSelectedIndex()));
						gameClass.noOption.setSelected(false);
						if (comboboxActivatedField.get(gameClass) instanceof Boolean) {
							comboboxActivatedField.set(gameClass, true);
						}
						gameClass.yesOption.setFocusable(false);
						gameClass.noOption.setFocusable(false);
						gameClass.container.requestFocus();
					} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
							| IllegalAccessException ex) {
						// ex.printStackTrace();
					}
				}
			};
		});
		gameClass.noOption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == gameClass.noOption) {
					try {
						Field comboboxActivatedField = gameClass.getClass()
								.getDeclaredField(gameClass.comboBox.getItemAt(gameClass.comboBox.getSelectedIndex()));
						gameClass.yesOption.setSelected(false);

						if (comboboxActivatedField.get(gameClass) instanceof Boolean) {
							comboboxActivatedField.set(gameClass, false);
						}
						gameClass.yesOption.setFocusable(false);
						gameClass.noOption.setFocusable(false);
						gameClass.container.requestFocus();

					} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
							| IllegalAccessException ex) {
						// ex.printStackTrace();
					}
				}
			};
		});

	}

	private void viewImageGamePannelSetup(gameSelfDrivingCar gameClass) {
		gameClass.container.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int keyevent = e.getKeyCode();
				checkKeyActions(keyevent);
				// System.out.println(e.getKeyCode());
			}
		});
	}

	private void inputFieldSetup(gameSelfDrivingCar gameClass) {
		gameClass.inputField.setFocusable(false);

		gameClass.inputField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (e.getSource() == gameClass.inputField) {
						String selectedField = comboBox.getItemAt(comboBox.getSelectedIndex());
						Field gameField = gameClass.getClass().getDeclaredField(selectedField);
						gameField.setAccessible(true);
						Object valueToSet = null;
						if (gameField.get(gameClass) instanceof Double) {
							valueToSet = Double.parseDouble(gameClass.inputField.getText());
						} else if (gameField.get(gameClass) instanceof Integer) {
							valueToSet = Integer.parseInt(gameClass.inputField.getText());
						} else if (gameField.get(gameClass) instanceof Boolean) {
							valueToSet = Boolean.parseBoolean(gameClass.inputField.getText());
						} else {
							valueToSet = gameClass.inputField.getText();
						}
						gameField.set(gameClass, valueToSet);
					}
					gameClass.inputField.setFocusable(false);
					gameClass.container.requestFocus();

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

	private void comboboxSetup(gameSelfDrivingCar gameClass) {
		gameClass.comboBox.setFocusable(false);
		gameClass.comboBox.addActionListener(new ActionListener() {
			String comboboxSelection;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == gameClass.comboBox) {
					comboboxSelection = gameClass.comboBox.getItemAt(gameClass.comboBox.getSelectedIndex());
					try {
						Field gameField = gameClass.getClass().getDeclaredField(comboboxSelection);
						if (gameField.get(gameClass) instanceof Boolean) {
							gameClass.paramsGamePannel.remove(inputField);
							gameClass.paramsGamePannel.add(yesOption);
							gameClass.paramsGamePannel.add(noOption);
							gameClass.yesOption.setFocusable(true);
							gameClass.noOption.setFocusable(true);
							gameClass.yesOption.setSelected((boolean) gameField.get(gameClass));
							gameClass.noOption.setSelected(!((boolean) gameField.get(gameClass)));

						} else {
							gameClass.paramsGamePannel.remove(yesOption);
							gameClass.paramsGamePannel.remove(noOption);
							gameClass.paramsGamePannel.add(comboBox);
							gameClass.paramsGamePannel.add(gameClass.inputField);
							gameClass.inputField.setFocusable(true);
							gameClass.inputField.setText(gameField.get(gameClass).toString());
						}
						gameClass.paramsGamePannel.revalidate();
						gameClass.paramsGamePannel.repaint();
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
		String menuItemsToreturn[] = new String[fields.length];
		for (byte i = 0; i < menuItemsToreturn.length; ++i) {
			menuItemsToreturn[i] = fields[i].getName();
		}

		return menuItemsToreturn;
	}

	private void checkKeyActions(int keyevent) {
		// System.out.println(keyevent);
		userKeyEvent = keyevent;
		switch (userKeyEvent) {
			case 44:
				// < - 44 - chainedmutations enablet or not +
				chainedmutations = !chainedmutations;
				if (comboBox.getItemAt(comboBox.getSelectedIndex()).equalsIgnoreCase("chainedmutations")) {
					yesOption.setSelected(chainedmutations);
					noOption.setSelected(!chainedmutations);
				}
				;
				break;
			case 76:
				// l - 76 - load best car brain +
				getSavedGame();
				break;
			case 77:
				// m - 77 - mutations enablet or not +
				mutations = !mutations;
				if (comboBox.getItemAt(comboBox.getSelectedIndex()).equalsIgnoreCase("mutations")) {
					yesOption.setSelected(mutations);
					noOption.setSelected(!mutations);
				}
				;
				break;
			case 78:
				// n - 78 - new game +
				gamereloading = true;
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
				;
				break;
		}

	}

	private void filterDamaged(CopyOnWriteArrayList<Car> cars2) {
		cars2.removeIf(carobj -> carobj.damaged && !carobj.bestCar && !carobj.humanDrives);
	}

	public CopyOnWriteArrayList<Car> generateCars(int CarsNumber, boolean mutations) {
		if (CarsNumber == 0)
			CarsNumber = 1;
		RAYS_SPREAD_ANGLE = Math.PI * RAYS_SPREAD_ANGLE_timser;
		CopyOnWriteArrayList<Car> carListToBeCreated = new CopyOnWriteArrayList<Car>();

		for (int i = 0; i < CarsNumber; ++i) {

			carListToBeCreated.add(
					new Car((int) Math.floor((double) CAR_CANVAS_WIDTH / 2), 100, carWidth, carHeight, RAYS_COUNT,
							RAYS_SPREAD_ANGLE, CAR_DECISIONS_COUNT,
							NNLayersInput,
							"AI", botMaxSpeed, Color.BLUE, this));

			if (savedBrain != null && useSavedBrain) {
				if (carListToBeCreated.get(i).useBrain) {
					if (i == 0) {
						try {
							carListToBeCreated.get(i).brain = (NNetwork) savedBrain.clone();
						} catch (CloneNotSupportedException e) {
							e.printStackTrace();
						}
						;
					} else {
						if (mutations && chainedmutations) {
							carListToBeCreated.get(i).brain = carListToBeCreated.get(i).brain.mutate(
									carListToBeCreated.get(i - 1).brain,
									mutationStep);
						} else if (mutations && !chainedmutations) {
							carListToBeCreated.get(i).brain = carListToBeCreated.get(i).brain.mutate(savedBrain,
									mutationStep);
						}
					}
				}
			}

		}
		if (userWantsToPlay) {
			userCar = new Car((int) Math.floor((double) CAR_CANVAS_WIDTH / 2), 100, carWidth, carHeight, RAYS_COUNT,
					RAYS_SPREAD_ANGLE, CAR_DECISIONS_COUNT,
					NNLayersInput,
					"KEYS", humanBotMaxSpeed, carStartColor, this);
			carListToBeCreated.add(userCar);
		}
		return carListToBeCreated;
	}

	public CopyOnWriteArrayList<Car> generateTraffic(int cascades) {
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
				;
				traffic1.add(
						(new Car(carX, carY, carWidth, carHeight, RAYS_COUNT, RAYS_SPREAD_ANGLE,
								CAR_DECISIONS_COUNT,
								NNLayersInput, "DUMMY",
								dummyMaxSpeed / 4
										+ random.nextDouble() * dummyMaxSpeed * 3 / 4,
								null,
								this)));
			}
		}
		return traffic1;
	}

	public void getSavedGame() {
		SaveGame savedGame = Utils.getSavedGameFromFile(this);
		if (savedGame != null) {
			this.savedGame = savedGame;

			Field[] fields = this.savedGame.getClass().getDeclaredFields();
			for (Field field : fields) {
				Field gamefield = null;
				try {
					gamefield = this.getClass().getDeclaredField(field.getName());
					field.setAccessible(true);
					gamefield.setAccessible(true);
					if (field.getName().equalsIgnoreCase("savedBrain")
							&& field.get(this.savedGame) instanceof NNetwork) {
						NNetwork clonedSavedBrain = (NNetwork) ((NNetwork) field.get(this.savedGame)).clone();
						gamefield.set(this, clonedSavedBrain);
					} else {
						gamefield.set(this, field.get(this.savedGame));
					}
				} catch (IllegalArgumentException | IllegalAccessException | CloneNotSupportedException
						| NoSuchFieldException e) {
					e.printStackTrace();
				}
			}

		} else {
			// System.out.println("--Saved game not found");
			this.savedGame = savedGame;
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
			g2d.drawString("Ланцюг-мутації: " + chainedmutations, 15, startTextY += textLineHeight);
			g2d.drawString("Н-мережа: " + RAYS_COUNT + "," + NNLayersInput + "," + CAR_DECISIONS_COUNT, 15,
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

	public void restartGame(gameSelfDrivingCar gameClass) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		gameClass.bestCar = null;
		gameClass.cars.clear();
		gameClass.traffic.clear();
		if (gameClass.reloadRoadIfRestart) {
			gameClass.road = null;
			gameClass.road = new Road(gameClass.CAR_CANVAS_WIDTH, gameClass.carWidth);
		}
		gameClass.traffic = gameClass.generateTraffic(gameClass.trafficCascades);
		gameClass.cars = gameClass.generateCars(gameClass.CarsNumber, gameClass.mutations);
	}
}
