import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class Helper extends peerProcess {
	volatile int sourcepid;
	Socket sock;
	Vector<Integer> peerlist, unchokedpeers, prefunchoke, chokedpeers;
	int optun;
	int numPrefNeigh;
	int unchokeInterval;
	int optUnchokeInterval;
	String fileName;
	double fileSize;
	double pieceSize;
	long numPieces;
	BitSet bitf;
	String[] stringArray;
	ArrayList<String[]> lines;
	int sourcePort;
	int haveFile;
	int numParam;
	int bitfByteLen;
	String sourceHostname;
	ConcurrentHashMap<Integer, Socket> peerSockHashM = new ConcurrentHashMap<>();
	ConcurrentHashMap<Socket, Integer> peeridHM = new ConcurrentHashMap<>();
	ConcurrentHashMap<Integer, Boolean> chokeHM = new ConcurrentHashMap<>();
	ConcurrentHashMap<Integer, BitSet> peerBitsetHM = new ConcurrentHashMap<>();
	ConcurrentHashMap<Integer, Socket> interestedHM = new ConcurrentHashMap<>();
	ConcurrentHashMap<Integer, Float> drateHM = new ConcurrentHashMap<>();
	// It turns the common.cfg file into an array and keeps only the values.
	//It stores the values into stringArray and then assigns these values to their corresponding variables.
	// It turns the common.cfg file into an array and keeps only the values.
	Helper()
	{
		peerlist = new Vector<>();
		chokedpeers = new Vector<>();
		try {
			//Reads Common.cfg
			File common = new File("Common.cfg");
			Scanner fileReader = new Scanner(common);
			StringBuffer stringBuffer = new StringBuffer();
			while (fileReader.hasNextLine()) {
				stringBuffer.append(fileReader.nextLine());
				stringBuffer.append(" ");
			}
			fileReader.close();
			//Split the line by virtue of spaces
			String[] commonParam = stringBuffer.toString().split(" ");
			Arrays.toString(commonParam);
			stringArray = new String[6];
			int i=0;
			for( int index = 1; index < commonParam.length; index += 2) {
				stringArray[i] = commonParam[index];
				i++;
			}
			
			//optimistic o = new optimistic();
			//prefUnchoke pu = new prefUnchoke();
		} 
		
		catch (IOException e) {
			e.printStackTrace();
		}
		//Assigns the parameters of the Common.cfg file to variables
		numPrefNeigh = Integer.parseInt(stringArray[0]);
		unchokeInterval = Integer.parseInt(stringArray[1]);
		optUnchokeInterval = Integer.parseInt(stringArray[2]);
		fileName = stringArray[3];
		fileSize = Double.parseDouble(stringArray[4]);
		pieceSize = Double.parseDouble(stringArray[5]);
		//Finds the number of pieces
		numPieces = (long) Math.ceil(fileSize/pieceSize);
		bitf = new BitSet((int) numPieces);
		bitfByteLen = bitf.toByteArray().length;
		System.out.println("Helper constructor end");
	}
	
	//Reads PeerInfo.cfg and stores it into an ArrayList.
	public void ReadPeerInfo(){
		
		lines = new ArrayList<String[]>();
		try {
			Scanner scanPeerInfo = new Scanner(new BufferedReader(new FileReader("PeerInfo.cfg")));
		    int numrows = 0;
		    while(scanPeerInfo.hasNextLine()){
		    	String[] line = scanPeerInfo.nextLine().trim().split(" ");
		    	lines.add(line);
		    	numParam= line.length;
		    	numrows++;
		    }
		    scanPeerInfo.close();
		    //System.out.println("Number of lines in ReadPPeerInfo(): " + lines.size());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	void connect() throws UnknownHostException, IOException{
		int i = 0;
		setSourcePeerID(peerProcess.getPeerID());
		while(i< lines.size()) {

			String[] param = lines.get(i);

			//if the peerid of the line is same as source peer id
			if (Integer.parseInt(param[0]) == getSourcePeerID()) {
				//set hostname
				setSourceHostname(param[1]);
				//set port
				setSourcePort(Integer.parseInt(param[2]));
				//set havefile
				setHaveFile(Integer.parseInt(param[3]));
				if (haveFile == 1) {
					//set all of the bitfield values to 1
					bitf.set(0, bitf.size());
				} else {
					//set all of the bitfield values to zeroes
					bitf.clear();
				}

				peerBitsetHM.put(getSourcePeerID(), getBitfield());

				//System.out.println("sourcepid " + getSourcePeerID());
				//System.out.println("getSourcePort " + getSourcePort());
			}
			i++;
		}
		int j = 0;
		while(j< lines.size()){
			//setSourcePeerID(peerProcess.getPeerID());
			String[] param = lines.get(j);

			//if the peerid of the line is same as source peer id
			if(Integer.parseInt(param[0]) == getSourcePeerID()){

				ServerSocket ssock = new ServerSocket(getSourcePort());
				while(true) {
					Socket sck = ssock.accept();
					System.out.println("Connection accepted");
					Handler ha = new Handler(sck, this);
				}
				//Stop making connections to peers.
			}
			
			else{

				int destpid = Integer.parseInt(param[0]);
				peerlist.add(destpid);
				String destHostname = param[1];
				int destport = Integer.parseInt(param[2]);
				int desthave = Integer.parseInt(param[3]);
				
				//Make connection with the other peer
				sock = new Socket(destHostname,destport);
				peerSockHashM.put(destpid, sock);
				peeridHM.put(sock, destpid);
				
				//Need to hand off to a new thread and send bitfield and handshake
				
				//System.out.println("Else statement");
				Handler ha = new Handler(sock, this);
				
			}
			
			j++;
		}
	}
	
	synchronized void setSourcePeerID(int p) {
		sourcepid = p;
	}
	
	synchronized int getSourcePeerID() {
		return sourcepid;
	}
	
	synchronized void setSourcePort(int port){
		sourcePort = port;
	}
	
	synchronized void setSourceHostname(String hostname){
		sourceHostname = hostname;
	}
	
	synchronized void setHaveFile(int i){
		haveFile = i;
	}
	
	synchronized int getSourcePort(){
		return sourcePort;
	}
	
	synchronized String getHostname(){
		return sourceHostname;
	}
	
	synchronized BitSet getBitfield(){
		return bitf;
	}
	
	synchronized int getByteBitfieldLen(){
		return bitfByteLen;
	}

	void readCommonCfg() throws FileNotFoundException{
		File common = new File("./src/Common.cfg");
		Scanner fileReader = new Scanner(common);
		StringBuffer stringBuffer = new StringBuffer();
		while (fileReader.hasNextLine()) {
			stringBuffer.append(fileReader.nextLine());
			stringBuffer.append(" ");
		}
		fileReader.close();
		
		//Split the line by virtue of spaces
		String[] commonParam = stringBuffer.toString().split(" ");
		Arrays.toString(commonParam);
		stringArray = new String[6];
		int i=0;
		for( int index = 1; index < commonParam.length; index += 2) {
			stringArray[i] = commonParam[index];
			i++;
		}
		
		numPrefNeigh = Integer.parseInt(stringArray[0]);
		unchokeInterval = Integer.parseInt(stringArray[1]);
		optUnchokeInterval = Integer.parseInt(stringArray[2]);
		fileName = stringArray[3];
		fileSize = Double.parseDouble(stringArray[4]);
		pieceSize = Double.parseDouble(stringArray[5]);
		numPieces = (long) Math.ceil(fileSize/pieceSize);
	}
	
	
}
