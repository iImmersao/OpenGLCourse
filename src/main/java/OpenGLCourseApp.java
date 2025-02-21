import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_L;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL33.*;

public class OpenGLCourseApp {

    private float deltaTime = 0.0f;

    private float lastTime = 0.0f;

    private float blackhawkAngle = 0.0f;

    // Vertex Shader
    final static String vShader = "shader.vert";

    // Fragment Shader
    final static String fShader = "shader.frag";

    private final List<Mesh> meshList = new LinkedList<>();

    private final List<Shader> shaderList = new LinkedList<>();

    Shader directionalShadowShader;
    Shader omniShadowShader;

    private int uniformProjection = 0;
    private int uniformModel = 0;
    private int uniformView = 0;
    private int uniformEyePosition = 0;
    private int uniformSpecularIntensity = 0;
    private int uniformShininess = 0;

    private int uniformDirectionalLightTransform = 0;
    private int uniformOmniLightPos = 0;
    private int uniformFarPlane = 0;

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
    private Model sphere;
    private Model eyeball;
    private Model airliner;

    private DirectionalLight mainLight;
    private PointLight[] pointLights = new PointLight[CommonValues.MAX_POINT_LIGHTS];
    private SpotLight[] spotLights = new SpotLight[CommonValues.MAX_SPOT_LIGHTS];

    private Skybox skybox;

    private int pointLightCount = 0;
    private int spotLightCount = 0;

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

        /*
        float[] floorVertices = {
                -20.0f, 0.0f, -20.f,   0.0f, 0.0f,        0.0f, -1.0f, 0.0f,
                20.0f, 0.0f, -20.f,    20.0f, 0.0f,       0.0f, -1.0f, 0.0f,
                -20.0f, 0.0f, 20.0f,   0.0f, 20.0f,       0.0f, -1.0f, 0.0f,
                20.0f, 0.0f, 20.0f,    20.0f, 20.0f,      0.0f, -1.0f, 0.0f
        };
         */

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

        directionalShadowShader = new Shader();
        directionalShadowShader.createFromFiles("directional_shadow_map.vert", "directional_shadow_map.frag");
        omniShadowShader = new Shader();
        omniShadowShader.createFromFiles("omni_shadow_map.vert", "omni_shadow_map.geom", "omni_shadow_map.frag");
    }

    void RenderScene()
    {
        Matrix4f model = new Matrix4f(); // Should be identity matrix?

        float[] modelArr = new float[16];

        model = model.translate(new Vector3f(0.0f, 0.0f, -2.5f));
        //model = glm::scale(model, glm::vec3(0.4f, 0.4f, 1.0f));
        glUniformMatrix4fv(uniformModel, false, model.get(modelArr));
        brickTexture.useTexture();
        shinyMaterial.useMaterial(uniformSpecularIntensity, uniformShininess);
        meshList.get(0).renderMesh();

        model = new Matrix4f();
        model = model.translate(new Vector3f(0.0f, 4.0f, -2.5f));
        //model = glm::scale(model, glm::vec3(0.4f, 0.4f, 1.0f));
        glUniformMatrix4fv(uniformModel, false, model.get(modelArr));
        dirtTexture.useTexture();
        dullMaterial.useMaterial(uniformSpecularIntensity, uniformShininess);
        meshList.get(1).renderMesh();

        model = new Matrix4f();
        model = model.translate(new Vector3f(0.0f, -2.0f, 0.0f));
        //model = glm::scale(model, glm::vec3(0.4f, 0.4f, 1.0f));
        glUniformMatrix4fv(uniformModel, false, model.get(modelArr));
        dirtTexture.useTexture();
        shinyMaterial.useMaterial(uniformSpecularIntensity, uniformShininess);
        meshList.get(2).renderMesh();

        /*
         */
        model = new Matrix4f();
        model = model.translate(new Vector3f(-7.0f, 0.0f, 10.0f));
        model = model.scale(new Vector3f(0.006f, 0.006f, 0.006f));
        glUniformMatrix4fv(uniformModel, false, model.get(modelArr));
        dirtTexture.useTexture();
        shinyMaterial.useMaterial(uniformSpecularIntensity, uniformShininess);
        xwing.renderModel();

        /*
         */
        blackhawkAngle += 0.1f;
        if (blackhawkAngle > 360.0f) {
            blackhawkAngle = 0.1f;
        }

        model = new Matrix4f();
        model = model.rotate(-blackhawkAngle * CommonValues.TO_RADIANS, new Vector3f(0.0f, 1.0f, 0.0f));
        model = model.translate(new Vector3f(-8.0f, 2.0f, 0.0f));
        model = model.rotate(-20.0f * CommonValues.TO_RADIANS, new Vector3f(0.0f, 0.0f, 1.0f));
        model = model.rotate(-90.0f * CommonValues.TO_RADIANS, new Vector3f(1.0f, 0.0f, 0.0f));
        model = model.scale(new Vector3f(0.4f, 0.4f, 0.4f));
        glUniformMatrix4fv(uniformModel, false, model.get(modelArr));
        dirtTexture.useTexture();
        shinyMaterial.useMaterial(uniformSpecularIntensity, uniformShininess);
        blackhawk.renderModel();

    /*
    model = glm::mat4(1.0f);
    model = glm::translate(model, glm::vec3(-10.0f, 2.0f, 10.0f));
    model = glm::rotate(model, -90.0f * toRadians, glm::vec3(1.0f, 0.0f, 0.0f));
    model = glm::scale(model, glm::vec3(0.05f, 0.05f, 0.05f));
    glUniformMatrix4fv(uniformModel, 1, GL_FALSE, glm::value_ptr(model));
    dirtTexture.UseTexture();
    shinyMaterial.UseMaterial(uniformSpecaularIntensity, uniformShininess);
    tooth30.RenderModel();
    */

    /*
    model = glm::mat4(1.0f);
    model = glm::translate(model, glm::vec3(-5.0f, 2.0f, 5.0f));
    //model = glm::scale(model, glm::vec3(10.0f, 10.0f, 10.0f));
    glUniformMatrix4fv(uniformModel, 1, GL_FALSE, glm::value_ptr(model));
    dirtTexture.UseTexture();
    shinyMaterial.UseMaterial(uniformSpecaularIntensity, uniformShininess);
    sphere.RenderModel();
    */

    /*
    model = glm::mat4(1.0f);
    model = glm::translate(model, glm::vec3(-5.0f, 2.0f, 5.0f));
    //model = glm::scale(model, glm::vec3(10.0f, 10.0f, 10.0f));
    glUniformMatrix4fv(uniformModel, 1, GL_FALSE, glm::value_ptr(model));
    dirtTexture.UseTexture();
    shinyMaterial.UseMaterial(uniformSpecaularIntensity, uniformShininess);
    eyeball.RenderModel();
    */

    /*
    model = glm::mat4(1.0f);
    model = glm::translate(model, glm::vec3(-5.0f, 5.0f, 5.0f));
    model = glm::rotate(model, -90.0f * toRadians, glm::vec3(1.0f, 0.0f, 0.0f));
    model = glm::scale(model, glm::vec3(0.01f, 0.01f, 0.01f));
    glUniformMatrix4fv(uniformModel, 1, GL_FALSE, glm::value_ptr(model));
    dirtTexture.UseTexture();
    shinyMaterial.UseMaterial(uniformSpecaularIntensity, uniformShininess);
    airliner.RenderModel();
    */
    }

    void DirectionalShadowMapPass(DirectionalLight light)
    {
        directionalShadowShader.useShader();

        glViewport(0, 0, light.getShadowMap().getShadowWidth(), light.getShadowMap().getShadowHeight());

        light.getShadowMap().write();
        glClear(GL_DEPTH_BUFFER_BIT);

        uniformModel = directionalShadowShader.getUniformModel();
        Matrix4f transform = light.calculateLightTransform();
        directionalShadowShader.setUniformDirectionalLightTransform(transform);
        //directionalShadowShader.SetDirectionalLightTransform(&light->CalculateLightTransform());

        directionalShadowShader.validate();

        RenderScene();

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    void OmniShadowMapPass(PointLight light)
    {
        glViewport(0, 0, light.getShadowMap().getShadowWidth(), light.getShadowMap().getShadowHeight());

        omniShadowShader.useShader();

        uniformModel = omniShadowShader.getUniformModel();
        uniformOmniLightPos = omniShadowShader.getUniformOmniLightPos();
        uniformFarPlane = omniShadowShader.getUniformFarPlane();

        light.getShadowMap().write();
        glClear(GL_DEPTH_BUFFER_BIT);

        glUniform3f(uniformOmniLightPos, light.getPosition().x, light.getPosition().y, light.getPosition().z);
        glUniform1f(uniformFarPlane, light.getFarPlane());
        omniShadowShader.setUniformLightMatrices(light.calculateLightTransform());

        omniShadowShader.validate();

        RenderScene();

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

    }

    void RenderPass(Matrix4f projectionMatrix, Matrix4f viewMatrix)
    {
        glViewport(0, 0, 1366, 768);

        // Clear window
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        skybox.DrawSkybox(viewMatrix, projectionMatrix);

        shaderList.get(0).useShader();

        uniformModel = shaderList.get(0).getUniformModel();
        uniformProjection = shaderList.get(0).getUniformProjection();
        uniformView = shaderList.get(0).getUniformView();
        uniformEyePosition = shaderList.get(0).getUniformEyePosition();
        uniformSpecularIntensity = shaderList.get(0).getUniformSpecularIntensity();
        uniformShininess = shaderList.get(0).getUniformShininess();

        float[] modelArr = new float[16];

        glUniformMatrix4fv(uniformProjection, false, projectionMatrix.get(modelArr));
        glUniformMatrix4fv(uniformView, false, viewMatrix.get(modelArr));
        glUniform3f(uniformEyePosition, camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);

        shaderList.get(0).setDirectionalLight(mainLight);
        shaderList.get(0).setPointLights(pointLights, pointLightCount, 3, 0);
        shaderList.get(0).setSpotLights(spotLights, spotLightCount, 3 + pointLightCount, pointLightCount);
        Matrix4f transform = mainLight.calculateLightTransform();
        shaderList.get(0).setUniformDirectionalLightTransform(transform);
        //shaderList[0].SetDirectionalLightTransform(&mainLight.CalculateLightTransform());

        mainLight.getShadowMap().read(GL_TEXTURE2);
        shaderList.get(0).setTexture(1);
        shaderList.get(0).setDirectionalShadowMap(2);

        Vector3f lowerLight = new Vector3f(camera.getPosition());
        lowerLight.y -= 0.3f;
        spotLights[0].setFlash(lowerLight, camera.getDirection());

        RenderScene();
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

        /*
         */
        xwing = new Model();
        xwing.loadModel("D:/gitrepos/OpenGLCourseApp/src/main/resources/Models/x-wing.obj");

        /*
         */
        blackhawk = new Model();
        blackhawk.loadModel("D:/gitrepos/OpenGLCourseApp/src/main/resources/Models/uh60.obj");

        /*
         */
        tooth30 = new Model();
        tooth30.loadModel("D:/gitrepos/OpenGLCourseApp/src/main/resources/Models/Lower_Right_First_Molar_30_Enamel.obj");

        /*
        sphere = new Model();
        sphere.loadModel("D:/gitrepos/OpenGLCourseApp/src/main/resources/Models/sphere2.obj");
         */

        /*
        eyeball = new Model();
        eyeball.loadModel("D:/gitrepos/OpenGLCourseApp/src/main/resources/Models/eyeball.obj");
         */

        /*
        airliner = new Model();
        airliner.loadModel("D:/gitrepos/OpenGLCourseApp/src/main/resources/Models/11803_Airplane_v1_l1.obj");
         */

        mainLight = new DirectionalLight(2048, 2048,
                1.0f, 0.53f, 0.3f,
                0.1f, 0.9f,
                -10.0f, -12.0f, 18.5f);

        //int pointLightCount = 0;
        pointLights[0] = new PointLight(1024, 1024,
                0.01f, 100.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 1.0f,
                1.0f, 2.0f, 0.0f,
                0.3f, 0.2f, 0.1f);
        pointLightCount++;
        pointLights[1] = new PointLight(1024, 1024,
                0.01f, 100.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f,
                -4.0f, 3.0f, 0.0f,
                0.3f, 0.2f, 0.1f);
        pointLightCount++;

        //int spotLightCount = 0;
        spotLights[0] = new SpotLight(1024, 1024,
                0.01f, 100.0f,
                1.0f, 1.0f, 1.0f,
                0.0f, 2.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                20.0f);
        spotLightCount++;
        spotLights[1] = new SpotLight(1024, 1024,
                0.01f, 100.0f,
                1.0f, 1.0f, 1.0f,
                0.0f, 1.0f,
                0.0f, 1.5f, 0.0f,
                -2.0f, -1.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                20.0f);
        spotLightCount++;


        /*
        int uniformModel, uniformProjection, uniformView, uniformEyePosition,
                uniformSpecaularIntensity, uniformShininess;
         */

        String[] skyboxFaces = new String[6];
        skyboxFaces[0] = "Textures/Skybox/cupertin-lake_rt.tga";
        skyboxFaces[1] = "Textures/Skybox/cupertin-lake_lf.tga";
        skyboxFaces[2] = "Textures/Skybox/cupertin-lake_up.tga";
        skyboxFaces[3] = "Textures/Skybox/cupertin-lake_dn.tga";
        skyboxFaces[4] = "Textures/Skybox/cupertin-lake_bk.tga";
        skyboxFaces[5] = "Textures/Skybox/cupertin-lake_ft.tga";

        skybox = new Skybox(skyboxFaces);

        Matrix4f projection = new Matrix4f();
        projection.setPerspective((float) Math.toRadians(60.0f), (float)mainWindow.getBufferWidth()/(float)mainWindow.getBufferHeight(),
                0.1f, 100.0f);

        while (!mainWindow.getShouldClose()) {
            float now = (float) glfwGetTime();
            deltaTime = now - lastTime;
            lastTime = now;

            GLFW.glfwPollEvents();

            camera.keyControl(mainWindow.getKeys(), deltaTime);
            camera.mouseControl(mainWindow.getXChange(), mainWindow.getYChange());

            if (mainWindow.getKeys()[GLFW.GLFW_KEY_L]) {
                spotLights[0].toggle();
                mainWindow.getKeys()[GLFW.GLFW_KEY_L] = false;
            }

            DirectionalShadowMapPass(mainLight);

            for (int i = 0; i < pointLightCount; i++) {
                OmniShadowMapPass(pointLights[i]);
            }

            for (int i = 0; i < spotLightCount; i++) {
                OmniShadowMapPass(spotLights[i]);
            }

            RenderPass(projection, camera.calculateViewMatrix());

            glUseProgram(0);

            mainWindow.swapBuffers();
        }
    }

    public static void main(String[] args) {
        OpenGLCourseApp app = new OpenGLCourseApp();
        app.run();
    }
}
