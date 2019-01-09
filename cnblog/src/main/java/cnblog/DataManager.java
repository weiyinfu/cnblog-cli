package cnblog;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cnblog.entity.Post;
import cnblog.xmlrpc.Blog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class DataManager {
static final Blog blog = new Blog("xxx", "xxx", "");
static final HashSet<String> postSet = new HashSet<>();
static JSONObject root;
final static Path path = Paths.get(System.getProperty("user.home")).resolve("web.serial");

static void save(String json) {//保存客户端的更改
    try {
        root = JSON.parseObject(json);
        buildSet();
        Files.write(path, json.getBytes());
    } catch (IOException e) {
        e.printStackTrace();
    }
}

static void load() {
    if (!Files.exists(path)) {//本地没有文件，直接加载到根目录
        root = new JSONObject();
        JSONArray array = new JSONArray();
        blog.getRecentPosts(0).forEach((p) -> {
            array.add(toJSONObject(p));
        });
        root.put("children", array);
    } else {
        try {
            String s = Files.readAllLines(path).stream().collect(Collectors.joining());
            root = JSON.parseObject(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    buildSet();
}

static void buildSet() {
    postSet.clear();
    buildSet(root);
}

static void buildSet(JSONObject r) {//递归构建postSet
    String id = r.getString("id");
    if (id != null) {
        postSet.add(id);
    }
    JSONArray children = r.getJSONArray("children");
    if (children != null) {
        for (Object i : children) {
            buildSet((JSONObject) i);
        }
    }
}

static void loadRecent() {//加载最近的博客
    int sz = 2;
    List<Post> l;
    while (true) {
        l = blog.getRecentPosts(sz);
        if (postSet.contains(l.get(l.size() - 1).getPostid())) {//包含了最后一片的id
            break;
        }
        sz <<= 1;
    }
    JSONArray children = root.getJSONArray("children");
    if (children == null) children = new JSONArray();
    for (Post i : l) {
        if (postSet.contains(i.getPostid().toString())) {
            break;
        }
        postSet.add(i.getPostid().toString());
        children.add(toJSONObject(i));
    }
}

static void deletePost(String postId) {
    blog.deletePost(postId, false);
    postSet.remove(postId);
}

static JSONObject toJSONObject(Post post) {
    JSONObject json = new JSONObject();
    json.put("id", post.getPostid());
    json.put("text", post.getTitle());
    json.put("icon", false);
    return json;
}

public static void main(String[] args) {
    deletePost("5097302");
}
}
