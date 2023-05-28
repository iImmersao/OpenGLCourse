import org.joml.Vector3f;

import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform3f;

public class Light {
    private Vector3f colour;

    private float ambientIntensity;

    private Vector3f direction;

    private float diffuseIntensity;

    public Light(float red, float green, float blue, float ambientIntensity,
                 float xDir, float yDir, float zDir, float dIntensity) {
        this.colour = new Vector3f(red, green, blue);
        this.ambientIntensity = ambientIntensity;
        this.direction = new Vector3f(xDir, yDir, zDir);
        this.diffuseIntensity = dIntensity;
    }

    public void useLight(int ambientIntensityLocation, int ambientColourLocation,
                         int diffuseIntensityLocation, int directionLocation) {
        glUniform3f(ambientColourLocation, colour.x, colour.y, colour.z);
        glUniform1f(ambientIntensityLocation, ambientIntensity);

        glUniform3f(directionLocation, direction.x, direction.y, direction.z);
        glUniform1f(diffuseIntensityLocation, diffuseIntensity);
    }
}
