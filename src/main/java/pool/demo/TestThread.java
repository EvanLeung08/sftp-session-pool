package pool.demo;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import pool.common.utils.SftpSessionPool;
import pool.exceptions.NoAvailableSessionException;

import java.util.Vector;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TestThread extends Thread {
    private static final int MAX_RETRIES = 3;
    private SftpSessionPool pool;
    private String host;
    private String username;
    private String password;
    private String remoteSftpPath;

    public TestThread(SftpSessionPool pool, String host, String username, String password, String remoteSftpPath) {
        this.pool = pool;
        this.host = host;
        this.username = username;
        this.password = password;
        this.remoteSftpPath = remoteSftpPath;
    }

    public void run() {
        Session session = null;
        ChannelSftp channelSftp = null;
        try {
            int retries = 0;
            while (true) {
                try {
                    session = pool.getSession(host, username, password, 1, TimeUnit.SECONDS);
                    break; // if getSession() is successful, break the loop
                } catch (NoAvailableSessionException e) {
                    if (++retries > MAX_RETRIES) {
                        throw e; // if exceeded max retries, rethrow the exception
                    }
                    log.info("Thread {}:Failed to get session in this round. Start to retry now. Current retry count is {}", this.getId(), retries);
                    // if not exceeded max retries, sleep for a while and then continue the loop to retry
                    Thread.sleep(5000);
                }
            }
            log.info("Thread {} got session {}", this.getId(), session);

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            // ... use the session and channel
            Vector<ChannelSftp.LsEntry> list = channelSftp.ls(this.remoteSftpPath);
            for (ChannelSftp.LsEntry entry : list) {
                // System.out.println(entry.getFilename());
                //Simulate file process
                Thread.sleep(5000);
            }
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                pool.closeSession(session);
                log.info("Thread {} closed session {}", this.getId(), session);
            }
        }
    }
}
