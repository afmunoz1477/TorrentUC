package TorrentClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	public static final String START = "START";
	public static final String ERRASE = "ER";
	private static final Object SHUTDOWN = "SHUTDOWN";
	private static final Object COMPLETE = "COMPLETE";
	private static final Object RESULT = "RESULT";
		
	public static void main(String[] args) {
		
		ServerSocket server;
		Socket connect;
		TorrentClient torrent;
		int port = 10031;
		Boolean torrentRunning = false;
		try{
			torrent = new TorrentClient();
			server = new ServerSocket(port);
			System.out.println("Waiting for call!! Port: "+port);
			while (true) {
				connect = server.accept();
				InputStream fromServer = connect.getInputStream();
				DataInputStream read = new DataInputStream(fromServer);
				OutputStream toServer = connect.getOutputStream();
				DataOutputStream write = new DataOutputStream(toServer);
				String receive = read.readUTF();
				System.out.println("Listen: "+receive);
				if(receive.equals(START)){
					if (torrentRunning) {
						write.writeUTF("Torrent already running!");
					}else{
						try{
							torrent.startTorrent(write);
							torrentRunning = torrent.state;						
							System.out.println("Torrent state: "+torrentRunning);
						}catch (Exception e) {
							write.writeUTF(e.getMessage());
						}						
					}
				}else if (receive.equals(SHUTDOWN)) {
					if (torrentRunning) {
						torrentRunning = torrent.shutdown();
						write.writeUTF("Torrent client shutdown!");
					}else{
						write.writeUTF("Agent down, please start it!!");					
					}
				}else if(receive.equals(COMPLETE)){
					int complete = torrent.getCompletion();
					write.writeInt(complete);
				}
				else if(receive.equals(RESULT)){
					String complete = torrent.getResult();
					if(complete.equals("")){
						write.writeUTF("Waiting for download");
					}else{
						write.writeUTF(complete);
					}
				}
				else if (receive.equals(ERRASE)) {
					String delete = torrent.deleteFile();
					write.writeUTF(delete);
				}else if(receive.equals("q")){
					System.exit(0);
				}
				
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
}
