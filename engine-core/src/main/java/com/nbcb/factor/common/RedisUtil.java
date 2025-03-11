package com.nbcb.factor.common;

import com.nbcb.factor.strategy.StrategyInstanceCalculate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.*;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class RedisUtil {
    private volatile JedisCluster jedis;
    private volatile Thread thread;

    private RedisUtil() {
        try {
            initializeRedis();
        } catch (Exception e) {
            jedis = null;
            log.info("start thread listening");
            startThread();
            log.info("finished thread listening");
        }
    }

    private void startThread() {
        try {
            // todo ?????
            if (null != null) {
                thread.interrupt();
            }
        } catch (Exception e) {
            log.warn("Failed to interrupt thread:" + thread, e);
        }
        thread = new Thread(() -> {
            while (jedis == null) {
                try {
                    log.info("try connect to redis cluster");
                    initializeRedis();
                } catch (Exception ex) {
                    jedis = null;
                    log.error("redis connection failed");
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        log.warn("thread interrupted while sleeping", ex);
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void initializeRedis() throws Exception {
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxWait(Duration.ofMillis(-1));
            config.setMaxTotal(1000);
            config.setMinIdle(8);
            config.setMaxIdle(100);

            //添加集群的服务节点set 集合

            String redisTimeOut = LoadConfig.getProp().getProperty("jedis.timeout");
            String redisSoTimeOut = LoadConfig.getProp().getProperty("jedis.sotimeout");
            String redisMaxRedirections = LoadConfig.getProp().getProperty("jedis.maxRedirections");
            String redisPassword = LoadConfig.getProp().getProperty("jedis.auth");
            redisPassword = EncipherUtil.AESDecode(redisPassword);//AES 解密
            String hosts = LoadConfig.getProp().getProperty("jedis.host");
            log.info("redis host {}", hosts);
            String[] hostArray = hosts.split(",");

            Set<HostAndPort> hostAndPortSet = new HashSet<>();
            String[] nodes;
            for (String hostStr : hostArray) {
                nodes = hostStr.split(":");
                hostAndPortSet.add(new HostAndPort(nodes[0], Integer.parseInt(nodes[1])));

            }
            jedis = new JedisCluster(hostAndPortSet, Integer.parseInt(redisTimeOut), Integer.parseInt(redisSoTimeOut),
                    Integer.parseInt(redisMaxRedirections), redisPassword, config);
            jedis.set("check", "1");
            log.info("redis connection start success");

        } catch (Exception ex) {
            log.error("init connection redis error !", ex);
            jedis = null;
            throw ex;
        }
    }

    private static final class InstanceHolder {
        static final RedisUtil instance = new RedisUtil();
    }

    /**
     * 获取redis 工具类实例
     */
    public static RedisUtil getInstance(){
        return InstanceHolder.instance;
    }

    /**
     * 根据策略实例id，获取实例的计算缓存结果
     *
     */
    public String getStraInsCalc(String instanceId) {
        if (jedis == null) {
            return "-1";
        }
        String key = instanceId;
        String object = AllRedisConstants.FACTOR_STRA_INS_CALC;
        try {
            return jedis.hget(object, key);
        } catch (Exception e) {
            jedis = null;
            startThread();
            log.error("redis getStraInsCalc occur error", e);
            return "-1";
        }
    }
    /**
     * 根据策略实例id，存入缓存结果
     *
     * @param instanceId
     * @return
     */
    public void setStraInsCalc(String instanceId, StrategyInstanceCalculate strategyInstanceCalculate) {
        if (jedis != null) {
            String key = instanceId;
            String object = AllRedisConstants.FACTOR_STRA_INS_CALC;
            try {
                jedis.hset(object, key, JsonUtil.toJson(strategyInstanceCalculate));
            } catch (Exception e) {
                jedis = null;
                startThread();
                log.error("redis setStraInsCalc occur error", e);
            }
        }
    }

    /**
     * 根据策略实例id,存入实例的昨日值，优化左边数查询速度
     * @param instanceId
     * @param lastdayValue
     */
    public void setLastdayValue(String instanceId,String lastdayValue) {
        if (jedis !=null) {
            String key = instanceId;
            String object = AllRedisConstants.FACTOR_STRA_INS_CALC;
            try {
                jedis.hset(object,key,lastdayValue);
            } catch (Exception e) {
                jedis = null;
                startThread();
                log.error("redis setLastdayValue occur error", e);
            }
        }
    }

    /**
     * 获取中债价差缓存
     *
     */
    public String getChinaBondSpreadCache(String leg1, String leg2, Long period) {
        String key = String.format("leg1_%s_leg2_%s_period_%s", leg1, leg2, period);
        if (jedis == null) {
            return "-1";
        }
        String object = AllRedisConstants.FACTOR_SPREAD_CBOND_PERCENT;
        try {
            return jedis.hget(object, key);
        } catch (Exception e) {
            jedis = null;
            startThread();
            log.error("redis FACTOR_CCB_PERCENTILE occur error", e);
            return "-1";
        }
    }

    /**
     * 存入ChinaBondSpreadCache
     *
     */
    public void setChinaBondSpreadCache(String leg1, String leg2, Long period, String chinaBondSpreadJson) {
        if (jedis != null) {
            String key = String.format("leg1_%s_leg2_%s_period_%s", leg1, leg2, period);
            String object = AllRedisConstants.FACTOR_SPREAD_CBOND_PERCENT;
            try {
                jedis.hset(object, key, chinaBondSpreadJson);
            } catch (Exception e) {
                jedis = null;
                startThread();
                log.error("redis setChinaBondSpreadCache occur error", e);
            }
        }
    }

    /**
     * 获取波动率和平均值缓存
     *
     */
    public String getChinaBondVolatilityCache(String leg1, String leg2, Long period) {
        String key = String.format("leg1_%s_leg2_%s_period_%s", leg1, leg2, period);
        if (jedis == null) {
            return "-1";
        }
        String object = AllRedisConstants.FACTOR_SPREAD_CBOND_VOLATILITY;
        try {
            return jedis.hget(object, key);
        } catch (Exception e) {
            jedis = null;
            startThread();
            log.error("redis FACTOR_CCB_VOLATILITY occur error", e);
            return "-1";
        }
    }

    /**
     * 存入ChinaBondVolatilityCache
     *
     */
    public void setChinaBondVolatilityCache(String leg1, String leg2, Long period, String chinaBondSpreadJson) {
        if (jedis != null) {
            String key = String.format("leg1_%s_leg2_%s_period_%s", leg1, leg2, period);
            String object = AllRedisConstants.FACTOR_SPREAD_CBOND_VOLATILITY;
            try {
                jedis.hset(object, key, chinaBondSpreadJson);
            } catch (Exception e) {
                jedis = null;
                startThread();
                log.error("redis setChinaBondVolatilityCache occur error", e);
            }
        }
    }

    /**
     * 缓存map
     */
    public void setCacheMap(final String key, final Map<String,String> dataMap){
        if (dataMap !=null) {
            jedis.hmset(key,dataMap);
        }
    }

    /**
     * 存入redis 中
     *
     */
    public void setString(String object, String key, String value) {
        if (jedis != null) {
            try {
                jedis.hset(object, key, value);
            } catch (Exception e) {
                jedis = null;
                startThread();
                log.error("redis setChinaBondSpreadCache occur error", e);
            }
        }
    }

    /**
     * 从redis 中取值
     *
     */
    public String getString(String object, String key) {
        if (jedis != null) {
            try {
                return jedis.hget(object, key);
            } catch (Exception e) {
                jedis = null;
                startThread();
                log.error("redis getString occur error", e);
            }
        }
        return "-1";
    }

    /**
     * 从redis 中取值
     *
     * @param object
     */
    public Map<String, String> getString(String object) {
        if (jedis != null) {
            try {
                return jedis.hgetAll(object);
            } catch (Exception e) {
                jedis = null;
                startThread();
                log.error("redis getString occur error", e);
            }
        }
        return null;
    }

    /**
     * 从redis删除object （redis中对应key)
     *
     */
    public Long delString(String object) {
        if (jedis != null) {
            try {
                return jedis.del(object);
            } catch (Exception e) {
                jedis = null;
                startThread();
                log.error("redis delString occur error", e);
            }
        }
        return -1L;
    }

    /**
     * 从redis删除object filed （redis中对应key filed)
     *
     */
    public Long delKeyString(String object, String key) {
        if (jedis != null) {
            try {
                return jedis.hdel(object, key);
            } catch (Exception e) {
                jedis = null;
                startThread();
                log.error("redis delKeyString occur error", e);
            }
        }
        return -1L;
    }

    /**
     * key 是否存在
     *
     */
    public boolean keyExists(String object, String key) {
        if (jedis != null) {
            try {
                return jedis.hexists(object, key);
            } catch (Exception e) {
                jedis = null;
                startThread();
                log.error("redis keyExists occur error", e);
            }
        }
        return false;
    }

    public boolean keyInfoExists(String key) {
        if (jedis != null) {
            try{
                return jedis.exists(key);
            }catch (Exception e){
                jedis = null;
                startThread();
                log.error("redis keyInfoExists occur error", e);
            }
        }
        return false;
    }

    /**
     * 存入因子实例 web 请求失败次数
     */
    public void setWebFailure(String leg1, String leg2, String mapJson) {
        if (jedis !=null) {
            String key = String.format("leg1_%s_leg2_%s", leg1, leg2);
            String object = RedisConstant.FACTOR_WEB_FAILURE;
            try {
                jedis.hset(object, key, mapJson);
            }catch (Exception e){
                jedis = null;
                startThread();
                log.error("redis setWebFailure occur error", e);
            }
        }
    }

    /**
     * 存入因子实例web请求失败次数
     */
    public String getWebFailure(String leg1, String leg2) {
        if (jedis !=null) {
            String key = String.format("leg1_%s_leg2_%s", leg1, leg2);
            String object = RedisConstant.FACTOR_WEB_FAILURE;
            try {
                return jedis.hget(object, key);
            }catch (Exception e){
                jedis = null;
                startThread();
                log.error("redis setWebFailure occur error", e);
            }
        }
        return "-1";
    }
    /**
     * 存入redis中
     */
    public void setRiding(String object, String value) {
        if (jedis !=null) {
            try {
                jedis.set(object,value);
            }catch (Exception e){
                jedis = null;
                startThread();
                log.error("redis setRiding occur error", e);
            }
        }
    }
    /**
     * 从redis中取值
     */
    public String getRiding(String object){
        if (jedis !=null) {
            try {
                return jedis.get(object);
            }catch (Exception e){
                jedis = null;
                startThread();
                log.error("redis setRiding occur error", e);
            }
        }
        return "-1";
    }

    /**
     * 删除redis以redisKeyStartWith开头的key
     */
    public void deleteRedisKeyStartWith(String redisKeyStartWith){
        try{
            Map<String, JedisPool> clusterNodes = jedis.getClusterNodes();
            for (Map.Entry<String, JedisPool> entry : clusterNodes.entrySet()) {
                Jedis jedisNode = entry.getValue().getResource();
                //判断非从节点（因为若主从复制，从节点会跟随主节点的变化而变化）
                if (!jedisNode.info("replication").contains("role:slave")) {
                    redisScan(jedisNode,redisKeyStartWith);
                }
            }
        }catch (Exception e){
            jedis = null;
            startThread();
            log.error("redis deleteRedisKeyStartWith occur error",e);
        }
    }
    /**
     * 通过sacn命令实现对redis数据的删除
     */
    private void redisScan(Jedis jedis,String redisKeyStartWith){
        //游标 初始为0
        String cursor= ScanParams.SCAN_POINTER_START;
        String key = redisKeyStartWith+"*";
        ScanParams scanParams = new ScanParams();
        scanParams.match(key);//以key值为前缀，匹配的redis里的key
        scanParams.count(1000);
        while (true){
            //使用scan命令获取数据，使用cursor游标记录位置，下次循环使用
            ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
            cursor = scanResult.getCursor();//返回0，说明遍历完成
            List<String> list = scanResult.getResult();
            for (String mapentry : list) {
                jedis.del(mapentry);
            }
            if (StringUtils.equals("0",cursor)) {
                break;
            }
        }
    }
}
