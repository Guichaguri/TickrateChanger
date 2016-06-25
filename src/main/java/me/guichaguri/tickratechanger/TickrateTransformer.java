package me.guichaguri.tickratechanger;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

/**
 * @author Guilherme Chaguri
 */
public class TickrateTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String name2, byte[] bytes) {
        if(bytes == null) return null;

        try {
            if(name.equals("net.minecraft.server.MinecraftServer")) {
                return patchServerTickrate(bytes);
            } else if(name.equals("paulscode.sound.SoundSystem")) {
                return patchSoundSystem(bytes);
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
                for(AbstractInsnNode node : method.instructions.toArray()) {

                    if(node instanceof LdcInsnNode) {
                        LdcInsnNode ldcNode = (LdcInsnNode)node;
                        if((ldcNode.cst instanceof Long) && ((Long)ldcNode.cst == 50L)) {
                            list.add(new FieldInsnNode(Opcodes.GETSTATIC, "me/guichaguri/tickratechanger/TickrateChanger", "MILISECONDS_PER_TICK", "J"));
                            continue;
                        }
                    }

                    list.add(node);
                }

                method.instructions.clear();
                method.instructions.add(list);
            }

        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    public byte[] patchSoundSystem(byte[] bytes) {
        TickrateChanger.LOGGER.info("Patching sound system...");

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        Iterator<MethodNode> methods = classNode.methods.iterator();
        while(methods.hasNext()) {
            MethodNode method = methods.next();
            if(method.name.equals("setPitch") && method.desc.equals("(Ljava/lang/String;F)V")) {
                InsnList inst = new InsnList();
                inst.add(new VarInsnNode(Opcodes.FLOAD, 2));
                inst.add(new FieldInsnNode(Opcodes.GETSTATIC, "me/guichaguri/tickratechanger/TickrateChanger", "GAME_SPEED", "F"));
                inst.add(new InsnNode(Opcodes.FMUL));
                inst.add(new VarInsnNode(Opcodes.FSTORE, 2));
                inst.add(method.instructions);
                method.instructions = inst;
                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }

}
