package ru.itis.pokerproject.shared.template.server;

import ru.itis.pokerproject.shared.template.listener.ServerEventListener;
import ru.itis.pokerproject.shared.template.message.AbstractServerMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSocketServer<E extends Enum<E>, M extends AbstractServerMessage<E>> implements Server<E, M> {
    protected List<ServerEventListener<E, M>> listeners;
    protected int port;
    protected ServerSocket server;
    protected boolean started;

    public AbstractSocketServer(int port) {
        this.listeners = new ArrayList<>();
        this.port = port;
        this.started = false;
    }

    @Override
    public void registerListener(ServerEventListener<E, M> listener) throws ServerException {
        if (started) {
            throw new ServerException("Server has been started already.");
        }
        this.listeners.add(listener);
    }

    @Override
    public void start() throws ServerException {
        try {
            server = new ServerSocket(this.port);
            started = true;

            while (true) {
                Socket s = server.accept();
                handleConnection(s);
            }
        } catch (IOException e) {
            throw new ServerException("Problem with server starting.", e);
        }
    }

    protected abstract void handleConnection(Socket socket);

    @Override
    public abstract void sendMessage(Socket socket, M message) throws ServerException;
}
