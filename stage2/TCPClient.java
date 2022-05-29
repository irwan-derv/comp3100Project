import java.net.*;
import java.io.*;
import java.util.*;

public class TCPClient {
  static BufferedReader in;
  static DataOutputStream out;
  static Socket s = null;
  static int serverPort = 50000; // default server port

  static boolean loggingMode = false;

  public static void main(String[] args) {
    System.out.println("# Started TCPClient on port " + serverPort);
    boolean running = true;

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-l")) {
        loggingMode = true;
      } else if (args[i].equals("-p")) {
        try {
          serverPort = Integer.parseInt(args[i+1]);
        } catch(Exception e) {
          System.out.println("Invalid port: Defaulting to 50000");
        }
      }
    }

    try {
      s = new Socket("127.0.0.1", serverPort);
      in = new BufferedReader(new InputStreamReader(s.getInputStream()));
      out = new DataOutputStream(s.getOutputStream());
      String data;

      performHandshake(System.getProperty("user.name")); // provide user to authenticate with using logged in user
      sendMessage("REDY");

      String server = "";
      int maxServer = 0;
      int nextServer = 0;

      while (running) {
        data = receiveMessage();
        if (data.startsWith("JOBN")) {
          String[] job = data.split(" ");
          String jobNo = job[2];

          String[] serverInfo = getFirstFit(job[4], job[5], job[6]);
          scheduleJob(jobNo, serverInfo[0], serverInfo[1]);

          nextServer++;
          if (nextServer > maxServer) {
            nextServer = 0;
          }
        } else if (data.equals("NONE")) {
          running = false;
          closeConnection();
        } else {
          sendMessage("REDY");
        }
      }

    } catch (UnknownHostException e) {
      System.out.println("Sock: " + e.getMessage());
    } catch (EOFException e) {
      System.out.println("EOF: " + e.getMessage());
    } catch (IOException e) {
      System.out.println("IO: " + e.getMessage());
    } catch (RuntimeException e) {
      System.out.println("RuntimeException: " + e.getMessage());
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

  private static void sendMessage(String message) throws IOException {
    out.write((message + "\n").getBytes());
    if (loggingMode) {
      System.out.println("SENT: " + message);
    }
  }

  private static String receiveMessage() throws IOException {
    String data = in.readLine();
    if (loggingMode) {
      System.out.println("RCVD: " + data);
    }
    return data;
  }

  private static void scheduleJob(String jobNo, String server, String nextServer) throws IOException {
    String scheduleMsg = "SCHD " + jobNo + " " + server + " " + nextServer;
    sendMessage(scheduleMsg);
  }

  private static void performHandshake(String userId) throws IOException, RuntimeException {
    String data;

    sendMessage("HELO");

    data = receiveMessage();
    if (data.equals("OK")) {
      sendMessage("AUTH " + userId);

      data = receiveMessage();
      if (!data.equals("OK")) {
        throw new RuntimeException("Unexpected handshake response");
      }
    } else {
      throw new RuntimeException("Unexpected handshake response");
    }
  }

  private static void closeConnection() throws IOException, RuntimeException {
    sendMessage("QUIT");
    String data = receiveMessage();
    if (data.equals("QUIT")) {
      s.close();
    } else {
      throw new RuntimeException("Failed to close the connection");
    }
  }

  private static String[][] getCapableServers(String threads, String memory, String disk) throws IOException {
    String data;
    String getsMessage = "GETS Capable " + threads + " " + memory + " " + disk;
    sendMessage(getsMessage);

    data = receiveMessage();
    int serverCount = Integer.parseInt(data.split(" ")[1]);

    sendMessage("OK");

    String[][] servers = new String[serverCount][9];
    for (int i = 0; i < serverCount; i++) {
      servers[i] = receiveMessage().split(" ");
    }
    sendMessage("OK");
    receiveMessage();

    return servers;
  }

  private static String[] getFirstFit(String threads, String memory, String disk) throws IOException {
    String[][] servers = getCapableServers(threads, memory, disk);
    int smallestQueueIdx = servers.length -1;

    for (int i = 0; i < servers.length; i++) {
      int currentRemainder = Integer.parseInt(servers[i][4]) - Integer.parseInt(threads);
      if (currentRemainder >= 0 && Integer.parseInt(servers[i][7]) < 1) {
        return servers[i];
      }
    }
    int currentServer = servers.length -1;
    while (servers[currentServer][4] == servers[servers.length-1][4]) {
      if (Integer.parseInt(servers[currentServer][7]) < smallestQueueIdx) {
        smallestQueueIdx = Integer.parseInt(servers[currentServer][7]);
      }
      currentServer--;
    }

    return servers[smallestQueueIdx];
  }
}
