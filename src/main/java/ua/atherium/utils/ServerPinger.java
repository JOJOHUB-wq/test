package ua.atherium.utils;

import javax.naming.Context;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;

public class ServerPinger {

    public static class PingResult {
        public boolean online;
        public String version;
        public String motd;
        public String favicon; // Base64
        public int players;
        public int maxPlayers;
        public long latency;
        public String error;
        public String ip;
        public int port;

        public PingResult(String ip, int port) {
            this.ip = ip;
            this.port = port;
            this.online = false;
        }
    }

    public PingResult ping(String address) {
        String[] parts = address.split(":");
        String host = parts[0];
        int port = 25565;
        if (parts.length > 1) {
            try {
                port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {}
        }

        if (parts.length == 1) {
            String[] srv = resolveSRV(host);
            if (srv != null) {
                host = srv[0];
                port = Integer.parseInt(srv[1]);
            }
        }

        PingResult result = new PingResult(host, port);
        long start = System.currentTimeMillis();

        try (Socket socket = new Socket()) {
            socket.setSoTimeout(2000);
            socket.connect(new InetSocketAddress(host, port), 2000);

            OutputStream out = socket.getOutputStream();
            DataOutputStream dataOut = new DataOutputStream(out);
            InputStream in = socket.getInputStream();
            DataInputStream dataIn = new DataInputStream(in);

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream handshake = new DataOutputStream(b);
            handshake.writeByte(0x00);
            writeVarInt(handshake, -1);
            writeString(handshake, host);
            handshake.writeShort(port);
            writeVarInt(handshake, 1);

            writeVarInt(dataOut, b.size());
            dataOut.write(b.toByteArray());

            dataOut.writeByte(0x01);
            dataOut.writeByte(0x00);

            readVarInt(dataIn);
            int id = readVarInt(dataIn);

            if (id == -1) throw new IOException("Premature end of stream");
            if (id != 0x00) throw new IOException("Invalid packetID");

            int length = readVarInt(dataIn);
            if (length == -1) throw new IOException("Premature end of stream");
            if (length == 0) throw new IOException("Invalid string length.");

            byte[] inBytes = new byte[length];
            dataIn.readFully(inBytes);
            String json = new String(inBytes, StandardCharsets.UTF_8);

            result.latency = System.currentTimeMillis() - start;
            result.online = true;

            result.version = extractJsonValue(json, "name");
            result.motd = extractMotd(json);
            result.favicon = extractJsonValue(json, "favicon");

            String onlineStr = extractJsonValue(json, "online");
            String maxStr = extractJsonValue(json, "max");

            if (onlineStr != null) result.players = Integer.parseInt(onlineStr);
            if (maxStr != null) result.maxPlayers = Integer.parseInt(maxStr);

        } catch (Exception e) {
            result.online = false;
            result.error = e.getMessage();
        }

        return result;
    }

    private String extractMotd(String json) {
        int descIndex = json.indexOf("\"description\"");
        if (descIndex == -1) return "A Minecraft Server";

        int colon = json.indexOf(":", descIndex);

        int start = colon + 1;
        while (Character.isWhitespace(json.charAt(start))) start++;

        if (json.charAt(start) == '"') {
            int end = json.indexOf("\"", start + 1);
            return json.substring(start + 1, end);
        } else if (json.charAt(start) == '{') {
            int textIndex = json.indexOf("\"text\"", start);
            if (textIndex != -1) {
                int textColon = json.indexOf(":", textIndex);
                int textStart = json.indexOf("\"", textColon) + 1; // Start quote
                int textEnd = json.indexOf("\"", textStart); // End quote
                return json.substring(textStart, textEnd);
            }
        }
        return "A Minecraft Server";
    }

    private String[] resolveSRV(String domain) {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
            env.put("java.naming.provider.url", "dns:");
            DirContext ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes("_minecraft._tcp." + domain, new String[]{"SRV"});
            if (attrs != null && attrs.get("SRV") != null) {
                String[] parts = attrs.get("SRV").get().toString().split(" ");
                String port = parts[2];
                String target = parts[3];
                if (target.endsWith(".")) target = target.substring(0, target.length() - 1);
                return new String[]{target, port};
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String extractJsonValue(String json, String key) {
        int index = json.indexOf("\"" + key + "\"");
        if (index == -1) return null;

        int start = json.indexOf(":", index) + 1;
        while (Character.isWhitespace(json.charAt(start))) start++;

        if (json.charAt(start) == '"') {
            int end = json.indexOf("\"", start + 1);
            return json.substring(start + 1, end);
        } else {
            int end = start;
            while (end < json.length() && (Character.isDigit(json.charAt(end)))) end++;
            return json.substring(start, end);
        }
    }

    private void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        while (true) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.writeByte(paramInt);
                return;
            }
            out.writeByte(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
    }

    private void writeString(DataOutputStream out, String string) throws IOException {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    private int readVarInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        while (true) {
            int k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) throw new RuntimeException("VarInt too big");
            if ((k & 0x80) != 128) break;
        }
        return i;
    }
}
