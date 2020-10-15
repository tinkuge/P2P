import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.text.SimpleDateFormat;

public class Handler implements Runnable{
	Helper h;
	Socket sock;
	Thread t;
	String handshakeHeader = "P2PFILESHARINGPROJ";
	byte[] zero, bytePeerid, headerStringBytes, headerBytes, rcvdHeader, inpidbytes;
	ByteBuffer bb;
	DataOutputStream writeout, sendMsg;
	DataInputStream readin, rcvMsg;
	int spid;

	Handler(Socket s, Helper he){

		h = he;
		spid = h.sourcepid;
		sock = s;
		t = new Thread(this);
		headerStringBytes = new byte[18];
		headerStringBytes = handshakeHeader.getBytes();
		zero = new byte[10];
		bb = ByteBuffer.allocate(4);
		bb.putInt(h.getSourcePeerID());
		bytePeerid = bb.array();
		headerBytes = new byte[headerStringBytes.length+zero.length+bytePeerid.length];
		System.arraycopy(headerStringBytes, 0, headerBytes, 0, headerStringBytes.length);
		System.arraycopy(zero, 0, headerBytes, headerStringBytes.length, zero.length);
		rcvdHeader = new byte[headerBytes.length];

		System.arraycopy(bytePeerid, 0, headerBytes, headerStringBytes.length+zero.length, bytePeerid.length);

		inpidbytes = new byte[4];

		t.start();
	}


	public void run(){
		try {
			writeout = new DataOutputStream(sock.getOutputStream());
			readin = new DataInputStream(sock.getInputStream());
			writeout.writeInt(headerBytes.length);
			writeout.write(headerBytes);
			int incLen = readin.readInt();
			readin.read(rcvdHeader,0,incLen);
			//System.out.println("Connected");

			//should you get it from socket HM or get it from the bytes in rcvdHeader
			byte[] commonHead = new byte[18];
			System.arraycopy(rcvdHeader,0, commonHead,0,18);
			System.arraycopy(rcvdHeader, 28, inpidbytes, 0, 4);
			String first = new String(commonHead);
			ByteBuffer bbuf = ByteBuffer.wrap(inpidbytes);

			//should you get it from socket HM or get it from the bytes in rcvdHeader
			System.arraycopy(inpidbytes, 0, rcvdHeader, 28, 4);

			//get peerid in int form from byte array
			int inpid = bbuf.getInt();
			h.peeridHM.put(sock, inpid);
			//int connPeer = peeridHM.get(sock);
			/*
			//check if both are same
			if(inpid == connPeer)
				System.out.println("True");

			else {
				System.out.println("False");
			}*/

			if(handshakeHeader.intern()!= first.intern()){
				sock.close();
			}

			else{
				//send bitfield
				PeerLogging TCP = new PeerLogging();
				SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:ms");
				String timeString  = dateFormat.format(new Date());
				TCP.logTCP(spid, inpid, timeString);
				//send bitfield by packing it appropriately
				sendMsg = new DataOutputStream(sock.getOutputStream());
				rcvMsg = new DataInputStream(sock.getInputStream());
				int type = 5;
				byte btype = (byte) type;
				byte[] mesType = new byte[1];
				mesType[0] = btype;

				System.out.println("Right before bitfield call");
				byte[] bitfBytes;//getBitfield().toByteArray();
				bitfBytes = h.getBitfield().toByteArray();
				//does message length include the message type length though?
				byte[] mesLen = ByteBuffer.allocate(4).putInt(bitfBytes.length+mesType.length).array();
				byte[] bitMsg = new byte[mesLen.length+mesType.length+bitfBytes.length];
				System.arraycopy(mesLen, 0, bitMsg, 0, mesLen.length);
				System.arraycopy(mesType, 0, bitMsg, mesLen.length, mesType.length);
				System.arraycopy(bitfBytes, 0, bitMsg, mesLen.length+mesType.length, bitfBytes.length);
				try{
					sendMsg.writeInt(bitMsg.length);
					sendMsg.write(bitMsg);
				}
				catch(Exception e){
					System.out.println(e);
				}

				h.chokedpeers.add(inpid);
				h.chokeHM.put(inpid,true);
				while(true){
					int rcvlen = rcvMsg.readInt();
					byte[] inMsg = new byte[rcvlen];
					long startTime = System.nanoTime();
					rcvMsg.read(inMsg, 0 , rcvlen);
					long endTime = System.nanoTime();
					long totalTime = endTime - startTime;
					Byte msgcode = inMsg[4];
					int code = msgcode.intValue();

					//choke
					if(code == 0) {
						h.chokeHM.put(inpid, true);
					}

					//unchoke
					else if(code == 1) {

						//set the choked status to false, which means it's unchoked
						for(int i = 0; i < h.chokedpeers.size();i++){
							if(h.chokedpeers.get(i) == inpid)
								h.chokedpeers.remove(i);
						}
						h.chokeHM.put(inpid, false);
						BitSet bs = (BitSet) h.peerBitsetHM.get(inpid).clone();
						//check which pieces the other peer has so that you can request those peers
						bs.andNot(h.peerBitsetHM.get(spid));

						//get the first index position of bitfield that is 1
						int req = bs.nextSetBit(0);

						//if a piece exists that this peer does not have, request it
						if(req != -1) {
							byte[] index = ByteBuffer.allocate(4).putInt(req).array();
							byte[] mtype = new byte[1];
							byte typ = (byte) 6;
							mtype[0] = typ;
							byte[] mlen = ByteBuffer.allocate(4).putInt(mtype.length+index.length).array();
							byte[] reqmsg = new byte[index.length+mtype.length+mlen.length];
							System.arraycopy(mlen, 0, reqmsg, 0, mlen.length);
							System.arraycopy(mtype, 0, reqmsg, mlen.length, mtype.length);
							System.arraycopy(index, 0, reqmsg, mlen.length+mtype.length, index.length);
							try{
								writeout.writeInt(reqmsg.length);
								writeout.write(reqmsg);
							}
							catch(Exception e){
								System.out.println(e);
							}
						}

						//should we implement else case

					}

					//interested
					else if(code == 2) {
						h.interestedHM.put(inpid, sock);
					}

					//notinterested
					else if(code == 3) {
						h.interestedHM.remove(inpid);
					}

					//have
					else if(code == 4) {
						//get the index field into a byte array
						byte[] index = new byte[4];
						System.arraycopy(inMsg, 5, index, 0, 4);
						ByteBuffer indexbyte = ByteBuffer.wrap(index);
						int bitfindex = indexbyte.getInt();
						BitSet newbs = h.peerBitsetHM.get(inpid);
						BitSet fakebs = (BitSet) newbs.clone();
						newbs.set(bitfindex);
						h.peerBitsetHM.put(inpid, newbs);
						fakebs.andNot(h.peerBitsetHM.get(spid));
						int nextindex = fakebs.nextSetBit(0);

						//if a piece exists with the other peer, send interested
						if(nextindex != -1) {
							byte[] mlen = ByteBuffer.allocate(4).putInt(1).array();	//just 1 as size because no payload
							byte[] mtype = new byte[1];
							byte typ = (byte) 2;		//type 2
							mtype[0] = typ;
							byte[] intermsg = new byte[5];	//4 bytes + 1 byte
							System.arraycopy(mlen, 0, intermsg, 0, mlen.length);
							System.arraycopy(mtype, 0, intermsg, mlen.length, mtype.length);

							try{
								writeout.writeInt(intermsg.length);
								writeout.write(intermsg);
							}
							catch(Exception e){
								System.out.println(e);
							}
						}

						else {//not interested
							byte[] mlen = ByteBuffer.allocate(4).putInt(1).array();	//just 1 as size because no payload
							byte[] mtype = new byte[1];
							byte typ = (byte) 3;		//type 3
							mtype[0] = typ;
							byte[] intermsg = new byte[5];	//4 bytes + 1 byte
							System.arraycopy(mlen, 0, intermsg, 0, mlen.length);
							System.arraycopy(mtype, 0, intermsg, mlen.length, mtype.length);

							try{
								writeout.writeInt(intermsg.length);
								writeout.write(intermsg);
							}
							catch(Exception e){
								System.out.println(e);
							}
						}

					}

					//received bitfield
					else if(code == 5) {
						byte[] bitfbyte = new byte[h.getByteBitfieldLen()];
						System.arraycopy(inMsg, 5, bitfbyte, 0, bitfbyte.length);
						BitSet inBitset;
						inBitset = BitSet.valueOf(bitfBytes);
						BitSet fakebs = (BitSet) inBitset.clone();
						System.out.println(inBitset.get(0));
						h.peerBitsetHM.put(inpid, inBitset);
						fakebs.andNot(h.peerBitsetHM.get(spid));
						int reqindex = fakebs.nextSetBit(0);

						if(reqindex != -1) {//interested
							byte[] mlen = ByteBuffer.allocate(4).putInt(1).array();	//just 1 as size because no payload
							byte[] mtype = new byte[1];
							byte typ = (byte) 2;		//type 2
							mtype[0] = typ;
							byte[] intermsg = new byte[5];	//4 bytes + 1 byte
							System.arraycopy(mlen, 0, intermsg, 0, mlen.length);
							System.arraycopy(mtype, 0, intermsg, mlen.length, mtype.length);

							try{
								writeout.writeInt(intermsg.length);
								writeout.write(intermsg);
							}
							catch(Exception e){
								System.out.println(e);
							}
						}

						else {//not interested
							byte[] mlen = ByteBuffer.allocate(4).putInt(1).array();	//just 1 as size because no payload
							byte[] mtype = new byte[1];
							byte typ = (byte) 3;		//type 3
							mtype[0] = typ;
							byte[] intermsg = new byte[5];	//4 bytes + 1 byte
							System.arraycopy(mlen, 0, intermsg, 0, mlen.length);
							System.arraycopy(mtype, 0, intermsg, mlen.length, mtype.length);

							try{
								writeout.writeInt(intermsg.length);
								writeout.write(intermsg);
							}
							catch(Exception e){
								System.out.println(e);
							}
						}
					}

					else if(code == 6) {	//request
						File f;
						byte[] filebytes = new byte[(int) h.fileSize];
						byte[] reqindex = new byte[4];
						System.arraycopy(inMsg, 5, reqindex, 0, reqindex.length);
						ByteBuffer indx = ByteBuffer.wrap(reqindex);
						int pindex = indx.getInt();	//convert the byte array to integer to get index value
						if(!h.chokeHM.get(inpid)) {	//if the peer is not choked
							if(h.getBitfield().get(pindex)) {	//check if the corresponding bitfield index is set
								String fname = String.valueOf(pindex);

								try {
									f = new File(fname);
									filebytes = Files.readAllBytes(f.toPath());
								}

								catch(FileNotFoundException fnfe){
									System.out.println("File not found in folder");
								}
								byte mtype = (byte) 7;
								byte[] typ = new byte[1];
								typ[0] = mtype;
								byte[] mlen = ByteBuffer.allocate(4).putInt(1+filebytes.length).array();
								byte[] fmsg = new byte[mlen.length+typ.length+filebytes.length];
								System.arraycopy(mlen,0, fmsg,0,mlen.length);
								System.arraycopy(typ,0,fmsg, mlen.length,typ.length);
								System.arraycopy(filebytes,0, fmsg,mlen.length+typ.length,filebytes.length);
								try{
									writeout.writeInt(fmsg.length);
									writeout.write(fmsg);
								}
								catch(Exception e){
									System.out.println(e);
								}

							}
						}
					}

					else if(code == 7) {
						byte[] pieceindex = new byte[4];
						System.arraycopy(inMsg,5,pieceindex,0,pieceindex.length);
						byte[] piece = new byte[(int) h.fileSize];
						System.arraycopy(inMsg,9, piece,0,piece.length);

						float drate = piece.length/totalTime;
						if(h.drateHM.containsKey(inpid)){
							h.drateHM.replace(inpid, drate);
						}
						else
							h.drateHM.put(inpid,drate);



						//writing the file to disk
						ByteBuffer bf = ByteBuffer.wrap(pieceindex);
						int intpindex = bf.getInt();
						String pname = String.valueOf(intpindex);
						try{
							FileOutputStream fop = new FileOutputStream(pname);
							fop.write(piece);
						}
						catch (FileNotFoundException f) {
							System.out.println("File not written");
						}

						//update bitfield and send have message
						//bitf.set(intpindex);
						h.peerBitsetHM.get(inpid).set(intpindex);
						//send have msg
						byte[] mlen = new byte[4];
						byte mtype = (byte) 4;
						byte[] typ = new byte[1];
						byte[] havmsg = new byte[mlen.length+typ.length+pieceindex.length];
						typ[0] = mtype;
						System.arraycopy(mlen,0, havmsg, 0, mlen.length);
						System.arraycopy(typ, 0, havmsg, mlen.length, typ.length);
						System.arraycopy(pieceindex,0, havmsg, mlen.length+typ.length,pieceindex.length);

						for(int i: h.peerlist) {

							Socket havsock = h.peerSockHashM.get(i);
							try {
								DataOutputStream writhav = new DataOutputStream(havsock.getOutputStream());
								writhav.writeInt(havmsg.length);
								writhav.write(havmsg);
							} catch (Exception e) {
								System.out.println(e);
							}

						}

					}
				}

			}
		} catch (IOException e) {
			// FIXME Auto-generated catch block
			e.printStackTrace();
		}
	}

}



/* To do

Fix log file name according to the protocol specifics i.e. ‘~/project/log_peer_1001.log’. Maybe add a time stamp on the title to distinguish it from different runs, so it doesn't get replaced'
Delete first line of log file
Handle message types (choke, unchoke, etc..)
Read each piece, convert it into bytearray and send it
Check input methods for different file types to convert to bytearray
Check handshake between 2 peers

Check the conversion from string to bytes and back same for peerid


NEW: Synchronize access to hashmaps: https://beginnersbook.com/2013/12/how-to-synchronize-hashmap-in-java-with-example/

*/

