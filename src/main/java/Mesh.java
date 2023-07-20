import java.io.*;
import java.text.DecimalFormat;

import static org.lwjgl.opengl.GL33.GL_FLOAT;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.opengl.GL33.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL33.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL33.glVertexAttribPointer;
import static org.lwjgl.opengl.GL33.glBindVertexArray;
import static org.lwjgl.opengl.GL33.glGenVertexArrays;

public class Mesh {
    private int VAO;
    private int VBO;
    private int IBO;
    private int indexCount;
    private int vertexCount;

    public Mesh() {
        // This may be a better place to put the code in createMesh().
    }

    private void outputMesh(float[] vertices, int[] indices) throws IOException {
        FileWriter meshFile = new FileWriter("D:\\tmp\\mesh_java.txt");
        BufferedWriter bw = new BufferedWriter(meshFile);

        bw.write("Vertices...");
        bw.newLine();
        DecimalFormat df = new DecimalFormat("#0.0000");
        for (int i = 0; i < vertices.length; i+=8) {
            StringBuilder line = new StringBuilder();
            line.append(df.format(vertices[i]));
            line.append(", ");
            line.append(df.format(vertices[i+1]));
            line.append(", ");
            line.append(df.format(vertices[i+2]));
            line.append(", ");
            line.append(df.format(vertices[i+3]));
            line.append(", ");
            line.append(df.format(vertices[i+4]));
            line.append(", ");
            line.append(df.format(vertices[i+5]));
            line.append(", ");
            line.append(df.format(vertices[i+6]));
            line.append(", ");
            line.append(df.format(vertices[i+7]));
            bw.write(line.toString());
            bw.newLine();
        }

        bw.write("Indices...");
        bw.newLine();
        for (int i = 0; i < indices.length; i+=3) {
            StringBuilder line = new StringBuilder();
            line.append(indices[i]);
            line.append(", ");
            line.append(indices[i+1]);
            line.append(", ");
            line.append(indices[i+2]);
            bw.write(line.toString());
            bw.newLine();
        }

        bw.flush();
        bw.close();
    }

    public void createMesh(float[] vertices, int[] indices) {
        indexCount = indices.length;
        vertexCount = vertices.length;
        //System.out.println("Mesh has " + indices.length + " indices");

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

        /*
        try {
            outputMesh(vertices, indices);
        } catch (IOException e) {
            e.printStackTrace();
        }
         */
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

    public int getIndexCount() {
        return indexCount;
    }

    public int getVertexCount() {
        return vertexCount;
    }
}
