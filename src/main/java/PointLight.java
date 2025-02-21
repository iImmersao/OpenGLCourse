import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL33.glUniform1f;
import static org.lwjgl.opengl.GL33.glUniform3f;

public class PointLight extends Light {
    private Vector3f position;

    private float constant;

    private float linear;

    private float exponent;

    private float farPlane;

    public float getFarPlane() {
        return farPlane;
    }

    public PointLight(int shadowWidth, int shadowHeight,
                      float near, float far,
                      float red, float green, float blue,
                      float aIntensity, float dIntensity,
                      float xPos, float yPos, float zPos,
                      float con, float lin, float exp) {
        super(shadowWidth, shadowHeight, red, green, blue, aIntensity, dIntensity);
        position = new Vector3f(xPos, yPos, zPos);
        constant = con;
        linear = lin;
        exponent = exp;

        farPlane = far;

        float aspect = (float)shadowWidth / (float)shadowHeight; // Should equal 1, as the shadow map is supposed to be a cube.
        lightProj = new Matrix4f();
        lightProj = lightProj.perspective(CommonValues.TO_RADIANS * 90.0f, aspect, near, far);

        shadowMap = new OmniShadowMap();
        shadowMap.init(shadowWidth, shadowHeight);
    }

    public void useLight(int ambientIntensityLocation, int ambientColourLocation,
                         int diffuseIntensityLocation, int positionLocation,
                         int constantLocation, int linearLocation, int exponentLocation) {
        glUniform3f(ambientColourLocation, getColour().x, getColour().y, getColour().z);
        glUniform1f(ambientIntensityLocation, getAmbientIntensity());
        glUniform1f(diffuseIntensityLocation, getDiffuseIntensity());

        glUniform3f(positionLocation, position.x, position.y, position.z);
        glUniform1f(constantLocation, constant);
        glUniform1f(linearLocation, linear);
        glUniform1f(exponentLocation, exponent);
    }

    private Matrix4f createLightMatrix(Vector3f v1, Vector3f v2) {
        Matrix4f view = new Matrix4f();

        Vector3f shiftedPosition = new Vector3f(position);
        shiftedPosition.add(v1);
        view.lookAt(position, shiftedPosition, v2);

        Matrix4f tmpLightProj = new Matrix4f(lightProj);
        tmpLightProj.mul(view);

        return tmpLightProj;
    }

    public List<Matrix4f> calculateLightTransform()
    {
        List<Matrix4f> lightMatrices = new ArrayList<>();
        Matrix4f view = new Matrix4f();

        /*
            // +x, -x
    lightMatrices.push_back(lightProj * glm::lookAt(position, position + glm::vec3(1.0, 0.0, 0.0), glm::vec3(0.0, -1.0, 0.0)));
    lightMatrices.push_back(lightProj * glm::lookAt(position, position + glm::vec3(-1.0, 0.0, 0.0), glm::vec3(0.0, -1.0, 0.0)));

    // +y, -y
    lightMatrices.push_back(lightProj * glm::lookAt(position, position + glm::vec3(0.0, 1.0, 0.0), glm::vec3(0.0, 0.0, 1.0)));
    lightMatrices.push_back(lightProj * glm::lookAt(position, position + glm::vec3(0.0, -1.0, 0.0), glm::vec3(0.0, 0.0, -1.0)));

    // +z, -z
    lightMatrices.push_back(lightProj * glm::lookAt(position, position + glm::vec3(0.0, 0.0, 1.0), glm::vec3(0.0, -1.0, 0.0)));
    lightMatrices.push_back(lightProj * glm::lookAt(position, position + glm::vec3(0.0, 0.0, -1.0), glm::vec3(0.0, -1.0, 0.0)));

         */
        // +x, -x
        lightMatrices.add(createLightMatrix(new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, -1.0f, 0.0f)));
        lightMatrices.add(createLightMatrix(new Vector3f(-1.0f, 0.0f, 0.0f), new Vector3f(0.0f, -1.0f, 0.0f)));

        // +y, -y
        lightMatrices.add(createLightMatrix(new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f)));
        lightMatrices.add(createLightMatrix(new Vector3f(0.0f, -1.0f, 0.0f), new Vector3f(0.0f, 0.0f, -1.0f)));

        // +z, -z
        lightMatrices.add(createLightMatrix(new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, -1.0f, 0.0f)));
        lightMatrices.add(createLightMatrix(new Vector3f(0.0f, 0.0f, -1.0f), new Vector3f(0.0f, -1.0f, 0.0f)));

        return lightMatrices;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f pos) {
        position = pos;
    }

    public float getConstant() {
        return constant;
    }

    public float getLinear() {
        return linear;
    }

    public float getExponent() {
        return exponent;
    }
}
