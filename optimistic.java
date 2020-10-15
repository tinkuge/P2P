import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class optimistic  extends Helper implements Runnable{
    Thread t;
    optimistic(){
        t = new Thread(this);
        t.start();
    }

    public void run() {
        //make it wait for one unchoke interval before it finds peers
        try {
            Thread.sleep(optUnchokeInterval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while(true) {
            System.out.println("Optimistic unchoking");
            //send choke message to currently unchoked peers
            if(peerlist.size()<3){
                try {
                    Thread.sleep(optUnchokeInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            else {
                if (peerlist.size() >= 3) {
                    //send choke message to already optimistically unchoked peer
                    Socket chosock = peerSockHashM.get(optun);
                    DataOutputStream chodos = null;
                    try {
                        chodos = new DataOutputStream(chosock.getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    byte[] meslen;
                    meslen = ByteBuffer.allocate(4).putInt(1).array();
                    byte mestype = (byte) 0;
                    byte[] typ = new byte[1];
                    typ[0] = mestype;
                    byte[] umsg = new byte[meslen.length + typ.length];
                    System.arraycopy(meslen, 0, umsg, 0, meslen.length);
                    System.arraycopy(typ, 0, umsg, meslen.length, typ.length);
                    try {
                        chodos.writeInt(umsg.length);
                        chodos.write(umsg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    CreateRandom cr = new CreateRandom();
                    int optPeer = cr.RandomList(chokedpeers);
                    Socket optisock = peerSockHashM.get(optPeer);
                    try {
                        DataOutputStream dos = new DataOutputStream(optisock.getOutputStream());

                        byte[] mlen;
                        mlen = ByteBuffer.allocate(4).putInt(1).array();
                        byte mtype = (byte) 1;
                        byte[] ty = new byte[1];
                        typ[0] = mestype;
                        byte[] unmsg = new byte[mlen.length + typ.length];
                        System.arraycopy(mlen, 0, unmsg, 0, mlen.length);
                        System.arraycopy(ty, 0, unmsg, mlen.length, ty.length);
                        dos.writeInt(unmsg.length);
                        dos.write(unmsg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(optUnchokeInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    } else {
                        try {
                        Thread.sleep(optUnchokeInterval);
                        } catch (InterruptedException e) {
                        e.printStackTrace();
                        }
                    }
                }

            }
        }


}
