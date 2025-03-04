import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL33.glEnable;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private final int width;
    private final int height;
    private long mainWindow;
    private final int[] bufferWidth = new int[1];
    private final int[] bufferHeight = new int[1];
    private double xChange;
    private double yChange;
    private final boolean[] keys = new boolean[1024];
    private boolean mouseFirstMoved;
    private double lastX;
    private double lastY;

    private GLFWKeyCallbackI handleKeys;
    private GLFWCursorPosCallbackI handleMouse;

    public Window(int windowWidth, int windowHeight) {
        this.width = windowWidth;
        this.height = windowHeight;
        xChange = 0.0f;
        yChange = 0.0f;
        mouseFirstMoved = true;

        for (int i = 0; i < 1024; i++) {
            keys[i] = false;
        }
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

        createCallbacks();

        glfwSetInputMode(mainWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        glEnable(GL_DEPTH_TEST);

        GL33.glViewport(0, 0, bufferWidth[0], bufferHeight[0]);

        return 0;
    }

    public int getBufferWidth() { return bufferWidth[0]; }

    public int getBufferHeight() { return bufferHeight[0]; }

    public boolean getShouldClose() { return glfwWindowShouldClose(mainWindow); }

    public void createCallbacks() {
        glfwSetKeyCallback(mainWindow, handleKeys = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int code, int action, int mode) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                    glfwSetWindowShouldClose(window, true);
                }

                if (key >= 0 && key < 1024) {
                    if (action == GLFW_PRESS) {
                        keys[key] = true;
                    }
                    else if (action == GLFW_RELEASE) {
                        keys[key] = false;
                    }
                }

            }
        });

        glfwSetCursorPosCallback(mainWindow, handleMouse = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xPos, double yPos) {
                if (mouseFirstMoved) {
                    lastX = xPos;
                    lastY = yPos;
                    mouseFirstMoved = false;
                }
                xChange = xPos - lastX;
                yChange = lastY - yPos;

                lastX = xPos;
                lastY = yPos;
            }
        });
    }

    public double getXChange() {
        double theChange = xChange;
        xChange = 0;
        return theChange;
    }

    public double getYChange() {
        double theChange = yChange;
        yChange = 0;
        return theChange;
    }

    public void swapBuffers() { glfwSwapBuffers(mainWindow); }

    public void handleKeys(long window, int key, int code, int action, int mode) {

        if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
            glfwSetWindowShouldClose(window, true);
        }

        if (key >= 0 && key < 1024) {
            if (action == GLFW_PRESS) {
                keys[key] = true;
            } else if (action == GLFW_RELEASE) {
                keys[key] = false;
            }
        }
    }

    public void handleMouse(long window, double xPos, double yPos) {
        if (mouseFirstMoved) {
            lastX = xPos;
            lastY = yPos;
            mouseFirstMoved = false;
        }

        xChange = xPos - lastX;
        yChange = lastY - yPos;

        lastX = xPos;
        lastY = yPos;
    }

    public boolean[] getKeys() {
        return keys;
    }
}
