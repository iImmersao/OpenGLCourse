import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

public class Mesh {
    private int VAO;
    private int VBO;
    private int IBO;
    private int indexCount;

    public Mesh() {
        // This may be a better place to put the code in createMesh().
    }

    public void createMesh(float[] vertices, int[] indices) {
        indexCount = indices.length;

        VAO = glGenVertexArrays();
        glBindVertexArray(VAO);

        IBO = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, IBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * 4, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 8 * 4, 3 * 4);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 8 * 4, 5 * 4);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        glBindVertexArray(0);
    }

    void renderMesh() {
        glBindVertexArray(VAO);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, IBO);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    void clearMesh() {
        if (IBO != 0) {
            glDeleteBuffers(IBO);
            IBO = 0;
        }

        if (VBO != 0) {
            glDeleteBuffers(VBO);
            VBO = 0;
        }

        if (VAO != 0) {
            glDeleteBuffers(VAO);
            VAO = 0;
        }

        indexCount = 0;
    }
}
