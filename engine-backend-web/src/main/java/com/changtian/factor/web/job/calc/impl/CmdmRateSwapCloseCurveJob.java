//package com.nbcb.factor.web.job.calc.impl;
//
//import com.nbcb.factor.common.DateUtil;
//import com.nbcb.factor.web.entity.po.RateSwapClosingCurvePO;
//import com.nbcb.factor.web.job.calc.FactorCalcJob;
//import com.nbcb.factor.web.service.RateSwapClosingCurveService;
//import com.nbcb.factor.web.util.IdGeneratorUtils;
//import com.nbcb.factor.web.util.StringUtils;
//import com.nbcb.factor.web.util.XmlUtils;
//import com.xxl.job.core.biz.model.ReturnT;
//import com.xxl.job.core.log.XxlJobLogger;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.lang.Nullable;
//import org.springframework.stereotype.Component;
//import org.springframework.util.CollectionUtils;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * esb被动接口查询利率互换定盘曲线
// *
// * @author k11866
// * @date 2024/8/22 9:01
// */
//@Slf4j
//@Component
//public class CmdmRateSwapCloseCurveJob implements FactorCalcJob {
//
//    /**
//     * 成功代码
//     */
//    public static final String SUCCESS_CODE = "000000";
//    /**
//     * 失败代码
//     */
//    public static final String FAIL_CODE = "FFFFFF";
//    public static final String CMDM = "CMDM";
//
//    /**
//     * yaml属性
//     */
//    @Autowired
//    private EsbTcpProperties esbTcpProperties;
//
//    @Autowired
//    private CurveProperties curveProperties;
//
//    @Autowired
//    private RateSwapClosingCurvePOAssembler assembler;
//
//    @Autowired
//    private RateSwapClosingCurveService ratesSwapClosingCurveService;
//
//    /**
//     * 利率互换处理调用接口
//     *
//     * @param param
//     * @return
//     */
//    @Override
//    public ReturnT executeJob(@Nullable String param) {
//        CmdmRequest request = buildRequest(param);
//        XxlJobLogger.log("文件传输请求参数：{}", MFTUtil.stringToXml(request.toXmlString()));
//        try {
//            Ret ret = MfUtil.transformFileWithSocketReq(esbTcpProperties.getIp(),
//                    Integer.parseInt(esbTcpProperties.getPort()), request);
//            if (!SUCCESS_CODE.equals(ret.getReturnCode())) {
//                // 请求调用失败
//                XxlJobLogger.log("esb请求调用失败，返回报文为：{}", ret.toString());
//                return ReturnT.FAIL;
//            }
//
//            // 第一次请求成功，解析xml报文
//            String body = ret.getBody();
//            BsInfArraysEntity readEntity = XmlUtils.convertXmlToObject(body, BsInfArraysEntity.class);
//            assert readEntity != null;
//
//            // 转换并过滤数据
//            List<CmdsEsbDataInfArray> cmdmEsbDataInfArrays = mapFilter(readEntity);
//
//            // 查询数据库中该天数据
//            int num = ratesSwapClosingCurveService.selectNumByDate(request.getQuryDt());
//
//            // 该天数据条数都为空或者请求数据条数与数据库条数都不一致则该天不更新数据库
//            if (num == 0 || num != cmdmEsbDataInfArrays.size()) {
//                // 数据库无该天数据
//                sink(cmdmEsbDataInfArrays);
//            } else {
//                XxlJobLogger.log("该日期：{}请求数据量与数据库查询数据量数据大小一致，{}，不进行数据库更新保存",
//                        request.getQuryDt(), num);
//            }
//            return ReturnT.SUCCESS;
//        } catch (Exception e) {
//            XxlJobLogger.log("利率互换定盘曲线保存失败，原因为：{}", e);
//            return ReturnT.FAIL;
//        }
//    }
//
//    /**
//     * 请求参数转化
//     */
//    protected CmdmRequest buildRequest(String param) {
//        // 校验日期格式
//        CmdmRequest request = new CmdmRequest();
//        request.setTranCode(esbTcpProperties.getTranCode());
//        request.setConsumerId(esbTcpProperties.getConsumerId());
//        String consumerSeqNo = IdGeneratorUtils.generatorId().toString();
//        request.setConsumerSeqNo(consumerSeqNo);
//        String[] dayStr = DateUtil.getDaysStr().split(" ");
//        request.setTranDate(dayStr[0]);
//        request.setTranTime(dayStr[1]);
//        request.setServiceCode(esbTcpProperties.getServiceCode());
//        request.setServiceScene(esbTcpProperties.getServiceScene());
//        request.setDataAp(esbTcpProperties.getDataAp());
//        request.setVariety(curveProperties.getAllMapKey());
//        // 默认为当天时间
//        request.setQuryDt(StringUtils.isEmpty(param) ? DateUtil.getDayStr() : param);
//        request.setDataSrc(CMDM);
//        return request;
//    }
//
//    /**
//     * 解析过滤数据
//     *
//     * @return 解析后数据
//     */
//    protected List<CmdsEsbDataInfArray> mapFilter(BsInfArraysEntity readEntity) {
//        // 过滤查询结果中该天为空的数据，且请求数据与数据库数据一致则不更新数据库
//        List<CmdsEsbDataInfArray> cmdmEsbDataInfArrays = readEntity.getBsnInf().stream()
//                .filter(e -> !e.getRetCd().equals(FAIL_CODE))
//                .flatMap(e -> e.getArray().getDataInfArray().stream()).filter(e ->
//                        curveProperties.containsKey(e.getCdId())
//                                && e.getName().contains("收盘")
//                                && e.getName().endsWith("(均值)"))
//                .collect(Collectors.collectingAndThen(
//                        Collectors.toCollection(() -> new TreeSet<>(
//                                Comparator.comparing(e -> e.getCdId() + ',' + e.getTerm() + ',' + e.getUdtDt())
//                        )), ArrayList::new));
//
//        XxlJobLogger.log("请求查询过滤后数据量为：{}", cmdmEsbDataInfArrays.size());
//        if (CollectionUtils.isEmpty(cmdmEsbDataInfArrays)) {
//            XxlJobLogger.log("过滤请求查询数据量为空");
//            return Collections.emptyList();
//        }
//        // 过滤出请求无数据的类型
//        String failType = readEntity.getBsnInf().stream().filter(e -> e.getRetCd().equals(FAIL_CODE))
//                .map(CmdsEsbDataInf::getVariety).collect(Collectors.joining(","));
//        if (StringUtils.isNotEmpty(failType)) {
//            XxlJobLogger.log("利率互换定盘曲线中类型，{}查询为空", failType);
//        }
//        return cmdmEsbDataInfArrays;
//    }
//
//    /**
//     * 数据格式转化并落库
//     */
//    public void sink(List<CmdsEsbDataInfArray> cmdmEsbDataInfArrays) {
//        // 数据库格式化
//        // 数据歌华抓换存库
//        List<RateSwapClosingCurvePO> curveDTOS = cmdmEsbDataInfArrays.stream().map(e -> {
//            //修改期限term为Y-360 /M-30 /W-7 /D-1
//            String term = termTra(e.getTerm());
//            RateSwapClosingCurvePO curveDTO = new RateSwapClosingCurvePO();
//            curveDTO.setCurveId(e.getCdId());
//            curveDTO.setSymbol(e.getName());
//            curveDTO.setTerm(term);
//            curveDTO.setRate(e.getValue());
//            curveDTO.setTradeDt(e.getTrdDt());
//            return curveDTO;
//        }).collect(Collectors.toList());
//
//        List<RateSwapClosingCurvePO> poList = assembler.toPos(curveDTOS);
//        // 数据完善
//        for (RateSwapClosingCurvePO e : poList) {
//            curveProperties.CurveDtO curveDtO = curveProperties.getAllMap().get(e.getCdId());
//            e.setCurveName(curveDtO.getCurveName());
//            e.setSymbol(curveDtO.getSymbol());
//            // securityId=getCurveName+term
//            e.setSecurityId(String.format("%s_%s", e.getCurveName(), e.getTerm()));
//            // id = securityId +dt
//            e.setId(e.getSecurityId() + e.getTradeDt());
//            //请求返回利率单位为%
//            e.setRate(e.getRate());
//            e.setCreateTime(new Date());
//        }
//        // 删除数据库中该日期的旧数据并插入新数据
//        ratesSwapClosingCurveService.replaceAll(poList);
//    }
//
//    /**
//     * 天数转String
//     *
//     * @param term
//     * @return
//     */
//    public String termTra(String term) {
//        Integer integer = Integer.parseInt(term);
//        if (integer % 360 == 0) {
//            int num = integer / 360;
//            return num + "Y";
//        }
//        if (integer % 30 == 0) {
//            int num = integer / 30;
//            return num + "M";
//        }
//        if (integer % 7 == 0) {
//            int num = integer / 7;
//            return num + "W";
//        }
//        return term + "D";
//    }
//}