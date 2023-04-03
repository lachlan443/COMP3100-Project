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

            boolean moreJobs = true;
            boolean executeOnce = true;

            //For each job.
            while (moreJobs) {
                out.write(("REDY\n").getBytes());
                System.out.println("Sent: REDY");
                out.flush();


                data = in.readLine();
                System.out.println("Recieved: " + data);

            
                if (data.equals("NONE")) {
                    break;
                }
                else if (data.equals("JCPL")) {
                    continue;
                    //MIGHT NEED TO PRINT THE JCPL.
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
                        System.out.println("Sent: OK");
                        out.flush();


                        ArrayList<String> serverType = new ArrayList<String>();
                        ArrayList<String> serverID = new ArrayList<String>();
                        ArrayList<String> serverCores = new ArrayList<String>();
                        

                        for (int i = 0; i < nRecs; i++) {
                            data = in.readLine();
                            String serverStateInfo[] = data.split("\\s");
                            //serverStateInfo = {ServerType serverID state curStartTime core memory disk #wJobs #rJobs}

                            serverType.add(serverStateInfo[0]);
                            serverID.add(serverStateInfo[1]);
                            serverCores.add(serverStateInfo[4]);


                            //Schedule requires serverType and serverID
                            //Need to know cores too.


                            //Should find the largest and remove any from the list that are below the largest cores,
                            //then we have a list of servers to round robin.


                            


                            //Result should be a list of only the largest servers.

                            //Then loop through jobs, and assign to servers in round-robin.





                        }

                    
                        executeOnce = false;
                    }

                    //Find server with most core


                    //Schedule all jobs in round robin
                    //on all instances of that one server



                    

                    
                    
                    moreJobs = false;

                }


            }


            // if (data.equals("OK")) {
            //     out.write(("REDY\n").getBytes());
            //     System.out.println("Sent: REDY");
            //     out.flush();
            //     data = in.readLine();
            //     System.out.println("Received: " + data);
            // }

            //Send quit as the final step.
            out.write(("QUIT\n").getBytes());
            System.out.println("Sent: QUIT");
            out.flush();


            //Need to close the socket




            

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
