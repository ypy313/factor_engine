package com.nbcb.factor.filter;

import cn.hutool.core.util.StrUtil;
import com.nbcb.factor.event.cdh.BondBestOffer;
import com.nbcb.factor.event.cdh.BondBestOfferDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * cdh最优报价过滤
 */
@Slf4j
public class BondBestOfferFilter {
    private static final String regexps = ".明\\s*天\\s*\\+\\s*[1-9].*|.*今\\s*天\\s*\\+\\s*[02-9]{1,}.*|.*后\\s*天\\s*\\+\\s*[0-9].*;"
            + ".*星(\\s*|\\t*)期.*|.*周.*;.*[tT](\\s*)\\+[02-9]{1,}.*|.*[^天]\\s*\\+\\s*[02-9]{1,}.*|.*\\(\\D*^\\+\\s*[02-9]{1,1}\\D*\\).*;" +
            ".*[号年月日]+.*|.*[0-9]+\\s*\\.\\s*[0-9]+.|.*[0-9]+\\s*/\\s*[0-9]+.*;.*行\\s*权.*|.*远\\s*期.*|.*现\\s*券.*|.*全\\s*价.*|" +
            ".*含\\s*权.*|.*净\\s*价.*|.*换\\s*券.*|.*到\\s*期.*|";

    private static final String REGEX_COMMENT_SPLIT = "\\+(?![^()]*+\\))";
    private static final String REGEX_EXTRACT_QUANTITY = "([0-9]+)(?![^()]*+\\))";
    private static List<String> regexpsList;

    public BondBestOfferFilter() {
        regexpsList = StrUtil.split(regexps, ';');
    }

    public static void init() {
        if (regexpsList == null || regexpsList.isEmpty()) {
            regexpsList = StrUtil.split(regexps, ';');
        }
    }

    /**
     * 处理bidcomment
     */
    public static boolean bidFilter(BondBestOffer bondBestOffer) {
        BondBestOfferDetail bondBestOfferDetail = bondBestOffer.getText();
        //数据状态
        String bidQuoteStatus = bondBestOfferDetail.getBid_ss_detect();
        BigDecimal bidNetPrice = bondBestOfferDetail.getBidNetPrice();
        BigDecimal bidYield = bondBestOfferDetail.getBidYield();
        String bidSize = bondBestOffer.getBidSize();
        if (bidNetPrice == null || bidYield == null) {
            //价格为空 跳出
            //setBidWithNull(bondBestOfferDetail)
            return false;
        }
        String bidComment = bondBestOfferDetail.getBidComment();
        if (StringUtils.isBlank(bidComment)) {
            //如果comment为空 跳过过滤
            return false;
        }
        StringBuilder bidCommentBuilder = new StringBuilder();
        String[] bidCommentList = bidComment.split(REGEX_COMMENT_SPLIT);
        long bidQty = 0L;
        //如果bidcommon为空，直接取134域
        if (StringUtils.isEmpty(bidComment)) {
            bidQty = Long.parseLong(bidSize);
            if (bidQty % 1000 != 0) {
                //量不是1000的整数倍 过滤
                setBidWithNull(bondBestOfferDetail);
                log.info("bid quantity is not 1000 {} {} {}"
                        , bondBestOfferDetail.getBondCode(), bidSize, bondBestOffer.getScQuoteTime());
                return true;//被过滤
            }
            if (bidQty == 0) {
                //量为0 则默认2000
                bidQty = 2000;
            }
        } else {
            for (String comment : bidCommentList) {
                long qtyLong = extractQtyFromComment(comment);
                //非T+1或者量不满足1000的整数倍过滤
                if (matchRegex(comment) || qtyLong % 1000 != 0) {
                    continue;
                }
                bidQty += qtyLong;
                bidCommentBuilder.append(comment);
            }
            //量为0 默认为2000
            if (bidQty == 0) {
                bidQty = 2000;
                bondBestOffer.setBidSize(bidSize);
            }
            if (bidCommentBuilder.toString().equals("")) {
                //非T+1过滤
                setBidWithNull(bondBestOfferDetail);
                log.info("bid not t+1 {} {} ", bondBestOfferDetail.getBondCode(), bidCommentList
                        , bondBestOffer.getScQuoteTime());
                return true;
            }
        }
        if(!"1".equals(bidQuoteStatus)){
            //异常数据 过滤
            setBidWithNull(bondBestOfferDetail);
            log.info("bidQuoteStatus {} {} {}" ,bondBestOfferDetail.getBondCode()
                    ,bidQuoteStatus,bondBestOffer.getCreated());
            return true;
        }
        bondBestOffer.setBidSize(String.valueOf(bidQty));
        bondBestOfferDetail.setBidSize(String.valueOf(bidQty));
        String bidCommentStr = bidCommentBuilder.toString().replaceFirst("\\+", "");
        bondBestOfferDetail.setBidComment(bidCommentStr);
        return false;
    }

    /**
     * 置空bid价格和收益率 过滤
     */
    private static void setBidWithNull(BondBestOfferDetail bondBestOfferDetail) {
        bondBestOfferDetail.setBidNetPrice(null);
        bondBestOfferDetail.setBidYield(null);
    }

    /**
     * comment 非T+1过滤
     */
    private static boolean matchRegex(String comment){
        for (String regex : regexpsList){
            if (Pattern.matches(regex,comment)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从comment提取报价量
     */
    private static long extractQtyFromComment(String comment){
        Pattern pattern = Pattern.compile(REGEX_EXTRACT_QUANTITY);
        Matcher matcher = pattern.matcher(comment);
        if (matcher.find()) {
            String qty = matcher.group(0);
            try{
                return Long.parseLong(StringUtils.trim(qty));
            }catch (Exception e){
                return 0;
            }
        }else {
            return 0;
        }
    }

    /**
     * 处理ofrcomment
     */
    private static boolean ofrFilter(BondBestOffer bondBestOffer) {
        BondBestOfferDetail bondBestOfferDetail = bondBestOffer.getText();
        //数据状态
        String ofrQuoteStatus = bondBestOfferDetail.getOfr_ss_detect();
        BigDecimal ofrNetPrice = bondBestOfferDetail.getOfrNetPrice();
        BigDecimal ofrYield = bondBestOfferDetail.getOfrYield();
        String ofrSize = bondBestOffer.getOfferSize();
        if (ofrNetPrice==null || ofrYield == null) {
            //setOfrWithNull(bondBestOfferDetail)
            return false;//不过滤
        }
        String ofrComment = bondBestOfferDetail.getOfrComment();
        if (StringUtils.isBlank(ofrComment)) {
            //如果comment为空 跳过过滤
            return false;
        }
        StringBuilder ofrCommentBuilder = new StringBuilder();
        StringBuilder ofrSizeBuilder = new StringBuilder();
        String[] ofrCommentList = ofrComment.split(REGEX_COMMENT_SPLIT);
        long ofrQty = 0L;
        //如果bidcomment为空，直接取134域
        if (StringUtils.isEmpty(ofrComment)) {
            ofrQty = Long.parseLong(ofrSize);
            if (ofrQty%1000!=0) {
                //量不是1000的整数倍，过滤
                setOfrWithNull(bondBestOfferDetail);
                log.info("ofr quantity is 10000 {} {} {} ",bondBestOfferDetail.getBondCode()
                ,ofrSize,bondBestOffer.getScQuoteTime());
                return true;
            }
            if (ofrQty ==0) {
                //量为0 则默认2000
                ofrQty = 2000;
            }
        }else {
            for (String comment : ofrCommentList) {
                long qtyLong = extractQtyFromComment(comment);
                //非T+1或者量不满足1000的整数倍过滤
                if (matchRegex(comment)||qtyLong%1000!=0) {
                    continue;
                }
                ofrQty += qtyLong;
                ofrCommentBuilder.append("+").append(comment);
            }
            //量为0 默认2000
            if (ofrQty ==0) {
                ofrQty = 2000;
                bondBestOffer.setBidSize(ofrSize);
            }
            if (ofrCommentBuilder.toString().equals("")) {
                //非T+1过滤
                setOfrWithNull(bondBestOfferDetail);
                log.info("ofr not t+1 {} {} {}",bondBestOfferDetail.getBondCode()
                ,ofrCommentList,bondBestOffer.getScQuoteTime());
                return true;
            }
        }
        if(!"1".equals(ofrQuoteStatus)){
            //异常数据 过滤
            setBidWithNull(bondBestOfferDetail);
            log.info("ofrQuoteStatus {} {} {}" ,bondBestOfferDetail.getBondCode()
            ,ofrQuoteStatus,bondBestOffer.getCreated());
            return true;
        }
        bondBestOffer.setBidSize(String.valueOf(ofrQty));
        bondBestOfferDetail.setBidSize(String.valueOf(ofrQty));
        String ofrCommentStr = ofrCommentBuilder.toString().replaceFirst("\\+", "");
        bondBestOfferDetail.setOfrComment(ofrCommentStr);
        return false;
    }

    /**
     * 置空ofr价格和收益率 过滤
     */
    private static void setOfrWithNull(BondBestOfferDetail bondBestOfferDetail) {
        bondBestOfferDetail.setOfrNetPrice(null);
        bondBestOfferDetail.setOfrYield(null);
    }
}
