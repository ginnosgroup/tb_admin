package org.zhinanzhen.tb.scheduled;

import org.zhinanzhen.b.service.pojo.DataDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/09/28 12:16
 * Description:
 * Version: V1.0
 */
public class RegionClassification {

    // dataDTOList 按照地区进行分类，返回每个地区顾问的数据
    public  static List<List<DataDTO>> classification (List<DataDTO> dataDTOList){
        //每一个area的数据
        HashSet<String> areaset = new HashSet();
        dataDTOList.forEach(dataDTO -> {
            areaset.add(dataDTO.getArea());
        });
        List<List<DataDTO>> regionList = new ArrayList();
        for(String area : areaset){
            List<DataDTO> regionDataDTOList = new  ArrayList<DataDTO>();
            for( int index = 0 ;index < dataDTOList.size(); index++){

                if(dataDTOList.get(index).getArea().equals(area)){
                    regionDataDTOList.add(dataDTOList.get(index));
                }
            }
            regionList.add(regionDataDTOList) ;
        }
        return  regionList;
    }
}
