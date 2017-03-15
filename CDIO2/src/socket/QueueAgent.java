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
				Thread.currentThread().sleep(1000);
			} 
			catch (InterruptedException e) 
			{
				
			}
			if(sCtrl.getActiveSocket()==null || !sCtrl.getActiveSocket().isActive())
			{
				if(sCtrl.getActiveSocket()!=null)
					sCtrl.getActiveSocket().unRegisterObserver(sCtrl);
				sCtrl.setActiveSocket(queue.deQueue());
			}
				
		}
		
		
	}
}
