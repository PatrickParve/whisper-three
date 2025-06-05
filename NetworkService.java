package fi.utu.tech.telephonegame.network;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A concrete implementation of Network interface.
 * Should be able to listen for peers, connect to
 * peer network (by connecting to a peer in existing
 * peer network) as well as send and receive
 * messages from neighbouring peers
 * 
 * You probably need to create more methods and attributes
 * to this class as well as create additional classes
 * to be able to implement all the required functionality
 */
public class NetworkService extends Thread implements Network {
	private final List<PeerHandler> connectedPeers = Collections.synchronizedList(new ArrayList<>());
	private final LinkedBlockingQueue<Serializable> incoming = new LinkedBlockingQueue<>();
	private final LinkedBlockingQueue<Serializable> outgoing = new LinkedBlockingQueue<>();
	/*
	 * No need to change the construtor
	 */
	public NetworkService() {
		this.start();
	}

	/**
	 * Creates a server instance and starts listening for new peers on specified port
	 * 
	 * The port used for listening for incoming connections is provided automatically
	 * by the Resolver upon calling.
	 *  
	 * @param serverPort Which port should we start to listen to?
	 * 
	 */
	public void startListening(int serverPort) {
		System.out.printf("I should start listening for new peers at TCP port %d%n", serverPort);
		// TODO

		new Thread(() -> {
			try (ServerSocket serverSocket = new ServerSocket(serverPort)){
				while (true){
					Socket socket = serverSocket.accept();
					PeerHandler peerHandler = new PeerHandler(socket, incoming);
					peerHandler.start();
					connectedPeers.add(peerHandler);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}).start();
    }

	/**
	 * This method will be called when connecting to a peer (other whispers
	 * instance)
	 * The IP address and port will be provided by the template (by the resolver)
	 * upon calling.
	 * 
	 * @param peerIP   The IP address to connect to
	 * @param peerPort The TCP port to connect to
	 */
	public void connect(String peerIP, int peerPort) throws IOException, UnknownHostException {
		System.out.printf("I should connect myself to %s, TCP port %d%n", peerIP, peerPort);
		// TODO
		Socket socket = new Socket(peerIP, peerPort);
		PeerHandler peerHandler = new PeerHandler(socket, incoming);
		peerHandler.start();
		connectedPeers.add(peerHandler);
		System.out.printf("Connected to %s, TCP port %d%n", peerIP, peerPort);
	}

	/**
	 * This method is used to send the message to all connected neighbours (directly connected nodes)
	 * 
	 * @param out The serializable object to be sent to all the connected nodes
	 * 
	 */
	private void sendToNeighbours(Serializable out) {
		// Send the object to all neighbouring nodes
		// TODO
		for (PeerHandler peerHandler : connectedPeers){
            try {
                peerHandler.outputStream.writeObject(out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
	}
	
	/**
	 * Add an object to the queue for sending it to the peer network (all neighbours)
	 * 
	 * Note: This method is made for others to use. Ie. The implementation
	 * here is called by others (eg. MessageBroker) to post messages INTO
	 * network. To work out the structure of the internal implementation
	 * see run method and sendToNeighbours
	 * 
	 * @param out The Serializable object to be sent
	 */
	public void postMessage(Serializable out) {
		//TODO
        try {
            outgoing.put(out);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

	/**
	 * Read the next message from the queue of received messages.
	 * 
	 * Note: This method is made for others to use. Ie. The implementation
	 * here is called by others (eg. MessageBroker) to get access to messages
	 * received from the peer network.
	 * 
	 * @return The next message
	 */
	public Object retrieveMessage() throws InterruptedException {
		//TODO
		return incoming.take();
	}

	public void message(){
		for (PeerHandler peerHandler : connectedPeers){
            try {
				for (Serializable serializable : peerHandler.incoming){
					incoming.put(serializable);
				}
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
	}

	/**
	 * Waits for messages from the core application and forwards them to the network
	 * 
	 * Ie. When MessageBroker calls postMessage, the message-to-be-sent should be spooled
	 * into some kind of a producer-consumer-friendly data structure and picked up here for
	 * the actual delivery over sockets.
	 * 
	 * Thread running this method is started in the constructor of NetworkService.
	 * 
	 */
	public void run() {
		// TODO
		while (true) {
			try {
				// We do not have structure (yet) where messages being sent are spooled
				Serializable message = outgoing.take();
                sendToNeighbours(message);
            } catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}