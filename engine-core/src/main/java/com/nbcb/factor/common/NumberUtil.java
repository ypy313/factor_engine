package com.nbcb.factor.common;

import cn.hutool.core.lang.Assert;
import com.sun.org.apache.bcel.internal.generic.PUSH;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 数值工具类
 */
public class NumberUtil {
    private static final int SCALE = 12;
    public static int getOneToHundred()
    {
        return new Random().nextInt(100) + 1;
    }
    public static double round(BigDecimal val){
        return round(val,SCALE);
    }
    public static double round(BigDecimal val, int scale){
        return val.setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }
    public static double round(double val){
        return round(val,SCALE);
    }
    public static double round(double val, int scale){
        double factor = Math.pow(10,scale);
        return Math.round(val*factor)/factor;
    }

    /**
     * 计算后N位的ma
     * 在n>0的时候，不会出现问题
     */
    public static double ma2(double[] array,int n){
        String format = "(n=%d,len=%d) 不满足 n>0 && array.length>=n";
        Assert.isTrue(n>0&&array.length>=n,String.format(format,n,array.length));
        //截取，后n个数据
        OptionalDouble ma = Arrays.stream(array).skip(array.length-n).average();
        return ma.orElse(0D);
    }

    /**
     * 单个ema计算ema(X,n) = 2/(n+1)*X+(n-1)/(n+1)ref(ema,1)
     * @param x o/h/l/c/x
     * @param emaList emaList集合
     * @param n n参数
     */
    public static double ema(double x, List<Double> emaList, int n){
        //2/(n+1)*close
        double emaHead = 2*x/(n+1);
        //(n-1)/(n+1)*ref(ema,1)
        double refEma = emaList.isEmpty() ?x:emaList.get(emaList.size()-1);
        double emaEnd = (n-1)*refEma/(n+1);
        return emaHead+emaEnd;
    }

    /**
     * @param currIndex 当前坐标
     * @param moveStep 向后取第n个
     */
    public static Optional<Object> ref(Object[] list,int currIndex,int moveStep){
        int targetIndex = currIndex+moveStep;
        if (rangeCheck(currIndex,list.length) && rangeCheck(targetIndex,list.length)) {
            return Optional.ofNullable(list[targetIndex]);
        }
        return Optional.empty();
    }

    public static boolean rangeCheck(int index,int size){
        return index <size && index>-1;
    }

    /**
     * 求和
     */
    public static double sum(List<Double> list){
        return list.stream().mapToDouble(Double::doubleValue).sum();
    }
}
