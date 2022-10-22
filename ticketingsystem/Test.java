package ticketingsystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Test {
	private final static int ROUTE_NUM = 5;// 列车车次
	private final static int COACH_NUM = 8;// 车箱数
	private final static int SEAT_NUM = 10;// 每个车厢的座位数
	private final static int STATION_NUM = 10;// 总站数

	private final static int TEST_NUM = 10000;// 每个线程里调用的方法数是10000次
	private final static int refund = (int) (0.1 * TEST_NUM);// 退票数目
	private final static int buy = (int) (0.3 * TEST_NUM);// 买票数目
	private final static int query = (int) (0.6 * TEST_NUM);// 查询票数目
	private final static int thread = 64;// 线程数目

	public static void main(String[] args) throws InterruptedException {

		final TicketingDS tds = new TicketingDS(ROUTE_NUM, COACH_NUM, SEAT_NUM, STATION_NUM, TEST_NUM);
		Random r=new Random();

		ArrayList<Thread> threadList = new ArrayList<>();
		for(int i = 0; i < refund; i++){
			//先买后退
			Thread t = new Thread(new Runnable(){
				@Override
				public void run() {

					int departure = r.nextInt(STATION_NUM-1) + 1;
					int arrival = departure + r.nextInt(STATION_NUM - departure) + 1;
					Ticket result = tds.buyTicket("ding",r.nextInt(ROUTE_NUM)+1,departure,arrival);
					if (result!= null){
					//	printTicket(result);
						tds.refundTicket(result);
					}
				}
			});
			threadList.add(t);

		}
		for(int i = 0; i < buy - refund; i++){
			//买了不退
			Thread t = new Thread(new Runnable(){
				@Override
				public void run() {

					int departure = r.nextInt(STATION_NUM-1) + 1;
					int arrival = departure + r.nextInt(STATION_NUM - departure) + 1;
					Ticket result = tds.buyTicket("ding",r.nextInt(ROUTE_NUM)+1,departure,arrival);
				}
			});
			threadList.add(t);

		}

		for(int i = 0; i < query; i++){
			//查询余票
			Thread t = new Thread(new Runnable(){
				@Override
				public void run() {
					int departure = r.nextInt(STATION_NUM-1) + 1;
					int arrival = departure + r.nextInt(STATION_NUM - departure) + 1;
					tds.inquiry(r.nextInt(ROUTE_NUM)+1,departure,arrival);
				}
			});
			threadList.add(t);

		}


		for(int i = 0 ;i < threadList.size();i++){
			threadList.get(i).start();
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
