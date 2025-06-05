package fi.utu.tech.telephonegame.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;


public class PeerHandler extends Thread{
	protected Socket socket;
	protected ObjectOutputStream outputStream;
	protected ObjectInputStream inputStream;
	protected LinkedBlockingQueue<Serializable> incoming;


	public PeerHandler(Socket socket, LinkedBlockingQueue<Serializable> incoming) {
		this.socket = socket;
		this.incoming = incoming;
	}


	@Override
	public void run() {
		try {
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());
			Object object;
			while ((object = inputStream.readObject()) instanceof Serializable){
                Serializable message = (Serializable) object;
                try {
                    incoming.put(message);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
    }
}
