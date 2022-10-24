package ticketingsystem;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class RandomTest {
    private final static int ROUTE_NUM = 5;// 列车车次
    private final static int COACH_NUM = 8;// 车箱数
    private final static int SEAT_NUM = 100;// 每个车厢的座位数
    private final static int STATION_NUM = 10;// 总站数

    private final static int TEST_NUM = 640000;// 每个线程里调用的方法数是10000次
    private final static int refund = 10;// 退票百分比
    private final static int buy = 40;// 买票百分比
    private final static int query = 100;// 查询票百分比
    private static int threadNum = 8;// 线程数目
    final static Map<Long,Long> recordRefundTime =  new ConcurrentHashMap<>();;
    final static Map<Long,Long> recordBuyTime = new ConcurrentHashMap<>();;
    final static Map<Long,Long> recordQueryTime = new ConcurrentHashMap<>();;

    private static String passengerName() {
        Random rand = new Random(System.currentTimeMillis());
        long uid = rand.nextInt(TEST_NUM);
        return "passenger" + uid;
    }
    public static void main(String[] args) throws InterruptedException {
        int[ ] threadNums = {4,8,16,32,64};
        for(int  i = 0 ;i < threadNums.length;i++){
            threadNum = threadNums[i];
            testFixedNumberThread();
        }
    }
    public static void testFixedNumberThread() throws InterruptedException {
        final TicketingDS tds = new TicketingDS(ROUTE_NUM, COACH_NUM, SEAT_NUM, STATION_NUM, threadNum);
        Random r=new Random();
        int perThreadTestNum = TEST_NUM/ threadNum;

        Thread[] threads = new Thread[threadNum];
        for (int i = 0; i < threadNum; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    Random rand = new Random(System.currentTimeMillis());
                    Ticket ticket = new Ticket();
                    ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();

                    long totalRefundTime = 0,totalBuyTime = 0,totalQueryTime = 0; //总时间
                    long totalRefundTimes = 0,totalBuyTimes = 0,totalQueryTimes = 0; //次数
                    for (int i = 0; i < perThreadTestNum; i++) {
                        int species = rand.nextInt(query);
                        if (0 <= species && species < refund && !soldTicket.isEmpty()){
                            int select = rand.nextInt(soldTicket.size());
                            if ((ticket = soldTicket.remove(select)) != null) {
                                long startTime = System.nanoTime();
                                if (tds.refundTicket(ticket)) {
                                    totalRefundTime += System.nanoTime() - startTime;
                                    totalRefundTimes +=1;
                                    //System.out.println(startTime + " " + endTime + " " + ThreadId.get() + " "
                                    //        + "TicketRefund" + " " + ticket.tid + " " + ticket.passenger + " "
                                    //        + ticket.route + " " + ticket.coach + " " + ticket.departure + " "
                                    //      + ticket.arrival + " " + ticket.seat);
                                    //System.out.flush();
                                } else {
                                    assert false : "Err: cannot refund ticket";
                                }
                            } else {
                                assert false : "Err: soldTicket out of bounds";
                            }

                        }else if((refund <= species && species < buy)){
                            String passenger = passengerName();
                            int route = rand.nextInt(ROUTE_NUM) + 1;
                            int departure = rand.nextInt(STATION_NUM - 1) + 1;
                            int arrival = departure + rand.nextInt(STATION_NUM - departure) + 1;
                            long startTime = System.nanoTime();
                            if ((ticket = tds.buyTicket(passenger, route, departure, arrival)) != null) {
                                totalBuyTime += System.nanoTime() - startTime;
                                totalBuyTimes +=1;
                                soldTicket.add(ticket);
                                // System.out.println(preTime + " " + postTime + " " + ThreadId.get() + " "
                                //         + "TicketBought" + " " + ticket.tid + " " + ticket.passenger + " "
                                //         + ticket.route + " " + ticket.coach + " " + ticket.departure + " "
                                //         + ticket.arrival + " " + ticket.seat);
                            }

                        }else if((buy <= species && species < query)){

                            int route = rand.nextInt(ROUTE_NUM) + 1;
                            int departure = rand.nextInt(STATION_NUM - 1) + 1;
                            int arrival = departure + rand.nextInt(STATION_NUM - departure) + 1;

                            long startTime = System.nanoTime();
                            tds.inquiry(route, departure, arrival);
                            totalQueryTime += System.nanoTime() - startTime;
                            totalQueryTimes +=1;

                        }
                    }

                    //将统计结果保存
                    recordRefundTime.put(Thread.currentThread().getId(),totalRefundTime/totalRefundTimes) ;
                    recordBuyTime.put(Thread.currentThread().getId(),totalBuyTime/totalBuyTimes)  ;
                    recordQueryTime.put(Thread.currentThread().getId(),totalQueryTime/totalQueryTimes)  ;

                }
            });
            threads[i].start();

        }
        for (int i = 0; i < threadNum; i++) {
            threads[i].join();
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


        System.out.println(
                "Using Thread: " + threadNum +", "
                + " buyAvgTime: " + buyAvgTime + " ns,"
                + " refundAvgTime: " +refundAvgTime + " ns,"
                + " queryAvgTime: " +queryAvgTime + " ns,");
    }

}
