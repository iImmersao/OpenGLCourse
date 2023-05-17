import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MathUtil.*;
import static org.lwjgl.system.MemoryUtil.NULL;

//import static org.joml.Matrix4f;

public class OpenGLCourseApp {
    final static int WIDTH = 800;
    final static int HEIGHT = 600;

    final static float TO_RADIANS = 3.14159265f / 180.0f;

    static int VAO, VBO;
    static int shader;
    static int uniformModel;

    static boolean direction = true;
    static float triOffset = 0.0f;
    final static float triMaxoffset = 0.7f;
    final static float triIncrement = 0.005f;

    static float curAngle = 0.0f;

    static boolean sizeDirection = true;
    static float curSize = 0.4f;
    final static float maxSize = 0.8f;
    final static float minSize = 0.1f;

    // Vertex Shader
    final static String vShader = "                                      \n" +
    "#version 330                                                        \n" +
    "                                                                    \n" +
    "layout (location = 0) in vec3 pos;                                  \n" +
    "                                                                    \n" +
    "uniform mat4 model;                                                 \n" +
    "                                                                    \n" +
    "void main()                                                         \n" +
    "{                                                                   \n" +
    "    gl_Position = model * vec4(pos, 1.0);                           \n" +
    "}                                                                   \n"
    ;

    // Fragment Shader
    final static String fShader = "                                      \n" +
    "#version 330                                                        \n" +
    "                                                                    \n" +
    "out vec4 colour;                                                    \n" +
    "                                                                    \n" +
    "void main()                                                         \n" +
    "{                                                                   \n" +
    "    colour = vec4(1.0, 0.0, 0.0, 1.0);                              \n" +
    "}                                                                   \n"
    ;

    static void CreateTriangle() {
        float vertices[] = {
                -1.0f, -1.0f, 0.0f,
                1.0f, -1.0f, 0.0f,
                0.0f, 1.0f, 0.0f
        };

        VAO = glGenVertexArrays();
        glBindVertexArray(VAO);

        VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glBindVertexArray(0);
    }

    static void AddShader(int theProgram, String shaderCode, int shaderType) {
        int theShader = glCreateShader(shaderType);

        String[] theCode = new String[1];
        theCode[0] = shaderCode;

        int[] codeLength = new int[1];
        codeLength[0] = shaderCode.length();

        System.out.println(shaderCode);
        //glShaderSource(theShader, theCode);
        glShaderSource(theShader, shaderCode);
        glCompileShader(theShader);

        int[] result = { 0 };
        byte[] eLogRaw = new byte[1024];
        ByteBuffer eLog = ByteBuffer.wrap(eLogRaw);
        int[] intBufRaw = new int[1];
        IntBuffer intBuf = IntBuffer.wrap(intBufRaw);

        glGetShaderiv(theShader, GL_COMPILE_STATUS, result);
        if (result[0] != 1) {
            glGetProgramInfoLog(theShader, result, eLog);
            System.out.println("Error compiling the " + shaderType + " shader: " + eLog + "\n");
            return;
        }

        glAttachShader(theProgram, theShader);
    }

    static void CompileShaders() {
        shader = glCreateProgram();

        if (shader < 0) {
            System.out.println("Error creating shader program!");
            return;
        }

        AddShader(shader, vShader, GL_VERTEX_SHADER);
        AddShader(shader, fShader, GL_FRAGMENT_SHADER);

        //glBindAttribLocation(shader, 0, "vertices");

        int[] result = { 0 };
        byte[] eLogRaw = new byte[1024];
        ByteBuffer eLog = ByteBuffer.wrap(eLogRaw);
        int[] intBufRaw = new int[1];
        IntBuffer intBuf = IntBuffer.wrap(intBufRaw);

        glLinkProgram(shader);
        glGetProgramiv(shader, GL_LINK_STATUS, result);
        if (result[0] != 1) {
            glGetProgramInfoLog(shader, result, eLog);
            System.out.println("Error linking program: " + eLog);
            return;
        }

        glValidateProgram(shader);
        glGetProgramiv(shader, GL_VALIDATE_STATUS, result);
        if (result[0] != 1) {
            glGetProgramInfoLog(shader, result, eLog);
            System.out.println("Error validating program: " + eLog);
            return;
        }

        uniformModel = glGetUniformLocation(shader, "model");
    }

    public static void main(String args[]) {

        if (!GLFW.glfwInit()) {
            System.out.println("GLFW initialisation failed!");
            System.exit(1);
        }

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);

        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        long mainWindow = GLFW.glfwCreateWindow(WIDTH, HEIGHT, "Test Window", NULL, NULL);
        if (mainWindow == NULL) {
            System.out.println("GLFW window creation failed!");
            GLFW.glfwTerminate();
            System.exit(2);
        }

        int[] bufferWidth = new int[1];
        int[] bufferHeight = new int[1];
        GLFW.glfwGetFramebufferSize(mainWindow, bufferWidth, bufferHeight);

        GLFW.glfwMakeContextCurrent(mainWindow);

        // This line is essential! Without it, the native code crashes.
        GL.createCapabilities();

        GL33.glViewport(0, 0, bufferWidth[0], bufferHeight[0]);

        CreateTriangle();
        CompileShaders();

        while (!GLFW.glfwWindowShouldClose(mainWindow)) {
            GLFW.glfwPollEvents();

            if (direction) {
                triOffset += triIncrement;
            }
            else {
                triOffset -= triIncrement;
            }

            if (Math.abs(triOffset) >= triMaxoffset) {
                direction = !direction;
            }

            curAngle += 0.1f;
            if (curAngle >= 360) {
                curAngle -= 360;
            }

            if (sizeDirection) {
                curSize += 0.001f;
            }
            else {
                curSize -= 0.001f;
            }

            if (curSize >= maxSize || curSize <= minSize) {
                sizeDirection = !sizeDirection;
            }

            GL33.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);

            glUseProgram(shader);

            Matrix4f model = new Matrix4f();
            //model = model.rotate(curAngle * TO_RADIANS, 0.0f, 0.0f, 1.0f);
            model = model.translate(triOffset, 0.0f, 0.0f);
            model = model.scale(curSize, 0.4f, 1.0f);

            float[] modelArr = new float[16];
            glUniformMatrix4fv(uniformModel, false, model.get(modelArr));

            glBindVertexArray(VAO);

            glDrawArrays(GL_TRIANGLES, 0, 3);

            glBindVertexArray(0);

            glUseProgram(0);

            GLFW.glfwSwapBuffers(mainWindow);
        }
    }
}
