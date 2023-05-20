import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;

import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private int width = 800;
    private int height = 600;
    private long mainWindow;
    private int[] bufferWidth = new int[1];
    private int[] bufferHeight = new int[1];

    public Window(int windowWidth, int windowHeight) {
        this.width = windowWidth;
        this.height = windowHeight;
    }

    public int initialise() {
        if (!GLFW.glfwInit()) {
            System.out.println("GLFW initialisation failed!");
            System.exit(1);
        }

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);

        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        mainWindow = GLFW.glfwCreateWindow(width, height, "Test Window", NULL, NULL);
        if (mainWindow == NULL) {
            System.out.println("GLFW window creation failed!");
            GLFW.glfwTerminate();
            System.exit(2);
        }

        GLFW.glfwGetFramebufferSize(mainWindow, bufferWidth, bufferHeight);

        GLFW.glfwMakeContextCurrent(mainWindow);

        // This line is essential! Without it, the native code crashes.
        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);

        GL33.glViewport(0, 0, bufferWidth[0], bufferHeight[0]);

        return 0;
    }

    public int getBufferWidth() { return bufferWidth[0]; }

    public int getBufferHeight() { return bufferHeight[0]; }

    public boolean getShouldClose() { return glfwWindowShouldClose(mainWindow); }

    public void swapBuffers() { glfwSwapBuffers(mainWindow); }

}
