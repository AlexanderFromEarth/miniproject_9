package edu.coursera.distributed;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A basic and very limited implementation of a file server that responds to GET
 * requests from HTTP clients.
 */
public final class FileServer {
    /**
     * Main entrypoint for the basic file server.
     *
     * @param socket Provided socket to accept connections on.
     * @param fs A proxy filesystem to serve files from. See the PCDPFilesystem
     *           class for more detailed documentation of its usage.
     * @throws IOException If an I/O error is detected on the server. This
     *                     should be a fatal error, your file server
     *                     implementation is not expected to ever throw
     *                     IOExceptions during normal operation.
     */
    public void run(final ServerSocket socket, final PCDPFilesystem fs)
            throws IOException {
        Pattern pattern = Pattern.compile("GET ((/[a-zA-Z0-9.]+)+) HTTP/1.1");

        while (true) {
            Socket connectionSocket = socket.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream writer = new DataOutputStream(connectionSocket.getOutputStream());
            Matcher matcher = pattern.matcher(reader.readLine());

            try {
                if (!matcher.matches()) {
                    throw new Exception("Not Found");
                }

                PCDPPath path = new PCDPPath(matcher.group(1));
                String fileContents = fs.readFile(path);

                if (fileContents == null) {
                    throw new Exception("Not Found");
                }

                writer.writeBytes("HTTP/1.0 200 OK\r\nServer: FileServer\r\n\r\n" + fileContents + "\r\n");
            } catch (Exception e) {
                writer.writeBytes("HTTP/1.0 404 Not Found\r\nServer: FileServer\r\n\r\n");
            }

            connectionSocket.close();
        }
    }
}
