package socket;

/**
 * Created by ymuslu on 16-03-2017.
 */
public interface IClientSocket extends Runnable {
    void sendMessage(SocketOutMessage message);
    void close();
}
