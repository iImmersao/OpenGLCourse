import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Model {

    List<Mesh> meshList;

    List<Texture> textureList;

    List<Integer> meshToTex;

    public Model() {
        meshList = new ArrayList<>();
        textureList = new ArrayList<>();
        meshToTex = new ArrayList<>();
    }

    public void loadModel(String fileName) {
        AIScene scene = Assimp.aiImportFile(fileName,
                Assimp.aiProcess_Triangulate | Assimp.aiProcess_FlipUVs | Assimp.aiProcess_GenSmoothNormals | Assimp.aiProcess_JoinIdenticalVertices);
        if (scene == null) {
            System.out.println("Model " + fileName + " failed to load: " + Assimp.aiGetErrorString());
            return;
        }

        loadNode(scene.mRootNode(), scene);

        loadMaterials(scene);
    }

    public void renderModel() {
        for (int i = 0; i < meshList.size(); i++) {
            int materialIndex = meshToTex.get(i);

            if (materialIndex < textureList.size() && textureList.get(materialIndex) != null) {
                textureList.get(materialIndex).useTexture();
            }

            meshList.get(i).renderMesh();
        }
    }

    private void loadNode(AINode node, AIScene scene) {
        System.out.println("Processing node " + node.mName().dataString());
        int numMeshes = scene.mNumMeshes();
        PointerBuffer aiMeshes = scene.mMeshes();
        for (int i = 0; i < numMeshes; i++) {
            AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
            loadMesh(aiMesh, scene);
        }

        PointerBuffer children = node.mChildren();
        for (int i = 0; i < node.mNumChildren(); i++) {
            AINode childNode = AINode.create(children.get(i));
            loadNode(childNode, scene);
        }
    }

    private static void processVertices(AIMesh aiMesh, List<Float> vertices) {
        AIVector3D.Buffer aiVertices = aiMesh.mVertices();
        while (aiVertices.remaining() > 0) {
            AIVector3D aiVertex = aiVertices.get();
            vertices.add(aiVertex.x());
            vertices.add(aiVertex.y());
            vertices.add(aiVertex.z());
        }
    }

    private static void processNormals(AIMesh aiMesh, List<Float> normals) {
        AIVector3D.Buffer aiNormals = aiMesh.mNormals();
        while (aiNormals != null && aiNormals.remaining() > 0) {
            AIVector3D aiNormal = aiNormals.get();
            normals.add(aiNormal.x());
            normals.add(aiNormal.y());
            normals.add(aiNormal.z());
        }
    }

    private static void processTextCoords(AIMesh aiMesh, List<Float> textures) {
        AIVector3D.Buffer textCoords = aiMesh.mTextureCoords(0);
        int numTextCoords = textCoords != null ? textCoords.remaining() : 0;
        for (int i = 0; i < numTextCoords; i++) {
            AIVector3D textCoord = textCoords.get();
            textures.add(textCoord.x());
            textures.add(1 - textCoord.y());
        }
    }

    private static void processIndices(AIMesh aiMesh, List<Integer> indices) {
        int numFaces = aiMesh.mNumFaces();
        AIFace.Buffer aiFaces = aiMesh.mFaces();
        for (int i = 0; i < numFaces; i++) {
            AIFace aiFace = aiFaces.get(i);
            IntBuffer buffer = aiFace.mIndices();
            while (buffer.remaining() > 0) {
                indices.add(buffer.get());
            }
        }
    }

    private static void processMaterial(AIMaterial aiMaterial, List<Material> materials, String texturesDir) throws Exception {
        AIColor4D colour = AIColor4D.create();

        AIString path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, Assimp.aiTextureType_DIFFUSE, 0, path,
                (IntBuffer) null, null, null, null, null, null);
        String textPath = path.dataString();
        Texture texture = null;
        if (textPath != null && textPath.length() > 0) {
            String textureFile = "Textures/" + textPath.lastIndexOf("\\");

        }
    }

    private static Mesh processMesh(AIMesh aiMesh, List<Material> materials) {
        List<Float> vertices = new ArrayList<>();
        List<Float> textures = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        System.out.println("Loading mesh " + aiMesh.mName().dataString());
        processVertices(aiMesh, vertices);
        processNormals(aiMesh, normals);
        processTextCoords(aiMesh, textures);
        processIndices(aiMesh, indices);

        if (textures.size() == 0) {
            int numElements = (vertices.size() / 3) * 2;
            for (int i = 0; i < numElements; i++) {
                textures.add(0.0f);
            }
        }

        int numVertices = vertices.size() / 3;
        int vertexDataSize = vertices.size() + textures.size() + normals.size();
        float[] vertexArr = new float[vertexDataSize];
        int vertexInd = 0;
        for (int i = 0; i < numVertices; i++) {
            vertexArr[vertexInd++] = vertices.get(i * 3);
            vertexArr[vertexInd++] = vertices.get((i * 3) + 1);
            vertexArr[vertexInd++] = vertices.get((i * 3) + 2);

            vertexArr[vertexInd++] = textures.get(i * 2);
            vertexArr[vertexInd++] = textures.get((i * 2) + 1);

            vertexArr[vertexInd++] = -normals.get(i * 3);
            vertexArr[vertexInd++] = -normals.get((i * 3) + 1);
            vertexArr[vertexInd++] = -normals.get((i * 3) + 2);
        }

        Mesh newMesh = new Mesh();
        int[] indexArr = indices.stream().mapToInt(f -> f != null ? f : 0).toArray();
        newMesh.createMesh(vertexArr, indexArr);

        return newMesh;
    }

    private void loadMesh(AIMesh mesh, AIScene scene) {
        List<Material> materials = new ArrayList<>();
        Mesh newMesh = processMesh(mesh, materials);
        meshList.add(newMesh);
        meshToTex.add(mesh.mMaterialIndex());
    }

    private void loadMaterials(AIScene scene) {
        int numMaterials = scene.mNumMaterials();
        System.out.println("Processing " + numMaterials + " materials for this model");
        PointerBuffer aiMaterials = scene.mMaterials();
        for (int i = 0; i < numMaterials; i++) {
            AIMaterial material = AIMaterial.create(aiMaterials.get(i));

            textureList.add(null);

            AIString path = AIString.calloc();
            int textureNo = Assimp.aiGetMaterialTexture(material, Assimp.aiTextureType_DIFFUSE, 0, path, (IntBuffer) null,
                    null, null, null, null, null);
            String textPath = path.dataString();
            Texture texture = null;
            if (textPath != null && textPath.length() > 0) {
                int idx = textPath.lastIndexOf('\\');
                String filename = textPath.substring(idx + 1);

                String texPath = "Textures/" + filename;
                System.out.println("Creating and loading new Texture from: " + texPath);

                textureList.set(i, new Texture(texPath));

                if (!textureList.get(i).loadTexture()) {
                    System.out.println("Failed to load texture at: " + texPath);
                    textureList.set(i, null);
                }
            }

            if (textureList.get(i) == null) {
                textureList.set(i, new Texture("Textures/brick.png"));
                textureList.get(i).loadTextureA();
            }
        }
    }
}
