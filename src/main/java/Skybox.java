import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.*;

import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;

public class Skybox {

    Mesh skyMesh;
    Shader skyShader;

    int textureId;
    int uniformProjection;
    int uniformView;

    public Skybox(String[] faceLocations) {
        System.out.println("Creating Skybox instance");
        // Shader set-up
        skyShader = new Shader();
        skyShader.createFromFiles("skybox.vert", "skybox.frag");

        uniformProjection = skyShader.getUniformProjection();
        uniformView = skyShader.getUniformView();

        // Texture set-up
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureId);

        //int width, height, bitDepth;
        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer bitDepth = BufferUtils.createIntBuffer(1);

        String skyboxTextureRoot = System.getProperty("user.dir") + "/target/classes/";
        ByteBuffer texData = null;
        System.out.println("Creating " + faceLocations.length + " skybox textures...");
        for (int i = 0; i < 6; i++) {
            String actualFileLocation = skyboxTextureRoot + faceLocations[i];
            System.out.println("Reading file: " + actualFileLocation);
            texData = stbi_load(actualFileLocation, width, height, bitDepth, 0);
            if (texData == null) {
                System.out.println("Failed to find: " + faceLocations[i]);
                return;
            }

            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB, width.get(0), height.get(0), 0, GL_RGB, GL_UNSIGNED_BYTE, texData);
            stbi_image_free(texData);
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // Mesh set-up
        int skyboxIndices[] = {
                // front
                0, 1, 2,
                2, 1, 3,
                // right
                2, 3, 5,
                5, 3, 7,
                // back
                5, 7, 4,
                4, 7, 6,
                // left
                4, 6, 0,
                0, 6, 1,
                // top
                4, 0, 5,
                5, 0, 2,
                // bottom
                1, 6, 3,
                3, 6, 7
        };

        float skyboxVertices[] = {
                -1.0f, 1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                -1.0f, -1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                1.0f, 1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                1.0f, -1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,

                -1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                -1.0f, -1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                1.0f, -1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f
        };

        skyMesh = new Mesh();
        skyMesh.createMesh(skyboxVertices, skyboxIndices);
    }

    void DrawSkybox(Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        // Need to remove any changes due to movement, rotation, scaling, etc
        viewMatrix = new Matrix4f(new Matrix3f(viewMatrix));
        glDepthMask(false);

        skyShader.useShader();

        float[] modelArr = new float[16];
        glUniformMatrix4fv(uniformProjection, false, projectionMatrix.get(modelArr));
        glUniformMatrix4fv(uniformView, false, viewMatrix.get(modelArr));

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureId);

        skyShader.validate();

        skyMesh.renderMesh();

        glDepthMask(true);
    }

}