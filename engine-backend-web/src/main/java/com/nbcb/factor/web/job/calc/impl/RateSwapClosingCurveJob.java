package com.nbcb.factor.web.job.calc.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nbcb.factor.common.JsonUtil;
import com.nbcb.factor.web.entity.po.RateSwapClosingCurvePO;
import com.nbcb.factor.web.job.calc.FactorCalcJob;
import com.nbcb.factor.web.service.RateSwapClosingCurveService;
import com.nbcb.factor.web.util.ExceptionUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.TreeSet;
import java.util.stream.Collectors;
import static org.springframework.util.StringUtils.hasText;
/**
 * 获取获取cvs文件并存储
 *
 * @author k12222
 */
@Component
public class RateSwapClosingCurveJob extends AbstractDownloadFileAndSinkTemplate<List<RateSwapClosingCurveDTO>> implements FactorCalcJob {

    @Autowired
    private RateSwapClosingCurvePOAssembler assembler;
    @Autowired
    private CurveProperties curveProperties;
    @Autowired
    private RateSwapClosingCurveService rateSwapClosingCurveService;

    /**
     * 执行job
     *
     * @param param string
     * @return 结果
     */
    @Override
    public ReturnT executeJob(String param) {
        try {
            Object paramObject = parseParam(param);
            List<FileRequestLeaderModelDTO> reqList = buildRequest(paramObject);
            XxlJobLogger.log("本次执行请求{}个", reqList.size());
            // 多日刷支持
            int i = 0;
            for (FileRequestLeaderModelDTO dto : reqList) {
                XxlJobLogger.log("----{}----", ++i);
                FileBusRequest req = (FileBusRequest) dto.getLeader();
                setDefault(req);
                deleteFileIfExist(req);
                boolean success = downloadFile(req);
                if (!success) {
                    success = getFollow(req, success, dto);
                }
                // 全失败就返回
                if (!success) {
                    return ReturnT.FAIL;
                }
                List<RateSwapClosingCurveDTO> data = map(req);
                sink(data, paramObject);
            }
        } catch (Exception e) {
            XxlJobLogger.log(e);
            return ReturnT.FAIL;
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 主获取文件失败，从备机获取
     */
    public boolean getFollow(FileBusRequest req, boolean success, FileRequestLeaderModelDTO dto) {
        XxlJobLogger.log("获取主{}文件失败", req.getConsumerSvrId());
        // 从
        for (FileBusRequest follower : dto.getFollowers()) {
            if (downloadFile(follower)) {
                success = true;
                req = follower;
                XxlJobLogger.log("备机{}获取文件成功", follower.getConsumerSvrId());
                break;
            }
        }
        return success;
    }

    /**
     * 解析job参数
     *
     * @param param job参数
     * @return job参数对象
     */
    @Override
    protected Object parseParam(@Nullable String param) {
        try {
            RateSwapClosingCurveJobParam obj = null;
            if (hasText(param)) {
                try {
                    obj = JsonUtil.toObject(param, RateSwapClosingCurveJobParam.class);
                } catch (JsonProcessingException e) {
                    XxlJobLogger.log("json参数解析失败\n{}", ExceptionUtils.toString(e));
                }
            }
            if (obj == null) {
                obj = new RateSwapClosingCurveJobParam();
            }

            boolean hasStart = hasText(obj.getStart());
            boolean hasEnd = hasText(obj.getEnd());
            if (!hasStart && !hasEnd) {
                obj.setStart(getDaystr());
                hasStart = true;
            }
            // 只输入了一个日期的补全 01 10
            if (!hasStart) {
                obj.setStart(obj.getEnd());
            } else if (!hasEnd) {
                obj.setEnd(obj.getStart());
            }
            if (!hasText(obj.getSourceIp())) {
                obj.setSourceIp(properties.getSourceIp());
            }
            SimpleDateFormat sdf = new SimpleDateFormat(PURE_DATE_PATTERN);
            try {
                sdf.parse(obj.getStart());
                sdf.parse(obj.getEnd());
            } catch (ParseException e) {
                XxlJobLogger.log("日期格式应为: " + PURE_DATE_PATTERN);
                throw new IllegalArgumentException(e);
            }

            Assert.hasLength(obj.getSourceIp(),"源端IP不能为空");
            XxlJobLogger.log("执行参数:{}", obj);
            return obj;
        } catch (Exception e) {
            XxlJobLogger.log("跑批失败，原因为：{}", ExceptionUtils.toString(e));
            return ReturnT.FAIL;
        }
    }

    /**
     * 定义请求对象
     *
     * @param param {@link #parseParam}
     * @return 请求对象
     */
    protected List<FileRequestLeaderModelDTO> buildRequest(Object param) {
        RateSwapClosingCurveJobParam curveJobParam = (RateSwapClosingCurveJobParam) param;
        Date startDate = DateUtil.parse(curveJobParam.getStart(), PURE_DATE_PATTERN);
        Date endDate = DateUtil.parse(curveJobParam.getEnd(), PURE_DATE_PATTERN);
        List<FileRequestLeaderModelDTO> list = new ArrayList<>();
        final SimpleDateFormat sdf = new SimpleDateFormat(PURE_DATE_PATTERN);
        String fileName = "rateswapcurve_factor.csv";
        String[] ips = curveJobParam.getSourceIp().split(",");
        for (Date date = startDate; date.compareTo(endDate) <= 0; date = DateUtil.offsetDay(date, offset:1)){
            String dateStr = sdf.format(date);
            FileBusRequest req = new FileBusRequest();
            req.setConsumerSvrId(ips[0]); // 设置源端IP
            req.setSrcFilePath(Paths.get(properties.getSourcePath(), dateStr, fileName).toString().replace(
            "\\","/")); // 设置源端路径
            req.setDestSvrId(properties.getDestIp()); // 设置目的端IP
            req.setDestFilePath(Paths.get(properties.getDestPath(), dateStr, fileName).toString().replace(
            "\\","/")); // 设置目的端路径
            req.setCrtFlg("1");
            list.add(new FileRequestLeaderModelDTO(req, ips));
        }
        return list;
    }

    /**
     * 解析文件数据
     *
     * @param req {@link #buildRequest}
     * @return 数据 nonNull
     */
    @Override
    protected List<RateSwapClosingCurveDTO> map(Request req) {
        FileBusRequest fileBusRequest = (FileBusRequest) req;
        String destFilePath = fileBusRequest.getDestFilePath();
        CsvReader reader = CsvUtil.getReader();
        try (BufferedReader bufferedReader = ResourceUtil.getReader(destFilePath, properties.getCharset())) {
            BeanRowHandler<RateSwapClosingCurveDTO> handler = new BeanRowHandler<>(new RateSwapClosingCurveDTO());
            reader.read(bufferedReader, handler);
            return handler.getList();
        } catch (Exception ex) {
            XxlJobLogger.log(ex);
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * 数据落库
     *
     * @param data  解析后的数据
     * @param param job参数
     */
    @Override
    public void sink(List<RateSwapClosingCurveDTO> data, Object param) {
        Assert.notNull(data,"利率互换曲线收盘数据集不能为空");
        // 过滤不要的数据和去重 CurveId + Term + TradeDt
        if (data.isEmpty()) {
            XxlJobLogger.log("没有需要保存的利率互换曲线收盘数据");
            return;
        }
        data = data.stream().filter(e -> e.getSymbol().contains("收盘")
                        && e.getSymbol().endsWith("(均值)")
                        && curveProperties.containsKey(e.getCurveId()))
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(
                                Comparator.comparing(e -> e.getCurveId() + ',' + e.getTerm() + ',' + e.getTradeDt())
                        )),
                        ArrayList::new
                ));
        if (data.isEmpty()) {
            XxlJobLogger.log("过滤后的收盘数据为空,可能.csv编码不为{}", properties.getCharset());
            return;
        }
        List<String> tradeDtList = data.stream().map(RateSwapClosingCurveDTO::getTradeDt).distinct().collect(Collectors.toList());
        Assert.isTrue(tradeDtList.size() == 1,"利率互换曲线收盘数据集里有多个日期");

        List<RateSwapClosingCurvePO> poList = assembler.toPos(data);
        // 数据完善
        for (RateSwapClosingCurvePO e : poList) {
            CurveProperties.CurveDTO curveDTO = curveProperties.getCurveDTO(e.getCurveId());
            e.setCurveName(curveDTO.getCurveName());
            e.setSymbol(curveDTO.getSymbol());
            // securityId = getCurveName + term
            e.setSecurityId(String.format("%s_%s", e.getCurveName(), e.getTerm()));
            // id = securityId + dt
            e.setId(e.getSecurityId() + e.getTradeDt());
            // 百分比 * 100
            e.setRate(e.getRate().multiply(BigDecimal.valueOf(100)));
            e.setCreateTime(new Date());
        }
        rateSwapClosingCurveService.replaceAll(poList);
    }
}