package org.zhinanzhen.tb.utils;


import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.ikasoa.core.utils.StringUtil;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class WebDavUtils {
//   public static String username = "23508311977@qq.com";
//    public static String password = "a382y2bychm9by5y";
    public static String username = "jiaheng.xu@zhinanzhen.org";
    public static String password = "anxjg8crwx8u7kpk";
    public static String serviceAddress = "https://dav.jianguoyun.com/dav";

    public static Sardine sardine;
    static {
        // 根据用户名、密码初始化Sardine对象
         sardine = SardineFactory.begin(username, password);
    }

    /**
     * 上传
     * @param netDiskPath 坚果云网盘目录地址
     * @param filePath 文件地址
     * @throws IOException
     */
    public static void upload(String netDiskPath, String filePath) throws IOException {
        //判断是否存在目录 不存在则创建
        String packagePath = netDiskPath.substring(0,getPath(netDiskPath).lastIndexOf("/"));
        mkdir(packagePath);

        // 执行文件上传操作
        sardine.put(getPath(netDiskPath), new File(filePath),"application/x-www-form-urlencoded");

    }
    public static void upload2(String netDiskPath,List<String> pathList) throws IOException {
        //判断是否存在目录 不存在则创建
        String s1 = netDiskPath+"/";
        String packagePath = s1.substring(0,getPath(s1).lastIndexOf("/"));
        mkdir(packagePath);

        // 执行文件上传操作
        for (String s : pathList) {
            File file = new File(s);
            sardine.put(getPath(s1 + s.substring(s.lastIndexOf(File.separator)+1)),file,"application/x-www-form-urlencoded");
        }

    }

    /**
     * 下载
     * @param netDiskPath 坚果云网盘目录地址
     * @param filePath 文件地址
     * @throws IOException
     */
    public static void down(String netDiskPath, String filePath ) throws IOException{
        InputStream fis = sardine.get(getPath(netDiskPath));
        FileOutputStream fos = new FileOutputStream(filePath);
        int len;
        byte[] buffer = new byte[1024];

        while ((len = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, len);
        }
        fis.close();
        fos.close();
    }

    /**
     * 下载目录下所有文件
     * @param name 客户姓名缩写
     * @param filePath 文件地址
     * @throws IOException
     */
    public static List<String> MMdown(String name, String filePath) throws IOException{
        String Dir="https://dav.jianguoyun.com/dav/MMtest/";
        List<DavResource> davResources = get(Dir);
        List<DavResource> davResourceLists = new ArrayList<>();
//        List<String> list = davResources.stream().map(davResource -> davResource.getDisplayName()).collect(Collectors.toList());
        for (DavResource davResource : davResources) {
            if (davResource.getDisplayName().contains(name))
                davResourceLists.add(davResource);

        }
//        Arrays.sort(davResourceLists,new Comparator<DavResource>(){
//            @Override
//            public Long compare(DavResource d1, DavResource d2){
//                return d2.getModified().getTime()-d1.getModified().getTime();
//
//            }
//
//        });
        List<DavResource> collect = davResourceLists.stream().sorted(Comparator.comparing(DavResource::getModified).reversed()).collect(Collectors.toList());
        List<DavResource> davResourceList = get(Dir+collect.get(0).getDisplayName());
        List<String> urlList =new ArrayList<>();
        File folder = new File(filePath);
        if (!folder.isDirectory())
            folder.mkdirs();
        for (int i = 1; i < davResourceList.size(); i++) {
            String path = davResourceList.get(i).getDisplayName();
            String mmFilePath = StringUtil.merge(Dir,collect.get(0).getDisplayName()+"/"+path);
            String outpath = StringUtil.merge(filePath, path);
            String file = outpath.replace("/", File.separator);
            down(mmFilePath,file);
            urlList.add(file);

        }
        return urlList;
    }

    public static void main(String[] args) throws IOException {
        List<String> strings = MMdown("LILS", "C:/Users/yjt/Desktop/data/uploads/customerInformation/LILS");
    }

    /**
     * 删除
     * @param netDiskPath 坚果云网盘目录地址
     * @throws IOException
     */
    public static void delete(String netDiskPath) throws IOException{
        if (!sardine.exists(getPath(netDiskPath)))
        sardine.delete(getPath(netDiskPath));
    }
    /**
     * 获取文件列表
     * @param netDiskPath 坚果云网盘目录地址
     * @throws IOException
     */
    public static List<DavResource> get(String netDiskPath) throws IOException{
        List<DavResource> resources = sardine.list(getPath(netDiskPath));
        for (DavResource resource : resources) {
            System.out.println(resource);
        }
      return resources;
    }

    /**
     * 递归创建目录
     * @param filePath 待创建目录
     * @throws IOException
     */
    public static void mkdir(String filePath) throws IOException {
        String dirPath = filePath.replace(serviceAddress+"/", "");

        if (StringUtils.isNotBlank(dirPath)) {
            String[] dirs = dirPath.split("/");
            String path = serviceAddress;
            for (int i = 0; i < dirs.length; i++) {
                path +=  "/"+ dirs[i] ;
                if(i==0){
                    try {
                        if (!sardine.exists(path))
                            sardine.createDirectory(path);
                    }catch (Exception e){

                    }
                }
                else if(!sardine.exists(path)) {
                    sardine.createDirectory(path);
                }
            }
        }
    }

    /**
     *
     * @param netDiskPath 网盘路径
     * @return 网盘绝对路径
     */
    public static String getPath(String netDiskPath ){
        return serviceAddress.contains(netDiskPath)?serviceAddress+"/"+netDiskPath:netDiskPath;
    }


//    public static void main(String[] args) {
//        try {
//
//            String netDiskPath = "https://dav.jianguoyun.com/dav/MMpdf/test.pdf";
//            upload(netDiskPath,"C:/Users/yjt/Desktop/pdf/test.pdf");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
