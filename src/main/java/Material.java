import static org.lwjgl.opengl.GL33.glUniform1f;

public class Material {
    private float specularIntensity;

    private float shininess;

    public Material() {
        specularIntensity = 0.0f;
        shininess = 0.0f;
    }

    public Material(float specularIntensity, float shininess) {
        this.specularIntensity = specularIntensity;
        this.shininess = shininess;
    }

    public void useMaterial(int specularIntensityLocation, int shininessLocation) {
        glUniform1f(specularIntensityLocation, specularIntensity);
        glUniform1f(shininessLocation, shininess);
    }
}
