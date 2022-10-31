package ticketingsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class RouteSection {

    int routeId;//车次序号
    int coachNum;//车厢数目
    int perSeatNum;//每节车厢的座位数目
    int totalSeatNum;//总座位数目，最后全都转化为座位号
    private ArrayList<AtomicLong> seatList;//下标是座位号，对应的值是座位在车次站台区间内的占用位图，缺点是最多纪录64位的区间
    public Map<Long,Ticket> ticketMap;//ConcurrentHashMap 已售出的车票，用于判断车票是否有效

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
        ticketMap = new ConcurrentHashMap<>();

    }


    //购票就是先读取余票后，写 seatList
    public  Ticket sellTicket(String passenger, int departure, int arrival) {

        long oldAvailSeat = 0;
        long newAvailSeat = 0;
        long temp = getBinaryInt(departure, arrival);

        int avaiSeatIndex = -1;


        //有锁
        synchronized(this.seatList) {
            for (int k = 0; k < seatList.size(); k++) {
                oldAvailSeat = seatList.get(k).longValue();
                long result = temp & oldAvailSeat;
                // System.out.println("temp:"+temp+",oldAvailSeat:"+oldAvailSeat);
                if (result == 0) {
                    newAvailSeat = temp | oldAvailSeat;
                    seatList.set(k,
                            new AtomicLong(newAvailSeat));
                    avaiSeatIndex = k;
                    break;
                }
            }
        }

/*
        //无锁
        for (int k = 0; k < seatList.size(); k++) {

                while(true){
                    oldAvailSeat = seatList.get(k).longValue();
                    long result = temp & oldAvailSeat;
                    if (result != 0) {
                        break;
                    }
                    else {
                        newAvailSeat = temp | oldAvailSeat;
                    }
                    if(this.seatList.get(k).compareAndSet(oldAvailSeat, newAvailSeat)){
                        avaiSeatIndex = k;
                    }
                }
                break;

        }
*/


        //无余票
        if (avaiSeatIndex == -1){
            return null;
        }

        Ticket ticket = new Ticket();
        ticket.passenger = passenger;
        ticket.route = routeId;

        ticket.coach = avaiSeatIndex/perSeatNum + 1;
        /*
        System.out.println("avaiSeatIndex: "+avaiSeatIndex+" coach:"+ticket.coach);
        if (ticket.coach > 2)
        {
            System.out.println("coach:"+ticket.coach);
        }
        */
        ticket.seat = avaiSeatIndex % perSeatNum + 1;
        ticket.departure =departure ;
        ticket.arrival = arrival;

        return ticket;
    }

    //读取seatList
    public  int inquiryTicket(int departure, int arrival) {


        long temp = getBinaryInt(departure, arrival);
        int count  = 0;
       // synchronized(this.seatList) {
        for (int k = 0; k < seatList.size(); k++) {
            //表示该区间有空
            if ((seatList.get(k).intValue() & temp) == 0) {
                count++;
            }
        }
       // }
        return count;
    }

    //退票就是直接写 seatList 和  ticketMap
    public  boolean refundTicket(Ticket ticket) {

        //先判断是否合法
        if (!ticketMap.containsKey(ticket.tid)){
            System.out.println("refund containsKey");
            return false;
        }

        //进行删除
        ticketMap.remove(ticket.tid);
        long temp = getBinaryInt(ticket.departure, ticket.arrival);
        temp = ~temp;
        int seatNumber = (ticket.coach - 1)*perSeatNum+ticket.seat-1;


        //无锁
        long oldAvailSeat = 0;
        long newAvailSeat = 0;
        do {
            oldAvailSeat = this.seatList.get(seatNumber).longValue();
            newAvailSeat = temp & oldAvailSeat;
        } while (!this.seatList.get(seatNumber).compareAndSet(oldAvailSeat, newAvailSeat));



/*
        //写前加锁
        synchronized(this.seatList){
            seatList.set(seatNumber,
                    new AtomicLong(seatList.get(seatNumber).longValue() & temp)
            );
        }
 */
        return true;
    }

    public long getBinaryInt(int departure, int arrival){
        long temp = 0;
        int i = departure - 1;
        while(i < arrival - 1){
            long pow = 1;
            pow = pow << i;
            temp |= pow;
            i++;
        }
        return temp;

    }
}
