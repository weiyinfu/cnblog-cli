package cnblog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test {
public static void main(String[] args) {
    Logger logger = LoggerFactory.getLogger(Test.class);
    logger.info("good afternoon");
    Cmd cmd = new Cmd();
    cmd.parse(new String[]{"cn", "load"});
}
}
