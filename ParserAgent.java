import java.io.*;
import java.lang.instrument.*;
import java.lang.reflect.*;
import java.security.ProtectionDomain;
import org.objectweb.asm.*;
import org.objectweb.asm.util.*;

public class ParserAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
    	if (agentArgs==null || agentArgs.equals("")){
    	    System.out.println("if this were a real agent you'd be transformed by now");
    	} else {
    		System.out.println("here we go transforming with arguments '"+agentArgs+"'");
			ParserTransformer xform = new ParserTransformer();
            xform.setArgs(agentArgs);
	        inst.addTransformer(xform);
    	}
    }

    public static void main(String args[]) {
    	System.out.println("this is not the main you are looking for");
    }
}

class ParserTransformer implements ClassFileTransformer, Opcodes {

	String agentArgs = null;

	void setArgs(String agentArgs) {
		this.agentArgs = agentArgs;
	}

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {

    	if (!("Parser".equals(className))) {
    		return classfileBuffer;
    	}

        ClassReader reader = new ClassReader(classfileBuffer);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    	PrintWriter printWriter = new PrintWriter(System.out);
    	ClassVisitor visitor = classWriter;

    	for (char ch : agentArgs.toCharArray()){
            System.out.println("now switching on: "+ch);
    		switch (ch) {
                case 'a':
                    visitor = new TraceClassVisitor(visitor, new ASMifier() , printWriter);
                    break;
                case 't':
                    visitor = new TraceClassVisitor(visitor, printWriter);
                    break;
    			case 'p':
    				visitor = new ParserClassVisitor(visitor);
    				break;
    			case 'c':
    				visitor = new CheckClassAdapter(visitor);
                    break;
    			default:
    				System.out.println("can't transform with visitor: "+ch);
    		}
	    }

        // ParserClassVisitor.explain(visitor);
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);

        printWriter.flush();
        return classWriter.toByteArray();

    }
}

class ParserClassVisitor extends ClassVisitor {

    private String owner;
    private boolean isInterface;

    public ParserClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM4, cv);
    }

    static void explain(ClassVisitor cv) {
        ClassVisitor v = cv;
        try {
            Field field = ClassVisitor.class.getDeclaredField("cv");
            field.setAccessible(true);
            while (v != null) {
                System.out.println("we'll visit with: "+v);
                // v = v.cv;
                v = (ClassVisitor) field.get(v);
            }
        } catch (Exception e) {
            System.out.println("Can't reflect: "+e);
        }
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
    private final int access;

    public ParserMethodVisitor(MethodVisitor mv, int access, String name, String desc, String owner) {
        super(Opcodes.ASM4, mv);
        this.owner = owner;
        this.name = name;
        this.desc = desc;
        this.access = access;
    }
    
    @Override
    public void visitCode() {
        super.visitCode();
        if (!("()Z".equals(desc))) {
        	return;
        }
        if (name.contains("_")) {
        	return;
        }
        if ((access & ACC_PRIVATE) != 0) {
        	return;
        }
		visitVarInsn(ALOAD, 0);
		visitLdcInsn(name);
		visitMethodInsn(INVOKEVIRTUAL, "Parser", "mark", "(Ljava/lang/String;)V");
    }

}