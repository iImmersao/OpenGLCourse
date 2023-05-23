import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Camera {

    private Vector3f position;

    private Vector3f up;

    private Vector3f worldUp;

    private Vector3f front;

    private Vector3f right;

    private float yaw;

    private float pitch;

    private float moveSpeed;

    private float turnSpeed;

    public Camera(Vector3f startPosition, Vector3f startUp, float startYaw, float startPitch,
                  float startMoveSpeed, float startTurnSpeed) {
        position = startPosition;
        worldUp = startUp;
        yaw = startYaw;
        pitch = startPitch;
        moveSpeed = startMoveSpeed;
        turnSpeed = startTurnSpeed;

        front = new Vector3f(0.0f, 0.0f, -1.0f);

        update();
    }

    public void keyControl(boolean[] keys, float deltaTime) {
        float velocity = moveSpeed * deltaTime;

        if (keys[GLFW.GLFW_KEY_W]) {
            position = position.add(front.mul(velocity));
        }

        if (keys[GLFW.GLFW_KEY_S]) {
            position = position.sub(front.mul(velocity));
        }

        if (keys[GLFW.GLFW_KEY_A]) {
            position = position.sub(right.mul(velocity));
        }

        if (keys[GLFW.GLFW_KEY_D]) {
            position = position.add(right.mul(velocity));
        }
    }

    public void mouseControl(double xChange, double yChange) {
        xChange *= turnSpeed;
        yChange *= turnSpeed;

        yaw += xChange;
        pitch += yChange;

        if (pitch > 89.0f) {
            pitch = 89.0f;
        }

        if (pitch < -89.0f) {
            pitch = -89.0f;
        }

        update();
    }

    public Matrix4f calculateViewMatrix() {
        Matrix4f view = new Matrix4f();
        return view.lookAt(position, position.add(front), up);
    }

    private void update() {
        front.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        front.y = (float) Math.sin(Math.toRadians(pitch));
        front.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        front = front.normalize();

        right = front.cross(worldUp).normalize();

        up = right.cross(front).normalize();
    }
}
