package com.changtian.factor.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.changtian.factor.common.JsonUtil;
import com.changtian.factor.common.RedisUtil;
import com.changtian.factor.common.StringUtil;
import com.changtian.factor.common.key.MyKey;
import com.changtian.factor.enums.DataTypeEnum;
import com.changtian.factor.enums.ResultTypeEnum;
import com.changtian.factor.event.ForexOhlcInputEvent;
import com.changtian.factor.event.SymbolInputEvent;
import com.changtian.factor.external.IOhlcHistoryService;
import com.changtian.factor.external.assembler.mapper.FactorFxOhlcHisMapper;
import com.changtian.factor.external.dto.FactorFxOhlcHis;
import com.changtian.factor.flink.aviatorfun.entity.OhlcParam;
import com.changtian.factor.output.FxOhlcResultOutputEvent;
import com.changtian.factor.output.OhlcDetailResult;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ohlc行情队列
 */
@Slf4j
public class GeneQueueManager {
    /**
     * 单例
     */
    private static volatile GeneQueueManager INSTANCE;

    /**
     * 单例
     */
    public static GeneQueueManager getInstance() {
        if (INSTANCE == null) {
            synchronized (GeneQueueManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new GeneQueueManager();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 每个k线缓存数据的最大数量，默认501
     */
    private static final int CAPACITY = 501;
    /**
     * OHLC行情队列， key-myKey(symbol) value-ohlcParam
     */
    private final Map<String, OhlcParam[]> queueMap = new ConcurrentHashMap<String, OhlcParam[]>();
    /**
     * 判断是否通过请求加载过ohlc数据
     */
    private final ConcurrentHashMap.KeySetView<String, Boolean> isLoadCache = ConcurrentHashMap.newKeySet();

    /**
     * 内存初始的ohlc查询存储的redis key
     */
    public static final String FACTOR_FOREX_HISTORY_OHLC_QUEUE =
            StringUtil.redisKeyFxJoint(DataTypeEnum.FOREX_HISTORY_OHLC_QUEUE.getKey());

    /**
     * 无需计算的返回false
     */
    public boolean addDetailToQueue(FxOhlcResultOutputEvent inputEvent) {
        return updateQueueLastOne(OhlcParam.convert(inputEvent.getEventData(), inputEvent.getSrcTimestamp()));
    }

    /**
     * 无需计算的返回false
     */
    public boolean addHistoryToQueue(ForexOhlcInputEvent inputEvent) {
        return addQueue(OhlcParam.convert((OhlcDetailResult) inputEvent.getEventData(), inputEvent.getSrcTimestamp()));
    }

    /**
     * 更新并获取内存ohlc bar 线数据
     *
     * @param event 行情数据
     * @return bar线队列
     */
    public List<OhlcParam> getAndUpdateDetail(SymbolInputEvent event) {
        OhlcDetailResult ohlcDetailResult = (OhlcDetailResult) event.getEventData();
        String symbolPeriod = ohlcDetailResult.getSymbol() + "_" + ohlcDetailResult.getPeriod();
        //Detail 行情更新并获取公共队列
        if (ResultTypeEnum.DETAIL.getCode().equals(ohlcDetailResult.getSummaryType())) {
            boolean updateFlag = updateQueueLastOne(OhlcParam.convert(ohlcDetailResult, event.getScQuoteTime()));
            if (!updateFlag) {
                log.error("ohlc queue update is failed");
                return Collections.emptyList();
            }
        }
        //获取行情队列
        List<OhlcParam> ohlcParamList = getOhlcArrayWithNonNullElement(
                new MyKey(ohlcDetailResult.getSymbol(), ohlcDetailResult.getPeriod(), ""));
        if (CollectionUtils.isEmpty(ohlcParamList)) {
            log.warn("ohlcParamList size is less than 501 for symbol:[{}]", symbolPeriod);
            return Collections.emptyList();
        }
        return ohlcParamList;
    }

    /**
     * 更新历史队列
     *
     * @param event 行情
     */
    public void updateHisOhlcBar(SymbolInputEvent event) {
        OhlcDetailResult ohlcDetailResult = (OhlcDetailResult) event.getEventData();
        //HIS行情更新
        if (ResultTypeEnum.HIS.getCode().equals(ohlcDetailResult.getSummaryType())) {
            addHistoryToQueue((ForexOhlcInputEvent) event);
        }
    }

    /**
     * 初始/获取ohlc队列
     */
    public OhlcParam[] getOrCreateQueue(MyKey key) {
        return queueMap.compute(key.toString(), (k, oldValue) -> {
            oldValue = oldValue != null ? oldValue : new OhlcParam[CAPACITY];
            if (oldValue.length == 0 || oldValue[0] == null) {
                return initValue(key, oldValue);
            }
            return oldValue;
        });
    }

    /**
     * detail 永远更新最后一个
     * <p>eg1:[null,null,被更新]</p>
     * <p>eg1:[ohlc1,null,被更新]</p>
     * <p>eg1:[ohlc1,ohlc2,被更新]</p>
     */
    public boolean updateQueueLastOne(OhlcParam ohlc) {
        MyKey myKey = new MyKey(ohlc.getSymbol(), ohlc.getPeriod(), "");
        synchronized (this) {
            OhlcParam[] queue = getOrCreateQueue(myKey);
            if (queue == null || queue.length == 0) {
                return false;
            }
            queue[queue.length - 1] = ohlc;
        }
        return true;
    }

    /**
     * 从redis加载
     */
    public OhlcParam[] initValue(MyKey key, OhlcParam[] queue) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("init history").append(key.toAllString()).append("start").append(LocalTime.now()
                .format(DateTimeFormatter.ofPattern("ss.SSS"))).append(":");
        appendInfoString(queue, logBuilder);
        //优先 redis ,里面的元素不含null
        OhlcParam[] redisOhlcArr = queryHistoryOhlc(key.getSymbol(), key.getPeriod());
        int srcLen = redisOhlcArr.length;
        //历史ohlc个数不够，先尝试从api加载
        if (srcLen < CAPACITY && isLoadCache.add(key.toString())) {
            //todo sourceType 贵金属可能不一样
            List<FactorFxOhlcHis> factorFxOhlcHis = IOhlcHistoryService.getInstance()
                    .listBy(key.getSymbol(), key.getPeriod(), CAPACITY, "");
            OhlcParam[] dbOhlcArr = FactorFxOhlcHisMapper.INSTANCE.convert(factorFxOhlcHis).toArray(new OhlcParam[0]);
            //redis与数据中数据去重
            redisOhlcArr = unionAndSort(redisOhlcArr, dbOhlcArr);
            srcLen = redisOhlcArr.length;
            setHistoryOhlc(key.getSymbol(), key.getPeriod(), redisOhlcArr);
        }
        /**
         * 从redis数组的第几位获取
         * 已知redis数组里存的数据不知道有多少个，可能超长，可能不够长
         * queue = {null,null,'实时位，不更新'}，可更新index=[0~queue.len-2],最大可更新元素数 N=queue.len-1
         * redisOhlcArr.len 如果>=N,需截取末N位更新，即srcIndex=[redisOhlcArr.len-N~rediOhlcArr.len-1]
         * redisOhlcArr.len 如果<N全取，即srcIndex =[0~redisOhlcArr.len-1]
         */
        for (int i = Math.max(srcLen - CAPACITY, 0), dest = 0; i < srcLen; i++, dest++) {
            queue[dest] = redisOhlcArr[i];
        }
        logBuilder.append("\nend:");
        appendInfoString(queue, logBuilder);
        log.info(logBuilder.toString());
        return queue;
    }

    /**
     * union & distinct by time & order by time asc
     */
    private static OhlcParam[] unionAndSort(OhlcParam[] arr1, OhlcParam[] arr2) {
        int len1 = arr1 == null ? 0 : arr1.length;
        int len2 = arr2 == null ? 0 : arr2.length;
        Set<String> existTime = new HashSet<String>(len1 + len2);
        List<OhlcParam> list = new ArrayList<>(len1 + len2);
        if (arr1 != null) {
            Collections.addAll(list, arr1);
        }
        if (arr2 != null) {
            Collections.addAll(list, arr2);
        }
        return list.stream().filter(e -> !isNull(e.getEndTime()) && existTime.add(e.getEndTime()))
                .sorted(Comparator.comparing(OhlcParam::getEndTime)).toArray(OhlcParam[]::new);
    }

    public boolean addQueue(OhlcParam ohlc) {
        MyKey myKey = new MyKey(ohlc.getSymbol(), ohlc.getPeriod(), "");
        synchronized (this) {
            OhlcParam[] queue = getOrCreateQueue(myKey);
            if (queue == null || queue.length <= 0) {
                return false;
            }
            int len = queue.length;
            int theSecondToLastIndex = len - 2;//倒数第2个
            if (queue[theSecondToLastIndex] != null) {
                moveLeft(queue);
                //倒数第2个为原先的最后一个（实时数据），依旧放到最后
                queue[len - 1] = queue[theSecondToLastIndex];
                //倒数第二个更新为最新的his
                queue[theSecondToLastIndex] = ohlc;
            } else {
                for (int i = 0; i < len - 1; i++) {
                    if (queue[i] == null) {
                        //获取上一个是否为同一根bar,是则不进行更新
                        queue[i] = ohlc;
                        break;
                    }
                }
            }
            //sink redis
            OhlcParam[] toRedisHistoryArray = toRedisHistoryArray(queue);
            StringBuilder sb = new StringBuilder();
            sb.append("histories to redis").append(myKey).append(":");
            appendInfoString(toRedisHistoryArray, sb);
            log.info(sb.toString());
            setHistoryOhlc(ohlc.getSymbol(), ohlc.getPeriod(), toRedisHistoryArray);
        }
        return true;
    }

    /**
     * addQueue /updateQueueLastOne 后被调用
     *
     * @return element with not null
     */
    @NonNull
    public List<OhlcParam> getOhlcArrayWithNonNullElement(MyKey key) {
        OhlcParam[] queue = queueMap.get(key.toString());
        if (queue == null || queue.length <= 0 || queue[queue.length - 1] == null) {
            log.info("无实时ohlc,返空组，一般发生在初始化后");
            return new ArrayList<>();
        }
        return Arrays.stream(queue).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 仅要非null,且末位作为实时数据不进行存储
     */
    private static OhlcParam[] toRedisHistoryArray(OhlcParam[] queue) {
        int len = queue.length - 1;
        List<OhlcParam> list = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            if (queue[i] != null) {
                list.add(queue[i]);
            }
        }
        return list.toArray(new OhlcParam[0]);
    }

    /**
     * 数组元素都左移1位
     */
    private static void moveLeft(@NonNull OhlcParam[] queue) {
        if (queue.length - 1 >= 0) {
            System.arraycopy(queue, 1, queue, 0, queue.length - 1);
        }
    }

    private static boolean isNull(String str) {
        return str == null || "null".equals(str);
    }

    /**
     * ohlc数据打印
     */
    static void appendInfoString(OhlcParam[] queue, StringBuilder sb) {
        String lastType = null;
        String thisType;
        int count = 0;
        sb.append('[');
        for (OhlcParam e : queue) {
            thisType = e == null ? "null" : "OhlcParam";
            if (lastType == null) {
                lastType = thisType;
                count = 1;
            } else {
                if (thisType.equals(lastType)) {
                    count++;
                } else {
                    sb.append(lastType).append(" x ").append(count).append(',').append(' ');
                    lastType = thisType;
                    count = 1;
                }
            }
        }
        if (count > 0) {
            sb.append(lastType).append(" x ").append(count).append(',').append(' ');
        }
        sb.replace(sb.length() - 2, sb.length(), "]");
    }

    /**
     * N个非空历史
     */
    public static OhlcParam[] queryHistoryOhlc(String symbol, String period) {
        RedisUtil redis = RedisUtil.getInstance();
        if (redis.keyExists(FACTOR_FOREX_HISTORY_OHLC_QUEUE, symbol + "_" + period)) {
            String json = redis.getString(FACTOR_FOREX_HISTORY_OHLC_QUEUE, symbol + "_" + period);
            if (StringUtils.hasLength(json)) {
                try {
                    return JsonUtil.toObject(json, OhlcParam[].class);
                } catch (JsonProcessingException e) {
                    log.error("query redis key :{} fail!", FACTOR_FOREX_HISTORY_OHLC_QUEUE, e);
                }
            }
        }
        return new OhlcParam[0];
    }

    /**
     * 正常队列 = N个历史 +末位实时
     */
    public static void setHistoryOhlc(String symbol, String period, OhlcParam[] queue) {
        RedisUtil redis = RedisUtil.getInstance();
        if (ArrayUtils.isNotEmpty(queue)) {
            redis.setString(FACTOR_FOREX_HISTORY_OHLC_QUEUE, symbol + "_" + period,
                    "[" + Arrays.stream(queue).map(OhlcParam::toJsonString)
                            .collect(Collectors.joining(",")) + "]");
        } else {
            redis.delKeyString(FACTOR_FOREX_HISTORY_OHLC_QUEUE, symbol + "_" + period);
        }
    }
}
