package ru.itis.pokerproject.shared.template.server;

import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class SocketArrayList extends CopyOnWriteArrayList<Socket> {
    @Override
    public Socket remove(int index) {
        Socket socket = get(index);
        try {
            socket.close(); // Закрываем сокет перед удалением
            System.out.println("Сокет закрыт: " + socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        boolean removed = super.remove(o);
        if (removed && o instanceof Socket socket) {
            try {
                socket.close(); // Закрываем сокет перед удалением
                System.out.println("Сокет закрыт: " + socket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return removed;
    }

    public boolean removeWithoutClosing(Object o) {
        return super.remove(o);
    }

    public Socket removeWithoutClosing(int i) {
        return super.remove(i);
    }
}
