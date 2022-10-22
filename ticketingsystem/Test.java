package ticketingsystem;

public class Test {
	private final static int ROUTE_NUM = 5;// 列车车次
	private final static int COACH_NUM = 3;// 车箱数
	private final static int SEAT_NUM = 2;// 每个车厢的座位数
	private final static int STATION_NUM = 10;// 总站数

	private final static int TEST_NUM = 10000;// 每个线程里调用的方法数是10000次
	private final static int refund = 10;// 退票数目
	private final static int buy = 40;// 买票数目
	private final static int query = 100;// 查询票数目
	private final static int thread = 64;// 线程数目

	public static void main(String[] args) throws InterruptedException {

		final TicketingDS tds = new TicketingDS(ROUTE_NUM, COACH_NUM, SEAT_NUM, STATION_NUM, TEST_NUM);


		// 创建线程对象，采用匿名内部类方式。
		System.out.println((tds.inquiry(1,2,5)));

		for(int i = 0; i < 100; i++){

			Thread t = new Thread(new Runnable(){
				@Override
				public void run() {
					Ticket result = tds.buyTicket("ding",1,2,5);
					if (result!= null){
						System.out.println("Thread.currentThread().getId():"+Thread.currentThread().getId());
						printTicket(result);

						if ( Thread.currentThread().getId() % 3 == 0){
							System.out.println("refund:"+tds.refundTicket(result));
						}
					}


				}
			});

			// 启动线程
			t.start();
		}


	}

	private static void printTicket(Ticket ticket) {
		System.out.println("**************"+
				"\nTicket tid : "+ ticket.tid
				+"\npassenger: " + ticket.passenger
				+"\nroute: " + ticket.route
				+"\ncoach: " + ticket.coach
				+"\nseat: " + ticket.seat
				+"\ndeparture: " + ticket.departure
				+"\narrival: " + ticket.arrival
				+"\n**************");
	}


}
