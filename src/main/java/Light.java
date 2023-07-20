import org.joml.Vector3f;

public class Light {
    private Vector3f colour;

    private float ambientIntensity;

    private float diffuseIntensity;

    public Light(float red, float green, float blue, float ambientIntensity, float dIntensity) {
        this.colour = new Vector3f(red, green, blue);
        this.ambientIntensity = ambientIntensity;
        this.diffuseIntensity = dIntensity;
    }

    public Vector3f getColour() {
        return colour;
    }

    public float getAmbientIntensity() {
        return ambientIntensity;
    }

    public float getDiffuseIntensity() {
        return diffuseIntensity;
    }
}
