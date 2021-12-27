package org.zhinanzhen.b.controller;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.core.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zhinanzhen.b.service.CommissionOrderService;
import org.zhinanzhen.b.service.DashboardService;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.CommissionOrderListDTO;
import org.zhinanzhen.b.service.pojo.DataDTO;
import org.zhinanzhen.b.service.pojo.DataRankDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.DashboardResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.scheduled.Data;
import org.zhinanzhen.tb.scheduled.DateClass;
import org.zhinanzhen.tb.scheduled.RegionClassification;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.RegionService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.RegionDTO;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/dashboard")
public class DashboardController extends BaseController {

	@Resource
	DashboardService dashboardService;

	@Resource
	CommissionOrderService commissionOrderService;

	@Resource
	private ServiceOrderService serviceOrderService;

	@Resource
	private RegionService regionService;

	@Autowired
	private Data data;

	@Resource
	private AdviserService adviserService;

	@Resource
	private UserService userService;

	@RequestMapping(value = "/getMonthExpectAmount", method = RequestMethod.GET)
	@ResponseBody
	public Response<Double> getMonthExpectAmount(HttpServletRequest request, HttpServletResponse response) {
		try {
			if (getAdminUserLoginInfo(request) == null)
				return new Response<Double>(1, "请先登录!", null);
			return new Response<Double>(0, dashboardService.getThisMonthExpectAmount(getAdviserId(request), null));
		} catch (ServiceException e) {
			return new Response<Double>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/listThisMonthCommissionOrder", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<CommissionOrderListDTO>> listThisMonthCommissionOrder(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			if (getAdminUserLoginInfo(request) == null)
				return new Response<List<CommissionOrderListDTO>>(1, "请先登录!", null);
			return new Response<List<CommissionOrderListDTO>>(0,
					commissionOrderService.listThisMonthCommissionOrder(getAdviserId(request), getOfficialId(request)));
		} catch (ServiceException e) {
			return new Response<List<CommissionOrderListDTO>>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/NotReviewedServiceOrder",method = RequestMethod.GET)
	@ResponseBody
	public  Response<List<ServiceOrderDTO>> NotReviewedServiceOrder(
			@RequestParam(value = "thisMonth",required = false)boolean thisMonth,
			@RequestParam(value = "officialId",required = false)Integer officialId,
			HttpServletRequest request){
		if (getAdminUserLoginInfo(request) == null)
			return  new Response(1,"先登录！");
		Integer _OfficialId = getOfficialId(request);
		if (_OfficialId != null){
			if (getOfficialAdminId(request) == null) //不是文案管理员返回null
				officialId = _OfficialId; //不是文案管理员则显示自己的服务订单
		}
		return  new Response(0 , serviceOrderService.NotReviewedServiceOrder(officialId,thisMonth));
	}

	@GetMapping(value = "/caseCount")
	@ResponseBody
	public Response<Integer> caseCount(
			@RequestParam(value = "officialId")Integer officialId,
			@RequestParam(value = "days",required = false)String Days,
			@RequestParam(value = "state",required = false)String state,
			HttpServletRequest request){
		if (getAdminUserLoginInfo(request) == null)
			return  new Response(1,"先登录！");
		Integer _officialId = getOfficialId(request);
		if (_officialId != null)
			officialId = _officialId;
		return  new Response<>(0,serviceOrderService.caseCount(officialId,Days,state));
	}


	/**
	 * 全澳本月顾问业绩排名
	 * @param request
	 * @param response
	 * @return
	 * @throws ServiceException
	 */
	@GetMapping(value = "/thisMonthPerformanceRank")
	@ResponseBody
	public DashboardResponse thisMonthPerformanceRank(HttpServletRequest request, HttpServletResponse response) throws Exception {
		super.setGetHeader(response);
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		//List<Integer> regionIdList = new ArrayList<>();
		if (adminUserLoginInfo == null || !("GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
				|| "SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())))
			return new DashboardResponse(1,"No permission");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = DateClass.thisMonthFirstDay(Calendar.getInstance());
		String endDate = sdf.format(Calendar.getInstance().getTime());

		//顾问排名.也是全部数据。顾问id,月份分组数据
		List<DataDTO> dataList = data.dataReport(startDate,endDate,"R",null); //  R 全area顾问倒序排名的数据  顾问
		//dataList数据中顾问是相同的地区(regionId)合并到一条记录
		//List<DataDTO> areaTodayDataList = data.dataReport(startDate,endDate,"A"); //   A  全area地区的area数据   数据
		//dataList数据中顾问是相同的地区就添加到一个List<DataDTO>里面。将dataList的数据按照顾问地区进行分组放在不同的List<DataDTO>中。
		// 一个area实例中包含此area所有顾问的数据,和areaTodayDataList是和并成一条记录
		//List<List<DataDTO>> regionList = RegionClassification.classification(dataList);//  按照地区将顾问进行分组

		return  new DashboardResponse(0,"全澳本月累计业绩排名",RegionClassification.dataSplitByRegionId(dataList,null), startDate, endDate);
	}

	/**
	 * Super:全澳地区本月累计业绩排名
	 * Manger：管理地区本月累计业绩排名
	 * Gw:本地区本月累计业绩排名
	 * @param request
	 * @param response
	 * @return
	 * @throws ServiceException
	 */
	@GetMapping(value = "/thisMonthPerformanceRankDiffAp")
	@ResponseBody
	public DashboardResponse thisMonthPerformanceRankDiffAp(HttpServletRequest request, HttpServletResponse response) throws Exception {
		super.setGetHeader(response);
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo == null || !("GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
				|| "SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())))
			return new DashboardResponse(1,"No permission");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		List<Integer> regionIdList = new ArrayList<>();
		String startDate = DateClass.thisMonthFirstDay(Calendar.getInstance());
		String endDate = sdf.format(Calendar.getInstance().getTime());
		List<DataDTO> dataList = data.dataReport(startDate,endDate,"R",null); //  R 全area顾问倒序排名的数据  顾问
		if ("SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())){
			return new DashboardResponse(0, "success", RegionClassification.dataSplitByRegionId(dataList,regionIdList), startDate, endDate);
		}else {
			if (adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0){//顾问管理员显示管理区域
				List<RegionDTO> _regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
				regionIdList.add(adminUserLoginInfo.getRegionId());
				for (RegionDTO region : _regionList)
					regionIdList.add(region.getId());
				List<DataRankDTO> _list = RegionClassification.dataSplitByRegionId(dataList,regionIdList);
				return new DashboardResponse(0,"顾问管理员", _list, startDate, endDate);
			}else {//顾问显示自己区域排名
				AdviserDTO adviserDTO = adviserService.getAdviserById(adminUserLoginInfo.getAdviserId());
				if (adviserDTO != null)
					regionIdList.add(adviserDTO.getRegionId());
				List<DataRankDTO> _list = RegionClassification.dataSplitByRegionId(dataList,regionIdList);
				return new DashboardResponse(0,"顾问", _list, startDate, endDate);
			}
		}
	}

	/**
	 * 上周业绩组成
	 * SUPER：全澳上周业绩组成
	 * MANAGER：管理区域上周业绩组成
	 * GW：上周自己的业绩组成
	 * @param request
	 * @param response
	 * @return
	 * @throws ServiceException
	 */
	@GetMapping(value = "/lastWeekPerformance")
	@ResponseBody
	public DashboardResponse lastWeekPerformance(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
		super.setGetHeader(response);
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo == null)
			return  new DashboardResponse(1,"未登录",null);
		List<Integer> regionIdList = new ArrayList<>();
		String startDate = DateClass.lastLastSaturday();//上上周六
		String endDate = DateClass.lastFriday();//上周五
		List<DataDTO> areaDataList = data.dataReport(startDate,endDate,"A",null); //全area地区的area数据   数据
		if (adminUserLoginInfo != null && ("GW".equalsIgnoreCase(adminUserLoginInfo.getApList()))){
			if (adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0){//顾问管理员
				List<RegionDTO> _regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
				regionIdList.add(adminUserLoginInfo.getRegionId());
				for (RegionDTO region : _regionList)
					regionIdList.add(region.getId());
				DataDTO _dto = new DataDTO();
				for (DataDTO dto : areaDataList){
					if (regionIdList.contains(dto.getRegionId())){
						_dto.setDate(dto.getDate());
						_dto.setServiceFee(dto.getServiceFee() + _dto.getServiceFee());
						_dto.setDeductionCommission(dto.getDeductionCommission() + _dto.getDeductionCommission());
						_dto.setClaimCommission(dto.getClaimCommission() + _dto.getClaimCommission());
						_dto.setClaimedCommission(dto.getClaimedCommission() + _dto.getClaimedCommission());
						_dto.setTotal(dto.getTotal() + _dto.getTotal());
					}
				}
				return new DashboardResponse(0,"管理区域上周业绩组成", _dto, startDate, endDate);
			}else {//普通顾问
				Integer adviserId = adminUserLoginInfo.getAdviserId();
				List<DataDTO> dataList = data.dataReport(startDate,endDate,"R",null); //全area地区的area数据   数据
				DataDTO dto = RegionClassification.adviserDateByAdviserId(dataList, adviserId);
				return new DashboardResponse(0,"自己上周业绩组成",dto, startDate, endDate);
			}
		}else if(adminUserLoginInfo != null && "SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())){
			return new DashboardResponse(0,"全澳-上周业绩组成",areaDataList, startDate, endDate);
		}
		return  new DashboardResponse(0,"",null,startDate,endDate);
	}

	/**
	 * 本月业绩组成
	 * SUPER：全澳本月业绩组成
	 * MANAGER：管理区域本月业绩组成
	 * GW：本月自己的业绩组成
	 * @param request
	 * @param response
	 * @return
	 * @throws ServiceException
	 */
	@GetMapping(value = "/thisMonthPerformance")
	@ResponseBody
	public DashboardResponse thisMonthPerformance(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
		super.setGetHeader(response);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		List<Integer> regionIdList = new ArrayList<>();
		String startDate = DateClass.thisMonthFirstDay(Calendar.getInstance());
		String endDate = sdf.format(Calendar.getInstance().getTime());
		List<DataDTO> areaDataList = data.dataReport(startDate,endDate,"A",null); //全area地区的area数据   数据
		if (adminUserLoginInfo != null && "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
			if (adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0) {//顾问管理员返回本地区业绩组成
				List<RegionDTO> _regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
				regionIdList.add(adminUserLoginInfo.getRegionId());
				for (RegionDTO region : _regionList)
					regionIdList.add(region.getId());
				DataDTO _dto = new DataDTO();
				for (DataDTO dto : areaDataList) {
					if (regionIdList.contains(dto.getRegionId())) {
						_dto.setDate(dto.getDate());
						_dto.setServiceFee(dto.getServiceFee() + _dto.getServiceFee());
						_dto.setDeductionCommission(dto.getDeductionCommission() + _dto.getDeductionCommission());
						_dto.setClaimCommission(dto.getClaimCommission() + _dto.getClaimCommission());
						_dto.setClaimedCommission(dto.getClaimedCommission() + _dto.getClaimedCommission());
						_dto.setTotal(dto.getTotal() + _dto.getTotal());
					}
				}
				return new DashboardResponse(0, "管理区域本月业绩组成", _dto, startDate, endDate);
			}else {
				Integer adviserId = adminUserLoginInfo.getAdviserId();
				List<DataDTO> dataList = data.dataReport(startDate,endDate,"R",null); //全area地区的area数据   数据
				DataDTO dto = RegionClassification.adviserDateByAdviserId(dataList, adviserId);
				return new DashboardResponse(0,"自己本月业绩组成",dto, startDate, endDate);
			}
		} else if (adminUserLoginInfo != null && "SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
			return new DashboardResponse(0, "全澳-本月业绩组成", areaDataList, startDate, endDate);
		}
		return new DashboardResponse(0, "", null, startDate, endDate);
	}

	/**
	 *本月业绩环比:该数据截止前一天，对比上个月每个业务类型业绩
	 */
	@GetMapping(value = "/thisMonthPerformanceRingRatio")
	@ResponseBody
	public DashboardResponse thisMonthRingRatio(HttpServletRequest request, HttpServletResponse response){
		super.setGetHeader(response);
		String thisMonthFirstDay = DateClass.thisMonthFirstDay(Calendar.getInstance());
		String today = DateClass.today();
		List<DataDTO> dataListThisMonth = data.dataReport(thisMonthFirstDay,today,"R",null);
		List<DataDTO> resultList = new ArrayList<>();
		DataDTO thisMonthData = new DataDTO();
		thisMonthData.setDate(today.substring(0,7));
		dataListThisMonth.forEach(dataDTO -> {
			//thisMonthData.setDate(dataDTO.getDate());
			thisMonthData.setServiceFee(thisMonthData.getServiceFee() + dataDTO.getServiceFee());
			thisMonthData.setClaimedCommission(thisMonthData.getClaimedCommission() + dataDTO.getClaimedCommission());
			thisMonthData.setClaimCommission(thisMonthData.getClaimCommission() + dataDTO.getClaimCommission());
			thisMonthData.setDeductionCommission(thisMonthData.getDeductionCommission() + dataDTO.getDeductionCommission());
		});
		String lastMonthFirstDay = DateClass.lastMonthFirstDay(Calendar.getInstance());
		String lastMonthEndDay = DateClass.lastMonthLastDay(Calendar.getInstance());
		List<DataDTO> dataListLastMonth = data.dataReport(lastMonthFirstDay,lastMonthEndDay,"R",null);
		DataDTO lastMonthData = new DataDTO();
		lastMonthData.setDate(lastMonthEndDay.substring(0,7));
		dataListLastMonth.forEach(dataDTO -> {
			lastMonthData.setDate(dataDTO.getDate());
			lastMonthData.setServiceFee(lastMonthData.getServiceFee() + dataDTO.getServiceFee());
			lastMonthData.setClaimedCommission(lastMonthData.getClaimedCommission() + dataDTO.getClaimedCommission());
			lastMonthData.setClaimCommission(lastMonthData.getClaimCommission() + dataDTO.getClaimCommission());
			lastMonthData.setDeductionCommission(lastMonthData.getDeductionCommission() + dataDTO.getDeductionCommission());
		});
		//dashboardService.thisMonthRingRatio();
		resultList.add(thisMonthData);
		resultList.add(lastMonthData);
		return new DashboardResponse(0,"success", resultList,
				StringUtil.toInt(thisMonthFirstDay.substring(5,7)) > 10 ? thisMonthFirstDay.substring(5,7) : thisMonthFirstDay.substring(6,7),
				StringUtil.toInt(lastMonthFirstDay.substring(5,7)) > 10 ? lastMonthFirstDay.substring(5,7) : lastMonthFirstDay.substring(6,7));
	}

	/**
	 *本月业绩与去年同比:当月 数据截止前一天，本月业绩总和对比去年同月业绩
	 */
	@GetMapping(value = "/thisMonthPerformanceYearOnYear")
	@ResponseBody
	public DashboardResponse thisMonthYearOnYear(HttpServletRequest request, HttpServletResponse response){
		super.setGetHeader(response);
		String thisMonthFirstDay = DateClass.thisMonthFirstDay(Calendar.getInstance());
		String today = DateClass.today();
		List<DataDTO> dataListThisMonth = data.dataReport(thisMonthFirstDay,today,"R",null);
		List<DataDTO> resultList = new ArrayList<>();
		DataDTO thisMonthData = new DataDTO();
		thisMonthData.setDate(today.substring(0,7));
		dataListThisMonth.forEach(dataDTO -> {
			thisMonthData.setDate(dataDTO.getDate());
			thisMonthData.setServiceFee(thisMonthData.getServiceFee() + dataDTO.getServiceFee());
			thisMonthData.setClaimedCommission(thisMonthData.getClaimedCommission() + dataDTO.getClaimedCommission());
			thisMonthData.setClaimCommission(thisMonthData.getClaimCommission() + dataDTO.getClaimCommission());
			thisMonthData.setDeductionCommission(thisMonthData.getDeductionCommission() + dataDTO.getDeductionCommission());
		});
		String lastYearThisMonthFirstDay = DateClass.lastYearThisMonthFirstDay(Calendar.getInstance());
		String lastYearThisMonthLastDay = DateClass.lastYearThisMonthLastDay();
		List<DataDTO> dataListLastMonth = data.dataReport(lastYearThisMonthFirstDay,lastYearThisMonthLastDay,"R",null);
		DataDTO lastYearThisMonthData = new DataDTO();
		lastYearThisMonthData.setDate(lastYearThisMonthLastDay.substring(0,7));
		dataListLastMonth.forEach(dataDTO -> {
			lastYearThisMonthData.setDate(dataDTO.getDate());
			lastYearThisMonthData.setServiceFee(lastYearThisMonthData.getServiceFee() + dataDTO.getServiceFee());
			lastYearThisMonthData.setClaimedCommission(lastYearThisMonthData.getClaimedCommission() + dataDTO.getClaimedCommission());
			lastYearThisMonthData.setClaimCommission(lastYearThisMonthData.getClaimCommission() + dataDTO.getClaimCommission());
			lastYearThisMonthData.setDeductionCommission(lastYearThisMonthData.getDeductionCommission() + dataDTO.getDeductionCommission());
		});
		//dashboardService.thisMonthRingRatio();
		resultList.add(thisMonthData);
		resultList.add(lastYearThisMonthData);
		return new DashboardResponse(0,"success", resultList,
				StringUtil.toInt(thisMonthFirstDay.substring(5,7)) > 10 ? thisMonthFirstDay.substring(5,7) : thisMonthFirstDay.substring(6,7),
				StringUtil.toInt(lastYearThisMonthFirstDay.substring(5,7)) > 10 ?
						lastYearThisMonthFirstDay.substring(5,7) : lastYearThisMonthFirstDay.substring(6,7));
	}

	/**
	 * SUPER：全澳地区全年累计业绩排名
	 * MANAGER：本地区全年累计业绩排名
	 * GW：本地区全年累计业绩排名
	 * @param request
	 * @param response
	 * @return
	 */
	@GetMapping(value = "/allYearPerformanceRankDiffAp")
	@ResponseBody
	public DashboardResponse allYearPerformanceRankDiffAp(HttpServletRequest request, HttpServletResponse response) throws Exception {
		super.setGetHeader(response);
		AdminUserLoginInfo loginInfo = getAdminUserLoginInfo(request);
		if (loginInfo == null)
			return  new DashboardResponse(1,"未登录");
		String thisYearFirstDay = DateClass.thisYearFirstDay();
		String today = DateClass.today();
		List<DataDTO> dataList = data.dataReport(thisYearFirstDay,today,"R","Y");
		List<Integer> regionIdList = new ArrayList<>();
		if ("SUPERAD".equalsIgnoreCase(loginInfo.getApList())){
			return new DashboardResponse(0,"success",RegionClassification.dataSplitByRegionId(dataList,regionIdList), thisYearFirstDay, today);
		}else if ("GW".equalsIgnoreCase(loginInfo.getApList())){
			if (loginInfo.getRegionId() != null && loginInfo.getRegionId() > 0){//顾问管理员
				List<RegionDTO> _regionList = regionService.listRegion(loginInfo.getRegionId());
				regionIdList.add(loginInfo.getRegionId());
				for (RegionDTO region : _regionList)
					regionIdList.add(region.getId());
				List<DataRankDTO> _list = RegionClassification.dataSplitByRegionId(dataList,regionIdList);
				return new DashboardResponse(0,"顾问管理区域排名", _list, thisYearFirstDay, today);
			}else {
				AdviserDTO adviserDTO = adviserService.getAdviserById(loginInfo.getAdviserId());
				if (adviserDTO != null)
					regionIdList.add(adviserDTO.getRegionId());
				List<DataRankDTO> _list = RegionClassification.dataSplitByRegionId(dataList,regionIdList);
				return new DashboardResponse(0,"顾问所属区域排名", _list, thisYearFirstDay, today);
			}
		}
		return  new DashboardResponse(0,"");
	}

	/**
	 *全澳全年累计业绩排名
	 * @throws ServiceException
	 */
	@GetMapping(value = "/allYearPerformanceRank")
	@ResponseBody
	public DashboardResponse allYearPerformanceRank(HttpServletRequest request, HttpServletResponse response) throws Exception {
		super.setGetHeader(response);
		String thisYearFirstDay = DateClass.thisYearFirstDay();
		String today = DateClass.today();
		List<DataDTO> dataList = data.dataReport(thisYearFirstDay,today,"R","Y");
		return new DashboardResponse(0,"success", RegionClassification.dataSplitByRegionId(dataList,null), thisYearFirstDay, today);
	}

	/**
	 * SUPER:全澳全年业绩总和
	 * MANAGER：本地区全年业绩总和
	 * GW：自己全年业绩总和
	 * @return
	 * @throws ServiceException
	 */
	@GetMapping(value = "/allYearPerformanceTotal")
	@ResponseBody
	public Response allYearPerformanceTotal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		super.setGetHeader(response);
		AdminUserLoginInfo loginInfo = getAdminUserLoginInfo(request);
		if (loginInfo == null)
			return  new Response(1,"未登录");
		String thisYearFirstDay = DateClass.thisYearFirstDay();
		String today = DateClass.today();
		List<DataDTO> dataList = data.dataReport(thisYearFirstDay,today,"R","Y");
		List<Integer> regionIdList = new ArrayList<>();
		double total = 0;
		if ("SUPERAD".equalsIgnoreCase(loginInfo.getApList())){
			for (DataDTO dataDTO : dataList){
				total = dataDTO.getTotal() + total;
			}
			return new Response(0,"全澳全年业绩总和", total);
		}else if ("GW".equalsIgnoreCase(loginInfo.getApList())){
			if (loginInfo.getRegionId() != null && loginInfo.getRegionId() > 0){//顾问管理员
				Integer adviserId = loginInfo.getAdviserId();
				double _total = 0;
				List<RegionDTO> _regionList = regionService.listRegion(loginInfo.getRegionId());
				regionIdList.add(loginInfo.getRegionId());
				for (RegionDTO region : _regionList)
					regionIdList.add(region.getId());
				List<DataRankDTO> _list = RegionClassification.dataSplitByRegionId(dataList,regionIdList);
				for (DataDTO dataDTO : _list){
					total = dataDTO.getTotal() + total;
					if (dataDTO.getAdviserId() == adviserId)
						_total = dataDTO.getTotal();
				}
				Map map = new HashMap();
				map.put("total", total);
				map.put("managerTotal", _total);
				return new Response(0,"本地区全年累计业绩总和/MANAGER自己全年业绩总和", map);
			}else {
				Integer adviserId = loginInfo.getAdviserId();
				int i = 0, size = dataList.size();
				for ( ; i < size ; i++ ){
					if (adviserId == dataList.get(i).getAdviserId()){
						total = dataList.get(i).getTotal();
						break;
					}
				}
				Map map = new HashMap();
				map.put("total", total);
				map.put("RANK", i+1);
				return new Response(0,"顾问全年业绩总和", map);
			}
		}
		return  new Response(0,"");
	}

	/**
	 * user/count 接口在顾问管理员是返回本地区所有顾问下的所有客户总和/顾问返回自己的客户总和
	 * 此接口返回：顾问管理员自己的客户总数/superad返回所有客户总数
	 * @return
	 */
	@GetMapping(value = "/countUser")
	@ResponseBody
	public Response countUser(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
		super.setGetHeader(response);
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo != null && "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
				&& adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0) {
			Integer adviserId = adminUserLoginInfo.getAdviserId();
			int count = userService.countUser(null, null, null, null, null,
					adviserId, null, 0);
			return new Response(0, count);
		}
		if (adminUserLoginInfo != null && "SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())){
			int count = userService.countUser(null, null, null, null, null,
					0, null, 0);
			return new Response(0, count);
		}
		return new Response(0, 0);
	}

	/**
	 * /user/countMonth 返回顾问或者顾问管理员自己本月新客户
	 * 此接口返回：顾问管理员地区下所有顾问的本月新增客户总数/superad返回本月所有顾问新增客户总数
	 * @throws ServiceException
	 */
	@GetMapping(value = "/countMonth")
	@ResponseBody
	public Response countMonth(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
		super.setGetHeader(response);
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo != null && "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
				&& adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0) {
			List<Integer> regionIdList = ListUtil.buildArrayList(adminUserLoginInfo.getRegionId());
			List<RegionDTO> regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
			for (RegionDTO region : regionList)
				regionIdList.add(region.getId());
			return new Response(0, userService.countUserByThisMonth(null, regionIdList));
		}
		if (adminUserLoginInfo != null && "SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())){
			int count = userService.countUserByThisMonth(null, null);
			return new Response(0, count);
		}
		return new Response(0, 0);
	}

	/**
	 * /dashboard/getMonthExpectAmount 返回顾问管理员/顾问自己的本月预收业绩
	 * 此接口返回：顾问管理员地区下所有顾问的本月预收业绩/superad返回所有顾问本月预收业绩
	 * @throws ServiceException
	 */
	@GetMapping(value = "/getMonthExpectAmount2")
	@ResponseBody
	public Response getMonthExpectAmount2(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
		super.setGetHeader(response);
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo != null && "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
				&& adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0) {
			List<Integer> regionIdList = ListUtil.buildArrayList(adminUserLoginInfo.getRegionId());
			List<RegionDTO> regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
			for (RegionDTO region : regionList)
				regionIdList.add(region.getId());
			return new Response(0, dashboardService.getThisMonthExpectAmount(null, regionIdList));
		}
		if (adminUserLoginInfo != null && "SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())){
			return new Response(0, dashboardService.getThisMonthExpectAmount(null, null));
		}
		return new Response(0, 0);
	}
}
