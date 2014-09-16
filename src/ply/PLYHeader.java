package ply;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple class for extracting the number of vertices and faces from a PLY file.
 * <p/>
 * Doesn't adhere to the flexible PLY format. Instead it expects a very specific format
 * designed for the scope of this project for speed.
 */
class PLYHeader {

    private final String headerContent;

    private int vertexCount = -1;
    private int faceCount = -1;
    private final int dataOffset;

    private static final Pattern endHeaderPattern = Pattern.compile("[\\s\\S]*?end_header.*?\n", Pattern.MULTILINE | Pattern.UNIX_LINES);

    public PLYHeader(final ByteBuffer buffer) throws InvalidFileException {

        byte[] tmp = new byte[1024];
        buffer.get(tmp);

        this.headerContent = isolateHeader(new String(tmp));
        this.dataOffset = this.headerContent.length();

        parse();
    }

    // Thanks for this, Ben
    private static String isolateHeader(final String input) throws InvalidFileException {
        Matcher m = endHeaderPattern.matcher(input);
        if (!m.find()) throw new InvalidFileException("PLY header could not be found");
        return m.group();
    }

    private void parse() throws InvalidFileException {
        StringTokenizer tokenizedHeader = new StringTokenizer(this.headerContent, "\n");

        try {
            String line = tokenizedHeader.nextToken();

            if (!line.startsWith("ply")) {
                throw new InvalidFileException("Magic number PLY not present.");
            }

            do {
                line = tokenizedHeader.nextToken().trim();
                String[] parts = line.split(" ");

                if (parts.length == 3 && parts[0].equals("element")) {
                    if (parts[1].equals("vertex")) {
                        this.vertexCount = Integer.parseInt(parts[2]);
                    } else if (parts[1].equals("face")) {
                        this.faceCount = Integer.parseInt(parts[2]);
                    }
                }
            } while (tokenizedHeader.hasMoreTokens());

            if (this.faceCount < 0 || this.vertexCount < 0) {
                throw new InvalidFileException("PLY file missing vertex or face count.");
            }

        } catch (NoSuchElementException e) {
            throw new InvalidFileException();
        }
    }

    public int getVertexCount() {
        return this.vertexCount;
    }

    public int getFaceCount() {
        return this.faceCount;
    }

    public int getDataOffset() {
        return this.dataOffset;
    }
}
