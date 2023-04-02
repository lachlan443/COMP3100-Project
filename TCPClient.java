import java.net.*;
import java.io.*;

public class TCPClient {
    public static void main(String args[]) {
        // arguments supply message and hostname of destination
        Socket s = null;
        try {
            int serverPort = 50000;
            s = new Socket("127.0.0.1", serverPort);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            //DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            String data;

            //Start of the 3 way handshake, client sends HELO
            out.write(("HELO\n").getBytes());
            out.flush();
            data = in.readLine();
            System.out.println("Received: " + data);

            //Server responds OK, client sends AUTH.
            if (data.equals("OK")) {
                out.write(("AUTH 47156074\n").getBytes());
                out.flush();
                data = in.readLine();
                System.out.println("Received: " + data);
            }

            if (data.equals("OK")) {
                out.write(("REDY\n").getBytes());
                out.flush();
                data = in.readLine();
                System.out.println("Received: " + data);
            }


            out.write(("QUIT\n").getBytes());
            out.flush();




            

        } catch (UnknownHostException e) {
            System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        } finally {
            if (s != null) try {
                s.close();
            } catch (IOException e) {
                System.out.println("close:" + e.getMessage());
            }
        }
    }
}
