package ticketingsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class RouteSection {

    int routeId;//车次序号
    int coachNum;//车厢数目
    int perSeatNum;//每节车厢的座位数目
    int totalSeatNum;//总座位数目，最后全都转化为座位号
    private ArrayList<AtomicLong> seatList;//下标是座位号，对应的值是座位在车次站台区间内的占用位图，缺点是最多纪录64位的区间
    private Map<Long,Ticket> ticketMap;//已售出的车票，用于判断车票是否有效
    private AtomicLong ticketId;//车票的票号，用于给出下一个车票的id
    //用一个数组记录已经售出的票号

    public RouteSection(int routeId, int coachNum, int seatNum) {
        this.routeId = routeId;
        this.coachNum = coachNum;
        this.perSeatNum = seatNum;
        this.totalSeatNum = seatNum * coachNum;
        seatList = new ArrayList<>();
        for(int i = 0 ; i < totalSeatNum;i++){
            seatList.add(new AtomicLong(0));
        }
        ticketMap = new HashMap<>();
        ticketId = new AtomicLong(1);
    }


    //购票就是先读取余票后，写 seatList
    public synchronized  Ticket initSeal(String passenger, int departure, int arrival) {

        long oldAvailSeat = 0;
        long newAvailSeat = 0;
        long temp = 0;

        int i = departure - 1;
        while(i < arrival ){
            long pow = 1;
            pow = pow << i;
            temp |= pow;
            i++;
        }


        int avaiSeatIndex = -1;
        for( int k = 0; k < seatList.size();k++){
            oldAvailSeat = seatList.get(k).longValue();
            long result = temp & oldAvailSeat;
           // System.out.println("temp:"+temp+",oldAvailSeat:"+oldAvailSeat);
            if (result == 0){
                newAvailSeat = temp | oldAvailSeat;
                seatList.set(k,
                        new AtomicLong(newAvailSeat));
                avaiSeatIndex = k;
                break;
            }
        }
        //无余票
        if (avaiSeatIndex == -1){
            return null;
        }

        Ticket ticket = new Ticket();
        ticket.tid = ticketId.getAndIncrement();
        ticket.passenger = passenger;
        ticket.route = routeId;
        ticket.coach = avaiSeatIndex/perSeatNum + 1;
        ticket.seat = avaiSeatIndex % perSeatNum + 1;

        ticket.departure =departure ;
        ticket.arrival = arrival;

        ticketMap.put(ticket.tid,ticket);

        return ticket;
    }

    //读取seatList
    public synchronized  int initInquiry(int departure, int arrival) {

        long temp = 0;
        int i = departure - 1;
        while(i < arrival - 1){
            long pow = 1;
            pow = pow << i;
            temp |= pow;
            i++;
        }
        int count  = 0;
        for(int k = 0 ;k < seatList.size();k++){
            //表示该区间有空
            if((seatList.get(k).intValue() & temp)== 0){
                count++;
            }
        }

        return count;
    }

    //退票就是直接写 seatList 和  queue_SoldTicket
    public synchronized  boolean initRefund(Ticket ticket) {

        //先判断是否合法
        if (!ticketMap.containsKey(ticket.tid)){
            return false;
        }

        //进行删除
        ticketMap.remove(ticket.tid);
        long temp = 0;
        int i = ticket.departure - 1;
        while(i < ticket.arrival ){
            long pow = 1;
            pow = pow << i;
            temp |= pow;
            i++;
        }
        temp = ~temp;



        int seatNumber = (ticket.coach - 1)*perSeatNum+ticket.seat-1;

        seatList.set(seatNumber,
                new AtomicLong(seatList.get(
                        seatNumber).longValue() & temp)
        );


        return true;
    }
}
