package cnblog.xmlrpc;

import cnblog.entity.*;
import com.alibaba.fastjson.JSON;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

public class Blog {
XmlRpcClient client = new XmlRpcClient();
private String userid, username, password;

public Blog(String userid, String username, String password) {
    this.userid = userid;
    this.username = username;
    this.password = password;
    String url = "http://rpc.cnblogs.com/metaweblog/" + userid;
    XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
    try {
        config.setServerURL(new URL(url));
    } catch (MalformedURLException e) {
        e.printStackTrace();
    }
    client.setConfig(config);

}

Map<String, Object> toMap(Object object) {
    Map<String, Object> map = JSON.parseObject(JSON.toJSONString(object));
    return map;
}

<T> T toBean(Object object, Class<T> type) {
    T obj = JSON.parseObject(JSON.toJSONString(object), type);
    return obj;
}

<T> List<T> toBeanList(Object object, Class<T> type) {
    return JSON.parseArray(JSON.toJSONString(object), type);
}

List<Fun> help() {
    try {
        Object method = client.execute("system.listMethods", new Object[]{});
        List<Fun> a = new ArrayList<>();
        Object m[] = (Object[]) method;
        for (Object o : m) {
            Object args = client.execute("system.methodSignature", new Object[]{o});
            Object help = client.execute("system.methodHelp", new Object[]{o});
            Fun f = new Fun();
            f.setName(o.toString());
            List<String> params = toBeanList(((Object[]) args)[0], String.class);
            f.setArgs(params.subList(1, params.size()));
            f.setType(params.get(0));
            f.setHelp(help.toString());
            a.add(f);
        }
        return a;
    } catch (XmlRpcException e) {
        e.printStackTrace();
    }
    return null;
}

public void deletePost(String postid, boolean publish) {
    try {
        client.execute("blogger.deletePost", new Object[]{"", postid, username, password, publish});
    } catch (XmlRpcException e) {
        e.printStackTrace();
    }
}

public List<BlogInfo> getUsersBlogs() {
    try {
        Object object = client.execute("blogger.getUsersBlogs", new Object[]{"", username, password});
        return toBeanList(object, BlogInfo.class);
    } catch (XmlRpcException e) {
        e.printStackTrace();
    }
    return null;
}

public boolean editPost(String postId, Post post, boolean publish) {
    try {
        Object object = client.execute("metaWeblog.editPost",
                new Object[]{postId, username, password, toMap(post), publish});
        return (boolean) object;
    } catch (XmlRpcException e) {
        e.printStackTrace();
    }
    return false;
}

public List<CategoryInfo> getCategories() {
    try {
        Object object = client.execute("metaWeblog.getCategories", new Object[]{"", username, password});
        return toBeanList(object, CategoryInfo.class);
    } catch (XmlRpcException e) {
        e.printStackTrace();
    }
    return null;
}

public Post getPost(String postid) {
    try {
        Object object = client.execute("metaWeblog.getPost", new Object[]{postid, username, password});
        return toBean(object, Post.class);
    } catch (XmlRpcException e) {
        e.printStackTrace();
    }
    return null;
}

public List<Post> getRecentPosts(int numberOfPosts) {
    try {
        Object object = client.execute("metaWeblog.getRecentPosts",
                new Object[]{"", username, password, numberOfPosts});
        return toBeanList(object, Post.class);
    } catch (XmlRpcException e) {
        e.printStackTrace();
    }
    return null;
}

public String newPost(Post post, boolean publish) {
    try {
        Object object = client.execute("metaWeblog.newPost",
                new Object[]{"", username, password, toMap(post), publish});
        return (String) object;
    } catch (XmlRpcException e) {
        e.printStackTrace();
    }
    return null;
}

public int newCategory(WpCategory category) {
    try {
        Object object = client.execute("wp.newCategory", new Object[]{"", username, password, toMap(category)});
        return (int) object;
    } catch (XmlRpcException e) {
        e.printStackTrace();
    }
    return -1;
}

// 上传图片等二进制文件，返回url链接
public String newMediaObject(byte[] bits, String filename) {
    try {
        filename = Paths.get(filename).getFileName().toString();
        int pointIndex = filename.lastIndexOf('.');
        if (pointIndex == -1) {
            throw new RuntimeException("can only upload jpg/png/gif");
        }
        String fileType = filename.substring(pointIndex + 1);
        if (Arrays.asList("jpg", "png", "gif").indexOf(fileType) == -1) {
            throw new RuntimeException("can only upload jpg/png/gif");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("name", filename);
        map.put("bits", bits);
        map.put("type", "image/" + fileType);
        Object object = client.execute("metaWeblog.newMediaObject", new Object[]{"", username, password, map});
        @SuppressWarnings("unchecked")
        String url = (String) ((Map<String, Object>) object).get("url");
        return url;
    } catch (XmlRpcException e) {
        e.printStackTrace();
    }
    return null;
}
}
