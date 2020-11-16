/*
*
* Description: 指南针公共函数文件
* Author: Dmh
* Create Date: 2019-12-15
* Modify Date: 2020-02-20
*/

function return_servicename(va)
{
   switch(va)
   {
   		case 'VISA':
   					return '签证';
   		break;
   		case 'OVST':
   					return '留学';
   		break;
   }
}

function visastate_wa(va)
{
		var str_html = '';
		switch(va)
		{
			case 'WAIT':
						str_html = '待提交审核';
			break;
			case 'REVIEW':
						str_html = '<label style="color:blue;">资料审核中</label>';
			break;
			case 'FINISH':
						str_html = '<label style="color:blue;">资料已审核</label>';
			break;
			case 'MA_FINISH':
						str_html = '<label style="color:blue;">已审核</label>';
			break;
			case 'APPLY':
						str_html = '<label style="color:blue;">服务申请中</label>';
			break;
			case 'COMPLETE':
						str_html = '<label style="color:red;">申请成功</label>';
			break;
			case 'COE_COMPLETE':
						str_html = '<label style="color:red;">申请成功</label><br/>COE待支付';
			break;
			case 'PAID':
						str_html = '<label style="color:red;">COE支付成功</label>';
			break;
			case 'CLOSE':
						str_html = '<label style="color:red;">已关闭</label>';
			break;						
		}
		return str_html;
}