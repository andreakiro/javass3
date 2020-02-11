package javass3.gui;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javass3.gui.beans.HandBean;
import javass3.gui.beans.ScoreBean;
import javass3.gui.beans.TrickBean;
import javass3.gui.beans.TrumpBean;
import javass3.jass.Jass;
import javass3.jass.components.Card;
import javass3.jass.components.Card.Color;
import javass3.jass.components.Card.Rank;
import javass3.jass.ids.PlayerId;
import javass3.jass.ids.TeamId;

public final class GraphicalPlayer {
	
	private final static int V_GAP_WAITING_PANE = 100;
    private final static int ANIMATION_HEIGHT = 150;
    private final static int ANIMATION_WIDTH = 72;
    private final static int CHOOSING_TRUMP = 120; 

	private final static int SMALL_SIZE = 160;
	private final static int LARGE_SIZE = 240;

	private final static double SMALL_CARD_IMAGE_HEIGHT = 120d;
	private final static double SMALL_CARD_IMAGE_WIDTH = 80d;

	private final static double LARGE_CARD_IMAGE_HEIGHT = 180d;
	private final static double LARGE_CARD_IMAGE_WIDTH = 120d;

	private final static double TRUMP_IMAGE_HEIGHT = 101d;
	private final static double TRUMP_IMAGE_WIDTH = TRUMP_IMAGE_HEIGHT;

	private final static double PLAYABLE_OPACITY = 1d;
	private final static double NON_PLAYABLE_OPACITY = 0.2;
	private final static double GAUSSIAN_EFFECT = 4d;
	
	private final static String BUTTON_STYLE = "-fx-background-color: #e7e7e7; -fx-background-radius: 12px;";

	private Stage stage;
	private final Scene scene;
	private final PlayerId ownId;
	private final Map<PlayerId, String> names;

	/**
	 * Constructor for a graphical player
	 * 
	 * @param id    (PlayerId)
	 * @param names (Map<PlayerId, String>)
	 * @param sb    (ScoreBean)
	 * @param tb    (TrickBean)
	 * @param hb    (HandBean)
	 */
	public GraphicalPlayer(PlayerId id, Map<PlayerId, String> names, ScoreBean sb, TrickBean tb, HandBean hb,
			TrumpBean tpb, ArrayBlockingQueue<Card> queue, ArrayBlockingQueue<Color> trumpQ,
			ArrayBlockingQueue<Boolean> passQ) {
		this.ownId = id;
		this.names = names;

		ObservableMap<Card, Image> largeImageCard = constructImageCard(false); // boolean stands for card size
		ObservableMap<Card, Image> smallImageCard = constructImageCard(true);
		ObservableMap<Color, Image> imageTrump = constructImageTrump();
		ObservableMap<TeamId, String> teamName = constructTeamName();

		StackPane central = new StackPane(createTrickPane(tb, hb, largeImageCard, imageTrump),
				createTrumpPane(tpb, imageTrump, trumpQ, passQ), createWaitingPane(tpb));
		
		BorderPane bp = new BorderPane();
		bp.setCenter(central);
		bp.setTop(createScorePane(sb, teamName));
		bp.setBottom(createHandPane(hb, smallImageCard, queue));

		StackPane sp = new StackPane(bp, createVictoryPanes(TeamId.TEAM_1, sb, teamName),
				createVictoryPanes(TeamId.TEAM_2, sb, teamName));

		this.scene = new Scene(sp);
	}

	/**
	 * Create a new stage for the scene
	 * 
	 * @return (Stage) : the stage for the scene
	 */
	public Stage createStage() {
		this.stage = new Stage();
		stage.setTitle("Javass - " + names.get(ownId));
		stage.setScene(this.scene);
		stage.sizeToScene();
		stage.setOnCloseRequest(g -> System.exit(0));
		return stage;
	}

	/**
	 * Create the score pane
	 * 
	 * @param sb       (ScoreBean) : the score property
	 * @param teamName (ObservableMap<TeamId, String>) : map containing the player
	 *                 names
	 * @return (GridPane) : the score pane
	 */
	private GridPane createScorePane(ScoreBean sb, ObservableMap<TeamId, String> teamName) {
		GridPane pane = new GridPane();
		pane.setStyle("-fx-font: 16 Optima; -fx-background-color: lightgray; -fx-padding: 5px; -fx-alignment: center;");

		for (TeamId team : TeamId.ALL) {
			Text nameTeam = new Text(teamName.get(team) + " : ");
			GridPane.setHalignment(nameTeam, HPos.RIGHT);

			Text turnPoints = new Text();
			turnPoints.textProperty().bind(Bindings.convert(sb.turnPointsProperty(team)));
			GridPane.setHalignment(turnPoints, HPos.RIGHT);

			IntegerProperty diff = new SimpleIntegerProperty();
			Text diffText = new Text();
			diffText.textProperty().bind(Bindings.format(" (+%s)", Bindings.convert(diff)));
			sb.turnPointsProperty(team).addListener((p, oldV, newV) -> {
				int d = newV.intValue() - oldV.intValue();
				diff.set(d < 0 ? 0 : d);
			});

			Text total = new Text(" / Total : ");

			Text totalPoints = new Text();
			totalPoints.textProperty().bind(Bindings.convert(sb.totalPointsProperty(team)));
			GridPane.setHalignment(totalPoints, HPos.RIGHT);

			pane.addRow(team.ordinal(), nameTeam, turnPoints, diffText, total, totalPoints);
		}
		return pane;
	}

	/**
	 * Create the "choose trump" pane
	 * 
	 * @param tb         (TrickBean) : the trick property
	 * @param imageTrump (ObservableMap<Color, Image>) : map containing the images
	 *                   representing the trump color
	 * @param trumpQ     (ArrayBlockingQueue<Color>) : queue for the choosen trump
	 * @param passQ      (ArrayBlockingQueue<Integer>) : queue for the right of
	 *                   passing or not
	 * @return the choosing trump pane
	 */
	private GridPane createTrumpPane(TrumpBean tpb, ObservableMap<Color, Image> imageTrump,
			ArrayBlockingQueue<Color> trumpQ, ArrayBlockingQueue<Boolean> passQ) {

		// bonus : the choosing trump pane

		GridPane pane = new GridPane();
		pane.setStyle("-fx-background-color: whitesmoke; -fx-padding: 5px; -fx-border-width: 3px 0px;"
				+ " -fx-border-style: solid; -fx-border-color: gray; -fx-alignment: center;");
		
		for (Color c : Color.ALL) {
			ImageView trumpView = new ImageView();
			trumpView.setImage(imageTrump.get(c));
			trumpView.disableProperty().bind(tpb.isChoosingProperty().not());
			trumpView.setFitWidth(CHOOSING_TRUMP);
			trumpView.setFitHeight(CHOOSING_TRUMP);
			
			trumpView.setOnMouseClicked(g -> {
				trumpQ.add(c);
				passQ.add(false);
			});
			
			setEffectOn(trumpView, javafx.scene.paint.Color.DARKGRAY);
			
			pane.add(trumpView, c.ordinal() > 1 ? 0 : 2, (c.ordinal() & 1) == 0 ? 0 : 2);
		}
		
		Button button = new Button("Je chibe !");
		button.setStyle(BUTTON_STYLE);
		setEffectOn(button, javafx.scene.paint.Color.DARKGRAY);
		
		button.setOnMouseClicked(g -> {
			passQ.add(true);
		});
		
		button.disableProperty().bind(tpb.canPassProperty().not());
		button.opacityProperty()
				.bind(Bindings.when(tpb.canPassProperty()).then(PLAYABLE_OPACITY).otherwise(NON_PLAYABLE_OPACITY));
		pane.add(button, 1, 1);

		pane.visibleProperty().bind(tpb.isChoosingProperty());

		return pane;
	}
	
	/**
     * Create a waiting pane
     * 
     * @param tpb (TrumpBean) 
     * @return the waiting pane
     */
    private GridPane createWaitingPane(TrumpBean tpb) {
        GridPane pane = new GridPane();
        pane.setStyle(
                "-fx-background-color: whitesmoke; -fx-padding: 5px; -fx-border-width: 3px 0px;"
                        + " -fx-border-style: solid; -fx-border-color: gray; -fx-alignment: center;");
        pane.visibleProperty().bind(tpb.isWaitingProperty());
        pane.setVgap(V_GAP_WAITING_PANE);
        
        Text text = new Text("Currently waiting for the trump to be choosen");
        text.setStyle("-fx-font: 14 Optima;");
        
        BooleanProperty bp = new SimpleBooleanProperty();
        
        ImageView anim = new ImageView();
        anim.imageProperty().bind(Bindings.when(bp).then(new Image("/timer_2.png")).otherwise(new Image("/timer_1.png")));
        anim.setOnMouseEntered(t -> bp.set(true));
        anim.setOnMouseExited(t -> bp.set(false));
        anim.setFitWidth(ANIMATION_WIDTH);
        anim.setFitHeight(ANIMATION_HEIGHT);
        
        pane.addColumn(0, text, anim);
        
        GridPane.setHalignment(anim, HPos.CENTER);

        return pane;
    }

	/**
	 * Create the trick pane, cards and trump representations
	 * 
	 * @param tb         (TrickBean) : the trick property
	 * @param imageCard  (ObservableMap<Card, Image>) : map containing the images
	 *                   representing the cards
	 * @param imageTrump (ObservableMap<Color, Image>) : map containing the images
	 *                   representing the trump color
	 * @return (GridPane) : the trick pane
	 */
	private GridPane createTrickPane(TrickBean tb, HandBean hb, ObservableMap<Card, Image> imageCard,
			ObservableMap<Color, Image> imageTrump) {
		GridPane pane = new GridPane();
		pane.setStyle("-fx-background-color: whitesmoke; -fx-padding: 5px; -fx-border-width: 3px 0px;"
				+ " -fx-border-style: solid; -fx-border-color: gray; -fx-alignment: center;");

		for (PlayerId player : PlayerId.ALL) {
			Text name = new Text(names.get(player));
			name.setStyle("-fx-font: 14 Optima;");

			ImageView cardView = new ImageView();
			cardView.imageProperty().bind(Bindings.valueAt(imageCard, Bindings.valueAt(tb.trickProperty(), player)));
			cardView.setFitWidth(LARGE_CARD_IMAGE_WIDTH);
			cardView.setFitHeight(LARGE_CARD_IMAGE_HEIGHT);

			Rectangle rec = new Rectangle(LARGE_CARD_IMAGE_WIDTH, LARGE_CARD_IMAGE_HEIGHT);
			rec.setStyle("-fx-arc-width: 20; -fx-arc-height: 20; -fx-fill: transparent;"
					+ " -fx-stroke: lightpink; -fx-stroke-width: 5; -fx-opacity: 0.5;");
			rec.setEffect(new GaussianBlur(GAUSSIAN_EFFECT));
			rec.visibleProperty().bind(tb.winningPlayerProperty().isEqualTo(player));

			StackPane sp = new StackPane(cardView, rec);

			boolean isOwnPlayer = player == ownId;

			VBox vbox = new VBox();
			vbox.setStyle("-fx-padding: 5px; -fx-alignment: center;");
			vbox.setAlignment(Pos.CENTER);

			vbox.getChildren().add(isOwnPlayer ? sp : name);
			vbox.getChildren().add(isOwnPlayer ? name : sp);

			if (player.team() == ownId.team()) {
				// The other player of the team at the top and the ownPlayer at
				// the bottom of the middle column
				pane.add(vbox, 1, isOwnPlayer ? 2 : 0);
			} else {
				// The player after the ownPlayer takes the right column (2) and the
				// other one the left column (0)
				boolean isRightPlayer = player.ordinal() == (ownId.ordinal() + 1) % PlayerId.COUNT;
				pane.add(vbox, isRightPlayer ? 2 : 0, 0, 1, 3);
			}
		}

		ImageView trumpView = new ImageView();
		trumpView.imageProperty().bind(Bindings.valueAt(imageTrump, tb.trumpProperty()));
		trumpView.setFitWidth(TRUMP_IMAGE_WIDTH);
		trumpView.setFitHeight(TRUMP_IMAGE_HEIGHT);
		pane.add(trumpView, 1, 1); // add trump image at the center
		GridPane.setHalignment(trumpView, HPos.CENTER);
		GridPane.setValignment(trumpView, VPos.CENTER);

		// bonus : help button

		Button button = new Button("I need help !");
		setEffectOn(button, javafx.scene.paint.Color.DARKGRAY);
		button.setStyle(BUTTON_STYLE);

		button.setOnAction(g -> hb.setHelp(true));
		button.disableProperty().bind(Bindings.isEmpty(hb.playableCards()));

		pane.add(button, 1, 3);
		GridPane.setHalignment(button, HPos.CENTER);
		GridPane.setValignment(button, VPos.CENTER);

		return pane;
	}

	/**
	 * Create the hand pane
	 * 
	 * @param hb        (HandBean) : the hand property
	 * @param imageCard (ObservableMap<Card, Image>) : map containing the images
	 *                  representing the cards
	 * @return (HBox) : the hand pane
	 */
	private HBox createHandPane(HandBean hb, ObservableMap<Card, Image> imageCard, ArrayBlockingQueue<Card> queue) {
		HBox hbox = new HBox();
		hbox.setStyle("-fx-background-color: lightgray; -fx-spacing: 5px; -fx-padding: 5px;");
		hbox.setAlignment(Pos.CENTER);

		for (int i = 0; i < Jass.HAND_SIZE; i++) {
			final int index = i;
			ImageView cardView = new ImageView();
			cardView.imageProperty().bind(Bindings.valueAt(imageCard, Bindings.valueAt(hb.hand(), i)));
			cardView.setFitHeight(SMALL_CARD_IMAGE_HEIGHT);
			cardView.setFitWidth(SMALL_CARD_IMAGE_WIDTH);

			BooleanProperty isPlayable = new SimpleBooleanProperty();
			isPlayable.bind(Bindings.createBooleanBinding(() -> {
				return hb.playableCards().contains(hb.hand().get(index));
			}, hb.hand(), hb.playableCards()));

			cardView.opacityProperty()
					.bind(Bindings.when(isPlayable).then(PLAYABLE_OPACITY).otherwise(NON_PLAYABLE_OPACITY));
			cardView.disableProperty().bind(Bindings.not(isPlayable));

			cardView.setOnMouseClicked(g -> {
				queue.add(hb.hand().get(index));
			});

			// bonus : properties for help button

			BooleanProperty isBestCard = new SimpleBooleanProperty();
			isBestCard.bind(Bindings.createBooleanBinding(() -> {
				return hb.bestCard().contains(hb.hand().get(index));
			}, hb.hand(), hb.playableCards(), hb.bestCard()));

			BooleanBinding help = isBestCard.and(hb.help());

			Rectangle rec = new Rectangle(SMALL_CARD_IMAGE_WIDTH, SMALL_CARD_IMAGE_HEIGHT);
			rec.setStyle("-fx-arc-width: 20; -fx-arc-height: 20; -fx-fill: transparent;"
					+ " -fx-stroke: blueviolet; -fx-stroke-width: 5; -fx-opacity: 0.5;");
			rec.setEffect(new GaussianBlur(4));
			rec.visibleProperty().bind(help);

			// bonus : effects on mouse drag
			
			setEffectOn(cardView, javafx.scene.paint.Color.BLACK);

			hbox.getChildren().add(new StackPane(rec, cardView));
		}

		return hbox;
	}

	/**
	 * Create the two victory panes
	 * 
	 * @param team     (TeamId) : the "winning" team
	 * @param sb       (ScoreBean) : the score property
	 * @param teamName (ObservableMap<TeamId, String>) : map containing the player
	 *                 names
	 * @return (BorderPane) : the two victory panes
	 */
	private BorderPane createVictoryPanes(TeamId team, ScoreBean sb, ObservableMap<TeamId, String> teamName) {
		BorderPane pane = new BorderPane();
		pane.setStyle("-fx-font: 16 Optima; -fx-background-color: white;");
		Text text = new Text();
		pane.setCenter(text);
		text.textProperty().bind(Bindings.format("%s won with %d points against %d.", teamName.get(team),
				sb.totalPointsProperty(team), sb.totalPointsProperty(team.other())));
		pane.visibleProperty().bind(sb.winningTeamProperty().isEqualTo(team));
		
		Button restart = new Button("Main menu");
		setEffectOn(restart, javafx.scene.paint.Color.DARKGREY);
		restart.setStyle(BUTTON_STYLE);
		
		restart.setOnAction(g -> {
			this.stage.close();
			Interface menu = new Interface();
			Stage newStage = menu.createStage();
			newStage.show();
		});
		
		pane.setBottom(restart);
		BorderPane.setAlignment(restart, Pos.CENTER);
		
		return pane;
	}

	/**
	 * Get graphic representation of a card from the bank image
	 * 
	 * @param c    (Card) : the card we want to get
	 * @param size (boolean) : true for small size (160x240 px), false for large
	 *             size (240x360 px)
	 * @return (Image) : graphical representation of the given card
	 */
	private Image getCardImage(Card c, boolean size) {
		String s = String.format("/card_%d_%d_%d.png", c.color().ordinal(), c.rank().ordinal(),
				size ? SMALL_SIZE : LARGE_SIZE);
		return new Image(s);
	}

	/**
	 * Create the map relating the image representing the trump to his color
	 * 
	 * @return (ObservableMap<Color, Image>) : the map of trump and their
	 *         corresponding image
	 */
	private ObservableMap<Color, Image> constructImageTrump() {
		ObservableMap<Color, Image> map = FXCollections.observableHashMap();
		Color.ALL.forEach(c -> map.put(c, new Image(String.format("/trump_%d.png", c.ordinal()))));
		return map;
	}

	/**
	 * Create the map relating the image representing the cards to their cards (rank
	 * & color)
	 * 
	 * @param size (boolean) : true for small size (160x240 px), false for large
	 *             size (240x360 px)
	 * @return (ObservableMap<Card, Image>) : the map of the cards and their
	 *         corresponding images
	 */
	private ObservableMap<Card, Image> constructImageCard(boolean size) {
		ObservableMap<Card, Image> map = FXCollections.observableHashMap();
		for (Color c : Color.ALL) {
			Rank.ALL.forEach(r -> {
				Card card = Card.of(c, r);
				map.put(card, getCardImage(card, size));
			});
		}
		return map;
	}

	/**
	 * Create the map relating the teams to the names of their player
	 * 
	 * @return (ObservableMap<TeamId, String>) : the map of the teams and the player
	 *         names
	 */
	private ObservableMap<TeamId, String> constructTeamName() {
		ObservableMap<TeamId, String> map = FXCollections.observableHashMap();
		StringBuilder name1 = new StringBuilder();
		StringBuilder name2 = new StringBuilder();

		for (int i = 0; i < PlayerId.COUNT; ++i) {
			PlayerId player = PlayerId.ALL.get((ownId.ordinal() + i) % PlayerId.COUNT);

			StringBuilder sb = player.team() == TeamId.TEAM_1 ? name1 : name2;
			if (sb.length() != 0)
				sb.append(" and ");
			sb.append(names.get(player));
		}

		map.put(TeamId.TEAM_1, name1.toString());
		map.put(TeamId.TEAM_2, name2.toString());
		return map;
	}
	
	/**
	 * Set a drop shadow effect on the given node
	 * @param n (Node)
	 */
	private void setEffectOn(Node n, javafx.scene.paint.Color c) {
		// bonus : to set an effect on a node
		
		n.setOnMouseEntered(g -> {
			DropShadow ds = new DropShadow();
			ds.setOffsetY(3.0);
			ds.setOffsetX(3.0);
			ds.setColor(c);
			n.setEffect(ds);
		});

		n.setOnMouseExited(g -> {
			n.setEffect(null);
		});
	}
}
