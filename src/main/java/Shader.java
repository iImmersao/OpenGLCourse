import org.joml.Matrix4f;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.opengl.GL33.glGetUniformLocation;

public class Shader {
    private class UniformOmniShadowMap {
        private int shadowMap;
        private int farPlane;
    }

    private class UniformDirectionalLight {
        private int uniformColour;
        private int uniformAmbientIntensity;
        private int uniformDiffuseIntensity;
        private int uniformDirection;

    }

    private class UniformPointLight {
        private int uniformColour;
        private int uniformAmbientIntensity;
        private int uniformDiffuseIntensity;
        private int uniformPosition;
        private int uniformConstant;
        private int uniformLinear;
        private int uniformExponent;
    }

    private class UniformSpotLight {
        private int uniformColour;
        private int uniformAmbientIntensity;
        private int uniformDiffuseIntensity;
        private int uniformPosition;
        private int uniformConstant;
        private int uniformLinear;
        private int uniformExponent;
        private int uniformDirection;
        private int uniformEdge;
    }

    private int shaderID;
    private int uniformModel;
    private int uniformProjection;

    private int uniformView;

    private int uniformEyePosition;
    private int uniformSpecularIntensity;
    private int uniformShininess;

    private int uniformTexture;
    private int uniformDirectionalLightTransform;
    private int uniformDirectionalShadowMap;
    private int uniformOmniLightPos;
    private int uniformFarPlane;

    private int[] uniformLightMatrices = new int[6];

    private UniformOmniShadowMap[] uniformOmniShadowMap = new UniformOmniShadowMap[CommonValues.MAX_POINT_LIGHTS + CommonValues.MAX_SPOT_LIGHTS];

    void setUniformLightMatrices(List<Matrix4f> lightMatrices)
    {
        float[] matArr = new float[16];
        for (int i = 0; i < 6; i++) {
            glUniformMatrix4fv(uniformLightMatrices[i], false, lightMatrices.get(i).get(matArr));
        }
    }

    public int getUniformTexture() {
        return uniformTexture;
    }

    public void setUniformTexture(int uniformTexture) {
        this.uniformTexture = uniformTexture;
    }

    public int getUniformDirectionalLightTransform() {
        return uniformDirectionalLightTransform;
    }

    public void setUniformDirectionalLightTransform(int uniformDirectionalLightTransform) {
        this.uniformDirectionalLightTransform = uniformDirectionalLightTransform;
    }

    public int getUniformDirectionalShadowMap() {
        return uniformDirectionalShadowMap;
    }

    public void setUniformDirectionalShadowMap(int uniformDirectionalShadowMap) {
        this.uniformDirectionalShadowMap = uniformDirectionalShadowMap;
    }

    public int getUniformOmniLightPos() {
        return uniformOmniLightPos;
    }

    public void setUniformOmniLightPos(int uniformOmniLightPos) {
        this.uniformOmniLightPos = uniformOmniLightPos;
    }

    public int getUniformFarPlane() {
        return uniformFarPlane;
    }

    public void setUniformFarPlane(int uniformFarPlane) {
        this.uniformFarPlane = uniformFarPlane;
    }

    private UniformDirectionalLight uniformDirectionalLight = new UniformDirectionalLight();

    private int uniformPointLightCount;

    private UniformPointLight[] uniformPointLight = new UniformPointLight[CommonValues.MAX_POINT_LIGHTS];

    private int uniformSpotLightCount;

    private UniformSpotLight[] uniformSpotLight = new UniformSpotLight[CommonValues.MAX_SPOT_LIGHTS];

    public Shader() {
        for (int i = 0; i < CommonValues.MAX_POINT_LIGHTS; i++) {
            uniformPointLight[i] = new UniformPointLight();
        }

        for (int i = 0; i < CommonValues.MAX_SPOT_LIGHTS; i++) {
            uniformSpotLight[i] = new UniformSpotLight();
        }

        for (int i = 0; i < CommonValues.MAX_POINT_LIGHTS + CommonValues.MAX_SPOT_LIGHTS; i++) {
            uniformOmniShadowMap[i] = new UniformOmniShadowMap();
        }
    }

    public void createFromString(String vertexCode, String fragmentCode) {
        compileShader(vertexCode, fragmentCode);
    }

    public void createFromFiles(String vertexFilename, String fragmentFilename) {
        String vertexString = readFile(vertexFilename);
        String fragmentString = readFile(fragmentFilename);
        compileShader(vertexString, fragmentString);
    }

    public void createFromFiles(String vertexLocation, String geometryLocation, String fragmentLocation)
    {
        String vertexString = readFile(vertexLocation);
        String geometryString = readFile(geometryLocation);
        String fragmentString = readFile(fragmentLocation);
        compileShader(vertexString, geometryString, fragmentString);
    }

    void validate()
    {
        int[] result = { 0 };
        byte[] eLogRaw = new byte[1024];
        ByteBuffer eLog = ByteBuffer.wrap(eLogRaw);

        glValidateProgram(shaderID);
        glGetProgramiv(shaderID, GL_LINK_STATUS, result);
        if (result[0] != 1) {
            glGetProgramInfoLog(shaderID, result, eLog);
            System.out.println("Error validating program: " + eLog);
            return;
        }

    }

    private String readFile(String fileName) {
        StringBuilder content = new StringBuilder();
        String line;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = classLoader.getResourceAsStream(fileName);
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));

        try {
            while (((line = buf.readLine()) != null)) {
                content.append(line);
                content.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content.toString();
    }

    static void AddShader(int theProgram, String shaderCode, int shaderType) {
        int theShader = glCreateShader(shaderType);

        //System.out.println(shaderCode);
        glShaderSource(theShader, shaderCode);
        glCompileShader(theShader);

        int[] result = { 0 };
        byte[] eLogRaw = new byte[1024];
        ByteBuffer eLog = ByteBuffer.wrap(eLogRaw);

        glGetShaderiv(theShader, GL_COMPILE_STATUS, result);
        if (result[0] != 1) {
            glGetProgramInfoLog(theShader, result, eLog);
            System.out.println("Error compiling the " + shaderType + " shader: " + eLog + "\n");
            return;
        }

        glAttachShader(theProgram, theShader);
    }

    public void compileShader(String vertexCode, String fragmentCode) {
        shaderID = glCreateProgram();

        if (shaderID < 0) {
            System.out.println("Error creating shader program!");
            return;
        }

        AddShader(shaderID, vertexCode, GL_VERTEX_SHADER);
        AddShader(shaderID, fragmentCode, GL_FRAGMENT_SHADER);

        CompileProgram();
    }


    public void compileShader(String vertexCode, String geometryCode, String fragmentCode) {
        shaderID = glCreateProgram();

        if (shaderID < 0) {
            System.out.println("Error creating shader program!");
            return;
        }

        AddShader(shaderID, vertexCode, GL_VERTEX_SHADER);
        AddShader(shaderID, geometryCode, GL_GEOMETRY_SHADER);
        AddShader(shaderID, fragmentCode, GL_FRAGMENT_SHADER);

        CompileProgram();
    }

    private void CompileProgram() {

        int[] result = { 0 };
        byte[] eLogRaw = new byte[1024];
        ByteBuffer eLog = ByteBuffer.wrap(eLogRaw);

        glLinkProgram(shaderID);
        glGetProgramiv(shaderID, GL_LINK_STATUS, result);
        if (result[0] != 1) {
            glGetProgramInfoLog(shaderID, result, eLog);
            System.out.println("Error linking program: " + eLog);
            return;
        }

        /*
        glValidateProgram(shaderID);
        glGetProgramiv(shaderID, GL_VALIDATE_STATUS, result);
        if (result[0] != 1) {
            glGetProgramInfoLog(shaderID, result, eLog);
            System.out.println("Error validating program: " + eLog);
            return;
        }
         */

        uniformModel = glGetUniformLocation(shaderID, "model");
        uniformProjection = glGetUniformLocation(shaderID, "projection");
        uniformView = glGetUniformLocation(shaderID, "view");
        uniformDirectionalLight.uniformColour = glGetUniformLocation(shaderID, "directionalLight.base.colour");
        uniformDirectionalLight.uniformAmbientIntensity = glGetUniformLocation(shaderID, "directionalLight.base.ambientIntensity");
        uniformDirectionalLight.uniformDirection = glGetUniformLocation(shaderID, "directionalLight.direction");
        uniformDirectionalLight.uniformDiffuseIntensity = glGetUniformLocation(shaderID, "directionalLight.base.diffuseIntensity");
        uniformSpecularIntensity = glGetUniformLocation(shaderID, "material.specularIntensity");
        uniformShininess = glGetUniformLocation(shaderID, "material.shininess");
        uniformEyePosition = glGetUniformLocation(shaderID, "eyePosition");

        uniformPointLightCount = glGetUniformLocation(shaderID, "pointLightCount");

        for (int i = 0; i < CommonValues.MAX_POINT_LIGHTS; i++) {
            String locBuff;

            locBuff = "pointLights[" + i + "].base.colour";
            uniformPointLight[i].uniformColour = glGetUniformLocation(shaderID, locBuff);

            locBuff = "pointLights[" + i + "].base.ambientIntensity";
            uniformPointLight[i].uniformAmbientIntensity = glGetUniformLocation(shaderID, locBuff);

            locBuff = "pointLights[" + i + "].base.diffuseIntensity";
            uniformPointLight[i].uniformDiffuseIntensity = glGetUniformLocation(shaderID, locBuff);

            locBuff = "pointLights[" + i + "].position";
            uniformPointLight[i].uniformPosition = glGetUniformLocation(shaderID, locBuff);

            locBuff = "pointLights[" + i + "].constant";
            uniformPointLight[i].uniformConstant = glGetUniformLocation(shaderID, locBuff);

            locBuff = "pointLights[" + i + "].linear";
            uniformPointLight[i].uniformLinear = glGetUniformLocation(shaderID, locBuff);

            locBuff = "pointLights[" + i + "].exponent";
            uniformPointLight[i].uniformExponent = glGetUniformLocation(shaderID, locBuff);
        }

        uniformSpotLightCount = glGetUniformLocation(shaderID, "spotLightCount");

        for (int i = 0; i < CommonValues.MAX_SPOT_LIGHTS; i++) {
            String locBuff;

            locBuff = "spotLights[" + i + "].base.base.colour";
            uniformSpotLight[i].uniformColour = glGetUniformLocation(shaderID, locBuff);

            locBuff = "spotLights[" + i + "].base.base.ambientIntensity";
            uniformSpotLight[i].uniformAmbientIntensity = glGetUniformLocation(shaderID, locBuff);

            locBuff = "spotLights[" + i + "].base.base.diffuseIntensity";
            uniformSpotLight[i].uniformDiffuseIntensity = glGetUniformLocation(shaderID, locBuff);

            locBuff = "spotLights[" + i + "].base.position";
            uniformSpotLight[i].uniformPosition = glGetUniformLocation(shaderID, locBuff);

            locBuff = "spotLights[" + i + "].base.constant";
            uniformSpotLight[i].uniformConstant = glGetUniformLocation(shaderID, locBuff);

            locBuff = "spotLights[" + i + "].base.linear";
            uniformSpotLight[i].uniformLinear = glGetUniformLocation(shaderID, locBuff);

            locBuff = "spotLights[" + i + "].base.exponent";
            uniformSpotLight[i].uniformExponent = glGetUniformLocation(shaderID, locBuff);

            locBuff = "spotLights[" + i + "].direction";
            uniformSpotLight[i].uniformDirection = glGetUniformLocation(shaderID, locBuff);

            locBuff = "spotLights[" + i + "].edge";
            uniformSpotLight[i].uniformEdge = glGetUniformLocation(shaderID, locBuff);
        }

        uniformTexture = glGetUniformLocation(shaderID, "theTexture");
        uniformDirectionalLightTransform = glGetUniformLocation(shaderID, "directionalLight");
        uniformDirectionalShadowMap = glGetUniformLocation(shaderID, "directionalShadowMap");

        uniformOmniLightPos = glGetUniformLocation(shaderID, "lightPos");
        uniformFarPlane = glGetUniformLocation(shaderID, "farPlane");

        for (int i = 0; i < 6; i++) {
            String locBuff;

            locBuff = "lightMatrices[" + i + "]";
            uniformLightMatrices[i] = glGetUniformLocation(shaderID, locBuff);
        }

        for (int i = 0; i < CommonValues.MAX_POINT_LIGHTS + CommonValues.MAX_SPOT_LIGHTS; i++) {
            String locBuff;

            locBuff = "omniShadowMaps[" + i + "].shadowMap";
            uniformOmniShadowMap[i].shadowMap = glGetUniformLocation(shaderID, locBuff);
            locBuff = "omniShadowMaps[" + i + "].farPlane";
            uniformOmniShadowMap[i].farPlane = glGetUniformLocation(shaderID, locBuff);
        }
    }

    public void useShader() {
        glUseProgram(shaderID);
    }

    public void clearShader() {
        if (shaderID != 0) {
            glDeleteProgram(shaderID);
            shaderID = 0;
        }

        uniformModel = 0;
        uniformProjection = 0;
        uniformView = 0;
    }

    void setDirectionalLight(DirectionalLight dLight) {
        dLight.useLight(uniformDirectionalLight.uniformAmbientIntensity, uniformDirectionalLight.uniformColour,
                uniformDirectionalLight.uniformDiffuseIntensity, uniformDirectionalLight.uniformDirection);
    }

    void setPointLights(PointLight[] pLight, int lightCount, int textureUnit, int offset) {
        if (lightCount > CommonValues.MAX_POINT_LIGHTS) {
            lightCount = CommonValues.MAX_POINT_LIGHTS;
        }

        glUniform1i(uniformPointLightCount, lightCount);

        for (int i = 0; i < lightCount; i++) {
            pLight[i].useLight(uniformPointLight[i].uniformAmbientIntensity, uniformPointLight[i].uniformColour,
                    uniformPointLight[i].uniformDiffuseIntensity, uniformPointLight[i].uniformPosition,
                    uniformPointLight[i].uniformConstant, uniformPointLight[i].uniformLinear, uniformPointLight[i].uniformExponent);

            pLight[i].getShadowMap().read(GL_TEXTURE0 + textureUnit + i);
            glUniform1i(uniformOmniShadowMap[i + offset].shadowMap, textureUnit + i);
            glUniform1f(uniformOmniShadowMap[i + offset].farPlane, pLight[i].getFarPlane());
        }
    }

    void setSpotLights(SpotLight[] sLight, int lightCount, int textureUnit, int offset) {
        if (lightCount > CommonValues.MAX_POINT_LIGHTS) {
            lightCount = CommonValues.MAX_POINT_LIGHTS;
        }

        glUniform1i(uniformSpotLightCount, lightCount);

        for (int i = 0; i < lightCount; i++) {
            sLight[i].useLight(uniformSpotLight[i].uniformAmbientIntensity, uniformSpotLight[i].uniformColour,
                    uniformSpotLight[i].uniformDiffuseIntensity, uniformSpotLight[i].uniformPosition, uniformSpotLight[i].uniformDirection,
                    uniformSpotLight[i].uniformConstant, uniformSpotLight[i].uniformLinear, uniformSpotLight[i].uniformExponent,
                    uniformSpotLight[i].uniformEdge);

            sLight[i].getShadowMap().read(GL_TEXTURE0 + textureUnit + i);
            glUniform1i(uniformOmniShadowMap[i + offset].shadowMap, textureUnit + i);
            glUniform1f(uniformOmniShadowMap[i + offset].farPlane, sLight[i].getFarPlane());
        }
    }

    public int getUniformModel() {
        return uniformModel;
    }

    public int getUniformProjection() {
        return uniformProjection;
    }

    public int getUniformView() { return uniformView; }

    public int getUniformEyePosition() {
        return uniformEyePosition;
    }

    public int getUniformSpecularIntensity() {
        return uniformSpecularIntensity;
    }

    public int getUniformShininess() {
        return uniformShininess;
    }

    public void setTexture(int textureUnit) {
        glUniform1i(uniformTexture, textureUnit);
    }

    public void setDirectionalShadowMap(int textureUnit) {
        glUniform1i(uniformDirectionalShadowMap, textureUnit);
    }

    public void setUniformDirectionalLightTransform(Matrix4f lTransform) {
        float[] modelArr = new float[16];
        lTransform.get(modelArr);

        glUniformMatrix4fv(uniformDirectionalLightTransform, false, modelArr);
    }
}
