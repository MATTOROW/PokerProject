package ru.itis.pokerproject.clientserver.server;

import ru.itis.pokerproject.clientserver.server.listeners.ServerEventListener;
import ru.itis.pokerproject.clientserver.server.listeners.ServerEventListenerException;
import ru.itis.pokerproject.shared.protocol.clientserver.ClientServerMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NioServerExample implements ServerExample {
    protected List<ServerEventListener> listeners;
    protected int port;
    protected ServerSocket server;
    protected boolean started;
    // Better to incapsulate to List<Connection>
    protected List<SocketChannel> channels;

    public NioServerExample(int port){
        this.listeners = new ArrayList<>();
        this.port = port;
        this.channels = new ArrayList<>();
        this.started = false;
    }

    @Override
    public void registerListener(ServerEventListener listener) throws ServerException{
        if(started){
            throw new ServerException("Server has been started already.");
        }
        listener.init(this);
        this.listeners.add(listener);
    }

    @Override
    public void start() throws ServerException{
        try{
            // Start server
            Selector selector = Selector.open();
            ServerSocketChannel ssc = ServerSocketChannel.open();
            InetSocketAddress addr = new InetSocketAddress(this.port);
            ssc.bind(addr);
            ssc.configureBlocking(false);
//      int ops = ssc.validOps();
            int ops = SelectionKey.OP_ACCEPT;//Same for this context
            ssc.register(selector, ops);

            started = true;

            // Process connections
            while(selector.isOpen()){
                int num = selector.select();
                if (num == 0) continue;
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while(it.hasNext()) {
                    SelectionKey key = it.next();
                    if ((key.readyOps() & SelectionKey.OP_ACCEPT) != 0) { //isAcceptable()
                        SocketChannel channel = ssc.accept();
                        channels.add(channel);
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    }
                    else if ((key.readyOps() & SelectionKey.OP_READ) != 0) { //isReadable()
                        SocketChannel channel = (SocketChannel) key.channel();
                        handleConnection(channel);
                    }
                    it.remove();
                }
            }
        }
        catch(IOException ex){
            throw new ServerException("Problem with server starting.", ex);
        }
    }

    protected void handleConnection(SocketChannel channel) throws ServerException{
        try{
            int connectionId = channels.indexOf(channel);
            ByteBuffer buffer = ByteBuffer.allocate(ClientServerMessage.MAX_LENGTH+10);
            if( channel.read(buffer) != -1) {
                // Every connection will wait creating and handling of message
                ByteArrayInputStream in = new ByteArrayInputStream(buffer.array());
                ClientServerMessage message = ClientServerMessage.readMessage(in);
                System.out.println("New message:");
                System.out.println(ClientServerMessage.toString(message));
                for (ServerEventListener listener : listeners) {
                    if (message.getType() == listener.getType()) {
                        // One by one! Another left listeners will wait current
                        // Another thread could be created here or before for every Listener
                        listener.handle(connectionId, message);
                    }
                }
            }
        }
        catch(IOException ex){
            throw new ServerException("Problem with handling connection.", ex);
        } catch (ServerEventListenerException ex) {
            throw new ServerException("Problem with handling message.", ex);
        }
    }

    @Override
    public void sendMessage(int connectionId, ClientServerMessage message) throws ServerException{
        if(!started){
            throw new ServerException("Server hasn't been started yet.");
        }
        try{
            SocketChannel channel = channels.get(connectionId);
            ByteBuffer buffer = ByteBuffer.wrap(ClientServerMessage.getBytes(message));
            channel.write(buffer);
        } catch (IOException ex) {
            throw new ServerException("Can't send message.", ex);
        }
    }

    @Override
    public void sendBroadCastMessage(ClientServerMessage message) throws ServerException{
        if(!started){
            throw new ServerException("Server hasn't been started yet.");
        }
        try{
            byte[] rawMessage = ClientServerMessage.getBytes(message);
            for(SocketChannel channel : channels){
                ByteBuffer buffer = ByteBuffer.wrap(ClientServerMessage.getBytes(message));
                channel.write(buffer);
            }
        } catch (IOException ex) {
            throw new ServerException("Can't send message.", ex);
        }
    }
}
