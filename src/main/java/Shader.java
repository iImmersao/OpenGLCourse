import java.io.*;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;

public class Shader {
    private int shaderID;
    private int uniformModel;
    private int uniformProjection;

    private int uniformView;

    public void createFromString(String vertexCode, String fragmentCode) {
        compileShader(vertexCode, fragmentCode);
    }

    public void createFromFiles(String vertexFilename, String fragmentFilename) {
        String vertexString = readFile(vertexFilename);
        String fragmentString = readFile(fragmentFilename);
        compileShader(vertexString, fragmentString);
    }

    private String readFile(String fileName) {
        StringBuilder content = new StringBuilder();
        String line;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = classLoader.getResourceAsStream(fileName);
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));

        try {
            while (((line = buf.readLine()) != null)) {
                content.append(line);
                content.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content.toString();
    }

    static void AddShader(int theProgram, String shaderCode, int shaderType) {
        int theShader = glCreateShader(shaderType);

        System.out.println(shaderCode);
        glShaderSource(theShader, shaderCode);
        glCompileShader(theShader);

        int[] result = { 0 };
        byte[] eLogRaw = new byte[1024];
        ByteBuffer eLog = ByteBuffer.wrap(eLogRaw);

        glGetShaderiv(theShader, GL_COMPILE_STATUS, result);
        if (result[0] != 1) {
            glGetProgramInfoLog(theShader, result, eLog);
            System.out.println("Error compiling the " + shaderType + " shader: " + eLog + "\n");
            return;
        }

        glAttachShader(theProgram, theShader);
    }

    public void compileShader(String vertexCode, String fragmentCode) {
        shaderID = glCreateProgram();

        if (shaderID < 0) {
            System.out.println("Error creating shader program!");
            return;
        }

        AddShader(shaderID, vertexCode, GL_VERTEX_SHADER);
        AddShader(shaderID, fragmentCode, GL_FRAGMENT_SHADER);

        int[] result = { 0 };
        byte[] eLogRaw = new byte[1024];
        ByteBuffer eLog = ByteBuffer.wrap(eLogRaw);

        glLinkProgram(shaderID);
        glGetProgramiv(shaderID, GL_LINK_STATUS, result);
        if (result[0] != 1) {
            glGetProgramInfoLog(shaderID, result, eLog);
            System.out.println("Error linking program: " + eLog);
            return;
        }

        glValidateProgram(shaderID);
        glGetProgramiv(shaderID, GL_VALIDATE_STATUS, result);
        if (result[0] != 1) {
            glGetProgramInfoLog(shaderID, result, eLog);
            System.out.println("Error validating program: " + eLog);
            return;
        }

        uniformModel = glGetUniformLocation(shaderID, "model");
        uniformProjection = glGetUniformLocation(shaderID, "projection");
        uniformView = glGetUniformLocation(shaderID, "view");
    }

    public void useShader() {
        glUseProgram(shaderID);
    }

    public void clearShader() {
        if (shaderID != 0) {
            glDeleteProgram(shaderID);
            shaderID = 0;
        }

        uniformModel = 0;
        uniformProjection = 0;
        uniformView = 0;
    }

    public int getUniformModel() {
        return uniformModel;
    }

    public int getUniformProjection() {
        return uniformProjection;
    }

    public int getUniformView() { return uniformView; }
}
