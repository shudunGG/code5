package com.om.common.util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import com.om.bo.base.FtpInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 这个有问题，用不成，还是放弃吧，拿不到对端的操作系统
 * @version: 1.0
 * @Description: 远程解压缩指定目录下的指定名字的文件
 * @author: zshuai
 * @date: 2019年4月9日
 * @source http://www.manongjc.com/article/98387.html
 */
public class ExtractUtils {
    private static final Logger logger = LoggerFactory.getLogger(ExtractUtils.class);
    /*
     * @Description:远程解压缩指定目录下的指定名字的文件
     * @param path:指定解压文件的目录
     * @param fileName:需要解压的文件名字
     * @param decpath :解压完成后的存放路径
     */
    public static Boolean remoteZipToFile(String path, String fileName ,  FtpInfo ftp) {

        Boolean result =false;
        String os = System.getProperty("os.name").toLowerCase();
        Session sess = null;
        Connection connection = null;
        logger.info("----------------localMac:  os:" + os + "-------------");
        try {
            connection = new Connection(ftp.getIp());// 创建一个连接实例
            connection.connect();// Now connect
            boolean isAuthenticated = connection.authenticateWithPassword(ftp.getUsername(),
                    ftp.getPassword());// Authenticate
            if (isAuthenticated == false)
                throw new IOException("user and password error");
            sess = connection.openSession();// Create a session
            System.out.println("start exec command.......");
            sess.requestPTY("bash");
            sess.startShell();
            InputStream stdout = new StreamGobbler(sess.getStdout());
            InputStream stderr = new StreamGobbler(sess.getStderr());
            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdout));
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderr));
            PrintWriter out = new PrintWriter(sess.getStdin());
            if(os.indexOf("window")>-1){
                out.println("cd " + path);
                out.println("dir");
                out.println("unzip "+fileName);//解压tar格式
                out.println("dir");
                out.println("exit");
                out.close();
            }else if(os.indexOf("linux")>-1){
                out.println("cd " + path);
                out.println("ll");
                out.println("unzip -o " + fileName );//解压zip格式
                out.println("ll");
                out.println("exit");
                out.close();
            }else {
                return false;
            }
//            out.println("cd " + path);
//            out.println("ll");
//            // out.println("unzip -o " + fileName + "  -d /" + decpath + "/");//解压zip格式
//            out.println("tar zxvf " + fileName + "  -C /" + decpath + "/");//解压tar格式
//            out.println("ll");
//            out.println("exit");
//            out.close();
            sess.waitForCondition(ChannelCondition.CLOSED | ChannelCondition.EOF | ChannelCondition.EXIT_STATUS, 5000);
            logger.info("下面是从stdout输出:");
            while (true) {
                String line = stdoutReader.readLine();
                if (line == null)
                    break;
                logger.info(line);
            }
            logger.info("下面是从stderr输出:");
            while (true) {
                String line = stderrReader.readLine();
                if (line == null)
                    break;
                logger.info(line);
            }
            logger.info("ExitCode: " + sess.getExitStatus());

            result = true;
        } catch (IOException e) {
            e.printStackTrace(System.err);
            result =false;
            //System.exit(2);
        }finally {
            if(sess!=null){
                sess.close();/* Close this session */
            }
            if(connection!=null){
                connection.close();/* Close the connection */
            }

        }
        return result;

    }

}