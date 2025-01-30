package ru.itis.pokerproject.shared.template.listener;

import ru.itis.pokerproject.shared.template.server.Server;

public abstract class AbstractServerEventListener implements ServerEventListener {
    protected boolean init;
    protected Server server;

    @Override
    public void init(Server server) {
        this.server = server;
        this.init = true;
    }
}
