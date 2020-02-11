package javass3;

import static javass3.net.StringSerializer.split;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javass3.gui.GraphicalMctsPlayer;
import javass3.gui.GraphicalPlayerAdapter;
import javass3.gui.Interface;
import javass3.jass.JassGame;
import javass3.jass.ids.PlayerId;
import javass3.jass.players.MctsPlayer;
import javass3.jass.players.PacedPlayer;
import javass3.jass.players.Player;
import javass3.net.RemotePlayerClient;
import javass3.net.RemotePlayerServer;

public final class Main extends Application {
	
	private final static Map<PlayerId, Player> PLAYERS = new EnumMap<>(PlayerId.class);
	private final static Map<PlayerId, String> PLAYERS_NAMES = new EnumMap<>(PlayerId.class);

	private static double PACE = 0.8; // in seconds (usually 2)
	private static long SLEEP = 1000; // in milliseconds (usually 1000)

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Platform.setImplicitExit(false);
		Interface menu = new Interface();
		primaryStage = menu.createStage();
		primaryStage.show();
		primaryStage.setOnCloseRequest(g -> System.exit(0));
	}

	/**
	 * Start a local game
	 * 
	 * @param args (List<String>) : the parameters for the game
	 */
	public static void startLocalGame(List<String> args) {
		checkSizeList(args);
		
		Random random = createRandom(args);
		long seed = random.nextLong();

		for (PlayerId p : PlayerId.ALL) {
			String[] str = split(":", args.get(p.ordinal()));
			checkNumberComponent(str);
			setPlayerType(p, str, random.nextLong());
			setPlayerName(p, str);
		}

		Thread gameThread = new Thread(() -> {
			JassGame game = new JassGame(seed, PLAYERS, PLAYERS_NAMES);
			while (!game.isGameOver()) {
				game.advanceToEndOfNextTrick();
				try {
					Thread.sleep(SLEEP);
				} catch (InterruptedException e) {
					throw new Error("Thread sleep error");
				}
			}
			PACE = 0.8;
			SLEEP = 1000;
		});

		gameThread.setDaemon(true);
		gameThread.start();
	}

	/**
	 * Start a remote game
	 */
	public static void startRemoteGame() {
		RemotePlayerServer server = new RemotePlayerServer(new GraphicalPlayerAdapter());
		Thread serverThread = new Thread(() -> server.run());
		serverThread.setDaemon(true);
		serverThread.start();
	}
	
	// private settings methods

	private final static String[] DEFAULT_NAMES = { "Aline", "Bastien", "Colette", "David" };
	private final static String DEFAULT_IP = "localhost";
	private final static int DEFAULT_IT = 10_000;

	private final static int MIN_IT = 10;
	private final static int SEED_INDEX = 4;
	private final static int MAX_INDEX = 5;

	private final static int FIRST_ARG = 0;
	private final static int SECOND_ARG = 1;
	private final static int THIRD_ARG = 2;

	/**
	 * Create the random generator used for the simulated players and the game seed
	 * 
	 * @param args (List<String>) : the given parameters, if a seed is specified
	 * @return (Random) : the random generator
	 */
	private static Random createRandom(List<String> args) {
		Random random = new Random();
		if (args.size() > SEED_INDEX) {
			try {
				random = new Random(Long.parseLong(args.get(SEED_INDEX)));
			} catch (NumberFormatException e) {
				System.err.println("Erreur: entier non valide pour la graine : " + args.get(SEED_INDEX));
				System.exit(1);
			}
		}
		return random;
	}

	/**
	 * Set the names of the given player with respect to the specification
	 * 
	 * @param p   (PlayerId) : the given player
	 * @param str (String[]) : the array containing the specification
	 * @return (String) :
	 */
	private static void setPlayerName(PlayerId p, String[] str) {
		// the 1 stands for minimal size of an argument
		String name = str.length > 1 && !str[SECOND_ARG].equals("") ? str[SECOND_ARG] : DEFAULT_NAMES[p.ordinal()];
		PLAYERS_NAMES.put(p, name);
	}

	/**
	 * Set the type of the given player with respect to the specification
	 * 
	 * @param p    (PlayerId) : the given player
	 * @param str  (String[]) : the array containing the type specification
	 * @param seed (long) : the random seed for the simulated player
	 * @return (Player) : the type of the player
	 */
	private static void setPlayerType(PlayerId pId, String[] str, long seed) {
		Player type = null;
		String arg = str[FIRST_ARG];

		switch (arg) {
		case "h":
			type = new GraphicalPlayerAdapter();
			break;
		case "s":
			try {
				type = createSimulatedPlayer(pId, str, seed);
			} catch (NumberFormatException e) {
				System.err.println(
						"Erreur: entier non valide pour le nombre d'itérations pour joueur simulé : " + str[THIRD_ARG]);
				System.exit(1);
			}
			break;
		case "r":
			try {
				// the 3 stands for the position of the ip adress in the array
				type = new RemotePlayerClient(str.length == 3 ? str[THIRD_ARG] : DEFAULT_IP);
			} catch (IOException e) {
				System.err.println("Erreur: connexion avec le client non établie");
				System.exit(1);
			}
			break;
		case "z":
			type = new GraphicalMctsPlayer();
			PACE = 0;
			SLEEP = 0;
			break;
		default:
			System.err.println("Erreur: spécification du joueur invalide : " + str[FIRST_ARG]);
			System.exit(1);
		}

		PLAYERS.put(pId, type);
	}

	/**
	 * Create a simulated player given with respect to the given seed and the
	 * specified number of iterations
	 * 
	 * @param pId  (PlayerId) : the given player
	 * @param str  (String[]) : the array containing the specification about the
	 *             number of iterations
	 * @param seed (long) : the random seed
	 * @return (Player) : the simulated player
	 */
	private static Player createSimulatedPlayer(PlayerId pId, String[] str, long seed) {
		// the 2 stands for the minimal size arguments of a simulated player
		int it = str.length > 2 ? Integer.parseInt(str[THIRD_ARG]) : DEFAULT_IT;

		if (it < MIN_IT) {
			System.err.println(
					"Erreur: le nombre d'itérations pour le joueur simulé est inférieur à 10 : " + str[THIRD_ARG]);
			System.exit(1);
		}

		// return new PacedPlayer(new RandomPlayer(), PACE);
		return new PacedPlayer(new MctsPlayer(pId, seed, it), PACE);
	}

	/**
	 * Check the size of the game parameters, exit and print an error if they are
	 * not valid
	 * 
	 * @param args (List<String>) : the list of the game parameters
	 */
	private static void checkSizeList(List<String> args) {
		if (args.size() != SEED_INDEX && args.size() != MAX_INDEX) {
			System.err.println("Utilisation: java ch.epfl.javass.LocalMain <j1>…<j4> [<graine>]\n" + "où :\n"
					+ "<jn> spécifie le joueur n, ainsi:\n" + "  h:<nom>  un joueur humain nommé <nom>\n"
					+ "  s:<nom>:<int> un joueur simulé nommé <nom> avec <int> itérations du mcts\n"
					+ "  r:<nom>:<ip> un joueur distant nommé <nom> d'adresse ip <ip>\n"
					+ "  z:<nom> une simulation nommé <nom> \n"
					+ "[<graine>] est optionnel et spécifie la graine générant les générateurs aléatoires");
			System.exit(1);
		}
	}

	/**
	 * Check the number of components of each sub parameters list
	 * 
	 * @param str (String[]) : the array containing the components
	 */
	private static void checkNumberComponent(String[] str) {
		// the 2 stands for too many arguments for an human player
		if (str[FIRST_ARG].equals("h") && str.length > 2) {
			System.err.println("Erreur: trop de composantes spécifiée pour ce type de joueur : " + str[FIRST_ARG]);
			System.exit(1);
		}

		// the 3 stands for too many arguments for the other players
		if ((str[FIRST_ARG].equals("s") || str[FIRST_ARG].equals("r")) && str.length > 3) {
			System.err.println("Erreur: trop de composantes spécifiée pour ce type de joueur : " + str[FIRST_ARG]);
			System.exit(1);
		}
	}
}
