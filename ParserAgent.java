import java.lang.instrument.*;
import java.security.ProtectionDomain;
import org.objectweb.asm.*;

public class ParserAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
    	System.out.println("if this were a real agent you'd be transformed by now");
        ParserTransformer xform = new ParserTransformer();
        inst.addTransformer(xform);
    }

    public static void main(String args[]) {
    	System.out.println("this is not the main you are looking for");
    }
}

class ParserTransformer implements ClassFileTransformer {

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {

    	if (!("Parser".equals(className))) {
    		return classfileBuffer;
    	}

        ClassReader reader = new ClassReader(classfileBuffer);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor visitor = new ParserClassVisitor(classWriter);
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);

        return classWriter.toByteArray();
    }
}

class ParserClassVisitor extends ClassVisitor {

    private String owner;
    private boolean isInterface;

    public ParserClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM4, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        cv.visit(version, access, name, signature, superName, interfaces);
        owner = name;
        isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (mv != null && !isInterface) {
            mv = new ParserMethodVisitor(mv, access, name, desc, owner);
        }
        return mv;
    }
}

class ParserMethodVisitor extends MethodVisitor implements Opcodes {

    private final String owner;
    private final String name;
    private final String desc;
    private int complexity = 0;

    public ParserMethodVisitor(MethodVisitor mv, int access, String name, String desc, String owner) {
        super(Opcodes.ASM4, mv);
        this.owner = owner;
        this.name = name;
        this.desc = desc;
    }
    
    @Override
    public void visitCode() {
        super.visitCode();
        if (!("()Z".equals(desc))) {
        	return;
        }
		visitVarInsn(ALOAD, 0);
		visitLdcInsn(name);
		visitMethodInsn(INVOKEVIRTUAL, "Parser", "mark", "(Ljava/lang/String;)V");

        complexity++;
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        super.visitJumpInsn(opcode, label);
        complexity++;
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        System.err.println("Complexity of method " + owner + "#" + name + " is " + complexity);
    }
}