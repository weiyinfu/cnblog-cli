package cnblog.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Util {

static HttpClient client = HttpClients.createDefault();


/**
 * 暂停程序，用于调试
 */
public static void pause() {
    try {
        System.in.read();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

/**
 * 正则表达式在字符串中搜索
 */
public static String search(String pattern, int groupCount, String s) {
    Pattern p = Pattern.compile(pattern);
    Matcher matcher = p.matcher(s);
    matcher.find();
    return matcher.group(groupCount);
}

/**
 * 判断正则表达式和字符串是否匹配
 */
public static boolean match(String pattern, String s) {
    Pattern p = Pattern.compile(pattern);
    Matcher matcher = p.matcher(s);
    return matcher.matches();
}

/**
 * 将字符串写入到文件
 */
public static void writeAndClose(Path path, String s) {
    try {
        path = path.toAbsolutePath();
        if (Files.notExists(path.getParent())) {
            Files.createDirectory(path.getParent());
        }
        BufferedWriter cout = Files.newBufferedWriter(path, Charset.forName("utf8"), StandardOpenOption.CREATE);
        cout.write(s);
        cout.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

/**
 * 获取家目录下的某个文件，默认家目录为~/cnblog目录
 */
public static Path home(String path) {
    Path p = Paths.get(System.getProperty("user.home")).resolve("cnblog");
    if (Files.notExists(p)) {
        try {
            Files.createDirectory(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    return p.resolve(path);
}

/**
 * 加载文件
 */
public static String loads(Path path) {
    try {
        return Files.readAllLines(path, Charset.forName("utf8")).stream().collect(Collectors.joining("\n"));
    } catch (IOException e) {
        e.printStackTrace();
    }
    return null;
}

/**
 * 加载类路径下的文件
 */
public static String loads(String classpath) {
    Scanner cin = new Scanner(Util.class.getResourceAsStream(classpath), "utf8");
    StringBuilder builder = new StringBuilder();
    while (cin.hasNext()) {
        builder.append(cin.nextLine() + "\n");
    }
    cin.close();
    return builder.toString();
}

/**
 * 加载网页并解析为Document
 */
public static Document request(String url) {
    try {
        HttpResponse resp = client.execute(new HttpGet(url));
        String content = EntityUtils.toString(resp.getEntity(), "utf8");
        return Jsoup.parse(content);
    } catch (IOException e) {
        e.printStackTrace();
    }
    return null;
}

public static String getFileType(String filepath) {
    String filename = Paths.get(filepath).getFileName().toString();
    int pointIndex = filename.lastIndexOf('.');
    if (pointIndex == -1) return null;
    String fileType = filename.substring(pointIndex + 1);
    return fileType;
}

public static void main(String[] args) {
    System.out.println(Util.loads(Paths.get("天下大势为我所控.txt")));
}
}
