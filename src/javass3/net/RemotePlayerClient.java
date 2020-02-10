package javass3.net;

import static javass3.net.StringSerializer.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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

public final class RemotePlayerClient implements Player, AutoCloseable {
	
	public static final int PORT_NUMBER = 5108;

	private final Socket socket;
	private final BufferedReader reader;
	private final BufferedWriter writer;

	/**
	 * Constructor for remote player client
	 * 
	 * @param host (String) : IP adress of the remote player ("localhost" for local)
	 * @throws IOException
	 */
	public RemotePlayerClient(String host) throws IOException {
		this.socket = new Socket(host, PORT_NUMBER);
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
		this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.epfl.javass.jass.Player#cardToPlay(ch.epfl.javass.jass.TurnState,
	 * ch.epfl.javass.jass.CardSet)
	 */
	@Override
	public Card cardToPlay(TurnState state, CardSet hand) {
		String ts = combine(",", serializeLong(state.packedScore()), serializeLong(state.packedUnplayedCards()),
				serializeInt(state.packedTrick()));
		String s = combine(" ", JassCommand.CARD.name(), ts, serializeLong(hand.packed()));
		try {
			writer.write(s);
			writer.write('\n');
			writer.flush();
			return Card.ofPacked(deserializeInt(reader.readLine()));
		} catch (IOException e) {
			System.err.println("Le server a quitté la partie...");
			throw new UncheckedIOException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.epfl.javass.jass.Player#setPlayers(ch.epfl.javass.jass.PlayerId,
	 * java.util.Map)
	 */
	@Override
	public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
		String[] names = new String[playerNames.values().size()];
		names = playerNames.values().toArray(names);
		for (int i = 0; i < names.length; i++)
			names[i] = serializeString(names[i]);
		String combined = combine(",", names);
		String s = combine(" ", JassCommand.PLRS.name(), serializeInt(ownId.ordinal()), combined);
		write(s);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.epfl.javass.jass.Player#updateHand(ch.epfl.javass.jass.CardSet)
	 */
	@Override
	public void updateHand(CardSet newHand) {
		String s = combine(" ", JassCommand.HAND.name(), serializeLong(newHand.packed()));
		write(s);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.epfl.javass.jass.Player#setTrump(ch.epfl.javass.jass.Card.Color)
	 */
	@Override
	public void setTrump(Color trump) {
		String s = combine(" ", JassCommand.TRMP.name(), serializeInt(trump.ordinal()));
		write(s);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.epfl.javass.jass.Player#updateTrick(ch.epfl.javass.jass.Trick)
	 */
	@Override
	public void updateTrick(Trick newTrick) {
		String s = combine(" ", JassCommand.TRCK.name(), serializeInt(newTrick.packed()));
		write(s);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.epfl.javass.jass.Player#updateScore(ch.epfl.javass.jass.Score)
	 */
	@Override
	public void updateScore(Score score) {
		String s = combine(" ", JassCommand.SCOR.name(), serializeLong(score.packed()));
		write(s);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.epfl.javass.jass.Player#setWinningTeam(ch.epfl.javass.jass.TeamId)
	 */
	@Override
	public void setWinningTeam(TeamId winningTeam) {
		String s = combine(" ", JassCommand.WINR.name(), serializeInt(winningTeam.ordinal()));
		write(s);
	}

    @Override
    public Color trumpToChoose(CardSet hand, boolean canPass) {
        String s = combine(" ", JassCommand.CHTR.name(), serializeLong(hand.packed()), serializeBoolean(canPass));
        try {
            writer.write(s);
            writer.write('\n');
            writer.flush();
            int i = deserializeInt(reader.readLine());
            return i == Color.COUNT ? null : Color.ALL.get(i);
        } catch (IOException e) {
            System.err.println("Le server a quitté la partie...");
            throw new UncheckedIOException(e);
        }
    }
	
	/*
     * (non-Javadoc)
     * @see ch.epfl.javass.jass.Player#pleaseWait(boolean)
     */
    @Override
    public void pleaseWait(boolean b) {
        String s = combine(" ", JassCommand.PLSW.name(), serializeBoolean(b));
        write(s);
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() throws Exception {
		socket.close();
		reader.close();
		writer.close();
	}

	/**
	 * Write the given string on the buffered writer and flush
	 * 
	 * @param s (String) : the string we want to write
	 */
	private void write(String s) {
		try {
			writer.write(s);
			writer.write('\n');
			writer.flush();
		} catch (IOException e) {
			System.err.println("Le server a quitté la partie...");
			throw new UncheckedIOException(e);
		}
	}
}
