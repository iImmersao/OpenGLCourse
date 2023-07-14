import org.joml.Vector3f;

import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform3f;

public class PointLight extends Light {
    private Vector3f position;

    private float constant;

    private float linear;

    private float exponent;

    public PointLight(float red, float green, float blue,
                            float aIntensity, float dIntensity,
                            float xPos, float yPos, float zPos,
                            float con, float lin, float exp) {
        super(red, green, blue, aIntensity, dIntensity);
        position = new Vector3f(xPos, yPos, zPos);
        constant = con;
        linear = lin;
        exponent = exp;
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
}
