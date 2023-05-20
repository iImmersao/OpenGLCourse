import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.LinkedList;
import java.util.List;

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

    static int VAO, VBO, IBO;
    static int shader;
    static int uniformModel, uniformProjection;

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
    final static String vShader = "shader.vert";

    // Fragment Shader
    final static String fShader = "shader.frag";

    private List<Mesh> meshList = new LinkedList<>();

    private List<Shader> shaderList = new LinkedList<>();

    void CreateTriangle() {
        /*
         */
        int indices[] = {
                0, 3, 1,
                1, 3, 2,
                2, 3, 0,
                0, 1, 2
        };


        float vertices[] = {
                -1.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 1.0f,
                1.0f, -1.0f, 0.0f,
                0.0f, 1.0f, 0.0f
        };

        Mesh obj1 = new Mesh();
        obj1.createMesh(vertices, indices);
        meshList.add(obj1);

        Mesh obj2 = new Mesh();
        obj2.createMesh(vertices, indices);
        meshList.add(obj2);
    }

    private void CreateShaders() {
        Shader shader1 = new Shader();
        shader1.createFromFiles(vShader, fShader);
        shaderList.add(shader1);
    }
    public void run() {

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

        glEnable(GL_DEPTH_TEST);

        GL33.glViewport(0, 0, bufferWidth[0], bufferHeight[0]);

        CreateTriangle();
        CreateShaders();;

        Matrix4f projection = new Matrix4f();
        projection.setPerspective(45.0f, (float)bufferWidth[0]/(float)bufferHeight[0], 0.1f, 100.0f);

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
            GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            Shader shader = shaderList.get(0);
            shader.useShader();
            uniformModel = shader.getUniformModel();
            uniformProjection = shader.getUniformProjection();

            Matrix4f model = new Matrix4f();
            //model = model.rotate(curAngle * TO_RADIANS, 0.0f, 1.0f, 0.0f);
            model = model.translate(triOffset, 0.0f, -2.5f);
            model = model.scale(0.4f, 0.4f, 1.0f);

            float[] modelArr = new float[16];
            glUniformMatrix4fv(uniformModel, false, model.get(modelArr));
            /*
             */
            float[] perspectiveArr = new float[16];
            glUniformMatrix4fv(uniformProjection, false, projection.get(perspectiveArr));

            meshList.get(0).renderMesh();

            model = new Matrix4f();
            model = model.translate(-triOffset, 1.0f, -2.5f);
            model = model.scale(0.4f, 0.4f, 1.0f);
            glUniformMatrix4fv(uniformModel, false, model.get(modelArr));
            meshList.get(1).renderMesh();

            glUseProgram(0);

            GLFW.glfwSwapBuffers(mainWindow);
        }
    }

    public static void main(String[] args) {
        OpenGLCourseApp app = new OpenGLCourseApp();
        app.run();
    }
}
