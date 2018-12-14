package com.lanlengran.mvp_helper;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;

public class MvpAction extends BaseGenerateAction {

    private PsiFile mFile;
    private PsiClass mClass;
    private PsiElementFactory mFactory;
    protected static final Logger log = Logger.getInstance(MvpAction.class);
    private PsiDirectory mMVPDir;
    private String viewName;
    private String viewIName;
    private String modelName;
    private String presenterName;

    public MvpAction() {
        super(null);
    }

    public MvpAction(CodeInsightActionHandler handler) {
        super(handler);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        // TODO: insert action logic here
        //获取当前点击工程
        Project project = event.getData(PlatformDataKeys.PROJECT);
        Editor editor = event.getData(PlatformDataKeys.EDITOR);

        actionPerformedImpl(project, editor);
    }

    @Override
    public void actionPerformedImpl(@NotNull Project project, Editor editor) {


        mFile = PsiUtilBase.getPsiFileInEditor(editor, project); //获取点击的文件
        mClass = getTargetClass(editor, mFile); //获取点击的类
        if (mClass.getName() == null) {
            return;
        }
        log.info("mClass=====" + mClass.getName());
        mFactory = JavaPsiFacade.getElementFactory(project);
        mMVPDir = createMVPDir(); //创建mvp文件夹
        viewName = mClass.getName();

        creatMVPFile();
        writeActivity(project);
    }
    /**
     * 修改activity
     *
     * @param project
     */
    private void writeActivity(@NotNull Project project) {

        WriteCommandAction.runWriteCommandAction(project, new Runnable() {
            @Override
            public void run() {

                PsiReferenceList list = mClass.getImplementsList();
                list.add(mFactory.createReferenceFromText(viewIName,mClass));



                mClass.addBefore(mFactory.createAnnotationFromText("@Inject", mClass), mClass.getMethods()[0]);
                mClass.addBefore(mFactory.createFieldFromText("protected " + presenterName + " mPrenseter;", mClass), mClass.getMethods()[0]);
                mClass.add(mFactory.createMethodFromText("@Override  public BaseActivity getActivity() {return this;}", mClass));
            }
        });
    }
    private void creatMVPFile() {
        viewIName = mClass.getName() + "ViewI"; //viewI的名称
        modelName = mClass.getName() + "Model"; //model的名称
        presenterName = mClass.getName() + "Presenter"; //presenter的名称

        log.info("mClass=====" + mClass.getName());
        boolean hasModel = false; //是否包含model
        boolean hasPresenter = false; //是否包含presenter
        boolean hasViewI = false; //是否包含viewI

        //查找是否已经包含有mvp文件，如果有的话，则不再创建
        for (PsiFile f : mMVPDir.getFiles()) {
            if (f.getName().contains("Model")) {
                String realName = f.getName().split("Model")[0];
                if (mClass.getName().contains(realName)) {
                    hasModel = true;
                    modelName = f.getName().replace(".java", "");
                }
            }

            if (f.getName().contains("Presenter")) {
                String realName = f.getName().split("Presenter")[0];
                if (mClass.getName().contains(realName)) {
                    hasPresenter = true;
                    presenterName = f.getName().replace(".java", "");
                }
            }

            if (f.getName().contains("ViewI")) {
                String realName = f.getName().split("ViewI")[0];
                if (mClass.getName().contains(realName)) {
                    hasViewI = true;
                    viewIName = f.getName().replace(".java", "");
                }
            }
        }

        if (!hasPresenter) {
            createPresenter();
        }
        if (!hasViewI) {
            createViewI();
        }
        if (!hasModel) {
            createModel();
        }
    }

    private void createModel() {
        PsiFile ModelFile = mMVPDir.createFile(modelName + ".java");

        StringBuffer modelText = new StringBuffer();
        modelText.append("package " + AndroidUtils.getFilePackageName(mMVPDir.getVirtualFile()) + ";\n\n\n");
        modelText.append(getHeaderAnnotation() + "\n");
        modelText.append("public class " + modelName + " extends BaseModel{\n\n\n");
        modelText.append("    @Inject\n" +
                "    public " + modelName + "() {\n" +
                "    }");
        modelText.append("}");

        Util_File.string2Stream(modelText.toString(), ModelFile.getVirtualFile().getPath());
    }

    private void createViewI() {
        PsiFile viewIFile = mMVPDir.createFile(viewIName + ".java");

        StringBuffer modelText = new StringBuffer();
        modelText.append("package " + AndroidUtils.getFilePackageName(mMVPDir.getVirtualFile()) + ";\n\n\n");
        modelText.append(getHeaderAnnotation() + "\n");
        modelText.append("public interface " + viewIName + " extends BaseViewI{\n\n\n");
        modelText.append("}");

        Util_File.string2Stream(modelText.toString(), viewIFile.getVirtualFile().getPath());
    }

    private void createPresenter() {

        //创建文件
        PsiFile presenterFile = mMVPDir.createFile(presenterName + ".java");

        //生成要写入的字符串
        StringBuffer modelText = new StringBuffer();
        modelText.append("package " + AndroidUtils.getFilePackageName(mMVPDir.getVirtualFile()) + ";\n\n\n");

        modelText.append(getHeaderAnnotation() + "\n");

        modelText.append("public class " + presenterName + " extends BasePresenter{\n\n\n");
        modelText.append(viewIName + " mView;\n");
        modelText.append(" @Inject\n");
        modelText.append(modelName + " mModel;\n");
        modelText.append("   public " + presenterName + "(" + viewIName + " arg) {\n" +
                "        super(arg);\n" +
                "        this.mView = arg;\n" +
                "        this.mModel = this.mView.getActivityComponent().get" + modelName + "();\n" +
                "\n" +
                "    }\n");
        modelText.append("    @Override\n" +
                "    public BaseModel getBaseModel() {\n" +
                "        return mModel;\n" +
                "    }");
        modelText.append("}");

        //将字符串写入文件
        Util_File.string2Stream(modelText.toString(), presenterFile.getVirtualFile().getPath());
    }

    /**
     * 生成该代码生成的时间
     * @return
     */
    private String getHeaderAnnotation() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(System.currentTimeMillis());
        String annotation = "/**\n" +
                " * Created  on " + time + ".\n" +
                " */";
        return annotation;
    }
    private PsiDirectory createMVPDir() {
        PsiDirectory mvpDir = mFile.getParent().findSubdirectory("mvp"); //获取mvp文件夹
        if (mvpDir == null) {
            //如果没有找到mvp文件夹，则创建一个
            mvpDir = mFile.getParent().createSubdirectory("mvp");
        }

        return mvpDir;
    }
}
