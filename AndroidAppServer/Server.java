import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.Date;
import java.lang.Math;

public class Server {

    private static Date serverStart;

    public static void main(String[] args) throws Exception {
        System.out.println("Vinyl server is running.");
        int clientNumber = 0;
        ServerSocket listener = new ServerSocket(9898);
        serverStart = new Date();
        try {
            while (true) {
                new Vinyl(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class Vinyl extends Thread {
        private Socket socket;
        private int clientNumber;

        public String[][] phrases = {
            // Standard greetings
            {"hi", "hello", "hi there", "greetings", "hey", "hey vinyl", "hi vinyl", "hello vinyl", "hi there vinyl", "greetings vinyl"},
            {"Hello Dan", "Hi Dan", "Hey Dan"},

            // Default
            {"Can you run that by me again?", "Not sure I got that one", "Uh... didn't exactly get that", "Can you say that again?", "Can you reword that, maybe?", "Don't know what to do with that", "Not sure I understand what you're saying"}
        };

        public Vinyl(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            log("New connection with client #" + clientNumber + " at " + socket);
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Connected to Vinyl successfully");
                out.println("Welcome back Dan, everything seems fine\r\n");

                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        break;
                    }
                    if (input.equals("what's the server uptime")) {
                        Date now = new Date();
                        long diff = now.getTime() - serverStart.getTime();
                        long diffSeconds = diff / 1000 % 60;
                        long diffMinutes = diff / (60 * 1000) % 60;
                        long diffHours = diff / (60 * 60 * 1000) % 60;
                        out.printf("Server has been running for: %dh %dm %ds\r\n", diffHours, diffMinutes, diffSeconds);
                    } else {
                        int i = 0;
                        byte response = 0;
                        while (response == 0) {
                            if (inArray(input.toLowerCase(), phrases[i*2])) {
                                response = 2;
                                int rand = (int)Math.floor(Math.random() * phrases[(i*2) + 1].length);
                                out.println(phrases[(i*2) + 1][rand]);
                            }
                            i++;
                            if (i * 2 == phrases.length - 1 && response == 0) {
                                log("not found");
                                response = 1;
                            }
                        }
                        if (response == 1) {
                            int rand = (int)Math.floor(Math.random() * phrases[phrases.length - 1].length);
                            out.println(phrases[phrases.length - 1][rand]);
                        }
                    }
                }
            } catch (IOException e) {
                log("Error handling client #" + clientNumber + ": " + e.toString());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log("Couldn't close a socket, something is very wrong...");
                } finally {
                    log("Connection with client #" + clientNumber + " closed");
                }
            }
        }

        private boolean inArray(String needle, String[] haystack) {
            boolean found = false;

            for (int i = 0; i < haystack.length; i++) {
                if (needle.equals(haystack[i])) {
                    found = true;
                    break;
                }
            }

            return found;
        }

        private void log(String message) {
            System.out.println(message);
        }
    }
}
