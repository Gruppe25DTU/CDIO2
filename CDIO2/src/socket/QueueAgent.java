package socket;

public class QueueAgent extends Thread {

	private SocketController sCtrl;
	private SocketQueue queue;

	public QueueAgent(SocketController sCtrl)
	{
		queue = SocketQueue.getInstance();
		this.sCtrl = sCtrl;
	}

	@Override
	public void run()
	{
		while(true)
		{
			try 
			{
				Thread.currentThread().sleep(300);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			if(sCtrl.getActiveSocket()==null || !sCtrl.getActiveSocket().isActive())
				sCtrl.setActiveSocket(queue.deQueue());
		}
	}
}
