import org.joml.Matrix4f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

//import static org.joml.Matrix4f;

public class OpenGLCourseApp {

    // Vertex Shader
    final static String vShader = "shader.vert";

    // Fragment Shader
    final static String fShader = "shader.frag";

    private List<Mesh> meshList = new LinkedList<>();

    private List<Shader> shaderList = new LinkedList<>();

    private Window mainWindow;

    void CreateObjects() {
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
        mainWindow = new Window(800, 600);
        mainWindow.initialise();

        CreateObjects();
        CreateShaders();;

        int uniformModel, uniformProjection;

        Matrix4f projection = new Matrix4f();
        projection.setPerspective(45.0f, (float)mainWindow.getBufferWidth()/(float)mainWindow.getBufferHeight(), 0.1f, 100.0f);

        while (!mainWindow.getShouldClose()) {
            GLFW.glfwPollEvents();

            GL33.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            Shader shader = shaderList.get(0);
            shader.useShader();
            uniformModel = shader.getUniformModel();
            uniformProjection = shader.getUniformProjection();

            Matrix4f model = new Matrix4f();
            model = model.translate(0.0f, 0.0f, -2.5f);
            model = model.scale(0.4f, 0.4f, 1.0f);

            float[] modelArr = new float[16];
            glUniformMatrix4fv(uniformModel, false, model.get(modelArr));
            float[] perspectiveArr = new float[16];
            glUniformMatrix4fv(uniformProjection, false, projection.get(perspectiveArr));

            meshList.get(0).renderMesh();

            model = new Matrix4f();
            model = model.translate(0.0f, 1.0f, -2.5f);
            model = model.scale(0.4f, 0.4f, 1.0f);
            glUniformMatrix4fv(uniformModel, false, model.get(modelArr));
            meshList.get(1).renderMesh();

            glUseProgram(0);

            mainWindow.swapBuffers();
        }
    }

    public static void main(String[] args) {
        OpenGLCourseApp app = new OpenGLCourseApp();
        app.run();
    }
}
