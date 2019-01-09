package cnblog;

import cnblog.entity.Post;
import cnblog.util.CharTable;
import cnblog.util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 * 实现命令行
 */
public class Cmd {
static final String HELP = Util.loads("/help.txt");
static Cnblog cnblog = new Cnblog();
final static String[] options = "load help new ls edit get del media config".split(" ");

static void error() {
    System.out.println("命令解析错误，请查看帮助 cn help");
}

static void help() {
    System.out.println(HELP);
}

static void parse(String[] args) {
    if (args.length == 0) return;//什么都没有，直接返回
    if (args.length == 1) {
        help();
        return;
    }
    //将命令规范化，只规范化第1个参数即可，如：将lo规范化为load，将d规范化为del
    args[1] = args[1].toLowerCase();
    int which = -1;
    for (int i = 0; i < options.length; i++) {
        if (options[i].startsWith(args[1])) {
            if (which == -1) {
                which = i;
            } else {
                System.out.printf("ambiguous command `%s`,options are `%s`,`%s`\n\n",
                        args[1], options[i], options[which]);
                return;
            }
        }
    }
    if (which == -1) {
        help();
        return;
    } else {
        args[1] = options[which];
    }
    //如果博客还没准备好，就不能直接开工
    if (!"config help".contains(args[1]) && !cnblog.isReady()) {
        System.out.println("请先使用命令cn config userid username password 设置全局");
        return;
    }
    switch (args[1]) {
        case "load": {
            if (args.length == 3 && args[2].equals("complete")) {
                System.out.println("load complete may cost some time ,please wait patiently");
                cnblog.updateCache(true);
            } else if (args.length == 2) {
                cnblog.updateCache(false);
            } else {
                error();
            }
            System.out.println("load successfully");
            break;
        }
        case "new": {
            if (args.length == 3) {
                String filename = args[2];
                System.out.println(filename);
                if (Files.notExists(Paths.get(filename))) {
                    System.out.println("文件" + filename + "不存在");
                } else {
                    Post post = loadPostFromFile(Paths.get(filename));
                    if (post == null) {
                        System.out.println("文件" + filename + "解析异常");
                    } else {
                        String postId = cnblog.newPost(post);
                        if (postId == null) {
                            System.out.println("创建随笔失败，请检查配置");
                        } else {
                            System.out.println(cnblog.getLinkById(postId));
                        }
                    }
                }
                break;
            } else {
                error();
            }
        }
        case "help": {
            help();
            break;
        }
        case "edit": {
            if (args.length == 4) {
                String post = args[2], file = args[3];
                edit(post, file);
            } else {
                error();
            }
            break;
        }
        case "del": {
            if (args.length == 3) {
                String postid = args[2];
                postid = getPostId(postid);
                Post post = cnblog.getPost(postid);
                if (post == null) {
                    System.out.println("此博客不存在");
                } else {
                    bakPost(post);
                    cnblog.deletePost(postid);
                    System.out.println("删除《" + post.getTitle() + "》成功，可在~/cnblog目录下找回");
                }
            } else {
                error();
            }
            break;
        }
        case "get": {
            if (args.length == 3) {
                String postid = getPostId(args[2]);
                if (postid == null) {
                    System.out.println("no this post");
                } else {
                    downloadPost(postid);
                    System.out.println("博客下载成功");
                }
            } else {
                error();
            }
            break;
        }
        case "config": {
            if (args.length == 5) {
                String userid = args[2], username = args[3], password = args[4];
                System.out.println(userid + " " + username + " " + password);
                cnblog = new Cnblog(userid, username, password);
                System.out.println("设置成功");
            } else {
                error();
            }
            break;
        }
        case "ls": {
            int cnt = 10;
            if (args.length == 3) {
                cnt = Integer.parseInt(args[2]);
            }
            List<Post> posts = cnblog.getCachePosts();
            String[][] matrix = new String[Math.min(cnt, posts.size())][3];
            for (int i = 0; i < Math.min(cnt, posts.size()); i++) {
                Post p = posts.get(i);
                matrix[i][0] = p.getTitle();
                matrix[i][1] = p.getPostid().toString();
                matrix[i][2] = p.getCreatedMonthDay();
            }
            System.out.println(CharTable.tos(matrix, 3, new int[]{20, 10, 10}, 0xffff));
            break;
        }
        case "media": {
            if (args.length < 3) {
                System.out.println("usage: cn media filename");
                break;
            }
            String filename = args[2];
            try {
                byte[] data = Files.readAllBytes(Paths.get(filename));
                String url = cnblog.newMediaObject(data, Paths.get(filename).getFileName().toString());
                System.out.println("media url is : " + url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            break;
        }
        default: {
            help();
        }
    }
}

private static void edit(String postId, String file) {
    Path path = Paths.get(file);
    if (Files.notExists(path)) {
        System.out.println("文件" + file + "不存在");
        return;
    }
    String filename = path.getFileName().toString();
    if (filename.contains(".")) {
        filename = filename.substring(0, filename.lastIndexOf('.'));
    }
    postId = getPostId(postId);
    Post oldPost = cnblog.getPost(postId);
    Post newPost = loadPostFromFile(path);
    if (!oldPost.getTitle().equals(newPost.getTitle())) {
        bakPost(oldPost);
        System.out.printf("原博客题目为%s，文件名称为%s，原博客已保存到本地(%s)", oldPost.getTitle(), filename, Util.home(oldPost.getTitle()));
    }
    boolean res = cnblog.editPost(oldPost.getPostid().toString(), newPost);
    if (res) {
        System.out.println("编辑成功");
    } else {
        System.out.println("编辑失败");
    }
}

//备份博客，防止用户误删除
private static void bakPost(Post p) {
    Util.writeAndClose(Util.home(p.getTitle()), p.getDescription());
}

//根据博客ID获取博客内容
private static void downloadPost(String postid) {
    Post post = cnblog.getPost(postid);
    String filename = post.getTitle() + ".txt";
    Util.writeAndClose(Paths.get(filename), post.getDescription());
}

//根据数字ID获取随便真正的ID
private static String getPostId(String id) {
    if (Util.match("\\d+", id)) {
        int postid = Integer.parseInt(id);
        if (postid < 10) {
            return cnblog.getRecentPost(postid).getPostid().toString();
        } else {
            return id;
        }
    } else {
        //可以考虑通过随笔题目获取随笔id
        return null;
    }
}

//从本地文件中加载博客
private static Post loadPostFromFile(Path path) {
    Post p = new Post();
    String filename = path.getFileName().toString();
    if (filename.contains(".")) {
        int pos = filename.lastIndexOf('.');
        if (filename.length() - pos < 5) {
            filename = filename.substring(0, pos);
        }
    }
    p.setTitle(filename);
    p.setDescription(Util.loads(path));
    p.setCategories(Collections.singletonList("[Markdown]"));
    return p;
}


public static void main(String[] args) {
    parse(args);
}

}
