package cn.ztuo.bitrade.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
@Data
public class MergeOrder {

    public MergeOrder(){

    }
    private List<ExchangeOrder> orders = new ArrayList<>();

    //最后位置添加一个
    public void add(ExchangeOrder order){
        orders.add(order);
    }


    public ExchangeOrder get(){
        return orders.get(0);
    }

    public int size(){
        return orders.size();
    }

    public BigDecimal getPrice(){
        return orders.get(0).getPrice();
    }

    public Iterator<ExchangeOrder> iterator(){
        return orders.iterator();
    }
}
