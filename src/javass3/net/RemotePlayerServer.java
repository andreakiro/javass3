package javass3.net;

import static javass3.net.StringSerializer.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javass3.jass.components.Card;
import javass3.jass.components.Card.Color;
import javass3.jass.components.CardSet;
import javass3.jass.components.Score;
import javass3.jass.components.Trick;
import javass3.jass.components.TurnState;
import javass3.jass.ids.PlayerId;
import javass3.jass.ids.TeamId;
import javass3.jass.players.Player;

public final class RemotePlayerServer {
	
	private final Player local;

	private static final int FIRST_ARG = 0;
	private static final int SECOND_ARG = 1;
	private static final int THIRD_ARG = 2;

	/**
	 * Constructor for remote player server
	 * 
	 * @param local (Player) : the underlined player of the server
	 */
	public RemotePlayerServer(Player local) {
		this.local = local;
	}

	/**
	 * Run the player's server. Constantly reads and execute line received by the
	 * client, and writes back his answers to the client.
	 */
	public void run() {
		try (ServerSocket s0 = new ServerSocket(RemotePlayerClient.PORT_NUMBER);
				Socket s = s0.accept();
				BufferedReader r = new BufferedReader(
						new InputStreamReader(s.getInputStream(), StandardCharsets.US_ASCII));
				BufferedWriter w = new BufferedWriter(
						new OutputStreamWriter(s.getOutputStream(), StandardCharsets.US_ASCII))) {

			while (true) {
				String command = r.readLine();
				if (command == null)
					break;
				String exec = execute(command);
				if (exec != null) {
					w.write(exec);
					w.write('\n');
					w.flush();
				}
			}
			
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Execute the commands that the server receives and reads
	 * 
	 * @param line (String) : the received line to execute
	 * @return (String) : the response of the server to the client
	 */
	private String execute(String line) {
		String[] commands = split(" ", line);
		switch (JassCommand.valueOf(commands[FIRST_ARG])) {
		case PLRS:
			int ord = deserializeInt(commands[SECOND_ARG]);
			Map<PlayerId, String> map = computePlayerNamesMap(commands[THIRD_ARG]);
			local.setPlayers(PlayerId.ALL.get(ord), map);
			return null;
		case TRMP:
			local.setTrump(Color.ALL.get(deserializeInt(commands[SECOND_ARG])));
			return null;
		case HAND:
			local.updateHand(CardSet.ofPacked(deserializeLong(commands[SECOND_ARG])));
			return null;
		case TRCK:
			local.updateTrick(Trick.ofPacked(deserializeInt(commands[SECOND_ARG])));
			return null;
		case CARD:
			Card card = executeCardCommand(commands[SECOND_ARG], commands[THIRD_ARG]);
			return serializeInt(card.packed());
		case SCOR:
			local.updateScore(Score.ofPacked(deserializeLong(commands[SECOND_ARG])));
			return null;
		case WINR:
			local.setWinningTeam(TeamId.ALL.get(deserializeInt(commands[SECOND_ARG])));
			return null;
		case CHTR:
            Color color = local.trumpToChoose(CardSet.ofPacked(deserializeLong(commands[SECOND_ARG])), 
                    deserializeBoolean(commands[THIRD_ARG]));
            return color == null ? serializeInt(Color.COUNT) : serializeInt(color.ordinal());
		case PLSW:
            local.pleaseWait(deserializeBoolean(commands[SECOND_ARG]));
            return null;
		default:
			throw new Error("La commande n'est pas reconnue");
		}
	}

	/**
	 * Compute the player names map, assigning for each player identity a name
	 * 
	 * @param line (String) : the line containing the names
	 * @return (Map<PlayerId, String>) : the player names map
	 */
	private Map<PlayerId, String> computePlayerNamesMap(String line) {
		Map<PlayerId, String> playerNames = new HashMap<>();
		String[] names = split(",", line);
		PlayerId.ALL.forEach(p -> playerNames.put(p, deserializeString(names[p.ordinal()])));
		return playerNames;
	}

	/**
	 * Execute the "CARD" jass command and compute the card to play by the server
	 * 
	 * @param turnstate (String) : the string containing the turn state informations
	 * @param cardset   (String) : the string containing the cardset informations
	 * @return (Card) : the card to play by the server
	 */
	private Card executeCardCommand(String turnstate, String cardset) {
		String[] components = split(",", turnstate);
		TurnState ts = TurnState.ofPackedComponents(deserializeLong(components[FIRST_ARG]),
				deserializeLong(components[SECOND_ARG]), deserializeInt(components[THIRD_ARG]));
		return local.cardToPlay(ts, CardSet.ofPacked(deserializeLong(cardset)));
	}
}
