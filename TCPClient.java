import java.net.*;
import java.io.*;

public class TCPClient {
  public static void main(String[] args) {
    Socket s = null;
    boolean running = true;
    try {
      int serverPort = 50001;
      s = new Socket("127.0.0.1", serverPort);
      BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
      DataOutputStream out = new DataOutputStream(s.getOutputStream());

      out.write("HELO\n".getBytes());
      System.out.println("SENT: HELO");

      String data; 

      data = in.readLine();;
      System.out.println("RCVD: " + data);

      if (data.equals("OK")) {
        out.write("AUTH test\n".getBytes());
        System.out.println("SENT: AUTH test");

        data = in.readLine();
        System.out.println("RCVD: " + data);
        if (data.equals("OK")) {
          out.write("REDY\n".getBytes());
          System.out.println("SENT: REDY");

          while (running) {
            data = in.readLine();
            System.out.println("RCVD: " + data);
            if (data.startsWith("JOBN")) {
              String jobNo = data.split(" ")[2];
              String scheduleMsg = "SCHD " + jobNo + " 4xlarge 0";
              out.write((scheduleMsg + "\n").getBytes());
              System.out.println(scheduleMsg);
            } else if (data.equals("NONE")) {
              out.write("QUIT\n".getBytes());
              System.out.println("SENT: QUIT");
              running = false;
            } else {
              out.write("REDY\n".getBytes());
              System.out.println("SENT: REDY");
            }
          }
        }
      }

      

    } catch (UnknownHostException e) {
      System.out.println("Sock: " + e.getMessage());
    } catch (EOFException e) {
      System.out.println("EOF: " + e.getMessage());
    } catch (IOException e) {
      System.out.println("IO: " + e.getMessage());
    } finally {
      if (s != null) {
        try {
          s.close();
        } catch (IOException e) {
          System.out.println("close: " + e.getMessage());
        }
      }
    }
  }

  private static void debug(int num) {
    System.out.println("checkpoint " + num);
  }
}
