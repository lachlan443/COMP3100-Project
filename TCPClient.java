import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class TCPClient {
    public static void main(String args[]) {
        // arguments supply message and hostname of destination
        Socket s = null;
        try {
            int serverPort = 50000;
            s = new Socket("127.0.0.1", serverPort);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            String data;

            boolean executeOnce = true;
            String largestServer = null;
            int largestServerCount = 0;
            int largestServerCores = 0;
            //7 is the number of parameters in a JOBN request.
            //JOBN submitTime jobID estRuntime core memory disk
            String[] request = new String[7];
            int jobID = 0;
            int currentServerID = 0;


            //Start of the 3 way handshake, client sends HELO
            out.write(("HELO\n").getBytes());
            out.flush();
            data = in.readLine();
            //Server responds OK, client sends AUTH.
            if (data.equals("OK")) {
                out.write(("AUTH "+System.getProperty("user.name")+"\n").getBytes());
                out.flush();
                data = in.readLine();
            }


            //For each job.
            while (true) {
                out.write(("REDY\n").getBytes());
                out.flush();

                data = in.readLine();
                //Split the response by the whitespaces.
                request = data.split("\\s");

            
                if (data.equals("NONE")) {
                    break;
                }
                //Response is either JCPL or JOBN
                
                if (executeOnce) {
                    out.write(("GETS All\n").getBytes());
                    out.flush();

                    //Recieve DATA, then send OK.
                    data = in.readLine();
                    out.write(("OK\n").getBytes());
                    out.flush();

                    //msgSplit = {DATA nRecs recLen}
                    String msgSplit[] = data.split("\\s");  //Split string by whitespaces
                    int nRecs = Integer.parseInt(msgSplit[1]);


                    for (int i = 0; i < nRecs; i++) {
                        //As we iterate though servers, find the largest.
                        data = in.readLine();

                        //serverStateInfo = {ServerType serverID state curStartTime core memory disk #wJobs #rJobs}
                        String serverStateInfo[] = data.split("\\s");
                        boolean foundServerWithMoreCores = Integer.parseInt(serverStateInfo[4]) > largestServerCores;
                        boolean sameCores = Integer.parseInt(serverStateInfo[4]) == largestServerCores;
                        boolean sameName = serverStateInfo[0].equals(largestServer);

                        if (foundServerWithMoreCores) {
                            largestServer = serverStateInfo[0];
                            largestServerCount = 1;
                            largestServerCores = Integer.parseInt(serverStateInfo[4]);
                        }
                        //Increment server count if another server of same type is found.
                        if (sameCores && sameName) {
                            largestServerCount++;
                        }
                    }

                    out.write(("OK\n").getBytes());
                    out.flush();
                    data = in.readLine();
                    executeOnce = false;
                }

                if (request[0].equals("JOBN")) {
                    //Request = {JOBN submitTime jobID estRuntime core memory disk}
                    jobID = Integer.parseInt(request[2]);

                    out.write(("SCHD "+jobID+" "+largestServer+" "+currentServerID+"\n").getBytes());
                    out.flush();

                    //Round robin through all largest servers
                    currentServerID++;
                    if (currentServerID >= largestServerCount) {
                        currentServerID = 0;
                    }

                    //Wait for response from server.
                    while (data == in.readLine()) {
                        continue;
                    }
                }
            }
            //Send quit as the final step.
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
                s.close(); //Close the socket
            } catch (IOException e) {
                System.out.println("close:" + e.getMessage());
            }
        }
    }
}
