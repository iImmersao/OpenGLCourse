import org.joml.Vector3f;

import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform3f;

public class SpotLight extends PointLight {

    private Vector3f direction;

    private float edge;

    private float procEdge;

    public SpotLight(float red, float green, float blue,
                     float ambientIntensity, float dIntensity,
                     float xPos, float yPos, float zPos,
                     float xDir, float yDir, float zDir,
                     float con, float lin, float exp,
                     float edg) {
        super(red, green, blue, ambientIntensity, dIntensity, xPos, yPos, zPos, con, lin, exp);
        direction = new Vector3f(xDir, yDir, zDir).normalize();
        edge = edg;
        procEdge = (float) Math.cos(Math.toRadians(edge));
    }

    public void useLight(int ambientIntensityLocation, int ambientColourLocation,
                 int diffuseIntensityLocation, int positionLocation, int directionLocation,
                 int constantLocation, int linearLocation, int exponentLocation,
                 int edgeLocation) {
        glUniform3f(ambientColourLocation, getColour().x, getColour().y, getColour().z);
        glUniform1f(ambientIntensityLocation, getAmbientIntensity());
        glUniform1f(diffuseIntensityLocation, getDiffuseIntensity());

        glUniform3f(positionLocation, getPosition().x, getPosition().y, getPosition().z);
        glUniform1f(constantLocation, getConstant());
        glUniform1f(linearLocation, getLinear());
        glUniform1f(exponentLocation, getExponent());

        glUniform3f(directionLocation, direction.x, direction.y, direction.z);
        glUniform1f(edgeLocation, procEdge);
    }

    public void setFlash(Vector3f pos, Vector3f dir) {
        super.setPosition(pos);
        direction = dir;
    }
}
