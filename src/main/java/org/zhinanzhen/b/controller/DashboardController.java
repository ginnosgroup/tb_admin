package org.zhinanzhen.b.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ikasoa.core.utils.ListUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zhinanzhen.b.service.CommissionOrderService;
import org.zhinanzhen.b.service.DashboardService;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.CommissionOrderListDTO;
import org.zhinanzhen.b.service.pojo.DataDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.scheduled.Data;
import org.zhinanzhen.tb.scheduled.DateClass;
import org.zhinanzhen.tb.service.RegionService;
import org.zhinanzhen.tb.service.ServiceException;
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

	@RequestMapping(value = "/getMonthExpectAmount", method = RequestMethod.GET)
	@ResponseBody
	public Response<Double> getMonthExpectAmount(HttpServletRequest request, HttpServletResponse response) {
		try {
			if (getAdminUserLoginInfo(request) == null)
				return new Response<Double>(1, "请先登录!", null);
			return new Response<Double>(0, dashboardService.getThisMonthExpectAmount(getAdviserId(request)));
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
	 * 全澳顾问业绩排名
	 * @param request
	 * @param response
	 * @return
	 * @throws ServiceException
	 */
	@GetMapping(value = "/allRegionThisMonthPerformanceRank")
	@ResponseBody
	public Response allRegionThisMonthPerformanceRank(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
		super.setGetHeader(response);
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		//List<Integer> regionIdList = new ArrayList<>();
		if (adminUserLoginInfo == null || !(("GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
				&& adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0)
				|| "SUPER".equalsIgnoreCase(adminUserLoginInfo.getApList())))
			return new Response(1,"No permission");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String startDate = DateClass.thisMonthFirstDay(Calendar.getInstance());
		String endDate = sdf.format(Calendar.getInstance().getTime());

		//下面的数据是月初到当前的
		//顾问排名.也是全部数据。顾问id,月份分组数据
		List<DataDTO> dataList = data.dataReport(startDate,endDate,"R"); //  R 全area顾问倒序排名的数据  顾问
		//dataList数据中顾问是相同的地区合并数据
		//List<DataDTO> areaTodayDataList = data.dataReport(startDate,endDate,"A"); //   A  全area地区的area数据   数据
		//dataList数据中顾问是相同的地区就添加到一个List<DataDTO>里面。一个area包含此area所有顾问的数据
		//List<List<DataDTO>> regionList = RegionClassification.classification(dataList);//  按照地区将顾问进行分组


		return  new Response(0,"全澳本月累计业绩排名",dataList);
	}

	/**
	 * 上周业绩组成
	 * @param request
	 * @param response
	 * @return
	 * @throws ServiceException
	 */
	@GetMapping(value = "/lastWeekPerformance")
	@ResponseBody
	public Response lastWeekPerformance(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
		super.setGetHeader(response);
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		List<Integer> regionIdList = new ArrayList<>();
		String startDate = DateClass.lastLastSaturday();//上上周六
		String endDate = DateClass.lastFriday();//上周五
		List<DataDTO> areaDataList = data.dataReport(startDate,endDate,"A"); //全area地区的area数据   数据
		if (adminUserLoginInfo != null || ("GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
				&& adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0)){//顾问管理员返回本地区业绩组成
			List<RegionDTO> _regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
			regionIdList.add(adminUserLoginInfo.getRegionId());
			for (RegionDTO region : _regionList)
				regionIdList.add(region.getId());
			for (DataDTO dto : areaDataList){
				if (regionIdList.contains(dto.getRegionId()))
					return new Response(0,"上周业绩组成",dto);
			}
		}else if(adminUserLoginInfo != null || "SUPER".equalsIgnoreCase(adminUserLoginInfo.getApList())){
			return new Response(0,"全澳-上周业绩组成",areaDataList);
		}
		return  new Response(0,"",null);
	}

	/**
	 * 本月一号到现在的业绩组成
	 * @param request
	 * @param response
	 * @return
	 * @throws ServiceException
	 */
	@GetMapping(value = "/thisMonthPerformance")
	@ResponseBody
	public Response thisMonthPerformance(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
		super.setGetHeader(response);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		List<Integer> regionIdList = new ArrayList<>();
		String startDate = DateClass.thisMonthFirstDay(Calendar.getInstance());//上上周六
		String endDate = sdf.format(Calendar.getInstance().getTime());//上周五
		List<DataDTO> areaDataList = data.dataReport(startDate,endDate,"A"); //全area地区的area数据   数据
		if (adminUserLoginInfo != null || ("GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
				&& adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0)){//顾问管理员返回本地区业绩组成
			List<RegionDTO> _regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
			regionIdList.add(adminUserLoginInfo.getRegionId());
			for (RegionDTO region : _regionList)
				regionIdList.add(region.getId());
			for (DataDTO dto : areaDataList){
				if (regionIdList.contains(dto.getRegionId()))
					return new Response(0,"上周业绩组成",dto);
			}
		}else if(adminUserLoginInfo != null || "SUPER".equalsIgnoreCase(adminUserLoginInfo.getApList())){
			return new Response(0,"全澳-上周业绩组成",areaDataList);
		}
		return  new Response(0,"",null);
	}
}
