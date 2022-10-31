package ticketingsystem;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class UnitTest {
	private final static int ROUTE_NUM = 2;// 列车车次
	private final static int COACH_NUM = 5;// 车箱数
	private final static int SEAT_NUM = 3;// 每个车厢的座位数
	private final static int STATION_NUM = 2;// 总站数

	private final static int TEST_NUM = 10000;// 每个线程里调用的方法数是10000次
	private final static int refund = (int) (0.1 * TEST_NUM);// 退票数目
	private final static int buy = (int) (0.3 * TEST_NUM);// 买票数目
	private final static int query = (int) (0.6 * TEST_NUM);// 查询票数目
	private final static int thread = 64;// 线程数目
	final static Map<Long,Long> recordRefundTime =  new ConcurrentHashMap<>();;
	final static Map<Long,Long> recordBuyTime = new ConcurrentHashMap<>();;
	final static Map<Long,Long> recordQueryTime = new ConcurrentHashMap<>();;
	public static void main(String[] args) throws InterruptedException {

		final Ticketing tds = new Ticketing(ROUTE_NUM, COACH_NUM, SEAT_NUM, STATION_NUM, TEST_NUM);
		Random r=new Random();

		final long startTime = System.nanoTime();

		ArrayList<Thread> threadList = new ArrayList<>();
		for(int i = 0; i < refund; i++){
			//先买后退
			Thread t = new Thread(new Runnable(){
				@Override
				public void run() {

					int departure = r.nextInt(STATION_NUM-1) + 1;
					int arrival = departure + r.nextInt(STATION_NUM - departure) + 1;

					long preTime = System.nanoTime();
					Ticket result = tds.buyTicket("ding",r.nextInt(ROUTE_NUM)+1,departure,arrival);
					long afterTime = System.nanoTime();

					recordBuyTime.put(Thread.currentThread().getId(),afterTime - preTime);
					if (result!= null){
						printTicket(result);
						 preTime = System.nanoTime();
						tds.refundTicket(result);
						 afterTime = System.nanoTime();
						recordRefundTime.put(Thread.currentThread().getId(),afterTime - preTime);
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
					long preTime = System.nanoTime();
					Ticket result = tds.buyTicket("ding",r.nextInt(ROUTE_NUM)+1,departure,arrival);
					long afterTime = System.nanoTime();
					if (result!= null) {
						printTicket(result);
					}

					recordBuyTime.put(Thread.currentThread().getId(),afterTime - preTime);
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
					long preTime = System.nanoTime();
					tds.inquiry(r.nextInt(ROUTE_NUM)+1,departure,arrival);
					long afterTime = System.nanoTime();
					recordQueryTime.put(Thread.currentThread().getId(),afterTime - preTime);
				}
			});
			threadList.add(t);

		}


		for(int i = 0 ;i < threadList.size();i++){
			threadList.get(i).start();
			threadList.get(i).join();
		}

		long buyAvgTime = 0;
		for (Long value:recordBuyTime.values()){
			buyAvgTime += value;
		}
		buyAvgTime /= recordBuyTime.size();

		long refundAvgTime = 0;
		for (Long value:recordRefundTime.values()){
			refundAvgTime += value;
		}
		refundAvgTime /= recordRefundTime.size();

		long queryAvgTime = 0;
		for (Long value:recordQueryTime.values()){
			queryAvgTime += value;
		}
		queryAvgTime /= recordQueryTime.size();


		System.out.println("buyAvgTime: " + buyAvgTime + "ns\n"
							+ "refundAvgTime: " +refundAvgTime + "ns\n"
							+ "queryAvgTime: " +queryAvgTime + "ns\n");
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
