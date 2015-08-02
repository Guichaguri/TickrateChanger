package me.guichaguri.tickratechanger;

import java.util.Iterator;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Guilherme Chaguri
 */
public class TickrateTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String name2, byte[] bytes) {
        if(bytes == null) return null;

        try {
            if(name.equals("net.minecraft.server.MinecraftServer") || name2.equals("net.minecraft.server.MinecraftServer")) {
                return patchServerTickrate(bytes);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return bytes;
    }

    public byte[] patchServerTickrate(byte[] bytes) {
        TickrateChanger.LOGGER.info("Applying ASM to Minecraft methods...");

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        Iterator<MethodNode> methods = classNode.methods.iterator();
        while(methods.hasNext()) {
            MethodNode method = methods.next();
            if((method.name.equals("run")) && (method.desc.equals("()V"))) {
                InsnList list = new InsnList();
                intrucLoop: for(AbstractInsnNode node : method.instructions.toArray()) {

                    if(node instanceof LdcInsnNode) {
                        LdcInsnNode ldcNode = (LdcInsnNode)node;
                        if((ldcNode.cst instanceof Long) && ((Long)ldcNode.cst == 50L)) {
                            list.add(new FieldInsnNode(Opcodes.GETSTATIC, "me/guichaguri/tickratechanger/TickrateChanger", "MILISECONDS_PER_TICK", "J"));
                            continue intrucLoop;
                        }
                    }

                    list.add(node);
                }

                method.instructions.clear();
                method.instructions.add(list);
            }

        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }

}
