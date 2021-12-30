package FinalProjectGlascock;

import java.io.File;

import javafx.application.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.animation.AnimationTimer;
import javafx.animation.TranslateTransition;

// brendan glascock
// itp 368
// fall 2021
// card sharks logo (used in intro sequence) courtesy of Dadillstnator on deviantart
// contestant podium scene courtesy of Greg Wicker
// card faces from https://code.google.com/archive/p/vector-playing-cards/downloads
// card backs from Wheel Genius on deviantart
// led font from https://www.fontspace.com/digital-7-font-f7087
// money cards background from Doug Morris at https://www.dougmorris.net/games/

// instructions: make sure you have a list of 11 questions prewritten and ready to go; hard code them into the answers array before you start
// read the question, and type the player's guess into the textbox (the program will only allow you to type in the answer in the box belonging to the player whose turn it is)
// have the other player guess higher or lower and click the given arrow (or press up/down)
// then, click the green rectangle in the center to reveal the answer
// you will be taken to the cards screen
// click on the player's first card to reveal it, if necessary
// if the player just won the question, they can optionally change their base card; to do that, click on the base card again or press C
// ask the player higher/lower, then click the appropriate arrow or press up/down, then click the next face-down card or press enter to flip it
// continue until the player turns over all 5 cards, guess wrong, or freezes (click the freeze button or press F to freeze)
// if the player answers incorrectly, they lose all progress for this question, and their opponent gets a chance to play the cards
// if no winner has been determined after 4 questions (3 in the tiebreaker round), the 4th hi/lo card game will be sudden death:
// whoever wins the sudden death question can pick to play or pass the cards; whoever has the cards must then call all of their remaining cards correctly or else they lose.
// after a player wins 2 games, they go to the money cards
// for each money card, ask the player their bet and to guess higher or lower than their current card
// in the money cards, the player can change the first card on each level if they choose (click the base card or press C to change)
// input their bet and press enter, then click the appropriate arrow or press up/down key
// click the next card to reveal it
// repeat until all cards have been revealed or the player runs out of money
// note: the last card is the big bet card and the player must bet half their winnings; otherwise, the minimum bet is 50 (all bets must be multiples of 50)

// accessibility info: all functionality can be performed with the keyboard
// in general, if there is only one button/item to click, the enter key will activate it
// enter is also used to flip the next card
// C is used to Change the card
// F is used to Freeze
// up/down are used to activate the hi/lo arrows
// up/down are also used to select play/pass

// the functionality of this program may be slightly obscure because I intended for it to be fairly polished without a lot of rules/instructions,
// rather than something that's self explanatory for new users to instantly pick up
// if you encounter any questions about how to use it or the rules of the game, email me at bglascoc@usc.edu

public class CardSharks extends Application
{
	// 4 different scenes, each with its own root
	private Pane rootIntro;
	private Scene sceneIntro;
	
	private Pane rootContestants;
	private Scene sceneContestants;
	
	private Pane rootCards;
	private Scene sceneCards;
	
	private Pane rootMoneyCards;
	private Scene sceneMoneyCards;
	
	// store the stage to switch scenes
	private Stage stage;
	
	// audio
	private AudioClip singleBell = new AudioClip(new File("src/FinalProjectGlascock/sounds/singlebell.wav").toURI().toString());
	private AudioClip manyBells = new AudioClip(new File("src/FinalProjectGlascock/sounds/manybells.wav").toURI().toString());
	private AudioClip buzzer = new AudioClip(new File("src/FinalProjectGlascock/sounds/buzzer.wav").toURI().toString());
	private AudioClip longBuzzer = new AudioClip(new File("src/FinalProjectGlascock/sounds/longbuzzer.wav").toURI().toString());
	private AudioClip win1 = new AudioClip(new File("src/FinalProjectGlascock/sounds/win1.wav").toURI().toString());
	private AudioClip win2 = new AudioClip(new File("src/FinalProjectGlascock/sounds/win2.wav").toURI().toString());
	
	// intro elements
	private Line verticalLine;
	private Line horizontalLine;
	private Image logoCard;
	private Image logoSharks;
	private ImageView[] logos;
	private Image aceOfHearts;
	private Image aceOfClubs;
	private Image twoOfSpades;
	private Image twoOfDiamonds;
	private ImageView[] cards;
	private int currentBeat = -1;
	private IntroDriver introDriver;
	
	// player decks
	private Deck redDeck;
	private Deck blueDeck;
	
	// text boxes for user numerical input
	private TextField redInput;
	private TextField blueInput;
	
	// up/down buttons
	private Polygon redUp;
	private Polygon redDown;
	private Polygon blueUp;
	private Polygon blueDown;
	
	// freeze bars
	private ImageView redBar = new ImageView(new Image(new File("src/FinalProjectGlascock/images/misc/redbar.png").toURI().toString(), 123, 120, true, false));
	private ImageView blueBar = new ImageView(new Image(new File("src/FinalProjectGlascock/images/misc/bluebar.png").toURI().toString(), 123, 120, true, false));
	
	// invisible rectangle over the beg led in the middle
	private Rectangle invisible;
	
	// center number text
	private Text actualNumber;
	
	// images for cardbacks
	private Image redBack;
	private Image blueBack;
	
	// hi lo buttons
	private Polygon redHi;
	private Polygon redLo;
	private Polygon blueHi;
	private Polygon blueLo;
	
	// turn indicators
	private Rectangle redSquare;
	private Rectangle blueSquare;
	
	// pointers to all the cards on the main board
	private ImageView[][] mainCards;
	
	// values of cards on the main board
	private int[][]  mainCardValues;
	
	// card coordinates
	private int[] cardX = {220, 340, 460, 580, 700};
	private int[] cardY = {150, 364};
	
	// colors
	public final Color transparent = new Color(0, 0, 0, 0);
	public final Color visibleRed = new Color(130.0/255, 5.0/255, 5.0/255, 1);
	public final Color selectRed = new Color(220.0/255, 30.0/255, 32.0/255, 1);
	public final Color visibleBlue = new Color(18.0/255, 20.0/255, 80.0/255, 1);
	public final Color selectBlue = new Color(50.0/255, 65.0/255, 200.0/255, 1);
	public final Color visibleGreen = new Color(47.0/255, 83.0/255, 52.0/255, 1);
	public final Color selectGreen = new Color(80.0/255, 183.0/255, 74.0/255, 1);
	
	// animation driver
	private Driver driver;
	
	// flicker driver for red squares
	private SquareDriver squareDriver;
	
	// one player's guess and the other player's hi/low response
	private int userGuess;
	private boolean guessHigher;
	
	// player's most recent card guess
	private boolean cardGuessHigher;
	
	// which card is next to flip (0-4)?
	private int redNextFlip = 0;
	private int blueNextFlip = 0;
	
	// where is the freeze bar for each player
	private int redFreezeBar = 0;
	private int blueFreezeBar = 0;
	
	// freeze buttons
	private ImageView redFreeze;
	private ImageView blueFreeze;
	private Image redFreezeImg = new Image(new File("src/FinalProjectGlascock/images/misc/freezered.png").toURI().toString(), 100, 30, true, false);
	private Image blueFreezeImg = new Image(new File("src/FinalProjectGlascock/images/misc/freezeblue.png").toURI().toString(), 100, 30, true, false);
	private Image redFreezeSelectImg = new Image(new File("src/FinalProjectGlascock/images/misc/freezeredselect.png").toURI().toString(), 100, 30, true, false);
	private Image blueFreezeSelectImg = new Image(new File("src/FinalProjectGlascock/images/misc/freezeblueselect.png").toURI().toString(), 100, 30, true, false);
	
	// play/pass buttons
	private ImageView redPlay;
	private ImageView redPass;
	private ImageView bluePlay;
	private ImageView bluePass;
	private Image playImg = new Image(new File("src/FinalProjectGlascock/images/misc/play.png").toURI().toString(), 100, 50, true, false);
	private Image passImg = new Image(new File("src/FinalProjectGlascock/images/misc/pass.png").toURI().toString(), 100, 50, true, false);
	private Image playSelectImg = new Image(new File("src/FinalProjectGlascock/images/misc/playselect.png").toURI().toString(), 100, 50, true, false);
	private Image passSelectImg = new Image(new File("src/FinalProjectGlascock/images/misc/passselect.png").toURI().toString(), 100, 50, true, false);
	
	// continue button
	private ImageView continueButton;
	private Image continueButtonImg = new Image(new File("src/FinalProjectGlascock/images/misc/continue.png").toURI().toString(), 100, 30, true, false);
	private Image continueButtonSelectImg = new Image(new File("src/FinalProjectGlascock/images/misc/continueselect.png").toURI().toString(), 100, 30, true, false);
	
	// whose turn is it to answer the question
	private boolean redTurn = true;
	
	// on when game is over
	private boolean gameOver = false;
	
	// how many questions in each round
	private int[] questionsPerRound = {4,4,3};
	
	// answer values
	private int[] answers = {34,66,30,49,3,30,34,57,26,12,36};
	
	// what round are we on in total
	private int roundNumber = 0;
	
	// how many games has each player won?
	private int redScore = 0;
	private int blueScore = 0;
	
	// what round is this within the current game
	private int roundCount = 1;
	
	// money cards and values
	private ImageView[][] moneyCards;
	private int[][] moneyCardValues;
	
	// money card coordinates
	private int[] moneyCardX = {559, 675, 790, 905};
	private int[] moneyCardY = {431, 259, 87};
	private int[][] moneyCardCoordinatePairs = {{0,1}, {0,2}, {0,3}, {1,1}, {1,2}, {1,3}, {2,1}};
	private int[][] prev = {{0,0}, {0,1}, {0,2}, {0,3}, {1,1}, {1,2}, {1,3}};
	
	// money cards ui
	private TextField betInput;
	private Polygon betHi;
	private Polygon betLo;
	
	// how much money contestant has to bet
	private int money = 200;
	private Text moneyDisplay;
	private ImageView moneyDisplayBorder = new ImageView(new Image(new File("src/FinalProjectGlascock/images/misc/rect.png").toURI().toString()));
	
	// what bet are we on (0-6)
	private int currentBet = 0;
	
	// money card animation driver
	private WinningsDriver wDriver;
	
	// whether person bet higher or lower
	private boolean moneyBetHi;
	
	// states
	public enum State {QUESTION_READING_RED, QUESTION_READING_BLUE, QUESTION_HI_LO_RED, QUESTION_HI_LO_BLUE, QUESTION_DONE_RED_BLUE, QUESTION_DONE_BLUE_RED,
						CARDS_TURN_FIRST_RED_1, CARDS_TURN_FIRST_RED_2, CARDS_TURN_FIRST_BLUE_1, CARDS_TURN_FIRST_BLUE_2, CARDS_CHANGE_RED, CARDS_CHANGE_BLUE,
						CARDS_HI_LO_RED_1, CARDS_HI_LO_BLUE_1, CARDS_REVEAL_RED_1, CARDS_REVEAL_BLUE_1, CARDS_HI_LO_RED_2, CARDS_HI_LO_BLUE_2, CARDS_REVEAL_RED_2,
						CARDS_REVEAL_BLUE_2, CARDS_DONE, CARDS_FINISHED, MONEY_CARDS_BET, MONEY_CARDS_CHANGE, MONEY_CARDS_HI_LO, MONEY_CARDS_REVEAL, PAUSE, STOP};
	private State state = State.QUESTION_READING_RED;
	private boolean playPass = false;
	
	public static void main(String[] args)
	{
		launch(args);
	}
	
	public void start(Stage stage)
	{
		rootIntro = new Pane();
		sceneIntro = new Scene(rootIntro, 1140, 640);
		stage.setScene(sceneIntro);
		stage.setTitle("Card Sharks");
		stage.setResizable(false);
		stage.show();
		this.stage = stage;
		
		renderUI();
		gameInit();
		buzzer.setVolume(0.3);
		playIntro();
	}
	
	// set up the scenes
	private void renderUI()
	{
		renderIntro();
		renderContestantsUI();
		renderCardsUI();
		renderMoneyCardsUI();
		addContestantsKeyListener(sceneContestants);
		addCardsKeyListener(sceneCards);
		addMoneyCardsKeyListener(sceneMoneyCards);
	}
	
	// render the intro scene
	private void renderIntro()
	{
		rootIntro.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
		
		// lines
		verticalLine = new Line(570, 0, 570, 640);
		horizontalLine = new Line(0, 320, 1140, 320);
		Color introGreen = new Color(0, 90.0/255, 0, 1);
		verticalLine.setStroke(introGreen);
		horizontalLine.setStroke(introGreen);
		verticalLine.setStrokeWidth(16);
		horizontalLine.setStrokeWidth(16);
		rootIntro.getChildren().addAll(horizontalLine, verticalLine);
		
		// logos
		logoCard = new Image(new File("src/FinalProjectGlascock/images/misc/logocard.png").toURI().toString(), 500, 100, false, false);
		logoSharks = new Image(new File("src/FinalProjectGlascock/images/misc/logosharks.png").toURI().toString(), 500, 100, false, false);
		int[] xCoordinatesLogos = {31,31,609,609,31,31,609,609};
		int[] yCoordinatesLogos = {50,150,50,150,378,478,378,478};
		logos = new ImageView[xCoordinatesLogos.length];
		for(int i = 0; i < xCoordinatesLogos.length; i++)
		{
			logos[i] = new ImageView(i % 2 == 0 ? logoCard : logoSharks);
			logos[i].setX(xCoordinatesLogos[i]);
			logos[i].setY(yCoordinatesLogos[i]);
		}
		
		// cards
		aceOfHearts = new Image(new File("src/FinalProjectGlascock/images/cards/ace_of_hearts.png").toURI().toString(), 168, 240, false, false);
		aceOfClubs = new Image(new File("src/FinalProjectGlascock/images/cards/ace_of_clubs.png").toURI().toString(), 168, 240, false, false);
		twoOfSpades = new Image(new File("src/FinalProjectGlascock/images/cards/2_of_spades.png").toURI().toString(), 168, 240, false, false);
		twoOfDiamonds = new Image(new File("src/FinalProjectGlascock/images/cards/2_of_diamonds.png").toURI().toString(), 168, 240, false, false);
		int[] xCoordinatesCards = {75, 319, 653, 897, 75, 319, 653, 897};
		int[] yCoordinatesCards = {64, 8, 64, 8, 336, 392, 336, 392};
		cards = new ImageView[xCoordinatesCards.length];
		for(int i = 0; i < xCoordinatesCards.length; i++)
		{
			cards[i] = new ImageView(i < 4 ? (i % 2 == 0 ? aceOfHearts : aceOfClubs) : (i % 2 == 0 ? twoOfDiamonds : twoOfSpades));
			cards[i].setX(xCoordinatesCards[i]);
			cards[i].setY(yCoordinatesCards[i]);
		}
	}
	
	// start up the intro sequence
	private void playIntro()
	{
		AudioClip openingTheme = new AudioClip(new File("src/FinalProjectGlascock/sounds/openingtheme.wav").toURI().toString());
		openingTheme.setVolume(0.3);
		introDriver = new IntroDriver();
		openingTheme.play();
		introDriver.start();
	}
	
	// animation driver for intro
	public class IntroDriver extends AnimationTimer
    {	
		long start = -1;
		
    	@Override
    	public void handle(long now)
    	{
    		if(start == -1)
    			start = now;
    		flickerIntro(now, start);
    	}
    }
	
	// animate the intro
	private void flickerIntro(long now, long start)
	{
		now -= 700000000; // adjust for delays in playing audio
		if(now < start)
			return;
		
		int beat = (int) ((now - start) /  349641176) + 1;
		
		if(beat == currentBeat)
			return;
		
		if(beat == 81)
		{
			introDriver.stop();
			rootIntro.getChildren().remove(cards[1]);
			rootIntro.getChildren().remove(cards[3]);
			rootIntro.getChildren().remove(cards[4]);
			rootIntro.getChildren().remove(cards[6]);
			stage.setScene(sceneContestants);
			sceneContestants.setCursor(Cursor.DEFAULT);
			state = State.QUESTION_READING_RED;
		}
		
		currentBeat = beat;
		boolean odd = beat % 2 == 1;
		
		// top left
		if(beat <= 32)
		{
			rootIntro.getChildren().add(odd ? logos[0] : logos[1]);
			if(beat != 0)
				rootIntro.getChildren().remove(odd ? logos[1] : logos[0]);
		}
		else if(beat == 33)
		{
			rootIntro.getChildren().remove(logos[1]);
			rootIntro.getChildren().add(cards[0]);
		}
		else if(beat <= 80)
		{
			rootIntro.getChildren().add(odd ? cards[0] : cards[1]);
			rootIntro.getChildren().remove(odd ? cards[1] : cards[0]);
		}
		
		// bottom right
		if(beat > 8 && beat <= 40)
		{
			rootIntro.getChildren().add(odd ? logos[6] : logos[7]);
			if(beat != 0)
				rootIntro.getChildren().remove(odd ? logos[7] : logos[6]);
		}
		else if(beat == 41)
		{
			rootIntro.getChildren().remove(logos[7]);
			rootIntro.getChildren().add(cards[7]);
		}
		else if(beat > 40 && beat <= 80)
		{
			rootIntro.getChildren().add(odd ? cards[7] : cards[6]);
			rootIntro.getChildren().remove(odd ? cards[6] : cards[7]);
		}
		
		// top right
		if(beat > 16 && beat <= 48)
		{
			rootIntro.getChildren().add(odd ? logos[3] : logos[2]);
			if(beat != 0)
				rootIntro.getChildren().remove(odd ? logos[2] : logos[3]);
		}
		else if(beat == 49)
		{
			rootIntro.getChildren().remove(logos[2]);
			rootIntro.getChildren().add(cards[2]);
		}
		else if(beat > 48 && beat <= 80)
		{
			rootIntro.getChildren().add(odd ? cards[2] : cards[3]);
			rootIntro.getChildren().remove(odd ? cards[3] : cards[2]);
		}
		
		// bottom left
		if(beat > 24 && beat <= 56)
		{
			rootIntro.getChildren().add(odd ? logos[5] : logos[4]);
			if(beat != 0)
				rootIntro.getChildren().remove(odd ? logos[4] : logos[5]);
		}
		else if(beat == 57)
		{
			rootIntro.getChildren().remove(logos[4]);
			rootIntro.getChildren().add(cards[5]);
		}
		else if(beat > 56 && beat <= 80)
		{
			rootIntro.getChildren().add(odd ? cards[5] : cards[4]);
			rootIntro.getChildren().remove(odd ? cards[4] : cards[5]);
		}
	}
	
	// render the scene with contestant podiums
	private void renderContestantsUI()
	{
		rootContestants = new Pane();
		sceneContestants = new Scene(rootContestants, 1140, 640);
		BackgroundImage backgroundContestants = new BackgroundImage(new Image(new File("src/FinalProjectGlascock/images/scenes/contestants.jpg").toURI().toString()),
		        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
		rootContestants.setBackground(new Background(backgroundContestants));
		sceneContestants.getStylesheets().add("FinalProjectGlascock/style.css");
		
		// input boxes for typing in guesses
		redInput = new TextField();
		blueInput = new TextField();
		redInput.getStyleClass().add("number-text");
		redInput.getStyleClass().add("mono");
		redInput.getStyleClass().add("red-number-text");
		redInput.requestFocus();
		blueInput.getStyleClass().add("number-text");
		blueInput.getStyleClass().add("mono");
		blueInput.getStyleClass().add("blue-number-text");
		redInput.setLayoutX(354);
		redInput.setLayoutY(435);
		blueInput.setLayoutX(681);
		blueInput.setLayoutY(435);
		blueInput.setEditable(false);
		rootContestants.getChildren().addAll(redInput, blueInput);
		
		// listeners to constrain input and handle submissions
		ChangeListener<String> listener = ((observable, oldValue, newValue) -> {
			handleInput(oldValue, newValue);
		});
		redInput.textProperty().addListener(listener);
		blueInput.textProperty().addListener(listener);
		redInput.setOnAction(ae -> {handleSubmit(redInput.getText());});
		blueInput.setOnAction(ae -> {handleSubmit(blueInput.getText());});
		
		// up/down buttons
		redUp = new Polygon(354, 427, 407, 375, 460, 427);
		redUp.setFill(transparent);
		redUp.getStyleClass().add("arrow");
		redUp.setOnMouseClicked(me -> {handleUpDownClick(1);});
		redUp.setOnMouseEntered(me -> {handleUpDownHover(1, true);});
		redUp.setOnMouseExited(me -> {handleUpDownHover(1, false);});
		redDown = new Polygon(354, 527, 407, 579, 460, 527);
		redDown.setFill(transparent);
		redDown.getStyleClass().add("arrow");
		redDown.setOnMouseClicked(me -> {handleUpDownClick(2);});
		redDown.setOnMouseEntered(me -> {handleUpDownHover(2, true);});
		redDown.setOnMouseExited(me -> {handleUpDownHover(2, false);});
		blueUp = new Polygon(681, 427, 734, 375, 787, 427);
		blueUp.setFill(transparent);
		blueUp.getStyleClass().add("arrow");
		blueUp.setOnMouseClicked(me -> {handleUpDownClick(3);});
		blueUp.setOnMouseEntered(me -> {handleUpDownHover(3, true);});
		blueUp.setOnMouseExited(me -> {handleUpDownHover(3, false);});
		blueDown = new Polygon(681, 527, 734, 579, 787, 527);
		blueDown.setFill(transparent);
		blueDown.getStyleClass().add("arrow");
		blueDown.setOnMouseClicked(me -> {handleUpDownClick(4);});
		blueDown.setOnMouseEntered(me -> {handleUpDownHover(4, true);});
		blueDown.setOnMouseExited(me -> {handleUpDownHover(4, false);});
		rootContestants.getChildren().addAll(redUp, redDown, blueUp, blueDown);
		
		// transparent rectangle over big led
		invisible = new Rectangle(480, 411, 180, 136);
		invisible.setFill(transparent);
		invisible.setOnMouseClicked(me -> {handleInvisibleClick();});
		invisible.setOnMouseEntered(me -> {handleInvisibleHover(true);});
		invisible.setOnMouseExited(me -> {handleInvisibleHover(false);});
		rootContestants.getChildren().add(invisible);
		
		// center led text
		actualNumber = new Text();
		actualNumber.setX(512);
		actualNumber.setY(521);
		actualNumber.getStyleClass().add("actual-number-text");
		actualNumber.setFill(Color.WHITE);
		rootContestants.getChildren().add(actualNumber);
	}
	
	// handle keypresses for the the contestants scene
	public void addContestantsKeyListener(Scene scene)
	{
		scene.setOnKeyPressed(e -> {
			if(e.getCode() == KeyCode.UP)
			{
				if(state == State.QUESTION_HI_LO_RED)
				{
					handleUpDownClick(1);
				}
				else if(state == State.QUESTION_HI_LO_BLUE)
				{
					handleUpDownClick(3);
				}
			}
			else if(e.getCode() == KeyCode.DOWN)
			{
				if(state == State.QUESTION_HI_LO_RED)
				{
					handleUpDownClick(2);
				}
				else if(state == State.QUESTION_HI_LO_BLUE)
				{
					handleUpDownClick(4);
				}
			}
			else if(e.getCode() == KeyCode.ENTER)
			{
				if(state == State.QUESTION_DONE_RED_BLUE || state == State.QUESTION_DONE_BLUE_RED)
				{
					handleInvisibleClick();
				}
			}
		});
	}
	
	// reset the contestants UI back to its default (done every question)
	private void resetContestants()
	{
		// colors back to normal
		redUp.setFill(transparent);
		redDown.setFill(transparent);
		blueUp.setFill(transparent);
		blueDown.setFill(transparent);
		invisible.setFill(transparent);
		
		// remove lingering selected borders
		redInput.getStyleClass().remove("selected");
		blueInput.getStyleClass().remove("selected");
		redUp.getStyleClass().remove("selected-arrow");
		redDown.getStyleClass().remove("selected-arrow");
		blueUp.getStyleClass().remove("selected-arrow");
		blueDown.getStyleClass().remove("selected-arrow");
		invisible.getStyleClass().remove("selected");

		// fonts
		redInput.getStyleClass().remove("not-mono");
		blueInput.getStyleClass().remove("not-mono");
		actualNumber.getStyleClass().remove("not-mono");
		
		// reset text
		redInput.setText("");
		blueInput.setText("");
		actualNumber.setText("");
		(redTurn ? redInput : blueInput).setEditable(true);
		(redTurn ? redInput : blueInput).requestFocus();
	}
	
	// handle when a user hits enter on a numerical field
	private void handleSubmit(String val)
	{
		if(val.equals(""))
			return;
		// give focus back to the pane
		if(state == State.QUESTION_READING_RED)
		{
			rootContestants.requestFocus();
			userGuess = Integer.parseInt(val.trim());
			redInput.setEditable(false);
			redInput.getStyleClass().add("selected");
			blueUp.setFill(visibleBlue);
			blueDown.setFill(visibleBlue);
			state = State.QUESTION_HI_LO_BLUE;
			singleBell.play();
		}
		else if(state == State.QUESTION_READING_BLUE)
		{
			rootContestants.requestFocus();
			userGuess = Integer.parseInt(val.trim());
			blueInput.setEditable(false);
			blueInput.getStyleClass().add("selected");
			redUp.setFill(visibleRed);
			redDown.setFill(visibleRed);
			state = State.QUESTION_HI_LO_RED;
			singleBell.play();
		}
	}
	
	// handle when a user types text into a numerical field
	private void handleInput(String oldValue, String newValue)
	{
		if(state != State.QUESTION_READING_BLUE && state != State.QUESTION_READING_RED)
		{
			return;
		}
		
		TextField input = state == State.QUESTION_READING_BLUE ? blueInput : redInput; 
		if(newValue.equals(" ")) // if a space lingers, clear it
		{
			Platform.runLater(() -> { 
				input.setText(""); 
	        }); 
		}
	    else if(!isValidInput(newValue)) // 0-9 digits only
	    {
	    	input.setText(oldValue);
	    }
		else if(newValue.length() == 3 && newValue.charAt(0) == ' ') // cut the leading space
		{
			input.setText(newValue.substring(1));
		}
		else if(newValue.length() > 2 && !newValue.equals("100")) // no 3 digit numbers except 100
	    {
			input.setText(oldValue);
	    }
	    else if(newValue.length() == 1) // insert space to shift 1-digit numbers right
	    {
	    	input.setText(" " + newValue);
	    }
	    else if(newValue.length() == 2 && newValue.charAt(0) == '0') // no leading 0s
	    {
	    	input.setText(newValue.substring(1));
	    }
		
		if(input.getText().equals("100")) // change font if 100 to save space
		{
			input.getStyleClass().add("not-mono");
		}
		else
		{
			input.getStyleClass().remove("not-mono"); // change back to normal font
		}
	}
	
	// validates that input is digits 0-9 only (1 leading space allowed)
	public static boolean isValidInput(String s)
	{
		if(s.length() == 0)
			return true;
		if(s.charAt(0) != ' ' && (s.charAt(0) < '0' || s.charAt(0) > '9'))
			return false;
		for(int i = 1; i < s.length(); i++)
		{
			if(s.charAt(i) < '0' || s.charAt(i) > '9')
				return false;
		}
		return true;
	}
	
	// when user hovers over hi/low button
	private void handleUpDownHover(int id, boolean enter)
	{
		if(state == State.QUESTION_HI_LO_RED)
		{
			if(id == 1)
			{
				if(enter)
					redUp.setFill(selectRed);
				else
					redUp.setFill(visibleRed);
			}
			else if(id == 2)
			{
				if(enter)
					redDown.setFill(selectRed);
				else
					redDown.setFill(visibleRed);
			}
			
			// cursor
			if(enter)
				sceneContestants.setCursor(Cursor.HAND);
			else
				sceneContestants.setCursor(Cursor.DEFAULT);
		}
		else if(state == State.QUESTION_HI_LO_BLUE)
		{
			if(id == 3)
			{
				if(enter)
					blueUp.setFill(selectBlue);
				else
					blueUp.setFill(visibleBlue);
			}
			else if(id == 4)
			{
				if(enter)
					blueDown.setFill(selectBlue);
				else
					blueDown.setFill(visibleBlue);
			}

			// cursor
			if(enter)
				sceneContestants.setCursor(Cursor.HAND);
			else
				sceneContestants.setCursor(Cursor.DEFAULT);
		}
	}
	
	// when user clicks button to guess hi/low on a question
	private void handleUpDownClick(int id) // red up 1, red down 2, blue up 3, blue down 4
	{
		if(state == State.QUESTION_HI_LO_RED)
		{
			if(id == 1)
			{
				redUp.setFill(selectRed);
				redUp.getStyleClass().add("selected-arrow");
				guessHigher = true;
			}
			else if(id == 2)
			{
				redDown.setFill(selectRed);
				redDown.getStyleClass().add("selected-arrow");
				guessHigher = false;
			}

			state = State.QUESTION_DONE_BLUE_RED;
			singleBell.play();
			sceneContestants.setCursor(Cursor.DEFAULT);
			invisible.setFill(visibleGreen);
		}
		else if(state == State.QUESTION_HI_LO_BLUE)
		{
			if(id == 3)
			{
				blueUp.setFill(selectBlue);
				blueUp.getStyleClass().add("selected-arrow");
				guessHigher = true;
			}
			else if(id == 4)
			{
				blueDown.setFill(selectBlue);
				blueDown.getStyleClass().add("selected-arrow");
				guessHigher = false;
			}

			state = State.QUESTION_DONE_RED_BLUE;
			singleBell.play();
			sceneContestants.setCursor(Cursor.DEFAULT);
			invisible.setFill(visibleGreen);
		}
	}
	
	// when user hovers over invisible rectangle
	private void handleInvisibleHover(boolean enter)
	{
		if(state == State.QUESTION_DONE_RED_BLUE || state == State.QUESTION_DONE_BLUE_RED)
		{
			if(enter)
			{
				invisible.setFill(selectGreen);
				sceneContestants.setCursor(Cursor.HAND);
			}
			else
			{
				invisible.setFill(visibleGreen);
				sceneContestants.setCursor(Cursor.DEFAULT);
			}
		}
	}
	
	// when the invisible rectangle on the middle led is clicked
	private void handleInvisibleClick()
	{
		if(state == State.QUESTION_DONE_RED_BLUE)
		{
			actualNumber.setText(String.format("%2s", Integer.toString(answers[roundNumber])));
			if(answers[roundNumber] == 100)
			{
				actualNumber.getStyleClass().add("not-mono");
			}
			if(userGuess == answers[roundNumber])
			{
				// graphics for red number to flash
				state = State.PAUSE;
				State next = State.CARDS_CHANGE_RED;
				if(redNextFlip == 0)
					next = State.CARDS_TURN_FIRST_RED_1;
				driver = new Driver(redInput, 0, next);
				manyBells.play();
				driver.start();
				
				rootCards.getChildren().add(redSquare);
				if(next == State.CARDS_CHANGE_RED)
					rootCards.getChildren().addAll(redHi, redLo);
			}
			else if(answers[roundNumber] > userGuess)
			{
				if(guessHigher)
				{
					// graphics for blue up arrow to flash
					state = State.PAUSE;
					State next = State.CARDS_CHANGE_BLUE;
					if(blueNextFlip == 0)
						next = State.CARDS_TURN_FIRST_BLUE_1;
					driver = new Driver(blueUp, 1, next);
					manyBells.play();
					driver.start();
					
					rootCards.getChildren().add(blueSquare);
					if(next == State.CARDS_CHANGE_BLUE)
						rootCards.getChildren().addAll(blueHi, blueLo);
				}
				else
				{
					// graphics for red number to flash
					state = State.PAUSE;
					State next = State.CARDS_CHANGE_RED;
					if(redNextFlip == 0)
						next = State.CARDS_TURN_FIRST_RED_1;
					driver = new Driver(redInput, 0, next);
					manyBells.play();
					driver.start();

					rootCards.getChildren().add(redSquare);
					if(next == State.CARDS_CHANGE_RED)
						rootCards.getChildren().addAll(redHi, redLo);
				}
			}
			else
			{
				if(guessHigher)
				{
					// graphics for red number to flash
					state = State.PAUSE;
					State next = State.CARDS_CHANGE_RED;
					if(redNextFlip == 0)
						next = State.CARDS_TURN_FIRST_RED_1;
					driver = new Driver(redInput, 0, next);
					manyBells.play();
					driver.start();

					rootCards.getChildren().add(redSquare);
					if(next == State.CARDS_CHANGE_RED)
						rootCards.getChildren().addAll(redHi, redLo);
				}
				else
				{
					// graphics for blue down arrow to flash
					state = State.PAUSE;
					State next = State.CARDS_CHANGE_BLUE;
					if(blueNextFlip == 0)
						next = State.CARDS_TURN_FIRST_BLUE_1;
					driver = new Driver(blueDown, 1, next);
					manyBells.play();
					driver.start();
					
					rootCards.getChildren().add(blueSquare);
					if(next == State.CARDS_CHANGE_BLUE)
						rootCards.getChildren().addAll(blueHi, blueLo);
				}
			}
			sceneContestants.setCursor(Cursor.DEFAULT);
			invisible.setFill(transparent);
			roundNumber++;
		}
		else if(state == State.QUESTION_DONE_BLUE_RED)
		{
			actualNumber.setText(String.format("%2s", Integer.toString(answers[roundNumber])));
			if(userGuess == answers[roundNumber])
			{
				// graphics for blue number to flash
				state = State.PAUSE;
				State next = State.CARDS_CHANGE_BLUE;
				if(blueNextFlip == 0)
					next = State.CARDS_TURN_FIRST_BLUE_1;
				driver = new Driver(blueInput, 0, next);
				manyBells.play();
				driver.start();
				
				rootCards.getChildren().add(blueSquare);
				if(next == State.CARDS_CHANGE_BLUE)
					rootCards.getChildren().addAll(blueHi, blueLo);
			}
			else if(answers[roundNumber] < userGuess)
			{
				if(!guessHigher)
				{
					// graphics for red down arrow to flash
					state = State.PAUSE;
					State next = State.CARDS_CHANGE_RED;
					if(redNextFlip == 0)
						next = State.CARDS_TURN_FIRST_RED_1;
					driver = new Driver(redDown, 1, next);
					manyBells.play();
					driver.start();

					rootCards.getChildren().add(redSquare);
					if(next == State.CARDS_CHANGE_RED)
						rootCards.getChildren().addAll(redHi, redLo);
				}
				else
				{
					// graphics for blue number to flash
					state = State.PAUSE;
					State next = State.CARDS_CHANGE_BLUE;
					if(blueNextFlip == 0)
						next = State.CARDS_TURN_FIRST_BLUE_1;
					driver = new Driver(blueInput, 0, next);
					manyBells.play();
					driver.start();
					
					rootCards.getChildren().add(blueSquare);
					if(next == State.CARDS_CHANGE_BLUE)
						rootCards.getChildren().addAll(blueHi, blueLo);
				}
			}
			else
			{
				if(guessHigher)
				{
					// graphics for red up arrow to flash
					state = State.PAUSE;
					State next = State.CARDS_CHANGE_RED;
					if(redNextFlip == 0)
						next = State.CARDS_TURN_FIRST_RED_1;
					driver = new Driver(redUp, 1, next);
					manyBells.play();
					driver.start();

					rootCards.getChildren().add(redSquare);
					if(next == State.CARDS_CHANGE_RED)
						rootCards.getChildren().addAll(redHi, redLo);
				}
				else
				{
					// graphics for blue number to flash
					state = State.PAUSE;
					State next = State.CARDS_CHANGE_BLUE;
					if(blueNextFlip == 0)
						next = State.CARDS_TURN_FIRST_BLUE_1;
					driver = new Driver(blueInput, 0, next);
					manyBells.play();
					driver.start();
					
					rootCards.getChildren().add(blueSquare);
					if(next == State.CARDS_CHANGE_BLUE)
						rootCards.getChildren().addAll(blueHi, blueLo);
				}
			}
			sceneContestants.setCursor(Cursor.DEFAULT);
			invisible.setFill(transparent);
			roundNumber++;
		}
	}
	
	// animation driver
	public class Driver extends AnimationTimer
    {
		Node n;
		int type;
		int stop;
		State nextState;
		
		public Driver(Node n, int type, State nextState)
		{
			this.n = n;
			this.type = type;
			this.nextState = nextState;
			stop = -1;
		}
		
    	@Override
    	public void handle(long now)
    	{
    		if(stop == -1)
    			stop = (int) (now / 1000000) + 2500;
    		flicker(n, type, (int) (now / 1000000), stop, nextState);
    	}
    }
	
	// animate this node so it flickers
	// type: 0 = textfield, 1 = polygon
	// stops flickering and advances the scene/state when now reaches stop
	private void flicker(Node n, int type, int now, int stop, State nextState)
	{
		now -= 100; // account for ~100 ms delay when starting the animation
		if(now >= stop)
		{
			driver.stop();
			n.getStyleClass().remove("selected");
			n.getStyleClass().remove("selected-arrow");
			state = nextState;
			stage.setScene(sceneCards);
			sceneContestants.setCursor(Cursor.DEFAULT);
			replaceCards();
			replacePlayPass();
			return;
		}
		
		if(type == 0)
		{
			if(((stop - now) / 150) % 2 == 0 && n.getStyleClass().contains("selected"))
			{
				n.getStyleClass().remove("selected");
			}
			else if(((stop - now) / 150) % 2 == 1 && !n.getStyleClass().contains("selected"))
			{
				n.getStyleClass().add("selected");
			}
		}
		else if(type == 1)
		{
			if(((stop - now) / 150) % 2 == 0 && n.getStyleClass().contains("selected-arrow"))
			{
				n.getStyleClass().remove("selected-arrow");
			}
			else if(((stop - now) / 150) % 2 == 1 && !n.getStyleClass().contains("selected-arrow"))
			{
				n.getStyleClass().add("selected-arrow");
			}
		}
	}
	
	// render the scene with the main cards
	private void renderCardsUI()
	{
		rootCards = new Pane();
		BackgroundImage backgroundCards = new BackgroundImage(new Image(new File("src/FinalProjectGlascock/images/scenes/cards.png").toURI().toString()),
				BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
		rootCards.setBackground(new Background(backgroundCards));
		sceneCards = new Scene(rootCards, 1140, 640);
		
		// freeze bars
		redBar.setX(cardX[0] - 10);
		redBar.setY(cardY[0] - 24);
		blueBar.setX(cardX[0] - 10);
		blueBar.setY(cardY[1] - 24);
		rootCards.getChildren().addAll(redBar, blueBar);
		
		// load card backs and put them on the scene
		mainCards = new ImageView[2][5];
		mainCardValues = new int[2][5];
		redBack = new Image(new File("src/FinalProjectGlascock/images/cards/red_back.png").toURI().toString(), 84, 120, false, true);
		blueBack = new Image(new File("src/FinalProjectGlascock/images/cards/blue_back.png").toURI().toString(), 84, 120, false, true);
		for(int i = 0 ; i < cardX.length; i++)
		{
			ImageView viewRed = new ImageView(redBack);
			placeCard(0, i, viewRed, -1);
			
			ImageView viewBlue = new ImageView(blueBack);
			placeCard(1, i, viewBlue, -1);
		}
		
		// play/pass buttons
		redPass = new ImageView(passImg);
		redPass.setX(94);
		redPass.setY(183);
		redPass.setOnMouseEntered(e -> {handlePlayPassHover(2, true);});
		redPass.setOnMouseExited(e -> {handlePlayPassHover(2, false);});
		redPass.setOnMouseClicked(e -> {handlePlayPassClick(2);});
		bluePass = new ImageView(passImg);
		bluePass.setX(94);
		bluePass.setY(183 + (362 - 148));
		bluePass.setOnMouseEntered(e -> {handlePlayPassHover(4, true);});
		bluePass.setOnMouseExited(e -> {handlePlayPassHover(4, false);});
		bluePass.setOnMouseClicked(e -> {handlePlayPassClick(4);});
		redPlay = new ImageView(playImg);
		redPlay.setX(94);
		redPlay.setY(183);
		redPlay.setOnMouseEntered(e -> {handlePlayPassHover(1, true);});
		redPlay.setOnMouseExited(e -> {handlePlayPassHover(1, false);});
		redPlay.setOnMouseClicked(e -> {handlePlayPassClick(1);});
		bluePlay = new ImageView(playImg);
		bluePlay.setX(94);
		bluePlay.setY(183 + (362 - 148));
		bluePlay.setOnMouseEntered(e -> {handlePlayPassHover(3, true);});
		bluePlay.setOnMouseExited(e -> {handlePlayPassHover(3, false);});
		bluePlay.setOnMouseClicked(e -> {handlePlayPassClick(3);});
		
		// hi lo buttons
		redHi = new Polygon(116, 416-214, 143, 383-214, 170, 416-214);
		redHi.setOnMouseEntered(me -> {handleHiLoHover(1, true);});
		redHi.setOnMouseExited(me -> {handleHiLoHover(1, false);});
		redHi.setOnMouseClicked(me -> {handleHiLoClick(1);});
		redLo = new Polygon(116, 431-214, 143, 464-214, 170, 431-214);
		redLo.setOnMouseEntered(me -> {handleHiLoHover(2, true);});
		redLo.setOnMouseExited(me -> {handleHiLoHover(2, false);});
		redLo.setOnMouseClicked(me -> {handleHiLoClick(2);});
		blueHi = new Polygon(116, 416, 143, 383, 170, 416);
		blueHi.setOnMouseEntered(me -> {handleHiLoHover(3, true);});
		blueHi.setOnMouseExited(me -> {handleHiLoHover(3, false);});
		blueHi.setOnMouseClicked(me -> {handleHiLoClick(3);});
		blueLo = new Polygon(116, 431, 143, 464, 170, 431);
		blueLo.setOnMouseEntered(me -> {handleHiLoHover(4, true);});
		blueLo.setOnMouseExited(me -> {handleHiLoHover(4, false);});
		blueLo.setOnMouseClicked(me -> {handleHiLoClick(4);});
		
		// turn indicators
		redSquare = new Rectangle(952, 181, 48, 48);
		redSquare.setStroke(Color.RED);
		redSquare.setStrokeWidth(5);
		redSquare.setFill(transparent);
		blueSquare = new Rectangle(952, 398, 48, 48);
		blueSquare.setStroke(Color.BLUE);
		blueSquare.setStrokeWidth(5);
		blueSquare.setFill(transparent);
		
		// freeze buttons
		redFreeze = new ImageView(redFreezeImg);
		redFreeze.setX(100);
		redFreeze.setY(100);
		redFreeze.setOnMouseEntered(e -> {handleFreezeHover(true, true);});
		redFreeze.setOnMouseExited(e -> {handleFreezeHover(true, false);});
		redFreeze.setOnMouseClicked(e -> {handleFreezeClick(true);});
		blueFreeze = new ImageView(blueFreezeImg);
		blueFreeze.setX(100);
		blueFreeze.setY(300);
		blueFreeze.setOnMouseEntered(e -> {handleFreezeHover(false, true);});
		blueFreeze.setOnMouseExited(e -> {handleFreezeHover(false, false);});
		blueFreeze.setOnMouseClicked(e -> {handleFreezeClick(false);});
		
		// continue button
		continueButton = new ImageView(continueButtonImg);
		continueButton.setX(930);
		continueButton.setY(300);
		continueButton.setOnMouseEntered(e -> {handleContinueHover(true);});
		continueButton.setOnMouseExited(e -> {handleContinueHover(false);});
		continueButton.setOnMouseClicked(e -> {handleContinueClick();});
	}
	
	// handle keypresses for the the cards scene
	public void addCardsKeyListener(Scene scene)
	{
		scene.setOnKeyPressed(e -> {
			if(e.getCode() == KeyCode.C)
			{
				if(state == State.CARDS_CHANGE_RED)
				{
					handleCardClick(redNextFlip - 1);
				}
				else if(state == State.CARDS_CHANGE_BLUE)
				{
					handleCardClick(blueNextFlip - 1 + 5);
				}
			}
			else if(e.getCode() == KeyCode.UP)
			{
				if(playPass)
				{
					if(rootCards.getChildren().contains(redPlay))
					{
						handlePlayPassClick(1);
					}
					else
					{
						handlePlayPassClick(2);
					}
					return;
				}
				if(state == State.CARDS_HI_LO_RED_1 || state == State.CARDS_HI_LO_RED_2 || state == State.CARDS_CHANGE_RED)
				{
					handleHiLoClick(1);
				}
				else if(state == State.CARDS_HI_LO_BLUE_1 || state == State.CARDS_HI_LO_BLUE_2 || state == State.CARDS_CHANGE_BLUE)
				{
					handleHiLoClick(3);
				}
			}
			else if(e.getCode() == KeyCode.DOWN)
			{
				if(playPass)
				{
					if(rootCards.getChildren().contains(bluePlay))
					{
						handlePlayPassClick(3);
					}
					else
					{
						handlePlayPassClick(4);
					}
					return;
				}
				if(state == State.CARDS_HI_LO_RED_1 || state == State.CARDS_HI_LO_RED_2 || state == State.CARDS_CHANGE_RED)
				{
					handleHiLoClick(2);
				}
				else if(state == State.CARDS_HI_LO_BLUE_1 || state == State.CARDS_HI_LO_BLUE_2 || state == State.CARDS_CHANGE_BLUE)
				{
					handleHiLoClick(4);
				}
			}
			else if(e.getCode() == KeyCode.ENTER)
			{
				if(state == State.CARDS_DONE || state == State.CARDS_FINISHED)
				{
					handleContinueClick();
				}
				else if(state == State.CARDS_TURN_FIRST_RED_1 || state == State.CARDS_TURN_FIRST_RED_2 ||state == State.CARDS_REVEAL_RED_1 ||
						state == State.CARDS_REVEAL_RED_2)
				{
					handleCardClick(redNextFlip);
				}
				else if(state == State.CARDS_TURN_FIRST_BLUE_1 || state == State.CARDS_TURN_FIRST_BLUE_2 ||state == State.CARDS_REVEAL_BLUE_1 ||
						state == State.CARDS_REVEAL_BLUE_2)
				{
					handleCardClick(blueNextFlip + 5);
				}
			}
			else if(e.getCode() == KeyCode.F)
			{
				if(rootCards.getChildren().contains(redFreeze))
				{
					handleFreezeClick(true);
				}
				else if(rootCards.getChildren().contains(blueFreeze))
				{
					handleFreezeClick(false);
				}
			}
		});
	}
	
	// animation driver
	public class SquareDriver extends AnimationTimer
    {
		int stop;
		State nextState;
		boolean red;
		
		public SquareDriver(boolean red)
		{
			this.red = red;
			stop = -1;
		}
		
    	@Override
    	public void handle(long now)
    	{
    		if(stop == -1)
    			stop = (int) (now / 1000000) + 3500;
    		flickerSquare((int) (now / 1000000), stop, red);
    	}
    }
	
	// flicker the contestants winnings at the end
	private void flickerSquare(int now, int stop, boolean red)
	{
		Rectangle rect = red ? redSquare : blueSquare;
		now -= 100; // account for ~100 ms delay when starting the animation
		if(now >= stop)
		{
			if(rootCards.getChildren().contains(rect))
				rootCards.getChildren().remove(rect);
			squareDriver.stop();
			return;
		}
		
		if(((stop - now) / 150) % 2 == 0 && rootCards.getChildren().contains(rect))
		{
			rootCards.getChildren().remove(rect);
		}
		else if(((stop - now) / 150) % 2 == 1 && !rootCards.getChildren().contains(rect))
		{
			rootCards.getChildren().add(rect);
		}
	}
	
	// replace cards that have been flipped but need to be cleared
	private void replaceCards()
	{
		for(int i = 1; i < 5; i++)
		{
			if(mainCardValues[0][i] != -1 && i > redFreezeBar) // maybe change to > redFreezeBar?
			{
				placeCard(0, i, new ImageView(redBack), -1);
			}
			if(mainCardValues[1][i] != -1 && i > blueFreezeBar)
			{
				placeCard(1, i, new ImageView(blueBack), -1);
			}
		}
	}
	
	// change cursor and image when hovering over freeze button
	private void handleFreezeHover(boolean red, boolean enter)
	{
		if(red)
		{
			if(enter)
			{
				redFreeze.setImage(redFreezeSelectImg);
				sceneCards.setCursor(Cursor.HAND);
			}
			else
			{
				redFreeze.setImage(redFreezeImg);
				sceneCards.setCursor(Cursor.DEFAULT);
			}
		}
		else
		{
			if(enter)
			{
				blueFreeze.setImage(blueFreezeSelectImg);
				sceneCards.setCursor(Cursor.HAND);
			}
			else
			{
				blueFreeze.setImage(blueFreezeImg);
				sceneCards.setCursor(Cursor.DEFAULT);
			}
		}
	}
	
	// freeze the board, if necessary
	private void handleFreezeClick(boolean red)
	{
		if(red)
		{
			removeFreezeButton(true);
			rootCards.getChildren().remove(redSquare);
			rootCards.getChildren().removeAll(redLo, redHi);
			redLo.setFill(Color.BLACK);
			redHi.setFill(Color.BLACK);
			transitionFreezeBar(true, redFreezeBar, redNextFlip - 1);
			redFreezeBar = redNextFlip - 1;
			redTurn = !redTurn;
			roundCount++;
			resetContestants();
			sceneContestants.setCursor(Cursor.DEFAULT);
			state = State.CARDS_DONE;
			rootCards.getChildren().add(continueButton);
		}
		else
		{
			removeFreezeButton(false);
			rootCards.getChildren().remove(blueSquare);
			rootCards.getChildren().removeAll(blueLo, blueHi);
			blueLo.setFill(Color.BLACK);
			blueHi.setFill(Color.BLACK);
			transitionFreezeBar(false, blueFreezeBar, blueNextFlip - 1);
			blueFreezeBar = blueNextFlip - 1;
			redTurn = !redTurn;
			roundCount++;
			resetContestants();
			sceneContestants.setCursor(Cursor.DEFAULT);
			state = State.CARDS_DONE;
			rootCards.getChildren().add(continueButton);
		}
	}
	
	// place the given freeze button in the given position
	private void placeFreezeButton(boolean red, int position)
	{
		ImageView button = (red ? redFreeze : blueFreeze);
		if(rootCards.getChildren().contains(button))
			rootCards.getChildren().remove(button);
		button.setX(cardX[position] - 8);
		button.setY(cardY[red ? 0 : 1] + 135);
		rootCards.getChildren().add(button);
	}
	
	// remove the given freeze button
	private void removeFreezeButton(boolean red)
	{
		ImageView button = (red ? redFreeze : blueFreeze);
		if(rootCards.getChildren().contains(button))
			rootCards.getChildren().remove(button);
	}
	
	// called every time a card is clicked to handle the freeze buttons
	private void replaceFreezeButton()
	{
		if(state != State.CARDS_HI_LO_RED_1 && state != State.CARDS_HI_LO_RED_2)
		{
			removeFreezeButton(true);
		}
		else if(redNextFlip > redFreezeBar + 1 && roundCount < questionsPerRound[redScore + blueScore])
		{
			placeFreezeButton(true, redNextFlip - 1);
		}
		
		if(state != State.CARDS_HI_LO_BLUE_1 && state != State.CARDS_HI_LO_BLUE_2)
		{
			removeFreezeButton(false);
		}
		else if(blueNextFlip > blueFreezeBar + 1 && roundCount != questionsPerRound[redScore + blueScore])
		{
			placeFreezeButton(false, blueNextFlip - 1);
		}
	}
	
	// change cursor when hovering over a selectable card
	private void handleCardHover(int card, boolean enter)
	{
		// don't do anything if waiting on user to decide play/pass
		if(playPass)
			return;
		
		if(!enter)
		{
			sceneCards.setCursor(Cursor.DEFAULT);
			return;
		}
		
		// red
		if(state == State.CARDS_CHANGE_RED) // can change or call hi lo
		{
			if(card == redNextFlip - 1)
			{
				sceneCards.setCursor(Cursor.HAND);
			}
		}
		else if(state == State.CARDS_TURN_FIRST_RED_1 || state == State.CARDS_TURN_FIRST_RED_2 || state == State.CARDS_REVEAL_RED_1 || state == State.CARDS_REVEAL_RED_2)
		{
			if(card == redNextFlip)
			{
				sceneCards.setCursor(Cursor.HAND);
			}
		}
		// blue
		else if(state == State.CARDS_CHANGE_BLUE) // can change or call hi lo
		{
			if(card - 5 == blueNextFlip - 1)
			{
				sceneCards.setCursor(Cursor.HAND);
			}
		}
		else if(state == State.CARDS_TURN_FIRST_BLUE_1 || state == State.CARDS_TURN_FIRST_BLUE_2 || state == State.CARDS_REVEAL_BLUE_1 || state == State.CARDS_REVEAL_BLUE_2)
		{
			if(card - 5 == blueNextFlip)
			{
				sceneCards.setCursor(Cursor.HAND);
			}
		}
	}
	
	// slide the freeze bar to the indicated position
	private void transitionFreezeBar(boolean red, int startPosition, int finalPosition)
	{
		ImageView bar = red ? redBar : blueBar;
		rootCards.getChildren().remove(bar);
		rootCards.getChildren().add(bar);
		TranslateTransition t = new TranslateTransition();
		t.setByX(cardX[finalPosition] - cardX[startPosition]);
		t.setAutoReverse(false);
		t.setNode(bar);
		t.setDuration(new Duration(1500 * (finalPosition - startPosition)));
		t.play();
	}
	
	// determine what to do when card is clicked
	private void handleCardClick(int card)
	{
		// don't do anything if waiting on user to decide play/pass
		if(playPass)
			return;
		
		// red
		if(state == State.CARDS_CHANGE_RED) // can change or call hi lo
		{
			if(card == redNextFlip - 1)
			{
				Card c = redDeck.deal();
				placeCard(0, card, c.getImageView(), c.getValue());
				state = State.CARDS_HI_LO_RED_1;
			}
		}
		else if(state == State.CARDS_TURN_FIRST_RED_1 || state == State.CARDS_TURN_FIRST_RED_2) // must flip first card
		{
			if(card == redNextFlip)
			{
				Card c = redDeck.deal();
				placeCard(0, card, c.getImageView(), c.getValue());
				redNextFlip++;
				rootCards.getChildren().addAll(redHi, redLo);
				if(state == State.CARDS_TURN_FIRST_RED_1)
					state = State.CARDS_CHANGE_RED;
				else
					state= State.CARDS_HI_LO_RED_2;
			}
		}
		else if(state == State.CARDS_REVEAL_RED_1 || state == State.CARDS_REVEAL_RED_2) // flip the next card
		{
			if(card == redNextFlip)
			{
				Card c = redDeck.deal();
				placeCard(0, card, c.getImageView(), c.getValue());
				if(((mainCardValues[0][redNextFlip] < mainCardValues[0][redNextFlip - 1]) ^ (cardGuessHigher))
						&& (mainCardValues[0][redNextFlip] != mainCardValues[0][redNextFlip - 1])) // guess was right
				{
					if(redNextFlip == 4 || redNextFlip == 2 && redScore == 1 && blueScore == 1) // red player wins the round
					{
						win1.play();
						squareDriver = new SquareDriver(true);
						squareDriver.start();
						rootCards.getChildren().removeAll(redLo, redHi);
						rootCards.getChildren().remove(redSquare);
						redLo.setFill(Color.BLACK);
						redHi.setFill(Color.BLACK);
						redScore++;
						updateBackground();
						gameOver = true;
						if(redScore == 2) // red wins the match and we go on to the money cards
						{
							state = State.CARDS_FINISHED;
							rootCards.getChildren().add(continueButton);
						}
						else if(blueScore == 1) // tiebreaker
						{
							redTurn = true;
							resetContestants();
							sceneContestants.setCursor(Cursor.DEFAULT);
							state = State.CARDS_DONE;
							rootCards.getChildren().add(continueButton);
							roundCount = 1;
						}
						else // red leads 1-0
						{
							redTurn = false;
							resetContestants();
							sceneContestants.setCursor(Cursor.DEFAULT);
							state = State.CARDS_DONE;
							rootCards.getChildren().add(continueButton);
							roundCount = 1;
						}
					}
					else // guess hi lo on next card
					{
						singleBell.play();
						state = state == State.CARDS_REVEAL_RED_1 ? State.CARDS_HI_LO_RED_1 : State.CARDS_HI_LO_RED_2;
						(cardGuessHigher ? redHi : redLo).setFill(Color.BLACK);
					}
				}
				else // wrong
				{
					buzzer.play();
					rootCards.getChildren().remove(redSquare);
					rootCards.getChildren().removeAll(redLo, redHi);
					redLo.setFill(Color.BLACK);
					redHi.setFill(Color.BLACK);
					redNextFlip = redFreezeBar;
					if(roundCount == questionsPerRound[redScore + blueScore]) // blue wins in sudden death
					{
						long time = System.nanoTime();
						while(System.nanoTime() - time < 2000000000)
						{
							
						}
						win1.play();
						squareDriver = new SquareDriver(false);
						squareDriver.start();
						blueScore++;
						updateBackground();
						gameOver = true;
						if(blueScore == 2) // blue wins entire game
						{
							state = State.CARDS_FINISHED;
							rootCards.getChildren().add(continueButton);
						}
						else // either 1-0 or 1-1 (either way, red starts next round)
						{
							redTurn = true;
							resetContestants();
							sceneContestants.setCursor(Cursor.DEFAULT);
							state = State.CARDS_DONE;
							rootCards.getChildren().add(continueButton);
							roundCount = 1;
						}
					}
					else if(state == State.CARDS_REVEAL_RED_1) // blue gets a shot
					{
						rootCards.getChildren().add(blueSquare);
						state = (blueNextFlip == 0) ? State.CARDS_TURN_FIRST_BLUE_2 : State.CARDS_HI_LO_BLUE_2;
						if(state == State.CARDS_HI_LO_BLUE_2)
							rootCards.getChildren().addAll(blueHi, blueLo);
					}
					else // back to podiums
					{
						redTurn = !redTurn;
						roundCount++;
						resetContestants();
						sceneContestants.setCursor(Cursor.DEFAULT);
						state = State.CARDS_DONE;
						rootCards.getChildren().add(continueButton);
					}
				}
				
				redNextFlip++;
			}
		}
		// blue
		else if(state == State.CARDS_CHANGE_BLUE) // can change or call hi lo
		{
			if(card-5 == blueNextFlip - 1)
			{
				Card c = blueDeck.deal();
				placeCard(1, card-5, c.getImageView(), c.getValue());
				state = State.CARDS_HI_LO_BLUE_1;
			}
		}
		else if(state == State.CARDS_TURN_FIRST_BLUE_1 || state == State.CARDS_TURN_FIRST_BLUE_2) // must flip first card
		{
			if(card-5 == blueNextFlip)
			{
				Card c = blueDeck.deal();
				placeCard(1, card-5, c.getImageView(), c.getValue());
				blueNextFlip++;
				rootCards.getChildren().addAll(blueHi, blueLo);
				if(state == State.CARDS_TURN_FIRST_BLUE_1)
					state = State.CARDS_CHANGE_BLUE;
				else
					state= State.CARDS_HI_LO_BLUE_2;
			}
		}
		else if(state == State.CARDS_REVEAL_BLUE_1 || state == State.CARDS_REVEAL_BLUE_2) // flip the next card
		{
			if(card-5 == blueNextFlip)
			{
				Card c = blueDeck.deal();
				placeCard(1, card-5, c.getImageView(), c.getValue());
				if(((mainCardValues[1][blueNextFlip] < mainCardValues[1][blueNextFlip - 1]) ^ (cardGuessHigher))
						&& (mainCardValues[1][blueNextFlip] != mainCardValues[1][blueNextFlip - 1])) // guess was right
				{
					if(blueNextFlip == 4 || blueNextFlip == 2 && redScore == 1 && blueScore == 1) // blue player wins the round
					{
						win1.play();
						squareDriver = new SquareDriver(false);
						squareDriver.start();
						rootCards.getChildren().remove(blueSquare);
						rootCards.getChildren().removeAll(blueLo, blueHi);
						blueLo.setFill(Color.BLACK);
						blueHi.setFill(Color.BLACK);
						blueScore++;
						updateBackground();
						gameOver = true;
						if(blueScore == 2) // blue wins the match and we go on to the money cards
						{
							state = State.CARDS_FINISHED;
							rootCards.getChildren().add(continueButton);
						}
						else // blue leads 1-0 or tiebreaker
						{
							redTurn = true;
							resetContestants();
							sceneContestants.setCursor(Cursor.DEFAULT);
							state = State.CARDS_DONE;
							rootCards.getChildren().add(continueButton);
							roundCount = 1;
						}
					}
					else // guess hi lo on next card
					{
						singleBell.play();
						state = state == State.CARDS_REVEAL_BLUE_1 ? State.CARDS_HI_LO_BLUE_1 : State.CARDS_HI_LO_BLUE_2;
						(cardGuessHigher ? blueHi : blueLo).setFill(Color.BLACK);
					}
				}
				else // wrong
				{
					buzzer.play();
					rootCards.getChildren().remove(blueSquare);
					rootCards.getChildren().removeAll(blueLo, blueHi);
					blueLo.setFill(Color.BLACK);
					blueHi.setFill(Color.BLACK);
					blueNextFlip = blueFreezeBar;
					if(roundCount == questionsPerRound[redScore + blueScore]) // red wins in sudden death
					{
						long time = System.nanoTime();
						while(System.nanoTime() - time < 2000000000)
						{
							
						}
						win1.play();
						squareDriver = new SquareDriver(true);
						squareDriver.start();
						redScore++;
						updateBackground();
						gameOver = true;
						if(redScore == 2) // red wins entire game
						{
							state = State.CARDS_FINISHED;
							rootCards.getChildren().add(continueButton);
						}
						else if(blueScore == 1) // tiebreaker
						{
							redTurn = true;
							resetContestants();
							sceneContestants.setCursor(Cursor.DEFAULT);
							state = State.CARDS_DONE;
							rootCards.getChildren().add(continueButton);
							roundCount = 1;
						}
						else // red leads 1-0
						{
							redTurn = false;
							resetContestants();
							sceneContestants.setCursor(Cursor.DEFAULT);
							state = State.CARDS_DONE;
							rootCards.getChildren().add(continueButton);
							roundCount = 1;
						}
					}
					else if(state == State.CARDS_REVEAL_BLUE_1) // red gets a shot
					{
						rootCards.getChildren().add(redSquare);
						state = (redNextFlip == 0) ? State.CARDS_TURN_FIRST_RED_2 : State.CARDS_HI_LO_RED_2;
						if(state == State.CARDS_HI_LO_RED_2)
							rootCards.getChildren().addAll(redHi, redLo);
					}
					else // back to podiums
					{
						redTurn = !redTurn;
						roundCount++;
						resetContestants();
						sceneContestants.setCursor(Cursor.DEFAULT);
						state = State.CARDS_DONE;
						rootCards.getChildren().add(continueButton);
					}
				}
				
				blueNextFlip++;
			}
		}
		
		replaceFreezeButton();
	}
	
	// cursor change when hovering over button
	private void handleContinueHover(boolean enter)
	{
		if(enter)
		{
			sceneCards.setCursor(Cursor.HAND);
			continueButton.setImage(continueButtonSelectImg);
		}
		else
		{
			sceneCards.setCursor(Cursor.DEFAULT);
			continueButton.setImage(continueButtonImg);
		}
	}
	
	// handle when the play/pass buttons are hovered
	// id 1 = redplay 2 = redpass 3 = blueplay 4 = bluepass
	private void handlePlayPassHover(int id, boolean enter)
	{
		if(enter)
		{
			(id <= 2 ? (id % 2 == 1 ? redPlay : redPass) : (id % 2 == 1 ? bluePlay : bluePass)).setImage(id % 2 == 1 ? playSelectImg : passSelectImg);
			sceneCards.setCursor(Cursor.HAND);
		}
		else
		{
			(id <= 2 ? (id % 2 == 1 ? redPlay : redPass) : (id % 2 == 1 ? bluePlay : bluePass)).setImage(id % 2 == 1 ? playImg : passImg);
			sceneCards.setCursor(Cursor.DEFAULT);
		}
	}
	
	// handle when the play/pass buttons are clicked
	// id 1 = redplay 2 = redpass 3 = blueplay 4 = bluepass
	private void handlePlayPassClick(int id)
	{
		if(rootCards.getChildren().contains(redPlay))
			rootCards.getChildren().remove(redPlay);
		if(rootCards.getChildren().contains(redPass))
			rootCards.getChildren().remove(redPass);
		if(rootCards.getChildren().contains(bluePlay))
			rootCards.getChildren().remove(bluePlay);
		if(rootCards.getChildren().contains(bluePass))
			rootCards.getChildren().remove(bluePass);
		
		playPass = false;
		if(id == 1)
		{
			state = (redNextFlip == 0) ? State.CARDS_TURN_FIRST_RED_1 : State.CARDS_CHANGE_RED;
			if(redNextFlip == 0)
				return;
			rootCards.getChildren().add(redHi);
			rootCards.getChildren().add(redLo);
			rootCards.getChildren().add(redSquare);
		}
		else if(id == 2)
		{
			state = (redNextFlip == 0) ? State.CARDS_TURN_FIRST_RED_2 : State.CARDS_HI_LO_RED_2;
			if(redNextFlip == 0)
				return;
			rootCards.getChildren().add(redHi);
			rootCards.getChildren().add(redLo);
			rootCards.getChildren().add(redSquare);
		}
		else if(id == 3)
		{
			state = (blueNextFlip == 0) ? State.CARDS_TURN_FIRST_BLUE_1 : State.CARDS_CHANGE_BLUE;
			if(blueNextFlip == 0)
				return;
			rootCards.getChildren().add(blueHi);
			rootCards.getChildren().add(blueLo);
			rootCards.getChildren().add(blueSquare);
		}
		else
		{
			state = (blueNextFlip == 0) ? State.CARDS_TURN_FIRST_BLUE_2 : State.CARDS_HI_LO_BLUE_2;
			if(blueNextFlip == 0)
				return;
			rootCards.getChildren().add(blueHi);
			rootCards.getChildren().add(blueLo);
			rootCards.getChildren().add(blueSquare);
		}
	}
	
	// continue the game back to contestants after done flipping cards
	private void handleContinueClick()
	{
		if(state == State.CARDS_FINISHED)
		{
			stage.setScene(sceneMoneyCards);
			redDeck.shuffle();
			Card c = redDeck.deal();
			placeMoneyCard(0, 0, c.getImageView(), c.getValue());
			state = State.MONEY_CARDS_CHANGE;
			return;
		}
		if(gameOver)
		{
			gameOver = false;
			resetCards();
		}
		stage.setScene(sceneContestants);
		state = (redTurn ? State.QUESTION_READING_RED : State.QUESTION_READING_BLUE);
		sceneContestants.setCursor(Cursor.DEFAULT);
		sceneCards.setCursor(Cursor.DEFAULT);
		rootCards.getChildren().remove(continueButton);
		continueButton.setImage(continueButtonImg);
	}
	
	// determine if play/pass buttons need to appear
	private void replacePlayPass()
	{
		if(rootCards.getChildren().contains(redPlay))
			rootCards.getChildren().remove(redPlay);
		if(rootCards.getChildren().contains(redPass))
			rootCards.getChildren().remove(redPass);
		if(rootCards.getChildren().contains(bluePlay))
			rootCards.getChildren().remove(bluePlay);
		if(rootCards.getChildren().contains(bluePass))
			rootCards.getChildren().remove(bluePass);
		
		
		// are we at the last question of the round?
		if(roundCount < questionsPerRound[redScore + blueScore])
			return;
		
		playPass = true;

		if(rootCards.getChildren().contains(redSquare))
			rootCards.getChildren().remove(redSquare);
		if(rootCards.getChildren().contains(blueSquare))
			rootCards.getChildren().remove(blueSquare);
		if(rootCards.getChildren().contains(redHi))
			rootCards.getChildren().remove(redHi);
		if(rootCards.getChildren().contains(redLo))
			rootCards.getChildren().remove(redLo);
		if(rootCards.getChildren().contains(blueHi))
			rootCards.getChildren().remove(blueHi);
		if(rootCards.getChildren().contains(blueLo))
			rootCards.getChildren().remove(blueLo);
		
		if(state == State.CARDS_CHANGE_RED || state == State.CARDS_TURN_FIRST_RED_1)
		{
			rootCards.getChildren().add(redPlay);
			rootCards.getChildren().add(bluePass);
		}
		else if(state == State.CARDS_CHANGE_BLUE || state == State.CARDS_TURN_FIRST_BLUE_1)
		{
			rootCards.getChildren().add(bluePlay);
			rootCards.getChildren().add(redPass);
		}
	}
	
	// reset the cards (and other stuff on that scene) between games
	private void resetCards()
	{
		redNextFlip = 0;
		redFreezeBar = 0;
		blueNextFlip = 0;
		blueFreezeBar = 0;
		redBar.setTranslateX(0);
		blueBar.setTranslateX(0);
		if(redScore == 1 && blueScore == 1)
		{
			BackgroundImage tiebreak = new BackgroundImage(new Image(new File("src/FinalProjectGlascock/images/scenes/cardstiebreaker.png").toURI().toString()),
					BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
			rootCards.setBackground(new Background(tiebreak));
			for(int i = 0 ; i < 3; i++)
			{
				ImageView viewRed = new ImageView(redBack);
				placeCard(0, i, viewRed, -1);
				
				ImageView viewBlue = new ImageView(blueBack);
				placeCard(1, i, viewBlue, -1);
			}
			for(int i = 3; i < cardX.length; i++)
			{
				removeCard(0, i);
				removeCard(1, i);
			}
		}
		else
		{
			for(int i = 0 ; i < cardX.length; i++)
			{
				ImageView viewRed = new ImageView(redBack);
				placeCard(0, i, viewRed, -1);
				
				ImageView viewBlue = new ImageView(blueBack);
				placeCard(1, i, viewBlue, -1);
			}
		}
	}
	
	// change background to indicate player has won
	private void updateBackground()
	{
		if(redScore > 1 || blueScore > 1)
			return;
		else if(redScore == 1 && blueScore == 0)
			rootContestants.setBackground(new Background(new BackgroundImage(new Image(new File("src/FinalProjectGlascock/images/scenes/contestantsred.jpg").toURI().toString()),
			        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
		else if(redScore == 0 && blueScore == 1)
			rootContestants.setBackground(new Background(new BackgroundImage(new Image(new File("src/FinalProjectGlascock/images/scenes/contestantsblue.jpg").toURI().toString()),
			        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
		else
			rootContestants.setBackground(new Background(new BackgroundImage(new Image(new File("src/FinalProjectGlascock/images/scenes/contestantsredblue.jpg").toURI().toString()),
			        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
	}
	
	// handle when hi/lo card buttons are hovered
	// 1 = redhi, 2=redlo, 3=bluehi, 4=bluelo
	private void handleHiLoHover(int id, boolean enter)
	{
		if((state == State.CARDS_HI_LO_RED_1 || state == State.CARDS_HI_LO_RED_2 || state == State.CARDS_CHANGE_RED) && (id == 1 || id == 2))
		{
			sceneCards.setCursor(enter ? Cursor.HAND : Cursor.DEFAULT);
			(id == 1 ? redHi : redLo).setFill(enter ? Color.GRAY : Color.BLACK);
		}
		else if((state == State.CARDS_HI_LO_BLUE_1 || state == State.CARDS_HI_LO_BLUE_2 || state == State.CARDS_CHANGE_BLUE) && (id == 3 || id == 4))
		{
			sceneCards.setCursor(enter ? Cursor.HAND : Cursor.DEFAULT);
			(id == 3 ? blueHi : blueLo).setFill(enter ? Color.GRAY : Color.BLACK);
		}
	}
	
	// handle when hi/lo card buttons are clicked
	private void handleHiLoClick(int id)
	{
		// update state
		if(state == State.CARDS_HI_LO_RED_1 || state == State.CARDS_CHANGE_RED)
			state = State.CARDS_REVEAL_RED_1;
		else if(state == State.CARDS_HI_LO_RED_2)
			state = State.CARDS_REVEAL_RED_2;
		else if(state == State.CARDS_HI_LO_BLUE_1 || state == State.CARDS_CHANGE_BLUE)
			state = State.CARDS_REVEAL_BLUE_1;
		else if(state == State.CARDS_HI_LO_BLUE_2)
			state = State.CARDS_REVEAL_BLUE_2;
		else
			return;
		
		cardGuessHigher = (id % 2 == 1);
		(id < 3 ? (id == 1 ? redHi : redLo) : (id == 3 ? blueHi : blueLo)).setFill(Color.GRAY);
		sceneCards.setCursor(Cursor.DEFAULT);
	}
	
	// render the scene with the money cards
	private void renderMoneyCardsUI()
	{
		rootMoneyCards = new Pane();
		BackgroundImage backgroundMoneyCards = new BackgroundImage(new Image(new File("src/FinalProjectGlascock/images/scenes/moneycards.png").toURI().toString()),
				BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
				BackgroundSize.DEFAULT);
		rootMoneyCards.setBackground(new Background(backgroundMoneyCards));
		sceneMoneyCards = new Scene(rootMoneyCards, 1140, 640);
		sceneMoneyCards.getStylesheets().add("FinalProjectGlascock/style.css");
		
		// deal initial cards
		moneyCards = new ImageView[3][4];
		moneyCardValues = new int[3][4];
		for(int i = 0; i < moneyCardCoordinatePairs.length; i++)
		{
			placeMoneyCard(moneyCardCoordinatePairs[i][0], moneyCardCoordinatePairs[i][1], new ImageView(redBack), -1);
		}
		
		// text field
		betInput = new TextField();
		betInput.getStyleClass().add("money-cards-text");
		Label betInputLabel = new Label("Bet: $");
		betInputLabel.setLabelFor(betInput);
		betInputLabel.setFont(Font.font("Verdana", 30));
		betInputLabel.setTextFill(Color.WHITE);
		HBox hb = new HBox();
		rootMoneyCards.getChildren().add(hb);
		hb.setAlignment(Pos.CENTER_LEFT);
		hb.setLayoutX(100);
		hb.setLayoutY(60);
		hb.getChildren().addAll(betInputLabel, betInput);
		ChangeListener<String> listener = ((observable, oldValue, newValue) -> { // listener for textfield
			handleBetInput(oldValue, newValue);
		});
		betInput.textProperty().addListener(listener);
		betInput.setOnAction(e -> {handleBetInputSubmit();});
		moneyDisplayBorder.setX(112);
		moneyDisplayBorder.setY(380);
		
		// hi/lo buttons
		betHi = new Polygon(154, 180, 201, 133, 248, 180);
		betHi.setOnMouseEntered(e -> {handleBetHiLoHover(true, true);});
		betHi.setOnMouseExited(e -> {handleBetHiLoHover(true, false);});
		betHi.setOnMouseClicked(e -> {handleBetHiLoClick(true);});
		betLo = new Polygon(154, 180+20, 201, 227+20, 248, 180+20);
		betLo.setOnMouseEntered(e -> {handleBetHiLoHover(false, true);});
		betLo.setOnMouseExited(e -> {handleBetHiLoHover(false, false);});
		betLo.setOnMouseClicked(e -> {handleBetHiLoClick(false);});
		
		// player's money total
		moneyDisplay = new Text("  200");
		moneyDisplay.getStyleClass().add("money-display");
		moneyDisplay.setX(130);
		moneyDisplay.setY(432);
		moneyDisplay.setFill(new Color(242.0 / 255, 211.0 / 255, 64.0 / 255, 1));
		rootMoneyCards.getChildren().add(moneyDisplay);
	}
	
	// handle keypresses for the the money cards scene
	public void addMoneyCardsKeyListener(Scene scene)
	{
		scene.setOnKeyPressed(e -> {
			if(e.getCode() == KeyCode.UP)
			{
				if(state == State.MONEY_CARDS_HI_LO)
				{
					handleBetHiLoClick(true);
				}
			}
			else if(e.getCode() == KeyCode.DOWN)
			{
				if(state == State.MONEY_CARDS_HI_LO)
				{
					handleBetHiLoClick(false);
				}
			}
			else if(e.getCode() == KeyCode.ENTER)
			{
				if(state == State.MONEY_CARDS_REVEAL)
				{
					handleMoneyCardClick(moneyCardCoordinatePairs[currentBet][0], moneyCardCoordinatePairs[currentBet][1]);
				}
			}
		});
	}
	
	// when mouse hovers over hi/lo buttons on money cards
	private void handleBetHiLoHover(boolean hi, boolean enter)
	{
		if(state != State.MONEY_CARDS_HI_LO)
		{
			return;
		}
		sceneMoneyCards.setCursor(enter ? Cursor.HAND : Cursor.DEFAULT);
		(hi ? betHi : betLo).setFill(enter ? Color.GRAY : Color.BLACK);
	}
	
	// when user clicks hi/lo buttons on money cards
	private void handleBetHiLoClick(boolean hi)
	{
		sceneMoneyCards.setCursor(Cursor.DEFAULT);
		(hi ? betHi : betLo).setFill(Color.GRAY);
		state = State.MONEY_CARDS_REVEAL;
		moneyBetHi = hi;
	}
	
	// when user enters bet
	private void handleBetInputSubmit()
	{
		if(state != State.MONEY_CARDS_BET && state != State.MONEY_CARDS_CHANGE)
		{
			System.out.println("Error - wrong state when submitting bet");
			return;
		}
		// verify bet is valid
		if(betInput.getText().length() == 0)
			return;
		int value = Integer.parseInt(betInput.getText());
		if(value <= 0 || value % 50 != 0 || value > money || currentBet == 6 && value < money / 2)
		{
			return;
		}
		
		state = State.MONEY_CARDS_HI_LO;
		betInput.setEditable(false);
		rootMoneyCards.requestFocus();
		rootMoneyCards.getChildren().addAll(betHi, betLo);
	}
	
	// prevalidate input for bet box
	public void handleBetInput(String oldValue, String newValue)
	{
		if(state != State.MONEY_CARDS_BET && state != State.MONEY_CARDS_CHANGE)
		{
			return;
		}
		
		if(state == State.MONEY_CARDS_CHANGE && newValue.length() != 0 && newValue.charAt(newValue.length() - 1) == 'c')
		{
			// change the card when c is detected (this textbox has focus, so this has to go here, not in the scene itself)
			handleMoneyCardClick(prev[currentBet][0], prev[currentBet][1]);
			betInput.setText(oldValue);
			return;
		}
		
		// enforce length
		if(newValue.length() > 5)
		{
			betInput.setText(oldValue);
			return;
		}
		
		// enforce 0-9
		if(newValue.length() != 0 && (newValue.charAt(newValue.length()-1) < '0' || newValue.charAt(newValue.length()-1) > '9'))
		{
			betInput.setText(oldValue);
			return;
		}
	}
	
	// handle when a money card is hovered
	private void handleMoneyCardHover(int row, int col, boolean enter)
	{
		// user is hovering over the next card to flip
		if(state == State.MONEY_CARDS_REVEAL && row == moneyCardCoordinatePairs[currentBet][0] && col == moneyCardCoordinatePairs[currentBet][1])
		{
			sceneMoneyCards.setCursor(enter ? Cursor.HAND : Cursor.DEFAULT);
		}
		
		// user is hovering over the card to change
		else if(state == State.MONEY_CARDS_CHANGE)
		{
			if(currentBet == 0 && row == prev[0][0] && col == prev[0][1] || currentBet == 3 && row == prev[3][0] && col == prev[3][1] ||
					currentBet == 6 && row == prev[6][0] && col == prev[6][1])
			{
				sceneMoneyCards.setCursor(enter ? Cursor.HAND : Cursor.DEFAULT);
			}
		}
	}
	
	// handle when a money card is clicked
	private void handleMoneyCardClick(int row, int col)
	{
		if(state == State.MONEY_CARDS_REVEAL)
		{
			// reveal this card
			if(row == moneyCardCoordinatePairs[currentBet][0] && col == moneyCardCoordinatePairs[currentBet][1])
			{
				// revert arrow colors and remove them
				betHi.setFill(Color.BLACK);
				betLo.setFill(Color.BLACK);
				rootMoneyCards.getChildren().removeAll(betHi, betLo);
				
				// revert cursor
				sceneMoneyCards.setCursor(Cursor.DEFAULT);
				
				// deal the card
				Card c = redDeck.deal();
				placeMoneyCard(row, col, c.getImageView(), c.getValue());
				
				// update money
				boolean correct = false;
				if(moneyCardValues[prev[currentBet][0]][prev[currentBet][1]] > moneyCardValues[row][col]) // next card is lower
				{
					if(moneyBetHi) // wrong
					{
						money -= Integer.parseInt(betInput.getText());
						if(currentBet == 6 || (money == 0 && currentBet > 2))
							longBuzzer.play();
						else
							buzzer.play();
					}
					else // correct
					{
						if(currentBet != 6)
							singleBell.play();
						else
							win2.play();
						money += Integer.parseInt(betInput.getText());
						correct = true;
					}
					moneyDisplay.setText(String.format("%5s", Integer.toString(money)));
				}
				else if(moneyCardValues[prev[currentBet][0]][prev[currentBet][1]] < moneyCardValues[row][col]) // next card is higher
				{
					if(!moneyBetHi) // wrong
					{
						money -= Integer.parseInt(betInput.getText());
						if(currentBet == 6 || (money == 0 && currentBet > 2))
							longBuzzer.play();
						else
							buzzer.play();
					}
					else // correct
					{
						if(currentBet != 6)
							singleBell.play();
						else
							win2.play();
						money += Integer.parseInt(betInput.getText());
						correct = true;
					}
					moneyDisplay.setText(String.format("%5s", Integer.toString(money)));
				}
				
				// make text editable again and clear it
				betInput.setText("");
				betInput.setEditable(true);
				betInput.requestFocus();
				
				// update current bet and transition card if necessary
				if(money == 0)
				{
					if(currentBet < 3)
					{
						currentBet = 3;
						// transition the card 
						transitionCard(row, col, 1, 0, true);
						
						// update the prev array since a different card moved up
						prev[3][0] = row;
						prev[3][1] = col;
					}
					else
						state = State.STOP;
				}
				else
				{
					if(currentBet == 2)
					{
						// transition the card
						transitionCard(row, col, 1, 0, true);
					}
					else if(currentBet == 5)
					{
						// transition the card
						transitionCard(row, col, 2, 0, false);
					}
					else if(currentBet == 6 && correct)
					{
						// flicker the contestants' final winnings
						wDriver = new WinningsDriver();
						wDriver.start();
					}
					currentBet++;
				}
				
				// update the state
				if(currentBet == 3 || currentBet == 6)
					state = State.MONEY_CARDS_CHANGE;
				else
					state = State.MONEY_CARDS_BET;

			}
		}
		else if(state == State.MONEY_CARDS_CHANGE)
		{
			// change the card
			if(currentBet == 0 && row == prev[0][0] && col == prev[0][1] || currentBet == 3 && row == prev[3][0] && col == prev[3][1] ||
					currentBet == 6 && row == prev[6][0] && col == prev[6][1])
			{
				Card c = redDeck.deal();
				
				// preserve translate x and y
				double x = moneyCards[row][col].getTranslateX();
				double y = moneyCards[row][col].getTranslateY();
				
				placeMoneyCard(row, col, c.getImageView(), c.getValue());

				moneyCards[row][col].setTranslateX(x);
				moneyCards[row][col].setTranslateY(y);
				
				// update state
				state = State.MONEY_CARDS_BET;
			}
		}
	}
	
	// slide the given card from (startRow, startCol) to (destRow, destCol)
	private void transitionCard(int startRow, int startCol, int destRow, int destCol, boolean ding)
	{
		ImageView card = moneyCards[startRow][startCol];
		rootMoneyCards.getChildren().remove(card);
		rootMoneyCards.getChildren().add(card);
		TranslateTransition t = new TranslateTransition();
		t.setByX(moneyCardX[destCol] - moneyCardX[startCol]);
		t.setByY(moneyCardY[destRow] - moneyCardY[startRow]);
		t.setAutoReverse(false);
		t.setNode(card);
		t.setDuration(new Duration(2000));
		if(ding)
		{
			// when card is done transitioning, update money and play ding
			t.setOnFinished(e -> {
				// wait an extra second
				long time = System.nanoTime();
				while(System.nanoTime() - time < 1000000000)
				{
					
				}
				singleBell.play();
				money += 200;
				moneyDisplay.setText(String.format("%5s", Integer.toString(money)));
		    });
		}
		t.play();
	}
	
	// animation driver
	public class WinningsDriver extends AnimationTimer
    {
		int stop;
		State nextState;
		
		public WinningsDriver()
		{
			stop = -1;
		}
		
    	@Override
    	public void handle(long now)
    	{
    		if(stop == -1)
    			stop = (int) (now / 1000000) + 3500;
    		flickerWinnings((int) (now / 1000000), stop);
    	}
    }
	
	// flicker the contestants winnings at the end
	private void flickerWinnings(int now, int stop)
	{
		now -= 100; // account for ~100 ms delay when starting the animation
		if(now >= stop)
		{
			if(rootMoneyCards.getChildren().contains(moneyDisplayBorder))
				rootMoneyCards.getChildren().remove(moneyDisplayBorder);
			wDriver.stop();
			return;
		}
		
		if(((stop - now) / 150) % 2 == 0 && rootMoneyCards.getChildren().contains(moneyDisplayBorder))
		{
			rootMoneyCards.getChildren().remove(moneyDisplayBorder);
		}
		else if(((stop - now) / 150) % 2 == 1 && !rootMoneyCards.getChildren().contains(moneyDisplayBorder))
		{
			rootMoneyCards.getChildren().add(moneyDisplayBorder);
		}
	}
	
	// set up the game state
	private void gameInit()
	{
		// make decks
		redDeck = new Deck();
		blueDeck = new Deck(redDeck);
	}
	
	// place a card in the given row/column indices in the money cards (note: rows go from 0-2 UP and cols go from 0-3 RIGHT)
	private void placeMoneyCard(int row, int col, ImageView card, int value)
	{
		// remove whatever was in the slot before
		if(moneyCards[row][col] != null)
		{
			rootMoneyCards.getChildren().remove(moneyCards[row][col]);
		}
		
		// add listeners
		card.setOnMouseClicked(e -> {handleMoneyCardClick(row, col);});
		card.setOnMouseEntered(e -> {handleMoneyCardHover(row, col, true);});
		card.setOnMouseExited(e -> {handleMoneyCardHover(row, col, false);});
		
		// place the card and update value
		card.setX(moneyCardX[col]);
		card.setY(moneyCardY[row]);
		rootMoneyCards.getChildren().add(card);
		moneyCards[row][col] = card;
		moneyCardValues[row][col] = value;
	}
	
	// place a card in the given row/column indices
	private void placeCard(int row, int col, ImageView card, int value)
	{
		// remove whatever was in the slot before
		if(mainCards[row][col] != null)
		{
			rootCards.getChildren().remove(mainCards[row][col]);
		}
		
		// add listeners
		card.setOnMouseClicked(e -> {handleCardClick(row*5 + col);});
		card.setOnMouseEntered(e -> {handleCardHover(row*5 + col, true);});
		card.setOnMouseExited(e -> {handleCardHover(row*5 + col, false);});
		
		// place the card
		card.setX(cardX[col]);
		card.setY(cardY[row]);
		rootCards.getChildren().add(card);
		mainCards[row][col] = card;
		mainCardValues[row][col] = value;
	}
	
	// remove the card from the given row/column indices, if it exists
	private void removeCard(int row, int col)
	{
		if(mainCards[row][col] != null)
		{
			rootCards.getChildren().remove(mainCards[row][col]);
		}
		mainCardValues[row][col] = -1;
	}
}
