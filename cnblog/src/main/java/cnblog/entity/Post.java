package cnblog.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Some attributes are useless although they are mentioned in the doc.when
 * uploading,some attributs are decided by server,so they are not decided by us.
 * When request,the server will tell fill in the form ,so don't delete
 * attributes not used .Some methods's parameter are useless,for
 * example:appKey,blogId. So I deleted them.
 */

public class Post {
public class Source {
    String name, url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}

public class Enclosure {
    int length;
    String type;
    String url;

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}

Date dateCreated;
String description;
String title;
List<String> categories;
Enclosure enclosure;
String link, permalink;
Object postid;
Source source;
String userid;
Object mt_allow_comments, mt_allow_pings, mt_convert_breaks;
String mt_text_more, mt_excerpt, mt_keywords;
String wp_slug;

public Date getDateCreated() {
    return dateCreated;
}

public void setDateCreated(Date dateCreated) {
    this.dateCreated = dateCreated;
}

public String getUserid() {
    return userid;
}

public void setUserid(String userid) {
    this.userid = userid;
}

public String getDescription() {
    return description;
}

public void setDescription(String description) {
    this.description = description;
}

public String getTitle() {
    return title;
}

public void setTitle(String title) {
    this.title = title;
}

public List<String> getCategories() {
    return categories;
}

public void setCategories(List<String> categories) {
    this.categories = categories;
}

public Enclosure getEnclosure() {
    return enclosure;
}

public void setEnclosure(Enclosure enclosure) {
    this.enclosure = enclosure;
}

public String getLink() {
    return link;
}

public void setLink(String link) {
    this.link = link;
}

public String getPermalink() {
    return permalink;
}

public void setPermalink(String permalink) {
    this.permalink = permalink;
}

public Object getPostid() {
    return postid;
}

public void setPostid(Object postid) {
    this.postid = postid;
}

public Source getSource() {
    return source;
}

public void setSource(Source source) {
    this.source = source;
}

public Object getMt_allow_comments() {
    return mt_allow_comments;
}

public void setMt_allow_comments(Object mt_allow_comments) {
    this.mt_allow_comments = mt_allow_comments;
}

public Object getMt_allow_pings() {
    return mt_allow_pings;
}

public void setMt_allow_pings(Object mt_allow_pings) {
    this.mt_allow_pings = mt_allow_pings;
}

public Object getMt_convert_breaks() {
    return mt_convert_breaks;
}

public void setMt_convert_breaks(Object mt_convert_breaks) {
    this.mt_convert_breaks = mt_convert_breaks;
}

public String getMt_text_more() {
    return mt_text_more;
}

public void setMt_text_more(String mt_text_more) {
    this.mt_text_more = mt_text_more;
}

public String getMt_excerpt() {
    return mt_excerpt;
}

public void setMt_excerpt(String mt_excerpt) {
    this.mt_excerpt = mt_excerpt;
}

public String getMt_keywords() {
    return mt_keywords;
}

public void setMt_keywords(String mt_keywords) {
    this.mt_keywords = mt_keywords;
}

public String getWp_slug() {
    return wp_slug;
}

public void setWp_slug(String wp_slug) {
    this.wp_slug = wp_slug;
}

@Override
public int hashCode() {
    return new Integer(this.getPostid().toString());
}

@Override
public boolean equals(Object obj) {
    if (obj instanceof Post) {
        Post p = (Post) obj;
        return this.getPostid().equals(p.getPostid());
    }
    return false;
}

/**
 * 获取月日
 */
public String getCreatedMonthDay() {
    if (getDateCreated() == null) return null;
    LocalDateTime local = LocalDateTime.ofInstant(this.getDateCreated().toInstant(), ZoneId.systemDefault());
    return String.format("%d月%d日", local.getMonthValue(), local.getDayOfMonth());
}

@Override
public String toString() {
    return String.format("%20s %10s %10s",
            this.getTitle(),
            this.getPostid(),
            this.getCreatedMonthDay()
    );
}
}
