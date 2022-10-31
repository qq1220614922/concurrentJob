package ticketingsystem;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class Ticketing implements TicketingSystem {
    private int routeNum;// 车次总数
    private int stationNum;// 车站总数
    private int threadnum; // 线程数
    private AtomicLong ticketId;//车票的票号，用于给出下一个车票的id
    private ArrayList<RouteSection> routeArray;//车次列表
    Ticketing(int routenum, int coachnum, int seatnum, int  stationnum, int  threadnum){
        this.routeNum = routenum;
        this.stationNum = stationnum;
        routeArray = new ArrayList<>();
        for (int routeId = 1; routeId <= routeNum; routeId++)
            this.routeArray.add(new RouteSection(routeId, coachnum, seatnum));
        this.threadnum = threadnum;
        ticketId = new AtomicLong(1);
    }

    Ticketing(){
        this.routeNum = 5;
        this.stationNum = 10;
        for (int routeId = 1; routeId <= routeNum; routeId++)
            this.routeArray.add(new RouteSection(routeId, 8, 100));
        this.threadnum = 16;
        ticketId = new AtomicLong(1);

    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        //先判断车次和车站是否在范围内
        if (route <= 0 || route > this.routeNum || arrival > this.stationNum
                || departure >= arrival)
            return null;
        //尝试购票，并返回(route - 1)
        Ticket ticket = this.routeArray.get(route - 1).sellTicket(passenger, departure, arrival);
        if(ticket != null){
            ticket.tid = ticketId.getAndIncrement();
            this.routeArray.get(route - 1).ticketMap.put(ticket.tid,ticket);
        }
        return ticket;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {


        //先判断车次和车站是否在范围内
        if (route <= 0 || route > this.routeNum || arrival > this.stationNum
                || departure >= arrival)

            return -1;

        //尝试查询，并返回(route - 1)
        return this.routeArray.get(route - 1).inquiryTicket(departure, arrival);
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        //获取车票的车次
        final int routeId = ticket.route;
        //先判断车票和车次是否在范围内
        if (ticket == null || routeId <= 0 || routeId > this.routeNum){
            return false;
        }
        //尝试退票，并返回(route - 1)
        return this.routeArray.get(routeId - 1).refundTicket(ticket);

    }

    @Override
    public boolean buyTicketReplay(Ticket ticket) {


        return false;
    }

    @Override
    public boolean refundTicketReplay(Ticket ticket) {


        return false;
    }


}
