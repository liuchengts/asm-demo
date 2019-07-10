package com.lc.asm.utils;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * asm字节码操作
 *
 * @author liucheng
 * @create 2018-06-14 18:47
 **/
public class ASMUtils extends ClassLoader {
    private static class LazyHolder {
        private static final ASMUtils INSTANCE = new ASMUtils();
    }

    private ASMUtils() {

    }

    public static final ASMUtils getInstance() {
        return ASMUtils.LazyHolder.INSTANCE;
    }

    static ThreadLocal<Map<String, String>> mapThreadLocal = new ThreadLocal<>();

    /**
     * 创建class并且加载到内存
     *
     * @param name              要创建的class名称 相当于 class.getName()
     * @param interfaceClasss   要实现的的接口class，可以是null
     * @param extendsClasssImpl 要继承的类class
     * @param isInterface       是一个接口true
     * @return 返回创建好的class对象
     */
    public static Class createClass(String name, Set<Class> interfaceClasss, Class extendsClasssImpl, boolean isInterface) {
        String classPath = name.replace(".", File.separator);
        //定义class书写器
        ClassWriter cw = new ClassWriter(0);
        if (extendsClasssImpl == null) {
            //默认继承顶级 Object 类
            extendsClasssImpl = Object.class;
        }
        if (interfaceClasss == null || interfaceClasss.isEmpty()) {
            //第一个参数 V1_1 是生成的class的版本号， 对应class文件中的主版本号和次版本号， 即minor_version和major_version
            //第二个参数 ACC_PUBLIC 表示该类的访问标识。这是一个public的类。 对应class文件中的access_flags
            //第三个参数是生成的类的类名。 需要注意，这里是类的全限定名。 如果生成的class带有包名， 如com.jg.zhang.Example， 那么这里传入的参数必须是com/jg/zhang/Example  。对应class文件中的this_class
            //第四个参数是和泛型相关的， 这里我们不关新， 传入null表示这不是一个泛型类。这个参数对应class文件中的Signature属性（attribute）
            //第五个参数是当前类的父类的全限定名。 没有的话默认应该继承Object。 这个参数对应class文件中的super_class
            //第六个参数是String[]类型的， 传入当前要生成的类的直接实现的接口。 这里这个类没实现任何接口， 所以传入null 。 这个参数对应class文件中的interfaces
            cw.visit(Opcodes.V1_1, Opcodes.ACC_PUBLIC, classPath, null, Type.getInternalName(extendsClasssImpl), null);
        } else {
            Set<String> interfaceSet = new HashSet<>();
            for (Class las : interfaceClasss) {
                interfaceSet.add(Type.getInternalName(las));
            }
            cw.visit(Opcodes.V1_1, Opcodes.ACC_PUBLIC, classPath, null, Type.getInternalName(extendsClasssImpl), interfaceSet.toArray(new String[interfaceSet.size()]));
        }

        //生成默认的构造方法
        //第一个参数是 ACC_PUBLIC ， 指定要生成的方法的访问标志。 这个参数对应method_info 中的access_flags 。 
        //第二个参数是方法的方法名。 对于构造方法来说， 方法名为<init> 。 这个参数对应method_info 中的name_index ， name_index引用常量池中的方法名字符串。 
        //第三个参数是方法描述符， 在这里要生成的构造方法无参数， 无返回值， 所以方法描述符为 ()V  。 这个参数对应method_info 中的descriptor_index 。 
        //第四个参数是和泛型相关的， 这里传入null表示该方法不是泛型方法。这个参数对应method_info 中的Signature属性。
        //第五个参数指定方法声明可能抛出的异常。 这里无异常声明抛出， 传入null 。 这个参数对应method_info 中的Exceptions属性
        MethodVisitor mw = cw.visitMethod(Opcodes.ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null);
        //生成构造方法的字节码指令
        //1 调用visitVarInsn方法，生成aload指令， 将第0个本地变量（也就是this）压入操作数栈。
        //2 调用visitMethodInsn方法， 生成invokespecial指令， 调用父类（也就是Object）的构造方法。
        //3 调用visitInsn方法，生成return指令， 方法返回。 
        //4 调用visitMaxs方法， 指定当前要生成的方法的最大局部变量和最大操作数栈。 对应Code属性中的max_stack和max_locals 。 
        //5 最后调用visitEnd方法， 表示当前要生成的构造方法已经创建完成。
        mw.visitVarInsn(Opcodes.ALOAD, 0);
        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(extendsClasssImpl), "<init>", "()V", isInterface);
        mw.visitInsn(Opcodes.RETURN);
        mw.visitMaxs(1, 1);
        mw.visitEnd();

        //生成mian方法
        mw = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                "main",
                "([Ljava/lang/String;)V",
                null,
                null);

        //生成main方法中的字节码指令
        mw.visitFieldInsn(Opcodes.GETSTATIC,
                "java/lang/System",
                "out",
                "Ljava/io/PrintStream;");
        mw.visitLdcInsn("Hello world!");
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream",
                "println",
                "(Ljava/lang/String;)V", isInterface);


        // 获取生成的class文件对应的二进制流
        byte[] code = cw.toByteArray();
        // 写文件到本地
        String classFile = writerFile(code, classPath);
        Map map = new HashMap();
        map.put(name, classFile);
        mapThreadLocal.set(map);
        //直接将二进制流加载到内存中
        return ASMUtils.getInstance().defineClass(name, code, 0, code.length);
    }

    /***
     *
     * 生成main方法
     * @param cw class书写器
     * @param mw 方法生成器
     * @param ldc
     * @param owner 要继承的类
     * @param name 方法名
     * @param descriptor 方法返回类型
     * @param isInterface 方法返回类型
     */
    private static void createMain(ClassWriter cw, MethodVisitor mw, Object ldc, String owner, String name,
                                   String descriptor, boolean isInterface) {

    }

    /**
     * 将二进制流写成文件到本地
     *
     * @param classByteArray 二进制数组
     * @param classPath      class的相对路径路径
     * @return 返回文件所在的位置
     */
    public static String writerFile(byte[] classByteArray, String classPath) {
        //将二进制流写到本地磁盘上
        FileOutputStream fos = null;
        String classFile = null;
        try {
            String packageName = classPath.substring(0, classPath.lastIndexOf(File.separator));
            String className = classPath.substring(classPath.lastIndexOf(File.separator) + 1, classPath.length());
            //这里有可能创造当前不存在的资源路径
            String rootPath = ASMUtils.getInstance().getResource("").getPath();
            classFile = rootPath + packageName;
            mkdir(classFile);
            classFile = classFile + File.separator + className + ".class";
            fos = new FileOutputStream(classFile);
            fos.write(classByteArray);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return classFile;
    }

    /**
     * 根据创建class的name获取生成好的文件路径
     *
     * @param name 创建class字节码时传递的名称
     * @return 返回class文件所在路径
     */
    public static String getClassFilePath(String name) {
        if (!mapThreadLocal.get().containsKey(name)) return null;
        return mapThreadLocal.get().get(name);
    }

    /**
     * 创建文件夹
     * 可以创建任意深度的文件夹
     * 检测不存在的文件夹就会创建
     *
     * @param path 要创建的路径
     */
    public static void mkdir(String path) {
        String[] folders = path.split(File.separator);
        String folderPath = "";
        for (String f : folders) {
            folderPath += File.separator + f;
            File folder = new File(folderPath);
            if (!folder.exists() && !folder.isDirectory()) folder.mkdirs();
        }
    }
}
