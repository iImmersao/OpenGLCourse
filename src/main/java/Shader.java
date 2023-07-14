import java.io.*;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;

public class Shader {
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

        public int getUniformColour() {
            return uniformColour;
        }

        public void setUniformColour(int uniformColour) {
            this.uniformColour = uniformColour;
        }

        public int getUniformAmbientIntensity() {
            return uniformAmbientIntensity;
        }

        public void setUniformAmbientIntensity(int uniformAmbientIntensity) {
            this.uniformAmbientIntensity = uniformAmbientIntensity;
        }

        public int getUniformDiffuseIntensity() {
            return uniformDiffuseIntensity;
        }

        public void setUniformDiffuseIntensity(int uniformDiffuseIntensity) {
            this.uniformDiffuseIntensity = uniformDiffuseIntensity;
        }

        public int getUniformPosition() {
            return uniformPosition;
        }

        public void setUniformPosition(int uniformPosition) {
            this.uniformPosition = uniformPosition;
        }

        public int getUniformConstant() {
            return uniformConstant;
        }

        public void setUniformConstant(int uniformConstant) {
            this.uniformConstant = uniformConstant;
        }

        public int getUniformLinear() {
            return uniformLinear;
        }

        public void setUniformLinear(int uniformLinear) {
            this.uniformLinear = uniformLinear;
        }

        public int getUniformExponent() {
            return uniformExponent;
        }

        public void setUniformExponent(int uniformExponent) {
            this.uniformExponent = uniformExponent;
        }
    }
    private int shaderID;
    private int uniformModel;
    private int uniformProjection;

    private int uniformView;

    private int uniformAmbientIntensity;
    private int uniformAmbientColour;

    private int uniformDiffuseIntensity;
    private int uniformDirection;

    private int uniformEyePosition;
    private int uniformSpecularIntensity;
    private int uniformShininess;

    private UniformDirectionalLight uniformDirectionalLight = new UniformDirectionalLight();

    private int uniformPointLightCount;

    private UniformPointLight[] uniformPointLight = new UniformPointLight[CommonValues.MAX_POINT_LIGHTS];

    public Shader() {
        for (int i = 0; i < CommonValues.MAX_POINT_LIGHTS; i++) {
            uniformPointLight[i] = new UniformPointLight();
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

        System.out.println(shaderCode);
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

        glValidateProgram(shaderID);
        glGetProgramiv(shaderID, GL_VALIDATE_STATUS, result);
        if (result[0] != 1) {
            glGetProgramInfoLog(shaderID, result, eLog);
            System.out.println("Error validating program: " + eLog);
            return;
        }

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

    void setPointLights(PointLight pLight[], int lightCount) {
        if (lightCount > CommonValues.MAX_POINT_LIGHTS) {
            lightCount = CommonValues.MAX_POINT_LIGHTS;
        }

        glUniform1i(uniformPointLightCount, lightCount);

        for (int i = 0; i < lightCount; i++) {
            pLight[i].useLight(uniformPointLight[i].uniformAmbientIntensity, uniformPointLight[i].uniformColour,
                    uniformPointLight[i].uniformDiffuseIntensity, uniformPointLight[i].uniformPosition,
                    uniformPointLight[i].uniformConstant, uniformPointLight[i].uniformLinear, uniformPointLight[i].uniformExponent);
        }
    }

    public int getUniformModel() {
        return uniformModel;
    }

    public int getUniformProjection() {
        return uniformProjection;
    }

    public int getUniformView() { return uniformView; }

    public int getUniformAmbientIntensity() {
        return uniformAmbientIntensity;
    }

    public int getUniformAmbientColour() {
        return uniformAmbientColour;
    }

    public int getUniformDiffuseIntensity() {
        return uniformDiffuseIntensity;
    }

    public int getUniformDirection() {
        return uniformDirection;
    }

    public int getUniformEyePosition() {
        return uniformEyePosition;
    }

    public int getUniformSpecularIntensity() {
        return uniformSpecularIntensity;
    }

    public int getUniformShininess() {
        return uniformShininess;
    }
}
