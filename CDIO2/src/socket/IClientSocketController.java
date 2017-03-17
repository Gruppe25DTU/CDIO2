package socket;

/**
 * Created by ymuslu on 17-03-2017.
 */
public interface IClientSocketController {
    void registerClientSocket(IClientSocket clientSocket);
    void unRegisterClientSocket(IClientSocket clientSocket);
    void notify(SocketInMessage message);
}
