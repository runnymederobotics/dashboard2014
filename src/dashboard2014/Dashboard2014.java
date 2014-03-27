package dashboard2014;

import java.awt.GridLayout;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import javax.swing.JApplet;

public class Dashboard2014 extends JApplet implements Runnable {

    public static final String host = "10.13.10.2";
    public static final int port = 8000;
    CameraPanel cameraPanel;
    AutonomousPanel autonomousPanel;
    Socket sock = null;
    InputStream is;
    OutputStream os;

    @Override
    public void init() {
        setSize(680, 480);

        setLayout(new GridLayout(2, 1));

        cameraPanel = new CameraPanel(this);
        autonomousPanel = new AutonomousPanel(this);

        add(cameraPanel);
        add(autonomousPanel);

        (new Thread(this)).start();
    }

    public void sendAutonomousMode(String autonomousMode) {
        try {
            if (os != null && connected()) {
                os.write(autonomousMode.getBytes());
                System.out.println(autonomousMode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean connected() {
        //connected if sock is not null, isConnected() and !isClosed
        return sock != null && sock.isConnected() && !sock.isClosed();
    }

    @Override
    public void run() {
        while (true) {
            System.out.print("Connecting...");
            try {
                //If the socket is not yet connected, or if it's disconnected, try to connect
                if (!connected()) {
                    sock = new Socket(host, port);
                    is = sock.getInputStream();
                    os = sock.getOutputStream();
                    System.out.println("Connected!");
                } else if (is != null && connected()) {
                    byte[] bytes = new byte[1024];
                    int len = is.read(bytes);

                    //If is.read returns a negative number, then there was an error so close the socket and kill this 
                    if (len < 0) {
                        sock.close();
                    }

                    String str = new String(bytes, 0, len);
                    System.out.println(str);
                }
            } catch (Exception e) {
                sock = null;
                autonomousPanel.clear();
            }

            System.out.println("Could not connect to " + host + " on port " + port + ".");
        }
    }
}