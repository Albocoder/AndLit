package com.andlit.device;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public class SSHInterface {
    private static Session session;

    public SSHInterface(String un,String pw,String ip) throws JSchException {
        if(!connect(un,pw,ip))
            throw new JSchException("Did not connect successfully!");
    }

    public SSHInterface() throws JSchException {
        if(!isReady())
            throw new JSchException("Session doesn't exist!");
    }

    public boolean isReady() { return session != null; }

    public String saveImageInFile(Context c) {
        if (session == null || !session.isConnected()) {
            return null;
        }

        try {
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand("cat image.png");
            InputStream in = channel.getInputStream();
            channel.connect();

            byte[] buf = new byte[1024];
            File theLocalFile = new File(c.getFilesDir() + File.separator +
                    "captured", "device_capture.png");
            theLocalFile.getParentFile().mkdirs();

            FileOutputStream fos = new FileOutputStream(theLocalFile);
            while(true){
                int readSize = in.read(buf);
                if(readSize == -1)
                    break;
                fos.write(buf,0,readSize);
            }
            channel.disconnect();
            return theLocalFile.getAbsolutePath();
        } catch (JSchException|IOException e) {
            return null;
        }
    }

    public boolean captureImage() {
        ChannelExec channelssh = null;
        try {
            channelssh = (ChannelExec) session.openChannel("exec");
        } catch (JSchException e) { return false; }
        // Execute command
        channelssh.setCommand("raspistill -v -o image.png");
        try {
            InputStream in = channelssh.getInputStream();
        } catch (IOException e) { return false; }
        try {
            channelssh.connect();
        } catch (JSchException e) { return false; }
        channelssh.disconnect();
        return true;
    }

    public boolean cleanupImage() {
        ChannelExec channelssh = null;
        try {
            channelssh = (ChannelExec) session.openChannel("exec");
        } catch (JSchException e) { return false; }
        // Execute command
        channelssh.setCommand("rm image.png");
        try {
            InputStream in = channelssh.getInputStream();
        } catch (IOException e) { return false; }
        try {
            channelssh.connect();
        } catch (JSchException e) { return false; }
        channelssh.disconnect();
        return true;
    }

    private void disconnect() {
        session.disconnect();
        session = null;
    }

    private boolean connect(String un, String pw, String ip) {
        if(isReady())
            disconnect();
        JSch jsch = new JSch();
        try {
            session = jsch.getSession(un,ip, 22);
        } catch (JSchException e) { return false; }
        session.setPassword(pw);

        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        try {
            session.connect();
        } catch (JSchException e) {
            session = null;
            return false;
        }
        return true;
    }
}
