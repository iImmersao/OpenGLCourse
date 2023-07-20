import org.joml.Vector3f;
import static org.lwjgl.opengl.GL33.glUniform1f;
import static org.lwjgl.opengl.GL33.glUniform3f;

public class DirectionalLight extends Light {

    private Vector3f direction;

    public DirectionalLight(float red, float green, float blue,
                            float aIntensity, float dIntensity,
                            float xDir, float yDir, float zDir) {
        super(red, green, blue, aIntensity, dIntensity);
        direction = new Vector3f(xDir, yDir, zDir);
    }

    public void useLight(int ambientIntensityLocation, int ambientColourLocation,
                         int diffuseIntensityLocation, int directionLocation) {
        glUniform3f(ambientColourLocation, getColour().x, getColour().y, getColour().z);
        glUniform1f(ambientIntensityLocation, getAmbientIntensity());

        glUniform3f(directionLocation, direction.x, direction.y, direction.z);
        glUniform1f(diffuseIntensityLocation, getDiffuseIntensity());
    }

}
