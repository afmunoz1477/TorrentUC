package TorrentClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.BasicConfigurator;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;

public class TorrentClient {
	
	public static final String IP = "192.168.0.5";
//	public static final String IP = "157.253.132.44";
	public static final String IPDEST = "192.168.0.3";
	public static final String PATH = "C:/Users/andre/Desktop/Andres/ProyectoUC/TorrentUC/torrent/";
	public static final String TRACKER_URL = "http://"+IPDEST+":6969/announce";
//	public static final String TRACKER_URL = "http://"+IPDEST+":6969/Users/ANDRES M/Desktop/UC/TrackerUC/TrackerUC/announce";
//	public static final String TRACKER_URL = "http://"+IPDEST+":6969/Users/af.munoz1477/Desktop/TrackerUC-master/TrackerUC/announce";
//	public static final String TRACKER_URL = "http://"+IPDEST+":6969/Users/af.munoz1477/Desktop/TrackerUC-master/TrackerUC/announce";
//	public static final String TRACKER_URL = "http://tracker.edoardocolombo.eu:6969/announce";
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void encodeObject(Object o, OutputStream out) throws IOException {
        if (o instanceof String)
            encodeString((String)o, out);
        else if (o instanceof Map)
            encodeMap((Map)o, out);
        else if (o instanceof byte[])
            encodeBytes((byte[])o, out);
        else if (o instanceof Number)
            encodeLong(((Number) o).longValue(), out);
        else
            throw new Error("Unencodable type");
    }
    private static void encodeLong(long value, OutputStream out) throws IOException {
        out.write('i');
        out.write(Long.toString(value).getBytes("US-ASCII"));
        out.write('e');
    }
    private static void encodeBytes(byte[] bytes, OutputStream out) throws IOException {
        out.write(Integer.toString(bytes.length).getBytes("US-ASCII"));
        out.write(':');
        out.write(bytes);
    }
    private static void encodeString(String str, OutputStream out) throws IOException {
        encodeBytes(str.getBytes("UTF-8"), out);
    }
    private static void encodeMap(Map<String,Object> map, OutputStream out) throws IOException{
        // Sort the map. A generic encoder should sort by key bytes
        SortedMap<String,Object> sortedMap = new TreeMap<String, Object>(map);
        out.write('d');
        for (java.util.Map.Entry<String, Object> e : sortedMap.entrySet()) {
            encodeString(((java.util.Map.Entry<String, Object>) e).getKey(), out);
            encodeObject(((java.util.Map.Entry<String, Object>) e).getValue(), out);
        }
        out.write('e');
    }
    private static byte[] hashPieces(File file, int pieceLength) throws IOException {
    	
        MessageDigest sha1;
        try {
            sha1 = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new Error("SHA1 not supported");
        }
        
        InputStream in = new FileInputStream(file);
        
        ByteArrayOutputStream pieces = new ByteArrayOutputStream();
        byte[] bytes = new byte[pieceLength];
        int pieceByteCount  = 0, readCount = in.read(bytes, 0, pieceLength);
        while (readCount != -1) {
            pieceByteCount += readCount;
            sha1.update(bytes, 0, readCount);
            if (pieceByteCount == pieceLength) {
                pieceByteCount = 0;
                pieces.write(sha1.digest());
            }
            readCount = in.read(bytes, 0, pieceLength-pieceByteCount);
        }
        in.close();
        if (pieceByteCount > 0)
            pieces.write(sha1.digest());
        return pieces.toByteArray();
    }
    public static void createTorrent(File file, File sharedFile, String announceURL) throws IOException {
        final int pieceLength = 512*1024;
        Map<String,Object> info = new HashMap<String,Object>();
        info.put("name", sharedFile.getName());
        info.put("length", sharedFile.length());
        info.put("piece length", pieceLength);
        info.put("pieces", hashPieces(sharedFile, pieceLength));
        Map<String,Object> metainfo = new HashMap<String,Object>();
        metainfo.put("announce", announceURL);
        metainfo.put("info", info);
        OutputStream out = new FileOutputStream(file);
        encodeMap(metainfo, out);
        out.close();
    }
    
            

	public static void main(String []args) throws UnknownHostException, IOException{
		String name = "Proyecto.jpg";
//		String name = "2.jpg";
		String file = PATH + "torrentFile"+"-"+name+".torrent";
		String sharedFile = PATH + name;
		File torrentUbication = new File (""+file);
		System.out.println("Parent Directory: " + torrentUbication.getParent());
		createTorrent(new File(""+file), new File(""+sharedFile), TRACKER_URL);
		
	
		
		
		
		try {
			
			InetAddress ip = InetAddress.getByName(IP);
			System.out.println("IP: "+ip);
//			byte[] bytes = ip.getAddress();
//			for (byte b : bytes) {
//			    System.out.println(b & 0xFF);
//			}
			BasicConfigurator.configure();
			System.out.printf("Inet Address: "+ip+
					"\n"+"File: " + file+"\n"
					+"Shared: "+sharedFile);
			
			Client client = new Client(ip, 
					SharedTorrent.fromFile(new File(""+file),new File(""+sharedFile).getParentFile()));
					// This is the interface the client will listen on (you might need something
					// else than localhost here).
					
					// Load the torrent from the torrent file and use the given
					// output directory. Partials downloads are automatically recovered.
					
							

		// You can optionally set download/upload rate limits
		// in kB/second. Setting a limit to 0.0 disables rate
		// limits.
		client.setMaxDownloadRate(1000.0);
		client.setMaxUploadRate(50.0);

		// At this point, can you either call download() to download the torrent and
//		 stop immediately after...
		System.out.println("Download");
//		Download();
		

		// Or call client.share(...) with a seed time in seconds:
		client.share(3600);
		// Which would seed the torrent for an hour after the download is complete.

		// Downloading and seeding is done in background threads.
		// To wait for this process to finish, call:
		client.waitForCompletion();
		// At any time you can call client.stop() to interrupt the download.
		} 
		catch (NoSuchAlgorithmException e) {
			System.out.println("NO se creo!");
			e.printStackTrace();
		}
	}


	//In this code segment, you can clearly see that you have to set the file name and location manually 
	//for download to take place. What you can do is, create a torrent file downloader (which would download the .torrent file) and set the file name and download location path  using 
	//the same programme and pass them to the ttorrent class which would carry out rest of the work.
	//Following code segments would allow you to download a desired torrent file (.torrent file)
	
	public static void Download() throws IOException {

		String fileName= "nuevo.torrent";
		
//		String url_1 = "http://"+IPDEST+":6969/Users/ANDRES M/Desktop/UC/TrackerUC/TrackerUC/announce/torrentFile-2.jpg.torrent";
		String url_1 = "http://"+IPDEST+":6969/announce/torrentFile-2.jpg.torrent";
//		String url_1 = "http://cdimage.debian.org/debian-cd/8.6.0/amd64/bt-dvd/debian-8.6.0-amd64-DVD-1.iso.torrent";
//		String url_1 = "http://kickasstorrents.to/x-men-apocalypse-2016-720p-hdrip-korsub-x264-aac2-0-stuttershit-t12968943.html";
		URL url = new URL(url_1);
		System.out.println(url_1);
		try (
				
				InputStream is = url.openStream()) {
				Files.copy(is, Paths.get(PATH+File.separator+fileName));
		}   
	}
}


