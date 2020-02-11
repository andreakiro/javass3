package javass3.gui;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javass3.Main;
import javass3.jass.components.Card.Color;
import javass3.jass.ids.PlayerId;
import names.DataNames;

public final class Interface {

	private final static String RANDOM_NAMES = "filtered";
	private final static String BUTTON_STYLE = "-fx-background-color: #e7e7e7; -fx-background-radius: 12px;";
	
	private final static int MAX_PARAM = 5;
	private final static double TRUMP_SIZE = 80d;

	private Stage stage;
	private final Scene scene;

	private final BooleanProperty primitivePaneVisible = new SimpleBooleanProperty(true);

	private final List<TextField> textfields = new ArrayList<>();
	private final List<ChoiceBox<String>> choicebox = new ArrayList<>();
	private final List<TextField> options = new ArrayList<>();

	/**
	 * Constructor of the game interface
	 */
	public Interface() {
		BorderPane primitive = new BorderPane();
		BorderPane settings = new BorderPane();

		primitive.setTop(createTitlePane("Welcome !"));
		primitive.setCenter(createPrimitiveChoicePane());
		primitive.setBottom(createPrimitiveExitPane());
		primitive.visibleProperty().bind(primitivePaneVisible);

		settings.setTop(createTitlePane("Set your game !"));
		settings.setCenter(createSettingsPane());
		settings.setBottom(createSettingsButtonPane());
		settings.visibleProperty().bind(primitivePaneVisible.not());
		
		StackPane sp = new StackPane(primitive, settings);
		this.scene = new Scene(sp);
	}
	
	/**
	 * Create the game interface stage
	 * 
	 * @return (Stage) : the stage
	 */
	public Stage createStage() {
		stage = new Stage();
		stage.setScene(scene);
		stage.sizeToScene();
		stage.setTitle("Javass");
		stage.setOnCloseRequest(g -> System.exit(0));
		return stage;
	}

	/**
	 * Create the most primitive pane of the game, where you choose to play a local
	 * game or to be a remote player
	 * 
	 * @return (VBox) : choice pane
	 */
	private GridPane createPrimitiveChoicePane() {
		Button local = new Button("Master");
		Button remote = new Button("I'm distant");

		local.setStyle(BUTTON_STYLE);
		remote.setStyle(BUTTON_STYLE);

		setEffectOn(local);
		setEffectOn(remote);

		local.setOnAction(g -> primitivePaneVisible.set(false));

		remote.setOnAction(g -> {
			Main.startRemoteGame();
			stage.close();
		});

		GridPane gp = new GridPane();
		gp.setStyle("-fx-background-color: whitesmoke; -fx-padding: 5px; -fx-border-width: 3px 0px;"
				+ " -fx-border-style: solid; -fx-border-color: gray; -fx-alignment: center;");

		// magic numbers stands for nodes positions
		
		gp.add(local, 2, 1);
		gp.add(remote, 2, 2);

		for (int i = 0; i < Color.COUNT; i++) {
			ImageView trumpView = new ImageView();
			trumpView.setImage(new Image("/trump_" + i + ".png"));
			trumpView.setFitHeight(TRUMP_SIZE);
			trumpView.setFitWidth(TRUMP_SIZE);
			gp.add(trumpView, i < 2 ? i : i + 1, 1);
		}

		GridPane.setHalignment(local, HPos.CENTER);
		GridPane.setValignment(local, VPos.CENTER);

		GridPane.setHalignment(remote, HPos.CENTER);
		GridPane.setValignment(remote, VPos.CENTER);

		return gp;
	}

	/**
	 * Create the primitive exit pane, to quit the game
	 * 
	 * @return (HBox) : the primitive exit pane
	 */
	private HBox createPrimitiveExitPane() {
		Button button = new Button("Exit");
		button.setStyle(BUTTON_STYLE);
		setEffectOn(button);

		button.setOnAction(g -> System.exit(0));

		HBox hbox = new HBox(button);
		hbox.setAlignment(Pos.CENTER);

		return hbox;
	}

	/**
	 * Create title pane for the interface
	 * 
	 * @return (HBox) : the title pane
	 */
	private HBox createTitlePane(String s) {
		HBox hbox = new HBox(new Text(s));
		hbox.setAlignment(Pos.CENTER);
		return hbox;
	}

	/**
	 * Create the settings pane, where you set the game as a local player
	 * 
	 * @return (GridPane) : the Settings pane
	 */
	private GridPane createSettingsPane() {
		GridPane gp = new GridPane();
		gp.setStyle("-fx-background-color: whitesmoke; -fx-padding: 5px; -fx-border-width: 3px 0px;"
				+ " -fx-border-style: solid; -fx-border-color: gray; -fx-alignment: center;");

		ObservableList<String> types = FXCollections.observableArrayList("Human", "Simulated", "Remote");

		for (int i = 0; i < PlayerId.COUNT; i++) {
			TextField tf = new TextField();
			tf.setPromptText("Name");
			ChoiceBox<String> cb = new ChoiceBox<>(FXCollections.unmodifiableObservableList(types));
			cb.getSelectionModel().select(i == 0 ? 0 : 1);
			TextField option = new TextField();
			option.setPromptText(translateOptions(cb.getValue()));
			cb.setOnAction(g -> option.setPromptText(translateOptions(cb.getValue())));
			textfields.add(tf);
			choicebox.add(cb);
			options.add(option);
			Label l = new Label("Player " + (i + 1) + " : ");
			gp.addRow(i, l, tf, cb, option);
		}

		return gp;
	}

	/**
	 * Create the settings start button / backward button pane
	 * 
	 * @return (HBox) : the settings buttons pane
	 */
	private HBox createSettingsButtonPane() {
		Button start = new Button("Start");
		Button backward = new Button("Go back");
		Button simulation = new Button("Simulate");

		start.setStyle(BUTTON_STYLE);
		backward.setStyle(BUTTON_STYLE);
		simulation.setStyle(BUTTON_STYLE);

		setEffectOn(start);
		setEffectOn(backward);
		setEffectOn(simulation);

		start.setOnAction(g -> {
			List<String> parameters = merge();
			Main.startLocalGame(parameters);
			stage.close();
		});

		backward.setOnAction(g -> primitivePaneVisible.set(true));
		
		simulation.setOnAction(g -> {
			List<String> parameters = Arrays.asList("z", "s", "s", "s");
			Main.startLocalGame(parameters);
			stage.close();
		});

		HBox hbox = new HBox();
		hbox.getChildren().addAll(start, simulation, backward);
		hbox.setAlignment(Pos.CENTER);

		return hbox;
	}

	/**
	 * Merge all the parameters to get the list of arguments required to launch a
	 * local game
	 * 
	 * @return (List<String) : arguments to launch local game
	 */
	private List<String> merge() {
		List<String> parameters = new ArrayList<>();
		List<String> names = computeNames();
		List<String> types = computeTypes();

		// regular parameters
		for (int i = 0; i < PlayerId.COUNT; i++) {
			StringJoiner sj = new StringJoiner(":");
			sj.add(types.get(i));
			sj.add(names.get(i));
			String opt = this.options.get(i).getText();
			if (isValidOption(types.get(i), opt))
				sj.add(opt);
			parameters.add(sj.toString());
		}

		// optionnal seed
		for (int i = 0; i < PlayerId.COUNT; i++) {
			String opt = this.options.get(i).getText();
			boolean h = types.get(i).equals("h");
			boolean valid = !opt.isEmpty();
			boolean size = parameters.size() < MAX_PARAM;
			if (h && valid && size)
				parameters.add(opt);
		}

		return parameters;
	}

	/**
	 * Compute an array containing the names of the players, if the text field is
	 * blank a random name is computed
	 * 
	 * @return (List<String>) : types of the players
	 */
	private List<String> computeNames() {
		List<String> names = new ArrayList<>();
		for (int i = 0; i < PlayerId.COUNT; i++) {
			String n = textfields.get(i).getText();
			if (n.isEmpty())
				n = DataNames.RANDOM_NAMES[new Random().nextInt(DataNames.RANDOM_NAMES.length)];
			names.add(n);
		}
		return names;
	}
	
	/**
	 * Compute an array containing the names of the players, if the text field is
	 * blank a random name is computed
	 * 
	 * @return (List<String>) : types of the players
	 */
	@SuppressWarnings("unused")
	private List<String> computeNamesDirectlyFromFile() {
		List<String> names = new ArrayList<>();
		for (int i = 0; i < PlayerId.COUNT; i++) {
			String n = textfields.get(i).getText();
			if (n.isEmpty())
				try {
					n = generateRandomName(RANDOM_NAMES, generateRandomInt(sizeOf(RANDOM_NAMES)));
				} catch (IOException e) {
					throw new Error("Generating the random name went wrong");
				}
			names.add(n);
		}
		return names;
	}

	/**
	 * Compute an array containing the types of the players
	 * 
	 * @return (List<String>) : types of the players
	 */
	private List<String> computeTypes() {
		List<String> types = new ArrayList<>();
		for (int i = 0; i < PlayerId.COUNT; i++) {
			String t = choicebox.get(i).getValue().toString();
			types.add(String.valueOf(t.toLowerCase().charAt(0)));
		}
		return types;
	}

	/**
	 * Translate the given string into an option
	 * 
	 * @param s (String) : the type of the player
	 * @return (String) : the option text
	 */
	private String translateOptions(String s) {
		switch (s) {
		case "Human":
			return "optionnal: game seed";
		case "Simulated":
			return "optionnal: mcts iterations";
		case "Remote":
			return "needed: IP adress";
		default:
			return null;
		}
	}

	/**
	 * Filter the options we don't want to merge into the array containing the
	 * parameters to launch local main
	 * 
	 * @param player (String) : the type of the player (h, s, r)
	 * @param opt (String) : the option string
	 * @return (boolean) : true iff option is valid
	 */
	private boolean isValidOption(String player, String opt) {
		boolean h = player.equals("h");
		boolean s = opt.equals(translateOptions("Simulated"));
		boolean r = opt.equals(translateOptions("Remote"));
		boolean n = opt.isEmpty();
		return !(h || s || r || n);
	}

	/**
	 * Set a drop shadow effect on the given node
	 * @param n
	 */
	private void setEffectOn(Node n) {
		n.setOnMouseEntered(g -> {
			DropShadow ds = new DropShadow();
			ds.setOffsetY(3.0); // 3 stands for effect
			ds.setOffsetX(3.0);
			ds.setColor(javafx.scene.paint.Color.DARKGRAY);
			n.setEffect(ds);
		});

		n.setOnMouseExited(g -> {
			n.setEffect(null);
		});
	}

	/**
	 * Generate a random integer between 0 and the upper range
	 * 
	 * @param upperRange (int)
	 * @return (int) : random integer
	 */
	private static int generateRandomInt(int upperRange) {
		return new Random().nextInt(upperRange);
	}
	
	private static int sizeOf(String file) throws IOException {
		InputStream stream = new FileInputStream(file);
		int count = 0;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
			while (br.readLine() != null) count++;
		}
		return count;
	}

	/**
	 * Generate a random name picking up from a file
	 * 
	 * @param file   (String) : the file containing all the names
	 * @param random (int) : the name we want from the file
	 * @return (String) : random name for simulated players
	 * @throws IOException
	 */
	private static String generateRandomName(String file, int random) throws IOException {
		InputStream stream = new FileInputStream(file);
		StringBuilder name = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
			String line;
			int count = 0;
			while ((line = br.readLine()) != null) {
				count++;
				if (count == random)
					name.append(line).append("\n");
			}
		}
		return polishString(name.toString());
	}

	/**
	 * Polish a string, ie remove all space characters and line breaks
	 * 
	 * @param s (String)
	 * @return (String) : "polished" string
	 */
	private static String polishString(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c != ' ' && c != '\n')
				sb.append(c);
		}
		return sb.toString();
	}
}
