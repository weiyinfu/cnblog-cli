package cnblog.util;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 给定一个xsize行ysize列的矩阵，矩阵中的元素都是字符串，每个字符串长度固定
 * 现在将这个字符串矩阵，画到一个矩形框中，使矩形框面积最小
 * 要知道：一个非ascii码字符占据两个英文字符的位置
 * <p>
 * 这个问题有一个约束和一个目标：
 * 约束：
 * 1、每列列宽，每行行高一致
 * 2、有列数限制，即每行最多有maxCols列，行数没有限制
 * 3、每个单元格内可以换行
 * <p>
 * 目标：最小化矩形框的面积
 * <p>
 * 有一个误差：如果列宽为奇数个字符，则汉字可能放不开，只能放到下一行，
 * 这种情况就不考虑了，也就是说最后有可能单元格内少了几个字
 * <p>
 * 算法描述：首先每列宽度最小为2，进行填充，调整最高的那一列（如果最高列有多列，则同时调整）
 * 调整方式为展宽最高列（只展宽一个单位），这样必然造成最高列变矮一点。
 * 然后，重新找出最高列，继续对最高列进行展宽
 **/
public class CharTable {
static String space(int cnt) {
    char[] a = new char[cnt];
    Arrays.fill(a, ' ');
    return new String(a);
}

/**
 * 指定每列的宽度，格式化打印字符串
 * 如果colWidth为null，则使用maxCols参数作为最大列宽进行自动调整列宽
 * 如果colWidth不为null，则使用colWidth作为宽度
 *
 * @param matrix   字符串矩阵
 * @param colGap   列与列之间的间隔大小
 * @param colWidth 每列的宽度
 * @param maxCols  最终table的最大列数
 */
public static String tos(String[][] matrix, int colGap, int[] colWidth, final int maxCols) {
    int xsize = matrix.length, ysize = matrix[0].length;
    int[][] len = new int[xsize][ysize];//每个单元格的字符串长度

    //列和列之间的间距
    String gap = space(colGap);

    //求每个单元格内字符的长度
    for (int i = 0; i < len.length; i++) {
        for (int j = 0; j < len[i].length; j++) {
            int l = 0;
            for (char k : matrix[i][j].toCharArray()) {
                if (k < 128) {
                    l++;
                } else {
                    l += 2;
                }
            }
            len[i][j] = l;
        }
    }
    if (colWidth == null) {
        //求colwidth：每列的宽度，先用平均值来作为每列的宽度
        colWidth = new int[ysize];//每个单元格的列宽
        for (int i = 0; i < ysize; i++) {
            int s = 0;
            for (int j = 0; j < xsize; j++) {
                s += len[j][i];
            }
            colWidth[i] = s / xsize;
        }
    }
    //先求出最大有多少行来，初始化每行的高度
    int[] rowHeight = new int[xsize];
    for (int i = 0; i < xsize; i++) {
        int h = 0;
        for (int j = 0; j < ysize; j++) {
            h = (int) Math.max(h, Math.ceil(len[i][j] * 1.0 / colWidth[j]));
        }
        rowHeight[i] = h;
    }
    int rows = Arrays.stream(rowHeight).sum();
    //对rowheight，colwidth进行累加，即的各个单元格的左上角坐标
    int[] left = new int[ysize];
    int[] top = new int[xsize];
    for (int i = 1; i < ysize; i++) {
        left[i] = left[i - 1] + colWidth[i - 1];
    }
    for (int i = 1; i < xsize; i++) {
        top[i] = top[i - 1] + rowHeight[i - 1];
    }
    //将字符串内容填充到table里面
    StringBuilder table[] = new StringBuilder[rows];
    for (int i = 0; i < rows; i++) {
        table[i] = new StringBuilder();
    }
    for (int i = 0; i < xsize; i++) {
        int t = top[i];
        for (int j = 0; j < ysize; j++) {
            String s = matrix[i][j];
            int k = 0;
            int x = 0, y = 0;
            int chinese = 0;
            while (k < s.length() && x < rowHeight[i]) {
                if (y + chinese >= colWidth[j]) {
                    table[t + x].append(gap);//在该单元格末尾添加间隔
                    chinese = 0;
                    x++;
                    y = 0;
                    //如果行数超了，直接跳出
                    if (x >= rowHeight[i]) {
                        break;
                    }
                }
                table[t + x].append(s.charAt(k));
                if (s.charAt(k) > 128) {
                    chinese++;
                }
                k++;
                y++;
            }
            if (x < rowHeight[i]) {
                //对于不足的地方，使用空格进行填充
                for (int z = 0; z < colWidth[j] - chinese - y; z++) {
                    table[t + x].append(' ');
                }
                //在该单元格末尾添加间隔
                table[t + x].append(gap);
                //在其余空白行添加空白
                for (x = x + 1; x < rowHeight[i]; x++) {
                    table[t + x].append(space(colWidth[j] + colGap));
                }
            }
        }
    }

    //将table转化为字符串
    return Arrays.stream(table).map(StringBuilder::toString).collect(Collectors.joining("\n"));
}

public static void main(String[] args) {
    String[][] a = new String[3][3];
    String s = "ha天34下大343势为344我5所控ha";
    Random random = new Random(0);
    for (int i = 0; i < a.length; i++) {
        for (int j = 0; j < a[i].length; j++) {
            a[i][j] = s.substring(0, random.nextInt(s.length() - 3) + 1);
            System.out.print(a[i][j] + " ");
        }
        System.out.println();
    }
    System.out.println("=========");
    System.out.println(tos(a, 5, null, 0xffffff));
}
}