import java.net.*;
import java.io.*;
import java.util.*;

public class TCPClient {
  static BufferedReader in;
  static DataOutputStream out;
  static Socket s;

  public static void main(String[] args) {
    s = null;
    boolean running = true;

    try {
      int serverPort = 50000;
      s = new Socket("127.0.0.1", serverPort);
      in = new BufferedReader(new InputStreamReader(s.getInputStream()));
      out = new DataOutputStream(s.getOutputStream());

      out.write("HELO\n".getBytes());
      System.out.println("SENT: HELO");

      String data; 

      data = in.readLine();
      System.out.println("RCVD: " + data);

      if (data.equals("OK")) {
        out.write("AUTH iderviskadic\n".getBytes());
        System.out.println("SENT: AUTH iderviskadic");

        data = in.readLine();
        System.out.println("RCVD: " + data);
        if (data.equals("OK")) {
          out.write("REDY\n".getBytes());
          System.out.println("SENT: REDY");


          String maxServer = "0";
          String server = "";
          int nextServer = 0;

          data = in.readLine();
          System.out.println("RCVD: " + data);
          if (data.startsWith("JOBN")) {
            String[] job = data.split(" ");
            String jobNo = job[2];

            String[] serverInfo = getLargestServer(job[4], job[5], job[6]);
            maxServer = serverInfo[1];
            server = serverInfo[0];

            String scheduleMsg = "SCHD " + jobNo + " " + server + " " + nextServer;
            out.write((scheduleMsg + "\n").getBytes());
            System.out.println("SENT: " + scheduleMsg);

            nextServer++;
            if (nextServer > Integer.parseInt(maxServer)) {
              nextServer = 0;
            }
          }


          while (running) {
            data = in.readLine();
            System.out.println("RCVD: " + data);
            if (data.startsWith("JOBN")) {
              String[] job = data.split(" ");
              String jobNo = job[2];

              System.out.println(maxServer);

              String scheduleMsg = "SCHD " + jobNo + " " + server + " " + nextServer;
              out.write((scheduleMsg + "\n").getBytes());
              System.out.println("SENT: " + scheduleMsg);

              nextServer++;
              if (nextServer > Integer.parseInt(maxServer)) {
                nextServer = 0;
              }
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

  private static int incrementNextCore(int core, int coreCount) {
    if (core < coreCount-1) {
      return core++;
    }
    return 0;
  }

  private static void sendMessage(String message) throws IOException {
    out.write((message + "\n").getBytes());
    System.out.println("SENT: " + message);
  }

  private static String[] getLargestServer(String threads, String memory, String disk) {
    try {
      String data;
      String getsMessage = "GETS Capable " + threads + " " + memory + " " + disk;
      sendMessage(getsMessage);

      data = in.readLine();
      System.out.println("RCVD: " + data);

      int serverCount = Integer.parseInt(data.split(" ")[1]);

      sendMessage("OK");

      String server[] = new String[0];
      int maxCores = 0;

      for (int i = 0; i < serverCount; i++) {
        String[] serverInfo = in.readLine().split(" ");
        if (Integer.parseInt(serverInfo[4]) > maxCores) {
          server = serverInfo;
          maxCores = Integer.parseInt(serverInfo[4]);
        } else if (serverInfo[0].equals(server[0])) {
          server = serverInfo;
        }
      }
      sendMessage("OK");
      System.out.println("RCVD: " + in.readLine());

      return server;

    } catch (UnknownHostException e) {
      System.out.println("Sock: " + e.getMessage());
    } catch (EOFException e) {
      System.out.println("EOF: " + e.getMessage());
    } catch (IOException e) {
      System.out.println("IO: " + e.getMessage());
    }
    return new String[0];
  }

  private static void debug(int num) {
    System.out.println("checkpoint " + num);
  }
}
