package com.changtian.factor.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 矩阵相关工具类
 */
@Slf4j
public class MatrixUtil {
    public static RealMatrix concatenateMatrix(List<double[][]> matrices){
        if (CollectionUtils.isEmpty(matrices)) {
            log.warn("矩阵拼接时矩阵集合为空");
            return null;
        }
        //检查所有矩阵的行是否相等
        int row = matrices.get(0).length;
        for (double[][] matrix : matrices) {
            if (matrix.length != row) {
                log.warn("矩阵列拼接中，行不相等");
                return null;
            }
        }
        //计算结果矩阵中的总列数
        int totalCols = 0;
        for (double[][] matrix : matrices) {
            totalCols += matrix[0].length;
        }
        //创造一个新的二位数组来存储拼接后的矩阵数据
        double[][] newData = new double[row][totalCols];
        //迭代矩阵列表，并将其列复制到新的数组中
        int currentCol = 0;
        for (double[][] matrix : matrices) {
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    newData[i][currentCol+j] = matrix[i][j];
                }
            }
            currentCol +=matrix.length;
        }
        //二维数组创建新的矩阵对象
        return new Array2DRowRealMatrix(newData);
    }

    /**
     * 向量拼接
     * @param vectorList 向量数组集合
     */
    public static RealVector concatenateVector(List<double[]> vectorList){
        //所有数组中元素总和
        int sumLength = vectorList.stream().mapToInt(v -> v.length).sum();
        RealVector realVector = new ArrayRealVector(sumLength);
        //遍历初始向量列值
        int currentIndex = 0;
        for (double[] vectors : vectorList) {
            for (double vector : vectors) {
                realVector.setEntry(currentIndex++, vector);
            }
        }
        return realVector;
    }

    /**
     * 计算平局值向量（矩阵）
     * @param matrix 矩阵
     * @return 向量
     */
    public static RealVector calculateColumnAverage(RealMatrix matrix){
        int row = matrix.getRowDimension();
        int cols = matrix.getColumnDimension();
        double[] averages = new double[cols];
        for (int i = 0; i < cols; i++) {
            double sum = 0;
            for (int j = 0; j < row; j++) {
                sum += matrix.getEntry(j, i);
            }
            averages[i] = sum / row;
        }
        return new ArrayRealVector(averages);
    }

    /**
     * 初始化单位矩阵
     * @param rowOrColumn 行/列
     * @return 单位矩阵
     */
    public static RealMatrix initIdentityMatrix(int rowOrColumn){
        double[][] identityMatrix = new double[rowOrColumn][rowOrColumn];
        for (int i = 0; i < rowOrColumn; i++) {
            for (int j = 0; j < rowOrColumn; j++) {
                if (j==i) {
                    identityMatrix[i][j] = 1;
                }else {
                    identityMatrix[i][j] = 0;
                }
            }
        }
        return new Array2DRowRealMatrix(identityMatrix);
    }

    /**
     * 打印矩阵
     * @param matrix 矩阵对象
     */
    public static void printMatrix(RealMatrix matrix){
        StringBuffer matrixInfo = new StringBuffer();
        matrixInfo.append("[");
        for (int i = 0; i < matrix.getRowDimension(); i++) {
            matrixInfo.append("[");
            for (int j = 0; j < matrix.getColumnDimension(); j++) {
                matrixInfo.append(matrix.getEntry(i,j));
                if (j!=matrix.getColumnDimension()-1) {
                    matrixInfo.append(",");
                }
            }
            matrixInfo.append("}");
            if (i!=matrix.getRowDimension()-1) {
                matrixInfo.append(",");
            }
        }
        matrixInfo.append("]");
        log.info("矩阵：{}",matrixInfo);
    }

    /**
     * double中四舍五入
     * @param value double数据
     * @param scale 保留小位数
     */
    public static double round(double value,int scale){
        BigDecimal db = BigDecimal.valueOf(value);
        db = db.setScale(scale, RoundingMode.HALF_UP);
        return db.doubleValue();
    }

    /**
     * 矩阵向量打印
     * @param realVector 向量
     */
    public static void printVector(RealVector realVector){
        StringBuffer vector = new StringBuffer();
        for (int i = 0; i < realVector.getDimension(); i++) {
            vector.append(realVector.getEntry(i));
            if (i!=realVector.getDimension()-1) {
                vector.append(",");
            }
        }
        vector.append("]");
        log.info("矩阵向量为：{}",vector);
    }
}
