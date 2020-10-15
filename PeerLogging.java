import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.*;


public class PeerLogging {

	private static final Logger LOGGER = Logger.getLogger(PeerLogging.class.getName());


	//Creates log file method
	PeerLogging() {
		SimpleFormatter simpleFormatter = new SimpleFormatter();
		FileHandler fileHandler = null;
		try {
			fileHandler  = new FileHandler("PeerLogger.log");
			LOGGER.addHandler(fileHandler);
			fileHandler.setFormatter(simpleFormatter);

		}

		catch(IOException io) {
			LOGGER.log(Level.OFF, "IO Exception");
		}
	}


	//Log event methods
	void logTCP(int p1, int p2, String time) {
		String s = time+": "+Integer.toString(p1)+ " makes a connection to "+Integer.toString(p2);
		LOGGER.log(Level.OFF, s);
	}

	void changePrefNeigh(int p1, ArrayList<Integer> pids, String time) {
		StringBuilder sb  = new StringBuilder();
		for(int a : pids) {
			sb.append(Integer.toString(a));
			sb.append(", ");
		}
		String s = time+": "+Integer.toString(p1)+ " has the preferred neighbors "+ sb;
		LOGGER.log(Level.OFF, s);

	}

	void changeOptNeigh(int p1, int p2, String time) {
		String s = time+": "+Integer.toString(p1)+ " has the optimistically unchoked neighbor "+Integer.toString(p2);
		LOGGER.log(Level.OFF, s);
	}

	void unchokePeer(int p1, int p2, String time) {
		String s = time+": "+Integer.toString(p2)+ " is unchoked by "+Integer.toString(p1);
		LOGGER.log(Level.OFF, s);
	}

	void chokePeer(int p1, int p2, String time) {
		String s = time+": "+Integer.toString(p2)+ " is choked by "+Integer.toString(p1);
		LOGGER.log(Level.OFF, s);
	}

	void haveMsg(int p1, int p2, String time, int pieceIndex) {
		String s = time+": "+Integer.toString(p2)+ " received the 'have' message from "+Integer.toString(p1)+ " for the piece "+Integer.toString(pieceIndex);
		LOGGER.log(Level.OFF, s);
	}

	void interestedMsg(int p1, int p2, String time) {
		String s = time+": "+Integer.toString(p1)+ " received the 'interested' message from "+Integer.toString(p2);
		LOGGER.log(Level.OFF, s);
	}

	void notInterestedMsg(int p1, int p2, String time) {
		String s = time+": "+Integer.toString(p1)+ " received the 'not interested' message from "+Integer.toString(p2);
		LOGGER.log(Level.OFF, s);
	}

	void dwnldPiece(int p1, int p2, String time, int pieceIndex) {
		String s = time+": "+Integer.toString(p1)+ " has downloaded the piece "+Integer.toString(pieceIndex)+ " from "+Integer.toString(p2);
		LOGGER.log(Level.OFF, s);
	}

	void notInterestedMsg(int p1, String time) {
		String s = time+": "+Integer.toString(p1)+ " has downloaded the complete file. ";
		LOGGER.log(Level.OFF, s);
	}
}