import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL33.*;


public class ShadowMap {

    protected int FBO;
    protected int shadowMap;
    protected int shadowWidth;
    protected int shadowHeight;

    public ShadowMap() {
        FBO = glGenFramebuffers();
        shadowMap = glGenTextures();
    }

    boolean init(int width, int height) {
        ByteBuffer texData = null;

        shadowWidth = width;
        shadowHeight = height;

        glBindTexture(GL_TEXTURE_2D, shadowMap);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, shadowWidth, shadowHeight, 0, GL_DEPTH_COMPONENT, GL_FLOAT, texData);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        float[] bColor = {1.0f, 1.0f, 1.0f, 1.0f};
        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, bColor);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glBindFramebuffer(GL_FRAMEBUFFER, FBO);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowMap, 0);

        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("Framebuffer Error : " + status);
            return false;
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return true;
    }

    void write() {
        glBindFramebuffer(GL_FRAMEBUFFER, FBO);
    }

    void read(int textureUnit) {
        glActiveTexture(textureUnit);
        glBindTexture(GL_TEXTURE_2D, shadowMap);
    }

    void deleteShadowMap() {
        glDeleteFramebuffers(FBO);

        glDeleteTextures(shadowMap);
    }

    public int getShadowMap() {
        return shadowMap;
    }

    public int getShadowWidth() {
        return shadowWidth;
    }

    public int getShadowHeight() {
        return shadowHeight;
    }

}