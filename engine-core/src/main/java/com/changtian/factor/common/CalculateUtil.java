package com.changtian.factor.common;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class CalculateUtil {
    /**
     * 保留几位小数点
     */
    public static final int SCALE = 16;

    /**
     * 计算百分位
     * @param list 价格列表，方法内自动排序
     * @param depth 区间深度
     * @return 百分位map
     */
    public static Map<BigDecimal,BigDecimal> calcPercentile(List<BigDecimal> list, int depth){
        if(list.isEmpty()){
            log.error("Percentile spread array is empty,calcPercentile return null.");
            return null;
        }
        if(depth<2){
            log.error("depth must more than 1.");
            return null;
        }
        if(list.size()<depth){
            log.error("Percetntile spread array size can not be less than depth. list {} depth {}",list,depth);
            return null;
        }
        //排序
        Collections.sort(list);
        TreeMap<BigDecimal, BigDecimal> percentileMap = new TreeMap<>();
        //计算分位点
        percentileMap.put(new BigDecimal("0"), list.get(0));
        for (int j = 1; j < depth - 1; j++) {
            double position=(list.size()-1)/(depth-1.0)*j;
            BigDecimal num = BigDecimal.valueOf(100 / (depth - 1.0) * j);
            int count = (list.size() - 1) / (depth - 1) * j;
            if(position==count)//刚好处于整数位置
                percentileMap.put(num, list.get((int)position));
            else
            {  //分位不在整数位置，使用插值法插值
                int pos1=(int)Math.floor(position);
                int pos2=(int)Math.ceil(position);
                BigDecimal  pos1Value=list.get(pos1);
                BigDecimal  pos2Value=list.get(pos2);
                BigDecimal value=pos1Value.add(pos2Value.subtract(pos1Value)
                        .multiply(new BigDecimal(position-pos1)));
                percentileMap.put(new BigDecimal(100 / (depth - 1.0) * j),value);
            }
        }
        percentileMap.put(new BigDecimal("100"), list.get(list.size() - 1));
        log.info("the result of pecentile map:{}",percentileMap.toString());
        return percentileMap;
    }

    /**
     * 传入一个数列x计算方差
     * 方差s^2=[（x1-x）^2+（x2-x）^2+......（xn-x）^2]/（n）（x为平均数）
     * @param x 要计算的数列
     * @return 方差
     */
    public static BigDecimal variance(List<BigDecimal> x) {
        BigDecimal n = BigDecimal.valueOf(x.size());            //数列元素个数
        BigDecimal avg = average(x);    //求平均值
        BigDecimal var = BigDecimal.valueOf(0);
        for (BigDecimal i : x) {
            var = var.add(i.subtract(avg).multiply(i.subtract(avg)));    //（x1-x）^2+（x2-x）^2+......（xn-x）^2
        }
        return var.divide(n,SCALE,BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 标准差σ=sqrt(s^2)
     * 牛顿迭代法求大数开方
     * @param x 数列
     * @return 标准差
     */
    public static BigDecimal standardDeviation(List<BigDecimal> x) {
        //方差
        BigDecimal variance = variance(x);
        BigDecimal base2 = BigDecimal.valueOf(2.0);
        //计算精度
        int precision = 100;
        MathContext mc = new MathContext(precision, RoundingMode.HALF_UP);
        BigDecimal deviation = variance;
        int cnt = 0;
        while (cnt < 100) {
            deviation = (deviation.add(variance.divide(deviation, mc))).divide(base2, mc);
            cnt++;
        }
        deviation = deviation.setScale(SCALE, RoundingMode.HALF_UP);
        Double e = Math.sqrt(Double.parseDouble(variance.toString()));
        return deviation;
    }

    /**
     * Math内置方法计算标准差
     * @param x 数列
     * @return 标准差
     */
    public static BigDecimal mathStandardDeviation(List<BigDecimal> x){
        if(x.isEmpty()){
            log.error("StandardDeviation array is empty,calcStandardDeviation return null");
            return null;
        }
        BigDecimal variance = variance(x);
        double e = Math.sqrt(Double.parseDouble(variance.toString()));
        return BigDecimal.valueOf(e).setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 传入一个数列x计算平均值
     * @param x 数列
     * @return 平均值
     */
    public static BigDecimal average(List<BigDecimal> x) {
        if(x.isEmpty()){
            log.error("average array is empty,calc average return null");
            return null;
        }
        BigDecimal n = BigDecimal.valueOf(x.size());            //数列元素个数
        BigDecimal sum = BigDecimal.valueOf(0);
        for (BigDecimal i : x) {        //求和
            sum = i.add(sum);
        }
        return sum.divide(n,SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 强制转型成bigDecimal
     */
    public static BigDecimal castBigDecimal(Object obj){
        BigDecimal ret = null;
        if (obj instanceof  String){
            String objStr = (String) obj;
            ret = new BigDecimal(objStr);
        }
        if (obj instanceof  Long){
            Long objLong = (Long) obj;
            ret = new BigDecimal(objLong);
        }
        if (obj instanceof  Integer){
            Integer objLong = (Integer) obj;
            ret = new BigDecimal(objLong);
        }
        if (obj instanceof  Double){
            Double objLong = (Double) obj;
            ret = new BigDecimal(objLong);
        }
        if (obj instanceof  BigDecimal){
            ret = (BigDecimal) obj;
        }

        return  ret;
    }
}
