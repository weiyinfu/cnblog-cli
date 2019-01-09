package cnblog.spider;

import com.alibaba.fastjson.JSON;
import cnblog.entity.Post;
import cnblog.util.Util;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class CnblogSpider {
String userid;
//爬虫的基本链接
final static String BASE_URL = "http://www.cnblogs.com/";
//存放缓存的目录
final static String POST_FILE = "posts.json";

public CnblogSpider(String username) {
    this.userid = username;
}

/**
 * 根据博客id返回博客链接
 */
public String getLinkById(String postId) {
    return BASE_URL + userid + "/p/" + postId + ".html";
}

/**
 * 更新本地缓存，有两种方式：增量更新、全部更新
 */
public void updateLocalCache(boolean completely) {
    if (completely) {
        List<Post> posts = getPostsAfter(0);
        save(posts);
    } else {
        List<Post> cachePosts = getCachePosts();
        if (cachePosts == null) {//本地无缓存，直接进行全部更新
            save(getPostsAfter(0));
        } else {
            updateChanges(cachePosts);
        }
    }
}

/**
 * 将Post列表保存到本地
 */
private void save(List<Post> posts) {
    Util.writeAndClose(Util.home(POST_FILE), JSON.toJSONString(posts));
}

/**
 * 增量更新，只更新比本文件新的
 *
 * @param posts 本地已经缓存了的博客
 */
private void updateChanges(List<Post> posts) {
    long lastModifiedTime = posts.get(0).getDateCreated().getTime();//获取本地最新的博客
    Post post = getRecentPost(0);//获取博客园最新的博客
    //本地最新博客等于博客园最新博客，则无需更新
    if (lastModifiedTime == post.getDateCreated().getTime()) {
        return;
    } else if (lastModifiedTime > post.getDateCreated().getTime()) {
        //本地最新博客晚于博客园最新博客，删除本地部分博客
        posts = posts.stream().filter(x -> x.getDateCreated().getTime() > post.getDateCreated().getTime()).collect(Collectors.toList());
        save(posts);
    } else {
        //本地最新博客早于博客园最新博客，从博客园获取部分博客
        List<Post> newPosts = getPostsAfter(lastModifiedTime);
        posts.addAll(newPosts);
        posts = posts.stream().distinct().sorted(Comparator.comparing(Post::getDateCreated).reversed()).collect(Collectors.toList());
        save(posts);
    }
}

/**
 * 获取一个时间点之后的全部博客题目，如果time=0，则获取全部博客
 */
public List<Post> getPostsAfter(long time) {
    String homepage = BASE_URL + userid;
    int totalPage = 1;
    List<Post> posts = new ArrayList<>();
    try {
        over:
        for (int i = 1; i <= totalPage; i++) {
            String url = homepage + "/p/?page=" + i;
            Document html = Util.request(url);
            if (totalPage == 1) {
                Elements node = html.getElementsByClass("pager");
                totalPage = Integer.parseInt(Util.search("共(\\d+)页", 1, node.text()));
            }
            Elements postList = html.getElementsByClass("PostList");
            for (Iterator<Element> j = postList.iterator(); j.hasNext(); ) {
                Element post = j.next();
                Element a = post.getElementsByClass("postTitl2").get(0).getElementsByTag("a").get(0);
                Post p = new Post();
                p.setTitle(a.text());
                p.setLink(a.attr("href"));
                String desc = post.getElementsByClass("postDesc2").text();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                p.setDateCreated(format.parse(Util.search("\\d+-\\d+-\\d+\\s+\\d+:\\d+", 0, desc)));
                p.setPostid(Util.search("(\\d+).html", 1, p.getLink()));
                if (p.getDateCreated().getTime() < time) {
                    break over;
                }
                posts.add(p);
                System.out.println(p);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return posts;
}

/**
 * 获取缓存的博客列表
 */
public List<Post> getCachePosts() {
    Path postFile = Util.home(POST_FILE);
    if (Files.notExists(postFile)) {
        return null;
    }
    try {
        return JSON.parseArray(Util.loads(Util.home(POST_FILE)), Post.class);
    } catch (Exception e) {
        try {
            //如果读文件出错了，那么这个文件也就没有存在的必要了
            Files.deleteIfExists(Util.home(POST_FILE));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }
}

/**
 * 获取第几篇文章，如果超出了第一页，必然报错
 */
public Post getRecentPost(int which) {
    String homepage = BASE_URL + userid;
    try {
        Document html = Util.request(homepage);
        Elements titles = html.getElementsByClass("postTitle");
        int cnt = 0;
        Element ansTitle = null;
        for (Element title : titles) {
            if (title.text().startsWith("[置顶]")) continue;//过滤掉置顶的博客
            if (cnt == which) {
                ansTitle = title;
                break;
            }
            cnt += 1;
        }
        Post p = new Post();
        Element a = ansTitle.getElementsByTag("a").get(0);
        p.setPostid(Util.search("(\\d+).html", 1, a.attr("href")));
        p.setLink(a.attr("href"));
        p.setTitle(a.text());
        Element desc = ansTitle.nextElementSibling();
        while (!desc.hasClass("postDesc")) {
            desc = desc.nextElementSibling();
        }
        p.setDateCreated(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(Util.search("\\d+-\\d+-\\d+\\s+\\d+:\\d+", 0, desc.text())));
        return p;
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}

public static void main(String[] args) {
    CnblogSpider spider = new CnblogSpider("weidiao");
    spider.updateLocalCache(false);
}
}

