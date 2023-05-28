import org.joml.Vector3f;

import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform3f;

public class Light {
    private Vector3f colour;

    private float ambientIntensity;

    public Light(float red, float green, float blue, float ambientIntensity) {
        this.colour = new Vector3f(red, green, blue);
        this.ambientIntensity = ambientIntensity;
    }

    public void useLight(int ambientIntensityLocation, int ambientColourLocation) {
        glUniform3f(ambientColourLocation, colour.x, colour.y, colour.z);
        glUniform1f(ambientIntensityLocation, ambientIntensity);

    }
}
