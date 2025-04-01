package com.changtian.factor.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.lexer.token.OperatorType;
import com.changtian.factor.common.JsonUtil;
import com.changtian.factor.flink.aviatorfun.entity.IndexCalcResult;
import javassist.scopedpool.SoftValueHashMap;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.tuple.Tuple2;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 表达式计算工具类
 */
@Slf4j
@UtilityClass
public class TaScriptUtils {
    private static final Map<String,Expression> cache = new SoftValueHashMap<>();
    static {
        AviatorEvaluatorInstance instance = AviatorEvaluator.getInstance();
//        instance.addFunction(new RefFunction());
//        instance.addFunction(new HhvFunction());
//        instance.addFunction(new LlvFunction());
        instance.aliasOperator(OperatorType.ADD,"and");
        instance.aliasOperator(OperatorType.OR,"or");
    }

    /**
     * 执行表达式
     * @param strExpression 表达式
     * @param context 参数数据
     * @return {结果，参数}
     */
    public static Tuple2<Boolean,Map<String, Object>> invoke(String strExpression,Map<String,Object> context){
        try {
            Expression compliedExp = cache.compute(strExpression,
                    (exp, expression) -> expression != null ? expression : AviatorEvaluator.compile(exp));
            Map<String,Object> hashMap = new HashMap<>();
            List<String> variableNames = compliedExp.getVariableNames();
            for (String variableName : variableNames) {
                if (!validateParamNum(strExpression,variableName,context.get(variableName))) {
                    return null;
                }
                hashMap.put(variableName,context.get(variableName));
            }
            Tuple2<Boolean, Map<String, Object>> result = Tuple2.of((Boolean) compliedExp.execute(context), hashMap);
            return result;
        }catch (Exception e){
            log.error("express:["+strExpression+"] can not run",e);
        }
        return Tuple2.of(false, Collections.emptyMap());
    }

    /**
     * 校验判断自定义函数中个数是否正确
     * @param strExpression 表达式
     * @param variableName 参数
     * @param value 参数值
     * @throws JsonProcessingException
     */
    public static Boolean validateParamNum(String strExpression,String variableName,Object value) throws JsonProcessingException {
        //判断是否为list_开头的参数
        if (variableName.startsWith("list_")) {
            String[] split = strExpression.split(variableName);
            Optional<Integer> max = Arrays.stream(split)
                    .filter(e -> e.startsWith(","))
                    .map(e -> Integer.valueOf(e.substring(1, e.indexOf(")"))))
                    .max(Integer::compareTo);
            //是 则获取variableName,与)之间的数字个数参数
            int num = max.orElse(100);
            //校验list数据是否足够，不够则不进行计算
            List<IndexCalcResult> list = JsonUtil.toList(JsonUtil.toJson(value), IndexCalcResult.class);
            return !CollectionUtils.isEmpty(list);
        }
        return true;
    }

    /**
     * 计算中间价
     */
    public static double clacMidPrice(String bid,String ask){
        BigDecimal bidPrice = new BigDecimal(bid);
        BigDecimal askPrice = new BigDecimal(ask);
        return bidPrice.add(askPrice)
                .divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
