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

    /**
     * 返回data中regionId存在于regionIdList的数据
     * @param data
     * @param regionIdList
     * @return
     */
    public  static List<DataDTO> dataSplitByRegionId (List<DataDTO> data, List<Integer> regionIdList){
        if (regionIdList.size() == 0)
            return data;
        List<DataDTO> resultList = new ArrayList<>();
        data.forEach(_data ->{
            if (regionIdList.contains(_data.getRegionId())){
                resultList.add(_data);
            }
        });
        return resultList;
    }

    public  static DataDTO adviserDateByAdviserId (List<DataDTO> data, Integer adviserId){
        if (adviserId == 0)
            return null;
        DataDTO resultData = new DataDTO();
        data.forEach(_data ->{
            if (_data.getAdviserId() == adviserId){
                resultData.setServiceFee(resultData.getServiceFee() + _data.getServiceFee());
                resultData.setDeductionCommission(resultData.getDeductionCommission() + _data.getDeductionCommission());
                resultData.setClaimCommission(resultData.getClaimCommission() + _data.getClaimCommission());
                resultData.setClaimedCommission(resultData.getClaimedCommission() + _data.getClaimedCommission());
            }
        });
        return resultData;
    }

}
