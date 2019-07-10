package com.lc.asm;

import com.lc.asm.service.impl.TestServiceImpl;
import com.lc.asm.utils.ASMUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class Application {

    public static void main(String[] args) {
        String name = "com.lc.study.Test";
        Class test = ASMUtils.createClass(name, null, TestServiceImpl.class, false);
        try {
            String classFile = ASMUtils.getClassFilePath(name);
            System.out.println("class文件位置：" + classFile);
            System.out.println("************* 获取到的当前类(包括父类的)所有方法");
            for (Method method : test.getMethods()) {
                System.out.println("> 方法：" + method.getName());
                Class[] parameterTypes = method.getParameterTypes();
                System.out.println(">>> 入参数量：" + parameterTypes.length);
                for (Class c : parameterTypes) {
                    System.out.println(">>>>>> 参数：" + c.getSimpleName());
                }
                System.out.println(">>> 返回类型：" + method.getReturnType().getSimpleName());
                Annotation[] annotations = method.getDeclaredAnnotations();
                System.out.println(">>> 方法注解数量：" + annotations.length);
                for (Annotation a : annotations) {
                    System.out.println(">>>>>> 注解：" + a.toString());
                }
            }
            System.out.println("************* 打印完成，开始方法调用测试");
            //调用当前类的main方法
            test.getDeclaredMethod("main", new String[]{}.getClass()).invoke(null, new Object[]{null});

            //调用父类中的的方法
            //先实例化，创建一个对象
            Object object = test.newInstance();
            //调用父类的 getTest 方法
            Method getTest = test.getMethod("getTest");
            getTest.invoke(object);
            //调用父类的 updateTest 方法
            Method updateTest = test.getMethod("updateTest", Integer.class);
            Object obj = updateTest.invoke(object, new Integer[]{1});
            System.out.println("updateTest 返回参数：" + obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
