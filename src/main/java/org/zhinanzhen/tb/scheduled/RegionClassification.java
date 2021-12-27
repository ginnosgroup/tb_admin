package org.zhinanzhen.tb.scheduled;

import org.zhinanzhen.b.service.pojo.DataDTO;
import org.zhinanzhen.b.service.pojo.DataRankDTO;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
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
     * 返回data中regionId存在于regionIdList的数据,并且赋值Rank排名
     * @param data
     * @param regionIdList
     * @return
     */
    public  static List<DataRankDTO> dataSplitByRegionId (List<DataDTO> data, List<Integer> regionIdList) throws Exception {
        int i = 0;
        List<DataRankDTO> resultList = new ArrayList<>();
        if (regionIdList == null || regionIdList.size() == 0){
            for (DataDTO _data : data){
                DataRankDTO dataRankDTO = new DataRankDTO();
                fatherCopyFieldToChild(_data, dataRankDTO);
                dataRankDTO.setAdjustments(new BigDecimal(dataRankDTO.getAdjustments()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                dataRankDTO.setClaimCommission(new BigDecimal(dataRankDTO.getClaimCommission()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                dataRankDTO.setClaimedCommission(new BigDecimal(dataRankDTO.getClaimedCommission()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                dataRankDTO.setDeductionCommission(new BigDecimal(dataRankDTO.getDeductionCommission()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                dataRankDTO.setServiceFee(new BigDecimal(dataRankDTO.getServiceFee()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                dataRankDTO.setTotal(new BigDecimal(dataRankDTO.getTotal()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                dataRankDTO.setRank( ++i );
                resultList.add(dataRankDTO);
            }
            return resultList;
        }
        for (DataDTO _data : data){
            if (regionIdList.contains(_data.getRegionId())){
                DataRankDTO dataRankDTO = new DataRankDTO();
                fatherCopyFieldToChild(_data, dataRankDTO);
                dataRankDTO.setAdjustments(new BigDecimal(dataRankDTO.getAdjustments()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                dataRankDTO.setClaimCommission(new BigDecimal(dataRankDTO.getClaimCommission()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                dataRankDTO.setClaimedCommission(new BigDecimal(dataRankDTO.getClaimedCommission()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                dataRankDTO.setDeductionCommission(new BigDecimal(dataRankDTO.getDeductionCommission()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                dataRankDTO.setServiceFee(new BigDecimal(dataRankDTO.getServiceFee()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                dataRankDTO.setTotal(new BigDecimal(dataRankDTO.getTotal()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                dataRankDTO.setRank( ++i );
                resultList.add(dataRankDTO);
            }
        }
        return resultList;
    }

    public  static DataDTO adviserDateByAdviserId (List<DataDTO> data, Integer adviserId){
        if (adviserId == 0)
            return null;
        DataDTO resultData = new DataDTO();
        data.forEach(_data ->{
            if (_data.getAdviserId() == adviserId){
                resultData.setServiceFee(new BigDecimal(resultData.getServiceFee() + _data.getServiceFee())
                        .setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                resultData.setDeductionCommission(new BigDecimal(resultData.getDeductionCommission() + _data.getDeductionCommission())
                        .setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                resultData.setClaimCommission(new BigDecimal(resultData.getClaimCommission() + _data.getClaimCommission())
                        .setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                resultData.setClaimedCommission(new BigDecimal(resultData.getClaimedCommission() + _data.getClaimedCommission())
                        .setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                resultData.setTotal(new BigDecimal(resultData.getTotal() + _data.getTotal())
                        .setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
            }
        });
        return resultData;
    }

    public static <T>void fatherCopyFieldToChild(T father,T child) throws Exception {
        if (child.getClass().getSuperclass()!=father.getClass()){
            throw new Exception("child 不是 father 的子类");
        }
        Class<?> fatherClass = father.getClass();
        Field[] declaredFields = fatherClass.getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            Field field=declaredFields[i];
            Method method=fatherClass.getDeclaredMethod("get"+upperHeadChar(field.getName()));
            Object obj = method.invoke(father);
            field.setAccessible(true);
            field.set(child,obj);
        }

    }

    public static String upperHeadChar(String in) {
        String head = in.substring(0, 1);
        String out = head.toUpperCase() + in.substring(1, in.length());
        return out;
    }


}
