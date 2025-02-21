import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL33.*;

public class OmniShadowMap extends ShadowMap {


    public OmniShadowMap() {
        super();
    }

    boolean init(int width, int height) {
        ByteBuffer texData = null;

        shadowWidth = width;
        shadowHeight = height;

        glBindTexture(GL_TEXTURE_CUBE_MAP, shadowMap);

        for (int i = 0; i < 6; i++) {
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_DEPTH_COMPONENT,
                    shadowWidth, shadowHeight, 0, GL_DEPTH_COMPONENT, GL_FLOAT, texData);
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE); // For Z coords

        glBindFramebuffer(GL_FRAMEBUFFER, FBO);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadowMap, 0);

        // Not interested in colour values here, just in shadows
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
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, FBO);
    }

    void read(int textureUnit) {
        glActiveTexture(textureUnit);
        glBindTexture(GL_TEXTURE_CUBE_MAP, shadowMap);
    }

}