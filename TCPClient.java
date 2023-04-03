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
            //DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            String data;

            //Start of the 3 way handshake, client sends HELO
            out.write(("HELO\n").getBytes());
            System.out.println("Sent: HELO");
            out.flush();
            data = in.readLine();
            System.out.println("Received: " + data);

            //Server responds OK, client sends AUTH.
            if (data.equals("OK")) {
                out.write(("AUTH vboxuser\n").getBytes());
                System.out.println("Sent: AUTH 47156074");
                out.flush();
                data = in.readLine();
                System.out.println("Received: " + data);
            }

            boolean executeOnce = true;
            String largestServer = null;
            int largestServerCount = 0;
            int largestServerCores = 0;

            //7 is the number of parameters in a JOBN request.
            //JOBN submitTime jobID estRuntime core memory disk
            String[] request = new String[7];
            int jobID = 0;
            int currentServerID = 0;

            //For each job.
            while (true) {
                out.write(("REDY\n").getBytes());
                System.out.println("Sent: REDY");
                out.flush();

                data = in.readLine();
                System.out.println("Recieved: " + data);
                //Split the response by the whitespaces.
                request = data.split("\\s");

            
                if (data.equals("NONE")) {
                    break;
                }
                else if (data.equals("JCPL")) {
                    continue;
                }
                else { //Response must be JOBN.
                
                    if (executeOnce) {
                        out.write(("GETS All\n").getBytes());
                        System.out.println("Sent: GETS All");
                        out.flush();
                        //Recieve DATA, then send OK.
                        data = in.readLine();
                        System.out.println("Recieved: " + data);

                    
                        String msgSplit[] = data.split("\\s");  //Split string by whitespaces
                        //msgSplit = {DATA nRecs recLen}
                        int nRecs = Integer.parseInt(msgSplit[1]);

                        out.write(("OK\n").getBytes());
                        System.out.println("Sent: OK1");
                        out.flush();

                        
                        for (int i = 0; i < nRecs; i++) {
                            data = in.readLine();
                            String serverStateInfo[] = data.split("\\s");
                            //serverStateInfo = {ServerType serverID state curStartTime core memory disk #wJobs #rJobs}

                            //As we iterate though servers, find the largest.
                            boolean serverWithMoreCores = Integer.parseInt(serverStateInfo[4]) > largestServerCores;
                            boolean sameCores = Integer.parseInt(serverStateInfo[4]) == largestServerCores;
                            boolean sameName = serverStateInfo[0].equals(largestServer);


                            if (serverWithMoreCores) {
                                largestServer = serverStateInfo[0];
                                largestServerCount = 1;
                                largestServerCores = Integer.parseInt(serverStateInfo[4]);
                            }
                            //Increment server count if another server of same type is found.
                            if (sameCores && sameName) {
                                largestServerCount++;
                            }
                        }

                        // System.out.println(largestServer);
                        //     System.out.println(largestServerCount);
                        //     System.out.println(largestServerCores);

                        out.write(("OK\n").getBytes());
                        System.out.println("Sent: OK2");
                        out.flush();

                        data = in.readLine();
                        System.out.println("Recieved: " + data);

                        executeOnce = false;
                    }

                    //Schedule the job, and then continue the loop.
                    //SCHD jobID serverType serverID
                    //currentServerID is initialised to 0.


                    if (request[0].equals("JOBN")) {
                        //JOBN submitTime jobID estRuntime core memory disk
                        jobID = Integer.parseInt(request[2]);

                        out.write(("SCHD "+jobID+" "+largestServer+" "+currentServerID+"\n").getBytes());
                        System.out.println("SCHD "+jobID+" "+largestServer+" "+currentServerID);
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
            }
            System.out.println(largestServerCount);



            //Send quit as the final step.
            out.write(("QUIT\n").getBytes());
            System.out.println("Sent: QUIT");
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
