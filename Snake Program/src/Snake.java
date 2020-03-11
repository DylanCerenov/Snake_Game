import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

/*
 * Dylan C.
 * Honors Computer Science II
 * June 3, 2019
 * TODO: make a high score function, make a lose screen with try again and quit, maybe multiple levels of play (easy medium hard),
 * make game less chunky
 */

public class Snake extends Application
{
	public enum Direction 
	{
		UP, DOWN, LEFT, RIGHT
	}
	
	public static final int BLOCK_SIZE = 32; // 24
	public static final int APP_W = 40 * BLOCK_SIZE; // originally 20 and 15 : 35 25
	public static final int APP_H = 23 * BLOCK_SIZE;
	public static double snakeSpeed = 0.10; // .14 seconds a new frame	EASY MODE
//	public static double snakeSpeed = 0.075; // 						NORM MODE
//	public static double snakeSpeed = .05; //							HARD MODE
	private Direction direction = Direction.RIGHT;
	private boolean moved = false;
	private boolean running = false;
	private Timeline timeline = new Timeline();
	private ObservableList<Node> snake;
	
	final int xWindowWidth = 1280;
	final int yWindowWidth = 720;
	String path = System.getProperty("user.dir");
	static int highscore;
	
	// random stuff that needed to be public and static.
	StackPane loseStackPane = new StackPane();
	Scene loseScene = new Scene(loseStackPane, xWindowWidth, yWindowWidth);
	Text prompt2;
	Image img_bg_game;
	BackgroundImage gameBackground;
	static Text[] leader;
	static Text l1 = new Text();
	static Text l2 = new Text();
	static Text l3 = new Text();
	static Text l4 = new Text();
	static Text l5 = new Text();
	
	//**********************************************
	// This is where the game is made.
	// Kept separate from the start()
	// for convienience. (testing)
	//**********************************************
	private Parent createContent(Stage primaryStage) 
	{
		Pane root = new Pane();
		root.setPrefSize(APP_W, APP_H);
		root.setBackground(new Background(gameBackground));
		
		Group snakeBody = new Group();
		snake = snakeBody.getChildren();
		
		Rectangle food = new Rectangle(BLOCK_SIZE,BLOCK_SIZE);
		food.setFill(Color.RED);
		food.setTranslateX((int) (Math.random() * (APP_H-BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
		food.setTranslateY((int) (Math.random() * (APP_H-BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
		
		KeyFrame frame = new KeyFrame(Duration.seconds(snakeSpeed), event ->
		{
			if (!running)
				return;
			
			boolean toRemove = snake.size() > 1;
			
			Node tail = toRemove ? snake.remove(snake.size()-1) : snake.get(0);
			((Shape) tail).setFill(Color.DEEPSKYBLUE);
			
			double tailX = tail.getTranslateX();
			double tailY = tail.getTranslateY();
			
			switch (direction) 
			{
				case UP: // up and down change y coord
					tail.setTranslateX(snake.get(0).getTranslateX());
					tail.setTranslateY(snake.get(0).getTranslateY() - BLOCK_SIZE);
					break;
				case DOWN:
					tail.setTranslateX(snake.get(0).getTranslateX());
					tail.setTranslateY(snake.get(0).getTranslateY() + BLOCK_SIZE);
					break;
				case LEFT: // left and right change x coord
					tail.setTranslateX(snake.get(0).getTranslateX() - BLOCK_SIZE);
					tail.setTranslateY(snake.get(0).getTranslateY());
					break;
				case RIGHT:
					tail.setTranslateX(snake.get(0).getTranslateX() + BLOCK_SIZE);
					tail.setTranslateY(snake.get(0).getTranslateY());
					break;
			}
			
			moved = true;
			
			if (toRemove)
				snake.add(0, tail);
			
			// collision detection
			// if snake hits snake block
			for (Node rect : snake)
			{
				if (rect != tail && tail.getTranslateX() == rect.getTranslateX() && tail.getTranslateY() == rect.getTranslateY())
				{
					restartGame(primaryStage);
					break;
				}
			}
			
			// if out of bounds
			if (tail.getTranslateX() < 0 || tail.getTranslateX() >= APP_W || tail.getTranslateY() < 0 || tail.getTranslateY() >= APP_H)
			{
				restartGame(primaryStage);
			}
			
			// if the snake eats food
			if (tail.getTranslateX() == food.getTranslateX() && tail.getTranslateY() == food.getTranslateY())
			{
				food.setTranslateX((int) (Math.random() * (APP_H-BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
				food.setTranslateY((int) (Math.random() * (APP_H-BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
				
				for (int i = 0; i < snake.size(); i++)
				{
					// if anywhere in the snake is in the same spot as the food
					if ((snake.get(i).getTranslateX() == food.getTranslateX()) && (snake.get(i).getTranslateY() == food.getTranslateY()))
					{
						// generate new food
						food.setTranslateX((int) (Math.random() * (APP_H-BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
						food.setTranslateY((int) (Math.random() * (APP_H-BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
						// reset i = 0; 
						i = 0;
					}
				}
				
				Rectangle rect = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);
				rect.setTranslateX(tailX);
				rect.setTranslateY(tailY);
				rect.setFill(Color.DEEPSKYBLUE);
				
				snake.add(rect);
				// making snake move slightly faster when it eats apple
//				snakeSpeed = snakeSpeed - 10;
			}
		});
		
		timeline.getKeyFrames().add(frame);
		timeline.setCycleCount(Timeline.INDEFINITE);
		root.getChildren().addAll(food, snakeBody);
		return root;
	}
	
	//******************************************
	// Method originally restarted the game,
	// hence the name, but it turned into a
	// method that just records the user
	// score and stops the game.
	//******************************************
	private void restartGame(Stage primaryStage)
	{
		highscore = snake.size();
		prompt2.setText("Your score was: " + highscore);
		
		stopGame();
		primaryStage.setScene(loseScene);
	}
	
	//***********************
	// Method stops the game.
	//***********************
	private void stopGame()
	{
		running = false;
		timeline.stop();
		snake.clear();
	}
	
	//****************************************
	// Method starts game by playing timeline.
	//****************************************
	private void startGame()
	{
		direction = Direction.RIGHT;
		Rectangle head = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);
		snake.add(head);
		timeline.play();
		running = true;
	}
	
	//**********************************************
	// Writes the leaderboard with score and name.
	//**********************************************
	public static void writeLeaderboard(String name)
	{
		try {
			FileWriter paperbackwriter = new FileWriter("leaderboard.txt", true);
			BufferedWriter out = new BufferedWriter(paperbackwriter);
			out.write(highscore + " " + name + "\n" + "");
		    out.close(); 
		} catch(IOException e) {
		    e.printStackTrace();
		}
	}
	
	//*************************************
	// Refreshes leaderboard
	//*************************************
	public static void refreshLeaderboard()
	{
		// sets data in the table
		ArrayList<String> stats = new ArrayList<String>();
					
		// get winners
		try (BufferedReader br = new BufferedReader(new FileReader("leaderboard.txt"))) {
		   String line;
		   while ((line = br.readLine()) != null) {
				stats.add(line);
		   }
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// sorts list by winners. highest snake length top
		int n = stats.size();  
        String temp;
        for(int i=0; i < n; i++)
        {
        	for(int j=1; j < (n-i); j++)
        	{
        		Scanner scan1 = new Scanner(stats.get(j-1));
        		Scanner scan2 = new Scanner(stats.get(j));
        		if (scan1.nextDouble() < scan2.nextDouble()) //if(arr[j-1] > arr[j])
        		{
        			//swap elements
        			temp = stats.get(j-1);//temp = arr[j-1];
        			stats.set(j-1, stats.get(j));//arr[j-1] = arr[j];
        			stats.set(j, temp);//arr[j] = temp;
        		}
        	}
        }
        
        // show top 5 players
        leader = new Text[5];
        for (int i = 0; i < leader.length; i++)
        {	
        	if (i < stats.size())
        	{
        		Scanner statsc = new Scanner(stats.get(i));
        		
        		int snakelength = statsc.nextInt();
        		String name = statsc.nextLine();
        		
	        	leader[i] = new Text(name + "     -     " + snakelength); //TODO
        	}
        	else
        		leader[i] = new Text("EMPTY");
        }
		l1.setText(leader[0].getText());
		l2.setText(leader[1].getText());
		l3.setText(leader[2].getText());
		l4.setText(leader[3].getText());
		l5.setText(leader[4].getText());
	}
	
	//************************************
	// Main method
	//************************************
	public static void main(String[] args)
	{
		launch(args);
	}

	//****************************************************************
	// Start method creates the scenes and makes everything pretty :-)
	//****************************************************************
	public void start(Stage primaryStage) throws IOException
	{
		// setting backgrounds and stuff
			// sky image
		Image img_bg_menu = new Image(new FileInputStream(path + "\\img_bg_menu.png"), xWindowWidth, yWindowWidth, true, false);
		BackgroundImage introBackground = new BackgroundImage(img_bg_menu, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
			// grass background
		img_bg_game = new Image(new FileInputStream(path + "\\img_bg_grass.png"), xWindowWidth, yWindowWidth+16, true, false);
		gameBackground = new BackgroundImage(img_bg_game, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
			// help background - meadow
		Image img_bg_help = new Image(new FileInputStream(path + "\\img_bg_meadow.png"), xWindowWidth, yWindowWidth, false, false);
		BackgroundImage helpBackground = new BackgroundImage(img_bg_help, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
			// lb background
		Image img_bg_lb = new Image(new FileInputStream(path + "\\img_bg_lb.png"), xWindowWidth, yWindowWidth, true, false);
		BackgroundImage lbBackground = new BackgroundImage(img_bg_lb, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
		
		// instantiates all the stackpanes and scenes required
			// intro scene
		StackPane introStackPane = new StackPane();
		Scene introScene = new Scene(introStackPane, xWindowWidth, yWindowWidth);
		introStackPane.setBackground(new Background(introBackground));
			// game scene
		Scene gameScene = new Scene(createContent(primaryStage));
			// leaderboard scene
		StackPane lbStackPane = new StackPane();
		Scene lbScene = new Scene(lbStackPane, xWindowWidth, yWindowWidth);
		lbStackPane.setBackground(new Background(lbBackground));
		
			// help scene
		StackPane helpStackPane = new StackPane();
		Scene helpScene = new Scene(helpStackPane, xWindowWidth, yWindowWidth);
		helpStackPane.setBackground(new Background(helpBackground));
			// lose scene
		loseStackPane.setBackground(new Background(gameBackground));
		
		
		// this one line that needed to be at the top of the code in order for the game to work.
		Button inputNameButton = new Button("SUBMIT");
		
		// intro scene ****************
		Text heading = new Text("Snake Game");
		heading.setFont(Font.font("Trebuchet MS",FontWeight.NORMAL,FontPosture.REGULAR,72));
		heading.setTranslateY(-280+50);
		introStackPane.getChildren().addAll(heading);
		
		Text subheading = new Text("by Dylan Cerenov");
		subheading.setFont(Font.font("Trebuchet MS",FontWeight.NORMAL,FontPosture.REGULAR,40));
		subheading.setTranslateY(-254+75);
		introStackPane.getChildren().addAll(subheading);
		
		Button playButton = new Button("PLAY");
		playButton.setOnAction(e -> 
		{ 
			inputNameButton.setDisable(false);
			startGame(); // starts the game
			primaryStage.setScene(gameScene);
		});
		playButton.setFont(new Font(36));
		playButton.setTranslateY(-80);
		playButton.setMinSize(200, 80);
		introStackPane.getChildren().addAll(playButton);
		
		Button lbButton = new Button("LEADERBOARD");
		lbButton.setOnAction(e -> 
		{ 
			refreshLeaderboard();
			primaryStage.setScene(lbScene);
		});
		lbButton.setFont(new Font(24));
		lbButton.setTranslateY(-80+100);
		lbButton.setMinSize(200, 80);
		introStackPane.getChildren().addAll(lbButton);
		
		Button helpButton = new Button("HELP");
		helpButton.setOnAction(e -> 
		{ 
			primaryStage.setScene(helpScene);
		});
		helpButton.setFont(new Font(36));
		helpButton.setTranslateY(-80+200);
		helpButton.setMinSize(200, 80);
		introStackPane.getChildren().addAll(helpButton);
		
		Button quitButton = new Button("QUIT");
		quitButton.setOnAction(e -> 
		{ 
			Stage stage = (Stage) quitButton.getScene().getWindow();
		    stage.close();
		});
		quitButton.setFont(new Font(36));
		quitButton.setTranslateY(-80+300);
		quitButton.setMinSize(200, 80);
		introStackPane.getChildren().addAll(quitButton);
		//****************
		
		
		
		// game scene ****************
		gameScene.setOnKeyPressed(event ->
		{
			if (!moved)
				return;
			
			switch (event.getCode())
			{
				case W:
					if (direction != Direction.DOWN)
						direction = Direction.UP;
					break;
				case S:
					if (direction != Direction.UP)
						direction = Direction.DOWN;
					break;
				case A:
					if (direction != Direction.RIGHT)
						direction = Direction.LEFT;
					break;
				case D: 
					if (direction != Direction.LEFT)
						direction = Direction.RIGHT;
					break;
				case UP:
					if (direction != Direction.DOWN)
						direction = Direction.UP;
					break;
				case DOWN:
					if (direction != Direction.UP)
						direction = Direction.DOWN;
					break;
				case LEFT:
					if (direction != Direction.RIGHT)
						direction = Direction.LEFT;
					break;
				case RIGHT: 
					if (direction != Direction.LEFT)
						direction = Direction.RIGHT;
					break;
			}
			
			moved = false;
		});
		//****************
				
		

		// help information scene ****************
		final ImageView imgVw_snake = new ImageView();
	    final Image img_snake = new Image(new FileInputStream(path + "\\img_snake.png"));
	    imgVw_snake.setImage(img_snake);
	    imgVw_snake.setFitHeight(400);
	    imgVw_snake.setFitWidth(240);
	    imgVw_snake.setTranslateY(160);
	    imgVw_snake.setTranslateX(-430);
	    helpStackPane.getChildren().addAll(imgVw_snake);
	    
	    final ImageView imgVw_apple = new ImageView();
	    final Image img_apple = new Image(new FileInputStream(path + "\\img_apple.png"));
	    imgVw_apple.setImage(img_apple);
	    imgVw_apple.setFitHeight(150);
	    imgVw_apple.setFitWidth(150);
	    imgVw_apple.setTranslateY(240);
	    imgVw_apple.setTranslateX(480);
	    helpStackPane.getChildren().addAll(imgVw_apple);
		
		Text helpInfo = new Text();
		helpInfo.setText("Snake is a video game where the player maneuvers a line on a bordered plane. As the line moves, it leaves behind a trail, and the trial gets longer as the game continues and the player eats more food objects. The primary objective of the game is to make the line you control as long as possible without colliding into barriers or yourself.");
		helpInfo.setFont(Font.font("Trebuchet MS",FontWeight.NORMAL,FontPosture.REGULAR,32));
		helpInfo.setWrappingWidth(600);
		helpInfo.setTranslateY(-100);
		helpStackPane.getChildren().addAll(helpInfo);
		
		Button returnButton = new Button("MENU");
		returnButton.setOnAction(e ->
		{
			primaryStage.setScene(introScene);
		});
		returnButton.setFont(new Font(36));
		returnButton.setTranslateY(200);
		returnButton.setMinSize(200, 80);
		helpStackPane.getChildren().addAll(returnButton);
		//****************
		
		
		
		// lose and restart game scene ****************
		Text prompt1 = new Text("You lost!");
		prompt1.setFont(Font.font("Trebuchet MS",FontWeight.NORMAL,FontPosture.REGULAR,48));
		prompt1.setTranslateY(-230);
		loseStackPane.getChildren().addAll(prompt1);
		
		prompt2 = new Text();
		prompt2.setFont(Font.font("Trebuchet MS",FontWeight.NORMAL,FontPosture.REGULAR,36));
		prompt2.setTranslateY(-230+46);
		loseStackPane.getChildren().addAll(prompt2);
		
		Button continueBtn = new Button("CONTINUE");
		continueBtn.setOnAction(e ->
		{
			primaryStage.setScene(introScene);
		});
		continueBtn.setFont(new Font(30));
		continueBtn.setTranslateY(-80);
		continueBtn.setMinSize(200, 80);
		loseStackPane.getChildren().addAll(continueBtn);
		
		Text prompt2 = new Text("Enter your name below to be put on the leaderboard:");
		prompt2.setFont(Font.font("Trebuchet MS",FontWeight.NORMAL,FontPosture.REGULAR,24));
		prompt2.setTranslateY(100);
		loseStackPane.getChildren().addAll(prompt2);
		
		// textfield to enter name
		TextField inputName = new TextField();
		inputName.setMaxWidth(250);
		inputName.setTranslateY(130);
		loseStackPane.getChildren().addAll(inputName);
		
		// submit name button
		inputNameButton.setOnAction(e -> 
		{
			writeLeaderboard(inputName.getText());
			inputNameButton.setDisable(true);
		});
		inputNameButton.setTranslateY(160);
		loseStackPane.getChildren().addAll(inputNameButton);
		
		//****************
		
		// Leaderboard scene ****************
		int x = -200;
		int y = -110;
		int fontsize = 32;
		int d = 300;
		
		Text lbHeading = new Text("Leaderboard");
		lbHeading.setFont(Font.font("Trebuchet MS",FontWeight.NORMAL,FontPosture.REGULAR,60));
		lbHeading.setTranslateY(-230);
		lbStackPane.getChildren().addAll(lbHeading);
		
		//1st
		Text place1 = new Text("1st place:");
		place1.setFont(Font.font("Trebuchet MS",FontWeight.NORMAL,FontPosture.REGULAR,fontsize));
		place1.setTranslateX(x);
		place1.setTranslateY(y);
		lbStackPane.getChildren().addAll(place1);
			l1.setFont(Font.font("Trebuchet MS",FontWeight.NORMAL,FontPosture.REGULAR,fontsize));
			l1.setTranslateX(x + d);
			l1.setTranslateY(y);
			lbStackPane.getChildren().addAll(l1);
		//2nd
		Text place2 = new Text("2nd place:");
		place2.setFont(Font.font("Trebuchet MS",FontWeight.NORMAL,FontPosture.REGULAR,fontsize));
		place2.setTranslateX(x);
		place2.setTranslateY(y + 1*(fontsize + 10));
		lbStackPane.getChildren().addAll(place2);
			l2.setFont(Font.font("Trebuchet MS",FontWeight.NORMAL,FontPosture.REGULAR,fontsize));
			l2.setTranslateX(x + d);
			l2.setTranslateY(y + 1*(fontsize + 10));
			lbStackPane.getChildren().addAll(l2);
		//3rd
		Text place3 = new Text("3rd place:");
		place3.setFont(Font.font("Trebuchet MS",FontWeight.NORMAL,FontPosture.REGULAR,fontsize));
		place3.setTranslateX(x);
		place3.setTranslateY(y + 2*(fontsize + 10));
		lbStackPane.getChildren().addAll(place3);
			l3.setFont(Font.font("Trebuchet MS",FontWeight.NORMAL,FontPosture.REGULAR,fontsize));
			l3.setTranslateX(x + d);
			l3.setTranslateY(y + 2*(fontsize + 10));
			lbStackPane.getChildren().addAll(l3);
		//4th
		Text place4 = new Text("4th place:");
		place4.setFont(Font.font("Trebuchet MS",FontWeight.NORMAL,FontPosture.REGULAR,fontsize));
		place4.setTranslateX(x);
		place4.setTranslateY(y + 3*(fontsize + 10));
		lbStackPane.getChildren().addAll(place4);
			l4.setFont(Font.font("Trebuchet MS",FontWeight.NORMAL,FontPosture.REGULAR,fontsize));
			l4.setTranslateX(x + d);
			l4.setTranslateY(y + 3*(fontsize + 10));
			lbStackPane.getChildren().addAll(l4);
		//5th
		Text place5 = new Text("5th place:");
		place5.setFont(Font.font("Trebuchet MS",FontWeight.NORMAL,FontPosture.REGULAR,fontsize));
		place5.setTranslateX(x);
		place5.setTranslateY(y + 4*(fontsize + 10));
		lbStackPane.getChildren().addAll(place5);
			l5.setFont(Font.font("Trebuchet MS",FontWeight.NORMAL,FontPosture.REGULAR,fontsize));
			l5.setTranslateX(x + d);
			l5.setTranslateY(y + 4*(fontsize + 10));
			lbStackPane.getChildren().addAll(l5);
		
		
		Button returnButton2 = new Button("MENU");
		returnButton2.setOnAction(e ->
		{
			primaryStage.setScene(introScene);
		});
		returnButton2.setFont(new Font(36));
		returnButton2.setTranslateY(200);
		returnButton2.setMinSize(200, 80);
		lbStackPane.getChildren().addAll(returnButton2);
		//****************
		
		
		
		primaryStage.setScene(introScene);
		primaryStage.setResizable(false);
		primaryStage.sizeToScene();
		primaryStage.setTitle("Snake Program");
		primaryStage.show();
	}
}