package com.lanlengran.mvp_helper;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Util_File {
    /**
     * 将字符串写入文件
     * @param javatempelt
     * @param fileName
     */
    public static void string2Stream(String javatempelt, String fileName) {
        File file=new File(fileName);
        if (file.exists()){
            file.delete();
        }else {
            if (!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }
        }

        try {
            PrintWriter printWriter=new PrintWriter(file);
            printWriter.print(javatempelt);
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取Java文件的Class类对象
     */
    public static PsiClass getFileClass(PsiFile file) {
        for (PsiElement psiElement : file.getChildren()) {
            if (psiElement instanceof PsiClass) {
                return (PsiClass) psiElement;
            }
        }
        return null;
    }

    /**
     * 字符串首字母变小写
     * @param str
     * @return
     */
    public static String stringFirstToLowerCase(String str){
        String lowCaseStr=str.toLowerCase();
        return lowCaseStr.substring(0,1)+str.substring(1,str.length());
    }
}
