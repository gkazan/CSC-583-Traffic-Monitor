
import java.io.*;
import java.net.Socket;

/**
 * CSC 583
 * AXIS IP Camera Traffic Monitor
 * Developed by George Kazanjian, Tyler Dungan, and Finn Moussa
 */
public class Main {

    private static boolean canContinue = true;

    public static void main(String[] args) {

        String ipAddress = "166.247.77.253";
        int port = 81;

        long startTime = System.currentTimeMillis();
        long totalBytesReceived = 0;
        long maxThroughput = Long.MIN_VALUE;
        long minThroughput = Long.MAX_VALUE;
        long numSamples = 0;

        createInputThread();

        try {
            Socket socket = new Socket(ipAddress, port);
            OutputStream outputStream = socket.getOutputStream();

            String request = "GET /axis-cgi/mjpg/video.cgi HTTP/1.0\r\n";
            request += "\r\n";
            outputStream.write(request.getBytes());

            byte[] buffer = new byte[1024];
            int bytesRead;
            boolean headerSkipped = false;

            while (canContinue && (bytesRead = socket.getInputStream().read(buffer)) != -1) {
                if (!headerSkipped) {
                    // Skip the HTTP response header
                    String response = new String(buffer, 0, bytesRead);
                    int headerEndIndex = response.indexOf("\r\n\r\n");
                    if (headerEndIndex != -1) {
                        bytesRead -= (headerEndIndex + 4);
                        System.arraycopy(buffer, headerEndIndex + 4, buffer, 0, bytesRead);
                        headerSkipped = true;
                    } else {
                        continue;
                    }
                }

                totalBytesReceived += bytesRead;
                numSamples++;

                long currentTime = System.currentTimeMillis();
                long duration = currentTime - startTime;
                double throughput = (double) bytesRead / duration * 1000;

                maxThroughput = Math.max(maxThroughput, (long) throughput);
                minThroughput = Math.min(minThroughput, (long) throughput);

                processReceivedData(buffer, bytesRead);
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            double throughput = (double) totalBytesReceived / duration * 1000; // Bytes per second
            double averageThroughput = (double) totalBytesReceived / numSamples / 1000; // Kilobytes per sample

            System.out.println("Camera's throughput consumption: " + throughput + " bytes/s");

            System.out.println("Total throughput consumption: " + totalBytesReceived + " bytes");
            System.out.println("Average throughput: " + averageThroughput + " bytes/s");
            System.out.println("Maximum throughput: " + maxThroughput + " bytes/s");
            System.out.println("Minimum throughput: " + minThroughput + " bytes/s");

            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createInputThread() {
        Thread userInputThread = new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                reader.readLine();
                canContinue = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        userInputThread.start();
    }

    private static String base64Encode(String text) {
        return java.util.Base64.getEncoder().encodeToString(text.getBytes());
    }

    private static void processReceivedData(byte[] data, int length) {
//        System.out.println("Received data: " + new String(data, 0, length));
    }
}
