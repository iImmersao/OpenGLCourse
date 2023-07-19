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
    private Texture plainTexture;

    private Material shinyMaterial;
    private Material dullMaterial;

    private Model xwing;
    private Model blackhawk;
    private Model tooth30;

    private DirectionalLight mainLight;
    private PointLight[] pointLights = new PointLight[CommonValues.MAX_POINT_LIGHTS];
    private SpotLight[] spotLights = new SpotLight[CommonValues.MAX_SPOT_LIGHTS];

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

        int[] floorIndices = {
                0, 2, 1,
                1, 2, 3
        };

        float[] floorVertices = {
                -10.0f, 0.0f, -10.f,   0.0f, 0.0f,        0.0f, -1.0f, 0.0f,
                10.0f, 0.0f, -10.f,    10.0f, 0.0f,       0.0f, -1.0f, 0.0f,
                -10.0f, 0.0f, 10.0f,   0.0f, 10.0f,       0.0f, -1.0f, 0.0f,
                10.0f, 0.0f, 10.0f,    10.0f, 10.0f,      0.0f, -1.0f, 0.0f
        };

        calcAverageNormals(indices, vertices, 8, 5);

        Mesh obj1 = new Mesh();
        obj1.createMesh(vertices, indices);
        meshList.add(obj1);

        Mesh obj2 = new Mesh();
        obj2.createMesh(vertices, indices);
        meshList.add(obj2);

        Mesh obj3 = new Mesh();
        obj3.createMesh(floorVertices, floorIndices);
        meshList.add(obj3);
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
        brickTexture.loadTextureA();
        dirtTexture = new Texture("Textures/dirt.png");
        dirtTexture.loadTextureA();
        plainTexture = new Texture("Textures/plain.png");
        plainTexture.loadTextureA();

        shinyMaterial = new Material(4.0f, 256);
        dullMaterial = new Material(0.3f, 4);

        xwing = new Model();
        xwing.loadModel("D:/gitrepos/OpenGLCourseApp/src/main/resources/Models/x-wing.obj");

        blackhawk = new Model();
        blackhawk.loadModel("D:/gitrepos/OpenGLCourseApp/src/main/resources/Models/uh60.obj");

        tooth30 = new Model();
        tooth30.loadModel("D:/gitrepos/OpenGLCourseApp/src/main/resources/Models/Lower_Right_First_Molar_30_Enamel.obj");
        mainLight = new DirectionalLight(1.0f, 1.0f, 1.0f,
                0.2f, 0.4f,
                0.0f, 0.0f, -1.0f);

        int pointLightCount = 0;
        pointLights[0] = new PointLight(0.0f, 0.0f, 1.0f,
                0.0f, 1.0f,
                0.0f, 0.0f, 0.0f,
                0.3f, 0.2f, 0.1f);
        pointLightCount++;
        pointLights[1] = new PointLight(0.0f, 1.0f, 0.0f,
                0.0f, 1.0f,
                -4.0f, 2.0f, 0.0f,
                0.3f, 0.1f, 0.1f);
        pointLightCount++;

        int spotLightCount = 0;
        spotLights[0] = new SpotLight(1.0f, 1.0f, 1.0f,
                0.0f, 2.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                20.0f);
        spotLightCount++;
        spotLights[1] = new SpotLight(1.0f, 1.0f, 1.0f,
                0.0f, 1.0f,
                0.0f, 1.5f, 0.0f,
                -2.0f, -1.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                20.0f);
        spotLightCount++;

        int uniformModel, uniformProjection, uniformView, uniformEyePosition,
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
            uniformEyePosition = shader.getUniformEyePosition();
            uniformSpecaularIntensity = shader.getUniformSpecularIntensity();
            uniformShininess = shader.getUniformShininess();

            Vector3f lowerLight = new Vector3f(camera.getPosition());
            lowerLight.y -= 0.3f;
            //spotLights[0].setFlash(lowerLight, camera.getDirection());

            shader.setDirectionalLight(mainLight);
            shader.setPointLights(pointLights, pointLightCount);
            shader.setSpotLights(spotLights, spotLightCount);

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

            model = new Matrix4f();
            model = model.translate(0.0f, -2.0f, 0.0f);
            //model = model.scale(0.4f, 0.4f, 1.0f);
            glUniformMatrix4fv(uniformModel, false, model.get(modelArr));
            dirtTexture.useTexture();
            shinyMaterial.useMaterial(uniformSpecaularIntensity, uniformShininess);
            meshList.get(2).renderMesh();

            // X-Wing
            /*
                         */
            model = new Matrix4f();
            model = model.translate(-7.0f, 0.0f, 10.0f);
            model = model.scale(0.006f, 0.006f, 0.006f);
            glUniformMatrix4fv(uniformModel, false, model.get(modelArr));
            dirtTexture.useTexture();
            shinyMaterial.useMaterial(uniformSpecaularIntensity, uniformShininess);
            xwing.renderModel();

            // Blackhawk
            model = new Matrix4f();
            model = model.translate(-3.0f, 2.0f, 0.0f);
            model = model.rotate((float)Math.toRadians(-90.0f), new Vector3f(1.0f, 0.0f, 0.0f));
            model = model.scale(0.4f, 0.4f, 0.4f);
            glUniformMatrix4fv(uniformModel, false, model.get(modelArr));
            dirtTexture.useTexture();
            shinyMaterial.useMaterial(uniformSpecaularIntensity, uniformShininess);
            blackhawk.renderModel();

            // Tooth30
            model = new Matrix4f();
            model = model.translate(-10.0f, 2.0f, 10.0f);
            model = model.rotate((float)Math.toRadians(-90.0f), new Vector3f(1.0f, 0.0f, 0.0f));
            model = model.scale(0.05f, 0.05f, 0.05f);
            glUniformMatrix4fv(uniformModel, false, model.get(modelArr));
            dirtTexture.useTexture();
            shinyMaterial.useMaterial(uniformSpecaularIntensity, uniformShininess);
            tooth30.renderModel();

            glUseProgram(0);

            mainWindow.swapBuffers();
        }
    }

    public static void main(String[] args) {
        OpenGLCourseApp app = new OpenGLCourseApp();
        app.run();
    }
}
