import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class OpenGLCourseApp {

    private float deltaTime = 0.0f;

    private float lastTime = 0.0f;

    // Vertex Shader
    final static String vShader = "shader.vert";

    // Fragment Shader
    final static String fShader = "shader.frag";

    private final List<Mesh> meshList = new LinkedList<>();

    private final List<Shader> shaderList = new LinkedList<>();

    private Camera camera;

    private Window mainWindow;

    private Texture brickTexture;
    private Texture dirtTexture;

    private Material shinyMaterial;
    private Material dullMaterial;

    private Light mainLight;

    public void calcAverageNormals(int[] indices, float[] vertices, int vLength, int normalOffset) {
        for (int i = 0; i < indices.length; i += 3) {
            int in0 = indices[i] * vLength;
            int in1 = indices[i + 1] * vLength;
            int in2 = indices[i + 2] * vLength;
            Vector3f v1 = new Vector3f(vertices[in1] - vertices[in0], vertices[in1 + 1] - vertices[in0 + 1], vertices[in1 + 2] - vertices[in0 + 2]);
            Vector3f v2 = new Vector3f(vertices[in2] - vertices[in0], vertices[in2 + 1] - vertices[in0 + 1], vertices[in2 + 2] - vertices[in0 + 2]);
            Vector3f normal = new Vector3f();
            v1.cross(v2, normal);
            normal.normalize();

            in0 += normalOffset; in1 += normalOffset; in2 += normalOffset;
            vertices[in0] += normal.x; vertices[in0 + 1] += normal.y; vertices[in0 + 2] += normal.z;
            vertices[in1] += normal.x; vertices[in1 + 1] += normal.y; vertices[in1 + 2] += normal.z;
            vertices[in2] += normal.x; vertices[in2 + 1] += normal.y; vertices[in2 + 2] += normal.z;
        }

        for (int i = 0; i < vertices.length / vLength; i++) {
            int nOffset = i * vLength + normalOffset;
            Vector3f vec = new Vector3f(vertices[nOffset], vertices[nOffset + 1], vertices[nOffset + 2]);
            vec.normalize();
            vertices[nOffset] = vec.x; vertices[nOffset + 1] = vec.y; vertices[nOffset + 2] = vec.z;
        }
    }

    void CreateObjects() {
        int[] indices = {
                0, 3, 1,
                1, 3, 2,
                2, 3, 0,
                0, 1, 2
        };

        float[] vertices = {
                -1.0f, -1.0f, -0.6f,     0.0f, 0.0f,     0.0f, 0.0f, 0.0f,
                0.0f, -1.0f, 1.0f,      0.5f, 0.0f,     0.0f, 0.0f, 0.0f,
                1.0f, -1.0f, -0.6f,      1.0f, 0.0f,     0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f,       0.5f, 1.0f,     0.0f, 0.0f, 0.0f
        };

        calcAverageNormals(indices, vertices, 8, 5);

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
        mainWindow = new Window(1368, 768);
        mainWindow.initialise();

        CreateObjects();
        CreateShaders();

        camera = new Camera(new Vector3f(0.0f, 0.0f, 0.0f),
                new Vector3f(0.0f, 1.0f, 0.0f),
                -90.0f, 0.0f, 5.0f, 0.5f);

        brickTexture = new Texture("Textures/brick.png");
        brickTexture.loadTexture();
        dirtTexture = new Texture("Textures/dirt.png");
        dirtTexture.loadTexture();

        shinyMaterial = new Material(1.0f, 32);
        dullMaterial = new Material(0.3f, 4);

        mainLight = new Light(1.0f, 1.0f, 1.0f, 0.1f,
                2.0f, -1.0f, -2.0f, 0.1f);

        int uniformModel, uniformProjection, uniformView, uniformEyePosition,
                uniformAmbientIntensity, uniformAmbientColour, uniformDirection, uniformDiffuseIntensity,
                uniformSpecaularIntensity, uniformShininess;

        Matrix4f projection = new Matrix4f();
        projection.setPerspective((float) Math.toRadians(45.0f), (float)mainWindow.getBufferWidth()/(float)mainWindow.getBufferHeight(),
                0.1f, 100.0f);

        while (!mainWindow.getShouldClose()) {
            float now = (float) glfwGetTime();
            deltaTime = now - lastTime;
            lastTime = now;

            GLFW.glfwPollEvents();

            camera.keyControl(mainWindow.getKeys(), deltaTime);
            camera.mouseControl(mainWindow.getXChange(), mainWindow.getYChange());

            GL33.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            Shader shader = shaderList.get(0);
            shader.useShader();
            uniformModel = shader.getUniformModel();
            uniformProjection = shader.getUniformProjection();
            uniformView = shader.getUniformView();
            uniformAmbientColour = shader.getUniformAmbientColour();
            uniformAmbientIntensity = shader.getUniformAmbientIntensity();
            uniformDirection = shader.getUniformDirection();
            uniformDiffuseIntensity = shader.getUniformDiffuseIntensity();
            uniformEyePosition = shader.getUniformEyePosition();
            uniformSpecaularIntensity = shader.getUniformSpecularIntensity();
            uniformShininess = shader.getUniformShininess();

            mainLight.useLight(uniformAmbientIntensity, uniformAmbientColour, uniformDiffuseIntensity, uniformDirection);

            float[] perspectiveArr = new float[16];
            glUniformMatrix4fv(uniformProjection, false, projection.get(perspectiveArr));
            float[] viewArr = new float[16];
            glUniformMatrix4fv(uniformView, false, camera.calculateViewMatrix().get(viewArr));
            glUniform3f(uniformEyePosition, camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);

            Matrix4f model = new Matrix4f();
            model = model.translate(0.0f, 0.0f, -2.5f);
            //model = model.scale(0.4f, 0.4f, 1.0f);

            float[] modelArr = new float[16];
            glUniformMatrix4fv(uniformModel, false, model.get(modelArr));

            brickTexture.useTexture();
            shinyMaterial.useMaterial(uniformSpecaularIntensity, uniformShininess);
            meshList.get(0).renderMesh();

            model = new Matrix4f();
            model = model.translate(0.0f, 4.0f, -2.5f);
            //model = model.scale(0.4f, 0.4f, 1.0f);
            glUniformMatrix4fv(uniformModel, false, model.get(modelArr));
            dirtTexture.useTexture();
            dullMaterial.useMaterial(uniformSpecaularIntensity, uniformShininess);
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
