package selfDrivingCar;

import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

public class imageProcessor {
	public static Image getImage(Color askedColor, Random random) {
		try {
			// Завантажуємо зображення з файлу
			BufferedImage originalImage = ImageIO.read(new File("img/original.png"));

			// Створюємо нове зображення того ж розміру, що і оригінальне
			BufferedImage newImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(),
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = newImage.createGraphics();

			// Задаємо контур червоного кольору для малюнка (використовуйте власні
			// координати)
			int x1 = 0;
			int y1 = 0;
			int x2 = originalImage.getWidth();
			int y2 = originalImage.getHeight();
			int deltaR = (int) Math.floor(random.nextDouble() * 255);
			int deltaG = (int) Math.floor(random.nextDouble() * 255);
			int deltaB = (int) Math.floor(random.nextDouble() * 255);
			// g2d.setColor(new Color(deltaR, deltaG, deltaB));
			g2d.setStroke(new BasicStroke(5)); // Ширина контуру
			g2d.clipRect(x1, y1, x2, y2);

			// Малюємо контур на новому зображенні
			g2d.drawImage(originalImage, 0, 0, null);

			// Змішуємо кольори малюнка з контуром
			for (int x = x1; x < x2; ++x) {
				for (int y = y1; y < y2; ++y) {
					int rgb = originalImage.getRGB(x, y);
					Color color = new Color(rgb);
					int alpha = (rgb >> 24) & 0xFF; // Отримуємо значення альфа-каналу
					// System.out.println(color);
					if (alpha > 0 && color.getRed() > 2 && color.getGreen() > 2 && color.getBlue() > 2) {
						// Color mixedColor = new Color(Math.min(color.getRed() + deltaR, 230),
						Color mixedColor = new Color(Math.min(color.getRed() + deltaR, 230),
								Math.min(color.getGreen() + deltaG, 230),
								Math.min(color.getBlue() + deltaB, 230));
						if (askedColor == null) {
							newImage.setRGB(x, y, mixedColor.getRGB());

						} else {
							newImage.setRGB(x, y, askedColor.getRGB());
						}
					}
				}
			}
			g2d.dispose();

			return newImage;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
