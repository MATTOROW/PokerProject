package ru.itis.pokerproject.clientserver.server.listeners;

import ru.itis.pokerproject.clientserver.server.ServerExample;

public abstract class AbstractServerEventListener implements ServerEventListener {
    protected boolean init;
    protected ServerExample server;

    @Override
    public void init(ServerExample server) {
        this.server = server;
        this.init = true;
    }
}
