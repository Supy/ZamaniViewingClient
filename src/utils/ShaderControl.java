package utils;

import javax.media.opengl.GL2;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ShaderControl class adapted from http://www.guyford.co.uk/showpage.php?id=50&page=How_to_setup_and_load_GLSL_Shaders_in_JOGL_2.0
 */
public class ShaderControl {

    private static final Logger log = Logger.getLogger(ShaderControl.class.getName());

    private int flatShaderProgram;
    private int smoothShaderProgram;
    private String[] vertexShaderFlatSrc = null;
    private String[] vertexShaderSrc = null;
    private String[] fragmentShaderSrc = null;
    private static boolean useFlatShader = false;

    private GL2 gl;

    public ShaderControl(GL2 gl){
        this.gl = gl;
    }

    // Loads the shader in a file.
    public void loadShader(String name, ShaderType shaderType) throws IOException {
        StringBuilder sb = new StringBuilder();

        InputStream is = new FileInputStream(name);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append('\n');
        }
        is.close();

        String[] shader = new String[]{sb.toString()};

        switch(shaderType) {
            case VERTEX_FLAT:
                this.vertexShaderFlatSrc = shader;
                break;
            case VERTEX:
                this.vertexShaderSrc = shader;
                break;
            case FRAGMENT:
                this.fragmentShaderSrc = shader;
                break;
        }
    }

    public void attachShaders() throws Exception {

        if(this.vertexShaderFlatSrc == null){
            throw new Exception("flat vertex shader not loaded");
        }else{
            this.flatShaderProgram = gl.glCreateProgram();
            compileShader(GL2.GL_VERTEX_SHADER, this.vertexShaderFlatSrc, this.flatShaderProgram);
            gl.glLinkProgram(this.flatShaderProgram);
            validateProgram(this.flatShaderProgram);
        }

        if(this.vertexShaderSrc == null || this.fragmentShaderSrc == null){
            log.log(Level.WARNING, "smooth vertex or fragment shader not loaded. only flat shader can be used");
        }else{
            this.smoothShaderProgram = gl.glCreateProgram();
            compileShader(GL2.GL_VERTEX_SHADER, this.vertexShaderSrc, this.smoothShaderProgram);
            compileShader(GL2.GL_FRAGMENT_SHADER, this.fragmentShaderSrc, this.smoothShaderProgram);
            gl.glLinkProgram(this.smoothShaderProgram);
            validateProgram(this.smoothShaderProgram);
        }
    }

    private void compileShader(int shaderType, String[] shaderSource, int program) {
        int shader = gl.glCreateShader(shaderType);

        gl.glShaderSource(shader, 1, shaderSource, null, 0);
        gl.glCompileShader(shader);
        gl.glAttachShader(program, shader);
    }

    private void validateProgram(int program) {
        gl.glValidateProgram(program);
        IntBuffer intBuffer = IntBuffer.allocate(1);
        gl.glGetProgramiv(program, GL2.GL_LINK_STATUS, intBuffer);

        if (intBuffer.get(0) != 1)
        {
            gl.glGetProgramiv(program, GL2.GL_INFO_LOG_LENGTH, intBuffer);
            int size = intBuffer.get(0);
            System.err.println("Program link error: ");
            if (size > 0)
            {
                ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                gl.glGetProgramInfoLog(program, size, intBuffer, byteBuffer);
                for (byte b : byteBuffer.array())
                {
                    System.err.print((char) b);
                }
            }
            else
            {
                System.out.println("Unknown");
            }
            System.exit(1);
        }
    }

    private boolean canUseSmoothShader(){
        return this.vertexShaderSrc != null && this.fragmentShaderSrc != null;
    }

    public void useShader() {
        gl.glUseProgram((!this.useFlatShader && canUseSmoothShader()) ? smoothShaderProgram : flatShaderProgram );
    }

    public void dontUseShader() {
        gl.glUseProgram(0);
    }

    public static void toggleFlatShader() {
        useFlatShader = !useFlatShader;
    }
}