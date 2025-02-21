import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.opengl.GL33.GL_TEXTURE0;
import static org.lwjgl.opengl.GL33.glActiveTexture;
import static org.lwjgl.opengl.GL33.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;

public class Texture {
    private int textureID;

    private int width;
    private int height;
    private int bitDepth;

    private String fileLocation;

    public Texture(String fileLocation) {
        textureID = 0;
        width = 0;
        height = 0;
        bitDepth = 0;
        String root = System.getProperty("user.dir");
        this.fileLocation = root + "/target/classes/" + fileLocation;
    }

    public boolean loadTextureA() {
        IntBuffer x = BufferUtils.createIntBuffer(1);
        IntBuffer y = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        ByteBuffer texData = stbi_load(fileLocation, x, y, channels, 0);
        if (texData == null) {
            System.out.println("Failed to find: " + fileLocation);
            return false;
        }
        width = x.get(0);
        height = y.get(0);
        bitDepth = channels.get(0);

        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, texData);
        glGenerateMipmap(GL_TEXTURE_2D);

        glBindTexture(GL_TEXTURE_2D, 0);

        stbi_image_free(texData);

        return true;
    }

    public boolean loadTexture() {
        IntBuffer x = BufferUtils.createIntBuffer(1);
        IntBuffer y = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        ByteBuffer texData = stbi_load(fileLocation, x, y, channels, 0);
        if (texData == null) {
            System.out.println("Failed to find: " + fileLocation);
            return false;
        }
        width = x.get(0);
        height = y.get(0);
        bitDepth = channels.get(0);

        //System.out.println("Texture width = " + width + " height = " + height);
        //System.out.println("Texdata length is " + texData.remaining());

        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, texData);
        glGenerateMipmap(GL_TEXTURE_2D);

        glBindTexture(GL_TEXTURE_2D, 0);

        stbi_image_free(texData);

        return true;
    }

    public void useTexture() {
        glActiveTexture(GL_TEXTURE1);

        glBindTexture(GL_TEXTURE_2D, textureID);
    }

    public void clearTexture() {
        glDeleteTextures(textureID);
        textureID = 0;
        width = 0;
        height = 0;
        bitDepth = 0;
        fileLocation = null;
    }
}
