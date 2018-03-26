package com.hjedu.course.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.util.StringUtils;

import com.hjedu.course.dao.ILessonDAO;
import com.hjedu.course.dao.ILessonTypeDAO;
import com.hjedu.course.entity.Lesson;
import com.hjedu.course.entity.LessonType;
import com.hjedu.customer.controller.AdminSessionBean;
import com.hjedu.customer.dao.IAdminDAO;
import com.hjedu.customer.dao.IBbsFileDAO;
import com.hjedu.customer.entity.AdminInfo;
import com.hjedu.examination.dao.IExamDepartmentDAO;
import com.hjedu.examination.dao.IExamModule2DAO;
import com.hjedu.examination.entity.DictionaryModel;
import com.hjedu.examination.entity.ExamDepartment;
import com.hjedu.examination.entity.ExamModuleModel;
import com.hjedu.examination.entity.module2.ModuleExamination2;
import com.hjedu.examination.service.impl.ExamDepartmentService;
import com.hjedu.platform.dao.ISystemConfigDAO;
import com.hjedu.platform.entity.BbsFileModel;
import com.hjedu.platform.service.ILogService;
import com.huajie.app.util.DocConverter;
import com.huajie.app.util.FileUtil;
import com.huajie.app.util.Pdf2htmlEXUtil2;
import com.huajie.app.util.PdfUtil;
import com.huajie.app.util.StringUtil;
import com.huajie.util.CookieUtils;
import com.huajie.util.JsfHelper;
import com.huajie.util.SpringHelper;
import com.sun.org.apache.regexp.internal.recompile;
import com.sun.org.apache.xpath.internal.operations.And;

@ManagedBean
@ViewScoped
public class AALesson implements Serializable {
	private static final Logger log4j = Logger.getLogger(AALesson.class);
    ILogService logger = SpringHelper.getSpringBean("LogService");
    ILessonDAO examDAO = SpringHelper.getSpringBean("LessonDAO");
    ILessonTypeDAO lessonTypeDAO = SpringHelper.getSpringBean("LessonTypeDAO");
    IAdminDAO adminDAO = SpringHelper.getSpringBean("AdminDAO");
    IExamModule2DAO module2DAO = SpringHelper.getSpringBean("ExamModule2DAO");
    IExamDepartmentDAO departmentDAO = SpringHelper.getSpringBean("ExamDepartmentDAO");
    Lesson exam;
    List<ExamDepartment> departments;
    boolean flag = false;
    TreeNode root = new DefaultTreeNode();
    ExamDepartmentService dicDAO = SpringHelper.getSpringBean("ExamDepartmentService");
    IExamModule2DAO moduleDicDAO = SpringHelper.getSpringBean("ExamModule2DAO");
    ISystemConfigDAO systemConfigDAO = SpringHelper.getSpringBean("SystemConfigDAO");
    private String yun_host=this.systemConfigDAO.getSystemConfig().getYun_host();
    List<DictionaryModel> dics = new ArrayList();
    private TreeNode[] selectedNodes;
    List<DictionaryModel> selectedNodes2 = new LinkedList();
    private List<TreeNode> nodes = new LinkedList();
    String typeId = "0";
    String chapterId = null;//节所属的章id
    List<LessonType> types;
    List<Lesson> chapters;
    String bussinessId;

    TreeNode root2;//章节练习挂载点
    List<TreeNode> selectedModule = new LinkedList();//选择的章节练习

    String leid="";

    IBbsFileDAO bbsFileDAO = SpringHelper.getSpringBean("BbsFileDAO");
    
    public String getLeid() {
		return leid;
	}

	public void setLeid(String leid) {
		this.leid = leid;
	}

	public Lesson getExam() {
        return exam;
    }

    public void setExam(Lesson exam) {
        this.exam = exam;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }
    
    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public List<LessonType> getTypes() {
        return types;
    }

    public void setTypes(List<LessonType> types) {
        this.types = types;
    }

    public List<Lesson> getChapters() {
		return chapters;
	}
    
    public void setChapters(List<Lesson> chapters) {
    	this.chapters = chapters;
	}
    
    public List<ExamDepartment> getDepartments() {
        return departments;
    }

    public void setDepartments(List<ExamDepartment> departments) {
        this.departments = departments;
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public TreeNode getRoot2() {
        return root2;
    }

    public void setRoot2(TreeNode root2) {
        this.root2 = root2;
    }

    public List<TreeNode> getSelectedModule() {
        return selectedModule;
    }

    public void setSelectedModule(List<TreeNode> selectedModule) {
        this.selectedModule = selectedModule;
    }

    public List<DictionaryModel> getDics() {
        return dics;
    }

    public void setDics(List<DictionaryModel> dics) {
        this.dics = dics;
    }

    public TreeNode[] getSelectedNodes() {
        return selectedNodes;
    }

    public void setSelectedNodes(TreeNode[] selectedNodes) {
        this.selectedNodes = selectedNodes;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    @PostConstruct
    public void init() {
        HttpServletRequest request = JsfHelper.getRequest();
        this.bussinessId = CookieUtils.getBusinessId(request);
        String id = request.getParameter("id");
        this.leid=id;
        String moduleId = request.getParameter("module_id");
        AdminSessionBean asb = JsfHelper.getBean("adminSessionBean");
        if (id != null) {
            this.flag = true;
            this.exam = this.examDAO.findLesson(id);
            if(this.exam==null){
            	this.exam = new Lesson();
            }
        } else {
            this.exam = new Lesson();
            //this.buildModuleLessons(exam);
        }
        this.loadStructure();
        AdminInfo ai = asb.getAdmin();
        if (ai != null) {
            //加载课程类别
            this.types = this.lessonTypeDAO.findLessonTypeByAdminAndTime(ai,CookieUtils.getBusinessId(JsfHelper.getRequest()));     
            this.chapters = this.examDAO.findChapterLesson(CookieUtils.getBusinessId(JsfHelper.getRequest()), 1);
            
        }
        if (this.exam.getChapterType() == 1) {
			this.chapterId = findLessonChapter(this.exam);
		}
        //设置当前课程的类别
        if (this.exam.getLessonType() != null) {
            this.typeId = this.exam.getLessonType().getId();
        }               
		
        if (this.exam.getParentId() != null) {
            this.chapterId = this.exam.getParentId();
        }
        
        //加载章节练习
        List<String> selectedId = new ArrayList<>();
        if (flag) {
//            ModuleExamination2 exam2 = exam.getModuleExam2();
        	List<ModuleExamination2> exam2 = exam.getModuleExaminations();
            if (exam2 != null) {
            	for(ModuleExamination2 exam : exam2){
            		ExamModuleModel emm = exam.getModule();
            		if (emm != null) {
            			selectedId.add(emm.getId());
            		}
            	}
            }
        }
        this.loadModuleStructure(null, selectedId);
    }

    private boolean testIfSelected(DictionaryModel dd) {
        dd.setSelected(false);
        if(this.exam!=null&&dd!=null){
        	String str = this.exam.getGroupStr();
        	if(StringUtil.isNotEmpty(str)){
        		if(str.contains(dd.getId())){
        			dd.setSelected(true);
                    return true;
        		}
        	}
        }
        return false;
    }

    private void loadStructure() {
        this.root = new DefaultTreeNode();
        DictionaryModel dm = dicDAO.findDictionaryModel(this.bussinessId,this.bussinessId);
        test(dm, root);
        this.dics = null;
        this.dics = dm.getSons();

    }

    public void test(DictionaryModel dd, TreeNode node) {
        //检查，如果某节点的所有子节点中只要有节点被选中，此节点就应该展开
        boolean k = false;
        List<DictionaryModel> ks = dicDAO.loadAllDescendants(dd.getId());
        for (DictionaryModel ddm : ks) {
            if (this.testIfSelected(ddm)) {
                k = true;
                break;
            }
        }
        if (k) {
            node.setExpanded(true);
        }
        //判断是否应该展开完毕
        //判断是否应该选中,若选中，也要展开（因为可能有子元素未选中，而父元素选中的情况）
        if (this.testIfSelected(dd)) {
            //System.out.println(dd.getId() + dd.getName());
            //node.setSelected(true);
            node.setExpanded(true);
        } else {
            node.setSelected(false);
        }
        //
        List<DictionaryModel> ls = dicDAO.findAllSonDictionaryModel(dd.getId());
        Collections.sort(ls);
        dd.setSons(ls);
        if (ls.isEmpty()) {
            node.setSelectable(true);
            return;
        } else {
            node.setSelectable(false);
            for (DictionaryModel d : ls) {
                TreeNode t = new DefaultTreeNode(d, node);
                test(d, t);
            }
        }
    }
    
    public String findLessonChapter(Lesson lesson) {
		if (lesson.getChapterType() == 2) {
			findLessonChapter(this.examDAO.findLesson(lesson.getParentId()));
		}else if (lesson.getChapterType() == 1) {
			return lesson.getId();
		}
		return null;
	}
    
    public void checkSons2(ExamModuleModel ex, boolean b) {
//        DictionaryModel emm = this.dicDAO.findDictionaryModel(id);
        List<String> dics1 = new ArrayList<>();
        List<ExamModuleModel> sons = ex.getSons();
        dics1.add(ex.getId());
        for(ExamModuleModel son : sons){
        	dics1.add(son.getId());
        }
        this.test2(dics1, root2, b);
    }
    public void test2(List<String> dds, TreeNode tn, boolean b) {
    	ExamModuleModel top = (ExamModuleModel) tn.getData();
        if (this.testIfContain2(dds, top)) {
            top.setSelected(b);
        }
        List<TreeNode> tns = tn.getChildren();
        if (tns == null) {
            return;
        } else {
            for (TreeNode t : tns) {
                test2(dds, t, b);
            }
        }
    }
    private boolean testIfContain2(List<String> dds, ExamModuleModel emm) {
        for (String em : dds) {
        	if(em.equals(emm.getId())){
        		return true;
        	}
        }
        return false;
    }
    //以下三个方法用于上级节点选取时级联到子节点
    public void checkSons(String id, boolean b) {
        DictionaryModel emm = this.dicDAO.findDictionaryModel(id,bussinessId);
        List<DictionaryModel> dics1 = this.dicDAO.loadAllDescendants(id);
        dics1.add(emm);
        this.test3(dics1, root, b);
    }

    public void test3(List<DictionaryModel> dds, TreeNode tn, boolean b) {
        //List<ExamModuleModel> mos = this.role.getModules();
        DictionaryModel top = (DictionaryModel) tn.getData();
        if (this.testIfContain(dds, top)) {
            top.setSelected(b);
        }
        List<TreeNode> tns = tn.getChildren();
        if (tns == null) {
            return;
        } else {
            for (TreeNode t : tns) {
                test3(dds, t, b);
            }
        }
    }

    private boolean testIfContain(List<DictionaryModel> dds, DictionaryModel emm) {
        for (DictionaryModel em : dds) {
            if (em != null && emm != null) {
                if (em.getId().equals(emm.getId())) {
                    //break;
                    return true;
                }
            }
        }
        return false;
    }
    //级联结束

    private void checkDepartment() {
        StringBuilder sb = new StringBuilder();
        if (this.departments != null) {
            for (ExamDepartment ed : this.departments) {
                if (ed.isSelected()) {
                    sb.append(ed.getName());
                    sb.append(",");
                }
            }
        }
        this.exam.setGroupStr(sb.toString());
    }

    //以下三方法用于提交考试前获取考试开放的部门
    public List<DictionaryModel> fetchDictionaryModels() {
        this.selectedNodes2.clear();
        this.testDictionaryModel2(this.selectedNodes2, root);
        return this.selectedNodes2;
    }

    public void testDictionaryModel2(List<DictionaryModel> ms, TreeNode root) {
        List<TreeNode> ls = root.getChildren();
        if (ls.isEmpty()) {
            return;
        } else {
            for (TreeNode d : ls) {
                DictionaryModel em = (DictionaryModel) d.getData();
                if (em.isSelected()) {
                    ms.add(em);
                }
                testDictionaryModel2(ms, d);
            }
        }
    }

    private void checkDepartment2() {
        this.fetchDictionaryModels();
        StringBuilder sb = new StringBuilder();
        Set<DictionaryModel> set = new HashSet();
        if (this.selectedNodes2 != null) {
            for (DictionaryModel ed : this.selectedNodes2) {
                set.add(ed);
                //set.addAll(this.dicDAO.loadAllDescendants(d.getId()));

            }
        }
        for (DictionaryModel dm : set) {
            sb.append(dm.getId());
            sb.append(",");
        }
        String sbb = sb.toString().trim();
        if ("".equals(sbb)) {
            sbb = this.bussinessId;
        }
        this.exam.setGroupStr(sbb);
    }
//获取结束

    public String done() {
        this.checkDepartment2();
        //设置课程的章节练习挂载点
//        if (selectedModule != null) {
//        	selectedModule.clear();
//        	selectedModule = root2.getChildren();
//        	addAllNodesIntoList(selectedModule,list);
//        }
        
        if(StringUtil.isEmpty(this.exam.getSourceUrl())&&this.exam.getFiles()!=null&&this.exam.getFiles().size()>0
        		&&this.exam.getFiles().get(0).getFileExt().equals(".mp4")){
    		String path = this.exam.getFiles().get(0).getId()+this.exam.getFiles().get(0).getFileExt();
    		this.exam.setSourceUrl(path);
    	}
        
        List<ModuleExamination2> list = new ArrayList<>();
        if(this.selectedNodes!=null){
        	for (TreeNode t : this.selectedNodes) {
        		ExamModuleModel dmm = (ExamModuleModel) t.getData();
        		list.add(dmm.getExam());
        	}
        	exam.setModuleExaminations(list);
        }
        //设置课程的类型
        LessonType lt = this.lessonTypeDAO.findLessonType(typeId);
        if (lt != null) {
            this.exam.setLessonType(lt);
        }
        
        if (chapterId != null && chapterId.length() > 2) {
        	this.exam.setChapterType(2);  
			this.exam.setParentId(chapterId);        	        
		}else {
			this.exam.setChapterType(1);  
			this.exam.setParentId(null);
		}
        
        
        if (flag) {
            this.logger.log("修改了课程：" + exam.getName());
            exam.setVersion(exam.getVersion()+1);
            this.examDAO.updateLesson(exam);
        } else {
            this.logger.log("添加了课程：" + exam.getName());
            exam.setBusinessId(this.bussinessId);
            this.examDAO.addLesson(exam);
        }
        AdminSessionBean asb = JsfHelper.getBean("adminSessionBean");
        asb.refreshAdmin();
        return "ListLesson?faces-redirect=true";
    }
    
	public String done2(){
		if(StringUtil.isNotEmpty(this.leid)){
			this.exam = this.examDAO.findLesson(this.leid);
			if(StringUtil.isEmpty(this.exam.getSourceUrl())&&this.exam.getFiles()!=null&&this.exam.getFiles().size()>0){
	    		BbsFileModel bfm = this.exam.getFiles().get(0);
	    		String docPath = FileUtil.getFileRealPath(bfm);
	    		String fileName = docPath.substring(docPath.lastIndexOf("/"),docPath.lastIndexOf("."));
	    		if(bfm.getFileExt().equals(".mp4")){
	    			String path = bfm.getId()+bfm.getFileExt();
	        		this.exam.setSourceUrl(path);
	        		this.examDAO.updateLesson(exam);
	    		}else if(bfm.getFileExt().equals(".doc") || bfm.getFileExt().equals(".docx") || bfm.getFileExt().equals(".ppt") || bfm.getFileExt().equals(".pptx")){
	    			//处理转pdf然后转html
//	    			String result = yun_host.substring(yun_host.indexOf("//")+2,yun_host.lastIndexOf(":"));
	    			String result = "127.0.0.1";
	    			DocConverter dc = new DocConverter(docPath,"C:\\huajie_exam/pdf/");
                    if(dc.conver(result,8100)){
	    	    		String pdfPath=dc.getpdfPath();
	    	    		File mf = new File(pdfPath);
	    				if(mf.exists()){
							try {
								if(PdfUtil.CrackCopy(pdfPath, pdfPath)){
									File mf2 = new File(pdfPath);
									FileInputStream fis = new FileInputStream(pdfPath);
									String md5 = DigestUtils.md5Hex(IOUtils.toByteArray(fis));
			    					IOUtils.closeQuietly(fis);
			    					bfm.setPdfmd5(md5);
			    					bfm.setPdfsize(getFileSize(mf2));
			    					bbsFileDAO.updateClientFileInfo(bfm);
								}
							} catch (Exception e) {
								e.printStackTrace();
								log4j.error(StringUtil.eToString(e));
							}
	    				}
	    	    		//转html
	    	    		if(Pdf2htmlEXUtil2.pdf2html("C:\\pdf2htmlEX-0.12-win32-static-with-poppler-data\\pdf2htmlEX.exe", pdfPath, "C:\\huajie_exam/html",fileName+".html")){
	    	    			System.out.println("转换成功");
	    	    			
	    	    		}else{
	    	    			System.out.println("转换失败");
	    	    		}
	    	    	}else{
	    	    		//保存失败
	    	    		System.out.println("保存失败");
	    	    	}
	    		}else if(bfm.getFileExt().equals(".pdf")){
	    			//pdf不用转
	    			String pdfPath = FileUtil.getPdfFileRealPath(bfm);
	    			File mf = new File(FileUtil.getPdfFileRealPath(bfm));
    				if(mf.exists()){
    					if(PdfUtil.CrackCopy(pdfPath, pdfPath)){
							try {
								File mf2 = new File(pdfPath);
								FileInputStream fis = new FileInputStream(pdfPath);
								String md5 = DigestUtils.md5Hex(IOUtils.toByteArray(fis));
		    					IOUtils.closeQuietly(fis);
	    						bfm.setPdfmd5(md5);
	        	    			bfm.setPdfsize(getFileSize(mf2));
	        	    			bbsFileDAO.updateClientFileInfo(bfm);
							} catch (Exception e) {
								e.printStackTrace();
								log4j.error(StringUtil.eToString(e));
							}
    					}
    				}
	    			if(Pdf2htmlEXUtil2.pdf2html("C:\\pdf2htmlEX-0.12-win32-static-with-poppler-data\\pdf2htmlEX.exe", docPath, "C:\\huajie_exam/html",fileName+".html")){
		    			System.out.println("转换成功");
		    			
		    		}else{
		    			System.out.println("转换失败");
		    		}
	    		}
	    	}else{
	    		this.exam.setClassNum(0d);
	    		this.examDAO.updateLesson(this.exam);
	    	}
		}
    	return "ListLesson?faces-redirect=true";
    }
    
    public String next(){
    	done();
    	return "AALesson3?id="+exam.getId()+"&faces-redirect=true";
    }
    
    public String next2(){
    	done();
    	return "AALesson3?id="+exam.getId()+"&faces-redirect=true";
    }
    
    public String back2(){
    	return "AALesson?id="+exam.getId()+"&faces-redirect=true";
    }
    
    public String back(){
    	return "AALesson?id="+exam.getId()+"&faces-redirect=true";
    }
    
    /**
     * 将nodes中所有节点以及子节点的data集合装进list中
     * @param nodes
     * @param list
     */
    private void addAllNodesIntoList(List<TreeNode> nodes,List<ModuleExamination2> list){
    	for(TreeNode node : nodes){
    		list.add(((ExamModuleModel) node.getData()).getExam());
    		if(node.getChildren().size()>0){
    			addAllNodesIntoList(node.getChildren(),list);
    		}
    	}
    }

    //以下三个方法用于加载章节练习
    public void loadModuleStructure(String id, List<String> selectedId) {
        if (id == null || bussinessId.equals(id)) {
            id = bussinessId;
            ExamModuleModel l = moduleDicDAO.findExamModuleModel(id);
            this.root2 = new DefaultTreeNode(l, null);
            testModule(l, root2, selectedId);
        } else {
            ExamModuleModel l = moduleDicDAO.findExamModuleModel(id);
            this.root = new DefaultTreeNode(l, null);
            testModule(l, new DefaultTreeNode(l, root2), selectedId);
        }

    }
    /**
     * 将dd的子对象添加到dd的sons属性中，并将子对象对应的TreeNode对象添加到node（dd对应的TreeNode对象）上，若dd的id在selectedId中，将其设置为已勾选
     * @param dd
     * @param node
     * @param selectedId
     */
    public void testModule(ExamModuleModel dd, TreeNode node, List<String> selectedId) {
//        if (this.moduleStr.contains(dd.getId())) {
//            node.setExpanded(true);
//        }
        if (dd == null) {
            return;
        }
        node.setExpanded(true);
        //设置是否该节点被选中
        if (selectedId.contains(dd.getId())) {
        	dd.setSelected(true);
        	//此处设置的是该节点的所在行变色，表示选中
            node.setSelected(true);
        }
        //System.out.println("test begain.");
        List<ExamModuleModel> ls = moduleDicDAO.findAllSonExamModuleModel(dd.getId(), this.bussinessId);
        Collections.sort(ls);
        dd.setSons(ls);
        if (ls.isEmpty()) {
        	node.setSelectable(true);
            return;
        } else {
        	node.setSelectable(false);
            for (ExamModuleModel d : ls) {
                if (this.testIfModuleShow(d)) {
                    TreeNode t = new DefaultTreeNode(d, node);
                    testModule(d, t, selectedId);
                }
            }
        }
    }

    private boolean testIfModuleShow(ExamModuleModel d) {
        boolean con = false;
        //List<ExamModuleModel> aems = null;

        //System.out.println("user group str:"+gstr);
        ModuleExamination2 me = d.getExam();
        //System.out.println(me);
        if (me == null) {
            return false;
        }
        if (!me.isIfOpen()) {
            return false;
        }
        return true;
    }

	
    /**
     * 获取文件大小，单位为B/KB/MB/GB
     * @param tl
     * @return
     */
    public String getFileSize(File item){
    	Long tl = item.length();
    	String str = "";
    	Float k = 1024F;
        Float m = 1024 * 1024F;
        Float g = 1024 * 1024 * 1024F;
        if (tl < k) {
            str = tl.toString() + "B";
        } else if (k <= tl && tl < m) {
            Float te = Math.round(tl / k * 100) / 100.00F;
            str = te.toString() + "KB";
        } else if (tl >= m && tl < g) {
            Float te = Math.round(tl / m * 100) / 100.00F;
            str = te.toString() + "MB";
        } else {
            Float te = Math.round(tl / g * 100) / 100.00F;
            str = te.toString() + "GB";
        }
        return str;
    }
    
    public static void main(String[] args) {
    	String a="http://192.168.1.245:8082/ucenter";
    	String b="http://jtcbs.bidexam.com:7086/ucenter";
    	System.out.println(a.substring(a.indexOf("//")+2, a.lastIndexOf(":")));
    	System.out.println(b.substring(b.indexOf("//")+2, b.lastIndexOf(":")));
	}
}
