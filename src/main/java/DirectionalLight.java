import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL33.glUniform1f;
import static org.lwjgl.opengl.GL33.glUniform3f;

public class DirectionalLight extends Light {

    private Vector3f direction;

    public DirectionalLight(int shadowWidth, int shadowHeight,
                            float red, float green, float blue,
                            float aIntensity, float dIntensity,
                            float xDir, float yDir, float zDir) {
        super(shadowWidth, shadowHeight, red, green, blue, aIntensity, dIntensity);
        direction = new Vector3f(xDir, yDir, zDir);

        //lightProj = createOrthographic(-5.0f, 5.0f, -5.0f, 5.0f, 0.1f, 20.0f);
        lightProj = createOrthographic(-20.0f, 20.0f, -20.0f, 20.0f, 0.1f, 100.0f);
    }

    private static Matrix4f createOrthographic(float left, float right, float bottom, float top, float near, float far) {
        Matrix4f ortho = new Matrix4f();

        float tx = -(right + left) / (right - left);
        float ty = -(top + bottom) / (top - bottom);
        float tz = -(far + near) / (far - near);

        ortho.m00(2f / (right - left));
        ortho.m11(2f / (top - bottom));
        ortho.m22(-2f / (far - near));
        ortho.m03(tx);
        ortho.m13(ty);
        ortho.m23(tz);

        return ortho;
    }

    public void useLight(int ambientIntensityLocation, int ambientColourLocation,
                         int diffuseIntensityLocation, int directionLocation) {
        glUniform3f(ambientColourLocation, getColour().x, getColour().y, getColour().z);
        glUniform1f(ambientIntensityLocation, getAmbientIntensity());

        glUniform3f(directionLocation, direction.x, direction.y, direction.z);
        glUniform1f(diffuseIntensityLocation, getDiffuseIntensity());
    }

    public Matrix4f calculateLightTransform() {
        Matrix4f view = new Matrix4f();
        Vector3f reverseDirection = new Vector3f(direction).mul(-1.0f);
        Matrix4f tmpLightProj = new Matrix4f(lightProj);

        return tmpLightProj.mul(view.lookAt(reverseDirection, new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f)));
    }
}
