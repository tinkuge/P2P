import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class peerProcess {
	static int pid;
	public static void main(String[] args) throws UnknownHostException, IOException {
		pid = Integer.parseInt(args[0]);
		PeerLogging pl = new PeerLogging();		
		Helper H = new Helper();
		H.ReadPeerInfo();
		H.connect();
		
		//pl.chokePeer(1024, 1025, 150000);
		//pl.logTCP(1,2,70000);
		
		

	}
	
	static int getPeerID(){
		return pid;
	}
}
