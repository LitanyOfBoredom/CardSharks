package FinalProjectGlascock;

import java.io.File;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Card
{
	private int value; // 0-12 indicating face value of the card
	private Image image;
	
	public Card(String name, String suit, int value)
	{
		image = new Image(new File("src/FinalProjectGlascock/images/cards/" + name + "_of_" + suit + ".png").toURI().toString(), 84, 120, false, true);
		this.value = value;
	}
	
	public int getValue()
	{
		return value;
	}
	
	public Image getImage()
	{
		return image;
	}
	
	// makes an image view consisting of the card's image
	public ImageView getImageView()
	{
		return new ImageView(image);
	}
}
