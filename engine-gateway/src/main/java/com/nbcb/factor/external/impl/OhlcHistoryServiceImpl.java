package com.nbcb.factor.external.impl;

import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nbcb.factor.common.*;
import com.nbcb.factor.external.IOhlcHistoryService;
import com.nbcb.factor.external.dto.FactorFxOhlcHis;
import com.nbcb.factor.external.response.MyResponse;
import com.nbcb.factor.flink.mtime.ReuterDateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class OhlcHistoryServiceImpl implements IOhlcHistoryService {
    private final TypeReference<MyResponse<List<FactorFxOhlcHis>>> typeReference =
            new TypeReference<MyResponse<List<FactorFxOhlcHis>>>(){};
    @Override
    public List<FactorFxOhlcHis> listBy(String symbol, String period, int count, String sourceType) {
        String url = LoadConfig.getProp().getProperty(FxLoadConfig.OHLC_HISTORY_URL);
        StringBuilder symbolType = new StringBuilder();
        symbolType.append(symbol).append(StringUtils.isEmpty(sourceType)?"":sourceType);
        String param = String.format("?symbol=%s&period=%s&count=%d",symbolType,period,count);
        try{
            log.info("call history api : {}",param);
            String str = HttpUtil.get(url+param);
            MyResponse<List<FactorFxOhlcHis>> response = JsonUtil.toObject(str,typeReference);
            if(!Constants.HTTP_SUCC.equals(response.getStatus())){
                log.error("The web server return fail :{},Initializing ohlc history failed.",str);
                return Collections.emptyList();
            }
            List<FactorFxOhlcHis> list = response.getData();
            //日期格式转换
            DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (FactorFxOhlcHis ohlc : list) {
                ohlc.setBeginTime(ReuterDateTimeUtils.format(DateUtil.parse(ohlc.getBeginTime(),pattern)));
                ohlc.setEndTime(ReuterDateTimeUtils.format(DateUtil.parse(ohlc.getEndTime(),pattern)));
            }
            //asc
            list.sort(Comparator.comparing(FactorFxOhlcHis::getBeginTime));
            return list;
        }catch (Exception e){
            log.error("获取历史ohlc失败",e);
            return Collections.emptyList();
        }
    }
}
