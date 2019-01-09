package cnblog;

import cnblog.entity.Post;
import cnblog.spider.CnblogSpider;
import cnblog.util.CipherUtil;
import cnblog.util.Util;
import cnblog.xmlrpc.Blog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;

/**
 * 用Cnblog这个类将XMLRPC和爬虫两部分结合起来，所以不需要直接调用XMLRPC和Spider
 */
public class Cnblog {
String userid, username, password;
Blog xmlrpc;
CnblogSpider spider;
final String CONFIG_FILE = "config.properties";

/**
 * @param userid:用户的唯一标识，终身不变
 * @param username：界面显示的名称，也就是用户昵称，用户可以自由设置
 * @param password：用户密码
 */
public Cnblog(String userid, String username, String password) {
    this.userid = userid;
    this.username = username;
    this.password = password;
    createComponent();
    saveConfig();
}

/**
 * 无参构造函数，默认加载过去的配置
 */
public Cnblog() {
    if (Files.exists(Util.home(CONFIG_FILE))) {
        loadConfig();
        createComponent();
    }
}

/**
 * 检测系统是否可用
 */
public boolean isReady() {
    return xmlrpc != null && spider != null;
}

/**
 * 创建功能组件：XMLRPC模块和爬虫模块
 */
private void createComponent() {
    xmlrpc = new Blog(userid, username, password);
    spider = new CnblogSpider(userid);
}

/**
 * 保存配置
 */
private void saveConfig() {
    Properties p = new Properties();
    p.setProperty("userid", userid);
    p.setProperty("username", username);
    p.setProperty("password", CipherUtil.encrypt(password));
    try {
        BufferedWriter cout = Files.newBufferedWriter(Util.home(CONFIG_FILE));
        p.store(cout, "user info");
    } catch (IOException e) {
        e.printStackTrace();
    }
}

/**
 * 加载配置
 */
private void loadConfig() {
    Properties p = new Properties();
    try {
        BufferedReader cin = Files.newBufferedReader(Util.home(CONFIG_FILE));
        p.load(cin);
        username = p.getProperty("username");
        userid = p.getProperty("userid");
        password = CipherUtil.decrypt(p.getProperty("password"));
        cin.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

/**
 * 获取缓存列表
 */
public List<Post> getCachePosts() {
    return spider.getCachePosts();
}

/**
 * 更新缓存列表
 */
public void updateCache(boolean completely) {
    spider.updateLocalCache(completely);
}

/**
 * 获取最近第which篇博客（下标从0开始）
 */
public Post getRecentPost(int which) {
    return spider.getRecentPost(which);
}

public Post getPost(String postId) {
    return xmlrpc.getPost(postId);
}

public boolean editPost(String postId, Post post) {
    return xmlrpc.editPost(postId, post, true);
}

public void deletePost(String postId) {
    xmlrpc.deletePost(postId, true);
}

public String newPost(Post post) {
    return xmlrpc.newPost(post, true);
}

public String getLinkById(String postId) {
    return spider.getLinkById(postId);
}

public String newMediaObject(byte[] mediaData, String filename) {
    return xmlrpc.newMediaObject(mediaData, filename);
}

public static void main(String[] args) {
    Cnblog cnblog = new Cnblog();
    cnblog.loadConfig();
    System.out.println(cnblog.userid + " " + cnblog.username + " " + cnblog.password);
}
}
