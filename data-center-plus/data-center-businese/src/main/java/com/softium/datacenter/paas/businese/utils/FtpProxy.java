package com.softium.datacenter.paas.web.utils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Fanfan.Gong
 **/
public class FtpProxy {
    private FTPClient ftpClient;
    private final String host;
    private final Integer port;
    private final String protocol;
    private boolean connected;
    private boolean loginFlag;
    private static final Logger logger = LoggerFactory.getLogger(FtpProxy.class);


    public FtpProxy(String host, Integer port) {
        this(host, port, "FTP");
    }

    public FtpProxy(String host, Integer port, String protocol) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
    }

    /**
     * 打开链接
     * @return
     */
    private boolean _open() {
        if ("FTP".equals(this.protocol)) {
            ftpClient = new FTPClient();
        } else {
            ftpClient = new FTPSClient();
        }
        ftpClient.setControlEncoding("UTF-8");
        try {
            logger.info("ftp connect params host: {}, port: {}, protocol: {}", this.host, this.port, this.protocol);
            ftpClient.connect(this.host, this.port);
            this.connected = true;
        } catch (IOException e) {
            this.connected = false;
            e.printStackTrace();
        }
        logger.info("ftp connect result: {}, replyCode: {}, replyString: {}", this.connected, this.ftpClient.getReplyCode(), this.ftpClient.getReplyString());
        return connected;
    }

    /**
     * 登录
     * @param username
     * @param password
     * @return
     */
    public boolean login(String username, String password) {
        //todo 此处可以做多次尝试
        if (!this.connected) {
            _open();
        }

        try {
            this.ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            this.ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
            this.ftpClient.enterLocalPassiveMode();
            logger.info("ftp login params, username: {}, password: {}", username, password);
            this.loginFlag = this.ftpClient.login(username, password);
            logger.info("ftp login result, replyCode: {}, replyStr: {}", this.ftpClient.getReplyCode(), this.ftpClient.getReplyString());
        } catch (IOException e) {
            this.loginFlag = false;
            e.printStackTrace();
        }
        return this.loginFlag;
    }

    /**
     *
     * @param dirName
     * @return
     */
    public boolean changeDir(String dirName) {
        boolean flag = false;
        try {
            flag = this.ftpClient.changeWorkingDirectory(dirName);
        }catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        logger.info("change work dir result: {}, to: {}, replyCode: {}, replyStr: {}", flag,
                dirName, this.ftpClient.getReplyCode(), this.ftpClient.getReplyString());
        return flag;
    }

    /**
     *
     * @param pathname
     * @return
     */
    public FTPFile[] list(String pathname) {
        FTPFile[] files = null;
        try {
            if (StringUtils.isEmpty(pathname)) {
                files = this.ftpClient.listFiles();
            }else {
                files = this.ftpClient.listFiles(pathname);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("ftp list dir: {}, replyCode: {}, replyStr: {}", pathname, this.ftpClient.getReplyCode(), this.ftpClient.getReplyString());
        return files;
    }

    /**
     * 打印当前工作目录
     * @return
     * @throws IOException
     */
    public String currentWorkDir() throws IOException {
        return this.ftpClient.printWorkingDirectory();
    }

    /**
     * 从FTP加载文件
     * @param pathname
     * @return
     */
    public InputStream loadFile(String pathname) {
        InputStream in = null;
        try {
            in =  this.ftpClient.retrieveFileStream(pathname);
            logger.info("ftp load file: {}, replyCode: {}, replyStr: {}", pathname, this.ftpClient.getReplyCode(), this.ftpClient.getReplyString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return in;
    }

    /**
     *
     * @return
     */
    public boolean completePendingCommand() throws IOException {
        return this.ftpClient.completePendingCommand();
    }
    public String getTimestamp(String pathname) {
        try {
            return this.ftpClient.getModificationTime(pathname);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
