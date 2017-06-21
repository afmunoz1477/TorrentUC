package TorrentClient;

import java.awt.SecondaryLoop;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
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


	public static final String PATH = "./";
	InetAddress ip;
	String name = "Prueba2GB.rar";
	String namePart = "Prueba2GB.rar.part";
	String partPath = PATH+namePart;
	String torrentPath = PATH+name;
	String file = PATH +name+".torrent";
	String sharedFile = PATH + name;
	File torrentUbication = new File (""+file);
	Boolean state;
	Client client;
	Boolean downloading = false;
	String result = "";

	public TorrentClient() {
		BasicConfigurator.configure();
		state = false;
	}
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



	public void startTorrent(DataOutputStream server) throws UnknownHostException, IOException{

		//Establecer nombre del archivo a crear 
		System.out.println("Parent Directory: " + torrentUbication.getParent());
		//Crea el archivo torrent objetivo
		//		createTorrent(new File(""+file), new File(""+sharedFile), TRACKER_URL);

		try { 

			//Transforma la IP de la maquina de String a InetAddress
			ip = InetAddress.getLocalHost();

			System.out.printf("Inet Address: "+ip+"\n"+"File: " + file+"\n"+"Shared: "+sharedFile);

			//Metodo del cliente se encarga de obtener el archivo original completo de un Torrent
			client = new Client(ip, 
					SharedTorrent.fromFile(new File(""+file),new File(""+sharedFile).getParentFile()));

			// Establecer limites de subida y bajada de informacion en KB/seg
			client.setMaxDownloadRate(500000.0);
			client.setMaxUploadRate(500000.0);

			//Descarga un archivo de algun servidor

			// Metodo para compartir los torrent locales del cliente, tasa medida en segundos. 
			client.share(3600);
			this.state = true;
			// Metodo que se encarga de esperar a que el proceso de descarga finalice exitosamente 
			//client.waitForCompletion();
			double start = System.currentTimeMillis();
			Boolean fileTo = false;




			this.state = true;
			server.writeUTF("Agent downloading file: --> "+this.state);

			//client.stop() - Para detener el cliente.
		} 
		catch (NoSuchAlgorithmException e) {
			System.out.println("NO se creo!");
			e.printStackTrace();
		}
	}

	public Boolean shutdown() throws InterruptedException {
		// TODO Auto-generated method stub
		client.stop();
		this.state = false;
		return this.state;
	}

	public String deleteFile() {
		String resp = null;
		File fileTorrent = new File(torrentPath);
		File filePart = new File(partPath);
		if(fileTorrent.exists()){
			if(fileTorrent.delete()){
				resp = "File: "+fileTorrent.getAbsolutePath()+", was delete!!";
			}else{
				resp = "Error deleting file!";
			}
		}else if(filePart.exists()){
			if(filePart.delete()){
				resp = "File: "+filePart.getAbsolutePath()+", was delete!!";
			}else{
				resp = "File doesn't exist!!";
			}
		}else{
			resp = "Error deleting file!";
		}

		return resp;
	}
	public int getCompletion() {
		// TODO Auto-generated method stub
		if(this.downloading){
			return 1;
		}else{
			return 0;
		}
	}
	public String getResult() {
		// TODO Auto-generated method stub
		return this.result;
	}
}


